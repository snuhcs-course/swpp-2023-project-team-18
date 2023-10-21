package snu.swpp.moment.api.response;

import com.google.gson.annotations.SerializedName;

import snu.swpp.moment.data.model.MomentPair;

public class MomentWriteResponse {

    @SerializedName("moment")
    private Moment moment;

    public static class Moment{

        @SerializedName("id")
        int id;
        @SerializedName("moment")
        String moment;
        @SerializedName("reply")
        String reply;
        @SerializedName("moment_created_at")
        int moment_created_at;
        @SerializedName("reply_created_at")
        int reply_created_at;

        public int getId() { return id; }
        public String getMoment() { return moment; }
        public String getReply() { return reply; }
        public int getReply_created_at() { return reply_created_at; }
        public int getMoment_created_at() { return moment_created_at; }
    }

    public MomentPair getMomentPair(){
        return new MomentPair(moment.getId(), moment.getMoment(), moment.getReply(), moment.getMoment_created_at(), moment.getReply_created_at());
    }
}
