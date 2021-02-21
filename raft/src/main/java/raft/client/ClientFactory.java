package raft.client;

import raft.common.RaftPeer;

public interface ClientFactory {

    RaftClientRpc newRaftClientRpc();
}
