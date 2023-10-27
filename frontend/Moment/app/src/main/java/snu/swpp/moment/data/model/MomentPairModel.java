package snu.swpp.moment.data.model;

import java.util.Date;
import snu.swpp.moment.utils.TimeConverter;


public class MomentPairModel {

    private final long id;
    private final String moment;
    private final String reply;
    private final Date moment_created_at;
    private final Date reply_created_at;

    public MomentPairModel(long id, String moment, String reply, long moment_created_at,
        long reply_created_at) {
        this.id = id;
        this.moment = moment;
        this.reply = reply;
        this.moment_created_at = TimeConverter.convertLongToDate(moment_created_at);
        this.reply_created_at = TimeConverter.convertLongToDate(reply_created_at);
    }

    public String getMoment() {
        return moment;
    }

    public String getReply() {
        return reply;
    }

    public Date getMomentCreatedTime() {
        return moment_created_at;
    }

    public Date getReplyCreatedTime() {
        return reply_created_at;
    }
}
