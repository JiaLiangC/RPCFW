package raft.requestBean;

public class RequestVoteReply {
    private int Term;
    private boolean VoteGranted;
    private String replyId;

    public int getTerm() {
        return Term;
    }

    public void setTerm(int term) {
        Term = term;
    }

    public boolean isVoteGranted() {
        return VoteGranted;
    }

    public void setVoteGranted(boolean voteGranted) {
        VoteGranted = voteGranted;
    }

    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
    }
}
