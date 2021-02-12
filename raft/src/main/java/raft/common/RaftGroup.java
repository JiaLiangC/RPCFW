package raft.common;

import raft.common.id.RaftGroupId;
import raft.common.id.RaftPeerId;

import java.lang.reflect.Array;
import java.util.*;

public class RaftGroup {

    private final RaftGroupId raftGroupId;
    private final Map<RaftPeerId,RaftPeer> peers;

    public RaftGroup(RaftGroupId id){
        this(id,Collections.emptyList());
    }

    public RaftGroup(RaftGroupId id, RaftPeer ...peers){
       this(id, Arrays.asList(peers));
    }

    RaftGroup(RaftGroupId id, Collection<RaftPeer> peers ){
        this.raftGroupId = Objects.requireNonNull(id,"groupid == null");
        if(peers==null || peers.isEmpty()){
            this.peers = Collections.emptyMap();
        }else {
            final Map<RaftPeerId,RaftPeer> map = new HashMap<>();
            peers.stream().forEach(peer->map.put(peer.getId(),peer));
            this.peers = Collections.unmodifiableMap(map);
        }

    }

    public RaftGroupId getRaftGroupId(){
        return this.raftGroupId;
    }

    public Collection<RaftPeer> getRaftPeers(){
        return peers.values();
    }
}
