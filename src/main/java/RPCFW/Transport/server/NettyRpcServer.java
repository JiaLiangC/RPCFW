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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyRpcServer implements RpcServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);

    private int port;

    public NettyRpcServer(int port) {
        this.port = port;
    }


    @Override
    public void start() {
        //boos 线程组负责处理请求
        EventLoopGroup boss = new NioEventLoopGroup();
        //worker 线程组负责处理工作任务
        EventLoopGroup work = new NioEventLoopGroup();
        try {
            //服务启动对象
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, work)
                    //设置channel 类型
                    .channel(NioServerSocketChannel.class)
                    //设置线程队列得到连接个数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //设置保持活动连接状态
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //使用匿名内部类的形式初始化通道对象
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel
                                    .pipeline()
                                    .addLast(new NettyProtocolDecoder(Constants.MAX_FRAME_LENGTH))
                                    .addLast(new NettyProtocolEncoder())
                                    .addLast((new RpcServerHandler()));

                        }
                    });

            ChannelFuture channelFutur = serverBootstrap.bind(port).sync();
            channelFutur.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();

        }
    }

    public void shutDown() {
    }
}
