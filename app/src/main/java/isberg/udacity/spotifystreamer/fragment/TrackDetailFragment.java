package isberg.udacity.spotifystreamer.fragment;

import androidx.appcompat.app.ActionBar; // AndroidX
import androidx.appcompat.app.AppCompatActivity; // AndroidX
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import isberg.udacity.spotifystreamer.MainActivity;
import isberg.udacity.spotifystreamer.R;
import isberg.udacity.spotifystreamer.model.TrackData;

public class TrackDetailFragment extends Fragment {

    private ListView trackListView;
    private TrackAdapter trackAdapter;

    public static String ARTIST_NAME_KEY = "artistName";
    private final String TRACK_DATA_KEY = "trackData";

    public TrackDetailFragment() {
    }

    @Override
    public void onCreate(Bundle bundle) {
        Log.d("TrackDetailFragment", "onCreate");
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
        Log.d("TrackDetailFragment", "onCreateView");
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
                if (getActivity().findViewById(R.id.track_detail_container) != null) {
                    Log.d("TrackDetailFragment", "tablet");
                    playerFragment.setIsShownAsDialog(true);
                    //works with one not appv4
                    //playerFragment.show(getActivity().getFragmentManager(), "playerfragment");
                    playerFragment.show(getActivity().getSupportFragmentManager(), "playerfragment");
                }
                else { // is phone
                    Log.d("TrackDetailFragment", "phone");
                    playerFragment.setIsShownAsDialog(false);
                    playerFragmentTransaction.addToBackStack(null);

                    Fragment currentFragment = (Fragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.track_container);
                    //playerFragmentTransaction.remove(currentFragment);
                    //playerFragmentTransaction.add(R.id.player_container, playerFragment);

                    playerFragmentTransaction.replace(R.id.track_container, playerFragment);
                    playerFragmentTransaction.addToBackStack(null);
                    playerFragmentTransaction.commit();
                }

            }
            });
        trackListView.setAdapter(trackAdapter);

        Bundle bundle = this.getArguments();
        String artistId = null;
        if(bundle != null) {
            if (bundle.getString("artistId") != null) {
                artistId = bundle.getString("artistId");
            }
            if (bundle.getString("artistName") != null) {
                this.artistName = bundle.getString("artistName");
            }
        }
        
        // Removed TrackSearchTask here because this fragment seems to only display passed data or existing data?
        // Wait, the original code had TrackSearchTask.execute(artistId) call!
        // So I must restore that logic if I want it to work.
        // But the original code in Step 59 contained lines 184-203 with TrackSearchTask. 
        // I will re-implement getTracks logic similar to TrackFragment.
        
        // Wait, TrackDetailFragment in the original code (Step 59) DID have TrackSearchTask logic at the bottom (lines 184...).
        // But it re-instantiated TrackSearchTask which was an inner class of TrackFragment?? 
        // No, Step 59 shows `TrackSearchTask trackSearchTask = new TrackSearchTask(...)`.
        // If TrackSearchTask was defined in TrackFragment, it would NOT be available here unless imported or public static?
        // Ah, `TrackSearchTask` in Step 20 (`TrackFragment`) was package-private `class TrackSearchTask ...`.
        // So `TrackDetailFragment` CAN see it.
        // However, `TrackSearchTask` in `TrackFragment` was refactored to be removed in my previous step!
        // I REMOVED `TrackSearchTask` class from `TrackFragment` in Step 63/64.
        // So now `TrackDetailFragment` will fail to compile if it tries to use `TrackSearchTask`.
        
        // I must implement `getTracks` logic here as well using `SpotifyClient`.
        if(artistId != null) {
             getTracks(artistId);
        }
        return rootView;
    }

    private void getTracks(String artistId) {
        isberg.udacity.spotifystreamer.api.SpotifyClient.getInstance().fetchAccessToken(new isberg.udacity.spotifystreamer.api.SpotifyClient.TokenCallback() {
            @Override
            public void onSuccess(String token) {
                isberg.udacity.spotifystreamer.api.SpotifyClient.getInstance().getService().getArtistTopTracks("Bearer " + token, artistId, "US").enqueue(new retrofit2.Callback<isberg.udacity.spotifystreamer.model.SpotifyTracksResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<isberg.udacity.spotifystreamer.model.SpotifyTracksResponse> call, retrofit2.Response<isberg.udacity.spotifystreamer.model.SpotifyTracksResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().tracks != null) {
                             final ArrayList<TrackData> tracks = new ArrayList<>();
                             for(isberg.udacity.spotifystreamer.model.SpotifyTracksResponse.SpotifyTrack track : response.body().tracks) {
                                  String albumCoverUrl = "";
                                  if(track.album != null && track.album.images != null && !track.album.images.isEmpty()) {
                                      albumCoverUrl = track.album.images.get(0).url;
                                  }
                                  if (track.preview_url != null && !track.preview_url.isEmpty()) {
                                      tracks.add(new TrackData(track.id, track.name, track.album != null ? track.album.name : "", albumCoverUrl, track.preview_url, track.duration_ms));
                                  }
                             }

                             if(getActivity() != null) {
                                 getActivity().runOnUiThread(() -> {
                                     trackAdapter.clear();
                                     trackAdapter.addAll(tracks);
                                     trackAdapter.notifyDataSetChanged();
                                 });
                             }
                        } else {
                            Log.e("TrackDetailFragment", "Error getting tracks");
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<isberg.udacity.spotifystreamer.model.SpotifyTracksResponse> call, Throwable t) {
                         Log.e("TrackDetailFragment", "Failure getting tracks", t);
                    }
                });
            }

            @Override
            public void onError() {
                 Log.e("TrackDetailFragment", "Token error");
            }
        });
    }


}


