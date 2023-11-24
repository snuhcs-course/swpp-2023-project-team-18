package snu.swpp.moment.data.callback;

public interface NudgeDeleteCallback {

    void onSuccess();

    void onFailure(Exception error);
}
