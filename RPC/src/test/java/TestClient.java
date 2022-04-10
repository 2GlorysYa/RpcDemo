import Client.client.RpcClientProxy;
import Server.Service.HelloObjectA;
import Server.Service.ServiceA;

public class TestClient {

    public static void main(String[] args) {
        // 指定RPC服务器的地址
        RpcClientProxy proxy = new RpcClientProxy("127.0.0.1", 9000);
        // 传入要代理的接口，注意这个helloService其实是服务端侧的服务对象
        ServiceA serviceA = proxy.getProxy(ServiceA.class);
        HelloObjectA object = new HelloObjectA(12, "This is a message");
        // 调用代理对象, 他会帮我们向服务端发出请求并获得结果
        // 结果即hello方法的返回值，也就是一个String
        String res = serviceA.hello(object);
        System.out.println(res);
    }
}
