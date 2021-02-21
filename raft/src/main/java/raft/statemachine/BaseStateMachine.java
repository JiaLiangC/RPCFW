package raft.statemachine;

import raft.common.RaftClientRequest;
import raft.common.TransactionContext;
import raft.requestBean.SMLogEntry;
import raft.storage.RaftStorage;
import raft.common.RaftProperties;
import raft.common.id.RaftPeerId;

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
    public TransactionContext startTransaction(RaftClientRequest request) {
        return new TransactionContextImpl(this,request, SMLogEntry.newBuilder().setData(request.getMsg().getContent()).build());
    }

    @Override
    public void close() {

    }
}
