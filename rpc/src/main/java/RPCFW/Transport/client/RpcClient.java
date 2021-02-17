package RPCFW.Transport.client;

import RPCFW.Transport.common.RPCRequest;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public interface RpcClient {

    void connect(InetSocketAddress address);
    void connect(String host, int port);
    Object sendRpcRequest(RPCRequest rpcRequest);

    void close();
}
