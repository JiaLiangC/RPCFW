package raft.handler;

import raft.RaftRole;
import raft.ServerState;
import raft.requestBean.RequestVoteReply;

public class RequestVoteHandler implements  Runnable{
    private RequestVoteReply reply;
    private ServerState serverState;

   public RequestVoteHandler(RequestVoteReply reply){
        this.reply = reply;
    }

    @Override
    public void run() {
        if (serverState.getRole()== RaftRole.Candidate){
            if(reply.getTerm() > serverState.getCurrentTerm()){
                //serverState.turnToFollower();
                //TODO reset election timer
                return;
            }
            if (reply.isVoteGranted()){

            }
        }

    }
}
