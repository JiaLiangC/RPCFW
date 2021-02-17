package RPCTest;

import RPCFW.ServiceManager.registry.DefaultRegistry;
import RPCFW.Transport.client.ClientProxy;
import RPCFW.Transport.client.NettyClientProxy;
import RPCFW.Transport.server.NettyRpcServer;
import raft.RaftService;
import raft.common.Preconditions;
import raft.requestBean.AppendEntriesArgs;
import raft.requestBean.AppendEntriesReply;
import raft.requestBean.RequestVoteArgs;
import raft.requestBean.RequestVoteReply;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TestRaftServiceImpl implements RaftService  {



    private String id;
    private int port;
    private NettyRpcServer rpcServer;
    private Collection<Peer> group;
    private Map<String,Peer> peerMap = new ConcurrentHashMap<>();
    private final Map<String, ClientProxy> proxyPeerMap=new ConcurrentHashMap<>();


    TestRaftServiceImpl(Peer peer, Collection<Peer> g){
        this.id=peer.getId();
        port= peer.getAddress().getPort();
        rpcServer = new NettyRpcServer(port);
        DefaultRegistry  registry = new DefaultRegistry();
        registry.newRegister(peer.getAddress(),this);
        group=g;
        g.stream().forEach(p->peerMap.put(p.getId(),p));
    }

    @Override
    public AppendEntriesReply AppendEntries(AppendEntriesArgs args) {
        return null;
    }

    @Override
     public RequestVoteReply RequestVote(RequestVoteArgs args) {
        Preconditions.assertTrue(args.getReplyId().equals(this.id),"RequestVote error");
        RequestVoteReply reply = new RequestVoteReply();
        reply.setReplyId(this.id);
        reply.setVoteGranted(true);
        reply.setTerm(1);
        System.out.println("RequestVote finish ");
        return reply;
    }

    @Override
    public String rpcTest() {
        return null;
    }

    public void start(){
        rpcServer.start();
    }

    synchronized public void sendRequestVoteToOthers(){
        getOtherPeers().forEach(p->{
            String pid = p.getId();
            ClientProxy proxy =  getProxy(p.getId());
            RaftService service = proxy.getProxy(RaftService.class);

            RequestVoteArgs req = RequestVoteArgs.newBuilder().setReplyId(pid).setCandidateId(this.id).setTerm(1).build();
            service.RequestVote(req);
        });
    }

    synchronized Collection<Peer> getOtherPeers(){
        return group.stream().filter((p)->!(p.getId().equals(this.id))).collect(Collectors.toList());
    }

    synchronized public ClientProxy getProxy(String targetid){
        Preconditions.assertTrue(!this.id.equals(targetid),"error in getProxy");
        return  proxyPeerMap.computeIfAbsent(targetid,
                pt-> new NettyClientProxy(getInetAddr(targetid)));
    }

    public int getPort(String id){
        return peerMap.get(id).getAddress().getPort();
    }

    synchronized public InetSocketAddress getInetAddr(String id){
        return peerMap.get(id).getAddress();
    }

}
