package raft.storage;

import raft.common.TermIndex;
import raft.common.id.RaftPeerId;
import raft.common.utils.AutoCloseLock;
import raft.requestBean.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MemLog extends  RaftLog{



    private final List<Entry> logEntries = new ArrayList<>();


    public MemLog(RaftPeerId peerId){
        super(peerId);
    }

    @Override
    public Entry get(long index){
        try(AutoCloseLock lock = acquireLock()) {
            final int i = (int) index;
            return i> 0 && i< logEntries.size() ? logEntries.get(i) : null;
        }
    }


    @Override
    public TermIndex getTermIndex(long index){
        try(AutoCloseLock lock = acquireLock()) {
            final int i = (int) index;
            if (i> 0 && i< logEntries.size() ){return null;}
            return new TermIndex(logEntries.get(i).getTerm(),logEntries.get(i).getIndex());
        }
    }

    @Override
    CompletableFuture<Long> appendEntry(Entry entry) {
        try(AutoCloseLock lock = acquireLock()) {
        logEntries.add(entry);
        return CompletableFuture.completedFuture(entry.getIndex());
        }
    }

    @Override
    TermIndex getLastLogEntryTermIndex() {
        try(AutoCloseLock lock = acquireLock()) {
            final int size=logEntries.size();
            if (size==0){return  null;}
            Entry e = logEntries.get(size-1);
            return new TermIndex(e.getTerm(),e.getIndex());
        }
    }



}
