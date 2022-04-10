package Server.registry;

import Server.config.RpcException;

// 服务容器，保存本地服务的信息
public interface ServiceProvider {

    // 注册服务
    <T> void register(T service) throws RpcException;

    // 获取服务
    Object getService(String serviceName) throws RpcException;
}
