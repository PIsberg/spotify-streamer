package isberg.udacity.spotifystreamer.api;

import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public class SpotifyClient {
    private static final String BASE_URL = "https://api.spotify.com/";
    private static final String AUTH_URL = "https://accounts.spotify.com/";
    private static SpotifyClient instance;
    private SpotifyService service;
    private String accessToken;
    private long tokenExpiryTime;
    
    // Credentials loaded from .env.local via BuildConfig
    private static final String CLIENT_ID = isberg.udacity.spotifystreamer.BuildConfig.SPOTIFY_CLIENT_ID;
    private static final String CLIENT_SECRET = isberg.udacity.spotifystreamer.BuildConfig.SPOTIFY_CLIENT_SECRET;

    private SpotifyClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        service = retrofit.create(SpotifyService.class);
    }

    public static synchronized SpotifyClient getInstance() {
        if (instance == null) {
            instance = new SpotifyClient();
        }
        return instance;
    }

    public SpotifyService getService() {
        return service;
    }

    public String getAccessToken() {
        // Simple logic: if token is null or expired, we should fetch a new one.
        // However, this is synchronous. In a real app we'd handle this async.
        // For this migration, the callers (Fragments) will use getAuthorizationHeader() which *should*
        // trigger a refresh if needed, but since we can't easily do async refresh inside a simple getter
        // without blocking, we might assume the app initializes the token or we do it lazily in background.
        
        // For simplicity in this older app structure, we will expose a method to ensure token exists.
        return accessToken;
    }
    
    public String getAuthorizationHeader() {
         return "Bearer " + accessToken;
    }

    public void fetchAccessToken(final TokenCallback callback) {
        // Auth Service
        Retrofit authRetrofit = new Retrofit.Builder()
                .baseUrl(AUTH_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        SpotifyAuthService authService = authRetrofit.create(SpotifyAuthService.class);
        String authString = CLIENT_ID + ":" + CLIENT_SECRET;
        String authHeader = "Basic " + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);

        authService.getToken(authHeader, "client_credentials").enqueue(new retrofit2.Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, retrofit2.Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    accessToken = response.body().access_token;
                    // timestamp + expires_in * 1000
                    tokenExpiryTime = System.currentTimeMillis() + (response.body().expires_in * 1000); 
                    if (callback != null) callback.onSuccess(accessToken);
                } else {
                    Log.e("SpotifyClient", "Auth failed: " + response.code());
                    if (callback != null) callback.onError();
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Log.e("SpotifyClient", "Auth error", t);
                if (callback != null) callback.onError();
            }
        });
    }

    public interface TokenCallback {
        void onSuccess(String token);
        void onError();
    }

    private interface SpotifyAuthService {
        @FormUrlEncoded
        @POST("api/token")
        Call<TokenResponse> getToken(@Header("Authorization") String auth, @Field("grant_type") String grantType);
    }

    private static class TokenResponse {
        String access_token;
        String token_type;
        long expires_in;
    }
}
