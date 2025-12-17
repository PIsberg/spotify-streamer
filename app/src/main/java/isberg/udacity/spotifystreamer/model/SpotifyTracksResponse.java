package isberg.udacity.spotifystreamer.model;

import java.util.List;

public class SpotifyTracksResponse {
    public List<SpotifyTrack> tracks;

    public static class SpotifyTrack {
        public String id;
        public String name;
        public String preview_url;
        public long duration_ms;
        public SpotifyAlbum album;
    }

    public static class SpotifyAlbum {
        public String name;
        public List<SpotifyArtistSearchResponse.SpotifyImage> images;
    }
}
