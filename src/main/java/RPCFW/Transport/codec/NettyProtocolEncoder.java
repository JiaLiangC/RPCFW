package RPCFW.Transport.codec;

import RPCFW.Transport.Serializer.KyroSerializerImpl;
import RPCFW.Transport.Serializer.Serializer;
import RPCFW.Transport.Serializer.SerializerAlgoirthm;
import RPCFW.Transport.common.Constants;
import RPCFW.Transport.common.MessageTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.Random;

public class NettyProtocolEncoder extends MessageToByteEncoder {

    private final Serializer serializer = new KyroSerializerImpl();

    //发送数据包长度 = 长度域的值 + lengthFieldOffset + lengthFieldLength + lengthAdjustment；
    //固定位： 魔术位(2字节)+整体长度(4字节)+消息头长度(2字节)+协议版本(1字节)+消息类型(1字节)+ 序列化方式(1字节)+消息ID(4字节)+
    //不固定位：不定长消息头扩展，不定长消息体
    // 这里Object 是 RpcRequest 心跳包或者是其他的对象，这里主要负责组装协议，序列化对象
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (o!=null){
             byte [] body =  serializer.encode(o);
             int bodylenth = body.length;
             int headerLength = 0;
             byte version = 0;
             int sessionId = new Random().nextInt();
            MessageTypeEnum messageTypeEnum = MessageTypeEnum.REQUEST;

            //write fix head
            byteBuf.writeShort(Constants.MAGIC_NUMBER);
            byteBuf.writeInt(bodylenth);
            byteBuf.writeShort(headerLength);
            byteBuf.writeByte(version);
            byteBuf.writeByte(messageTypeEnum.getType());
            byteBuf.writeByte(SerializerAlgoirthm.Kyro);
            byteBuf.writeInt(sessionId);

            //write header attachment
            if(headerLength>0){
            }
            //writer body
            byteBuf.writeBytes(body);

        }
    }

}
