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


    private  RaftServerImpl server;
    private ServerState serverState;
    private ExecutorService executorService;

    public LeaderState(RaftServerImpl server){
        this.server=server;
        this.serverState = server.getServerState();
        this.executorService = Executors.newFixedThreadPool(serverState.getPeersCount());
    }

    @Override
    public void run() {
        heartbeatDaemon();
    }


    public void heartbeatDaemon(){
        ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
        for(;;){
            if (serverState.getRole()!=RaftRole.Leader){
                return;
            }
            service.scheduleAtFixedRate(()->{
                int peersLen = serverState.getPeersCount();
                serverState.getPeers().forEach((RaftPeer peer)->{
                            if(peer.getId()!=serverState.getSelfId()){
                                executorService.submit(()->{
                                    //TODO construct heartbeat args
                                    AppendEntriesArgs args = server.createHeartBeatAppendEntryArgs();
                                    AppendEntriesReply reply = server.sendAppendEntries(peer,args);
                                    if(!reply.isSuccess()){
                                        server.changeToFollower();
                                    }
                                });
                            }
                        }
                );
            }, 0,500, TimeUnit.MILLISECONDS);
        }
    }


}
