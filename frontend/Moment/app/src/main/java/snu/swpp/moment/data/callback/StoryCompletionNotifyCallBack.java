package snu.swpp.moment.data.callback;

import snu.swpp.moment.api.response.StoryCompletionNotifyResponse;

public interface StoryCompletionNotifyCallBack {

    void onSuccess(int storyId);

    void onFailure(Exception error);
}
