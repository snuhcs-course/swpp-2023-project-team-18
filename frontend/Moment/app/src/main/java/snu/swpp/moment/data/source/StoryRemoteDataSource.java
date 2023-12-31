package snu.swpp.moment.data.source;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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
import snu.swpp.moment.data.callback.AiStoryCallback;
import snu.swpp.moment.data.callback.EmotionSaveCallback;
import snu.swpp.moment.data.callback.HashtagSaveCallback;
import snu.swpp.moment.data.callback.ScoreSaveCallback;
import snu.swpp.moment.data.callback.StoryCompletionNotifyCallBack;
import snu.swpp.moment.data.callback.StoryGetCallBack;
import snu.swpp.moment.data.callback.StorySaveCallback;
import snu.swpp.moment.exception.AIStoryRetrievalFailureException;
import snu.swpp.moment.exception.InvalidEmotionException;
import snu.swpp.moment.exception.InvalidHashtagSaveRequestException;
import snu.swpp.moment.exception.InvalidScoreSaveRequestException;
import snu.swpp.moment.exception.InvalidStoryCompletionTimeException;
import snu.swpp.moment.exception.NoInternetException;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.exception.UnknownErrorException;

public class StoryRemoteDataSource extends BaseRemoteDataSource {

    public void getStory(String access_token, long start, long end, StoryGetCallBack callBack) {
        String bearer = "Bearer " + access_token;
        Log.d("StoryRemoteDataSource", "getStory start: " + start + ", end: " + end);
        service.getStories(bearer, start, end).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<StoryGetResponse> call,
                Response<StoryGetResponse> response) {
                Log.d("APICall", "getStory: " + response.code());
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
        StoryCompletionNotifyRequest request = new StoryCompletionNotifyRequest(start, end);

        service.notifyStoryCompletion(bearer, request).enqueue(
            new Callback<>() {
                @Override
                public void onResponse(Call<StoryCompletionNotifyResponse> call,
                    Response<StoryCompletionNotifyResponse> response) {
                    Log.d("APICall", "notifyCompletion: " + response.code());
                    if (response.isSuccessful()) {
                        callback.onSuccess(response.body().getId());
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

    public void getAiGeneratedStory(String access_token, AiStoryCallback callback) {
        String bearer = "Bearer " + access_token;

        service.getAIGeneratedStory(bearer).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<AIStoryGetResponse> call,
                Response<AIStoryGetResponse> response) {
                Log.d("APICall", "getAIGeneratedStory: " + response.code());
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body().getTitle(), response.body().getContent());
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
        StorySaveRequest request = new StorySaveRequest(title, content);

        service.saveStory(bearer, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<StorySaveResponse> call,
                Response<StorySaveResponse> response) {
                Log.d("APICall", "saveStory: " + response.code());
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
        EmotionSaveRequest request = new EmotionSaveRequest(emotion);

        service.saveEmotion(bearer, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<EmotionSaveResponse> call,
                Response<EmotionSaveResponse> response) {
                Log.d("APICall", "saveEmotion: " + response.code());
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
        ScoreSaveRequest request = new ScoreSaveRequest(story_id, score);

        service.saveScore(bearer, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ScoreSaveResponse> call,
                Response<ScoreSaveResponse> response) {
                Log.d("APICall", "saveScore: " + response.code());
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
        HashtagSaveRequest request = new HashtagSaveRequest(story_id, content);
        Log.d("StoryRemoteDataSource", "saving hashtags: " + content);

        service.saveHashtags(bearer, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<HashtagSaveResponse> call,
                Response<HashtagSaveResponse> response) {
                Log.d("APICall", "saveHashtags: " + response.code());
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
