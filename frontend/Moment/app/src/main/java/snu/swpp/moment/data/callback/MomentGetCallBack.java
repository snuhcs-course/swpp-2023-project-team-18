package snu.swpp.moment.data.callback;

import snu.swpp.moment.data.model.MomentPair;
import java.util.ArrayList;

public interface MomentGetCallBack {

    void onSuccess(ArrayList<MomentPair> momentPair);

    void onFailure(int error);
}
