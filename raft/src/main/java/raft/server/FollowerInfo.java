package raft.server;

import raft.common.RaftPeer;
import raft.common.utils.RaftTimer;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class FollowerInfo {

    private final RaftPeer peer;
    private  long nextIndex;
    private final AtomicLong matchedIndex;
    private final AtomicLong commitedIndex=new AtomicLong(-1) ;
    private final AtomicReference<RaftTimer> lastRpcSendTime;
    private final AtomicReference<RaftTimer> lastRpcResponseTime;


    public FollowerInfo(RaftPeer peer, RaftTimer lastRpcTime, long nextIndex) {
        this.peer = peer;
        this.nextIndex = nextIndex;
        this.lastRpcSendTime = new AtomicReference<>(lastRpcTime);
        this.lastRpcResponseTime = new AtomicReference<>(lastRpcTime);
        this.matchedIndex = new AtomicLong(0);
    }

    public boolean updateCommitedIndex(long newIndex){
        long old = commitedIndex.getAndUpdate(oldIndex->newIndex);
        return old!=newIndex;
    }


    public void updateMatchedIndex(long matchedIndex){
        this.matchedIndex.set(matchedIndex);
    }

   synchronized public void updateNextIndex( long nextIndex){
        this.nextIndex=nextIndex;
    }
    synchronized public long getNextIndex() {
        return nextIndex;
    }

    public void updatelastRpcSendTime(){
        lastRpcSendTime.set(new RaftTimer());
    }

    public void updatelastRpcResponseTime(){
        lastRpcResponseTime.set(new RaftTimer());
    }



    public long getMatchedIndex() {
        return matchedIndex.get();
    }

    public long getCommitedIndex() {
        return commitedIndex.get();
    }

    public RaftTimer getLastRpcSendTime() {
        return lastRpcSendTime.get();
    }

    public RaftTimer getLastRpcTime() {
        return RaftTimer.latest(lastRpcSendTime.get(),lastRpcResponseTime.get());
    }

    public RaftPeer getPeer() {
        return peer;
    }





}
