package snu.swpp.moment.data.callback;

import snu.swpp.moment.api.response.SearchContentsResponse;

public interface SearchEntriesGetCallBack {

    void onSuccess(SearchContentsResponse response);

    void onFailure(Throwable t);
}
