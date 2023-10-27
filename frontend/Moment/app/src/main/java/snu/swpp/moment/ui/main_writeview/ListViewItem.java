package snu.swpp.moment.ui.main_writeview;

import java.text.SimpleDateFormat;
import java.util.Locale;
import snu.swpp.moment.data.model.MomentPair;

public class ListViewItem {

    private String userInput;
    private String serverResponse;
    private String inputTime;

    public ListViewItem(String userInput, String inputTime, String serverResponse) {
        this.userInput = userInput;
        this.inputTime = inputTime;
        this.serverResponse = serverResponse; // 초기에는 서버 응답이 없기 때문에 빈 문자열로 설정
    }

    public ListViewItem(MomentPair momentPair) {
        userInput = momentPair.getMoment();
        inputTime = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(
            momentPair.getMomentCreatedTime());
        serverResponse = momentPair.getReply();
    }

    // 사용자 입력 getter 및 setter
    public String getUserInput() {
        return this.userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    // 서버 응답 getter 및 setter
    public String getServerResponse() {
        return this.serverResponse;
    }

    public void setServerResponse(String serverResponse) {
        this.serverResponse = serverResponse;
    }

    // 입력 시간 getter 및 setter
    public String getInputTime() {
        return this.inputTime;
    }

    public void setInputTime(String inputTime) {
        this.inputTime = inputTime;
    }
}