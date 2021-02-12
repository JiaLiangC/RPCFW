package RPCFW.Transport.common;

public enum  RpcResponseCode {

    SUCCESS(500,"invoke success"), Fail(500,"invoke failed"),MethodNotFound(500,"method not fpund") ;

    RpcResponseCode(int code,String msg){
        this.message=msg;
        this.code =code;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    private final  int code;
    private final String message;

}
