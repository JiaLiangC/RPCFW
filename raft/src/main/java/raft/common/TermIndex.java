package raft.common;

public class TermIndex {
    private final int Term;
    private final long Index;


    public TermIndex(int term, long index) {
        Term = term;
        Index = index;
    }

    public int getTerm() {
        return Term;
    }

    public long getIndex() {
        return Index;
    }
}
