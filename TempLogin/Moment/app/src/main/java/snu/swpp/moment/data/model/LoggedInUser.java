package snu.swpp.moment.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String username;
    private String nickName;
    private String createAt;
    private String AccessToken;
    private String RefreshToken;

    public LoggedInUser(String nickName, String AccessToken, String RefreshToken) {
        this.nickName = nickName;
        this.AccessToken = AccessToken;
        this.RefreshToken = RefreshToken;
    }


    public String getNickName(){ return nickName; }
    public String getCreateAt(){ return createAt;}
    public String getAccessToken(){return AccessToken;}
    public String getRefreshToken(){return RefreshToken;}

}