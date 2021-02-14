package RPCFW.Transport.client;

import RPCFW.Transport.codec.NettyProtocolDecoder;
import RPCFW.Transport.codec.NettyProtocolEncoder;
import RPCFW.Transport.common.Constants;
import RPCFW.Transport.common.RPCRequest;
import RPCFW.Transport.common.RpcResponse;
import RPCFW.utils.NetUtils;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;




/**
 * netty client 代理
 *异步转同步,加大流量使得调用不阻塞，方便并发得请求，返回时阻塞
 * @author xiaoxiao
 * @date 2021/02/14
 */
public class NettyClientProxy implements InvocationHandler,Closeable{

    private final Connection connection;
    private InetSocketAddress address;

    public NettyClientProxy(String host, int port) throws InterruptedException {
        this.address = NetUtils.createSocketAddrForHost(host,port);
        this.connection = new Connection(new NioEventLoopGroup());
    }

    public NettyClientProxy(InetSocketAddress address) throws InterruptedException {
        this.address = address;
        this.connection = new Connection(new NioEventLoopGroup());
    }


    class  Connection implements Closeable {

        private final NettyClient client = new NettyClient();
        private final Queue<CompletableFuture<RpcResponse>> replies = new LinkedList();
        Connection(EventLoopGroup group) throws InterruptedException {
            final ChannelInboundHandler inboundHandler =
                    new SimpleChannelInboundHandler<RpcResponse>() {

                        @Override
                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
                            CompletableFuture<RpcResponse> future = poolReply();
                            if(future==null){
                                //TODO add rpc id for debug
                                throw  new IllegalStateException("Request not found");
                            }
                            future.complete(rpcResponse);
                        }
                    };

            final ChannelInitializer<SocketChannel> initializer= new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline()
                            .addLast(new LoggingHandler(LogLevel.DEBUG))
                            .addLast(new NettyProtocolEncoder())
                            .addLast(new NettyProtocolDecoder(Constants.MAX_FRAME_LENGTH))
                            .addLast(inboundHandler);
                }
            };

            client.connect(address,group,initializer);
        }

        CompletableFuture<RpcResponse> poolReply(){
            return replies.poll();
        }

        ChannelFuture offer(RPCRequest rpcRequest, CompletableFuture<RpcResponse> reply){
            replies.offer(reply);
            return client.writeAndFlush(rpcRequest);
        }


        @Override
        public void close()  {
            replies.clear();
        }
    }

    public <T> T getProxy(Class<T> clazz) {
        //TODO 报错，原因还未解决
        //  return (T) Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), ClientProxy.this);
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, NettyClientProxy.this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RPCRequest rpcRequest = new RPCRequest.Builder().setInterfaceName(method.getDeclaringClass().getName())
                .setMethodName(method.getName())
                .setParameters(args)
                .setParameterTypes(method.getParameterTypes())
                .build();
        //Object result = client.sendRpcRequest(rpcRequest);
        RpcResponse result = send(rpcRequest);
        return result.getData();
    }


    public RpcResponse send(RPCRequest rpcRequest) throws IOException {
        CompletableFuture<RpcResponse> reply = new CompletableFuture();
        ChannelFuture channelFuture = connection.offer(rpcRequest,reply);

        try {
            //TODO 阻塞
            channelFuture.sync();
            return reply.get();
        } catch (InterruptedException e) {
            throw new IOException("send request Interrupted");
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw  new IOException("execute failed");
        }
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }

}
