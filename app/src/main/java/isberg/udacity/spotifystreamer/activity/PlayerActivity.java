package isberg.udacity.spotifystreamer.activity;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import isberg.udacity.spotifystreamer.R;
import isberg.udacity.spotifystreamer.model.TrackData;
import isberg.udacity.spotifystreamer.service.PlayerService;

public class PlayerActivity extends Activity {

    private TextView artistNameTextView, albumNameTextView, trackNameTextView, trackCurrentTime, trackTotalTime;
    private ImageView albumCoverImageView;
    private SeekBar trackPlayingSeekbar;
    private Button prevTrackButton, playTrackButton, nextTrackButton;

    private String trackPreviewURL, albumName, albumCoverUrl, artistName, trackName;

    private ArrayList<TrackData> trackData;

    private long trackDurationMs;
    private int currentIndex;
    private int indexSize = 0;

    private final int MAX_TRACK_DURATION_DEMO = 30000;


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
            artistName = trackBundle.getString("artistName");
            trackData = trackBundle.getParcelableArrayList("trackData");
            currentIndex = trackBundle.getInt("currentIndex");
        }

        initGui();
        populateGui(currentIndex);
        LocalBroadcastManager.getInstance(this).registerReceiver(playerServiceMessageReceiver, new IntentFilter("PlayerActivity"));

    }

    private void initGui() {
        // Track info layout
        LinearLayout innerLinearLayout1 = (LinearLayout) findViewById(R.id.linearlayout1);

        artistNameTextView = (TextView) innerLinearLayout1.findViewById(R.id.artistname_player_textview);
        albumNameTextView = (TextView) innerLinearLayout1.findViewById(R.id.albumname_player_textview);
        albumCoverImageView = (ImageView) innerLinearLayout1.findViewById(R.id.albumcover_player_imageview);
        trackNameTextView = (TextView) innerLinearLayout1.findViewById(R.id.trackname_player_textview);


        // Track playing layout
        LinearLayout innerLinearLayout2 = (LinearLayout) findViewById(R.id.linearlayout2);
        trackPlayingSeekbar = (SeekBar) innerLinearLayout2.findViewById(R.id.track_playing_seekbar);


        trackPlayingSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("PlayerActivity", "onStopTrackingTouch");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("PlayerActivity", "onStartTrackingTouch");
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Log.d("PlayerActivity", "onProgressChanged:: " + progress);

                    seekBar.setProgress(progress);
                    trackCurrentTime.setText(formatTime(progress));
                    playTrackFrom(progress);
                }

            }
        });


        // Track playing info layout
        RelativeLayout innerLinearLayout3 = (RelativeLayout) findViewById(R.id.linearlayout3); // TODO change name
        trackCurrentTime = (TextView) innerLinearLayout3.findViewById(R.id.track_currenttime_textview);
        trackCurrentTime.setText(getString(R.string.track_start_time));

        trackTotalTime = (TextView) innerLinearLayout3.findViewById(R.id.track_totaltime_textview);


        // Track player controls layout
        RelativeLayout innerLinearLayout4 = (RelativeLayout) findViewById(R.id.linearlayout4); // TODO change name

        prevTrackButton = (Button) innerLinearLayout4.findViewById(R.id.player_prevTrack_button);
        prevTrackButton.setText("prev"); //TODO: put in strings.xml

        playTrackButton = (Button) innerLinearLayout4.findViewById(R.id.player_play_button);
        playTrackButton.setText("play"); //TODO: put in strings.xml

        nextTrackButton = (Button) innerLinearLayout4.findViewById(R.id.player_nextTrack_button);
        nextTrackButton.setText("next"); //TODO: put in strings.xml

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

    private void populateGui(int currentIndex) {

        indexSize = trackData.size();

        albumName = trackData.get(currentIndex).getAlbumName();
        albumCoverUrl = trackData.get(currentIndex).getAlbumCoverUrl();
        trackName = trackData.get(currentIndex).getName();

        albumNameTextView.setText(albumName);
        artistNameTextView.setText(artistName);
        trackNameTextView.setText(trackName);

        trackDurationMs = trackData.get(currentIndex).getDurationMs();

        //NOTE: in demo api only at most 30 seconds of preview is available
        if(trackDurationMs > MAX_TRACK_DURATION_DEMO) {
            trackDurationMs = MAX_TRACK_DURATION_DEMO;
        }

        trackTotalTime.setText(formatTime(trackDurationMs));

        Picasso.with(this).load(albumCoverUrl).into(albumCoverImageView, new Callback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError() {
                Log.d("PlayerActivity", "Piccasso error!");
            }
        });

        trackPlayingSeekbar.setMax((int)trackDurationMs);
    }

    private BroadcastReceiver playerServiceMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("PlayerActivity", "onReceive");

            int currentPositionInMs = intent.getIntExtra("currentPositionInMs", 0);
            trackCurrentTime.setText(formatTime(currentPositionInMs));
            trackPlayingSeekbar.setProgress(currentPositionInMs);
     }
    };


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_player, container, false);
        Toast.makeText(this, "onCreateView ", Toast.LENGTH_SHORT).show();

        return rootView;
    }

    private String formatTime(long timeInMs) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeInMs),
                TimeUnit.MILLISECONDS.toSeconds(timeInMs) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMs))
        );

    }


    public void playCurrentTrack(View view) {

        trackPreviewURL = trackData.get(currentIndex).getPreviewUrl();

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

        playTrackButton.setText("play");
        playTrackButton.setOnClickListener(playOnClickListener);

        Intent intent = new Intent(this, PlayerService.class);
        intent.setAction(PlayerService.PLAYER_ACTION_PAUSE);

        startService(intent);

    }
    public void playPreviousTrack(View view) {
        int prevIndex = --currentIndex;

        if(prevIndex>= 0 && prevIndex<= trackData.size() ) {

            Intent intent = new Intent(this, PlayerService.class);
            intent.setAction(PlayerService.PLAYER_ACTION_STOP);
            startService(intent);

            populateGui(prevIndex);

            playCurrentTrack(view);

            currentIndex = prevIndex;
        }


        if(currentIndex+1 == trackData.size()) {
            nextTrackButton.setEnabled(false);
        }

        if(currentIndex-1 > 0) {
            prevTrackButton.setEnabled(true);
        }

    }

    public void playNextTrack(View view) {
        int nextIndex = ++currentIndex;

        if(nextIndex>= 0 && nextIndex<= trackData.size() ) {

            Intent intent = new Intent(this, PlayerService.class);
            intent.setAction(PlayerService.PLAYER_ACTION_STOP);
            startService(intent);

            populateGui(nextIndex);

            playCurrentTrack(view);

            currentIndex = nextIndex;
        }

        if(currentIndex-1 == 0) {
            prevTrackButton.setEnabled(false);
        }

        if(currentIndex+1 < trackData.size()) {
            nextTrackButton.setEnabled(true);
        }

    }

    public void playTrackFrom(int progress) {

        trackPreviewURL = trackData.get(currentIndex).getPreviewUrl();

        Intent playIntent = new Intent(this, PlayerService.class);
        playIntent.setAction(PlayerService.PLAYER_ACTION_SEEK);
        Bundle bundle = new Bundle();

        bundle.putInt("seekTimeMsec", progress);
        bundle.putString("trackPreviewURL", trackPreviewURL);
        playIntent.putExtra("playerBundle", bundle);

        startService(playIntent);
    }

}