package isberg.udacity.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import isberg.udacity.spotifystreamer.fragment.ArtistFragment;
import isberg.udacity.spotifystreamer.fragment.PlayerFragment;
import isberg.udacity.spotifystreamer.fragment.TrackDetailFragment;
import isberg.udacity.spotifystreamer.fragment.TrackFragment;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This is when data
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString(TrackFragment.ARTIST_NAME_KEY);
        }


        if (savedInstanceState == null) {

            // is tablet
           if (findViewById(R.id.track_detail_container) != null) {
                Log.d("MainActivity", "onCreate tablet");
                TrackDetailFragment tdf = new TrackDetailFragment();
                tdf.setArguments(extras);

               //fragment_artist
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.track_detail_container, tdf, "trackdetailfragment")
                                .commit();


            }
            else { // is phone
                Log.d("MainActivity", "onCreate phone");
                ArtistFragment af = new ArtistFragment();
                af.setArguments(extras);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.artist_container, af)
                        .commit();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

}
