package raft.common.id;

import java.util.UUID;
import java.util.function.Supplier;

public abstract class RaftId {
    private final UUID uuid;
    private final String uuidString;

    RaftId(UUID uuid){
        this.uuid=uuid;
        this.uuidString =  createUuidString(uuid);
    }

    /** @return the last 12 hex digits. */
    String createUuidString(UUID uuid) {
        final String s = uuid.toString().toUpperCase();
        final int i = s.lastIndexOf('-');
        return s.substring(i + 1);
    }

}
