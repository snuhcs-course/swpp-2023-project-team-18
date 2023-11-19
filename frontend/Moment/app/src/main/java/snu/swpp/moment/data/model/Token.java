package snu.swpp.moment.data.model;

import snu.swpp.moment.api.LoginResponse;

public class Token {
    private String accessToken;
    private String refreshToken;

    public Token(String accessToken, String refreshToken){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken(){
        return this.accessToken;
    }
    public String getRefreshToken(){ return this.refreshToken; }
}