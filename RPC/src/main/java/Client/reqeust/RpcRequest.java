package Client.reqeust;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// RPC请求对象，封装了想要远程调用接口的方法的信息，包括接口名，方法名，参数类型，参数
@Data
@Builder    // 建造者模式，一次性给所有变量赋初始值，【考虑写到简历里？】
// 实现了Serializable接口的类【可以】被ObjectOutputStream转换为字节流。
//同时也可以通过ObjectInputStream再将其解析为对象
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {

    // 待调用接口名字
    private String interfaceName;

    // 待调用方法名字
    private String methodName;

    // 待调用方法的【参数】，是对象数组
    private Object[] parameters;

    // 待调用方法的参数【类型】，用class泛型数组表示
    private Class<?>[] paramTypes;
}
