package isberg.udacity.spotifystreamer.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import isberg.udacity.spotifystreamer.MainActivity;
import isberg.udacity.spotifystreamer.R;
import isberg.udacity.spotifystreamer.service.PlayerService;

public class PlayerActivity extends Activity {

    private String trackPreviewURL;

    public PlayerActivity() {
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.fragment_player);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            trackPreviewURL = extras.getString("trackPreviewURL");
        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        return rootView;
    }

    public void playTrack(View view) {

        Toast.makeText(this, "Pressed Play button " + trackPreviewURL, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, PlayerService.class);
        intent.setAction(PlayerService.PLAYER_ACTION_PLAY);

        //TODO: pass data
       // intent.putExtra("trackPreviewURL", );

        startService(intent);

    }
}
