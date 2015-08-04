package isberg.udacity.spotifystreamer.fragment;


import android.support.v4.app.Fragment;



import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import isberg.udacity.spotifystreamer.R;
import isberg.udacity.spotifystreamer.model.TrackData;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class TrackDetailFragment extends Fragment {

    private ListView trackListView;
    private TrackAdapter trackAdapter;

    public static String ARTIST_NAME_KEY = "artistName";
    private final String TRACK_DATA_KEY = "trackData";

    public TrackDetailFragment() {
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
                ArrayList<TrackData> trackDatas = trackAdapter.getTrackData();

                Toast.makeText(getActivity(), "Pressed item " + trackData.getId() + "with pos " + position, Toast.LENGTH_SHORT).show();

                ActionBar actionBar = getActivity().getActionBar();
                //actionBar.setTitle("player title"); //TODO something else what
                actionBar.setDisplayHomeAsUpEnabled(true);

                PlayerFragment playerFragment = new PlayerFragment();
                Bundle bundle = new Bundle();

                bundle.putParcelableArrayList("trackData", trackDatas);
                bundle.putInt("currentIndex", position);
                bundle.putString("artistName", artistName);

                playerFragment.setArguments(bundle);
                FragmentTransaction playerFragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();

                // is tablet
                if ( getActivity().findViewById(R.id.track_detail_container) != null) {

                    playerFragment.setIsShownAsDialog(true);
                    playerFragment.show(getActivity().getFragmentManager(), "playerfragment");
                }

                else { // is phone
                    playerFragment.setIsShownAsDialog(false);
                    playerFragmentTransaction.addToBackStack(null);

                    Fragment currentFragment = (Fragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.track_container);
                    //playerFragmentTransaction.remove(currentFragment);
                    //playerFragmentTransaction.add(R.id.player_container, playerFragment);
                    playerFragmentTransaction.replace(R.id.track_container, currentFragment);
                    playerFragmentTransaction.addToBackStack(null);
                    playerFragmentTransaction.commit();
                }

            }
        });
        trackListView.setAdapter(trackAdapter);

        Bundle bundle = this.getArguments();
        String artistId = bundle.getString("artistId");
        this.artistName = bundle.getString("artistName");

        TrackSearchTask trackSearchTask = new TrackSearchTask(new TrackAdapterCallBack() {

            @Override
            public void onCallBack(ArrayList<TrackData> result) {
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


