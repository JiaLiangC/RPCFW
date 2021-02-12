package RPCFW.Transport.handler;

import RPCFW.ServiceManager.registry.DefaultRegistry;
import RPCFW.Transport.Serializer.Serializer;
import RPCFW.Transport.common.RPCRequest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class RequestHandler implements Runnable {
    private  Socket socket;
    private Serializer serializer;

    public RequestHandler(Socket s){
        this.socket = s;
    }

    @Override
    public void run() {
        try {
            RPCRequest rpcRequest = serializer.decoder(socket.getInputStream(),RPCRequest.class);

            Object service = new DefaultRegistry().getService(rpcRequest.getInterfaceName());
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParameterTypes());
            Object res = method.invoke(service,rpcRequest.getParameters());

            serializer.encode(res);

        } catch (IOException  | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
