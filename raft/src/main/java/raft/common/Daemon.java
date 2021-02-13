package raft.common;

public class Daemon extends Thread {
     {
        setDaemon(true);
     }

     public Daemon(){
         super();
    }

    public Daemon(Runnable runnable){
         super(runnable);
         this.setName(runnable.toString());
    }
}
