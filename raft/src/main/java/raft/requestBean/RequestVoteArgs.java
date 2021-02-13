package raft.requestBean;

import java.util.Objects;

public class RequestVoteArgs {
    //候选人的任期号
    private int  Term;
    //候选人 id
    private String CandidateId;

    private RequestVoteArgs(int Term, String CandidateId){
        this.Term = Term;
        this.CandidateId= CandidateId;
    }

    public RequestVoteArgs(Builder b){
        this(b.Term,b.CandidateId);
    }


    public static  Builder newBuilder(){
        return new RequestVoteArgs.Builder();
    }

    public static class Builder{
        int Term;
        String CandidateId;


        public  Builder setTerm(int Term){
            this.Term=  Term;
            return this;
        }

        public  Builder setCandidateId(String CandidateId){
            this.CandidateId=  CandidateId;
            return this;
        }

        public  RequestVoteArgs build(){
            Objects.requireNonNull(Term);
            Objects.requireNonNull(CandidateId);
            return new RequestVoteArgs(this);
        }

    }
}
