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
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;




/**
 * netty client 代理
 *异步转同步,加大流量使得调用不阻塞，方便并发得请求，返回时阻塞
 * @author xiaoxiao
 * @date 2021/02/14
 */
//TODO channel pool and remove synchronized from connection
public class NettyClientProxy extends ClientProxy{
    public final static Logger LOG = LoggerFactory.getLogger(NettyClientProxy.class);

    private final Connection connection;
    private InetSocketAddress address;

    public NettyClientProxy(String host, int port) throws InterruptedException {
        LOG.info("NettyClientProxy init -------");
        this.address = NetUtils.createSocketAddrForHost(host,port);
        this.connection = new Connection(new NioEventLoopGroup());
    }

    public NettyClientProxy(InetSocketAddress address) {
        this.address = address;
        this.connection = new Connection(new NioEventLoopGroup());
    }


    public boolean isClientChannelClosed(){
        return   this.connection.isClientChannelClosed();
    }


    class  Connection implements Closeable {

        private  NettyClient client ;
        //private final Queue<CompletableFuture<RpcResponse>> replies = new LinkedList();
        private final Map<String,CompletableFuture<RpcResponse>> replies = new ConcurrentHashMap<>();
        private  final  EventLoopGroup group;

        Connection(EventLoopGroup group) {
            this.group =group;
            /* final ChannelInboundHandler inboundHandler =
                   new SimpleChannelInboundHandler<RpcResponse>() {
                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            LOG.error("异常信息：{}",cause.getMessage());
                            cause.printStackTrace();
                            super.exceptionCaught(ctx, cause);
                        }

                        @Override
                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
                            CompletableFuture<RpcResponse> future = poolReply();
                            if(future==null){
                                //TODO add rpc id for debug
                                throw  new IllegalStateException("Request not found");
                            }
                            future.complete(rpcResponse);
                        }
                    };*/

            clientInit();
        }

         synchronized void clientInit() {
            this.client = new NettyClient();
             final ChannelInboundHandler inboundHandler = new ChannelInboundHandlerAdapter(){
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    RpcResponse rpcResponse = (RpcResponse) msg;
                    CompletableFuture<RpcResponse> future = poolReply(rpcResponse.getUid());
                    if(future==null){
                        //TODO add rpc id for debug
                        LOG.info("Request not found------------");
                        //throw  new IllegalStateException("Request not found");
                    }
                    future.complete(rpcResponse);
                    ctx.channel().close().sync();
                    ReferenceCountUtil.release(msg);
                    //super.channelRead(ctx, msg);
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    LOG.error("异常信息：",cause);
                    super.exceptionCaught(ctx, cause);
                }
            };

             final ChannelInitializer<SocketChannel>  initializer = new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline()
                            .addLast(new LoggingHandler(LogLevel.DEBUG))
                            .addLast(new NettyProtocolEncoder())
                            .addLast(new NettyProtocolDecoder(Constants.MAX_FRAME_LENGTH))
                            .addLast(inboundHandler);
                }
            };

            try {
                client.connect(address,group,initializer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        synchronized CompletableFuture<RpcResponse> poolReply(String reqId){
            CompletableFuture<RpcResponse> r =  replies.get(reqId);
            replies.remove(reqId);
            return r;
        }

        synchronized ChannelFuture offer(RPCRequest rpcRequest, CompletableFuture<RpcResponse> reply){
            if(connection.isClientChannelClosed()){
                clientInit();
            }
            replies.put(rpcRequest.getUid(),reply);
            return   client.writeAndFlush(rpcRequest);
        }

        synchronized public boolean isClientChannelClosed(){
            return client.channelClosed();
        }

        synchronized void updateClient() {
            clientInit();
        }

        @Override
        public void close()  {
            replies.clear();
        }
    }

    @Override
    public <T> T getProxy(Class<T> clazz) {
        //TODO 报错，原因还未解决
        //  return (T) Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), ClientProxy.this);
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, NettyClientProxy.this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            RPCRequest rpcRequest = new RPCRequest.Builder().setInterfaceName(method.getDeclaringClass().getName())
                    .setMethodName(method.getName())
                    .setParameters(args)
                    .setParameterTypes(method.getParameterTypes())
                    .build();
            //Object result = client.sendRpcRequest(rpcRequest);
            RpcResponse result = send(rpcRequest);
            return result.getData();
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
        //return result.getData();
    }


    public RpcResponse send(RPCRequest rpcRequest) throws IOException {
        CompletableFuture<RpcResponse> reply = new CompletableFuture();
        ChannelFuture channelFuture = connection.offer(rpcRequest,reply);

        try {
            //TODO 阻塞
            channelFuture.sync();
            return reply.get();
        } catch (InterruptedException e) {
            if(isClientChannelClosed()){
                LOG.error("..................................");
                //connection.updateClient();

            }
            e.printStackTrace();
            //throw new IOException("send request Interrupted");
        } catch (ExecutionException e) {
            e.printStackTrace();
            //throw  new IOException("execute failed");
        }
        return null;
    }

    @Override
    public void close() {
        connection.close();
    }

}
