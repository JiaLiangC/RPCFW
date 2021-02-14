package raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.common.Daemon;
import raft.common.RaftPeer;
import raft.requestBean.RequestVoteArgs;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LeaderElection extends Daemon {
    public static final Logger LOG = LoggerFactory.getLogger(LeaderElection.class);

    private ServerState serverState;
    private RaftServerImpl server;
    private ExecutorService executorService;
    private volatile boolean running;

    LeaderElection(RaftServerImpl server){
        this.server = server;
        this.serverState =server.getServerState();
        this.running=true;
        this.executorService= Executors.newFixedThreadPool(100);
        LOG.info("LeaderElection peerCount is {}",serverState.getPeersCount());
    }

    @Override
    public void run() {
        canvassVotes();
    }

    public  void stopRunning(){
        this.running=false;
    }


    public void canvassVotes(){
        serverState.initEleciton();
        LOG.info("server:[{}] term:[{}] candidate start canvassVotes",serverState.getSelfId().getString(),serverState.getCurrentTerm());
        AtomicInteger receivedVotesCnt = new AtomicInteger(1);
        int peersLen = serverState.getPeersCount();

        for(RaftPeer p : serverState.getPeers()){
            if (!running){
                return;
            }
            if( p.getId().getString()!=serverState.getSelfId().getString()){
                RequestVoteArgs args = server.createRequestVoteRequest(serverState.getCurrentTerm(),serverState.getSelfId().getString());
                LOG.info("server:[{}] canvassVotes RequestVoteArgs term:{} peer_id:{}",serverState.getSelfId().getString(), args.getTerm(), p.getId().getString());
                CompletableFuture.supplyAsync(()->server.sendRequestVote(p,args), executorService)
                    .thenAccept(reply->{
                        LOG.info("canvassVotes get requestVote reply");
                        if (serverState.getRole() == RaftRole.Candidate){
                            if(reply.getTerm() > serverState.getCurrentTerm()){
                                server.changeToFollower();
                                return;
                            }
                            if (reply.isVoteGranted()){
                                if(receivedVotesCnt.get()>=peersLen/2){
                                    server.changeToLeader();
                                    return ;
                                }
                                receivedVotesCnt.addAndGet(1);
                            }
                        }
                    });
            }
        }
    }



}
