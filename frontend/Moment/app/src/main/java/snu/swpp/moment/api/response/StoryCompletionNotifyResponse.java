package snu.swpp.moment.api.response;

import com.google.gson.annotations.SerializedName;

public class StoryCompletionNotifyResponse {

    @SerializedName("id")
    private int id;
    @SerializedName("message")
    private String message;

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
