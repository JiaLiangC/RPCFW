package raft;

import RPCFW.RPCDemo.Netty.EatServiceImpl;
import RPCFW.ServiceManager.registry.DefaultRegistry;
import RPCFW.Transport.client.NettyClientProxy;
import RPCFW.Transport.server.NettyRpcServer;
import RPCFW.Transport.server.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.common.RaftPeer;
import raft.common.id.RaftId;
import raft.common.id.RaftPeerId;
import raft.common.utils.NetUtils;
import raft.requestBean.AppendEntriesArgs;
import raft.requestBean.AppendEntriesReply;
import raft.requestBean.RequestVoteArgs;
import raft.requestBean.RequestVoteReply;

import java.net.InetSocketAddress;


/**
 * raft 的 rpc 实现之 NettyRpc
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public class NettyRpcService implements RaftServerRpc {

    private static final Logger LOG = LoggerFactory.getLogger(NettyRpcService.class);

    private NettyClientProxy proxy;

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
        try {
            proxy = new NettyClientProxy(NetUtils.createSocketAddr(raftServer.getPeer(raftServer.getId()).getAddress(),-1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public RequestVoteReply sendRequestVote(RaftPeerId peerId, RequestVoteArgs args){
        RaftService raftService = proxy.getProxy(RaftService.class);
        RequestVoteReply res = raftService.RequestVote(args);
        LOG.info("sendRequestVote got replyTerm:{} repltVoted:{} ",res.getTerm(),res.isVoteGranted());
        return res;
    }

    @Override
    public AppendEntriesReply sendAppendEntries(RaftPeerId peerId, AppendEntriesArgs args){
        RaftService raftService = proxy.getProxy(RaftService.class);
        AppendEntriesReply res = raftService.AppendEntries(args);
        return res;
    }


    //Note this will block
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
