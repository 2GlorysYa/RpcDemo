package Client.handler;

import Server.response.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    /**
     * 这里只需要处理收到的消息，即 RpcResponse 对象，由于前面已经有解码器解码了
     * 这里就直接将返回的结果放入 ctx 中即可
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        try {
            logger.info(String.format("客户端接收到消息: %s", msg));
            // 生成一个AttributeKey
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
            // 把这个key set进Attribute
            // 所以等于说不是直接取出数据，而是在这里把响应数据set到属性里，然后在NettyClient里取出
            ctx.channel().attr(key).set(msg);
            ctx.channel().close();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 处理InboundHandler异常
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("过程调用中发生了错误: ");
        cause.printStackTrace();
        ctx.close();
    }
}
