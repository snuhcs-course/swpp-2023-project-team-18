package snu.swpp.moment.api.request;

import com.google.gson.annotations.SerializedName;

public class HashtagSaveRequest {

    @SerializedName("story_id")
    private int story_id;
    @SerializedName("content")
    private String content;

    public HashtagSaveRequest(int story_id, String content) {
        this.story_id = story_id;
        this.content = content;
    }
}
