package isberg.udacity.spotifystreamer.model;


import android.os.Parcel;
import android.os.Parcelable;

public class TrackData implements Parcelable {

    private String id;
    private String name;
    private String albumName;
    private String albumCoverUrl;
    private String previewUrl;
    private long durationMs;

    public TrackData(String id, String name, String albumName, String albumCoverUrl, String previewUrl, long durationMs) {
        this.id = id;
        this.name = name;
        this.albumName = albumName;
        this.albumCoverUrl = albumCoverUrl;
        this.previewUrl = previewUrl;
        this.durationMs = durationMs;
    }

    private TrackData(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.albumName = in.readString();
        this.albumCoverUrl = in.readString();
        this.previewUrl = in.readString();
        this.durationMs = in.readLong();
    }

    public void readFromParcel(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.albumName = in.readString();
        this.albumCoverUrl = in.readString();
        this.previewUrl = in.readString();
        this.durationMs = in.readLong();
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

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public int describeContents() {
        return 0;
    }


    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(name);
        out.writeString(albumName);
        out.writeString(albumCoverUrl);
        out.writeString(previewUrl);
        out.writeLong(durationMs);
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
