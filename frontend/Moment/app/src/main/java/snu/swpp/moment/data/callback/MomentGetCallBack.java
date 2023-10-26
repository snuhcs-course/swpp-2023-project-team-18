package snu.swpp.moment.data.callback;

import java.util.ArrayList;
import snu.swpp.moment.data.model.MomentPair;

public interface MomentGetCallBack {

    void onSuccess(ArrayList<MomentPair> momentPair);

    void onFailure(int error);
}
