package Client;

import Client.reqeust.RpcRequest;
import Server.config.RpcException;
import Server.serializer.CommonSerializer;

public interface RpcClient {
    Object sendRequest(RpcRequest rpcRequest);

    Object sendRequest1(RpcRequest rpcRequest, CommonSerializer serializer) throws Exception;

    Object sendRequest(RpcRequest rpcRequest, CommonSerializer serializer) throws RpcException;

    Object sendRequest(RpcRequest rpcRequest, String host, int port);

}
