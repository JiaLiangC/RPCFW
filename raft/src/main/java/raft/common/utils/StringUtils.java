package raft.common.utils;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;
import java.util.Locale;

public class StringUtils {
    public static  String bytesToHexShortString(ByteString byteString){
        final int size = byteString.size();
        if(size==0){
            return "<EMPTY>";
        }else if(size>10){
            return bytesToHexString(byteString.substring(0,10))+"...(size=" +size+ ")";
        }else {
            return bytesToHexString(byteString);
        }
    }

    public static  String bytesToHexString(ByteString bytes){
        return bytesToHexString(bytes.asReadOnlyByteBuffer());
    }

    public static  String bytesToHexString(byte[] bytes){
        return bytesToHexString(ByteBuffer.wrap(bytes));
    }

    public static  String bytesToHexString(ByteBuffer bytes){

        final StringBuilder builder = new StringBuilder(2*bytes.remaining());
        for(;bytes.remaining()>0;){
            builder.append(String.format(Locale.ENGLISH,"%02x",bytes.get()));
        }
        bytes.flip();
        return builder.toString();
    }
}
