import Server.NettyServer;
import Server.Service.Impl.ServiceAImpl;
import Server.Service.ServiceA;
import Server.config.RpcException;
import Server.registry.Impl.DefaultServiceProvider;
import Server.registry.ServiceProvider;

public class NettyTestServer5 {
    public static void main(String[] args) throws RpcException {
        ServiceA serviceA3 = new ServiceAImpl(); // 创建服务
        // ServiceB serviceB3 = new ServiceBImpl();
        ServiceProvider registry = new DefaultServiceProvider();    // 创建注册中心
        registry.register(serviceA3);    // 注册服务
        NettyServer server = new NettyServer(registry, 14000); // 将注册中心绑定到服务器
        // 注册服务到本地及ZooKeeper
        server.publishService(serviceA3, ServiceA.class);
        // server.publishService(serviceB3, ServiceB.class);
        server.start(); // 启动服务器
    }
}
