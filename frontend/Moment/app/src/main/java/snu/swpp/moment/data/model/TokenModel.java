package snu.swpp.moment.data.model;

public class TokenModel {

    private final String accessToken;
    private final String refreshToken;

    public TokenModel(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }
}
