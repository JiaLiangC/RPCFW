package raft.common;

public interface RaftRpcMessage {
    boolean isRequest();
    String getRequestorId();
    String getReplierId();
    String getRaftGroupId();
}
