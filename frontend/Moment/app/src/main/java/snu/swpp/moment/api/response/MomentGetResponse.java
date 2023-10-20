package snu.swpp.moment.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import snu.swpp.moment.data.model.MomentPair;

public class MomentGetResponse {
    @SerializedName("moments")
    private List<Moment> momentList;
    private class Moment {
        @SerializedName("id")
        private int id;
        @SerializedName("moment")
        private String moment;
        @SerializedName("reply")
        private String reply;
        @SerializedName("moment_created_at")
        private long moment_created_at;
        @SerializedName("reply_created_at")
        private long reply_created_at;

        public int getId() { return id; }
        public String getMoment() { return moment; }
        public String getReply() { return reply; }
        public Long getMomentCreatedAt() { return moment_created_at; }
        public Long getReplyCreatedAt() {return reply_created_at; }
    }

    public ArrayList<MomentPair> getMomentList() {
        ArrayList<MomentPair> result = new ArrayList<>();
        for (Moment moment: momentList) {
            result.add(new MomentPair(
                    moment.getId(),
                    moment.getMoment(),
                    moment.getReply(),
                    moment.moment_created_at,
                    moment.reply_created_at)
            );
        }
        return result;
    }
}
