import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.server.RaftServerImpl;
import raft.server.RaftServerProxy;
import raft.common.id.RaftPeerId;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RaftTestUtils {
    public static final Logger LOG = LoggerFactory.getLogger(RaftTestUtils.class);


    public static void  disconnect(MiniRaftCluster cluster, RaftPeerId peerId){
        cluster.addDisconnectedServer(peerId);
        disConnectNetwork( cluster,  peerId,true);
    }

    public static void  reconnect(MiniRaftCluster cluster, RaftPeerId peerId){
        cluster.removeDisconnectedServer(peerId);
        disConnectNetwork( cluster,  peerId,false);
    }

    public static void disConnectNetwork(MiniRaftCluster cluster, RaftPeerId peerId,boolean shouldDisconnect){
        cluster.getServers().values().forEach(server->{
            RaftServerImpl impl = server.getServerImpl();

            //停止server的proxyMap中其他Peer的 rpc 客户端，这样server 不能向其他Peer 发送请求，模拟网络中断.
            if (server.getId().toString().equals(peerId.toString())){
                impl.getServerState().getOtherPeers().forEach(peer -> impl.getServrRpc().disconnectProxy(peer.getId(),shouldDisconnect));
            }else {
                //然后停止其他Peer的这个server 的client，这样其他Peer 不能向这个server 发送数据
                impl.getServrRpc().disconnectProxy(peerId,shouldDisconnect);
            }
        });

    }


    //判断网络正常的节点 no leader
    public static void checkNoLeader(MiniRaftCluster cluster){
        cluster.getServers().values().forEach(server->{
            RaftServerImpl impl = server.getServerImpl();
            if(cluster.isConnected(server.getId())){
                if (impl.isLeader()){
                    Assert.fail("expetced no leader but there is a leader, leaderId:"+impl.getServerState().getSelfId()+" term:"+impl.getServerState().getCurrentTerm());
                }
            }
        });
    }

    public static RaftServerImpl checkOneLeader(MiniRaftCluster cluster){

        for(int num =0;num<10;num++){
            try {
                Thread.sleep(RaftServerImpl.getRandomTimeOutMs());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Map<String, ArrayList<RaftServerImpl>> termLeaders = new ConcurrentHashMap<>();
            cluster.getServers().values().forEach(server->{
                RaftServerImpl impl = server.getServerImpl();
                if (impl.isLeader()){
                    String term = String.valueOf(impl.getServerState().getCurrentTerm());
                    termLeaders.putIfAbsent(term, new ArrayList<>());
                    termLeaders.get(term).add(impl);
                }
            });

            int lastTermWithLeader = -1;
            for (Map.Entry<String, ArrayList<RaftServerImpl>> entry : termLeaders.entrySet()) {
                String term = entry.getKey();
                ArrayList<RaftServerImpl> leadersList = entry.getValue();
                Assert.assertEquals(leadersList.size(), 1);
                if (Integer.valueOf(term) > lastTermWithLeader) {
                    lastTermWithLeader = Integer.valueOf(term);
                }

            }

            if(termLeaders.size()!=0){
                RaftServerImpl server = termLeaders.get(String.valueOf(lastTermWithLeader)).get(0);
                return server;
            }
        };

        Assert.fail();
        return null;
    }


    public static int checkTerms(MiniRaftCluster cluster){
        int term=-1;
        for (RaftServerProxy server : cluster.getServers().values()) {
            int intTerm = server.getCurrentTerm();
            //term 初始化，后续和最初的term，对比
            if (term == -1) { term = intTerm; }

            if(intTerm!=term){
                Assert.fail("servers disagree one tem");
            }
        }
        return term;
    }



}
