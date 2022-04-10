package Server.registry.Impl;

import Client.RpcClient;
import Client.lb.Impl.ConsistentHashLoadBalancer;
import Client.lb.Impl.RandomLoadBalancer;
import Client.lb.Impl.RoundRobinLoadBalancer;
import Client.lb.LoadBalancer;
import Server.registry.ServiceRegistry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZkServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegistry.class);
    // TODO  优化硬编码
    private static final String SERVER_ADDR = "127.0.0.1:2181"; // zookeeper默认端口
    private static final String REGISTRY_PATH = "/registry";
    private static final int SESSION_TIMEOUT = 5000;
    private ZooKeeper zk;
    private LoadBalancer loadBalancer;
    private RpcClient rpcClient;

    /**
     * 服务端使用
     */
    public ZkServiceRegistry() {
        initializeZooKeeper();
    };

    /**
     * 客户端使用
     * 传入负载均衡策略
     */
    public ZkServiceRegistry(RpcClient rpcClient) {
        // TODO loadbalancer, zookeeper, ZksServiceRegistry三者耦合严重，所以没有直接传lb，需要想办法处理
        // 如果传入一个null，就默认是轮询
        // if (loadBalancer == null) {
        //     this.loadBalancer = new RoundRobinLoadBalancer();
        // } else {
        //     this.loadBalancer = loadBalancer;
        // }
        this.rpcClient = rpcClient;
        initializeZooKeeper();
    }

    /**
     * 初始化zooKeeper并建立连接
     */
    public void initializeZooKeeper() {
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            // 构建zookeeper，传入地址，timeout，监视器
            // 这个监视器用于监视与zookeeper服务器的连接是否成功，判断标准为 事件 == 枚举类syncConnected
            zk = new ZooKeeper(SERVER_ADDR, SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        logger.info("zk连接创建成功");
                        countDownLatch.countDown();
                    }
                }
            });
            // 使用CountDownLatch阻塞直到zookeeper连接完成
            countDownLatch.await();
        } catch (Exception e) {
            logger.error("zk连接创建失败", e);
        }
    }

    /**
     * 将服务注册到zookeeper，【为服务端侧调用】
     * 1. 注册服务名为根节点下的持久化节点
     * 2. 注册服务对应的服务器地址为服务名节点下的临时节点
     * 每次服务重启时会清空临时节点（分布式服务器），并重新注册临时节点
     *
     * 节点路径为 /registry/serviceName/address-
     *
     * 缺陷：目前服务端一次只能注册一个服务，如需注册多个，就得多调几次register
     */
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        String registryPath = REGISTRY_PATH;
        String host = inetSocketAddress.getHostName();
        int port = inetSocketAddress.getPort();
        // 将服务地址信息（IP : port）转换为字符串存储到zookeeper
        String serviceAddress = String.format("%s:%d", host, port);

        try {
            // 创建持久根节点，节点名为registry
            if (zk.exists(registryPath, false) == null) {
                zk.create(registryPath,null,ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                logger.info("创建注册表节点:{}", registryPath);
            }
            // 创建持久子节点，节点名为服务名
            // 注意zookeeper只支持绝对路径，因此这个子节点的path必须带上父节点的path
            String servicePath = registryPath + "/" + serviceName;  // registry/service_Name
            if (zk.exists(servicePath, false) == null) {
                zk.create(servicePath,null, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
                logger.info("创建服务节点:{}", servicePath);
            }
            // 创建临时有序子节点，节点名为服务地址
            // 这个节点也必须带上完整的父节点path
            String addressPath = servicePath + "/address-";
            // 地址节点名
            String addressNode = zk.create(addressPath, serviceAddress.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
            logger.info("创建地址节点:{} -> {}", addressNode, serviceAddress);
        } catch (KeeperException | InterruptedException e) {
            logger.error("zookeeper注册服务时发生故障", e);
            System.exit(0);
        }
    }

    /**
     * 本地缓存向zookeeper查询服务列表
     */
    @Override
    public List<String> getServiceList(String serviceName) {
        List<String> addressList = null;
        try {
            String address;
            String addressNode;
            String registryPath = REGISTRY_PATH;
            String servicePath = registryPath + "/" + serviceName;
            // 拿到zk临时节点的path
            addressList = zk.getChildren(servicePath, true);
            if (CollectionUtils.isEmpty(addressList)) {
                throw new RuntimeException(String.format(">>>无法在此路径上找到任何地址 {}", servicePath));
            }
        } catch (Exception e) {
            logger.error("本地缓存获取服务列表失败 ", e);
        }
        return addressList;
    }

    /**
     * 客户端通过待调用的服务名来查找该服务对应的服务器地址，【为客户端侧调用】
     * 使用负载均衡获取
     */
    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            String address;
            String addressNode;
            String registryPath = REGISTRY_PATH;
            String servicePath = registryPath + "/" + serviceName;
            // 拿到zk临时节点的path
            List<String> addressList = zk.getChildren(servicePath, true);
            if (CollectionUtils.isEmpty(addressList)) {
                throw new RuntimeException(String.format(">>>无法在此路径上找到任何地址 {}", servicePath));
            }

            loadBalancer = new ConsistentHashLoadBalancer(zk, servicePath);

            // 如果是随机或轮询
            if (loadBalancer instanceof RandomLoadBalancer || loadBalancer instanceof RoundRobinLoadBalancer) {
                // 获取zk临时节点中第一个节点的path
                // addressNode = addressList.get(0);
                // 负载均衡获取服务地址
                addressNode = loadBalancer.select(addressList);
                logger.info("负载均衡获得地址节点: {}", addressNode);
                // 获取临时节点中的数据
                // 将节点中的bytes数据转换为String
                address = new String(zk.getData(servicePath + "/" + addressNode, true, new Stat()));
                logger.info("服务地址为 {}", address);
                // 分离IP和端口
                String[] split = address.split(":");
                // 返回待请求的服务器地址
                return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
            } else {
                // 一致性哈希负载均衡
                // TODO hashcode需要重新设计，服务器是用地址path来计算而不是ip，因为都在本机上，所以hashcode太过于相近
                // TODO 导致客户端的hashcode总是处于环的最左或最右
                addressNode = loadBalancer.select(addressList, rpcClient.hashCode());
                logger.info("一致性哈希负载均衡获得地址节点路径: {}", addressNode);
                address = new String(zk.getData(servicePath + "/" + addressNode, true, new Stat()));
                logger.info("服务地址为 {}", address);
                String[] split = address.split(":");
                return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
            }
        } catch (Exception e) {
            logger.error("获取服务时发生错误:", e);
            System.exit(0);
        }
        return null;
    }
}
