package snu.swpp.moment.data.callback;

public interface NudgeGetCallback {

    void onSuccess(String nudge);

    void onFailure(Exception error);
}
