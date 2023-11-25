package snu.swpp.moment.data.callback;

public interface NicknameCallBack {
    void onSuccess(String nickname);
    void onFailure(Exception error);
}
