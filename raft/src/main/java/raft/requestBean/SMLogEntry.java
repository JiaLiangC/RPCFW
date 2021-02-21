package raft.requestBean;

import com.google.protobuf.ByteString;

public class SMLogEntry {
    ByteString data;

    SMLogEntry(ByteString b) {
        this.data=b;
    }

    public ByteString getData() {
        return data;
    }

    public void setData(ByteString data) {
        this.data = data;
    }


    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        ByteString bytes;

        public  Builder() {
        }

        public Builder setData(ByteString d) {
            this.bytes = d;
            return this;
        }

        public SMLogEntry build(){
            return new SMLogEntry(this.bytes);
        }


    }

}
