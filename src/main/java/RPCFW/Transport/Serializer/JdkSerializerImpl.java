package RPCFW.Transport.Serializer;

import java.io.*;

public class JdkSerializerImpl implements Serializer {

    @Override
    public byte[] encode(Object o) {
        return new byte[0];
    }

    @Override
    public byte[] encode(Object o, OutputStream outputStream) {
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(outputStream);
            out.writeObject(o);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public <T> T decoder(byte[] data, Class<T> clazz) {
        return null;
    }

    @Override
    public <T> T decoder(InputStream inputStream, Class<T> clazz) {
        try {
            ObjectInputStream in = new ObjectInputStream(inputStream);
            T t = (T)in.readObject();
            return t;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
