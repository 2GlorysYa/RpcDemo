import common.config.RpcException;
import server.registry.Impl.DefaultServiceProvider;
import server.registry.ServiceProvider;
import server.server.RpcServer;
import server.service.ServiceA;
import server.service.Impl.ServiceAImpl;

public class TestServer {
    public static void main(String[] args) throws RpcException {
        ServiceA serviceA = new ServiceAImpl();
        ServiceProvider serviceProvider = new DefaultServiceProvider();
        serviceProvider.register(serviceA);
        RpcServer rpcServer = new RpcServer(serviceProvider);
        rpcServer.start(9000);
    }
}
