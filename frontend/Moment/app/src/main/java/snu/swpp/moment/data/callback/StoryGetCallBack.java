package snu.swpp.moment.data.callback;

import java.util.ArrayList;
import snu.swpp.moment.data.model.StoryModel;

public interface StoryGetCallBack {

    void onSuccess(ArrayList<StoryModel> story);

    void onFailure(Exception error);
}
