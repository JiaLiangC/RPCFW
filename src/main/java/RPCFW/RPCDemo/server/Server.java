package RPCFW.RPCDemo.server;

import RPCFW.HelloService;
import RPCFW.RPCCommon.RPCServer;

public class Server {
    public static void main(String[] args) {
        RPCServer rpcServer = new RPCServer(8080);
        HelloService helloService = new HelloServiceImpl();
        rpcServer.register(helloService);
    }
}
