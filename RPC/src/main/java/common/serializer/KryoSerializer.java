package common.serializer;

import common.config.SerializeException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSerializer implements CommonSerializer{

    private static final Logger logger = LoggerFactory.getLogger(KryoSerializer.class);

    // 初始化ThreadLocal里的对象
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 注册类，如果是writeObject则不需要注册，但必须提供class信息
        // kryo.register(RpcResponse.class);
        // kryo.register(RpcRequest.class);
        // 打开循环引用支持
        kryo.setReferences(true);
        // 不强制要求注册类，否则涉及的所有类比如Object都必须注册
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    /**
     * 序列化
     */
    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Output output = new Output(byteArrayOutputStream)) {
            // 从ThreadLocal获取Kryo对象
            Kryo kryo = kryoThreadLocal.get();
            // 将待序列化对象写入OutPut对象
            kryo.writeObject(output, obj);
            // 从ThreadLocal中移除Kryo
            kryoThreadLocal.remove();
            // 最后把OutPut转换为Byte数组
            return output.toBytes();
        } catch (Exception e) {
            logger.error("序列化时发生错误: " + e);
            throw new SerializeException("序列化时发生错误");
        }
    }

    /**
     * 反序列化
     */
    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            Input input = new Input(byteArrayInputStream)) {
            // 从ThreadLocal中获取Kryo对象
            Kryo kryo = kryoThreadLocal.get();
            // kryo反序列化对象
            Object o = kryo.readObject(input, clazz);
            // ThreadLocal移除Kryo
            kryoThreadLocal.remove();
            return o;
        } catch (Exception e) {
            logger.error("反序列化时发生了错误" + e);
            throw new SerializeException("反序列化时发生了错误");
        }
    }

    /**
     * 获取序列化器编号
     */
    @Override
    public int getCode() {
        return SerializerCode.valueOf("KRYO").getCode();
    }
}
