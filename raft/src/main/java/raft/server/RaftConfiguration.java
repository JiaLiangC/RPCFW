package raft.server;


/**
 *raft 配置
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public class RaftConfiguration {

    private int port = 8081;
    private int heartbeatTimerInterval;
    private  int electionTimeOutDuration;



    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getHeartbeatTimerInterval() {
        return heartbeatTimerInterval;
    }

    public void setHeartbeatTimerInterval(int heartbeatTimerInterval) {
        this.heartbeatTimerInterval = heartbeatTimerInterval;
    }

    public int getElectionTimeOutDuration() {
        return electionTimeOutDuration;
    }

    public void setElectionTimeOutDuration(int electionTimeOutDuration) {
        this.electionTimeOutDuration = electionTimeOutDuration;
    }
}
