package raft;

public class RaftTimer {
    private static final long NANOSECONDS_PER_MILLISECOND = 1000000;
    private long elapsedTime;
    private long startTime;
    public RaftTimer(){
        elapsedTime=0;
        startTime = System.nanoTime();
    }

    long getElapsedTime(){
        elapsedTime =  (System.nanoTime()-startTime)/NANOSECONDS_PER_MILLISECOND;
        return elapsedTime;
    }

}
