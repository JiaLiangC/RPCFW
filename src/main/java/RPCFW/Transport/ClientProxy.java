package RPCFW.Transport;

import RPCFW.Transport.client.RpcClient;
import RPCFW.Transport.client.SocketRpcClient;
import RPCFW.Transport.common.RPCRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ClientProxy implements InvocationHandler {

    private RpcClient client;
    private int port;

    public ClientProxy(RpcClient rpcClient) {
        this.client = rpcClient;
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
        Object result = client.sendRpcRequest(rpcRequest);
        return result;
    }
}
