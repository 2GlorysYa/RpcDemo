package Client.client;

import Client.handler.NettyClientHandler;
import Server.config.CommonDecoder;
import Server.config.CommonEncoder;
import Server.config.RpcError;
import Server.config.RpcException;
import Server.serializer.CommonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 改造Netty Client，实现客户端连接失败重试的机制
 */
public class ChannelProvider {

    private static final Logger logger = LoggerFactory.getLogger(ChannelProvider.class);
    private static EventLoopGroup eventLoopGroup;
    private static Bootstrap bootstrap = initializeBootstrap();

    // 连接channel被封装在map里
    private static Map<String, Channel> channels = new ConcurrentHashMap<>();

    // 连接重试次数
    private static final int MAX_RETRY_COUNT = 5;
    private static Channel channel = null;

    private static Bootstrap initializeBootstrap() {
        eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true);
        return bootstrap;
    }

    /**
     * 返回连接以后的channel对象
     * 并实现心跳机制
     *      - 设定每5秒进行一次写检测，如果5秒内write()方法未被调用则触发一次userEventTrigger()方法
     *      - 该方法在NettyClientHandler类中实现
     */
    public static Channel get(InetSocketAddress inetSocketAddress, CommonSerializer serializer) {
        String key = inetSocketAddress.toString() + serializer.getCode();
        if (channels.containsKey(key)) {
            Channel channel = channels.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channels.remove(key);
            }
        }
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new CommonEncoder(serializer))
                        .addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                        .addLast(new CommonDecoder())
                        .addLast(new NettyClientHandler());
            }
        });
        // 设置计数器为1
        // 因为增加了连接重试的机制，并不会返回ChannelFuture，因此不能简单的使用sync()
        // CountDownLatch countDownLatch = new CountDownLatch(1);
        Channel channel = null;
        try {
            // 这里是异步的，主线程执行connect方法这里，具体方法体的执行是由另一个异步线程执行，因此才需要sync()
            // connect(bootstrap, inetSocketAddress, countDownLatch);
            channel = connect(bootstrap,inetSocketAddress);

            // 等待连接服务器完毕
            // countDownLatch.await();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("获取channel时发生错误", e);
            return null;
        }
        // 把连接放入map中
        channels.put(key, channel);
        return channel;
    }

    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, CountDownLatch countDownLatch) {
        connect (bootstrap, inetSocketAddress, MAX_RETRY_COUNT, countDownLatch);
    }

    private static Channel connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener)future -> {
           if (future.isSuccess()) {
               logger.info("客户端连接成功");
               // 手动调用complete完成这个任务，并使得get方法能获取到这个任务返回值
               completableFuture.complete(future.channel());
           }  else {
               throw new IllegalStateException();
           }
        });
        return completableFuture.get();
    }

    /**
     * 连接重试机制
     * 如果第一次connect失败，则递归执行connect直到重试次数为0
     * 注意delay设计为递增的时间，如果第二次客户端没有连接成功，则认为服务器还需要多一点时间来准备
     */
    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, int retry,
                                CountDownLatch countDownLatch) {
        bootstrap.connect(inetSocketAddress).addListener( (ChannelFutureListener) future -> {
            // 如果连接成功，则结束递归
            if (future.isSuccess()) {
                logger.info("客户端连接成功");
                channel = future.channel();
                countDownLatch.countDown();
                return;
            }
            // 如果重试次数为0，则结束递归
            if (retry == 0) {
                logger.error("客户端连接失败，重试次数已用完, 放弃连接！");
                countDownLatch.countDown();
                throw new RpcException(RpcError.CLIENT_CONNECT_SERVER_FAILURE);
            }
            // 查看目前是第几次连接
            int order = MAX_RETRY_COUNT - retry + 1;    // 1 -> 2 -> 3 -> 4 -> 5
            // 两次重试时间的间隔，左移相当于delay * (2^order) 比如 2 -> 5 -> 12 -> 30
            int delay = 1 << order;
            logger.error("{}:连接失败，第{}次重连...", new Date(), order);
            // 从config中取出EventLoopGroup，其中的schedule方法可以穿一个定时任务
            // 递归
            bootstrap.config().group().schedule(() -> {
                connect(bootstrap,inetSocketAddress,retry - 1, countDownLatch );
            }, delay, TimeUnit.SECONDS);
        });
    }
}
