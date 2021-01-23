package RPCFW.RPCDemo.Nio.server;

import RPCFW.RPCDemo.Nio.server.HelloServiceImpl;
import RPCFW.ServiceManager.registry.DefaultRegistry;
import RPCFW.Transport.server.SocketRpcServer;

public class Server {
    public static void main(String[] args) {
        SocketRpcServer rpcServer = new SocketRpcServer(8080);
        DefaultRegistry registry = new DefaultRegistry();
        registry.register(HelloServiceImpl.class);
        rpcServer.start();
    }
}
