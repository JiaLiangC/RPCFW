package raft.common;

import com.google.protobuf.ByteString;
import java.util.function.Supplier;

import static raft.common.utils.StringUtils.bytesToHexShortString;


//为啥用 ByteString ，因为其不可变，避免线程之间的同步，TODO 如果需要，哪些线程需要同步
public interface Message {

    static Message valueOf(ByteString bytes, Supplier<String> stringSupplier){
        return new Message() {

            @Override
            public ByteString getContent() {
                return bytes;
            }

            @Override
            public String toString() {
                return stringSupplier.get();
            }
        };
    }

    static  Message valueOf(ByteString bytes){
        return valueOf(bytes,()-> bytesToHexShortString(bytes));
    }

    static  Message valueOf(String  string){
        return valueOf(ByteString.copyFromUtf8(string),()->"Message:"+string);
    }

   ByteString getContent();



}
