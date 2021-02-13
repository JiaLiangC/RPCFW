package raft;


import RPCFW.ServiceManager.registry.DefaultRegistry;
import RPCFW.Transport.ClientProxy;
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
import raft.handler.AppendEntries;
import raft.requestBean.AppendEntriesArgs;
import raft.requestBean.AppendEntriesReply;
import raft.requestBean.RequestVoteArgs;
import raft.requestBean.RequestVoteReply;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * raft的具体实现，心跳，选举等
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public class RaftServerImpl {

    public static final Logger LOG =LoggerFactory.getLogger(RaftServerImpl.class);

    public int MaxTimeOutMs = 900;
    public int MinTimeOutMs = 400;

    RaftConfiguration configuration;

    ServerState serverState;
    ExecutorService executorService ;
    private final RaftGroupId groupId;
    private  final RaftServerProxy proxy;
    private RaftRole role;

    private volatile LeaderElection electionDaemon;


    public ServerState getServerState() {
        return serverState;
    }

    RaftServerImpl(RaftPeerId peerId, RaftGroup group, RaftServerProxy proxy, RaftProperties properties){
        this.groupId=group.getRaftGroupId();
        this.proxy=proxy;
        this.serverState=new ServerState(peerId,group,properties,this,proxy.getStateMachine());
    }

    private NettyRpcServer nettyRpcServer;
    private int port;


    private void startAsFollower(){
        role=RaftRole.Follower;
        setRole(RaftRole.Follower,"startAsFollower");

    }

    public void setRole(RaftRole newRole,String op) {
        if(newRole!=role){
            LOG.info("change role from{} to {} at Term {} for {}",role,newRole,serverState.getCurrentTerm());
            this.role = newRole;
        }
    }

//    public void turnToCandidate(){
//        lock.lock();
//        currentTerm+=1;
//        role= RaftRole.Candidate;
//        votedFor = selfId;
//        leaderId=null;
//        lock.unlock();
//    }

    void changeToCandidate(){
        Preconditions.assertTrue(isFollower(),"serverState is not follower,can't changeToCandidate");
        setRole(RaftRole.Candidate,"changeToCandidate");
        startLeaderElection();
    }

    public void startLeaderElection(){
        electionDaemon = new LeaderElection(this);
        electionDaemon.start();
    }

    public void  shutdownElectiondaemon(){
        LeaderElection leaderElection = electionDaemon;
        if(leaderElection!=null){
            leaderElection.stopRunning();
        }
    }

    void changeToLeader(){
        Preconditions.assertTrue(isCandidate(),"not candidate");
        //TODO shutdownElectiondaemon
        setRole(RaftRole.Leader,"changeToLeader");
        serverState.becomLeader();
        //TODO start sending AppendEntries RPC to followers
        LeaderState leaderState = new LeaderState(this);
        leaderState.start();
    }

    void changeToFollower(){
        final RaftRole old = role;
        if(old!=RaftRole.Follower){
            setRole(RaftRole.Follower,"changeToFollower");
            if(old==RaftRole.Leader){
                //TODO shutdownLeaderState();
            }else if(old == RaftRole.Candidate){
                //TODO shutdownElectiondaemon
            }
            startHeartBeatMonitor();
        }
    }

    public void startHeartBeatMonitor(){
        FollowerState heartBeatMonitor = new FollowerState(this);
        heartBeatMonitor.start();
    }


    int getRandomTimeOutMs(){
        return MinTimeOutMs+ThreadLocalRandom.current().nextInt(MaxTimeOutMs-MinTimeOutMs+1);
    }


    public RequestVoteReply sendRequestVote(RaftPeer peer,RequestVoteArgs args){
        RpcClient client = new NettyRpcClient();
        client.connect(NetUtils.createSocketAddr(peer.getAddress(),-1));
        RaftService raftService = new ClientProxy(client).getProxy(RaftService.class);
        RequestVoteReply res = raftService.RequestVote(args);
        return res;
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

    public AppendEntriesReply sendAppendEntries(RaftPeer peer, AppendEntriesArgs args){
        RpcClient client = new NettyRpcClient();
        client.connect(NetUtils.createSocketAddr(peer.getAddress(),-1));
        RaftService raftService = new ClientProxy(client).getProxy(RaftService.class);
        AppendEntriesReply res = raftService.AppendEntries(args);
        return res;
    }

    /* public  <T> ExecutorCompletionService<T> sendHeartbeat(){
        RpcClient client = new NettyRpcClient("localhost",8080);
        raft.RaftService raftService = new ClientProxy(client).getProxy(raft.RaftService.class);
        raftService.RequestVote();
    }*/

    public void start(){
        nettyRpcServer = new NettyRpcServer(configuration.getPort());
        DefaultRegistry registry = new DefaultRegistry();
        registry.register(new RaftServiceImpl());
        nettyRpcServer.start();
    }



    public void shutdown(){
        nettyRpcServer.shutDown();
    }


    public  boolean isFollower(){
        return role==RaftRole.Follower;
    }

    public boolean isCandidate(){
        return role==RaftRole.Candidate;
    }
}
/**
 * 流程梳理
 * 1.start as follower
 * 2.canvas votes
 */
