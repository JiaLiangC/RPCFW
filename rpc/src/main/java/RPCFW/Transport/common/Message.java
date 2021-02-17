package RPCFW.Transport.common;

import java.util.Map;

public class Message {
    //魔数，协议身份标示
    private short magicNumber;
    //协议 body长度
    private int  length;
    //head 扩展字段长度
    private short headerLength;
    //版本
    private byte version;
    //消息类型
    private MessageTypeEnum messageTypeEnum;
    //序列化协议
    private byte serializer;
    //head 扩展字段
    //请求ID
    private int sessionId;
    private Map<String,String> headAttachment;

}
