package Server.registry;

import java.net.InetSocketAddress;

// Nacos远程服务注册表
public interface ServiceRegistryForNacos {


    /**
     * 向Nacos注册服务
     */
    void register(String serviceName, InetSocketAddress inetSocketAddress);

    /**
     * 向Nacos拉取服务
     */
    InetSocketAddress lookupService(String serviceName);
}
