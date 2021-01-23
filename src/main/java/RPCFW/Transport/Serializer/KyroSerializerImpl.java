package RPCFW.Transport.Serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class KyroSerializerImpl implements Serializer {
    private  final ThreadLocal<Kryo> kryoLocal = new ThreadLocal<>();

    public KyroSerializerImpl(){
        Kryo kryo = new Kryo();
        //支持对象循环引用（否则会栈溢出）
        kryo.setReferences(true);
        //不强制要求注册类（注册行为无法保证多个 JVM 内同一个类的注册编号相同；而且业务系统中大量的 Class 也难以一一注册）
        kryo.setRegistrationRequired(false);
    }

    @Override
    public byte[] encode(Object obj) {
        Kryo kryo = kryoLocal.get();
        ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
        Output output = new Output(outputStream);
        kryo.writeObject(output, obj);
        //手动remove 加快内存释放
        kryoLocal.remove();
        return output.toBytes();
    }

    @Override
    public <T> T decoder(byte[] data, Class<T> clazz) {
        Kryo kryo = kryoLocal.get();
        ByteArrayInputStream inputStream= new ByteArrayInputStream(data);
        Input input = new Input(inputStream);
        Object o=  kryo.readObject(input, clazz);
        //手动remove 加快内存释放
        kryoLocal.remove();
        return clazz.cast(o);
    }

    @Override
    public <T> T decoder(InputStream inputStream, Class<T> clazz) {
        Kryo kryo = kryoLocal.get();
        Input input = new Input(inputStream);
        Object o=  kryo.readObject(input, clazz);
        //手动remove 加快内存释放
        kryoLocal.remove();
        return clazz.cast(o);
    }


}
