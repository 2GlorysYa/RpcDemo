package Server.config;

import Client.reqeust.RpcRequest;
import Server.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器
 */
public class CommonEncoder extends MessageToByteEncoder {

    // 4字节魔数，用来识别是自定义协议，4位二进制表示一个16进制
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    private final CommonSerializer serializer;

    public CommonEncoder(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf out) throws Exception {
        out.writeInt(MAGIC_NUMBER);
        // 如果消息是RPC请求，那就先写入请求标识到ByteBuf
        // 否则写入响应标识PackageType
        if (msg instanceof RpcRequest) {
            out.writeInt(PackageType.REQUEST_PACK.getCode());
        } else {
            out.writeInt(PackageType.RESPONSE_PACK.getCode());
        }
        out.writeInt(serializer.getCode()); // 写入序列化器编号，指定使用哪个序列化器，比如Kryo，protoStuff
        byte[] bytes = serializer.serialize(msg);   // 序列化
        out.writeInt(bytes.length); // 写入数据长度，防止粘包
        out.writeBytes(bytes);  // 写入数据
    }
}
