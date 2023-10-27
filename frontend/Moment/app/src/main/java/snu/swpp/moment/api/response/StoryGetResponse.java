package snu.swpp.moment.api.response;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import snu.swpp.moment.data.model.Story;

public class StoryGetResponse {

    @SerializedName("stories")
    private List<StoryResponse> storyList;

    private class StoryResponse {

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
    }

    private class Hashtag {

        @SerializedName("id")
        private int id;

        @SerializedName("content")
        private String content;

    }

    public ArrayList<Story> getStoryList() {
        ArrayList<Story> result = new ArrayList<>();
        for (StoryResponse storyResponse : storyList) {
            result.add(new Story(
                storyResponse.id,
                storyResponse.emotion,
                storyResponse.score,
                storyResponse.title,
                storyResponse.content,
                convertHashtags(storyResponse.hashtags),
                storyResponse.createdAt)
            );
        }
        return result;
    }

    private List<snu.swpp.moment.data.model.Hashtag> convertHashtags(List<Hashtag> hashtags) {
        List<snu.swpp.moment.data.model.Hashtag> result = new ArrayList<>();
        for (Hashtag hashtag : hashtags) {
            result.add(new snu.swpp.moment.data.model.Hashtag(hashtag.id, hashtag.content));
        }
        return result;
    }
}
