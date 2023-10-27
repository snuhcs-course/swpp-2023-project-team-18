package snu.swpp.moment.ui.main_writeview;

import java.text.SimpleDateFormat;
import java.util.Locale;
import snu.swpp.moment.data.model.MomentPairModel;

public class ListViewItem {

    private final String userInput;
    private final String serverResponse;
    private final String inputTime;

    public ListViewItem(String userInput, String inputTime, String serverResponse) {
        this.userInput = userInput;
        this.inputTime = inputTime;
        this.serverResponse = serverResponse; // 초기에는 서버 응답이 없기 때문에 빈 문자열로 설정
    }

    public ListViewItem(MomentPairModel momentPair) {
        userInput = momentPair.getMoment();
        inputTime = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(
            momentPair.getMomentCreatedTime());
        serverResponse = momentPair.getReply();
    }

    // 사용자 입력
    public String getUserInput() {
        return this.userInput;
    }

    // 서버 응답
    public String getServerResponse() {
        return this.serverResponse;
    }

    // 입력 시간
    public String getInputTime() {
        return this.inputTime;
    }
}