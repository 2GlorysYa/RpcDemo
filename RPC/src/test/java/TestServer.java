import Server.config.RpcException;
import Server.registry.Impl.DefaultServiceProvider;
import Server.registry.ServiceProvider;
import Server.server.RpcServer;
import Server.Service.ServiceA;
import Server.Service.Impl.ServiceAImpl;

public class TestServer {
    public static void main(String[] args) throws RpcException {
        ServiceA serviceA = new ServiceAImpl();
        ServiceProvider serviceProvider = new DefaultServiceProvider();
        serviceProvider.register(serviceA);
        RpcServer rpcServer = new RpcServer(serviceProvider);
        rpcServer.start(9000);
    }
}
