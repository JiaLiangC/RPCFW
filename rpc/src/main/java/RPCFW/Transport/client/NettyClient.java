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

import java.net.InetSocketAddress;

public class NettyClient implements RpcClient{

    private Channel channel;

    void connect(InetSocketAddress socketAddress, EventLoopGroup group, ChannelInitializer initializer) throws InterruptedException {
        channel = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(initializer)
                .connect(socketAddress).sync().channel();
    }

    ChannelFuture writeAndFlush(Object msg){
        return channel.writeAndFlush(msg);
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
