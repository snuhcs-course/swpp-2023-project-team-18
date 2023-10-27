package snu.swpp.moment.data.repository;

import java.util.ArrayList;
import snu.swpp.moment.data.callback.StoryGetCallBack;
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
}
