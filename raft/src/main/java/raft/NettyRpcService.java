package raft;

import RPCFW.RPCDemo.Netty.EatServiceImpl;
import RPCFW.ServiceManager.registry.DefaultRegistry;
import RPCFW.Transport.server.NettyRpcServer;
import RPCFW.Transport.server.RpcServer;
import raft.common.RaftPeer;
import raft.common.id.RaftId;
import raft.common.id.RaftPeerId;
import raft.common.utils.NetUtils;

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
        //TODO 从配置文件或者使用默认端口
        RaftPeerId id = raftServer.getId();
        RaftPeer peer = raftServer.getPeer(id);
        InetSocketAddress socketAddress = NetUtils.createSocketAddr( peer.getAddress(),-1);
        int port = socketAddress.getPort();
        this.rpcServer = new NettyRpcServer(port);
    }


    @Override
    public void start() {
        rpcServer.start();
    }

    @Override
    public InetSocketAddress getInetSocketAddress() {
        //TODO
        return null;
    }

    @Override
    public String getRpcType() {
        return null;
    }
}
