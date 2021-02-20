package raft.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.rpc.NettyRpcService;
import raft.rpc.RaftServerRpc;
import raft.common.RaftGroup;
import raft.common.RaftPeer;
import raft.common.RaftProperties;
import raft.common.id.RaftPeerId;
import raft.statemachine.StateMachine;

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


    //TODO serverstate 线程安全重构

    public RaftServerProxy(RaftPeerId id, StateMachine stateMachine, RaftGroup group, RaftProperties properties){
        this.id = id;
        this.raftServerImpl = new RaftServerImpl(id, group, this, properties);
        this.stateMachine = stateMachine;
        this.raftProperties=properties;
        //TODO 重构 raftServerRpc 中 port 解析依赖于 raftServerImpl
        this.raftServerRpc = NettyRpcService.newBuilder().setServer(this).build();
        //this.id = id!=null ? id : RaftPeerId.valueOf(getIdFrom(raftServerRpc));
        //raftServerRpc.initProxyMap(group.getRaftPeers());
    }

    public String getIdFrom(RaftServerRpc serverRpc){
        InetSocketAddress address  =   serverRpc.getInetSocketAddress();
        return address!=null ? address.getHostName()+":"+address.getPort() : serverRpc.getRpcType() +"-"+ UUID.randomUUID();
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public RaftServerRpc getServerRpc(){
        return this.raftServerRpc;
    }


    @Override
    public void start() {
        LOG.info("raft: {} server started",raftServerImpl.serverState.getSelfId());
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

    @Override
    public int getCurrentTerm() {
        return raftServerImpl.serverState.getCurrentTerm();
    }

    public RaftServerImpl getServerImpl(){
        return raftServerImpl;
    }

}
