package snu.swpp.moment.data.callback;

import snu.swpp.moment.api.response.SearchHashtagsResponse;

public interface SearchHashTagGetCallBack {

    void onSuccess(SearchHashtagsResponse response);

    void onFailure(Throwable t);
}
