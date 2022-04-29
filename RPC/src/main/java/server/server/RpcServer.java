package server.server;

import server.handler.RequestHandler;
import server.handler.RequestHandlerThread;
import server.registry.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    // 线程池参数都用final修饰，保证不可变
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 50;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;
    private final ExecutorService threadPool;

    private RequestHandler requestHandler = new RequestHandler();
    private final ServiceProvider serviceProvider;

    // 构造器初始化线程池和服务Registry
    public RpcServer(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME,
                 TimeUnit.SECONDS,
                workingQueue,
                threadFactory);
    }

    /**
     * 服务器启动监听，并执行handle线程
     * @param port
     */
    public void start(int port) {
        // 被发起连接的一方，只需要port构造
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                // 获得这个连接绑定的客户ip地址和端口
                logger.info("消费者连接: {}:{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new RequestHandlerThread(socket,
                        requestHandler,
                        serviceProvider));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("服务器启动时发生错误: ", e);
        }
    }
}
