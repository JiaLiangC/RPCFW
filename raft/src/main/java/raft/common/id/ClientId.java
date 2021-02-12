package raft.common.id;

import RPCFW.RPCDemo.Nio.client.Client;

import java.util.UUID;

public class ClientId extends  RaftId{

    public static ClientId randomId(){return new ClientId(UUID.randomUUID());}

    private ClientId(UUID uuid){super(uuid);}
}
