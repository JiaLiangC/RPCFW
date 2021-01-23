package RPCFW.Transport.Serializer;

import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {

     public  byte[] encode(Object o);

     default  byte[] encode(Object o, OutputStream outputStream){
          return null;
     }

     <T> T decoder(byte[] data,Class<T> clazz);

     default <T> T  decoder(InputStream in, Class<T> clazz){
          return null;
     };


}
