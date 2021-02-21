package raft.common;

import raft.common.RaftClientMessage;
import raft.common.RaftGroup;
import raft.common.RaftRpcMessage;
import raft.common.id.ClientId;
import raft.common.id.RaftGroupId;
import raft.common.id.RaftPeerId;

public class RaftClientRequest extends RaftClientMessage {

    private final long callId; //客户端总的 call 线性增长
    private final long seqNum; //客户端重试增长的num
    private  final Message msg; // 客户端消息


    public RaftClientRequest(ClientId clientId,RaftPeerId peerId, RaftGroupId groupId,long callId) {
        this(clientId,peerId,groupId,callId,0L,null);
    }

    public RaftClientRequest(ClientId clientId,RaftPeerId peerId, RaftGroupId groupId,long callId,long seqNum,Message msg) {
        super(clientId,peerId,groupId);
        this.callId=callId;
        this.seqNum=seqNum;
        this.msg = msg;
    }

    public long getCallId() {
        return callId;
    }

    public long getSeqNum() {
        return seqNum;
    }


    @Override
    public boolean isRequest() {
        return true;
    }

    public Message getMsg() {
        return msg;
    }
}
