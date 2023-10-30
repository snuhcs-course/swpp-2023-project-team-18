package snu.swpp.moment.data.callback;

import snu.swpp.moment.data.model.MomentPairModel;

public interface MomentWriteCallBack {

    void onSuccess(MomentPairModel momentPair);

    void onFailure(Exception error);
}
