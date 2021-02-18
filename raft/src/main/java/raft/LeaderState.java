package raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.common.Daemon;
import raft.common.RaftPeer;
import raft.requestBean.AppendEntriesArgs;
import raft.requestBean.AppendEntriesReply;

import java.util.Collection;
import java.util.concurrent.*;

public class LeaderState extends Daemon {
    public static final Logger LOG = LoggerFactory.getLogger(LeaderState.class);

    //TODO 线程池吞没异常

    private RaftServerImpl server;
    private ServerState serverState;
    private ExecutorService executorService;
    private volatile boolean running;

    private Collection<RaftPeer> others;

    public LeaderState(RaftServerImpl server) {
        this.server = server;
        this.serverState = server.getServerState();
        this.executorService = Executors.newFixedThreadPool(100);
        this.running = true;
        this.others = serverState.getOtherPeers();
    }

    @Override
    public void run() {
        heartbeatDaemon();
    }


    public void heartbeatDaemon() {
        LOG.info("LeaderState heartbeatDaemon start");

        while (server.isLeader() && running) {
                others.forEach((RaftPeer peer) -> {
                /*    CompletableFuture.supplyAsync(()->{
                        LOG.info("xxxxxxxxx leader send heart  start");
                        //TODO construct heartbeat args
                        AppendEntriesArgs args = server.createHeartBeatAppendEntryArgs();
                        AppendEntriesReply reply = server.getServrRpc().sendAppendEntries(peer.getId(), args);
                        return reply;
                    }).thenAccept(reply->{
                        if (!reply.isSuccess()) {
                            LOG.info("xxxxxxxxx leader send heart beat failed");
                            if (server.isLeader() && serverState.getCurrentTerm() < reply.getTerm()) {
                                server.changeToFollower(reply.getTerm());
                            }
                        }
                    }).exceptionally(e->{
                        e.printStackTrace();
                        return null;
                    });*/
                            executorService.submit(() -> {
                                AppendEntriesArgs args = server.createHeartBeatAppendEntryArgs(peer.getId());
                                try {
                                    LOG.info("server:{} leader sendAppendEntries to {} at term:{}",serverState.getSelfId(),peer.getId(),serverState.getCurrentTerm());
                                    AppendEntriesReply reply = server.getServrRpc().sendAppendEntries(args);
                                    if (reply== null ){
                                        return;
                                    }
                                    if (!reply.isSuccess()) {
                                        LOG.info("leader send heart beat failed");
                                        if (server.isLeader() && serverState.getCurrentTerm() < reply.getTerm()) {
                                            server.changeToFollower(reply.getTerm());
                                        }
                                    }else {
                                        //LOG.info("server:{} leader sendAppendEntries get reply from {} at term:{}",serverState.getSelfId(),peer.getId(),serverState.getCurrentTerm());
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                            });
                        }
                );
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopRunning() {
        this.running = false;
    }


}
