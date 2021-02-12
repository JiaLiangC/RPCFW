package raft;

import java.net.InetSocketAddress;
import java.util.Objects;


/**
 * raft 组件交互的RPC 层，方便扩展多种RPC实现
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public interface RaftServerRpc {

    abstract class Builder<B extends Builder,RPC extends RaftServerRpc>{
        private RaftServer raftServer;
        public RaftServer getServer(){
            return Objects.requireNonNull(raftServer,"server filed is not initialized");
        }

        public B setServer(RaftServer raftServer){
            this.raftServer=raftServer;
            return getThis();
        }

        protected abstract B getThis();
        abstract RPC build();



    }

    void start();
    InetSocketAddress getInetSocketAddress();

    public String getRpcType();

}
