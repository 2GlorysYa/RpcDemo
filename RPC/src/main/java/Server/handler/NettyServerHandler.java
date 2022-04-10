package Server.handler;

import Client.reqeust.RpcRequest;
import Server.config.RpcException;
import Server.registry.ServiceProvider;
import Server.response.RpcResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 接收RpcRequest，并执行接口的方法调用，再将调用结果返回，封装成RpcResponse发出去
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static RequestHandler requestHandler;
    private ServiceProvider serviceProvider;

    private static ExecutorService executorService;

    public NettyServerHandler (ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    static {
        requestHandler = new RequestHandler();
        executorService = Executors.newFixedThreadPool(1);
    }

    /**
     * 如果channelRead中执行业务逻辑过久，会阻塞整个worker线程，因为channelHandler链的整个流程是同步的
     * 因此引入线程池来异步执行业务逻辑，避免阻塞
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        executorService.submit(() -> {
            try {
                logger.info("服务端接收到请求: {}", msg);
                String interfaceName = msg.getInterfaceName();
                // 去注册中心拉取服务实例
                Object service = serviceProvider.getService(interfaceName);
                // 执行方法调用并获得调用结果
                Object result = requestHandler.handle(msg, service);
                // 将响应结果写入ChannelHandler上下文
                ChannelFuture future = ctx.writeAndFlush(RpcResponse.success(result));
                // ChannelFuture future = ctx.writeAndFlush();
                // 使用监听器监测数据包是否已发出，再关闭通道
                future.addListener(ChannelFutureListener.CLOSE);
            } catch (RpcException e) {
                logger.error("拉取服务时出现异常", e);
            } finally {
                // 引用计数-1，释放内存
                ReferenceCountUtil.release(msg);
            }
        });
    }

    /**
     * 可以用来捕获InboundHandler的异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("处理过程调用时发生了错误: ");
        cause.printStackTrace();
        ctx.close();
    }
}
