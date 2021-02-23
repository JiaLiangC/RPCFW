package raft.requestBean;

import com.google.protobuf.ByteString;
import raft.common.id.ClientId;

public class Entry {
    public Entry() {
    }

    SMLogEntry smLogEntry;
    int term;
    long index;
    long callId;

    public long getCallId() {
        return callId;
    }

    public ClientId getClientId() {
        return clientId;
    }

    ClientId clientId;



    Entry(SMLogEntry smLogEntry, int term,long index,long callId,ClientId clientId) {
        this.smLogEntry = smLogEntry;
        this.term = term;
        this.index=index;
        this.callId = callId;
        this.clientId=clientId;
    }

    public SMLogEntry getSmLogEntry() {
        return smLogEntry;
    }
    public long getIndex() {
        return index;
    }

    public int getTerm() {
        return term;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        SMLogEntry smLogEntry;
        int term;
        long index;
        long callId;
        ClientId clientId;

        public Builder() {
        }

        public Builder setIndex(long index) {
            this.index = index;
            return this;
        }

        public Builder setCallId(long i){
            this.callId=i;
            return this;
        }

        public Builder setClientId(ClientId clientId){
            this.clientId=clientId;
            return this;
        }

        public Builder setSmLogEntry(SMLogEntry d) {
            this.smLogEntry = d;
            return this;
        }

        public Builder setTerm(int t) {
            this.term = t;
            return this;
        }

        public Entry build() {
            return new Entry(this.smLogEntry, this.term,this.index,callId,clientId);
        }

    }


}
