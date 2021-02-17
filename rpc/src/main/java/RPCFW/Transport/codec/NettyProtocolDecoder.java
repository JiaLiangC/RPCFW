package RPCFW.Transport.codec;

import RPCFW.Transport.Serializer.KyroSerializerImpl;
import RPCFW.Transport.Serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyProtocolDecoder extends LengthFieldBasedFrameDecoder {
    private static final Logger logger = LoggerFactory.getLogger(NettyProtocolDecoder.class);

    int maxFrameLength;
    static  final int lengthFieldOffset=2;
    static  final int lengthFieldLength=4;
    static  final int lengthAdjustment=9;
    static  final int  initialBytesToStrip=0;
    private static Serializer serializer = new KyroSerializerImpl();


    public NettyProtocolDecoder(int maxFrameLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength,lengthAdjustment,initialBytesToStrip);
    }

    //发送数据包长度 = 长度域的值 + lengthFieldOffset + lengthFieldLength + lengthAdjustment；

    //固定位： 魔术位(2字节)+整体长度(4字节)+消息头长度(2字节)+协议版本(1字节)+消息类型(1字节)+ 序列化方式(1字节)+消息ID(4字节)
    //不固定位：不定长消息头扩展，不定长消息体
    //所以 lengthFieldOffset 2, lengthFieldLength 4, lengthAdjustment 9, initialBytesToStrip 0
    //TODO 魔术位验证
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
         ByteBuf decode =  (ByteBuf) super.decode(ctx, in);
         if(decode!=null){
             Short magicNumber = decode.readShort();
             int bodyLength = decode.readInt();
             int headerAttachmentLength = decode.readShort();
             byte version = decode.readByte();
             byte messageTYpe = decode.readByte();
             byte serializerType = decode.readByte();
             int sessionID = decode.readInt();

             if(headerAttachmentLength>0 && decode.readableBytes()>0){
                //reader header attachment

             }

             if (bodyLength>0&&decode.readableBytes()>0){
                 // read body
                 byte[] data = new byte[bodyLength];
                 decode.readBytes(data);
                 Object o = serializer.decoder(data);
                 return o;
             }
         }else{
             logger.error("ByteBuf decode is null");
         }
        return null;
    }
    public void test(){
    }
}
