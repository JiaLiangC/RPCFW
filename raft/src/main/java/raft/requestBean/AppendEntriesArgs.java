package raft.requestBean;

import java.util.List;

public class AppendEntriesArgs {
    private int Term;
    private String LeaderId;
    private String replyId;
    private List<Entry> Entries;
    private long PrevLogIndex;
    private int   PrevLogTerm;

    private long LeaderCommit;

    AppendEntriesArgs(){}

    private  AppendEntriesArgs(int Term, String LeaderId, List<Entry> entries,String replyId,long pPrevLogIndex,int prevLogTerm,long LeaderCommit){
        this.Term=Term;
        this.LeaderId=LeaderId;
        this.replyId=replyId;
        this.Entries = entries;
        this.PrevLogIndex =pPrevLogIndex;
        this.PrevLogTerm = prevLogTerm;
        this.LeaderCommit = LeaderCommit;
    }

    private  AppendEntriesArgs(Builder b){
        this(b.Term,b.LeaderId,b.Entries,b.replyId,b.PrevLogIndex,b.PrevLogTerm,b.LeaderCommit);
    }

    public static Builder newBuilder(){
        return new Builder();
    }

    public String getReplyId() {
        return replyId;
    }

    public static class Builder{
        private int Term;
        private String LeaderId;
        private List<Entry> Entries;
        private  String replyId;
        private long PrevLogIndex;
        private int   PrevLogTerm;
        private long LeaderCommit;

        public Builder setTerm(int Term){
            this.Term =Term;
            return this;
        }

        public Builder setLeaderCommit(long leaderCommit) {
            LeaderCommit = leaderCommit;
            return this;
        }


        public Builder setPrevLogIndex(long prevLogIndex) {
            PrevLogIndex = prevLogIndex;
            return this;
        }

        public Builder setPrevLogTerm(int prevLogTerm) {
            PrevLogTerm = prevLogTerm;
            return this;
        }


        public Builder setLeaderId(String LeaderId){
            this.LeaderId= LeaderId;
            return this;
        }

        public Builder setEntries(List<Entry> entries){
            this.Entries = entries;
            return this;
        }

        public Builder setReplyId(String replyId) {
            this.replyId = replyId;
            return this;
        }

        public AppendEntriesArgs build(){
            return  new AppendEntriesArgs(this);
        }

    }

    public long getLeaderCommit() {
        return LeaderCommit;
    }

    public int getTerm() {
        return Term;
    }


    public String getLeaderId() {
        return LeaderId;
    }


   public List<Entry> getEntries() {
        return Entries;
    }

    public void setEntries(List<Entry> entries) {
        Entries = entries;
    }


    public long getPrevLogIndex() {
        return PrevLogIndex;
    }

    public int getPrevLogTerm() {
        return PrevLogTerm;
    }
}
