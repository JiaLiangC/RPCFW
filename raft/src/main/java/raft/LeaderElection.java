package raft;

import raft.common.Daemon;
import raft.common.RaftPeer;
import raft.requestBean.RequestVoteArgs;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LeaderElection extends Daemon {

    private ServerState serverState;
    private RaftServerImpl server;
    private ExecutorService executorService = Executors.newFixedThreadPool(serverState.getPeersCount());
    private volatile boolean running;

    LeaderElection(RaftServerImpl server){
        this.server = server;
        this.serverState =server.getServerState();
        this.running=true;

    }

    @Override
    public void run() {
        canvassVotes();
    }

    public  void stopRunning(){
        this.running=false;
    }


    public void canvassVotes(){
        server.changeToCandidate();
        AtomicInteger receivedVotesCnt = new AtomicInteger(1);
        int peersLen = serverState.getPeersCount();

        for(RaftPeer p : serverState.getPeers()){
            if( p.getId()!=serverState.getSelfId()){
                RequestVoteArgs args = server.createRequestVoteRequest(serverState.getCurrentTerm(),serverState.getSelfId().toString());
                CompletableFuture.supplyAsync(()->server.sendRequestVote(p,args), executorService)
                    .thenAccept(reply->{
                        if (serverState.getRole() == RaftRole.Candidate){
                            if(reply.getTerm() > serverState.getCurrentTerm()){
                                server.changeToFollower();
                                return;
                            }
                            if (reply.isVoteGranted()){
                                if(receivedVotesCnt.get()>=peersLen/2){
                                    serverState.setRole(RaftRole.Leader);
                                    //TODO start heart beat daemon
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
