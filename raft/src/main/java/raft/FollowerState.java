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

    private RaftServerImpl raftServerImpl;
    private volatile RaftTimer lastHeartBeatRpcTime = new RaftTimer();
    private volatile boolean running;


    FollowerState(RaftServerImpl raftServerImpl){
        this.raftServerImpl = raftServerImpl;
        this.running=true;
    }

    public void stopRunning(){
        this.running=false;
    }

    public  void updateLastHeartBeatRpcTime(){
        lastHeartBeatRpcTime =new RaftTimer();
    }

    @Override
    public void run() {
        updateLastHeartBeatRpcTime();
        LOG.info("startHeartBeatMonitor");
        while (this.running && raftServerImpl.isFollower()){
            try {
                int electionTimeoutDur =  raftServerImpl.getRandomTimeOutMs();
                LOG.info("electionTimeoutDur is {}",electionTimeoutDur);
                Thread.sleep(electionTimeoutDur);
                if(!raftServerImpl.isFollower()){
                    break;
                }
                if(lastHeartBeatRpcTime.getElapsedTime()>electionTimeoutDur){
                    raftServerImpl.changeToCandidate();
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
