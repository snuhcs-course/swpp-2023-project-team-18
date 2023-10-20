package snu.swpp.moment.data.source;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import snu.swpp.moment.api.RetrofitClient;
import snu.swpp.moment.api.ServiceApi;
import snu.swpp.moment.api.response.MomentGetResponse;
import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.model.MomentPair;
import snu.swpp.moment.data.model.Token;

public class MomentRemoteDataSource {
    private ServiceApi service;
    private MomentPair momentPair;
    private Integer error;
    private final int NO_INTERNET = 0;

    public void getMoment(String access_token, long start, long end, MomentGetCallBack callback) {
        String bearer = "Bearer " + access_token;
        service = RetrofitClient.getClient().create(ServiceApi.class);
        service.getMoments(bearer, start, end).enqueue(new Callback<MomentGetResponse>() {
            @Override
            public void onResponse(Call<MomentGetResponse> call, Response<MomentGetResponse> response) {
                if (response.isSuccessful()) {
                    MomentGetResponse result = response.body();
                    System.out.println("#DEBUG: " + result.getMomentList().get(0).getMoment());
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
}
