package isberg.udacity.spotifystreamer.model;

import java.util.List;

public class SpotifyArtistSearchResponse {
    public ArtistsModel artists;

    public static class ArtistsModel {
        public List<SpotifyArtist> items;
    }

    public static class SpotifyArtist {
        public String id;
        public String name;
        public List<SpotifyImage> images;
    }

    public static class SpotifyImage {
        public String url;
        public int height;
        public int width;
    }
}
