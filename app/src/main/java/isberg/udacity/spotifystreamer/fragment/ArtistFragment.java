package isberg.udacity.spotifystreamer.fragment;

import android.app.ActionBar;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import java.util.ListIterator;

import isberg.udacity.spotifystreamer.R;
import isberg.udacity.spotifystreamer.model.ArtistData;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

public class ArtistFragment extends Fragment {
    private ListView listView;
    private EditText searchArtistEditText;

    private final String ARTIST_DATA_KEY = "artistData";

    private ArtistAdapter artistAdapter;

    public ArtistFragment() {
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);

        //Picasso.with(getActivity()).setIndicatorsEnabled(true);
        Picasso.with(getActivity()).setLoggingEnabled(true);

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
            Log.d("onViewStateRestored", artistDataList.get(0).getArtistName());
            artistAdapter.setArtistData(artistDataList);
        }

        super.onViewStateRestored(bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {

        if(artistAdapter != null && !artistAdapter.isEmpty()) {
            Log.d("onSaveInstanceState", artistAdapter.getArtistData().get(0).getArtistName());
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

                //Toast.makeText(getActivity(), "Clicked " + position + " with name" + itemContent.getArtist().name + " and id " + itemContent.getArtist().id, Toast.LENGTH_SHORT).show();

                ActionBar actionBar = getActivity().getActionBar();
                actionBar.setTitle(R.string.track_fragment_title);
                actionBar.setSubtitle(itemContent.getArtistName());
                actionBar.setDisplayHomeAsUpEnabled(true);

                TrackFragment trackFragment = new TrackFragment();
                Bundle bundle = new Bundle();
                bundle.putString("artistId", itemContent.getArtistId());
                trackFragment.setArguments(bundle);

                FragmentTransaction trackFragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                trackFragmentTransaction.addToBackStack(null);

                Fragment currentFragment = (Fragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.artist_container);
                trackFragmentTransaction.remove(currentFragment);
                trackFragmentTransaction.add(R.id.artist_container, trackFragment);
                trackFragmentTransaction.commit();
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


    public ArtistSearchTask getInstance() {
        return new ArtistSearchTask(new ArtistAdapterCallBack() {
            public void onCallBack(ArtistData[] result) {
                if (result != null) {
                    artistAdapter.clear();
                    for (ArtistData artistData : result) {
                        artistAdapter.add(artistData);
                    }
                }

                if(artistAdapter.isEmpty()) {
                    Toast.makeText(getActivity(), R.string.artist_noresult_toast, Toast.LENGTH_SHORT).show();
                }
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

        if (s != null && s.length() > 0) { // prevent same search muliple times like on rotation
            //TODO: better way of doing this?
            artistFragment.getInstance().execute(s.toString());
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
        // Get the data item for this position
        ArtistData artistData = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
        }

        ImageView imName = (ImageView) convertView.findViewById(R.id.list_item_artist_imageview);
        if(artistData != null) {

            if(artistData.getArtistCoverUrl() != null ) {

                Picasso.with(getContext()).cancelRequest(imName);
                Picasso.with(getContext()).load(artistData.getArtistCoverUrl()).into(imName, new Callback() {
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

        // Lookup view for data population
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

class ArtistSearchTask extends AsyncTask<String, Void, ArtistData[]> {

    private final String LOG_TAG = ArtistSearchTask.class.getSimpleName();
    private ArtistAdapterCallBack callback;
    private ArtistData[] array = null;
    private String artistId;

    public ArtistSearchTask(ArtistAdapterCallBack callback) {
        this.callback = callback;
    }

    @Override
    protected ArtistData[] doInBackground(String... params) {

        if (params.length == 0) {
            return null;
        }
        //TODO: initate somwhere else *once*
        SpotifyApi api = new SpotifyApi();

        SpotifyService spotifyService = api.getService();
        ArtistsPager artistsPager = null;
        try {
            artistsPager = spotifyService.searchArtists(params[0]);
        } catch (RetrofitError error) {
            Log.d(LOG_TAG, "Out of internet and stuff:" + error.getMessage());
            isCancelled();
        }
        if (artistsPager != null) {
            array = processArtistData(artistsPager);
        }

        return array;
    }
    @Override
    protected void onPostExecute(ArtistData[] result) {
        callback.onCallBack(result);
    }


    private ArtistData[] processArtistData(ArtistsPager artistsPager) {

        ArtistData[] array = null;

        ListIterator<Artist> artistIter = artistsPager.artists.items.listIterator();

        ArrayList<ArtistData> listViewData = new ArrayList<ArtistData>();

        while (artistIter.hasNext()) {
            Artist artist = artistIter.next();

            String artistCoverUrl = null;

            if (artist.images.size() > 0) {
                artistCoverUrl = artist.images.get(0).url;
            }
            ArtistData artistData = new ArtistData(artist.id, artist.name, artistCoverUrl);

            //  artistData.setArtistArt(artistArt);
            Log.d(artist.name, "Artist");
            if (artist.images != null && artist.images.size() > 0) {
                Log.d(artist.images.get(0).url, LOG_TAG + "ArtistArt URL");
            } else {
                Log.d("No artist art", LOG_TAG + "ArtistArt URL");
            }

            listViewData.add(artistData);
        }

        if (listViewData.size() > 0) {
            array = listViewData.toArray(new ArtistData[listViewData.size()]);
        }
        return array;
    }
}


interface ArtistAdapterCallBack{
    public void onCallBack(ArtistData[] result);
}

