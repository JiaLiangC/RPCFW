package raft;

import raft.common.Daemon;
import raft.common.RaftPeer;
import raft.requestBean.AppendEntriesArgs;
import raft.requestBean.AppendEntriesReply;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LeaderState extends Daemon {


    private RaftServerImpl server;
    private ServerState serverState;
    private ExecutorService executorService;
    private volatile boolean running;

    public LeaderState(RaftServerImpl server) {
        this.server = server;
        this.serverState = server.getServerState();
        this.executorService = Executors.newFixedThreadPool(serverState.getPeersCount());
        this.running = true;
    }

    @Override
    public void run() {
        heartbeatDaemon();
    }


    public void heartbeatDaemon() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
        while (server.isLeader() && running) {
            service.scheduleAtFixedRate(() -> {
                serverState.getPeers().forEach((RaftPeer peer) -> {
                            if (peer.getId() != serverState.getSelfId()) {
                                executorService.submit(() -> {
                                    //TODO construct heartbeat args
                                    AppendEntriesArgs args = server.createHeartBeatAppendEntryArgs();
                                    AppendEntriesReply reply = server.getServrRpc().sendAppendEntries(peer.getId(), args);
                                    if (!reply.isSuccess()) {
                                        if (server.isLeader() && serverState.getCurrentTerm() < reply.getTerm()) {
                                            server.changeToFollower(reply.getTerm());
                                        }
                                    }
                                });
                            }
                        }
                );
            }, 0, 100, TimeUnit.MILLISECONDS);
        }
    }

    public void stopRunning() {
        this.running = false;
    }


}
