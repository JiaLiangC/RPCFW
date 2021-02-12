package RPCFW.Transport.Serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class KyroSerializerImpl implements Serializer {
    private static final ThreadLocal<Kryo> kryoLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        //支持对象循环引用（否则会栈溢出）
        kryo.setReferences(true);
        //不强制要求注册类（注册行为无法保证多个 JVM 内同一个类的注册编号相同；而且业务系统中大量的 Class 也难以一一注册）
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public byte[] encode(Object obj) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();Output output = new Output(outputStream)) {
            Kryo kryo = kryoLocal.get();
            kryo.writeClassAndObject(output, obj);
            //手动remove 加快内存释放
            return output.toBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            kryoLocal.remove();
        }
        return null;
    }

    @Override
    public <T> T decoder(byte[] data, Class<T> clazz) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);Input input = new Input(inputStream)) {
            Kryo kryo = kryoLocal.get();
            Object o = kryo.readObject(input, clazz);
            //手动remove 加快内存释放
            kryoLocal.remove();
            return clazz.cast(o);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> T decoder(byte[] data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);Input input = new Input(inputStream)) {
            Kryo kryo = kryoLocal.get();
            return (T) kryo.readClassAndObject(input);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //手动remove 加快内存释放
            kryoLocal.remove();
        }
        return null;
    }

    @Override
    public <T> T decoder(InputStream inputStream, Class<T> clazz) {
        Kryo kryo = kryoLocal.get();
        Input input = new Input(inputStream);
        Object o = kryo.readObject(input, clazz);
        //手动remove 加快内存释放
        kryoLocal.remove();
        return clazz.cast(o);
    }


}
