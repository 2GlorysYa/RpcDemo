package Server.handler;

import Client.reqeust.RpcRequest;
import Server.config.RpcException;
import Server.response.RpcResponse;
import Server.registry.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// 请求处理线程, 用于RpcServer，但现在使用的NettyServer，所以简单看看就行
// 他获取rpc请求对象以及从registry里获取服务对象，把两个对象交给handler处理
public class RequestHandlerThread implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerThread.class);

    private Socket socket;
    private RequestHandler requestHandler;
    private ServiceProvider serviceProvider;

    public RequestHandlerThread(Socket socket, RequestHandler requestHandler, ServiceProvider serviceProvider) {
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            // 获取要调用的服务实例
            Object service = serviceProvider.getService(rpcRequest.getInterfaceName());
            // 调用并将结果封装到Response对象中
            Object result = requestHandler.handle(rpcRequest, service);
            objectOutputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            objectOutputStream.flush(); // 强制清空缓存区，将数据输出
        } catch (IOException | RpcException | ClassNotFoundException e) {
            logger.error("调用或发送是出现错误: ", e);
        }
    }
}
