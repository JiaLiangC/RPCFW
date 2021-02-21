package raft.statemachine;

import raft.client.RaftClient;
import raft.common.RaftClientRequest;
import raft.common.TransactionContext;
import raft.requestBean.Entry;
import raft.requestBean.SMLogEntry;

public class TransactionContextImpl  implements TransactionContext {


    private RaftClientRequest request;
    private SMLogEntry smLogEntry;
    private Entry logEntry;

    private final StateMachine stateMachine;


    TransactionContextImpl(StateMachine s,RaftClientRequest request, SMLogEntry smLogEntry){
        this.stateMachine=s;
        this.request=request;
        this.smLogEntry=smLogEntry;
    }



    @Override
    public SMLogEntry getSMLogEntry() {
        return null;
    }

    @Override
    public TransactionContext setLogEntry(Entry e) {
        this.logEntry = e;
        return this;
    }

    RaftClientRequest getClientRequest(){
    return request;
    }

    Entry getLogEntry(){
        return logEntry;
    }
}
