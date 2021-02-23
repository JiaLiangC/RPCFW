package raft.server;

import com.sun.source.doctree.EndElementTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.common.*;
import raft.common.id.RaftPeerId;
import raft.common.utils.RaftTimer;
import raft.requestBean.AppendEntriesArgs;
import raft.requestBean.AppendEntriesReply;
import raft.requestBean.Entry;
import raft.storage.RaftLog;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class LeaderState extends Daemon {
    public static final Logger LOG = LoggerFactory.getLogger(LeaderState.class);

    //TODO 线程池吞没异常

    private RaftServerImpl server;
    private ServerState serverState;
    private ExecutorService executorService;
    private volatile boolean running;

    private final Queue commitProcessor;
    private final PendingRequestHandler pendingRequestHandler;


    private final RaftLog raftLog;

    private Collection<RaftPeer> others;

    private List<LogAppender> logAppenders;
    //投票的，未投票但是通过了心跳变为follower的，必须都在groups中，
    private List<FollowerInfo> FollowerInfos;


    public LeaderState(RaftServerImpl server) {
        FollowerInfos = new ArrayList<>();
        commitProcessor = new LinkedBlockingDeque();
        this.server = server;
        this.serverState = server.getServerState();
        this.raftLog = serverState.getRaftLog();
        this.executorService = Executors.newFixedThreadPool(100);
        this.running = true;
        this.others = serverState.getOtherPeers();
        this.pendingRequestHandler = new PendingRequestHandler(server);
        final RaftTimer r = new RaftTimer().addTimeMs(-RaftServerImpl.getRandomTimeOutMs());
        others.stream().forEach(peer->{
            FollowerInfo followerInfo= createFollowerInfo(this,peer,r,raftLog.getNextIndex());
            FollowerInfos.add(followerInfo);
            logAppenders.add(new LogAppender(server,this,followerInfo));
        });
    }



    FollowerInfo createFollowerInfo(LeaderState state, RaftPeer p, RaftTimer lastRpc,long nextIndex){
        FollowerInfo f = new FollowerInfo(p,lastRpc,nextIndex);
        return f;
    }


    public void startLogAppenders(){
        logAppenders.forEach(Thread::start);
    }

    public void notifyAppenders(){
        logAppenders.forEach(LogAppender::notifyAppender);
    }

    @Override
    public void run() {
        heartbeatDaemon();
    }


    public void heartbeatDaemon() {
        LOG.info("LeaderState heartbeatDaemon start");

        while (server.isLeader() && running) {
                others.forEach((RaftPeer peer) -> {
                            executorService.submit(() -> {
                                AppendEntriesArgs args = server.createHeartBeatAppendEntryArgs(peer.getId());
                                try {
                                    LOG.info("server:{} leader sendAppendEntries to {} at term:{}",serverState.getSelfId(),peer.getId(),serverState.getCurrentTerm());
                                    AppendEntriesReply reply = server.getServrRpc().sendAppendEntries(args);
                                    if (reply== null ){
                                        return;
                                    }
                                    if (!reply.isSuccess()) {
                                        LOG.info("leader send heart beat failed");
                                        if (server.isLeader() && serverState.getCurrentTerm() < reply.getTerm()) {
                                            server.changeToFollower(reply.getTerm());
                                        }
                                    }else {
                                        //LOG.info("server:{} leader sendAppendEntries get reply from {} at term:{}",serverState.getSelfId(),peer.getId(),serverState.getCurrentTerm());
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                            });
                        }
                );
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /*
    * 如果存在一个满⾜N > commitIndex 的 N，并且⼤多数的 matchIndex[i] ≥ N 成⽴，
    * 并且 log[N].term == currentTerm 成立，那么令 commitIndex 等于这 个 N (5.3 和 5.4 节)
    * matchIndex集合中的多数值都大于N —— 意味着，多数follower都已经收到了logs[0,N]条目并应用到了本地状态机中；
    * 并且，logs[N].term等于当前节点（leader）的currentTerm —— 意味着，这条日志是当前任期产生而不是其他leader的任期同步过来的；
    */
    //TODO 这里根据请求的 replication level 进入两个队列，PendingRequest 或者 delayedRequest ,后者是应用到绝大多数状态机
    public void updateLastCommitIndex(){
        Map<RaftPeerId,Long> followerMacthIndex = new HashMap<>();
        FollowerInfos.forEach(followerInfo ->{
            followerMacthIndex.putIfAbsent(followerInfo.getPeer().getId(),followerInfo.getMatchedIndex());
        } );


        //macthinde 从小到大排序
        List<Long> sortedMacthIndex = followerMacthIndex.values().stream().sorted((a,b)->(int) (a-b)).collect(Collectors.toList());

         long majorityMatchIndex = sortedMacthIndex.get(sortedMacthIndex.size()/2);

         if(raftLog.getLastCommitedIndex()< majorityMatchIndex){
             if(raftLog.getTermIndex(majorityMatchIndex).getTerm()==serverState.getCurrentTerm()){
                 raftLog.updateLastCommitedIndex(majorityMatchIndex,serverState.getCurrentTerm());

                 //TODO apply to state machine and reply to client
             }
             Entry e =  raftLog.get(majorityMatchIndex);
             //TODO add commitinfo to reply
              RaftClientReply reply = new RaftClientReply(e.getClientId(),server.getId(),server.getGroupId(),e.getCallId(),true,null);
             pendingRequestHandler.replyPendingrequest(majorityMatchIndex,reply);
         }

    }

    //TODO LinkedTransferQueue
    public PendingRequest addPendingQequest(Long index, RaftClientRequest c, TransactionContext context){
       return pendingRequestHandler.addPendingQequest(index,c,context);
    }

    public void replyPendingrequest(long index, RaftClientReply reply){
        pendingRequestHandler.replyPendingrequest(index,reply);
    }




    public void stopRunning() {
        this.running = false;
    }


}
