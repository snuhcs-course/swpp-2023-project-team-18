package snu.swpp.moment.data.source;

import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import snu.swpp.moment.api.RetrofitClient;
import snu.swpp.moment.api.ServiceApi;
import snu.swpp.moment.api.request.LoginRequest;
import snu.swpp.moment.api.request.RegisterRequest;
import snu.swpp.moment.api.request.TokenRefreshRequest;
import snu.swpp.moment.api.request.TokenVerifyRequest;
import snu.swpp.moment.api.response.LoginResponse;
import snu.swpp.moment.api.response.RegisterResponse;
import snu.swpp.moment.api.response.TokenRefreshResponse;
import snu.swpp.moment.api.response.TokenVerifyResponse;
import snu.swpp.moment.data.callback.AuthenticationCallBack;
import snu.swpp.moment.data.callback.RefreshCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.LoggedInUserModel;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class UserRemoteDataSource {

    private ServiceApi service;
    LoggedInUserModel loggedInUser = null;
    Integer error;

    public void login(String username, String password, AuthenticationCallBack loginCallBack) {
        service = RetrofitClient.getClient().create(ServiceApi.class);
        LoginRequest loginRequest = new LoginRequest(username, password);
        service.userLogin(loginRequest).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse result = response.body();
                    loginCallBack.onSuccess(
                        new LoggedInUserModel(result.getUser(), result.getToken()));
                } else {
                    String message = "";
                    try {
                        message = response.errorBody().string();
                    } catch (IOException e) {
                        message = "Server";
                    } finally {
                        loginCallBack.onFailure(message);
                    }

                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loginCallBack.onFailure("NO INTERNET");
            }
        });
    }

    public void register(String username, String password, String nickname,
        AuthenticationCallBack registerCallBack) {
        //System.out.println("#Debug from datasource || username : " + username + " password : " + password);
        //System.out.println("#Debug service create");
        service = RetrofitClient.getClient().create(ServiceApi.class);
        //System.out.println("#Debug LoginRequest");
        RegisterRequest request = new RegisterRequest(username, password, nickname);
        service.userRegister(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<RegisterResponse> call,
                Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    //System.out.println("#Debug Login OnResponse ");
                    RegisterResponse result = response.body();
                    registerCallBack.onSuccess(
                        new LoggedInUserModel(result.getUser(), result.getToken()));

                } else {
                    String message = "";
                    try {
                        message = response.errorBody().string();
                    } catch (IOException e) {
                        message = "Server";
                    } finally {
                        registerCallBack.onFailure(message);
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                registerCallBack.onFailure("NO INTERNET");
                //System.out.println("#Debug  :: If not connected ::  " + t.getMessage().toString() + " HERE???");
            }
        });

    }

    public void isTokenValid(String token, TokenCallBack callBack) {
        service = RetrofitClient.getClient().create(ServiceApi.class);
        //System.out.println("#Debug LoginRequest");
        TokenVerifyRequest request = new TokenVerifyRequest(token);
        service.tokenVerify(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<TokenVerifyResponse> call,
                Response<TokenVerifyResponse> response) {
                if (response.code() == 401) {
                    callBack.onFailure();
                } else {
                    System.out.println("#Debug Login OnResponse ");
                    callBack.onSuccess();
                }
            }

            @Override
            public void onFailure(Call<TokenVerifyResponse> call, Throwable t) {
                callBack.onFailure();
                System.out.println(
                    "#Debug  :: If not connected ::  " + t.getMessage() + " HERE???");
            }
        });
    }

    public void refresh(String token, RefreshCallBack callBack) {
        service = RetrofitClient.getClient().create(ServiceApi.class);
        TokenRefreshRequest request = new TokenRefreshRequest(token);
        service.tokenRefresh(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<TokenRefreshResponse> call,
                Response<TokenRefreshResponse> response) {
                if (response.code() == 401) {
                    callBack.onFailure();
                } else {
                    String access = response.body().getAccess();
                    callBack.onSuccess(access);
                }
            }

            @Override
            public void onFailure(Call<TokenRefreshResponse> call, Throwable t) {
            }
        });
    }
}