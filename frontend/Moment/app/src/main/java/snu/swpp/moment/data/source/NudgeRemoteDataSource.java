package snu.swpp.moment.data.source;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import snu.swpp.moment.api.response.NudgeGetResponse;
import snu.swpp.moment.api.response.NudgeDeleteResponse;
import snu.swpp.moment.data.callback.NudgeGetCallback;
import snu.swpp.moment.data.callback.NudgeDeleteCallback;
import snu.swpp.moment.exception.NoInternetException;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.exception.UnknownErrorException;

public class NudgeRemoteDataSource extends BaseRemoteDataSource {

    public void getNudge(String access_token, long start, long end, NudgeGetCallback callback) {
        String bearer = "Bearer " + access_token;
        service.getNudge(bearer, start, end).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<NudgeGetResponse> call,
                Response<NudgeGetResponse> response) {
                Log.d("APICall", "getNudge: " + response.code());
                if (response.isSuccessful()) {
                    NudgeGetResponse result = response.body();
                    callback.onSuccess(result.getNudge());
                } else if (response.code() == 401) {
                    callback.onFailure(new UnauthorizedAccessException());
                } else {
                    callback.onFailure(new UnknownErrorException());
                }
            }

            @Override
            public void onFailure(Call<NudgeGetResponse> call, Throwable t) {
                Log.d("APICall", "getNudge: onFailure");
                callback.onFailure(new NoInternetException());

            }
        });
    }

    public void deleteNudge(String access_token, NudgeDeleteCallback callback) {
        String bearer = "Bearer " + access_token;
        service.deleteNudge(bearer).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<NudgeDeleteResponse> call,
                Response<NudgeDeleteResponse> response) {
                Log.d("APICall", "markNudge: " + response.code());

                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else if (response.code() == 401) {
                    callback.onFailure(new UnauthorizedAccessException());
                } else {
                    callback.onFailure(new UnknownErrorException());
                }
            }

            @Override
            public void onFailure(Call<NudgeDeleteResponse> call, Throwable t) {
                Log.d("APICall", "markNudge: onFailure");
                callback.onFailure(new NoInternetException());

            }
        });
    }
}
