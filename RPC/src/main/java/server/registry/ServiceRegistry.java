package server.registry;

import java.net.InetSocketAddress;
import java.util.List;

public interface ServiceRegistry {

    /**
     * 注册服务
     */
    void register(String serviceName, InetSocketAddress inetSocketAddress);

    /**
     * 拉取服务
     */
    InetSocketAddress lookupService(String serviceName);

    List<String> getServiceList(String serviceName);
}
