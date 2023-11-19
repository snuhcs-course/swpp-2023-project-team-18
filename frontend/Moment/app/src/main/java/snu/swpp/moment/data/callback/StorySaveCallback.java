package snu.swpp.moment.data.callback;

public interface StorySaveCallback {

    void onSuccess();

    void onFailure(Exception error);
}
