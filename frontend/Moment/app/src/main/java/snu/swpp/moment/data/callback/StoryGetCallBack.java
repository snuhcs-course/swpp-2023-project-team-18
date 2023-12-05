package snu.swpp.moment.data.callback;

import java.util.List;
import snu.swpp.moment.data.model.StoryModel;

public interface StoryGetCallBack {

    void onSuccess(List<StoryModel> story);

    void onFailure(Exception error);
}
