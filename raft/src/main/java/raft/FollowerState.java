package raft;

import raft.common.Daemon;
import raft.common.RaftPeer;
import raft.requestBean.AppendEntriesReply;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * 负责 follower 状态时的心跳检测管理
 *
 * @author xiaoxiao
 * @date 2021/02/13
 */
public class FollowerState extends Daemon {

    private static final long NANOSECONDS_PER_MILLISECOND = 1000000;
    private RaftServerImpl raftServerImpl;
    private Long lastHeartBeatRpcTime = System.nanoTime();
    private Long elapsedTime;


    FollowerState(RaftServerImpl raftServerImpl){
        this.raftServerImpl = raftServerImpl;
        elapsedTime=0L;
    }

    public  void updateLastHeartBeatRpcTime(){
        lastHeartBeatRpcTime = System.nanoTime();
    }

    @Override
    public void run() {
        while (raftServerImpl.isFollower()){
            try {
                int electionTimeoutDur =  raftServerImpl.getRandomTimeOutMs();
                Thread.sleep(electionTimeoutDur);
                if(!raftServerImpl.isFollower()){
                    break;
                }
                if(getElapsedTime()>electionTimeoutDur){
                    raftServerImpl.changeToCandidate();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    long getElapsedTime(){
        long ms =  (System.nanoTime()-lastHeartBeatRpcTime)/NANOSECONDS_PER_MILLISECOND;
        return ms;
    }
}
