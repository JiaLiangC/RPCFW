package RPCFW.RPCCommon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class RequestHandler implements Runnable {
    private  Socket socket;
    private Object service;

    RequestHandler(Socket s,Object service){
        this.socket = s;
        this.service=service;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            RPCRequest rpcRequest = (RPCRequest)in.readObject();
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParameterTypes());
            Object res = method.invoke(service,rpcRequest.getParameters());
            out.writeObject(res);
            out.flush();

        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
