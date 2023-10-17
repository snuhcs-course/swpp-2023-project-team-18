package snu.swpp.moment.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ServiceApi {

    @POST("/api/user/login")
    Call<LoginResponse> userLogin(@Body LoginRequest data);

    @POST("/api/user/token/verify")
    Call<TokenVerifyResponse> tokenVerify(@Body TokenVerifyRequest data);

    @POST("api/user/token/refresh")
    Call<TokenRefreshResponse> tokenRefresh(@Body TokenRefreshRequest data);

    @POST("api/user/register")
    Call<RegisterResponse> userRegister(@Body RegisterRequest data);
}
