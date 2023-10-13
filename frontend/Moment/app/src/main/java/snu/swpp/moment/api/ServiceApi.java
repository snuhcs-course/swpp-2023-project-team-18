package snu.swpp.moment.api;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
public interface ServiceApi {
    @POST("/api/user/login")
    Call<LoginResponse> userLogin(@Body LoginRequest data);

}
