package raft.common.utils;

public class RaftTimer {
    private static final long NANOSECONDS_PER_MILLISECOND = 1000000;
    private static final Long STARTED_AT = System.nanoTime();
    private long startTime;
    public RaftTimer(){
        startTime = System.nanoTime();
    }

    public RaftTimer(long nanos){
        startTime = nanos;
    }

    public long getElapsedTime(){
        long elapsedTime =  (System.nanoTime()-startTime)/NANOSECONDS_PER_MILLISECOND;
        return elapsedTime;
    }

     public static RaftTimer latest(RaftTimer a, RaftTimer b){
        if(a.startTime<b.startTime){return a;}
        return b;
    }

    public RaftTimer addTimeMs(long millSeconds){
        return new RaftTimer(startTime+millSeconds*NANOSECONDS_PER_MILLISECOND);
    }

}
