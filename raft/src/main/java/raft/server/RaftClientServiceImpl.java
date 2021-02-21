package raft.server;

import raft.common.RaftClientReply;
import raft.common.RaftClientRequest;

public class RaftClientServiceImpl implements RaftClientService {


    private final RaftServer server;
    RaftClientServiceImpl(RaftServer server){
        this.server=server;
    }
    @Override
    public RaftClientReply message(RaftClientRequest request) {
        RaftClientReply reply = server.submitClientRequest(request);
        return null;
    }
}
