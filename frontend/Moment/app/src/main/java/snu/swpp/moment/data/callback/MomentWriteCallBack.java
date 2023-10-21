package snu.swpp.moment.data.callback;

import snu.swpp.moment.data.model.LoggedInUser;
import snu.swpp.moment.data.model.MomentPair;

public interface MomentWriteCallBack {
    void onSuccess(MomentPair momentPair);

    void onFailure(int error);
}
