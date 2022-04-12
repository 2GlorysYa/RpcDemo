import Client.NettyClientV2;
import Client.RpcClient;
import Client.client.RpcClientProxy;
import Server.Service.HelloObjectA;
import Server.Service.ServiceA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyTestClient1 {
    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(NettyTestClient1.class);

        // RpcClient client = new NettyClientV2("127.0.0.1", 9999);
        RpcClient client = new NettyClientV2();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloObjectA object = new HelloObjectA(12, "this is netty style");
        // 这里发现实际代理的是服务对象
        // ServiceA serviceA = rpcClientProxy.getProxy(ServiceA.class);
        // 调用代理对象的hello方法，反射调用invoke
        // try {
        //     for (int i = 0; i < 10; i++) {
                ServiceA serviceA = rpcClientProxy.getProxy(ServiceA.class);
                String res = serviceA.hello(object);
                System.out.println(res);
                // Thread.sleep(3000);
            // }
        // } catch (InterruptedException e) {
        //     logger.error("调用失败: {}", e);
        // }
    }
}
