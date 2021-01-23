package RPCFW.Transport.common;

public class RpcResponse<T> {


    private Integer code;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

    public void setData(T data) {
        this.data = data;
    }

    private T data;

    public static <T>  RpcResponse success(T data){
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(RpcResponseCode.SUCCESS.getCode());

        if(data!=null){
            rpcResponse.setData(data);
        }
        return rpcResponse;
    }

    public static <T>  RpcResponse fail(RpcResponseCode code){
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(code.getCode());
        rpcResponse.setMessage(code.getMessage());
        return rpcResponse;
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }
}
