package isberg.udacity.spotifystreamer.api;

import isberg.udacity.spotifystreamer.model.SpotifyArtistSearchResponse;
import isberg.udacity.spotifystreamer.model.SpotifyTracksResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SpotifyService {
    @GET("v1/search?type=artist")
    Call<SpotifyArtistSearchResponse> searchArtists(@Header("Authorization") String authHeader, @Query("q") String query);

    @GET("v1/artists/{id}/top-tracks")
    Call<SpotifyTracksResponse> getArtistTopTracks(@Header("Authorization") String authHeader, @Path("id") String artistId, @Query("market") String countryCode);
}
