package snu.swpp.moment.data.callback;

import snu.swpp.moment.api.response.StoryCompletionNotifyResponse;

public interface StoryCompletionNotifyCallBack {

    void onSuccess(StoryCompletionNotifyResponse response);

    void onFailure(Exception error);
}
