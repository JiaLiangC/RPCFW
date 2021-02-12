package raft;


import RPCFW.ServiceManager.registry.DefaultRegistry;
import RPCFW.Transport.ClientProxy;
import RPCFW.Transport.client.NettyRpcClient;
import RPCFW.Transport.client.RpcClient;
import RPCFW.Transport.server.NettyRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.common.RaftGroup;
import raft.common.RaftPeer;
import raft.common.RaftProperties;
import raft.common.id.RaftGroupId;
import raft.common.id.RaftPeerId;
import raft.requestBean.AppendEntriesReply;
import raft.requestBean.RequestVoteReply;

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

    RaftConfiguration configuration;

    ServerState serverState;
    ExecutorService executorService ;
    private final RaftGroupId groupId;
    private  final RaftServerProxy proxy;


    RaftServerImpl(RaftPeerId peerId, RaftGroup group, RaftServerProxy proxy, RaftProperties properties){
        this.groupId=group.getRaftGroupId();
        this.proxy=proxy;
        this.serverState=new ServerState(peerId,group,properties,this,proxy.getStateMachine());
    }

    private NettyRpcServer nettyRpcServer;
    private int port;



    public RequestVoteReply sendRequestVote(){
        RpcClient client = new NettyRpcClient("localhost",8080);
        RaftService raftService = new ClientProxy(client).getProxy(RaftService.class);
        RequestVoteReply res = raftService.RequestVote();
        return res;
    }

    public AppendEntriesReply sendAppendEntries(){
        RpcClient client = new NettyRpcClient("localhost",8080);
        RaftService raftService = new ClientProxy(client).getProxy(RaftService.class);
        AppendEntriesReply res = raftService.AppendEntries();
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

    public void canvassVotes(){
        serverState.turnToCandidate();
        AtomicInteger receivedVotesCnt = new AtomicInteger(1);
        int peersLen = serverState.getPeersCount();

        for(RaftPeer p : serverState.getPeers()){
            if( p.getId()!=serverState.getSelfId()){
                CompletableFuture.supplyAsync(()->sendRequestVote(),executorService)
                        .thenAccept(reply->{
                            if (serverState.getRole()== RaftRole.Candidate){
                                if(reply.getTerm() > serverState.getCurrentTerm()){
                                    serverState.turnToFollower();
                                    //TODO reset election timer
                                    return;
                                }
                                if (reply.isVoteGranted()){
                                    if(receivedVotesCnt.get()>peersLen/2){
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
                             AppendEntriesReply reply = sendAppendEntries();
                             if(!reply.isSuccess()){
                                 serverState.turnToFollower();
                                 //TODO reset election
                             }
                         });
                     }
                    }
                 );
            }, 0, configuration.getHeartbeatTimerInterval(), TimeUnit.MILLISECONDS);
        }
    }

    public void electionDaemon(){
        Future f =  executorService.submit(()->{
            for(;;){
            }
        });

    }

    public void shutdown(){
        nettyRpcServer.shutDown();
    }
}
