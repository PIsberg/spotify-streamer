package isberg.udacity.spotifystreamer.model;


import android.os.Parcel;
import android.os.Parcelable;

public class TrackData implements Parcelable {

    private String id;
    private String name;
    private String albumName;
    private String albumCoverUrl;

    public TrackData(String id, String name, String albumName, String albumCoverUrl) {
        this.id = id;
        this.name = name;
        this.albumName = albumName;
        this.albumCoverUrl = albumCoverUrl;
    }

    private TrackData(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.albumName = in.readString();
        this.albumCoverUrl = in.readString();
    }

    public String getAlbumCoverUrl() {
        return albumCoverUrl;
    }

    public void setAlbumCoverUrl(String albumCoverUrl) {
        this.albumCoverUrl = albumCoverUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public int describeContents() {
        return 0;
    }


    public void writeToParcel(Parcel out, int flags) {
        out.writeValue(id);
        out.writeValue(name);
        out.writeValue(albumName);
        out.writeValue(albumCoverUrl);
    }

    public static final Parcelable.Creator<TrackData> CREATOR = new Parcelable.Creator<TrackData>() {
        public TrackData createFromParcel(Parcel in) {
            return new TrackData(in);
        }

        public TrackData[] newArray(int size) {
            return new TrackData[size];
        }
    };

}
