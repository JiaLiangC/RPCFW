package RPCFW.RPCDemo.Netty;

import RPCFW.ServiceManager.registry.DefaultRegistry;
import RPCFW.Transport.server.NettyRpcServer;
import RPCFW.Transport.server.RpcServer;

import java.rmi.registry.Registry;

public class Server {

    public static void main(String[] args) {
        RpcServer rpcServer = new NettyRpcServer(8080);
        rpcServer.start();
        DefaultRegistry registry = new DefaultRegistry();
        registry.register(EatServiceImpl.class);
    }
}
