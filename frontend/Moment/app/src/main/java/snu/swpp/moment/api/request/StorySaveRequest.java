package snu.swpp.moment.api.request;

import com.google.gson.annotations.SerializedName;

public class StorySaveRequest {
    @SerializedName("title")
    private final String title;
    @SerializedName("content")
    private final String content;

    public StorySaveRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
