package Server.registry;

import java.net.InetSocketAddress;

public interface ServiceRegistry {

    /**
     * 注册服务
     */
    void register(String serviceName, InetSocketAddress inetSocketAddress);

    /**
     * 拉取服务
     */
    InetSocketAddress lookupService(String serviceName);
}
