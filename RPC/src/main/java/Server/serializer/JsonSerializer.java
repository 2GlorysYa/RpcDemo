package Server.serializer;

import Client.reqeust.RpcRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonSerializer implements CommonSerializer{

    private static final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Jackson序列化
     * @param obj 待序列化对象
     * @return 二进制数组
     */
    @Override
    public byte[] serialize(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            logger.error("序列化时发生错误：{}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * jackson反序列化
     * @param bytes  二进制数组
     * @param clazz 对象类型
     * @return Java对象
     */
    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try {
            Object obj = objectMapper.readValue(bytes, clazz);
            if (obj instanceof RpcRequest) {
                obj = handleRequest(obj);
            }
            return obj;
        } catch (IOException e) {
            logger.error("反序列化时发生错误: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 这里有一个需要注意的点，就是在 RpcRequest 反序列化时，由于其中有一个字段是 Object 数组
     * 在反序列化时序列化器会根据字段类型进行反序列化，而 Object 就是一个十分模糊的类型，会出现反序列化失败的现象
     * 这时就需要 RpcRequest 中的另一个字段 ParamTypes 来获取到 Object 数组中的每个实例的实际类，辅助反序列化
     * 注意【只有Json序列化会出现这种情况】，因为他的本质是将数据序列化为【Json字符串】，这样就丢失了【类型信息】
     * 而其他序列化器是把数据序列化为二进制数组，包含了类型信息
     */
    private Object handleRequest(Object obj) throws IOException {
        RpcRequest rpcRequest = (RpcRequest) obj;
        for (int i = 0; i < rpcRequest.getParamTypes().length; i++) {
            Class<?> clazz = rpcRequest.getParamTypes()[i];
            // superClass.isAssignableFrom(childClass) 父类（或接口类）判断给定类是否是它本身或其子类，类似于InstanceOf
            // 判断方法参数（object数据）里的每个参数是否是其对应的类型
            // 如果不是，就把这个参数写到byte数组里，并重新反序列化读出来并指定他的反序列化类型
            if (!clazz.isAssignableFrom(rpcRequest.getParameters()[i].getClass())) {
                byte[] bytes = objectMapper.writeValueAsBytes(rpcRequest.getParameters()[i]);
                rpcRequest.getParameters()[i] = objectMapper.readValue(bytes, clazz);
            }
        }
        return rpcRequest;
    }

    /**
     * @return 所使用测定序列化器编号
     */
    @Override
    public int getCode() {
        return SerializerCode.valueOf("JSON").getCode();    // 返回枚举常量JSON的值 1
    }
}
