package RPCFW.Transport.client;

import java.io.Closeable;
import java.lang.reflect.InvocationHandler;

public abstract class ClientProxy implements InvocationHandler, Closeable {

    abstract public <T> T getProxy(Class<T> clazz);
    public void disConnect(boolean b){throw new UnsupportedOperationException();}

}
