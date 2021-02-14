package RPCFW.Transport.server;

import RPCFW.Transport.codec.NettyProtocolDecoder;
import RPCFW.Transport.codec.NettyProtocolEncoder;
import RPCFW.Transport.common.Constants;
import RPCFW.Transport.handler.RpcServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyRpcServer implements RpcServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);

    private int port;

    private final ChannelFuture channelFuture;
    //boos 线程组负责处理请求
    private final EventLoopGroup boss = new NioEventLoopGroup();
    //worker 线程组负责处理工作任务
    private final EventLoopGroup work = new NioEventLoopGroup();
    public NettyRpcServer(int port) {
        this.port = port;

        //服务启动对象
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss, work)
                //设置channel 类型
                .channel(NioServerSocketChannel.class)
                //设置线程队列得到连接个数
                .option(ChannelOption.SO_BACKLOG, 128)
                //设置保持活动连接状态
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                //使用匿名内部类的形式初始化通道对象
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new NettyProtocolDecoder(Constants.MAX_FRAME_LENGTH))
                                .addLast(new NettyProtocolEncoder())
                                .addLast((new RpcServerHandler()));

                    }
                });

        channelFuture = serverBootstrap.bind(port);
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void start() {
        logger.info("RPC server  started success on port {}",port);
        channelFuture.syncUninterruptibly();
    }

    public void shutDown() {
        boss.shutdownGracefully();
        work.shutdownGracefully();
        final ChannelFuture future =  channelFuture.awaitUninterruptibly().channel().close();
        future.syncUninterruptibly();
    }
}
