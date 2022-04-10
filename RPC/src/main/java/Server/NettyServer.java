package Server;

import Server.config.CommonDecoder;
import Server.config.CommonEncoder;
import Server.config.RpcException;
import Server.handler.NettyServerHandler;
import Server.registry.Impl.ZkServiceRegistry;
import Server.registry.ServiceProvider;
import Server.registry.ServiceRegistry;
import Server.serializer.KryoSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class NettyServer implements RpcServer{

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private final ServiceProvider serviceProvider;

    private ServiceRegistry serviceRegistry;

    private int port;

    public NettyServer(ServiceProvider serviceProvider, int port) {
        this.serviceProvider = serviceProvider;
        this.serviceRegistry = new ZkServiceRegistry();
        this.port = port;
    }

    @Override
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)   // 指定事件循环组
                    .channel(NioServerSocketChannel.class)  // 指定channel实现类
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 256)  // tcp内部维护两个连接队列，一个是sync queue，另一个是accept queue，如果连接数超过256，就拒绝连接
                    .option(ChannelOption.SO_KEEPALIVE, true)   // 开启心跳机制
                    .childOption(ChannelOption.TCP_NODELAY, true)   // 禁用Nagle算法，收到数据包就及时发出
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        /**
                         * 对于Handler的入站，先执行Decoder，再执行ServerHandler
                         * 对于Handler的出站，只执行Encoder，因此只要保证Decoder的顺序在ServerHandler上面就行
                         */
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // pipeline.addLast(new CommonEncoder(new JsonSerializer()))   // 出站
                            pipeline.addLast(new CommonEncoder(new KryoSerializer()))   // 出站
                                    .addLast(new CommonDecoder())   // 入站
                                    .addLast(new NettyServerHandler(serviceProvider)); // 入站
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(port).sync();   // ip即本机
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("启动服务器时发生错误", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 注册服务
     */
    @Override
    public <T> void publishService(Object service, Class<T> serviceClass) throws RpcException {
        // 注册服务到本地map
        serviceProvider.register(service);
        // 注册服务到zooKeeper
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress("127.0.0.1", port));
        logger.info("已注册服务 {} 到ZooKeeper", serviceClass.getCanonicalName());
    }
}
