package raft;

import raft.requestBean.AppendEntriesReply;
import raft.requestBean.RequestVoteReply;


/**
 * Raft 对外提供RPC 服务的接口实现类
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public class RaftServiceImpl implements RaftService {


    @Override
    public AppendEntriesReply AppendEntries() {
        return null;
    }

    @Override
    public RequestVoteReply RequestVote() {
        return null;
    }
}
