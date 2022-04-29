package common.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 响应状态码
@AllArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS(200, "调用方法成功"),
    Fail(500, "调用方法失败"),
    METHOD_NOT_FOUND(501, "未找到指定方法"),
    CLASS_NOT_FOUND(502, "未找到指定类");

    private int code;
    private final String message;
}
