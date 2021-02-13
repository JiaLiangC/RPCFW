package raft;

import RPCFW.RPCDemo.Netty.EatServiceImpl;
import RPCFW.ServiceManager.registry.DefaultRegistry;
import RPCFW.Transport.server.NettyRpcServer;
import RPCFW.Transport.server.RpcServer;

import java.net.InetSocketAddress;


/**
 * raft 的 rpc 实现之 NettyRpc
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public class NettyRpcService implements RaftServerRpc {

    public static class Builder extends  RaftServerRpc.Builder<Builder,NettyRpcService>{
        private Builder(){

        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        NettyRpcService build() {
            return new NettyRpcService(getServer());
        }
    }

    public static Builder newBuilder(){
        return new Builder();
    }


    private final RaftServer raftServer;
    private final NettyRpcServer rpcServer;

    private  NettyRpcService(RaftServer raftServer){
        this.raftServer=raftServer;

        DefaultRegistry registry = new DefaultRegistry();
        registry.register(new RaftServiceImpl());

        this.rpcServer = new NettyRpcServer(8080);

    }


    @Override
    public void start() {
        rpcServer.start();
    }

    @Override
    public InetSocketAddress getInetSocketAddress() {
        return null;
    }

    @Override
    public String getRpcType() {
        return null;
    }
}
