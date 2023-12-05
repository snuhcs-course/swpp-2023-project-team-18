package snu.swpp.moment.api.request;

import com.google.gson.annotations.SerializedName;

public class ScoreSaveRequest {

    @SerializedName("story_id")
    private final int story_id;
    @SerializedName("score")
    private final int score;

    public ScoreSaveRequest(int story_id, int score) {
        this.story_id = story_id;
        this.score = score;
    }
}
