package raft.rpc;

import RPCFW.Transport.client.ClientProxy;
import RPCFW.Transport.client.NettyClientProxy;
import RPCFW.Transport.server.NettyRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.common.Preconditions;
import raft.common.RaftPeer;
import raft.common.id.RaftPeerId;
import raft.common.utils.NetUtils;
import raft.requestBean.AppendEntriesArgs;
import raft.requestBean.AppendEntriesReply;
import raft.requestBean.RequestVoteArgs;
import raft.requestBean.RequestVoteReply;
import raft.server.RaftServer;
import raft.server.RaftService;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * raft 的 rpc 实现之 NettyRpc
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public class NettyRpcService implements RaftServerRpc {
    private static final Logger LOG = LoggerFactory.getLogger(NettyRpcService.class);


    public static class Builder extends  RaftServerRpc.Builder<Builder,NettyRpcService>{
        private Builder(){
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public NettyRpcService build() {
            return new NettyRpcService(getServer());
        }
    }

    public static Builder newBuilder(){
        return new Builder();
    }


    private final RaftServer raftServer;
    private final NettyRpcServer rpcServer;
    private final Map<String, ClientProxy> proxyPeerMap=new ConcurrentHashMap<>();
    private final InetSocketAddress selfAddress;


    private  NettyRpcService(RaftServer raftServer){
        this.raftServer=raftServer;
        //TODO 从配置文件或者使用默认端口
        RaftPeerId id = raftServer.getId();
        RaftPeer peer = raftServer.getPeer(id);
        InetSocketAddress socketAddress = NetUtils.createSocketAddr( peer.getAddress(),-1);
        selfAddress =socketAddress;
        int port = socketAddress.getPort();
        this.rpcServer = new NettyRpcServer(port);
    }

    public void initProxyMap(Collection<RaftPeer> peers){
        peers.forEach(peer -> {
            if (!peer.getId().toString().equals(raftServer.getId().toString())){
                proxyPeerMap.computeIfAbsent(peer.getId().toString(),pid-> {
                            return new NettyClientProxy(NetUtils.createSocketAddr(peer.getAddress(),-1));
                        }
                );
            }
        });
    }

    public void newProxyAndSendRpcTest(){
            NettyClientProxy p = new NettyClientProxy(NetUtils.createSocketAddr(raftServer.getPeer(raftServer.getId()).getAddress(),-1));
            RaftService raftService = p.getProxy(RaftService.class);
            String res = raftService.rpcTest();
            LOG.info("-----------test rpc---------{}",res);
    }


    @Override
    public RequestVoteReply sendRequestVote(RequestVoteArgs args){
        Preconditions.assertTrue(args.getCandidateId().equals(raftServer.getId().toString()),"RequestVoteArgs error");
        Preconditions.assertTrue(!args.getReplyId().equals(raftServer.getId().toString()),"RequestVoteArgs error");

        RaftService raftService = getProxy(RaftPeerId.valueOf(args.getReplyId())).getProxy(RaftService.class);
        try {
            RequestVoteReply res = raftService.RequestVote(args);
            Preconditions.assertTrue(res.getReplyId().equals(args.getReplyId()),"reply id error");
            return res;
        }catch (Exception e){
            LOG.error("sendRequestVote failed");
        }
        return null;
    }

    @Override
    public AppendEntriesReply sendAppendEntries(AppendEntriesArgs args){
        RaftService raftService = getProxy(RaftPeerId.valueOf(args.getReplyId())).getProxy(RaftService.class);
        try{
            AppendEntriesReply res = raftService.AppendEntries(args);
            return res;
        } catch (Exception e){
            LOG.error("sendAppendEntries failed");
        }
        return null;
    }


    //Note this will block
    @Override
    public void start() {
        rpcServer.start();
    }

    @Override
    public InetSocketAddress getInetSocketAddress() {
        return selfAddress;
    }

    @Override
    public String getRpcType() {
        return null;
    }

    public ClientProxy getProxy(RaftPeerId peerId){
        Preconditions.assertTrue(!peerId.toString().equals(raftServer.getId().toString()),"getProxy error");
        return  proxyPeerMap.computeIfAbsent(peerId.toString(),
                pid-> new NettyClientProxy(NetUtils.createSocketAddr(raftServer.getPeer(peerId).getAddress(),-1)));
    }


    //设置为true 断开连接，false 恢复连接
    @Override
    public void disconnectProxy(RaftPeerId peerId,boolean connect){
        proxyPeerMap.computeIfAbsent(peerId.toString(),
                pid-> new NettyClientProxy(NetUtils.createSocketAddr(raftServer.getPeer(peerId).getAddress(),-1)));
        proxyPeerMap.get(peerId.toString()).disConnect(connect);
    }


}