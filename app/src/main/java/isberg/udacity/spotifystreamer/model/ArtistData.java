package isberg.udacity.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Artist;

public class ArtistData implements Parcelable {

    private String artistId;
    private String artistName;
    private String artistCoverUrl;

    public ArtistData(String artistId, String artistName, String artistCoverUrl) {
        this.artistId = artistId;
        this.artistName = artistName;
        this.artistCoverUrl = artistCoverUrl;
    }

    private ArtistData(Parcel in) {
        artistId = in.readString();
        artistName = in.readString();
        artistCoverUrl = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeValue(artistId);
        out.writeValue(artistName);
        out.writeValue(artistCoverUrl);
    }

    public static final Parcelable.Creator<ArtistData> CREATOR = new Parcelable.Creator<ArtistData>() {
        public ArtistData createFromParcel(Parcel in) {
            return new ArtistData(in);
        }

        public ArtistData[] newArray(int size) {
            return new ArtistData[size];
        }
    };

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistCoverUrl() {
        return artistCoverUrl;
    }

    public void setArtistCoverUrl(String artistCoverUrl) {
        this.artistCoverUrl = artistCoverUrl;
    }

}
