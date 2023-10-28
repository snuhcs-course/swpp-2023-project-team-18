package snu.swpp.moment.data.model;

import java.util.Date;
import snu.swpp.moment.utils.TimeConverter;


public class MomentPairModel {

    private final long id;
    private final String moment;
    private final String reply;
    private final Date momentCreatedAt;
    private final Date replyCreatedAt;

    public MomentPairModel(long id, String moment, String reply, long momentCreatedAt,
        long replyCreatedAt) {
        this.id = id;
        this.moment = moment;
        this.reply = reply;
        this.momentCreatedAt = TimeConverter.convertTimestampToDate(momentCreatedAt);
        this.replyCreatedAt = TimeConverter.convertTimestampToDate(replyCreatedAt);
    }

    public String getMoment() {
        return moment;
    }

    public String getReply() {
        return reply;
    }

    public Date getMomentCreatedTime() {
        return momentCreatedAt;
    }

    public Date getReplyCreatedTime() {
        return replyCreatedAt;
    }
}
