package raft;

import RPCFW.ServiceManager.registry.DefaultRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.common.RaftGroup;
import raft.common.RaftPeer;
import raft.common.RaftProperties;
import raft.common.id.RaftPeerId;
import raft.requestBean.AppendEntriesReply;
import raft.requestBean.RequestVoteReply;

import java.net.InetSocketAddress;
import java.util.UUID;


/**
 * raft 代理，统一整合raft的所有服务
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public class RaftServerProxy implements RaftServer {

    private final RaftPeerId id;
    private final StateMachine stateMachine;
    private final RaftProperties raftProperties;
    private final RaftServerRpc raftServerRpc;
    private final RaftServerImpl raftServerImpl;

    private static final Logger LOG = LoggerFactory.getLogger(RaftServerProxy.class);

    public RaftServerProxy(RaftPeerId id, StateMachine stateMachine, RaftGroup group, RaftProperties properties){
        this.stateMachine = stateMachine;
        this.raftProperties=properties;
        this.raftServerRpc = NettyRpcService.newBuilder().setServer(this).build();
        this.id = id!=null ? id : RaftPeerId.valueOf(getIdFrom(raftServerRpc));
        this.raftServerImpl = new RaftServerImpl(id, group, this, properties);
    }

    public String getIdFrom(RaftServerRpc serverRpc){
        InetSocketAddress address =null;
        address  =   serverRpc.getInetSocketAddress();
        return address!=null ? address.getHostName()+":"+address.getPort() : serverRpc.getRpcType() +"-"+ UUID.randomUUID();
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }


    @Override
    public void start() {
        LOG.info("raft: {} server started",raftServerImpl.serverState.getSelfId().getString());
        raftServerImpl.start();
        raftServerRpc.start();
    }

    @Override
    public RaftPeer getPeer(RaftPeerId id) {
        return raftServerImpl.getPeer(id);
    }

    @Override
    public RaftPeerId getId() {
        return id;
    }


}
