package raft.server;

import raft.common.Daemon;
import raft.common.Preconditions;
import raft.common.TermIndex;
import raft.handler.AppendEntries;
import raft.requestBean.AppendEntriesArgs;
import raft.requestBean.AppendEntriesReply;
import raft.requestBean.Entry;
import raft.storage.RaftLog;

import java.util.ArrayList;
import java.util.List;

public class LogAppender extends Daemon {

    private final RaftLog log;
    private final FollowerInfo follower;
    private final RaftServerImpl server;
    private final LeaderState leaderState;
    private final LogEntryBuffer buffer;
    private volatile boolean runnning=true;

    LogAppender(RaftServerImpl server,LeaderState state,FollowerInfo follower){
        this.server=server;
        this.leaderState=state;
        this.follower=follower;
        this.log = server.getServerState().getRaftLog();
        this.buffer = new LogEntryBuffer();
    }


    public AppendEntriesReply appendEntriesMonitor() throws InterruptedException {

        while (isRunnning()&&server.isLeader()){
            if(shouldSendRequest()){
                final AppendEntriesReply reply =  sendAppendEntriesWithRetry();
                if(reply!=null){
                    replyHanddler(reply);
                }
            }

            if(isRunnning()){
                final  long remainTime = getHeartBeatRemainTime();
                if(remainTime>0){
                    synchronized (this){
                        wait(remainTime);
                    }
                }
            }
        }

        return null;
    }


    public AppendEntriesReply sendAppendEntriesWithRetry(){
        AppendEntriesArgs args=null;
        while(isRunnning()){
            try {
                args =  createAppendEntriesRequest(0);
                if(args==null){
                    //not enough entry wait
                    return null;
                }

                follower.updatelastRpcSendTime();
                //follower.
                AppendEntriesReply reply= server.getServrRpc().sendAppendEntries(args);

                follower.updatelastRpcResponseTime();

                updateFollowerCommitIndex(args.getLeaderCommit());
                return reply;
            }catch (Exception e){

            }
        }
        return null;
    }

    public synchronized void notifyAppender(){
        this.notify();
    }

    private long getHeartBeatRemainTime(){
        return server.getMinTimeOutMs()-follower.getLastRpcTime().getElapsedTime();
    }


    void updateFollowerCommitIndex(long i){
        follower.updateCommitedIndex(i);
    }

    /**
     *
     *
     * rf.nextIndex[id] <len(rf.logs)+rf.lastIncludedIndex
     * @return
     */
    public boolean shouldSendAppendEntries() {
        return follower.getNextIndex()< log.getNextIndex() ;

    }


    public boolean shouldHeartBeat(){
        long t = server.getMinTimeOutMs();
        return  (t-follower.getLastRpcTime().getElapsedTime())<=0;
    }

    boolean shouldSendRequest(){
        return shouldHeartBeat() || shouldSendAppendEntries();
    }



    /*
    *  //构造RPC请求参数
    var args = AppendEntriesArgs{
        Term:         rf.currentTerm,
        LeaderId:     rf.me,
        PrevLogIndex: pre,
        PrevLogTerm:  rf.logs[rf.subIdx(pre)].Term,
        Entries:      nil,
        LeaderCommit: rf.commitIndex,
    }
    * */
    public AppendEntriesArgs createAppendEntriesRequest(long callId){
        TermIndex leaderPre = getPreTermIndex();
        long followerNextIndex = follower.getNextIndex();
        long next =followerNextIndex;
        long leaderNext = log.getNextIndex();
        final boolean toSend;
        if(leaderNext==followerNextIndex && !buffer.isEmpty()){
            toSend=true;
        } else if (followerNextIndex< leaderNext){
            boolean hasSpace = true;
            for(;hasSpace && leaderNext>next;){
                hasSpace = buffer.addEntry(log.get(next++));
            }
            toSend = !hasSpace;
        }else {
            toSend = false;
        }

        if(toSend || shouldHeartBeat()){
            return buffer.getAppendRequest(leaderPre);
        }
        return null;
    }


    private class LogEntryBuffer{
        private final List<Entry> buff = new ArrayList<>();
        private int totalSize=0;
        //TODO estimate message size  after serialization
        boolean addEntry(Entry e){
            //if(totalSize+entrySize<maxBufferSize){
            if(buff.size()<10){
                buff.add(e);
                //totalSize+=entrySize
                return true;
            }
            return false;
        }

        boolean isEmpty(){
            return buff.isEmpty();
        }

        int getPendingEntryNum(){
            return buff.size();
        }


        AppendEntriesArgs getAppendRequest(TermIndex leaderPre){
            AppendEntriesArgs args = AppendEntriesArgs.newBuilder()
                    .setLeaderId(server.serverState.getSelfId().toString())
                    .setReplyId(follower.getPeer().getId().toString())
                    .setTerm(server.serverState.getCurrentTerm())
                    .setPrevLogIndex(leaderPre.getIndex())
                    .setPrevLogTerm(leaderPre.getTerm())
                    .setEntries(buff)
                    .setLeaderCommit(log.getLastCommitedIndex()).build();
            buff.clear();
            totalSize=0;
            return args;
        }

    }

    TermIndex getPreTermIndex(){
        return log.getTermIndex(follower.getNextIndex()-1);
    }


    public void replyHanddler(AppendEntriesReply reply){
        //update folower info
        //notify server to update leader info (last commit) and then reply client

        long oldNextIndex = follower.getNextIndex();
        long nextIndex= reply.getNextIndex();
        Preconditions.assertTrue(nextIndex>oldNextIndex,"reply error");
        if(reply!=null){
            if(reply.isSuccess()){
            follower.updateMatchedIndex(nextIndex-1);
            follower.updateNextIndex(nextIndex);
            //notify leaderState to update commit
                leaderState.updateLastCommitIndex();
            }else {
                //TODO 日志冲突
            }
        }
    }


    public LogAppender shutDown(){
        this.runnning=false;
        return this;
    }

    boolean isRunnning(){
        return this.runnning;
    }



    @Override
    public void run() {
        try {
            appendEntriesMonitor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //TODO fix kryo序列化溢出

    /*ByteArrayOutputStream baos=new ByteArrayOutputStream();
    Output output = new Output(baos);
    这种情况下，缓存字节数大于4096的话，就会发生问题。必须在建立Output对象的时候，指定更大的bufferSize，例如：
    ByteArrayOutputStream baos=new ByteArrayOutputStream();
    Output output = new Output(baos,100000);
    * */
}
