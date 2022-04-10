package Server.config;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RpcError {

    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("服务调用失败"),
    SERVICE_NOT_FOUND("找不到对应的服务"),
    UNKNOWN_PROTOCOL("不识别的协议包"),
    UNKNOWN_PACKAGE_TYPE("不识别的数据包类型"),
    UNKNOWN_SERIALIZER("不识别的(反)序列化器"),
    CLIENT_CONNECT_SERVER_FAILURE("客户端连接服务端失败"),
    SERIALIZER_NOT_FOUND("未找到序列化器");

    private final String message;
}
