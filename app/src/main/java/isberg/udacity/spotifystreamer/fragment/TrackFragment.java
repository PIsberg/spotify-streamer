package isberg.udacity.spotifystreamer.fragment;


import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import isberg.udacity.spotifystreamer.MainActivity;
import isberg.udacity.spotifystreamer.activity.PlayerActivity;
import isberg.udacity.spotifystreamer.R;
import isberg.udacity.spotifystreamer.model.TrackData;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class TrackFragment extends Fragment {

    private ListView trackListView;
    private TrackAdapter trackAdapter;

    public static String ARTIST_NAME_KEY = "artistName";
    private final String TRACK_DATA_KEY = "trackData";

    public TrackFragment() {
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
        //Picasso.with(getActivity()).setIndicatorsEnabled(true);
        Picasso.with(getActivity()).setLoggingEnabled(true);

        ArrayList<TrackData> trackDataList = new ArrayList<TrackData>();

        setRetainInstance(true);
        if(bundle != null && bundle.containsKey(TRACK_DATA_KEY)) {
            trackDataList = bundle.getParcelableArrayList(TRACK_DATA_KEY);
            Log.d("Track.onCreate", trackDataList.get(0).getName());
        }

        trackAdapter = new TrackAdapter(getActivity(), R.layout.list_item_track, trackDataList);

        setTrackAdapter(trackAdapter);
    }

    public void setTrackAdapter(TrackAdapter trackAdapter) {
        this.trackAdapter = trackAdapter;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.track_menu, menu);
    }


    // Note: the following code in onViewStateRestored/onSaveInstanceState is for retain the current data/state e.q when offline + rotate device

    @Override
    public void onViewStateRestored(Bundle bundle) {
        ArrayList<TrackData> trackDataList = new ArrayList<TrackData>();

        if(bundle != null && bundle.containsKey(TRACK_DATA_KEY)) {
            trackDataList = bundle.getParcelableArrayList(TRACK_DATA_KEY);
            Log.d("onViewStateRestored", trackDataList.get(0).getAlbumName());
            trackAdapter.setTrackData(trackDataList);
        }

        super.onViewStateRestored(bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        if(trackAdapter != null && !trackAdapter.isEmpty()) {
            Log.d("Artist.onSaveInstState", trackAdapter.getTrackData().get(0).getName());
            bundle.putParcelableArrayList(TRACK_DATA_KEY, trackAdapter.getTrackData());
        }
        super.onSaveInstanceState(bundle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                //Toast.makeText(getActivity(), "Pressed back", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra(ARTIST_NAME_KEY, getActivity().getActionBar().getSubtitle());
                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //TODO:
    private String artistName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        trackAdapter = new TrackAdapter(getActivity(), R.layout.list_item_track, new ArrayList<TrackData>());

        View rootView = inflater.inflate(R.layout.fragment_track, container, false);

        trackListView = (ListView) rootView.findViewById(R.id.listview_track);
        trackListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                TrackData trackData = (TrackData) adapter.getItemAtPosition(position);
                Toast.makeText(getActivity(), "Pressed item " + trackData.getId(), Toast.LENGTH_SHORT).show();

                ActionBar actionBar = getActivity().getActionBar();
                actionBar.setTitle("player title"); //TODO something else what
                actionBar.setDisplayHomeAsUpEnabled(true);

                PlayerActivity playerFragment = new PlayerActivity();

                Bundle bundle = new Bundle();
                bundle.putString("trackPreviewURL", trackData.getPreviewUrl());
                bundle.putString("albumCoverUrl", trackData.getAlbumCoverUrl());
                bundle.putString("albumName", trackData.getAlbumName());
                bundle.putString("trackName", trackData.getName());
                bundle.putString("artistName", artistName);
                bundle.putLong("trackDurationMs", trackData.getDurationMs());

                Intent intentMain = new Intent(getActivity(), PlayerActivity.class);
                intentMain.putExtra("trackBundle", bundle);
                startActivity(intentMain);
            }
            });
        trackListView.setAdapter(trackAdapter);

        Bundle bundle = this.getArguments();
        String artistId = bundle.getString("artistId");
        this.artistName = bundle.getString("artistName");

        TrackSearchTask trackSearchTask = new TrackSearchTask(new TrackAdapterCallBack() {

            @Override
            public void onCallBack(TrackData[] result) {
                if (result != null) {
                    trackAdapter.clear();
                    for (TrackData trackData : result) {
                        trackAdapter.add(trackData);
                    }
                    trackAdapter.notifyDataSetChanged();
                }
            }
        });

        trackSearchTask.execute(artistId);
        return rootView;
    }


}
    class TrackAdapter extends ArrayAdapter<TrackData> {

        private final String LOG_TAG = TrackAdapter.class.getSimpleName();

        private ArrayList<TrackData> trackData;

        public TrackAdapter(Context context, int resource, ArrayList<TrackData> trackData) {
            super(context, resource, trackData);
            this.trackData = trackData;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TrackData trackData = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_track, parent, false);
            }

            ImageView imName = (ImageView) convertView.findViewById(R.id.list_item_albumcover_imageview);
            if (trackData != null) {

                if (!trackData.getAlbumCoverUrl().isEmpty()) {

                    Picasso.with(getContext()).cancelRequest(imName);
                    Picasso.with(getContext()).load(trackData.getAlbumCoverUrl()).into(imName, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Log.d(LOG_TAG, "Piccasso error!");
                        }
                    });
                }
            }

            TextView trackTextView = (TextView) convertView.findViewById(R.id.list_item_trackname_textview);
            if (trackData != null && trackData.getName() != null) {
                trackTextView.setText(trackData.getName());
            }

            TextView albumTextView = (TextView) convertView.findViewById(R.id.list_item_albumname_textview);
            if (trackData != null && trackData.getAlbumName() != null) {
                albumTextView.setText(trackData.getAlbumName());
            }

            return convertView;
        }

        public void setTrackData(ArrayList<TrackData> trackData) {
            this.trackData = trackData;
            notifyDataSetChanged();
        }

        public ArrayList<TrackData> getTrackData(){
            return trackData;
        }
    }

    class TrackSearchTask extends AsyncTask<String, Void, TrackData[]> {
        private final String LOG_TAG = TrackSearchTask.class.getSimpleName();

        private TrackAdapterCallBack callback;

        public TrackSearchTask(TrackAdapterCallBack callback) {
            this.callback = callback;
        }

        @Override
        protected TrackData[] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            //TODO: initate somwhere else *once*
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotifyService = api.getService();

            TrackData[] array = null;

            ArrayList<TrackData> tracks = new ArrayList<TrackData>();
            Map<String, Object> queryParams = new HashMap<String, Object>();
            queryParams.put("country", "SE");

            Tracks musicTracks = null;
            try {
                musicTracks = spotifyService.getArtistTopTrack(params[0], queryParams);
            } catch (RetrofitError error) {
                Log.d(LOG_TAG, "Out of internet and stuff:" + error.getMessage() );
                isCancelled();
            }
            if(musicTracks != null) {
                ListIterator<Track> musicTracksIter = musicTracks.tracks.listIterator();

                while (musicTracksIter.hasNext()) {
                    Track track = musicTracksIter.next();
                    String albumCoverUrl = "";

                    if(track.album.images != null && track.album.images.size() > 0) {
                        albumCoverUrl = track.album.images.get(0).url;
                    }
                    TrackData trackData = new TrackData(track.id, track.name, track.album.name, albumCoverUrl, track.preview_url,  track.duration_ms);


                    Log.d(LOG_TAG, "Track " + track.name);
                    Log.d(LOG_TAG, "previewUrl" + track.preview_url);
                    Log.d(LOG_TAG, "albumCoverUrl" + albumCoverUrl);

                    tracks.add(trackData);
                }

                if (tracks.size() > 0) {
                    array = tracks.toArray(new TrackData[tracks.size()]);
                }
            }
            return array;
        }

        @Override
        protected void onPostExecute(TrackData[] result) {
            callback.onCallBack(result);
        }

    }

    interface TrackAdapterCallBack {
        public void onCallBack(TrackData[] result);
    }
