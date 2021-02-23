package raft.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.common.TermIndex;
import raft.common.TransactionContext;
import raft.common.id.ClientId;
import raft.common.id.RaftPeerId;
import raft.common.utils.AutoCloseLock;
import raft.requestBean.Entry;
import raft.requestBean.SMLogEntry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class RaftLog {
    private static final Logger LOG= LoggerFactory.getLogger(RaftLog.class);
    private final AtomicLong lastcommitIndex = new AtomicLong(-1);
    private final RaftPeerId peerId;
    private final ReentrantLock lock = new ReentrantLock(false);

    RaftLog(RaftPeerId peerId){
        this.peerId=peerId;
    }


    public long append(int Term, TransactionContext context, ClientId clientId, long callId){

        long nextIndex = getNextIndex();
        final SMLogEntry smLogEntry=  context.getSMLogEntry();

        Entry e=  Entry.newBuilder().setSmLogEntry(smLogEntry).setTerm(Term).setIndex(nextIndex).setCallId(callId).setClientId(clientId).build();
        appendEntry(e);
        context.setLogEntry(e);
        return nextIndex;
    }

    public long getNextIndex(){
        TermIndex last = getLastLogEntryTermIndex();
        return last.getIndex()+1;
    }

    public long getLastCommitedIndex(){
        return lastcommitIndex.get();
    }

    public boolean updateLastCommitedIndex(long majorIndex ,int currentTerm){
        try(AutoCloseLock lock = acquireLock()) {
            if(lastcommitIndex.get()< majorIndex){
                final TermIndex e = getTermIndex(majorIndex);
                if(e!=null && e.getTerm()==currentTerm){
                    lastcommitIndex.set(majorIndex);
                    return true;
                }
            }
        }
        return false;
    }


    public AutoCloseLock acquireLock(){
        return new AutoCloseLock(lock);
    }

    public abstract CompletableFuture<Long> appendEntry(Entry entry);

    public abstract TermIndex getLastLogEntryTermIndex();

    public  abstract TermIndex getTermIndex(long index);

    public abstract  Entry get(long index);

}
