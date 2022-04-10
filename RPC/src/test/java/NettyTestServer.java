import Server.Service.Impl.ServiceAImpl;
import Server.Service.ServiceA;
import Server.config.RpcException;
import Server.NettyServer;
import Server.registry.Impl.DefaultServiceProvider;
import Server.registry.ServiceProvider;

public class NettyTestServer{
    public static void main(String[] args) throws RpcException {
        ServiceA serviceA = new ServiceAImpl(); // 创建服务
        ServiceProvider registry = new DefaultServiceProvider();    // 创建注册中心
        registry.register(serviceA);    // 注册服务
        NettyServer server = new NettyServer(registry, 9999); // 将注册中心绑定到服务器
        // 注册服务到本地及ZooKeeper
        server.publishService(serviceA, ServiceA.class);
        server.start(); // 启动服务器
    }
}
