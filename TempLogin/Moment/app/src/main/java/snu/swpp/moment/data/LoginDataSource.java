package snu.swpp.moment.data;

        import retrofit2.Call;
        import retrofit2.Callback;
        import retrofit2.Response;
        import snu.swpp.moment.api.LoginRequest;
        import snu.swpp.moment.api.LoginResponse;
        import snu.swpp.moment.api.RetrofitClient;
        import snu.swpp.moment.api.ServiceApi;
        import snu.swpp.moment.data.model.LoggedInUser;
        import snu.swpp.moment.ui.login.LoginActivity;

        import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    private ServiceApi service;

    LoginResponse.User user;
    LoginResponse.Token token;
    //test
    private String username;
    private String nickname;
    private String createdAt;
    private String accessToken;
    private String refreshToken;


    public Result<LoggedInUser> login(String username, String password) {
        System.out.println("#Debug from datasource || username : " + username + " password : " + password);

        try {
            System.out.println("#Debug service create");
            service = RetrofitClient.getClient().create(ServiceApi.class);
            System.out.println("#Debug LoginRequest");
            LoginRequest loginRequest = new LoginRequest(username, password);
            service.userLogin(loginRequest).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    LoginResponse result = response.body();
                    LoginDataSource.this.username = result.getUser().getUsername();
                    LoginDataSource.this.nickname = result.getUser().getNickname();
                    LoginDataSource.this.createdAt= result.getUser().getCreatedAt();
                    LoginDataSource.this.accessToken = result.getToken().getAccessToken();
                    LoginDataSource.this.refreshToken = result.getToken().getRefreshToken();

                    System.out.println("#Debug  " + username + "  " + nickname + "  " + createdAt);

                    //이런식으로 추가해야함
                    return new Result.Success<LoggedInUser>(nickname, accessToken, refreshToken);
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    System.out.println("#Debug  :: If not connected ::  " + t.getMessage().toString() + " HERE???");
                }
            });


        } catch (IOException e) {
            System.out.println("#Debug IOException: " + e.getMessage());
            return new Result.Error(new IOException("Error logging in", e));
        } catch (Exception e) {
            System.out.println("#Debug General Error: " + e.getMessage());
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}

/*

            // Make a synchronous Retrofit call
            Response<LoginResponse> response = service.userLogin(loginRequest).execute();

            if (response.isSuccessful() && response.body() != null) {
                System.out.println("#Debug call onResponse");
                int resultCode = response.code();
                System.out.println(resultCode);

                LoginResponse result = response.body();
                user = result.getUser();
                token = result.getToken();

                // Check for null values before proceeding
                if (user != null && token != null) {
                    LoggedInUser loggedInUser = new LoggedInUser(user.getNickname(), token.getAccessToken(), token.getRefreshToken());
                    return new Result.Success<>(loggedInUser);
                } else {
                    System.out.println("#Debug Null user or token");
                    return new Result.Error(new IOException("Received null user or token"));
                }

            } else {
                System.out.println("#Debug Response not successful or body is null");
                return new Result.Error(new IOException("Unsuccessful response or null body"));
            }
*/
