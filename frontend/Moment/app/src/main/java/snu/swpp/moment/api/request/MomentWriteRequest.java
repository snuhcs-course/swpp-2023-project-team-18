package snu.swpp.moment.api.request;

import com.google.gson.annotations.SerializedName;

public class MomentWriteRequest {

    @SerializedName("moment")
    private final String moment;

    public MomentWriteRequest(String moment) {
        this.moment = moment;
    }
}
