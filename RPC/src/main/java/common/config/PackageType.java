package common.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PackageType放在标识自定义协议里，用来标识这个包是请求包还是响应包
 */
@Getter
@AllArgsConstructor
public enum PackageType {

    REQUEST_PACK(0),
    RESPONSE_PACK(1);

    private final int code;

}
