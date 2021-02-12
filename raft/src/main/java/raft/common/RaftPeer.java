package raft.common;

import raft.common.id.RaftPeerId;
import raft.common.utils.NetUtils;

import java.net.InetSocketAddress;
import java.util.Objects;

public class RaftPeer {
    //RaftServerId
    private final RaftPeerId id;
    private final String address;

    public RaftPeerId getId() {
        return id;
    }


    RaftPeer(RaftPeerId raftPeerId,String addr){
        this.id = Objects.requireNonNull(raftPeerId,"id == null");
        this.address = addr;
    }

    public RaftPeer(RaftPeerId raftPeerId, InetSocketAddress address){
        this(raftPeerId,address==null ? null: NetUtils.address2String(address));
    }

}
