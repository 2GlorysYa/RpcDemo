package Server;

import Server.config.RpcException;

public interface RpcServer {

    void start();

    <T> void publishService(Object service, Class<T> serviceClass) throws RpcException;
}
