package raft.server;

import raft.common.RaftClientReply;
import raft.common.RaftClientRequest;
import raft.common.TransactionContext;

import java.util.concurrent.CompletableFuture;

public class PendingRequest {


    private long entryIndex;
    private  RaftClientRequest request;
    private TransactionContext context;
    private CompletableFuture<RaftClientReply> future;

    public PendingRequest(long entryIndex, RaftClientRequest request, TransactionContext context){
        this.entryIndex=entryIndex;
        this.request=request;
        this.context=context;
        this.future = new CompletableFuture<>();
    }

   CompletableFuture<RaftClientReply> getFuture(){
        return future;
   }

   void setReply(RaftClientReply r){
        future.complete(r);
   }


}
