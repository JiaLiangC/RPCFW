package raft.common;

import raft.requestBean.Entry;
import raft.requestBean.SMLogEntry;

public interface TransactionContext {


    SMLogEntry  getSMLogEntry();

    TransactionContext setLogEntry(Entry e);
}
