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

        @SerializedName("created_at")
        private long created_at;

        public int getId() {
            return id;
        }

        public String getEmotion() {
            return emotion;
        }

        public int getScore() {
            return score;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public long getCreated_at() {
            return created_at;
        }
    }

    public ArrayList<Story> getStoryList() {
        ArrayList<Story> result = new ArrayList<>();
        for (StoryResponse storyResponse: storyList) {
            result.add(new Story(
                storyResponse.id,
                storyResponse.emotion,
                storyResponse.score,
                storyResponse.title,
                storyResponse.content,
                storyResponse.created_at)
            );
        }
        return result;
    }
}
