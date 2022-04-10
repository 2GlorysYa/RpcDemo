package Client.client;

import Client.reqeust.RpcRequest;
import Server.response.RpcResponse;
import Client.RpcClient;
import Server.serializer.KryoSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

// RPC客户端的实现，动态代理
// 客户端没有接口的实现类，那么使用JDK动态代理来生成实例
public class RpcClientProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);

    // 传递host和port来指定服务器的地址
    private String host;
    private int port;
    private RpcClient rpcClient;

    public RpcClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RpcClientProxy(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    // 抑制编译器产生警告
    // 生成代理对象
    @SuppressWarnings("unchecked")
    public <T> T getProxy (Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class<?>[]{clazz},
                this);
    }

    // 重写的invocation handler中的invoke方法
    // 可以看到，当调用动态代理对象上的hello方法时，底层就是执行了invoke方法
    // 使得这个动态代理对象【替我们】向RPC服务端发出了请求，并拿到了RPC响应对象
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.info("调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());
        // 建造者模式构建对象
        // RpcRequest rpcRequest = RpcRequest.builder()
        //         .interfaceName(method.getDeclaringClass().getName())
        //         .methodName(method.getName())
        //         .parameters(args)
        //         .paramTypes(method.getParameterTypes())
        //         .build();
        // RpcClient rpcClient = new NettyClient(); 构造器已经弄好了，别new新的，没有port和hostname
        // return ((RpcResponse) rpcClient.sendRequest(rpcRequest)).getData();
        RpcRequest rpcRequest = new RpcRequest(method.getDeclaringClass().getName(),
                method.getName(), args, method.getParameterTypes());
        // return rpcClient.sendRequest(rpcRequest);
        return rpcClient.sendRequest1(rpcRequest,new KryoSerializer());
    }
}
