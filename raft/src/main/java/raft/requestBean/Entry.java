package raft.requestBean;

public class Entry {
    Entry(){}
    SMLogEntry smLogEntry;

    public SMLogEntry getSmLogEntry() {
        return smLogEntry;
    }

    public void setSmLogEntry(SMLogEntry smLogEntry) {
        this.smLogEntry = smLogEntry;
    }


}
