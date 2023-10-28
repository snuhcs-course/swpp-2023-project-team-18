package snu.swpp.moment.data.callback;

public interface ScoreSaveCallback {

    public void onSuccess();

    public void onFailure(Exception error);
}
