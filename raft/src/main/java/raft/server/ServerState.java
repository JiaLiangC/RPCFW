package raft.server;

import raft.common.RaftGroup;
import raft.common.RaftPeer;
import raft.common.RaftProperties;
import raft.common.TransactionContext;
import raft.common.id.ClientId;
import raft.common.id.RaftPeerId;
import raft.server.RaftServerImpl;
import raft.statemachine.StateMachine;
import raft.storage.MemLog;
import raft.storage.RaftLog;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

//为啥引入其他地方static的log包
import static raft.server.RaftServerImpl.LOG;
/**
 * raft 服务状态
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public class ServerState {
    private final ReentrantLock lock = new ReentrantLock();
    //private Set<RaftPeer> peers;
    private int currentTerm;

    private  RaftPeerId votedFor;

    private RaftPeerId leaderId;
    private RaftPeerId selfId;
    private RaftServerImpl server;
    private RaftGroup group;

    private final RaftLog raftLog;


    public  ServerState(RaftPeerId peerId, RaftGroup group, RaftProperties properties, RaftServerImpl server, StateMachine stateMachine){
        this.selfId=peerId;
        this.server=server;
        this.leaderId=null;
        this.currentTerm=0;
        this.votedFor=null;
        this.group = group;
        this.raftLog=new MemLog(peerId);
    }

    public int getPeersCount(){
        lock.lock();
        int size = group.getRaftPeers().size();
        lock.unlock();
        return size;
    }


    public void becomLeader(){
        setLeader(selfId,"becomLeader");
    }


    public void setLeader(RaftPeerId newLeaderId,String operation){
        if(!Objects.equals(newLeaderId,leaderId)){
            LOG.info("server:{} change leader from {} to {} at term {}, for {}",selfId,
                    leaderId,newLeaderId,getCurrentTerm(),operation);
            leaderId=newLeaderId;
        }
    }

    synchronized public int initEleciton(){
        LOG.info("server:{} initEleciton term:{}",selfId, currentTerm+1);
        votedFor=selfId;
        setLeader(null,"initEleciton");
        return  ++currentTerm;
    }

    public Collection<RaftPeer> getPeers() {
        return group.getRaftPeers();
    }


    public int getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(int currentTerm) {
        this.currentTerm = currentTerm;
    }

    public RaftPeerId getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(RaftPeerId votedFor) {
        this.votedFor = votedFor;
    }


    public void setLeaderId(RaftPeerId leaderId) {
        this.leaderId = leaderId;
    }

    public RaftPeerId getLeaderId() {
        return leaderId;
    }

    public RaftPeerId getSelfId() {
        return selfId;
    }

    public void setSelfId(RaftPeerId selfId) {
        this.selfId = selfId;
    }

    public Collection<RaftPeer> getOtherPeers(){
        return group.getRaftPeers().stream().filter((peer)->!peer.getId().toString().equals(getSelfId().toString())).collect(Collectors.toList());
    }

    public long applyLog(TransactionContext context, ClientId clientId,long callId){
        return raftLog.append(currentTerm,context,clientId,callId);
    }

}
