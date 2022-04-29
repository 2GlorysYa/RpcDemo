package client;

import common.reqeust.RpcRequest;
import common.config.RpcException;
import common.response.RpcResponse;
import common.serializer.CommonSerializer;

import java.util.concurrent.CompletableFuture;

public interface RpcClient {
    Object sendRequest(RpcRequest rpcRequest);

    Object sendRequest1(RpcRequest rpcRequest, CommonSerializer serializer) throws Exception;

    Object sendRequest(RpcRequest rpcRequest, CommonSerializer serializer) throws RpcException;

    Object sendRequest(RpcRequest rpcRequest, String host, int port);

    CompletableFuture<RpcResponse> sendRequest2(RpcRequest rpcRequest, CommonSerializer serializer) throws RpcException;

}
