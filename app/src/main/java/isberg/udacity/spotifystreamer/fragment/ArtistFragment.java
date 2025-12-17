package isberg.udacity.spotifystreamer.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment; // AndroidX
import androidx.fragment.app.FragmentTransaction; // AndroidX
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import isberg.udacity.spotifystreamer.R;
import isberg.udacity.spotifystreamer.api.SpotifyClient;
import isberg.udacity.spotifystreamer.model.ArtistData;
import isberg.udacity.spotifystreamer.model.SpotifyArtistSearchResponse;
import retrofit2.Call;
import retrofit2.Response;

public class ArtistFragment extends Fragment {
    private ListView listView;
    private EditText searchArtistEditText;

    private final String ARTIST_DATA_KEY = "artistData";
    private final String LOG_TAG = ArtistFragment.class.getSimpleName();

    private ArtistAdapter artistAdapter;

    public ArtistFragment() {
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);

        //Picasso.get().setIndicatorsEnabled(true);
        Picasso.get().setLoggingEnabled(true);

        ArrayList<ArtistData> artistDataList = new ArrayList<ArtistData>();

        setRetainInstance(true);

        artistAdapter = new ArtistAdapter(
                getActivity(),
                R.layout.list_item_artist,
                R.id.list_item_artist_textview,
                artistDataList);

    }

    public void setArtistAdapter(ArtistAdapter artistAdapter) {
        this.artistAdapter = artistAdapter;
    }

    // Note: the following code in onViewStateRestored/onSaveInstanceState is for retain the current data/state e.q when offline + rotate device

    @Override
    public void onViewStateRestored(Bundle bundle) {
        ArrayList<ArtistData> artistDataList = new ArrayList<ArtistData>();

        if(bundle != null && bundle.containsKey(ARTIST_DATA_KEY)) {
            artistDataList = bundle.getParcelableArrayList(ARTIST_DATA_KEY);
            if (!artistDataList.isEmpty()) {
                Log.d("onViewStateRestored", artistDataList.get(0).getArtistName());
            }
            artistAdapter.setArtistData(artistDataList);
        }

        super.onViewStateRestored(bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {

        if(artistAdapter != null && !artistAdapter.isEmpty()) {
            if (!artistAdapter.getArtistData().isEmpty()) {
                Log.d("onSaveInstanceState", artistAdapter.getArtistData().get(0).getArtistName());
            }
            bundle.putParcelableArrayList(ARTIST_DATA_KEY, artistAdapter.getArtistData());
        }
        super.onSaveInstanceState(bundle);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        artistAdapter = new ArtistAdapter(
                getActivity(),
                R.layout.list_item_artist,
                R.id.list_item_artist_textview,
                new ArrayList<ArtistData>());


        View rootView = inflater.inflate(R.layout.fragment_artist, container, false);

        listView = (ListView) rootView.findViewById(R.id.listview_artist);
        listView.setAdapter(artistAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArtistData itemContent = artistAdapter.getItem(position);

                // is tablet
                if (getActivity().findViewById(R.id.track_detail_container) != null) {
                    Log.d("ArtistFragment", "tablet");

                    TrackDetailFragment trackDetailFragment = new TrackDetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("artistId", itemContent.getArtistId());
                    bundle.putString("artistName", itemContent.getArtistName());
                    trackDetailFragment.setArguments(bundle);

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.track_detail_container, trackDetailFragment, "trackdetailfragment")
                            .commit();

                }
                else { //is phone
                    Log.d("ArtistFragment", "phone");
                    androidx.appcompat.app.ActionBar actionBar = ((androidx.appcompat.app.AppCompatActivity)getActivity()).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setTitle(R.string.track_fragment_title);
                        actionBar.setSubtitle(itemContent.getArtistName());
                        actionBar.setDisplayHomeAsUpEnabled(true);
                    }

                    TrackFragment trackFragment = new TrackFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("artistId", itemContent.getArtistId());
                    bundle.putString("artistName", itemContent.getArtistName());
                    trackFragment.setArguments(bundle);

                    FragmentTransaction trackFragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                    trackFragmentTransaction.addToBackStack(null);

                    Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.artist_container);
                    if (currentFragment != null) {
                        trackFragmentTransaction.remove(currentFragment);
                    }
                    trackFragmentTransaction.add(R.id.track_container, trackFragment);
                    trackFragmentTransaction.commit();
                }
            }
        });

        searchArtistEditText = (EditText) rootView.findViewById(R.id.textview_artist);

        if(getArguments() != null && getArguments().size() > 0) {
            String prevValue = getArguments().getString(TrackFragment.ARTIST_NAME_KEY);
            if (prevValue != null) {
                searchArtistEditText.setText(prevValue);
            }
        }

        SearchArtistTextWatcher searchArtistTextWatcher = new SearchArtistTextWatcher();
        searchArtistTextWatcher.setSearchArtistTextWatcher(this);
        searchArtistEditText.addTextChangedListener(searchArtistTextWatcher);

        return rootView;
    }

    public void searchArtists(String query) {
        SpotifyClient.getInstance().fetchAccessToken(new SpotifyClient.TokenCallback() {
            @Override
            public void onSuccess(String token) {
                SpotifyClient.getInstance().getService().searchArtists("Bearer " + token, query).enqueue(new retrofit2.Callback<SpotifyArtistSearchResponse>() {
                    @Override
                    public void onResponse(Call<SpotifyArtistSearchResponse> call, Response<SpotifyArtistSearchResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().artists != null) {
                            List<SpotifyArtistSearchResponse.SpotifyArtist> items = response.body().artists.items;

                            if (items == null || items.isEmpty()) {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        artistAdapter.clear();
                                        Toast.makeText(getActivity(), R.string.artist_noresult_toast, Toast.LENGTH_SHORT).show();
                                    });
                                }
                                return;
                            }

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> 
                                    Toast.makeText(getActivity(), "Checking for playable tracks...", Toast.LENGTH_SHORT).show()
                                );
                            }

                            // Use CompletableFuture to check tracks in parallel
                            List<java.util.concurrent.CompletableFuture<ArtistData>> futures = new ArrayList<>();

                            for (SpotifyArtistSearchResponse.SpotifyArtist artist : items) {
                                futures.add(java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                                    try {
                                        Call<isberg.udacity.spotifystreamer.model.SpotifyTracksResponse> tracksCall = 
                                            SpotifyClient.getInstance().getService().getArtistTopTracks("Bearer " + token, artist.id, "US");
                                        
                                        Response<isberg.udacity.spotifystreamer.model.SpotifyTracksResponse> tracksResponse = tracksCall.execute();
                                        
                                        if (tracksResponse.isSuccessful() && tracksResponse.body() != null && tracksResponse.body().tracks != null) {
                                            for (isberg.udacity.spotifystreamer.model.SpotifyTracksResponse.SpotifyTrack track : tracksResponse.body().tracks) {
                                                if (track.preview_url != null && !track.preview_url.isEmpty()) {
                                                    // Found a playable track, this artist is valid
                                                    String imageUrl = null;
                                                    if (artist.images != null && !artist.images.isEmpty()) {
                                                        imageUrl = artist.images.get(0).url; 
                                                    }
                                                    return new ArtistData(artist.id, artist.name, imageUrl);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e(LOG_TAG, "Error checking tracks for artist: " + artist.name, e);
                                    }
                                    return null; // Invalid or error
                                }));
                            }

                            // Wait for all checks to complete
                            java.util.concurrent.CompletableFuture<Void> allFutures = java.util.concurrent.CompletableFuture.allOf(
                                futures.toArray(new java.util.concurrent.CompletableFuture[0])
                            );

                            allFutures.thenRun(() -> {
                                ArrayList<ArtistData> validArtists = new ArrayList<>();
                                for (java.util.concurrent.CompletableFuture<ArtistData> future : futures) {
                                    try {
                                        ArtistData data = future.join();
                                        if (data != null) {
                                            validArtists.add(data);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        artistAdapter.clear();
                                        artistAdapter.addAll(validArtists);
                                        if (artistAdapter.isEmpty()) {
                                            Toast.makeText(getActivity(), "No artists with playable tracks found.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });

                        } else {
                            Log.e(LOG_TAG, "Search failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<SpotifyArtistSearchResponse> call, Throwable t) {
                         Log.e(LOG_TAG, "Search error", t);
                    }
                });
            }

            @Override
            public void onError() {
                 Log.e(LOG_TAG, "Token fetch failed");
            }
        });
    }
}

class SearchArtistTextWatcher implements TextWatcher {
    private ArtistFragment artistFragment;

    public void setSearchArtistTextWatcher(ArtistFragment artistFragment) {
        this.artistFragment = artistFragment;
    }

    public void afterTextChanged(Editable s) {

    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s != null && s.length() > 0) { 
            artistFragment.searchArtists(s.toString());
        }
    }
}

class ArtistAdapter extends ArrayAdapter<ArtistData> {

    private final String LOG_TAG = ArtistAdapter.class.getSimpleName();

    private ArrayList<ArtistData> artistData;

    public ArtistAdapter(Context context, int resource, int textViewResourceId, ArrayList<ArtistData> artistData) {
        super(context, resource, textViewResourceId, artistData);
        this.artistData = artistData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ArtistData artistData = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
        }

        ImageView imName = (ImageView) convertView.findViewById(R.id.list_item_artist_imageview);
        if(artistData != null) {

            if(artistData.getArtistCoverUrl() != null ) {

                Picasso.get().cancelRequest(imName);
                Picasso.get().load(artistData.getArtistCoverUrl()).into(imName, new Callback() {
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

        TextView tvName = (TextView) convertView.findViewById(R.id.list_item_artist_textview);
        if(artistData != null && artistData.getArtistName() != null) {
            tvName.setText(artistData.getArtistName());
        }

        return convertView;
    }

    public ArrayList<ArtistData> getArtistData(){
        return artistData;
    }

    public void setArtistData(ArrayList<ArtistData> artistData) {
        this.artistData = artistData;
        notifyDataSetChanged();
    }
}


