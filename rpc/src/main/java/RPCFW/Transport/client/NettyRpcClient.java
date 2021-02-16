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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class NettyRpcClient implements RpcClient{
    private static final Logger logger = LoggerFactory.getLogger(NettyRpcClient.class);
    private String host;
    private InetSocketAddress addr;
    private int port;
    private Bootstrap bootstrap;
    NioEventLoopGroup group = new NioEventLoopGroup();
    private ChannelFuture serverChannel;

    public NettyRpcClient(InetSocketAddress address) {
    //public NettyRpcClient(String host, int port) {
        //this.host = host;
        this.addr=address;
        this.port = address.getPort();
        this.bootstrap = new Bootstrap();
         bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel)  {
                        socketChannel.pipeline()
                                .addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new NettyProtocolEncoder())
                                .addLast(new NettyProtocolDecoder(Constants.MAX_FRAME_LENGTH))
                                .addLast(new RpcClientHandler());
                    }
                });//.connect(address);

    }

    @Override
    public void connect(InetSocketAddress address){

        //TODO netty 异步操作返回的future
        try {
            serverChannel = bootstrap.connect(address).sync();
            logger.info("rpc client connected to server success");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect(String host, int port){
        //TODO netty 异步操作返回的future
        try {
            serverChannel = bootstrap.connect().sync();
            logger.info("rpc client connected to server success");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    @Override
    public Object sendRpcRequest(RPCRequest rpcRequest){
        try {
            ChannelFuture serverChannel = bootstrap.connect(addr).sync();
            Channel channel = serverChannel.channel();
            if(channel!=null){
                channel.writeAndFlush(rpcRequest).addListener(future->{
                    if (future.isSuccess()){
                        logger.info("send data success");
                    }else{

                        logger.error("send failed:{} addr {}",rpcRequest.getMethodName(),port);
                    }
                });

                channel.closeFuture().sync();
                AttributeKey<RpcResponse> attributeKey = AttributeKey.valueOf("rpcResponse");
                RpcResponse rpcResponse =  channel.attr(attributeKey).get();
                logger.info("sendRpcRequest finished received msg: {}, {}",rpcResponse.getCode(),rpcResponse.getData());
                return rpcResponse.getData();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    public void close() {
        serverChannel.channel().close();
    }

}
