package snu.swpp.moment.data;

        import retrofit2.Call;
        import retrofit2.Callback;
        import retrofit2.Response;
        import snu.swpp.moment.api.LoginRequest;
        import snu.swpp.moment.api.LoginResponse;
        import snu.swpp.moment.api.RetrofitClient;
        import snu.swpp.moment.api.ServiceApi;
        import snu.swpp.moment.data.model.LoggedInUser;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class UserRemoteDataSource {
    private ServiceApi service;
    LoggedInUser loggedInUser = null;
    Integer error;

    // Access Token 확인하는 로직도 여기에

    public void login(String username, String password, AuthenticationCallBack loginCallBack) {
        //System.out.println("#Debug from datasource || username : " + username + " password : " + password);
        //System.out.println("#Debug service create");
        service = RetrofitClient.getClient().create(ServiceApi.class);
        //System.out.println("#Debug LoginRequest");
        LoginRequest loginRequest = new LoginRequest(username, password);
        service.userLogin(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if(response.code() == 400){
                    loginCallBack.onFailure(response.message());
                }
                else{
                    System.out.println("#Debug Login OnResponse ");
                    LoginResponse result = response.body();
                    loginCallBack.onSuccess(new LoggedInUser(result.getUser(), result.getToken()));
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loginCallBack.onFailure("NO INTERNET");
                System.out.println("#Debug  :: If not connected ::  " + t.getMessage().toString() + " HERE???");
            }
        });
        /*
        if (loggedInUser != null){
            System.out.println("#Debug  LOGIN SUCCESS");
            //.return new Result.Success<LoggedInUser>(loggedInUser);
        }
        else {
            System.out.println("#Debug  LOGIN FAILED");
            //return new Result.Error(new Exception("ddd"));
        }
        */
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