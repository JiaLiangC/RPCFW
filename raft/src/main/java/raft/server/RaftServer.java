package raft.server;

import raft.common.RaftPeer;
import raft.common.id.RaftPeerId;

/**
 * raft 服务器
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public interface RaftServer {

    /**
     *启动服务器
     */
    void start();

    RaftPeer getPeer(RaftPeerId id);

    RaftPeerId getId();

    int getCurrentTerm();


}
