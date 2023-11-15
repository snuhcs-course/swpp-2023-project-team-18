package snu.swpp.moment.data.callback;

import snu.swpp.moment.api.response.SearchEntriesGetResponse;

public interface SearchEntriesGetCallBack {
    void onSuccess(SearchEntriesGetResponse response);
    void onFailure(Throwable t);
}
