package raft;


import RPCFW.ServiceManager.registry.DefaultRegistry;
import RPCFW.Transport.client.ClientProxy;
import RPCFW.Transport.client.NettyClientProxy;
import RPCFW.Transport.client.NettyRpcClient;
import RPCFW.Transport.client.RpcClient;
import RPCFW.Transport.server.NettyRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.common.Preconditions;
import raft.common.RaftGroup;
import raft.common.RaftPeer;
import raft.common.RaftProperties;
import raft.common.id.RaftGroupId;
import raft.common.id.RaftPeerId;
import raft.common.utils.NetUtils;
import raft.requestBean.AppendEntriesArgs;
import raft.requestBean.AppendEntriesReply;
import raft.requestBean.RequestVoteArgs;
import raft.requestBean.RequestVoteReply;

import java.util.Map;
import java.util.concurrent.*;


/**
 * raft的具体实现，心跳，选举等
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public class RaftServerImpl {

    public static final Logger LOG =LoggerFactory.getLogger(RaftServerImpl.class);

    public int MaxTimeOutMs = 1200;
    public int MinTimeOutMs = 600;

    RaftConfiguration configuration;

    ServerState serverState;
    ExecutorService executorService ;
    private final RaftGroupId groupId;
    private  final RaftServerProxy proxy;
    private RaftRole role;

    private volatile LeaderElection electionDaemon;
    private  volatile FollowerState heartBeatMonitor;
    private  volatile LeaderState leaderState;

    private final Map<RaftPeerId,RaftPeer> peerMap = new ConcurrentHashMap<>();


    public ServerState getServerState() {
        return serverState;
    }

    RaftServerImpl(RaftPeerId peerId, RaftGroup group, RaftServerProxy proxy, RaftProperties properties){
        this.groupId=group.getRaftGroupId();
        this.proxy=proxy;
        this.serverState=new ServerState(peerId,group,properties,this,proxy.getStateMachine());
        setPeerMap(group);
    }

    void setPeerMap(RaftGroup raftGroup){
        raftGroup.getRaftPeers().forEach(peer->{
            peerMap.computeIfAbsent(peer.getId(),k->peer);
        });
    }

    public RaftPeer getPeer(RaftPeerId id){
        return peerMap.get(id);
    }

    public RaftServerRpc getServrRpc(){
        return proxy.getServerRpc();
    }

    private void startAsFollower(){
        setRole(RaftRole.Follower,"startAsFollower");
        startHeartBeatMonitor();
    }

    public void setRole(RaftRole newRole,String op) {
        if(newRole!=role){
            LOG.info("change role from {} to {} at Term {} for {}",role,newRole,serverState.getCurrentTerm(),op);
            this.role = newRole;
        }
    }


    public void startLeaderElection(){
        electionDaemon = new LeaderElection(this);
        electionDaemon.start();
    }


    public void startHeartBeatMonitor(){
        heartBeatMonitor = new FollowerState(this);
        heartBeatMonitor.start();
    }

    public void  shutdownElectiondaemon(){
        LeaderElection leaderElection = electionDaemon;
        if(leaderElection!=null){
            leaderElection.stopRunning();
        }
        electionDaemon=null;
    }


    public void shutDonwHeartBeatMonitor(){
        if(heartBeatMonitor!=null){
            heartBeatMonitor.stopRunning();
        }
        heartBeatMonitor=null;
    }

    public void shutDownLeaderState(){
        if(leaderState!=null){
            leaderState.stopRunning();
        }
        leaderState=null;
    }

    void changeToCandidate(){
        Preconditions.assertTrue(isFollower(),"serverState is "+role.toString()+"  not follower,can't changeToCandidate");
        shutDonwHeartBeatMonitor();
        setRole(RaftRole.Candidate,"changeToCandidate");
        startLeaderElection();
    }

    void changeToLeader(){
        LOG.info("leader election success");
        Preconditions.assertTrue(isCandidate(),"not candidate");
        shutdownElectiondaemon();
        setRole(RaftRole.Leader,"changeToLeader");
        serverState.becomLeader();
        // start sending AppendEntries RPC to followers
        leaderState = new LeaderState(this);
        leaderState.start();
    }

    void changeToFollower(int newTerm){
        final RaftRole old = role;
        serverState.setCurrentTerm(newTerm);
        if(old!=RaftRole.Follower){
            setRole(RaftRole.Follower,"changeToFollower");
            if(old==RaftRole.Leader){
                shutDownLeaderState();
            }else if(old == RaftRole.Candidate){
                shutdownElectiondaemon();
            }
            startHeartBeatMonitor();
        }
    }

    public void resetElectionTimeOut(){
        LOG.info("reset electionTime out");
        heartBeatMonitor.updateLastHeartBeatRpcTime();
    }

    int getRandomTimeOutMs(){
        return MinTimeOutMs+ThreadLocalRandom.current().nextInt(MaxTimeOutMs-MinTimeOutMs+1);
    }


    public RequestVoteArgs createRequestVoteRequest(int Term,String candidateId){
        return RequestVoteArgs.newBuilder().setTerm(Term).setCandidateId(candidateId).build();
    }

    public AppendEntriesArgs createHeartBeatAppendEntryArgs(){
        AppendEntriesArgs args =  AppendEntriesArgs.newBuilder()
                .setLeaderId(serverState.getLeaderId().getString())
                .setTerm(serverState.getCurrentTerm())
                .setEntries(null).build();
        return args;
    }


    /* public  <T> ExecutorCompletionService<T> sendHeartbeat(){
        RpcClient client = new NettyRpcClient("localhost",8080);
        raft.RaftService raftService = new ClientProxy(client).getProxy(raft.RaftService.class);
        raftService.RequestVote();
    }*/

    public void start(){
        DefaultRegistry registry = new DefaultRegistry();
        registry.register(new RaftServiceImpl(this));
        startAsFollower();
    }



    public void shutdown(){
        //follower 和 leader心跳，超时则转为candidate
        heartBeatMonitor.stopRunning();
        //candidate 发起选举
        electionDaemon.stopRunning();
        //leader 停止发送心跳
        leaderState.stopRunning();
    }


    public  boolean isFollower(){
        return role==RaftRole.Follower;
    }

    public  boolean isLeader(){
        return role==RaftRole.Leader;
    }

    public boolean isCandidate(){
        return role==RaftRole.Candidate;
    }
}
/**
 * 流程梳理
 * 1.start as follower
 * 2.canvas votes,
 * turn to candidate,
 * send request votes,
 * turn to leader
 * 3.leader send heart beat append entries to follower
 * 4.follower reset their election timer.
 */
