package raft.common;

public class RaftClientMessage implements RaftRpcMessage {


    @Override
    public boolean isRequest() {
        return false;
    }

    @Override
    public String getRequestorId() {
        return null;
    }

    @Override
    public String getReplierId() {
        return null;
    }

    @Override
    public String getRaftGroupId() {
        return null;
    }
}
