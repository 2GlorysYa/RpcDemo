package Client.lb.Impl;

import Client.RpcClient;
import Client.lb.LoadBalancer;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConsistentHashLoadBalancer implements LoadBalancer {

    private static final Logger logger = LoggerFactory.getLogger(ConsistentHashLoadBalancer.class);

    /**
     * 引入5个虚拟节点到hash环
     */
    private static final int VIRTUAL_NODE_SIZE = 5;


    /**
     * 虚拟节点分隔符，比如NODE A#1, NODE A#2
     */
    private static final String VIRTUAL_NODE_SPLIT = "#";

    private ZooKeeper zk;

    private String servicePath;

    public ConsistentHashLoadBalancer(ZooKeeper zk, String servicePath) {
        this.zk = zk;
        this.servicePath = servicePath;
    }

    /**
     * @param addressList 需要负载均衡的服务列表
     * 哈希一致性算法, 传入客户端的hashcode
     * 返回zookeeper对应的【节点路径】
     */
    @Override
    public String select(List<String> addressList, int hashCode) {
        TreeMap<Integer, String> ring = makeConsistentHashRing(addressList);
        return allocateNode(ring,hashCode);
    }

    /**
     * @param ring  hash环
     * @param hashCode  客户端的hashcode
     * 返回zookeeper对应的节点（名）
     */
    private String allocateNode(TreeMap<Integer, String> ring, int hashCode) {
        // 取大于等于该key的第一个节点，如果没有，就取hash环中第一个节点
        Map.Entry<Integer, String> node = ring.ceilingEntry(hashCode);
        if (node == null) {
            node = ring.firstEntry();
        }
        return node.getValue();
    }

    /**
     * 生成一个hash环
     */
    private TreeMap<Integer, String> makeConsistentHashRing(List<String> addressList) {
        TreeMap<Integer, String> ring = new TreeMap<>();
        // 对每个物理服务节点都构建5个虚拟节点, 并绑定在一起
        for (String singleServer : addressList) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                // key为每个虚拟节点节点对应的hashcode，value为物理节点
                ring.put((buildVirtualNode(singleServer) + "#" + i).hashCode(), singleServer);
            }
        }
        return ring;
    }

    /**
     * 使用每个服务地址的ip和port构建一个地址串ip:port作为hash节点
     */
    private String buildVirtualNode(String singleServer) {
        try {
            String address = new String(zk.getData(servicePath + "/" + singleServer, true, new Stat()));
        } catch (Exception e) {
            logger.error("构建hash环时出现错误 ", e);
        }
        return singleServer;
    }

    @Override
    public String select(List<String> addressList) {
        return null;
    }
}
