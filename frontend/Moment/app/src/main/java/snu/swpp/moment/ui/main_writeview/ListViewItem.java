package snu.swpp.moment.ui.main_writeview;

import java.util.Date;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.utils.TimeConverter;

public class ListViewItem {

    private final String userInput;
    private final String aiReply;
    private final Date createdAt;
    private final String timestampText;

    private final String TIME_FORMAT = "HH:mm";

    public ListViewItem(String userInput, Date createdAt) {
        this.userInput = userInput;
        this.aiReply = "";  // 생성 직후에는 서버 응답이 없기 때문에 빈 문자열로 설정
        this.createdAt = createdAt;
        this.timestampText = TimeConverter.formatDate(createdAt, TIME_FORMAT);
    }

    public ListViewItem(MomentPairModel momentPair) {
        userInput = momentPair.getMoment();
        aiReply = momentPair.getReply();
        createdAt = momentPair.getMomentCreatedTime();
        timestampText = TimeConverter.formatDate(momentPair.getReplyCreatedTime(), TIME_FORMAT);
    }

    public String getUserInput() {
        return userInput;
    }

    public String getAiReply() {
        return aiReply;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getTimestampText() {
        return timestampText;
    }

    public boolean isWaitingAiReply() {
        // aiReply가 비어 있으면 아직 response를 받지 못한 것
        return aiReply.isEmpty();
    }
}