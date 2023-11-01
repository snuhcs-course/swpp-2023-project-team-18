package snu.swpp.moment.ui.main_writeview;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import snu.swpp.moment.data.model.MomentPairModel;

public class ListViewItem {

    private final String userInput;
    private final String aiReply;
    private final String timestampText;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ListViewItem(String userInput, Date inputTime) {
        this.userInput = userInput;
        this.timestampText = dateFormat.format(inputTime);
        this.aiReply = ""; // 초기에는 서버 응답이 없기 때문에 빈 문자열로 설정
    }

    public ListViewItem(MomentPairModel momentPair) {
        userInput = momentPair.getMoment();
        timestampText = dateFormat.format(momentPair.getMomentCreatedTime());
        aiReply = momentPair.getReply();
    }

    public String getUserInput() {
        return userInput;
    }

    public String getAiReply() {
        return aiReply;
    }

    public boolean isWaitingAiReply() {
        // aiReply가 비어 있으면 아직 response를 받지 못한 것
        return aiReply.isEmpty();
    }

    // 입력 시간
    public String getTimestampText() {
        return timestampText;
    }
}