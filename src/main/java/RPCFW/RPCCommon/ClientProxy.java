package RPCFW.RPCCommon;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ClientProxy implements InvocationHandler {

    private String host;
    private int port;

    public ClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public <T> T getProxy(Class<T> clazz) {
        //TODO 报错，原因还未解决
        //  return (T) Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), ClientProxy.this);
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, ClientProxy.this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RPCRequest rpcRequest = new RPCRequest.Builder().setInterfaceName(method.getDeclaringClass().getName())
                .setMethodName(method.getName())
                .setParameters(args)
                .setParameterTypes(method.getParameterTypes())
                .build();
        RPCClient rpcClient = new RPCClient();
        Object result = rpcClient.sendRpcRequest(rpcRequest, host, port);
        return result;
    }
}
