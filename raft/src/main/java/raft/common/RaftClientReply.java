package raft.common;

import raft.common.RaftClientMessage;
import raft.common.id.ClientId;
import raft.common.id.RaftGroupId;
import raft.common.id.RaftPeerId;

public class RaftClientReply extends RaftClientMessage {

    private final boolean success;
    private final long callId;
    private final Message message;


    RaftClientReply(ClientId cid, RaftPeerId pid, RaftGroupId groupId,long callId, boolean success,Message message){
        super(cid, pid, groupId);
        this.success=success;
        this.callId=callId;
        this.message=message;
    }

    @Override
    public boolean isRequest() {
        return false;
    }

    public boolean isSuccess(){
        return success;
    }

    public Message getMessage(){
        return  message;
    }
}
