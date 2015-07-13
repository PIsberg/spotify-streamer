package isberg.udacity.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import isberg.udacity.spotifystreamer.fragment.ArtistFragment;
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

        ArtistFragment af = new ArtistFragment();
        af.setArguments(extras);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.artist_container, af)
                    .commit();
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
