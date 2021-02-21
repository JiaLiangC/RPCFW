package raft.client;

public class NettyClientFactory implements ClientFactory{


    @Override
    public RaftClientRpc newRaftClientRpc() {
        return new NettyClientRpc();
    }
}
