package isberg.udacity.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity; // AndroidX
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import isberg.udacity.spotifystreamer.fragment.TrackDetailFragment;
import isberg.udacity.spotifystreamer.fragment.TrackFragment;


public class TrackDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        TrackDetailFragment tf = new TrackDetailFragment();
        Bundle bundle = getIntent().getExtras();
        tf.setArguments(bundle);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.track_detail_container, tf)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}