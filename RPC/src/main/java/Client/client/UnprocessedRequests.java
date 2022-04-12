package Client.client;

import Server.response.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnprocessedRequests {

    private static ConcurrentHashMap<String , CompletableFuture<RpcResponse>>
            unprrocessedRequests = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse> future) {
        unprrocessedRequests.put(requestId, future);
    }

    public void remove(String requestId) {
        unprrocessedRequests.remove(requestId);
    }

    public void complete(RpcResponse rpcResponse) {
        CompletableFuture<RpcResponse> future = unprrocessedRequests.remove(rpcResponse.getRequestId());
    }



}
