package Server.serializer;

public interface CommonSerializer {

    // 默认的序列化器编号
    Integer DEFAULT_SERIALIZER = new Integer(0);

    // 序列化为byte数组
    byte[] serialize(Object obj);

    // 反序列化为对象
    Object deserialize(byte[] bytes, Class<?> clazz);

    // 实际数据使用的序列化器编号，服务端和客户端应该使用统一标准
    int getCode();

    // 静态方法，获取序列化器
    static CommonSerializer getByCode(int code) {
        switch (code) {
            case 0:
                return new KryoSerializer();
            case 1:
                return new JsonSerializer();
            default:
                return null;
        }
    }
}
