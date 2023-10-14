package snu.swpp.moment.data.model;

import snu.swpp.moment.api.LoginResponse;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String username = null;
    private String nickName = null;
    private String createAt = null;
    private String AccessToken =null;
    private String RefreshToken=null;
    public LoggedInUser(LoginResponse.User user, LoginResponse.Token token){
        this.username = user.getUsername();
        this.nickName = user.getNickname();
        this.createAt = user.getCreatedAt();
        this.AccessToken = token.getAccessToken();
        this.RefreshToken = token.getRefreshToken();
    }
    public LoggedInUser(String username, String nickName, String createAt, String AccessToken, String RefreshToken) {
        this.username = username;
        this.nickName = nickName;
        this.createAt = createAt;
        this.AccessToken = AccessToken;
        this.RefreshToken = RefreshToken;
    }

    public String getUsername(){ return username; }
    public String getNickName(){ return nickName; }
    public String getCreateAt(){ return createAt;}
    public String getAccessToken(){return AccessToken;}
    public String getRefreshToken(){return RefreshToken;}

}