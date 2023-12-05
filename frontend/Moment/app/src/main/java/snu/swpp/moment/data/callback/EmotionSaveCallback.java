package snu.swpp.moment.data.callback;

public interface EmotionSaveCallback {

    void onSuccess();

    void onFailure(Exception error);
}
