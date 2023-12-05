package snu.swpp.moment.data.callback;

public interface ScoreSaveCallback {

    void onSuccess();

    void onFailure(Exception error);
}
