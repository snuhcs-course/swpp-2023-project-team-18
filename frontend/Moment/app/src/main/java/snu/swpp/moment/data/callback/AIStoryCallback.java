package snu.swpp.moment.data.callback;

import snu.swpp.moment.api.response.AIStoryGetResponse;

public interface AIStoryCallback {

    void onSuccess(AIStoryGetResponse response);

    void onFailure(Exception error);
}
