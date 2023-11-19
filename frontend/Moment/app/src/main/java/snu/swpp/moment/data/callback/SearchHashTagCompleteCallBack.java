package snu.swpp.moment.data.callback;

import java.util.List;

public interface SearchHashTagCompleteCallBack {

    void onSuccess(List<String> hashTagList);

    void onFailure(Exception error);
}
