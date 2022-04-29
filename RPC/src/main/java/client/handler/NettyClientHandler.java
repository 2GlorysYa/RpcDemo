package client.handler;

import client.client.ChannelProvider;
import client.client.UnprocessedRequests;
import common.reqeust.RpcRequest;
import common.response.RpcResponse;
import common.serializer.CommonSerializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    private UnprocessedRequests unprocessedRequests;

    public NettyClientHandler() {
        this.unprocessedRequests = new UnprocessedRequests();
    }

    /**
     * 如果5秒内write()方法未被调用则触发一次userEventTrigger()方法
     * IdleStateHandler 既是出站处理器也是入站处理器，继承了 ChannelDuplexHandler 。
     * 当连接的空闲时间（读或者写）太长时，将会触发一个【空写事件】IdleStateEvent.WRITE_IDLE
     * 然后，你可以通过你的 ChannelInboundHandler 中重写 userEventTrigged 方法来处理该事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 如果传进来的事件是IdleStateEvent
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            // 如果事件是一段事件内没有数据发送
            if (state == IdleState.WRITER_IDLE) {
                // 则获得channel并写入心跳包
                logger.info("发送心跳包[{}]", ctx.channel().remoteAddress());
                Channel channel = ChannelProvider.get((InetSocketAddress) ctx.channel().remoteAddress(),
                CommonSerializer.getByCode(CommonSerializer.DEFAULT_SERIALIZER));
                RpcRequest rpcRequest = new RpcRequest();
                rpcRequest.setHeartBeat(true);
                // 设置一个Listener监听服务端是否接收到心跳包，如果接收到就表示对方在线，不用关闭Channel
                // 如果这个写操作失败，则关闭通道
                channel.writeAndFlush(rpcRequest).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx,evt);
        }
    }

    /**
     * 这里只需要处理收到的消息，即 RpcResponse 对象，由于前面已经有解码器解码了
     * 这里就直接将返回的结果放入 ctx 中即可
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        try {
            logger.info(String.format("客户端接收到消息: %s", msg));
            // 生成一个AttributeKey
            // AttributeMap<AttributeKey, AttributeValue>是绑定在Channel上的，可以设置用来获取通道对象
            // AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + msg.getRequestId());
            // 把这个key set进Attribute
            // 所以等于说不是直接取出数据，而是在这里把响应数据set到属性里，然后在NettyClient里取出
            // ctx.channel().attr(key).set(msg);
            // ctx.channel().close();

            // 将响应数据取出
            logger.info("本次响应id:{}", msg.getRequestId());
            unprocessedRequests.complete(msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 处理InboundHandler异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("过程调用中发生了错误: ");
        cause.printStackTrace();
        ctx.close();
    }
}
