package raft;

import raft.requestBean.AppendEntriesArgs;
import raft.requestBean.AppendEntriesReply;
import raft.requestBean.RequestVoteArgs;
import raft.requestBean.RequestVoteReply;


/**
 * Raft 对外提供RPC 服务的接口
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public interface RaftService {
     AppendEntriesReply AppendEntries(AppendEntriesArgs args);
     RequestVoteReply RequestVote(RequestVoteArgs args);
}
