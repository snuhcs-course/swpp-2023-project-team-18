package snu.swpp.moment.api;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private final static String BASE_URL = "http://ec2-54-180-98-145.ap-northeast-2.compute.amazonaws.com:3000";
    private static Retrofit retrofit = null;

    private RetrofitClient() {
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

            Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("http://localhost:3000/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create());

            retrofit = builder.baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}
