package RPCFW.Transport.Serializer;

public class JsonSerializerImpl implements Serializer{
    @Override
    public byte[] encode(Object o) {
        return new byte[0];
    }

    @Override
    public <T> T decoder(byte[] data, Class<T> clazz) {
        return null;
    }
}
