package raft.common.utils;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class NetUtils {
    public static InetSocketAddress createLocalServerAddress(){
        try(ServerSocket s= new ServerSocket()) {
            s.setReuseAddress(true);
            //分配临时地址
            s.bind(null);
            return (InetSocketAddress)s.getLocalSocketAddress();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

   public static String address2String(InetSocketAddress address){
        final StringBuilder builder = new StringBuilder(address.getHostName());
        if(address.getAddress() instanceof Inet6Address){
            builder.insert(0,'[').append(']');
        }
        return builder.append(':').append(address.getPort()).toString();
    }
}
