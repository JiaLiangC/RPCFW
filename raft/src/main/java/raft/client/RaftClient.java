package raft.client;

import RPCFW.RPCDemo.Nio.client.Client;
import raft.common.*;
import raft.common.id.ClientId;
import raft.common.id.RaftPeerId;

import java.io.Closeable;
import java.text.DateFormat;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public interface RaftClient extends Closeable {

    ClientId getId();


    RaftClientRpc getClientRpc();


    CompletableFuture<RaftClientReply> sendAsync(Message message);

    RaftClientReply send(Message message);

    static Builder newBuilder() {
        return new Builder();
    }

    //DateFormat
    class Builder {
        private ClientId clientId;
        private RaftClientRpc clientRpc;
        private RaftGroup group;
        private RaftPeerId leaderId;
        private RaftProperties properties;

        public RaftClient build() {
            if(clientId ==null){
                clientId = ClientId.randomId();
            }
            if(clientRpc==null){
                //TODOD ependON Rpc type in config
                clientRpc = new NettyClientRpc();
            }

            Objects.requireNonNull(clientId);
            Objects.requireNonNull(group);
            Objects.requireNonNull(clientRpc);
            return new RaftClientImpl(clientId, group, leaderId, clientRpc, properties);
        }

        public Builder setClientId(ClientId clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder setClientRpc(RaftClientRpc clientRpc) {
            this.clientRpc = clientRpc;
            return this;
        }

        public Builder setGroup(RaftGroup group) {
            this.group = group;
            return this;
        }

        public Builder setLeaderId(RaftPeerId leaderId) {
            this.leaderId = leaderId;
            return this;
        }

        public Builder setProperties(RaftProperties properties) {
            this.properties = properties;
            return this;
        }
    }

}
