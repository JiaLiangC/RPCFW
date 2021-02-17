package raft;

import raft.common.RaftGroup;
import raft.common.RaftProperties;
import raft.common.id.RaftPeerId;

import java.io.Closeable;


/**
 * 状态机接口，和业务实现有关
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public interface StateMachine extends Closeable {
    void initialize(RaftPeerId peerId, RaftProperties properties,RaftStorage storage);

    void pause();

    void reInitialize(RaftPeerId peerId, RaftProperties properties,RaftStorage storage);

    long takeSnapshot();


}
