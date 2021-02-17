package RPCFW.Transport.client;

import RPCFW.Transport.codec.NettyProtocolDecoder;
import RPCFW.Transport.codec.NettyProtocolEncoder;
import RPCFW.Transport.common.Constants;
import RPCFW.Transport.common.RPCRequest;
import RPCFW.Transport.common.RpcResponse;
import RPCFW.utils.NetUtils;
import com.github.rholder.retry.Retryer;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Map;
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
    private volatile boolean disconnect;

    public NettyClientProxy(String host, int port) throws InterruptedException {
        LOG.info("NettyClientProxy init -------");
        this.address = NetUtils.createSocketAddrForHost(host,port);
        this.connection = new Connection(new NioEventLoopGroup());
    }

    public NettyClientProxy(InetSocketAddress address) {
        this.address = address;
        this.connection = new Connection(new NioEventLoopGroup());
    }


    class  Connection implements Closeable {

        private  NettyClient client ;
        //private final Queue<CompletableFuture<RpcResponse>> replies = new LinkedList();
        private final Map<String,CompletableFuture<RpcResponse>> replies = new ConcurrentHashMap<>();
        private  final  EventLoopGroup group;

        Connection(EventLoopGroup group) {
            this.group =group;
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
                    //don't closed the channel
                    //ctx.channel().close().sync();
                    ReferenceCountUtil.release(msg);
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
           /* if(connection.isClientChannelClosed()){
                clientInit();
            }*/
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
        RPCRequest rpcRequest = new RPCRequest.Builder().setInterfaceName(method.getDeclaringClass().getName())
                .setMethodName(method.getName())
                .setParameters(args)
                .setParameterTypes(method.getParameterTypes())
                .build();
        //Object result = client.sendRpcRequest(rpcRequest);
        RpcResponse result = send(rpcRequest);
        return result.getData();
    }



    synchronized public boolean isDisconnect() {
        return disconnect;
    }

    @Override
    synchronized public void disConnect(boolean disconnect) {
        this.disconnect = disconnect;
    }

    public RpcResponse send(RPCRequest rpcRequest) throws ExecutionException, InterruptedException {
        if (isDisconnect()){
            throw new IllegalStateException(" network unhealthy,can't reach");
        }
        int retryTims=3;
        while (retryTims>0){
            try {
                CompletableFuture<RpcResponse> reply = new CompletableFuture();
                ChannelFuture channelFuture = connection.offer(rpcRequest,reply);
                //TODO 阻塞
                channelFuture.sync();
                return reply.get();
            }catch (Exception e){
                if (e instanceof ClosedChannelException){
                    connection.clientInit();
                    LOG.error("ClosedChannelException reinitialize client");
                }else {
                    throw e;
                }
            }
            Thread.sleep(100);
            retryTims--;
        }
        return null;
    }

    @Override
    public void close() {
        connection.close();
    }

}
