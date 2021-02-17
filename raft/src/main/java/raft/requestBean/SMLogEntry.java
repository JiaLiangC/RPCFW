package raft.requestBean;

public class SMLogEntry {
    byte[] data;
    SMLogEntry(){}

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


}
