package RPCFW.Transport.client;

import RPCFW.Transport.common.RPCRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

public class NettyClientProxy1 extends  ClientProxy {
    private static final Logger LOG = LoggerFactory.getLogger(NettyClientProxy1.class);
    private RpcClient client;
    private InetSocketAddress address;

    public NettyClientProxy1(InetSocketAddress address) {
        LOG.info("NettyClientProxy1 init");
        this.address=address;
        this.client = new NettyRpcClient(address);
    }

    @Override
    public <T> T getProxy(Class<T> clazz) {
        //TODO 报错，原因还未解决
        //  return (T) Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), ClientProxy.this);
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, NettyClientProxy1.this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args){
        RPCRequest rpcRequest = new RPCRequest.Builder().setInterfaceName(method.getDeclaringClass().getName())
                .setMethodName(method.getName())
                .setParameters(args)
                .setParameterTypes(method.getParameterTypes())
                .build();
        Object result = client.sendRpcRequest(rpcRequest);
        return result;
    }

    @Override
    public void close() throws IOException {

    }
}
