package isberg.udacity.spotifystreamer.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity; // AndroidX
import androidx.fragment.app.Fragment; // AndroidX
import androidx.fragment.app.FragmentTransaction; // AndroidX
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import isberg.udacity.spotifystreamer.MainActivity;
import isberg.udacity.spotifystreamer.R;
import isberg.udacity.spotifystreamer.api.SpotifyClient;
import isberg.udacity.spotifystreamer.model.SpotifyTracksResponse;
import isberg.udacity.spotifystreamer.model.TrackData;
import retrofit2.Call;
import retrofit2.Response;

public class TrackFragment extends Fragment {

    private ListView trackListView;
    private TrackAdapter trackAdapter;

    public static String ARTIST_NAME_KEY = "artistName";
    private final String TRACK_DATA_KEY = "trackData";

    public TrackFragment() {
    }

    @Override
    public void onCreate(Bundle bundle) {
        Log.d("TrackFragment", "onCreate");
        super.onCreate(bundle);
        setHasOptionsMenu(true);
        //Picasso.get().setIndicatorsEnabled(true);
        Picasso.get().setLoggingEnabled(true);

        ArrayList<TrackData> trackDataList = new ArrayList<TrackData>();

        setRetainInstance(true);
        if(bundle != null && bundle.containsKey(TRACK_DATA_KEY)) {
            trackDataList = bundle.getParcelableArrayList(TRACK_DATA_KEY);
            if (!trackDataList.isEmpty()) {
                Log.d("Track.onCreate", trackDataList.get(0).getName());
            }
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
            if (!trackDataList.isEmpty()) {
                Log.d("onViewStateRestored", trackDataList.get(0).getAlbumName());
            }
            trackAdapter.setTrackData(trackDataList);
        }

        super.onViewStateRestored(bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        if(trackAdapter != null && !trackAdapter.isEmpty()) {
            if (!trackAdapter.getTrackData().isEmpty()) {
                Log.d("Artist.onSaveInstState", trackAdapter.getTrackData().get(0).getName());
            }
            bundle.putParcelableArrayList(TRACK_DATA_KEY, trackAdapter.getTrackData());
        }
        super.onSaveInstanceState(bundle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //Toast.makeText(getActivity(), "Pressed back", Toast.LENGTH_SHORT).show();

            ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            if (actionBar != null && actionBar.getSubtitle() != null) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra(ARTIST_NAME_KEY, actionBar.getSubtitle());
                startActivity(intent);
            } else {
                 getActivity().onBackPressed();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //TODO:
    private String artistName;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("TrackFragment", "onCreateView");

        trackAdapter = new TrackAdapter(getActivity(), R.layout.list_item_track, new ArrayList<TrackData>());

        View rootView = inflater.inflate(R.layout.fragment_track, container, false);

        trackListView = (ListView) rootView.findViewById(R.id.listview_track);
        trackListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                TrackData trackData = (TrackData) adapter.getItemAtPosition(position);
                ArrayList<TrackData> trackDatas = trackAdapter.getTrackData();

                //Toast.makeText(getActivity(), "Pressed item " + trackData.getId() + "with pos " + position, Toast.LENGTH_SHORT).show();

                ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
                //actionBar.setTitle("player title"); //TODO something else what
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }

                PlayerFragment playerFragment = new PlayerFragment();
                Bundle bundle = new Bundle();

                bundle.putParcelableArrayList("trackData", trackDatas);
                bundle.putInt("currentIndex", position);
                bundle.putString("artistName", artistName);

                playerFragment.setArguments(bundle);
                FragmentTransaction playerFragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();

                // is tablet
               if ( getActivity().findViewById(R.id.track_detail_container) != null) {
                   Log.d("TrackFragment", "tablet");
                    playerFragment.setIsShownAsDialog(true);
                   // works with the one not appv4
                   // playerFragment.show(getActivity().getFragmentManager(), "playerfragment");
                   playerFragment.show(getActivity().getSupportFragmentManager(), "playerfragment");
               }

                else { // is phone
                   Log.d("TrackFragment", "phone");
                    playerFragment.setIsShownAsDialog(false);
                    playerFragmentTransaction.addToBackStack(null);

                    Fragment currentFragment = (Fragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.track_container);

                    //playerFragmentTransaction.remove(currentFragment);
                    //playerFragmentTransaction.add(R.id.player_container, playerFragment);

                    playerFragmentTransaction.replace(R.id.track_container, playerFragment);
                    playerFragmentTransaction.commit();
                }

            }
            });
        trackListView.setAdapter(trackAdapter);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            String artistId = bundle.getString("artistId");
            this.artistName = bundle.getString("artistName");

            getTracks(artistId);
        }
        return rootView;
    }

    private void getTracks(String artistId) {
        SpotifyClient.getInstance().fetchAccessToken(new SpotifyClient.TokenCallback() {
            @Override
            public void onSuccess(String token) {
                SpotifyClient.getInstance().getService().getArtistTopTracks("Bearer " + token, artistId, "SE").enqueue(new retrofit2.Callback<SpotifyTracksResponse>() {
                    @Override
                    public void onResponse(Call<SpotifyTracksResponse> call, Response<SpotifyTracksResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().tracks != null) {
                             final ArrayList<TrackData> tracks = new ArrayList<>();
                             for(SpotifyTracksResponse.SpotifyTrack track : response.body().tracks) {
                                  String albumCoverUrl = "";
                                  if(track.album != null && track.album.images != null && !track.album.images.isEmpty()) {
                                      albumCoverUrl = track.album.images.get(0).url;
                                  }
                                  tracks.add(new TrackData(track.id, track.name, track.album != null ? track.album.name : "", albumCoverUrl, track.preview_url, track.duration_ms));
                             }

                             if(getActivity() != null) {
                                 getActivity().runOnUiThread(() -> {
                                     trackAdapter.clear();
                                     trackAdapter.addAll(tracks);
                                     trackAdapter.notifyDataSetChanged();
                                 });
                             }
                        } else {
                            Log.e("TrackFragment", "Error getting tracks");
                        }
                    }

                    @Override
                    public void onFailure(Call<SpotifyTracksResponse> call, Throwable t) {
                         Log.e("TrackFragment", "Failure getting tracks", t);
                    }
                });
            }

            @Override
            public void onError() {
                 Log.e("TrackFragment", "Token error");
            }
        });
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

                if (trackData.getAlbumCoverUrl() != null && !trackData.getAlbumCoverUrl().isEmpty()) {

                    Picasso.get().cancelRequest(imName);
                    Picasso.get().load(trackData.getAlbumCoverUrl()).into(imName, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.d(LOG_TAG, "Piccasso error!" + e.getMessage());
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

