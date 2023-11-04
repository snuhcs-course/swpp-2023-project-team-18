package snu.swpp.moment.data.callback;

public interface StoryCompletionNotifyCallBack {

    void onSuccess(int storyId);

    void onFailure(Exception error);
}
