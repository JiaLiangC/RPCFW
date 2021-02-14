package RPCFW.utils;

import java.io.IOException;
import java.net.*;
import java.util.Objects;

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


    /**
     * create a InetSocketAddress from given target and default port
     * @param target string of either "host", "host:port", "schema://host:port/path"
     * @param defaultPort
     * @return
     */
    public static InetSocketAddress createSocketAddr(String target , int defaultPort){
        Objects.requireNonNull(target,"target == null");
        boolean hasSchema = target.contains("://");
        final URI uri;
        try {
            uri = new URI(hasSchema ?  target: "dummy://"+target);
        } catch (URISyntaxException e) {
            throw  new IllegalArgumentException("failed create URI from target: "+target,e);
        }

        final String host= uri.getHost();
        int port = uri.getPort();
        if(port==-1){
            port=defaultPort;
        }

        final String path = uri.getPath();

        if(host==null){
            throw new IllegalArgumentException("host is null in " + target);
        }else if(port<0){
            throw new IllegalArgumentException("port is negative  in " + port);
        }else if(!hasSchema && path!=null && !path.isEmpty()){
            throw new IllegalArgumentException("Illegal path in target "+target);
        }
        return createSocketAddrForHost(host,port);
    }

    public static InetSocketAddress createSocketAddrForHost(String host ,int port){
        InetSocketAddress addr =  new InetSocketAddress(host,port);
        return addr;
    }
}
