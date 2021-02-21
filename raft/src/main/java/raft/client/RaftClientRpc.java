package raft.client;

import raft.common.RaftClientReply;
import raft.common.RaftClientRequest;
import raft.common.RaftPeer;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface RaftClientRpc {
    RaftClientReply sendRequest(RaftClientRequest request) throws IOException;

   default CompletableFuture<RaftClientReply> sendRequestAsync(RaftClientRequest request) throws IOException{
       throw  new UnsupportedOperationException();
   };

    void addServers(Iterable<RaftPeer> servers);

}
