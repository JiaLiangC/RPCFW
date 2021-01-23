package RPCFW.Transport.server;

import RPCFW.Transport.handler.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketRpcServer {
    private static final Logger logger = LoggerFactory.getLogger(SocketRpcServer.class);
    final  ExecutorService executorService;
    private int port;
    public SocketRpcServer(int port){
        executorService = Executors.newFixedThreadPool(100);
        this.port = port;
    }

    public void start() {
        try(ServerSocket server = new ServerSocket(port)) {
            Socket socket;
            while ((socket = server.accept())!=null){
                System.out.println("accept a req");
                executorService.submit(new RequestHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
