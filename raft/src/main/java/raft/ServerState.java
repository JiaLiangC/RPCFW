package raft;

import RPCFW.Transport.server.NettyRpcServer;
import com.esotericsoftware.minlog.Log;
import org.slf4j.LoggerFactory;
import raft.common.RaftGroup;
import raft.common.RaftPeer;
import raft.common.RaftProperties;
import raft.common.id.RaftPeerId;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

//为啥引入其他地方static的log包
import static raft.RaftServerImpl.LOG;
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
    private RaftRole role;
    private RaftServerImpl server;
    private RaftGroup group;


    public int initEleciton(){
        votedFor=selfId;
        setLeader(null,"initEleciton");
        return  ++currentTerm;
    }

    public  ServerState(RaftPeerId peerId, RaftGroup group, RaftProperties properties,RaftServerImpl server,StateMachine stateMachine){
        this.selfId=peerId;
        this.server=server;
        this.leaderId=null;
        this.currentTerm=0;
        this.votedFor=null;
        this.group = group;
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
            LOG.info("{} change leader from {} to {} at term {}, for {}",selfId,
                    leaderId,newLeaderId,getCurrentTerm(),operation);
            leaderId=newLeaderId;
        }
    }


    public Collection<RaftPeer> getPeers() {
        return group.getRaftPeers();
    }


    public RaftRole getRole() {
        return role;
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
}
