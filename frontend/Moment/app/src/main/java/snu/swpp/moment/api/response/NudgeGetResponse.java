package snu.swpp.moment.api.response;

import com.google.gson.annotations.SerializedName;

public class NudgeGetResponse {

    @SerializedName("nudge")
    private String nudge;

    public String getNudge() {
        return nudge;
    }
}
