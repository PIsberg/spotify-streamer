package isberg.udacity.spotifystreamer.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import isberg.udacity.spotifystreamer.R;
import isberg.udacity.spotifystreamer.service.PlayerService;

public class PlayerActivity extends Activity {

    private TextView artistNameTextView, albumNameTextView, trackNameTextView, trackCurrentTime, trackTotalTime;
    private ImageView albumCoverImageView;
    private SeekBar trackPlaying;
    private Button prevTrackButton, playTrackButton, nextTrackButton;

    private String trackPreviewURL, albumName, albumCoverUrl, artistName, trackName;
    private long trackDurationMs;

    private View.OnClickListener playOnClickListener;
    private View.OnClickListener pauseOnClickListener;

    public PlayerActivity() {
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_player);

        Bundle trackBundle = getIntent().getExtras().getBundle("trackBundle");
        if (trackBundle != null) {
            trackPreviewURL = trackBundle.getString("trackPreviewURL");
            albumName = trackBundle.getString("albumName");
            albumCoverUrl = trackBundle.getString("albumCoverUrl");
            trackName = trackBundle.getString("trackName");
            artistName = trackBundle.getString("artistName");
            trackDurationMs = trackBundle.getLong("trackDurationMs");
        }

        Toast.makeText(this, "onCreate albumName" + albumName + "trackName"+ trackName, Toast.LENGTH_SHORT).show();

        LinearLayout innerLinearLayout = (LinearLayout) findViewById(R.id.linearlayout1);

        artistNameTextView = (TextView) innerLinearLayout.findViewById(R.id.artistname_player_textview);
        artistNameTextView.setText(artistName);

        albumNameTextView = (TextView) innerLinearLayout.findViewById(R.id.albumname_player_textview);
        albumNameTextView.setText(albumName);

        albumCoverImageView = (ImageView) innerLinearLayout.findViewById(R.id.albumcover_player_imageview);

        Log.d("albumCoverUrl", albumCoverUrl);
        Picasso.with(this).load(albumCoverUrl).into(albumCoverImageView, new Callback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError() {
                Log.d("PlayerActivity", "Piccasso error!");
            }
        });


        trackNameTextView = (TextView) innerLinearLayout.findViewById(R.id.trackname_player_textview);
        trackNameTextView.setText(trackName);

        trackCurrentTime = (TextView) innerLinearLayout.findViewById(R.id.track_currenttime_textview);
        trackCurrentTime.setText("00:00");

        trackPlaying = (SeekBar) innerLinearLayout.findViewById(R.id.track_playing_seekbar);

        trackTotalTime = (TextView) innerLinearLayout.findViewById(R.id.track_totaltime_textview);
        //TODO: format duration time to readable format
        trackTotalTime.setText(String.valueOf(trackDurationMs));

        prevTrackButton = (Button) innerLinearLayout.findViewById(R.id.player_prevTrack_button);
        prevTrackButton.setText("prev");

        playTrackButton = (Button) innerLinearLayout.findViewById(R.id.player_play_button);
        playTrackButton.setText("play");

        nextTrackButton = (Button) innerLinearLayout.findViewById(R.id.player_nextTrack_button);
        nextTrackButton.setText("next");

        playOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playCurrentTrack(v);
            }
        };

        pauseOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseCurrentTrack(v);
            }
        };
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_player, container, false);
        Toast.makeText(this, "onCreateView ", Toast.LENGTH_SHORT).show();

        return rootView;
    }

    public void playCurrentTrack(View view) {

        Toast.makeText(this, "Pressed Play button " + trackPreviewURL, Toast.LENGTH_SHORT).show();

        playTrackButton.setText("pause");
        playTrackButton.setOnClickListener(pauseOnClickListener);

        Intent intent = new Intent(this, PlayerService.class);
        intent.setAction(PlayerService.PLAYER_ACTION_PLAY);

        Bundle bundle = new Bundle();
        bundle.putString("trackPreviewURL", trackPreviewURL);
        intent.putExtra("playerBundle", bundle);

        startService(intent);

    }


    public void pauseCurrentTrack(View view) {

        Toast.makeText(this, "Pressed Pause button " + trackPreviewURL, Toast.LENGTH_SHORT).show();

        playTrackButton.setText("play");
        playTrackButton.setOnClickListener(playOnClickListener);

        Intent intent = new Intent(this, PlayerService.class);
        intent.setAction(PlayerService.PLAYER_ACTION_PAUSE);

        startService(intent);

    }
    public void playPreviousTrack(View view) {

    }

    public void playNextTrack(View view) {

    }
}

