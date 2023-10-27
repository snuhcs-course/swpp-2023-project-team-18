package snu.swpp.moment.data.callback;

import java.util.ArrayList;
import snu.swpp.moment.data.model.MomentPairModel;

public interface MomentGetCallBack {

    void onSuccess(ArrayList<MomentPairModel> momentPair);

    void onFailure(int error);
}
