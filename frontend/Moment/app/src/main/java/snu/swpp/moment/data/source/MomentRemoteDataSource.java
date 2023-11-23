package snu.swpp.moment.data.source;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import snu.swpp.moment.api.request.MomentWriteRequest;
import snu.swpp.moment.api.response.MomentGetResponse;
import snu.swpp.moment.api.response.MomentWriteResponse;
import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.callback.MomentWriteCallBack;
import snu.swpp.moment.exception.NoInternetException;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.exception.UnknownErrorException;

public class MomentRemoteDataSource extends BaseRemoteDataSource {

    public void getMoment(String access_token, long start, long end, MomentGetCallBack callback) {
        String bearer = "Bearer " + access_token;
        service.getMoments(bearer, start, end).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<MomentGetResponse> call,
                Response<MomentGetResponse> response) {
                Log.d("APICall", "getMoment: " + response.code());
                if (response.isSuccessful()) {
                    MomentGetResponse result = response.body();
                    callback.onSuccess(result.getMomentList());
                } else if (response.code() == 401) {
                    callback.onFailure(new UnauthorizedAccessException());
                } else {
                    callback.onFailure(new UnknownErrorException());
                }
            }

            @Override
            public void onFailure(Call<MomentGetResponse> call, Throwable t) {
                Log.d("APICall", "getMoment: onFailure");
                callback.onFailure(new NoInternetException());
            }
        });
    }

    public void writeMoment(String access_token, String moment, MomentWriteCallBack callback) {
        String bearer = "Bearer " + access_token;
        MomentWriteRequest request = new MomentWriteRequest(moment);
        service.writeMoment(bearer, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<MomentWriteResponse> call,
                Response<MomentWriteResponse> response) {
                Log.d("APICall", "writeMoment: " + response.code());
                if (response.isSuccessful()) {
                    MomentWriteResponse result = response.body();
                    Log.d("MomentRemoteDataSource",
                        "Got AI reply: " + result.getMomentPair().getReply());
                    callback.onSuccess(result.getMomentPair());
                } else if (response.code() == 401) {
                    callback.onFailure(new UnauthorizedAccessException());
                } else {
                    callback.onFailure(new UnknownErrorException());
                }
            }

            @Override
            public void onFailure(Call<MomentWriteResponse> call, Throwable t) {
                Log.d("APICall", "writeMoment: onFailure");
                callback.onFailure(new NoInternetException());
            }
        });
    }
}
