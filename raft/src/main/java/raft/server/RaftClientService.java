package raft.server;


import raft.client.RaftClient;
import raft.common.RaftClientReply;
import raft.common.RaftClientRequest;

/**
 * raft 对client 提供的服务
 *
 * @author xiaoxiao
 * @date 2021/02/21
 */
public interface RaftClientService {

    RaftClientReply message(RaftClientRequest request);
}
