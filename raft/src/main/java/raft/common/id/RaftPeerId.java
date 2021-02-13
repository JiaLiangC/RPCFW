package raft.common.id;

import com.google.protobuf.ByteString;
import raft.common.Preconditions;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class RaftPeerId {
    private final String idString;
    private final ByteString id;

    private static final Map<String,RaftPeerId> stringMap = new ConcurrentHashMap<>();

    public static  RaftPeerId  valueOf(String id){
        return  stringMap.computeIfAbsent(id,RaftPeerId::new);
    }


    RaftPeerId(String id){
        this.idString = Objects.requireNonNull(id,"id == null");
        Preconditions.assertTrue(!id.isEmpty(),"id is an empty string");
        this.id = ByteString.copyFrom(idString, StandardCharsets.UTF_8);
    }

    public  String getString(){
        return this.idString;
    }

}
