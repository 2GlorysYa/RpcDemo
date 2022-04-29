package common.util;

import common.reqeust.RpcRequest;
import common.config.ResponseCode;
import common.config.RpcError;
import common.config.RpcException;
import common.response.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 检查响应和请求
 * 利用请求号对服务端返回的数据进行校验，保证请求与响应一一对应
 */
public class RpcMessageChecker {

    private static final Logger logger = LoggerFactory.getLogger(RpcMessageChecker.class);

    private static final String INTERFACE_NAME = "interfaceName";

    public static void check(RpcRequest rpcRequest, RpcResponse rpcResponse) throws RpcException {
        // 空响应
        if (rpcResponse == null) {
            logger.error("调用服务失败，该服务名为: {}", rpcRequest.getInterfaceName());
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        // 如果响应号与请求号不同
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcError.RESPONSE_NOT_MATCH, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        // 调用失败
        if (rpcResponse.getStatusCode() == null || !rpcResponse.getStatusCode().equals(ResponseCode.SUCCESS)) {
            logger.error("调用服务失败, 该服务名为: {}", rpcRequest.getInterfaceName(), rpcResponse);
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }

}
