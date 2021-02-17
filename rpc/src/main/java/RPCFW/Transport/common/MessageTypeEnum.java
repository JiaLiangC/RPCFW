package RPCFW.Transport.common;

public enum  MessageTypeEnum {
    REQUEST((byte)1),RESPONSE((byte)2),PING((byte)3),PONG((byte)4),EMPTY((byte)5);
    private byte type;

     MessageTypeEnum(byte type){
        this.type=type;
    }

    public byte getType(){
         return type;
    }

    public static MessageTypeEnum getValue(byte type){
         for(MessageTypeEnum m : MessageTypeEnum.values()){
             if(type==m.getType()){
                 return m;
             }
         }
         return null;
    }

}
