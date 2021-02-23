package raft.server;

import raft.common.Preconditions;
import raft.common.RaftClientReply;
import raft.common.RaftClientRequest;
import raft.common.TransactionContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PendingRequestHandler {

    private RaftServerImpl server;

    private final ConcurrentMap<Long,PendingRequest> requests = new ConcurrentHashMap();

    public PendingRequestHandler(RaftServerImpl server) {
        this.server = server;
    }


    public PendingRequest addPendingQequest(Long index, RaftClientRequest  request, TransactionContext context){
        PendingRequest p = new PendingRequest(index,request,context);
        addPendingQequest(index,p);
        return p;
    }

    public void addPendingQequest(Long index, PendingRequest p){
        final PendingRequest pre = requests.put(index,p);
        Preconditions.assertTrue(pre==null,"error in addPendingQequest");
    }

    public void replyPendingrequest(long index, RaftClientReply reply){
        final  PendingRequest request = requests.remove(index);
        if(request!=null){
            request.setReply(reply);
        }
    }
}
