package snu.swpp.moment.data;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import snu.swpp.moment.api.LoginRequest;
import snu.swpp.moment.api.LoginResponse;
import snu.swpp.moment.api.ServiceApi;
import snu.swpp.moment.data.model.LoggedInUser;
import snu.swpp.moment.ui.login.LoginActivity;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    private ServiceApi service;

    public Result<LoggedInUser> login(String username, String password) {
        try {
            // TODO: handle loggedInUser authentication
//            LoggedInUser fakeUser = new LoggedInUser( java.util.UUID.randomUUID().toString(), "Jane Doe");
//            return new Result.Success<>(fakeUser);

            LoginRequest loginRequest = new LoginRequest(username, password);

            service.userLogin(loginRequest).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    String username = response.body().getUser().getUsername();
                    LoggedInUser loggedInUser = new LoggedInUser(response.body().getUser().getUsername(),
                            response.body().getUser().getNickname());
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {

                }
            });


            /*
            LoggedInUser fakeUser =

                    new LoggedInUser(
                            java.util.UUID.randomUUID().toString(),
                            "Jane Doe");
            return new Result.Success<>(fakeUser);
             */


        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}