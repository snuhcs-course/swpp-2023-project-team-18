package snu.swpp.moment.data.source;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import snu.swpp.moment.api.response.MomentGetResponse;
import snu.swpp.moment.api.response.NudgeGetResponse;
import snu.swpp.moment.api.response.NudgeMarkResponse;
import snu.swpp.moment.data.callback.NudgeGetCallback;
import snu.swpp.moment.data.callback.NudgeMarkCallback;
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

    public void markNudge(String access_token, NudgeMarkCallback callback) {
        String bearer = "Bearer " + access_token;
        service.markNudge(bearer).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<NudgeMarkResponse> call,
                Response<NudgeMarkResponse> response) {
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
            public void onFailure(Call<NudgeMarkResponse> call, Throwable t) {
                Log.d("APICall", "markNudge: onFailure");
                callback.onFailure(new NoInternetException());

            }
        });
    }
}
