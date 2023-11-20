package snu.swpp.moment.data.source;

import android.util.Log;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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
public class UserRemoteDataSource extends BaseRemoteDataSource {

    public void login(String username, String password, AuthenticationCallBack loginCallBack) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        service.userLogin(loginRequest).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Log.d("APICall", "login: " + response.code());
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
        RegisterRequest request = new RegisterRequest(username, password, nickname);
        service.userRegister(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<RegisterResponse> call,
                Response<RegisterResponse> response) {
                Log.d("APICall", "register: " + response.code());
                if (response.isSuccessful()) {
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
        TokenVerifyRequest request = new TokenVerifyRequest(token);
        service.tokenVerify(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<TokenVerifyResponse> call,
                Response<TokenVerifyResponse> response) {
                Log.d("APICall", "isTokenValid: " + response.code());
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
        TokenRefreshRequest request = new TokenRefreshRequest(token);
        service.tokenRefresh(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<TokenRefreshResponse> call,
                Response<TokenRefreshResponse> response) {
                Log.d("APICall", "refresh: " + response.code());
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
