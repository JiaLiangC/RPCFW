package RPCFW.Transport.common;

import java.io.Serializable;
import java.util.UUID;

public class RPCRequest implements Serializable {
    private String methodName;
    private String interfaceName;
    private Object[] parameters;
    private Class<?>[] parameterTypes;
    private static final long serialVersionUID = 1L;
    private String uid;

    public RPCRequest() {
    }

    private RPCRequest(String methodName, String interfaceName, Object[] parameters, Class<?>[] parameterTypes) {
        this.methodName = methodName;
        this.interfaceName = interfaceName;
        this.parameters = parameters;
        this.parameterTypes = parameterTypes;
        this.uid= UUID.randomUUID().toString();
    }

    private RPCRequest(Builder builder){
         this(builder.methodName,builder.interfaceName, builder.parameters, builder.parameterTypes);
    }

    public String getMethodName() {
        return methodName;
    }


    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }


    public Object[] getParameters() {
        return parameters;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public static class Builder{
        private String methodName;
        private String interfaceName;
        private Object[] parameters;
        private Class<?>[] parameterTypes;

        public  Builder setInterfaceName(String interfaceName) {
            this.interfaceName = interfaceName;
            return  this;
        }

        public Builder setMethodName(String methodName) {
            this.methodName = methodName;
            return  this;
        }

        public Builder setParameterTypes(Class<?>[] parameterTypes) {
            this.parameterTypes = parameterTypes;
            return  this;
        }

        public Builder setParameters(Object[] parameters) {
            this.parameters = parameters;
            return  this;
        }

        public RPCRequest build(){
            RPCRequest rpcRequest = new RPCRequest(this);
            return rpcRequest;
        }
    }

    public static void main(String[] args) {
        Object[] as= {1,"1"};
        Class<?>[] cz = {int.class,String.class};
        RPCRequest rpcRequest = new RPCRequest.Builder().setInterfaceName("aa")
                .setMethodName("bb")
                .setParameters(as)
                .setParameterTypes(cz)
                .build();
        System.out.println(rpcRequest.getMethodName());
    }
}
