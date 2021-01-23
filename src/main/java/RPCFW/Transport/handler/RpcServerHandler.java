package RPCFW.Transport.handler;

import RPCFW.ServiceManager.registry.DefaultRegistry;
import RPCFW.Transport.common.RPCRequest;
import RPCFW.Transport.common.RpcResponse;
import RPCFW.Transport.common.RpcResponseCode;
import RPCFW.Transport.server.RpcServer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;


public class RpcServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        try {
            RpcResponse rpcResponse ;
            RPCRequest rpcRequest = (RPCRequest) msg;
            logger.info("server received messgae {}",rpcRequest);
            Object service = new DefaultRegistry().getService(rpcRequest.getInterfaceName());
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParameterTypes());

            if (method==null){
                rpcResponse =  RpcResponse.fail(RpcResponseCode.MethodNotFound);
            }
            Object res = method.invoke(service,rpcRequest.getParameters());
            rpcResponse = RpcResponse.success(res);
            ChannelFuture channelFuture = ctx.writeAndFlush(rpcResponse);
            //异步回调结束后自动关闭channel
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }finally {
            //手动释放对象，加速内存回收
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server catch exception: {}",cause.getMessage());
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
