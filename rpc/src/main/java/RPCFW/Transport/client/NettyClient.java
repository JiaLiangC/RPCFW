package RPCFW.Transport.client;

import RPCFW.Transport.common.RPCRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class NettyClient implements RpcClient{

    private final static Logger LOG = LoggerFactory.getLogger(NettyClient.class);
    private Channel channel;
    private  InetSocketAddress  address;

    void connect(InetSocketAddress socketAddress, EventLoopGroup group, ChannelInitializer initializer) throws InterruptedException {
        this.address = socketAddress;
        channel = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(initializer)
                .connect(socketAddress).sync().channel();

    }

    ChannelFuture writeAndFlush(Object msg){
        if(!channel.isOpen()){
            LOG.error("----------------------Channe closed ");
        }
        return channel.writeAndFlush(msg);
    }

    public boolean channelClosed(){
        return !channel.isOpen();
    }


    @Override
    public void connect(InetSocketAddress address) {

    }

    @Override
    public void connect(String host, int port) {

    }

    @Override
    public Object sendRpcRequest(RPCRequest rpcRequest) {
        return null;
    }

    @Override
    public void close() {
        channel.close();
    }
}
