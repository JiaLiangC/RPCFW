package RPCFW.RPCCommon;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RPCServer {
    private static final Logger logger = LoggerFactory.getLogger(RPCServer.class);
    final  ExecutorService executorService;
    private int port;
    public  RPCServer(int port){
        executorService = Executors.newFixedThreadPool(100);
        this.port = port;
    }

    public void register(Object service) {
        try(ServerSocket server = new ServerSocket(port)) {
            Socket socket;
            while ((socket = server.accept())!=null){
                System.out.println("accept a req");
                executorService.submit(new RequestHandler(socket,service));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
