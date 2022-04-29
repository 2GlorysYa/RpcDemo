import server.service.Impl.ServiceAImpl;
import server.service.ServiceA;
import common.config.RpcException;
import server.NettyServer;
import server.registry.Impl.DefaultServiceProvider;
import server.registry.ServiceProvider;

public class NettyTestServer1 {
    public static void main(String[] args) throws RpcException {
        ServiceA serviceA1 = new ServiceAImpl(); // 创建服务
        // ServiceA serviceA2 = new ServiceAImpl();
        ServiceProvider registry = new DefaultServiceProvider();    // 创建注册中心
        registry.register(serviceA1);    // 注册服务
        NettyServer server = new NettyServer(registry, 10000); // 将注册中心绑定到服务器
        // 注册服务到本地及ZooKeeper
        server.publishService(serviceA1, ServiceA.class);
        // server.publishService(serviceA2, ServiceA.class);
        server.start(); // 启动服务器
    }
}
