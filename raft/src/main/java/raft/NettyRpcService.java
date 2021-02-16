package raft;

import RPCFW.Transport.client.ClientProxy;
import RPCFW.Transport.client.NettyClientProxy1;
import RPCFW.Transport.client.NettyClientProxy;
import RPCFW.Transport.server.NettyRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.common.RaftPeer;
import raft.common.id.RaftPeerId;
import raft.common.utils.NetUtils;
import raft.requestBean.AppendEntriesArgs;
import raft.requestBean.AppendEntriesReply;
import raft.requestBean.RequestVoteArgs;
import raft.requestBean.RequestVoteReply;

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
    private final Map<String, ClientProxy> proxyPeerMap=new ConcurrentHashMap<>();


    private  NettyRpcService(RaftServer raftServer){
        this.raftServer=raftServer;
        //TODO 从配置文件或者使用默认端口
        RaftPeerId id = raftServer.getId();
        RaftPeer peer = raftServer.getPeer(id);
        InetSocketAddress socketAddress = NetUtils.createSocketAddr( peer.getAddress(),-1);
        int port = socketAddress.getPort();
        this.rpcServer = new NettyRpcServer(port);
        // proxy = new NettyClientProxy(NetUtils.createSocketAddr(raftServer.getPeer(raftServer.getId()).getAddress(),-1));
    }

    @Override
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

    @Override
    public void newProxyAndSendRpcTest(){
            NettyClientProxy p = new NettyClientProxy(NetUtils.createSocketAddr(raftServer.getPeer(raftServer.getId()).getAddress(),-1));
            RaftService raftService = p.getProxy(RaftService.class);
            String res = raftService.rpcTest();
            LOG.info("-----------test rpc---------{}",res);
    }


    @Override
    public RequestVoteReply sendRequestVote(RequestVoteArgs args){

        if(!args.getCandidateId().equals(raftServer.getId().toString()) ){
            LOG.info("sendRequestVote error -----------------");
        }

        if(args.getReplyId().equals(raftServer.getId().toString())){
            LOG.info("sendRequestVote error -----------------");
        }

        RaftService raftService = getProxy(RaftPeerId.valueOf(args.getReplyId())).getProxy(RaftService.class);

        RequestVoteReply res = raftService.RequestVote(args);
        if(!res.getReplyId().equals(args.getReplyId())){
            LOG.info("sendRequestVote error -----------------");
        }
        LOG.info("sendRequestVote got replyTerm:{} repltVoted:{} ",res.getTerm(),res.isVoteGranted());
        return res;
    }

    @Override
    public AppendEntriesReply sendAppendEntries(AppendEntriesArgs args){
        //
            LOG.info("xxxxx");
        RaftService raftService = getProxy(RaftPeerId.valueOf(args.getReplyId())).getProxy(RaftService.class);
        AppendEntriesReply res = raftService.AppendEntries(args);
        return res;
    }

    @Override
    public String sendRpcTest(){
        RaftService raftService = proxy.getProxy(RaftService.class);
        String res = raftService.rpcTest();
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

    public ClientProxy getProxy(RaftPeerId peerId){
        if(peerId.toString().equals(raftServer.getId().toString())|| proxyPeerMap.size()==3){
            LOG.info("xxxxxxxx");
        }
        //return proxyPeerMap.computeIfAbsent(peerId.toString(),
         //       pid-> new NettyClientProxy(NetUtils.createSocketAddr(raftServer.getPeer(peerId).getAddress(),-1)));
        return  proxyPeerMap.computeIfAbsent(peerId.toString(),
                pid-> new NettyClientProxy1(NetUtils.createSocketAddr(raftServer.getPeer(peerId).getAddress(),-1)));
    }


}
