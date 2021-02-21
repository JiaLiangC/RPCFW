package raft.requestBean;

import com.google.protobuf.ByteString;

public class Entry {
    public Entry(){}
    SMLogEntry smLogEntry;
    int term;


    Entry(SMLogEntry smLogEntry,int term){
        this.smLogEntry=smLogEntry;
        this.term=term;
    }

    public SMLogEntry getSmLogEntry() {
        return smLogEntry;
    }


    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        SMLogEntry smLogEntry;
        int term;
        public  Builder() {
        }

        public Builder setSmLogEntry(SMLogEntry d) {
            this.smLogEntry = d;
            return this;
        }

        public Builder setTerm(int t) {
            this.term = t;
            return this;
        }

        public Entry build(){
            return new Entry(this.smLogEntry,this.term);
        }


    }




}
