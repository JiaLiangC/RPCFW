import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.*;
import raft.common.RaftGroup;
import raft.common.RaftPeer;
import raft.common.RaftProperties;
import raft.common.id.RaftGroupId;
import raft.common.id.RaftId;
import raft.common.id.RaftPeerId;
import raft.common.utils.NetUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public  class MiniRaftCluster {
    public static final Logger LOG = LoggerFactory.getLogger(MiniRaftCluster.class);

    protected RaftGroup group;
    protected RaftProperties raftProperties;

    protected  final Map<RaftPeerId, RaftServerProxy> servers = new ConcurrentHashMap<>();

    MiniRaftCluster newCluster(int numPeers, RaftProperties prop){
        this.group = initRaftGroup(Arrays.asList(generateIds(numPeers,0)));
        this.raftProperties = new RaftProperties();
        return this;
    }

    public Map<RaftPeerId, RaftServerProxy> getServers(){
        return servers;

    }

    public static RaftGroup initRaftGroup(Collection<String> ids){
        final RaftPeer[] peers = ids.stream()
                .map(RaftPeerId::valueOf)
                .map(id-> new RaftPeer(id, NetUtils.createLocalServerAddress()))
                .toArray(RaftPeer[]::new);

        return new RaftGroup(RaftGroupId.randomId(),peers);

    }

    public static String[] generateIds(int numServers, int base){
        String[] ids = new String[numServers];
        for(int i=0;i<numServers;i++){
            ids[i]="s"+(i+base);
        }
        return ids;
    }

    public MiniRaftCluster initServers(){
        if(servers.isEmpty()){
            putNewServers(group.getRaftPeers());
        }
        return this;
    }

    /**
     *
     * @param peers
     * @return
     */
    Collection<RaftServerProxy> putNewServers(Collection<RaftPeer> peers){
       return peers.stream().map(peer->{
            StateMachine stateMachine = getStateMachineForTest();
            RaftServerProxy p = newRaftServer(peer.getId(),stateMachine,group,raftProperties);
            servers.put(peer.getId(),p);
            return p;
        }).collect(Collectors.toList());
    }

    static StateMachine getStateMachineForTest(){
        return new BaseStateMachine();
    }

    public RaftServerProxy newRaftServer(RaftPeerId id,StateMachine stateMachine,RaftGroup group,RaftProperties properties){
        RaftServerProxy proxy = new RaftServerProxy(id,stateMachine,group,properties);
        return proxy;
    }

    void start() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(20);
        initServers();
        servers.values().forEach(server->{
            service.submit(()->{
                server.start();
            });
        });
        service.awaitTermination(1, TimeUnit.HOURS);
    }


}
