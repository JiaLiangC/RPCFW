package RPCFW.RPCCommon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RPCClient {
    public Object sendRpcRequest(RPCRequest rpcRequest,String host,int port){
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
