package raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.common.Daemon;
import raft.common.RaftPeer;
import raft.requestBean.AppendEntriesReply;

import java.text.SimpleDateFormat;
import java.util.Date;
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
    SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss:SSS");//设置日期格式
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
        LOG.info("server:{} startHeartBeatMonitor at term:{} ",raftServerImpl.serverState.getSelfId(),raftServerImpl.serverState.getCurrentTerm());
        while (this.running && raftServerImpl.isFollower()){
            try {
                int electionTimeoutDur =  raftServerImpl.getRandomTimeOutMs();
                LOG.info("{} server:{} electionTimeoutDur is {}", df.format(new Date()),raftServerImpl.getServerState().getSelfId(),electionTimeoutDur);
                Thread.sleep(electionTimeoutDur);


                synchronized (raftServerImpl){
                    if(!raftServerImpl.isFollower()){
                        break;
                    }

                    if(lastHeartBeatRpcTime.getElapsedTime()>electionTimeoutDur){
                        LOG.info("{} server:{} electionTimeout, will change to candidate", df.format(new Date()),raftServerImpl.getServerState().getSelfId());
                        raftServerImpl.changeToCandidate();
                        return;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
