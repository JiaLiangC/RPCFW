package raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public static final Logger LOG = LoggerFactory.getLogger(RaftServerImpl.class);

    private static final long NANOSECONDS_PER_MILLISECOND = 1000000;
    private RaftServerImpl raftServerImpl;
    private Long lastHeartBeatRpcTime = System.nanoTime();
    private Long elapsedTime;
    private volatile boolean running;


    FollowerState(RaftServerImpl raftServerImpl){
        this.raftServerImpl = raftServerImpl;
        this.elapsedTime=0L;
        this.running=true;
    }

    public void stopRunning(){
        this.running=false;
    }

    public  void updateLastHeartBeatRpcTime(){
        lastHeartBeatRpcTime = System.nanoTime();
    }

    @Override
    public void run() {
        LOG.info("startHeartBeatMonitor");
        while (this.running && raftServerImpl.isFollower()){
            try {
                int electionTimeoutDur =  raftServerImpl.getRandomTimeOutMs();
                LOG.info("electionTimeoutDur is {}",electionTimeoutDur);
                Thread.sleep(electionTimeoutDur);
                if(!raftServerImpl.isFollower()){
                    break;
                }
                if(getElapsedTime()>electionTimeoutDur){
                    raftServerImpl.changeToCandidate();
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    long getElapsedTime(){
        elapsedTime =  (System.nanoTime()-lastHeartBeatRpcTime)/NANOSECONDS_PER_MILLISECOND;
        return elapsedTime;
    }
}
