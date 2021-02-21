package raft.common;

import RPCFW.RPCDemo.Nio.client.Client;
import raft.client.RaftClient;
import raft.common.id.ClientId;
import raft.common.id.RaftGroupId;
import raft.common.id.RaftPeerId;

public abstract class RaftClientMessage implements RaftRpcMessage {

    private ClientId clientId;
    private RaftPeerId serverId;
    private RaftGroupId groupId;


    RaftClientMessage(ClientId cid,RaftPeerId pid,RaftGroupId groupId){
        this.clientId=cid;
        this.serverId=pid;
        this.groupId=groupId;
    }


    public ClientId getClientId() {
        return clientId;
    }

    public RaftPeerId getServerId() {
        return serverId;
    }


    @Override
    public String getRequestorId() {
        return clientId.toString();
    }

    @Override
    public String getReplierId() {
        return serverId.toString();
    }

    @Override
    public String getRaftGroupId() {
        return groupId.toString();
    }
}
