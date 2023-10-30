package snu.swpp.moment.data.repository;

import java.util.ArrayList;
import snu.swpp.moment.api.response.AIStoryGetResponse;
import snu.swpp.moment.api.response.StoryCompletionNotifyResponse;
import snu.swpp.moment.data.callback.AIStoryCallback;
import snu.swpp.moment.data.callback.EmotionSaveCallback;
import snu.swpp.moment.data.callback.HashtagSaveCallback;
import snu.swpp.moment.data.callback.ScoreSaveCallback;
import snu.swpp.moment.data.callback.StoryCompletionNotifyCallBack;
import snu.swpp.moment.data.callback.StoryGetCallBack;
import snu.swpp.moment.data.callback.StorySaveCallback;
import snu.swpp.moment.data.model.StoryModel;
import snu.swpp.moment.data.source.StoryRemoteDataSource;

public class StoryRepository {

    private final StoryRemoteDataSource remoteDataSource;

    public StoryRepository(StoryRemoteDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }

    public void getStory(String access_token, long start, long end, StoryGetCallBack callback) {
        remoteDataSource.getStory(access_token, start, end, new StoryGetCallBack() {
            @Override
            public void onSuccess(ArrayList<StoryModel> story) {
                callback.onSuccess(story);
            }

            @Override
            public void onFailure(Exception error) {
                callback.onFailure(error);
            }
        });
    }

    public void notifyCompletion(String access_token, long start, long end,
        StoryCompletionNotifyCallBack callback) {
        remoteDataSource.notifyCompletion(access_token, start, end,
            new StoryCompletionNotifyCallBack() {
                @Override
                public void onSuccess(StoryCompletionNotifyResponse response) {
                    callback.onSuccess(response);
                }

                @Override
                public void onFailure(Exception error) {
                    callback.onFailure(error);
                }
            });
    }

    public void getAIGeneratedStory(String access_token, AIStoryCallback callback) {
        remoteDataSource.getAIGeneratedStory(access_token, new AIStoryCallback() {
            @Override
            public void onSuccess(AIStoryGetResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public void onFailure(Exception error) {
                callback.onFailure(error);
            }
        });
    }

    public void saveStory(String access_token, String title, String content,
        StorySaveCallback callback) {
        remoteDataSource.saveStory(access_token, title, content, new StorySaveCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception error) {
                callback.onFailure(error);
            }
        });
    }

    public void saveEmotion(String access_token, String emotion, EmotionSaveCallback callback) {
        remoteDataSource.saveEmotion(access_token, emotion, new EmotionSaveCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception error) {
                callback.onFailure(error);
            }
        });
    }

    public void saveScore(String access_token, int story_id, int score,
        ScoreSaveCallback callback) {
        remoteDataSource.saveScore(access_token, story_id, score, new ScoreSaveCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception error) {
                callback.onFailure(error);
            }
        });
    }

    public void saveHashtags(String access_token, int story_id, String content,
        HashtagSaveCallback callback) {
        remoteDataSource.saveHashtags(access_token, story_id, content, new HashtagSaveCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception error) {
                callback.onFailure(error);
            }
        });
    }
}