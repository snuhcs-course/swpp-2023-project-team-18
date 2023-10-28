package snu.swpp.moment.data.source;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import snu.swpp.moment.api.RetrofitClient;
import snu.swpp.moment.api.ServiceApi;
import snu.swpp.moment.api.request.EmotionSaveRequest;
import snu.swpp.moment.api.request.HashtagSaveRequest;
import snu.swpp.moment.api.request.ScoreSaveRequest;
import snu.swpp.moment.api.request.StoryCompletionNotifyRequest;
import snu.swpp.moment.api.request.StorySaveRequest;
import snu.swpp.moment.api.response.AIStoryGetResponse;
import snu.swpp.moment.api.response.EmotionSaveResponse;
import snu.swpp.moment.api.response.HashtagSaveResponse;
import snu.swpp.moment.api.response.ScoreSaveResponse;
import snu.swpp.moment.api.response.StoryCompletionNotifyResponse;
import snu.swpp.moment.api.response.StoryGetResponse;
import snu.swpp.moment.api.response.StorySaveResponse;
import snu.swpp.moment.data.callback.AIStoryCallback;
import snu.swpp.moment.data.callback.EmotionSaveCallback;
import snu.swpp.moment.data.callback.HashtagSaveCallback;
import snu.swpp.moment.data.callback.ScoreSaveCallback;
import snu.swpp.moment.data.callback.StoryCompletionNotifyCallBack;
import snu.swpp.moment.data.callback.StoryGetCallBack;
import snu.swpp.moment.data.callback.StorySaveCallback;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.exception.AIStoryRetrievalFailureException;
import snu.swpp.moment.exception.InvalidEmotionException;
import snu.swpp.moment.exception.InvalidHashtagSaveRequestException;
import snu.swpp.moment.exception.InvalidScoreSaveRequestException;
import snu.swpp.moment.exception.InvalidStoryCompletionTimeException;
import snu.swpp.moment.exception.NoInternetException;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.exception.UnknownErrorException;

public class StoryRemoteDataSource {

    private ServiceApi service;
    private MomentPairModel momentPair;
    private Exception error;

    public void getStory(String access_token, long start, long end, StoryGetCallBack callBack) {
        String bearer = "Bearer " + access_token;
        service = RetrofitClient.getClient().create(ServiceApi.class);
        service.getStories(bearer, start, end).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<StoryGetResponse> call,
                Response<StoryGetResponse> response) {
                if (response.isSuccessful()) {
                    StoryGetResponse result = response.body();
                    callBack.onSuccess(result.getStoryList());
                } else if (response.code() == 401) {
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

    public void notifyCompletion(String access_token, long start, long end,
        StoryCompletionNotifyCallBack callback) {
        String bearer = "Bearer " + access_token;
        service = RetrofitClient.getClient().create(ServiceApi.class);
        StoryCompletionNotifyRequest request = new StoryCompletionNotifyRequest(start, end);

        service.notifyStoryCompletion(bearer, request).enqueue(
            new Callback<StoryCompletionNotifyResponse>() {
                @Override
                public void onResponse(Call<StoryCompletionNotifyResponse> call,
                    Response<StoryCompletionNotifyResponse> response) {
                    if (response.isSuccessful()) {
                        callback.onSuccess(response.body());
                    } else if (response.code() == 400) {
                        callback.onFailure(new InvalidStoryCompletionTimeException());
                    } else if (response.code() == 401) {
                        callback.onFailure(new UnauthorizedAccessException());
                    } else {
                        callback.onFailure(new UnknownErrorException());
                    }
                }

                @Override
                public void onFailure(Call<StoryCompletionNotifyResponse> call, Throwable t) {
                    callback.onFailure(new NoInternetException());
                }
            });
    }

    public void getAIGeneratedStory(String access_token, AIStoryCallback callback) {
        String bearer = "Bearer " + access_token;
        service = RetrofitClient.getClient().create(ServiceApi.class);

        service.getAIGeneratedStory(bearer).enqueue(new Callback<AIStoryGetResponse>() {
            @Override
            public void onResponse(Call<AIStoryGetResponse> call,
                Response<AIStoryGetResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else if (response.code() == 401) {
                    callback.onFailure(new UnauthorizedAccessException());
                } else if (response.code() == 500) {
                    callback.onFailure(new AIStoryRetrievalFailureException());
                } else {
                    callback.onFailure(new UnknownErrorException());
                }
            }

            @Override
            public void onFailure(Call<AIStoryGetResponse> call, Throwable t) {
                callback.onFailure(new NoInternetException());
            }
        });
    }

    public void saveStory(String access_token, String title, String content,
        StorySaveCallback callback) {
        String bearer = "Bearer " + access_token;
        service = RetrofitClient.getClient().create(ServiceApi.class);
        StorySaveRequest request = new StorySaveRequest(title, content);

        service.saveStory(bearer, request).enqueue(new Callback<StorySaveResponse>() {
            @Override
            public void onResponse(Call<StorySaveResponse> call,
                Response<StorySaveResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else if (response.code() == 401) {
                    callback.onFailure(new UnauthorizedAccessException());
                } else {
                    callback.onFailure(new UnknownErrorException());
                }
            }

            @Override
            public void onFailure(Call<StorySaveResponse> call, Throwable t) {
                callback.onFailure(new NoInternetException());
            }
        });
    }

    public void saveEmotion(String access_token, String emotion, EmotionSaveCallback callback) {
        String bearer = "Bearer " + access_token;
        service = RetrofitClient.getClient().create(ServiceApi.class);
        EmotionSaveRequest request = new EmotionSaveRequest(emotion);

        service.saveEmotion(bearer, request).enqueue(new Callback<EmotionSaveResponse>() {
            @Override
            public void onResponse(Call<EmotionSaveResponse> call,
                Response<EmotionSaveResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else if (response.code() == 400) {
                    callback.onFailure(new InvalidEmotionException());
                } else if (response.code() == 401) {
                    callback.onFailure(new UnauthorizedAccessException());
                } else {
                    callback.onFailure(new UnknownErrorException());
                }
            }

            @Override
            public void onFailure(Call<EmotionSaveResponse> call, Throwable t) {
                callback.onFailure(new NoInternetException());
            }
        });
    }

    public void saveScore(String access_token, int story_id, int score,
        ScoreSaveCallback callback) {
        String bearer = "Bearer " + access_token;
        service = RetrofitClient.getClient().create(ServiceApi.class);
        ScoreSaveRequest request = new ScoreSaveRequest(story_id, score);

        service.saveScore(bearer, request).enqueue(new Callback<ScoreSaveResponse>() {
            @Override
            public void onResponse(Call<ScoreSaveResponse> call,
                Response<ScoreSaveResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else if (response.code() == 401) {
                    callback.onFailure(new UnauthorizedAccessException());
                } else if (response.code() == 400) {
                    callback.onFailure(new InvalidScoreSaveRequestException());
                } else {
                    callback.onFailure(new UnknownErrorException());
                }
            }

            @Override
            public void onFailure(Call<ScoreSaveResponse> call, Throwable t) {
                callback.onFailure(new NoInternetException());
            }
        });
    }

    public void saveHashtags(String access_token, int story_id, String content,
        HashtagSaveCallback callback) {
        String bearer = "Bearer " + access_token;
        service = RetrofitClient.getClient().create(ServiceApi.class);
        HashtagSaveRequest request = new HashtagSaveRequest(story_id, content);

        service.saveHashtags(bearer, request).enqueue(new Callback<HashtagSaveResponse>() {
            @Override
            public void onResponse(Call<HashtagSaveResponse> call,
                Response<HashtagSaveResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else if (response.code() == 400) {
                    callback.onFailure(new InvalidHashtagSaveRequestException());
                } else if (response.code() == 401) {
                    callback.onFailure(new UnauthorizedAccessException());
                } else {
                    callback.onFailure(new UnknownErrorException());
                }
            }

            @Override
            public void onFailure(Call<HashtagSaveResponse> call, Throwable t) {
                callback.onFailure(new NoInternetException());
            }
        });
    }
}
