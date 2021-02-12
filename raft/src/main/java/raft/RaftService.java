package raft;

import raft.requestBean.AppendEntriesReply;
import raft.requestBean.RequestVoteReply;


/**
 * Raft 对外提供RPC 服务的接口
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public interface RaftService {
     AppendEntriesReply AppendEntries();
     RequestVoteReply RequestVote();
}
