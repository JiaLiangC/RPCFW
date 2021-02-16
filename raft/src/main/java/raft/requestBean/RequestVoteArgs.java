package raft.requestBean;

import java.util.Objects;

public class RequestVoteArgs {

    RequestVoteArgs(){

    }

    //候选人的任期号
    private int  Term;
    //发起请求的候选人 id
    private String CandidateId;
    //请求对象的ID
    private String replyId;

    private RequestVoteArgs(int Term, String CandidateId, String replyId){
        this.Term = Term;
        this.CandidateId= CandidateId;
        this.replyId =replyId;
    }

    public RequestVoteArgs(Builder b){
        this(b.Term,b.CandidateId,b.replyId);
    }


    public static  Builder newBuilder(){
        return new RequestVoteArgs.Builder();
    }

    public int getTerm() {
        return Term;
    }


    public String getCandidateId() {
        return CandidateId;
    }

    public String getReplyId() {
        return replyId;
    }



    public static class Builder{
        private  int Term;
        private  String CandidateId;
        private String replyId;


        public  Builder setTerm(int Term){
            this.Term=  Term;
            return this;
        }

        public  Builder setCandidateId(String CandidateId){
            this.CandidateId=  CandidateId;
            return this;
        }

        public Builder setReplyId(String replyId) {
            this.replyId = replyId;
            return this;
        }

        public  RequestVoteArgs build(){
            Objects.requireNonNull(Term);
            Objects.requireNonNull(CandidateId);
            Objects.requireNonNull(replyId);
            return new RequestVoteArgs(this);
        }

    }
}
