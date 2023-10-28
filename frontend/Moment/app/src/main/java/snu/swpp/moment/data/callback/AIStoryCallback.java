package snu.swpp.moment.data.callback;

import snu.swpp.moment.api.response.AIStoryGetResponse;

public interface AIStoryCallback {

    public void onSuccess(AIStoryGetResponse response);

    public void onFailure(Exception error);
}
