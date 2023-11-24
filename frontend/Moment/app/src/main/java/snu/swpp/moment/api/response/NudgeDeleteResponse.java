package snu.swpp.moment.api.response;

import com.google.gson.annotations.SerializedName;

public class NudgeDeleteResponse {

    public String getMessage() {
        return message;
    }

    @SerializedName("message")
    private String message;
}
