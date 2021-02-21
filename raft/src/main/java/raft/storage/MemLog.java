package raft.storage;

import raft.common.TermIndex;
import raft.common.id.RaftPeerId;
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
    CompletableFuture<Long> appendEntry(Entry entry) {
        return null;
    }

    @Override
    TermIndex getLastLogEntryTermIndex() {
        return null;
    }


}
