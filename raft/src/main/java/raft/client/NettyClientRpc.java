package raft.client;

import RPCFW.Transport.client.ClientProxy;
import RPCFW.Transport.client.NettyClientProxy;
import raft.common.Preconditions;
import raft.common.RaftClientReply;
import raft.common.RaftClientRequest;
import raft.common.RaftPeer;
import raft.common.id.RaftPeerId;
import raft.common.utils.NetUtils;
import raft.server.RaftClientService;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class NettyClientRpc implements  RaftClientRpc {

    private final Map<RaftPeerId,  PeerProxy> peers = new ConcurrentHashMap<>();

    private class PeerProxy implements Closeable {
        private final RaftPeer peer;
        private final Function<RaftPeer,ClientProxy> createFunc;
        private volatile  ClientProxy proxy;
        PeerProxy(RaftPeer p,Function<RaftPeer,ClientProxy> createFunc){
            this.peer=p;
            this.createFunc = createFunc;
        }

        public RaftPeer getPeer() {
            return peer;
        }

        public ClientProxy getProxy(){
            if(proxy==null){
                synchronized (this){
                    if(proxy==null){
                        proxy=createFunc.apply(peer);
                    }
                }
            }
            return  proxy;
        }

        @Override
        public void close() throws IOException {
            proxy.close();
        }
    }



    @Override
    public RaftClientReply sendRequest(RaftClientRequest request) throws IOException {
        ClientProxy proxy = getProxy(request.getServerId());
        RaftClientService service = proxy.getProxy(RaftClientService.class);
        RaftClientReply reply =  service.message(request);
        return reply;
    }



    @Override
    public void addServers(Iterable<RaftPeer> servers) {
        //传入创建函数延迟加载，这样发送请求时，server已经启动，client能连接上
        for(RaftPeer p: servers){
            peers.computeIfAbsent(p.getId(), k -> new PeerProxy(p,peer->new NettyClientProxy(NetUtils.createSocketAddr(peer.getAddress(),-1))));
        }
    }

    public ClientProxy getProxy(RaftPeerId peerId) {
        PeerProxy peerProxy =  peers.get(peerId);
        return peerProxy.getProxy();
    }
}
