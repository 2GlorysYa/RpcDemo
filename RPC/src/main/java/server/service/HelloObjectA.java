package server.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// HelloObject对象会在调用过程中从客户端传递给服务端，必须进行序列化
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HelloObjectA implements Serializable {
    private Integer id;
    private String message;
}
