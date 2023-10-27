package snu.swpp.moment.data.source;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import snu.swpp.moment.api.RetrofitClient;
import snu.swpp.moment.api.ServiceApi;
import snu.swpp.moment.api.request.MomentWriteRequest;
import snu.swpp.moment.api.response.MomentGetResponse;
import snu.swpp.moment.api.response.MomentWriteResponse;
import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.callback.MomentWriteCallBack;
import snu.swpp.moment.data.model.MomentPairModel;

public class MomentRemoteDataSource {

    private ServiceApi service;
    private MomentPairModel momentPair;
    private Integer error;
    private final int NO_INTERNET = 0;

    public void getMoment(String access_token, long start, long end, MomentGetCallBack callback) {
        String bearer = "Bearer " + access_token;
        service = RetrofitClient.getClient().create(ServiceApi.class);
        service.getMoments(bearer, start, end).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<MomentGetResponse> call,
                Response<MomentGetResponse> response) {
                if (response.isSuccessful()) {
                    MomentGetResponse result = response.body();
                    callback.onSuccess(result.getMomentList());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<MomentGetResponse> call, Throwable t) {
                callback.onFailure(NO_INTERNET);
            }
        });
    }

    public void writeMoment(String access_token, String moment, MomentWriteCallBack callback) {
        String bearer = "Bearer " + access_token;
        MomentWriteRequest request = new MomentWriteRequest(moment);
        service = RetrofitClient.getClient().create(ServiceApi.class);
        service.writeMoment(bearer, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<MomentWriteResponse> call,
                Response<MomentWriteResponse> response) {
                if (response.isSuccessful()) {
                    MomentWriteResponse result = response.body();
                    callback.onSuccess(result.getMomentPair());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<MomentWriteResponse> call, Throwable t) {
                callback.onFailure(NO_INTERNET);
            }
        });

    }


}
