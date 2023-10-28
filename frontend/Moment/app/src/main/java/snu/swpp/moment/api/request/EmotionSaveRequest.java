package snu.swpp.moment.api.request;

import com.google.gson.annotations.SerializedName;

public class EmotionSaveRequest {
    @SerializedName("emotion")
    private final String emotion;

    public EmotionSaveRequest(String emotion) {
        this.emotion = emotion;
    }
}
