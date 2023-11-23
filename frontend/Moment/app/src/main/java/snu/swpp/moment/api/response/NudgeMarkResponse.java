package snu.swpp.moment.api.response;

import com.google.gson.annotations.SerializedName;

public class NudgeMarkResponse {

    public String getMessage() {
        return message;
    }

    @SerializedName("message")
    private String message;
}
