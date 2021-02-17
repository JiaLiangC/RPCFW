import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.RaftServerImpl;
import raft.common.RaftProperties;
import raft.common.id.RaftPeerId;
import java.util.concurrent.CompletableFuture;


public class LeaderElectionTest {

    public static final Logger LOG = LoggerFactory.getLogger(RaftTestUtils.class);

    @Test
    public void TestInitialElection() throws InterruptedException {
        MiniRaftCluster cluster = new MiniRaftCluster().newCluster(3,new RaftProperties());
        CompletableFuture.runAsync(()-> {
            try {
                cluster.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        LOG.info("cluster start election");
        RaftTestUtils.checkOneLeader(cluster);
        Thread.sleep(50);
        int term1 = RaftTestUtils.checkTerms(cluster);
        Thread.sleep(RaftServerImpl.getRandomTimeOutMs()*2);
        int term2 = RaftTestUtils.checkTerms(cluster);

        if(term1!=term2){
            Assert.fail("term changed even though there is no failures");
        }
        RaftTestUtils.checkOneLeader(cluster);
    }


    @Test
    public void TestReElection() throws InterruptedException {
        MiniRaftCluster cluster = new MiniRaftCluster().newCluster(3,new RaftProperties());
        cluster.start();
        LOG.info("election after netWork failure");

        RaftServerImpl leader1 = RaftTestUtils.checkOneLeader(cluster);
        RaftPeerId leader1Id =  leader1.getServerState().getSelfId();

        // if the leader disconnects, a new one should be elected.
        RaftTestUtils.disconnect(cluster,leader1Id);

        RaftTestUtils.checkOneLeader(cluster);

        // if the old leader rejoins, that shouldn't
        // disturb the new leader.
        RaftTestUtils.reconnect(cluster,leader1Id);
        RaftServerImpl leader2 = RaftTestUtils.checkOneLeader(cluster);
        RaftPeerId leader2Id =leader2.getServerState().getSelfId();

        // if there's no quorum, no leader should
        // be elected.
        RaftTestUtils.disconnect(cluster, leader2Id);
        RaftTestUtils.disconnect(cluster, leader1Id);
        RaftTestUtils.checkNoLeader(cluster);

        // if a quorum arises, it should elect a leader.
        RaftTestUtils.reconnect(cluster,leader1Id);
        RaftTestUtils.checkOneLeader(cluster);

        // re-join of last node shouldn't prevent leader from existing.
        RaftTestUtils.reconnect(cluster,leader2Id);
        RaftTestUtils.checkOneLeader(cluster);

    }
}
