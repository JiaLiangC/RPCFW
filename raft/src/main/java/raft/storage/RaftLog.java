package raft.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.common.TermIndex;
import raft.common.TransactionContext;
import raft.common.id.ClientId;
import raft.common.id.RaftPeerId;
import raft.requestBean.Entry;
import raft.requestBean.SMLogEntry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public abstract class RaftLog {
    private static final Logger LOG= LoggerFactory.getLogger(RaftLog.class);
    private final AtomicLong lastcommitIndex = new AtomicLong(-1);
    private final RaftPeerId peerId;


    RaftLog(RaftPeerId peerId){
        this.peerId=peerId;
    }


    public long append(int Term, TransactionContext context, ClientId clientId, long callId){

        long nextIndex = getNextIndex();
        final SMLogEntry smLogEntry=  context.getSMLogEntry();

        Entry e=  Entry.newBuilder().setSmLogEntry(smLogEntry).setTerm(Term).build();
        appendEntry(e);
        context.setLogEntry(e);
        return nextIndex;
    }

    abstract CompletableFuture<Long> appendEntry(Entry entry);

    long getNextIndex(){
        TermIndex last = getLastLogEntryTermIndex();
        return last.getIndex()+1;
    }

    abstract TermIndex getLastLogEntryTermIndex();


}
