package RPCFW.Transport.client;

import RPCFW.Transport.common.RPCRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketRpcClient implements RpcClient{
    private String host;
    private  int port;

    public SocketRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public Object sendRpcRequest(RPCRequest rpcRequest) {
        try(Socket socket = new Socket(host,port)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(rpcRequest);
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Object result = in.readObject();
            return result;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
