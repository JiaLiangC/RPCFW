package RPCFW.Transport.handler;

import RPCFW.Transport.common.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        AttributeKey<RpcResponse> attributeKey = AttributeKey.valueOf("rpcResponse");
        RpcResponse rpcResponse = (RpcResponse) msg;
        logger.info("RpcClientHandler received message {}",rpcResponse.getCode());
        ctx.channel().attr(attributeKey).set(rpcResponse);
        ctx.channel().close().sync();
        ReferenceCountUtil.release(msg);
    }
}
