package snu.swpp.moment.data.callback;

public interface NudgeMarkCallback {

    void onSuccess();

    void onFailure(Exception error);
}
