package snu.swpp.moment.api.response;

import com.google.gson.annotations.SerializedName;

public class AIStoryGetResponse {

    @SerializedName("title")
    private String title;
    @SerializedName("story")
    private String story;

    public String getTitle() {
        return title;
    }

    public String getStory() {
        return story;
    }
}
