package RPCFW.RPCDemo.Netty;

import RPCFW.ServiceManager.registry.DefaultRegistry;
import RPCFW.Transport.server.NettyRpcServer;
import RPCFW.Transport.server.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Server {
    private  static  Logger  logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        logger.info("server main");

        DefaultRegistry registry = new DefaultRegistry();
        registry.register(new EatServiceImpl());
        RpcServer rpcServer = new NettyRpcServer(8080);
        rpcServer.start();

    }
}
