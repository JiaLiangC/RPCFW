import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.client.RaftClient;
import raft.common.*;
import raft.server.RaftServerImpl;
import raft.server.RaftServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RaftLogTest {
    public static final Logger LOG = LoggerFactory.getLogger(RaftLogTest.class);

    @Test
    public void testBasicAgree(){

        MiniRaftCluster cluster = new MiniRaftCluster().newCluster(3,new RaftProperties());
        cluster.start();
        LOG.info("testBasicAgree");
        RaftGroup group = cluster.getGroup();
        List<RaftPeer> peers =cluster.getPeers();

        peers.forEach(peer -> {
            cluster.createRaftClient(peer.getId(),group);
        });

         RaftServerImpl leader = RaftTestUtils.checkOneLeader(cluster);
        RaftClient client = cluster.createRaftClient(leader.getServerState().getSelfId(), group);
        RaftClientReply reply=null;
        for(int i=0;i<10;i++){
             reply = client.send(Message.valueOf("message"+i));

        }












    }
}
