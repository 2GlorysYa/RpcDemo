package Client;

import Client.handler.NettyClientHandler;
import Client.reqeust.RpcRequest;
import Server.config.CommonDecoder;
import Server.config.CommonEncoder;
import Server.config.RpcException;
import Server.response.RpcResponse;
import Server.serializer.CommonSerializer;
import Server.serializer.KryoSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class NettyClient implements RpcClient {

    private static final Logger logger =  LoggerFactory.getLogger(NettyClient.class);

    private String host;
    private int port;
    private static final Bootstrap bootstrap;

    // 使用静态代码块初始化Netty客户端
    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public NettyClient() {};

    static {
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();    // netty引导类
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new CommonDecoder())   // 入站
                                .addLast(new CommonEncoder(new KryoSerializer()))   // 出站
                                .addLast(new NettyClientHandler()); // 入站
                    }
                });
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();
            logger.info("客户端连接到服务器{} : {}", host, port);
            Channel channel = future.channel();
            if (channel != null) {
                channel.writeAndFlush(rpcRequest).addListener(
                        // 向服务器发送请求，即向channel写入rpcQuest，并设置一个监听器
                        // 他的作用是开启一个线程，当writeAndFlush执行完毕，异步返回一个对象后
                        // 这个线程就会调用监听器的异步回调方法
                        future1 -> {
                            if (future1.isSuccess()) {
                                logger.info(String.format("客户端发送消息: %s", rpcRequest.toString()));
                            } else {
                                logger.error("发送消息时发生错误:", future1.cause());
                            }
                        });
                channel.closeFuture().sync();
                // Channel继承了AttributeMap，所以里面封装了一个AttributeMap
                // AttributeMap相对于一个map，AttributeKey相当于map的key，Attribute是一个持有key(AttributeKey)和value的对象。
                // 因此在map中我们可以通过AttributeKey key获取Attribute，从而获取Attribute中的value(即属性值)
                // 获取在ClientHandler中set进map的RpcResponse
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                RpcResponse rpcResponse = channel.attr(key).get();
                return rpcResponse.getData();
            }
        } catch (InterruptedException e) {
            logger.error("发送消息时产生错误:", e);
        }
        return null;
    }

    @Override
    public Object sendRequest1(RpcRequest rpcRequest, CommonSerializer serializer) throws Exception {
        return null;
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest, CommonSerializer serializer) throws RpcException {
        return null;
    }

    /**
     * 因为修改了RpcClientProxy sendRequest的调用RpcClient为NettyClient
     * 因此出现方法冲突，就在接口新增一个方法，暂时没用
     */
    @Override
    public Object sendRequest(RpcRequest rpcRequest, String host, int port) {
        return null;
    }

    @Override
    public CompletableFuture<RpcResponse> sendRequest2(RpcRequest rpcRequest, CommonSerializer serializer) throws RpcException {
        return null;
    }
}
