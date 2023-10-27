package snu.swpp.moment.data.source;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import snu.swpp.moment.api.RetrofitClient;
import snu.swpp.moment.api.ServiceApi;
import snu.swpp.moment.api.response.StoryGetResponse;
import snu.swpp.moment.data.callback.StoryGetCallBack;
import snu.swpp.moment.data.model.MomentPair;
import snu.swpp.moment.exception.NoInternetException;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.exception.UnknownErrorException;

public class StoryRemoteDataSource {
    private ServiceApi service;
    private MomentPair momentPair;
    private Exception error;

    public void getStory(String access_token, long start, long end, StoryGetCallBack callBack) {
        String bearer = "Bearer " + access_token;
        service = RetrofitClient.getClient().create(ServiceApi.class);
        service.getStories(bearer, start, end).enqueue(new Callback<StoryGetResponse>() {
            @Override
            public void onResponse(Call<StoryGetResponse> call,
                Response<StoryGetResponse> response) {
                if (response.isSuccessful()) {
                    StoryGetResponse result = response.body();
                    callBack.onSuccess(result.getStoryList());
                } else if (response.code()==401){
                    callBack.onFailure(new UnauthorizedAccessException());
                } else {
                    callBack.onFailure(new UnknownErrorException());
                }
            }

            @Override
            public void onFailure(Call<StoryGetResponse> call, Throwable t) {
                callBack.onFailure(new NoInternetException());
            }
        });
    }
}
