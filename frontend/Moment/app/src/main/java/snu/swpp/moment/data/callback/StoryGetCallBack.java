package snu.swpp.moment.data.callback;

import java.util.ArrayList;
import snu.swpp.moment.data.model.Story;

public interface StoryGetCallBack {
    void onSuccess(ArrayList<Story> story);
    void onFailure(Exception error);
}
