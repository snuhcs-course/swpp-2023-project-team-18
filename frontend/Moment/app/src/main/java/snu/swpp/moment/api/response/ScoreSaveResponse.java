package snu.swpp.moment.api.response;

import com.google.gson.annotations.SerializedName;

public class ScoreSaveResponse {

    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }
}
