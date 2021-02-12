package raft;

import raft.common.RaftProperties;
import raft.common.id.RaftPeerId;

import java.io.IOException;

/**
 * 基础状态机实现，测试用
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public class BaseStateMachine implements StateMachine {
    @Override
    public void initialize(RaftPeerId peerId, RaftProperties properties, RaftStorage storage) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void reInitialize(RaftPeerId peerId, RaftProperties properties, RaftStorage storage) {

    }

    @Override
    public long takeSnapshot() {
        return 0;
    }

    @Override
    public void close() throws IOException {

    }
}
