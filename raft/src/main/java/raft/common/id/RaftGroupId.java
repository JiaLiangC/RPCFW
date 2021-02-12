package raft.common.id;

import java.util.UUID;

public class RaftGroupId extends RaftId {

    private  RaftGroupId(UUID uuid){super(uuid);}
    public static RaftGroupId randomId(){return new RaftGroupId(UUID.randomUUID());}
}
