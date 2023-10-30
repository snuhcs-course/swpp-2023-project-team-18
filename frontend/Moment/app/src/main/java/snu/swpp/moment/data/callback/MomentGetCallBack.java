package snu.swpp.moment.data.callback;

import java.util.List;
import snu.swpp.moment.data.model.MomentPairModel;

public interface MomentGetCallBack {

    void onSuccess(List<MomentPairModel> momentPair);

    void onFailure(Exception error);
}
