package server.handler;

import common.reqeust.RpcRequest;
import common.config.ResponseCode;
import common.response.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    /**
     * handle处理rpc请求，调用invokeTargetMethod方法
     * @param rpcRequest
     * @param service
     * @return
     */
    public Object handle(RpcRequest rpcRequest, Object service) {
        Object result = null;
        try {
            result = invokeTargetMethod(rpcRequest,service);
            // 服务 -> 绑定的接口
            logger.info("服务: {} 成功调用方法: {}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("调用或发送时发生错误: ", e);
        } return result;
    }

    /**
     * 调用目标方法
     * @param rpcRequest  rpc请求对象，封装了远程调用方法的信息
     * @param service 要调用哪个实例上的方法
     * @return  方法调用的返回值
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service)
            throws IllegalAccessException, InvocationTargetException {
        Method method;
        try {
            // 反射只需要传入【方法名】和【方法参数类型】就可以确定方法
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND, rpcRequest.getRequestId());
        }
        // 最后使用invoke调这个方法，第一个参数是在哪个实例上调方法，第二个参数是传入被调用方法的参数
        return method.invoke(service, rpcRequest.getParameters());
    }

    public static void main(String[] args) {
    }
}
