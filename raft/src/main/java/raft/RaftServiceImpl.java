package raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.common.id.RaftPeerId;
import raft.requestBean.AppendEntriesArgs;
import raft.requestBean.AppendEntriesReply;
import raft.requestBean.RequestVoteArgs;
import raft.requestBean.RequestVoteReply;


//TODO  注册中心改本地或者ZK
/**
 * Raft 对外提供RPC 服务的接口实现类
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public class RaftServiceImpl implements RaftService {
    public static final Logger LOG = LoggerFactory.getLogger(RaftServiceImpl.class);

    private RaftServerImpl server;
    private ServerState serverState;

    public RaftServiceImpl() {

    }

    RaftServiceImpl(RaftServerImpl server) {
        this.server = server;
        this.serverState = server.getServerState();
    }


    /**
     * AppendEntries 可以用来做心跳服务，表示心跳时entries为空
     * 附加日志项RPC(最重要的一个RPC:心跳,日志同步,快照同步)
     * 更新自己日志和commitIndex
     * 通知applyDaemon 更新 lastApplied, 并且发送applyCh 到上层KV server
     **/
    @Override
    public AppendEntriesReply AppendEntries(AppendEntriesArgs args) {
        LOG.info("server:{}--------------------- got AppendEntries request at term:{}",serverState.getSelfId(),serverState.getCurrentTerm());
        AppendEntriesReply reply = new AppendEntriesReply();
        //如果对方任期比自己小,过期心跳，就拒绝(可能是网络隔离后恢复的一台leader发来的心跳)
        synchronized (server) {
            if (args.getTerm() < serverState.getCurrentTerm()) {
                reply.setSuccess(false);
                reply.setTerm(serverState.getCurrentTerm());
                return reply;
            }

            //收到的任期大于自己就更新自己任期
            if (args.getTerm() > serverState.getCurrentTerm()) {
                serverState.setCurrentTerm(args.getTerm());
            }

            //收到的任期大于自己，且自己是 leader ，可能是网络隔离后的老的leader
            if (server.isLeader() || server.isCandidate()) {
                server.changeToFollower(args.getTerm());
            }


            //重置自己的选举超时
            if (server.isFollower()) {
                server.resetElectionTimeOut();
            }

            //TODO 日志处理
            reply.setSuccess(true);
            reply.setTerm(serverState.getCurrentTerm());
        }
        return reply;
    }


    /**
     * 请求投票RPC的实现函数
     * 1.用VotedFor作为投票标志位，实现一个任期只投票一次
     * 2.如果候选人的 term < currentTerm 就返回false 拒绝投票
     * 3.如果votedFor为空或者与candidateId相同，并且候选人的日志和自己的日志一样新，则给该候选人投票
     * 在机器的交互之中都会更新自己的任期为较大者(收到的更大的任期)
     * 返回:投票者自己的当前任期号，是否投票
     **/
    @Override
    public RequestVoteReply RequestVote(RequestVoteArgs args) {
        LOG.info("server:{} at term:{} get RequestVote req, cid:{},replyId:{}, cterm:{}", serverState.getSelfId(), serverState.getCurrentTerm(),
                args.getCandidateId(),args.getReplyId(), args.getTerm());
        RequestVoteReply reply = new RequestVoteReply();
        reply.setReplyId(serverState.getSelfId().toString());

        if (!args.getReplyId().equals(serverState.getSelfId().toString()) ){
            LOG.error("RequestVote error--------------------------");
        }

        synchronized (server) {
            //请求者任期小于自己的任期，拒绝投票
            if (args.getTerm() < serverState.getCurrentTerm()) {
                reply.setVoteGranted(false);
                reply.setTerm(serverState.getCurrentTerm());
                return reply;
            }

            //如果任期比自己大就给投票，并且身份转变为follower,然后重置投票信息，voteFor(turnToFollower 中会重置voteFor).
            //如果任期相等就不管，可能是其他候选者发来的投票请求
            if (args.getTerm() > serverState.getCurrentTerm()) {
                reply.setTerm(serverState.getCurrentTerm());
                server.changeToFollower(args.getTerm());
            }

            //s1 在term 1获得s3投票成为，leader。 s2从term 0 超时成为candidate term1, 满足投票条件，但是VotedFor已经有数据了，一个任期只能投票一次，所以没问题
            if (serverState.getVotedFor() == null) {
                serverState.setVotedFor(RaftPeerId.valueOf(args.getCandidateId()));
                //重置自己的选举超时
                server.resetElectionTimeOut();
                LOG.info("RaftRPC impl RequestVote resetElectionTimeOut server:{} at term:{} get RequestVote req,cid:{}, cterm:{}", serverState.getSelfId(), serverState.getCurrentTerm(),
                        args.getCandidateId(), args.getTerm());
                reply.setVoteGranted(true);
            } else {
                reply.setVoteGranted(false);
            }
        }


        //TODO 增加日志索引判断
        //这里通过前面更新任期，以及设置votedFor 避免重复投票， 但是考虑到有两个时间段，A1,A2 A1>A2 ,
        //造成网络隔离导致两台机器成为candidate并且任期递增，此时两个candidate都大于本机器，一旦恢复网络
        //那么还是会出现重复投票的现象，但是因为任期都比自己大，每次投票时更新了任期，还是做到了一个任期只投票一次的原则。

        //1.其他相同任期的候选者的请求在这里会被过滤,因为候选者都会第一个给自己投票，此时votedFor是有值的
        //2.只给最后最后一条日志任期比我大的 或者 任期至少和我相同，并且日志索引大于等于我的 candidate 投票
        /*if(serverState.getVotedFor()==invalidNUm){

        }*/
        return reply;
    }

    @Override
    public String rpcTest() {
        return "hi client, server has got it";
    }


}

