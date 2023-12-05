package snu.swpp.moment.api.request;

import com.google.gson.annotations.SerializedName;

public class StoryCompletionNotifyRequest {

    @SerializedName("start")
    private final long start;

    @SerializedName("end")
    private final long end;

    public StoryCompletionNotifyRequest(long start, long end) {
        this.start = start;
        this.end = end;
    }
}
