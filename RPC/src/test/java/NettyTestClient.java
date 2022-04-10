import Client.NettyClientV2;
import Client.RpcClient;
import Client.cache.ServerDiscoveryCache;
import Client.client.RpcClientProxy;
import Server.Service.HelloObjectA;
import Server.Service.ServiceA;

public class NettyTestClient {
    public static void main(String[] args) {
        // RpcClient client = new NettyClientV2("127.0.0.1", 9999);
        RpcClient client = new NettyClientV2();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloObjectA object = new HelloObjectA(12, "this is netty style");
        // 这里发现实际代理的是服务对象
        ServiceA serviceA = rpcClientProxy.getProxy(ServiceA.class);
        // 调用代理对象的hello方法，反射调用invoke
        String res = serviceA.hello(object);
        System.out.println(res);
    }
}
