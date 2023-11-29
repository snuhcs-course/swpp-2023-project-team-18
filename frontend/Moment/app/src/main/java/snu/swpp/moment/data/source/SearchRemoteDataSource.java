package snu.swpp.moment.data.source;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import snu.swpp.moment.api.response.SearchContentsResponse;
import snu.swpp.moment.api.response.SearchHashTagGetCompleteResponse;
import snu.swpp.moment.api.response.SearchHashtagsResponse;
import snu.swpp.moment.data.callback.SearchEntriesGetCallBack;
import snu.swpp.moment.data.callback.SearchHashTagCompleteCallBack;
import snu.swpp.moment.data.callback.SearchHashTagGetCallBack;
import snu.swpp.moment.exception.NoInternetException;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.exception.UnknownErrorException;

public class SearchRemoteDataSource extends BaseRemoteDataSource {

    public void getCompleteHashTagList(String access_token, String query,
        SearchHashTagCompleteCallBack callback) {
        String bearer = "Bearer " + access_token;

        service.getCompleteHashTags(bearer, query).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<SearchHashTagGetCompleteResponse> call,
                Response<SearchHashTagGetCompleteResponse> response) {
                Log.d("APICall", "getCompleteHashTags: " + response.code());
                if (response.isSuccessful()) {
                    SearchHashTagGetCompleteResponse result = response.body();
                    callback.onSuccess(result.getHashtagList());
                } else if (response.code() == 401) {
                    callback.onFailure(new UnauthorizedAccessException());
                } else {
                    callback.onFailure(new UnknownErrorException());
                }
            }

            @Override
            public void onFailure(Call<SearchHashTagGetCompleteResponse> call, Throwable t) {
                Log.d("APICall", "getCompleteHashTags: onFailure");
                callback.onFailure(new NoInternetException());
            }
        });
    }

    public void getContentSearchList(String access_token, String query,
        SearchEntriesGetCallBack callback) {
        String bearer = "Bearer " + access_token;

        service.getContentSearchList(bearer, query).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<SearchContentsResponse> call,
                Response<SearchContentsResponse> response) {
                Log.d("APICall", "getContentSearchList: " + response.code());
                if (response.isSuccessful()) {
                    Log.d("APIContent", "content: " + response.body().getSearchEntries());
                    SearchContentsResponse result = response.body();
                    Log.d("SearchDataSource", "response list null? " + (response.body().getSearchEntries()==null));
                    callback.onSuccess(result);
                } else if (response.code() == 401) {
                    callback.onFailure(new UnauthorizedAccessException());
                } else {
                    callback.onFailure(new UnknownErrorException());
                }
            }

            @Override
            public void onFailure(Call<SearchContentsResponse> call, Throwable t) {
                Log.d("APICall", "getContentSearchList: onFailure");
                callback.onFailure(new NoInternetException());
            }
        });
    }

    public void getHashtagSearchList(String access_token, String query,
        SearchHashTagGetCallBack callback) {
        String bearer = "Bearer " + access_token;

        service.getHashtagSearchList(bearer, query).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<SearchHashtagsResponse> call,
                Response<SearchHashtagsResponse> response) {
                Log.d("APICall", "getHashtagSearchList: " + response.code());
                if (response.isSuccessful()) {
                    SearchHashtagsResponse result = response.body();
                    callback.onSuccess(result);
                } else if (response.code() == 401) {
                    callback.onFailure(new UnauthorizedAccessException());
                } else {
                    callback.onFailure(new UnknownErrorException());
                }
            }

            @Override
            public void onFailure(Call<SearchHashtagsResponse> call, Throwable t) {
                Log.d("APICall", "getHashtagSearchList: onFailure");
                callback.onFailure(new NoInternetException());
            }
        });
    }
}
