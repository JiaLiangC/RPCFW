package raft.requestBean;

import java.util.List;

public class AppendEntriesArgs {
    private int Term;
    private int LeaderId;
    private List<Entry> Entries;
}
