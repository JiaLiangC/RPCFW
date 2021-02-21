package raft.client;

import RPCFW.RPCDemo.Nio.client.Client;
import raft.common.*;
import raft.common.id.ClientId;
import raft.common.id.RaftGroupId;
import raft.common.id.RaftPeerId;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class RaftClientImpl implements RaftClient {

    private static final AtomicLong callIdCounter = new AtomicLong();

    private static long nextCallId(){return callIdCounter.getAndDecrement()&Long.MAX_VALUE;}


    private final ClientId clientId; //客户端ID
    private final RaftClientRpc raftClientRpc; //rpc client 接口:{netty client, grpc client}
    private final Collection<RaftPeer>peers; //group peers
    private final RaftGroupId groupId; //raft group id

    private volatile RaftPeerId leaderId;

    RaftClientImpl(ClientId cid, RaftGroup group, RaftPeerId leaderId, RaftClientRpc clientRpc, RaftProperties properties){
        this.clientId=cid;
        this.raftClientRpc=clientRpc;
        this.groupId =group.getRaftGroupId();
        this.peers = new ConcurrentLinkedQueue<>(group.getRaftPeers());
        this.leaderId = leaderId != null? leaderId :  !peers.isEmpty()? peers.iterator().next().getId():null;
        //TODO get retryInterval from  properties
    }


    @Override
    public ClientId getId() {
        return clientId;
    }

    @Override
    public RaftClientRpc getClientRpc() {
        return null;
    }

    //TODO
    @Override
    public CompletableFuture<RaftClientReply> sendAsync(Message m) {
        return null;
    }


    //todo add replication Level
    @Override
    public RaftClientReply send(Message message) {
       return send(message,null);
    }

    @Override
    public void close() throws IOException {

    }

    public RaftClientReply send(Message message,RaftPeerId server){
        final long callId = nextCallId();
        return sendWithRetry(()-> createRaftClientRequest(server,callId,0L, message));
    }

    private RaftClientRequest createRaftClientRequest( RaftPeerId server,long callid,long seq,Message m){
        return new RaftClientRequest(clientId,server!=null? server: leaderId,groupId,callid,seq,m);
    }


    public RaftClientReply sendWithRetry(Supplier<RaftClientRequest> requestSupplier){
        int retryTimes=3;
        for(;;){
            retryTimes--;
            if (retryTimes<=0){
                //TODO
                return null;
            }
            RaftClientRequest request = requestSupplier.get();
            RaftClientReply reply = sendRequest(request);
            if(reply!=null){
                return reply;
            }
            try{
                //Retry interval 100ms
                Thread.sleep(100);
            }catch (InterruptedException e){
                //TODO why? Thread.currentThread().interrupt();
            }
        }
    }


    public RaftClientReply sendRequest(RaftClientRequest request){
        RaftClientReply reply=null;

        try {
            reply = raftClientRpc.sendRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return reply;
    }
}
