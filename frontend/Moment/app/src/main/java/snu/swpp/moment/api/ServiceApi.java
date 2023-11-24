package snu.swpp.moment.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import snu.swpp.moment.api.request.EmotionSaveRequest;
import snu.swpp.moment.api.request.HashtagSaveRequest;
import snu.swpp.moment.api.request.LoginRequest;
import snu.swpp.moment.api.request.MomentWriteRequest;
import snu.swpp.moment.api.request.NicknameUpdateRequest;
import snu.swpp.moment.api.request.RegisterRequest;
import snu.swpp.moment.api.request.ScoreSaveRequest;
import snu.swpp.moment.api.request.StoryCompletionNotifyRequest;
import snu.swpp.moment.api.request.StorySaveRequest;
import snu.swpp.moment.api.request.TokenRefreshRequest;
import snu.swpp.moment.api.request.TokenVerifyRequest;
import snu.swpp.moment.api.response.AIStoryGetResponse;
import snu.swpp.moment.api.response.EmotionSaveResponse;
import snu.swpp.moment.api.response.HashtagSaveResponse;
import snu.swpp.moment.api.response.LoginResponse;
import snu.swpp.moment.api.response.MomentGetResponse;
import snu.swpp.moment.api.response.MomentWriteResponse;
import snu.swpp.moment.api.response.NicknameUpdateResponse;
import snu.swpp.moment.api.response.RegisterResponse;
import snu.swpp.moment.api.response.ScoreSaveResponse;
import snu.swpp.moment.api.response.SearchContentsResponse;
import snu.swpp.moment.api.response.SearchHashTagGetCompleteResponse;
import snu.swpp.moment.api.response.SearchHashtagsResponse;
import snu.swpp.moment.api.response.StoryCompletionNotifyResponse;
import snu.swpp.moment.api.response.StoryGetResponse;
import snu.swpp.moment.api.response.StorySaveResponse;
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

    @PUT("/api/user/info")
    Call<NicknameUpdateResponse> updateNickname(@Header("Authorization") String bearerToken,
        @Body NicknameUpdateRequest data);

    @GET("api/writing/moments/")
    Call<MomentGetResponse> getMoments(@Header("Authorization") String bearerToken,
        @Query("start") long start, @Query("end") long end);

    @POST("api/writing/moments/")
    Call<MomentWriteResponse> writeMoment(@Header("Authorization") String bearerToken,
        @Body MomentWriteRequest data);

    @GET("api/writing/stories/")
    Call<StoryGetResponse> getStories(@Header("Authorization") String bearerToken,
        @Query("start") long start, @Query("end") long end);

    @POST("api/writing/day-completion/")
    Call<StoryCompletionNotifyResponse> notifyStoryCompletion(
        @Header("Authorization") String bearerToken,
        @Body StoryCompletionNotifyRequest data);

    @GET("api/writing/ai-story/")
    Call<AIStoryGetResponse> getAIGeneratedStory(@Header("Authorization") String bearerToken);

    @POST("api/writing/stories/")
    Call<StorySaveResponse> saveStory(@Header("Authorization") String bearerToken,
        @Body StorySaveRequest data);

    @POST("api/writing/emotions/")
    Call<EmotionSaveResponse> saveEmotion(@Header("Authorization") String bearerToken,
        @Body EmotionSaveRequest data);

    @POST("api/writing/score/")
    Call<ScoreSaveResponse> saveScore(@Header("Authorization") String bearerToken,
        @Body ScoreSaveRequest data);

    @POST("api/writing/hashtags/")
    Call<HashtagSaveResponse> saveHashtags(@Header("Authorization") String bearerToken,
        @Body HashtagSaveRequest data);

    @GET("/api/writing/hashtags/complete/")
    Call<SearchHashTagGetCompleteResponse> getCompleteHashTags(
        @Header("Authorization") String bearerToken,
        @Query("tag_query") String str);

    @GET("/api/writing/search/contents/")
    Call<SearchContentsResponse> getContentSearchList(@Header("Authorization") String bearerToken,
        @Query("query") String query);

    @GET("/api/writing/search/hashtags/")
    Call<SearchHashtagsResponse> getHashtagSearchList(@Header("Authorization") String bearerToken,
        @Query("query") String query);
}
