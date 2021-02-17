package RPCFW.Transport.client;

import RPCFW.RPCDemo.Nio.client.Client;
import RPCFW.Transport.common.RPCRequest;
import RPCFW.Transport.common.RpcResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

public class NettyClientProxy4Test extends NettyClientProxy  {


    private volatile boolean disconnect;

    public NettyClientProxy4Test(String host, int port) throws InterruptedException {
        super(host, port);
        disconnect = false;
    }

    public NettyClientProxy4Test(InetSocketAddress address) {
        super(address);
        disconnect = false;
    }

    synchronized public boolean isDisconnect() {
        return disconnect;
    }

    @Override
    synchronized public void disConnect(boolean disconnect) {
        this.disconnect = disconnect;
    }

    @Override
    public RpcResponse send(RPCRequest rpcRequest) throws ExecutionException, InterruptedException {
        if (isDisconnect()){
            throw new IllegalStateException(" network unhealthy,can't reach");
        }
        return super.send(rpcRequest);
    }
}
