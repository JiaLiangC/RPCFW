package RPCFW.Transport.client;

import RPCFW.Transport.codec.NettyProtocolDecoder;
import RPCFW.Transport.codec.NettyProtocolEncoder;
import RPCFW.Transport.common.Constants;
import RPCFW.Transport.common.RPCRequest;
import RPCFW.Transport.common.RpcResponse;
import RPCFW.Transport.handler.RpcClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyRpcClient implements RpcClient{
    private static final Logger logger = LoggerFactory.getLogger(NettyRpcClient.class);
    private String host;
    private int port;
    private Bootstrap bootstrap;
    NioEventLoopGroup group = new NioEventLoopGroup();

    public NettyRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new NettyProtocolEncoder())
                                .addLast(new NettyProtocolDecoder(Constants.MAX_FRAME_LENGTH))
                                .addLast(new RpcClientHandler());
                    }
                });
    }


    @Override
    public Object sendRpcRequest(RPCRequest rpcRequest){
        try {
            ChannelFuture channelFuture = bootstrap.connect(host,port).sync();
            Channel channel =channelFuture.channel();
            if(channel!=null){
                channel.writeAndFlush(rpcRequest).addListener(future->{
                    if (future.isSuccess()){
                        logger.info("send data success");
                    }else{
                        logger.error("send failed");
                    }
                });

                channel.closeFuture().sync();
                AttributeKey<RpcResponse> attributeKey = AttributeKey.valueOf("rpcResponse");
                RpcResponse rpcResponse =  channel.attr(attributeKey).get();
                return rpcResponse.getData();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;

    }

}
