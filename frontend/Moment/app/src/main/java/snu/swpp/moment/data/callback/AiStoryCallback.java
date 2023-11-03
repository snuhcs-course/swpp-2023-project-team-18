package snu.swpp.moment.data.callback;

public interface AiStoryCallback {

    void onSuccess(String title, String content);

    void onFailure(Exception error);
}
