package Server.registry.Impl;

import Server.config.RpcError;
import Server.config.RpcException;
import Server.registry.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultServiceProvider implements ServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceProvider.class);

    // 使用synchronized和concurrentHashMap保证线程安全
    // 并且设置为static保证全局唯一的注册信息
    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * 向容器注册服务
     * @param service 待注册的服务
     * @param <T>
     */
    @Override
    public synchronized <T> void register(T service) throws RpcException {
        // 获取更容易理解的类名表示，getClass和getCanonicalName在获取普通类名上没有区别
        String serviceName = service.getClass().getCanonicalName();
        // 如果该服务已注册，就返回
        if (registeredService.contains(serviceName)) {
            return;
        } else {
            registeredService.add(serviceName);
            // 可能实现了多个接口，因此使用接口数组
            Class<?>[] interfaces = service.getClass().getInterfaces();
            if (interfaces.length == 0) {
                throw new RpcException(RpcError.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
            }
            // 一个服务对象可能实现了多个接口，因此注册的时候多个接口都绑定在同一个对象（value）上，逻辑更清晰
            // 如果某个服务对象实现了两个接口，那就相当于会注册两个Map.Entry<K， V>对象, 且两个K都是绑定同一个V
            for (Class<?> i : interfaces) {
                serviceMap.put(i.getCanonicalName(), service);
            }
            logger.info("向接口: {} 注册服务：{}", interfaces, serviceName);
        }

    }

    /**
     * 从容器拉取服务
     * @param serviceName 要拉取的服务名
     * @return
     * @throws RpcException
     */
    @Override
    public synchronized Object getService(String serviceName) throws RpcException {
        Object service = serviceMap.get(serviceName);
        // 如果没有拉取到，就抛出异常
        if (service == null) {
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }
        return service;
    }
}
