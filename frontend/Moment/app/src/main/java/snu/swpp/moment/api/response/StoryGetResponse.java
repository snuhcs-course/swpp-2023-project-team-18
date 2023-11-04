package snu.swpp.moment.api.response;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import snu.swpp.moment.data.model.HashtagModel;
import snu.swpp.moment.data.model.StoryModel;

public class StoryGetResponse {

    @SerializedName("stories")
    private List<StoryResponse> storyList;

    private static class StoryResponse {

        @SerializedName("id")
        private int id;

        @SerializedName("emotion")
        private String emotion;

        @SerializedName("score")
        private int score;

        @SerializedName("title")
        private String title;

        @SerializedName("content")
        private String content;

        @SerializedName("hashtags")
        private List<StoryGetResponse.Hashtag> hashtags;

        @SerializedName("created_at")
        private long createdAt;

        @SerializedName("is_point_completed")
        private boolean is_point_completed;
    }

    private static class Hashtag {

        @SerializedName("id")
        private int id;

        @SerializedName("content")
        private String content;

    }

    public ArrayList<StoryModel> getStoryList() {
        ArrayList<StoryModel> result = new ArrayList<>();
        for (StoryResponse storyResponse : storyList) {
            result.add(new StoryModel(
                storyResponse.id,
                storyResponse.emotion,
                storyResponse.score,
                storyResponse.title,
                storyResponse.content,
                convertHashtags(storyResponse.hashtags),
                storyResponse.createdAt,
                storyResponse.is_point_completed,
                false)
            );
        }
        return result;
    }

    private List<HashtagModel> convertHashtags(List<Hashtag> hashtags) {
        assert hashtags != null;
        List<snu.swpp.moment.data.model.HashtagModel> result = new ArrayList<>();
        for (Hashtag hashtag : hashtags) {
            result.add(new HashtagModel(hashtag.id, hashtag.content));
        }
        return result;
    }
}
