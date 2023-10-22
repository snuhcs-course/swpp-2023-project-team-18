package snu.swpp.moment.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import snu.swpp.moment.api.request.LoginRequest;
import snu.swpp.moment.api.request.MomentWriteRequest;
import snu.swpp.moment.api.request.RegisterRequest;
import snu.swpp.moment.api.request.TokenRefreshRequest;
import snu.swpp.moment.api.request.TokenVerifyRequest;
import snu.swpp.moment.api.response.LoginResponse;
import snu.swpp.moment.api.response.MomentGetResponse;
import snu.swpp.moment.api.response.MomentWriteResponse;
import snu.swpp.moment.api.response.RegisterResponse;
import snu.swpp.moment.api.response.TokenRefreshResponse;
import snu.swpp.moment.api.response.TokenVerifyResponse;

public interface ServiceApi {

    @POST("/api/user/login")
    Call<LoginResponse> userLogin(@Body LoginRequest data);

    @POST("/api/user/token/verify")
    Call<TokenVerifyResponse> tokenVerify(@Body TokenVerifyRequest data);

    @POST("api/user/token/refresh")
    Call<TokenRefreshResponse> tokenRefresh(@Body TokenRefreshRequest data);

    @POST("api/user/register")
    Call<RegisterResponse> userRegister(@Body RegisterRequest data);

    @GET("api/moments/")
    Call<MomentGetResponse> getMoments(@Header("Authorization") String bearerToken,
        @Query("start") long start, @Query("end") long end);

    @POST("api/moments/")
    Call<MomentWriteResponse> writeMoment(@Header("Authorization") String bearerToken,
        @Body MomentWriteRequest data);

}
