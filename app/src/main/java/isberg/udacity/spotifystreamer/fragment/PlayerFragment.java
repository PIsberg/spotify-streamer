package isberg.udacity.spotifystreamer.fragment;


import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class PlayerFragment extends DialogFragment {

    private TextView artistNameTextView, albumNameTextView, trackNameTextView, trackCurrentTime, trackTotalTime;
    private ImageView albumCoverImageView;
    private SeekBar trackPlayingSeekbar;
    private ImageButton prevTrackButton, playTrackButton, nextTrackButton;

    private String trackPreviewURL, albumName, albumCoverUrl, artistName, trackName;

    private ArrayList<TrackData> trackData;

    private long trackDurationMs;
    private int currentIndex = 0;
    private int indexSize = 0;

    private final int MAX_TRACK_DURATION_DEMO = 30000;

    private PlayButtonListener playOnClickListener;
    private PauseButtonListener pauseOnClickListener;

    private enum LOCAL_STATE { PLAY, PAUSE };

    private boolean isShownAsDialog = false;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setShowsDialog(isShownAsDialog);
    }

    private void initGui(View rootView) {
        // Track info layout
        LinearLayout innerLinearLayout1 = (LinearLayout) rootView.findViewById(R.id.linearlayout1);

        artistNameTextView = (TextView) innerLinearLayout1.findViewById(R.id.artistname_player_textview);
        albumNameTextView = (TextView) innerLinearLayout1.findViewById(R.id.albumname_player_textview);
        albumCoverImageView = (ImageView) innerLinearLayout1.findViewById(R.id.albumcover_player_imageview);
        trackNameTextView = (TextView) innerLinearLayout1.findViewById(R.id.trackname_player_textview);


        // Track playing layout
        LinearLayout innerLinearLayout2 = (LinearLayout) rootView.findViewById(R.id.linearlayout2);
        trackPlayingSeekbar = (SeekBar) innerLinearLayout2.findViewById(R.id.track_playing_seekbar);


        trackPlayingSeekbar.setOnSeekBarChangeListener(new SeekBarPlayerListener());

        // Track playing info layout
        RelativeLayout innerLinearLayout3 = (RelativeLayout) rootView.findViewById(R.id.linearlayout3);
        trackCurrentTime = (TextView) innerLinearLayout3.findViewById(R.id.track_currenttime_textview);
        trackCurrentTime.setText(getString(R.string.track_start_time));

        trackTotalTime = (TextView) innerLinearLayout3.findViewById(R.id.track_totaltime_textview);


        // Track player controls layout
        LinearLayout innerLinearLayout4 = (LinearLayout) rootView.findViewById(R.id.linearlayout4);

        prevTrackButton = (ImageButton) innerLinearLayout4.findViewById(R.id.player_prevTrack_button);
        playTrackButton = (ImageButton) innerLinearLayout4.findViewById(R.id.player_play_button);
        nextTrackButton = (ImageButton) innerLinearLayout4.findViewById(R.id.player_nextTrack_button);

        playOnClickListener = new PlayButtonListener();
        pauseOnClickListener = new PauseButtonListener();
        playTrackButton.setOnClickListener(playOnClickListener);

        //case when first item or last item is selected in list
        if(currentIndex == 0) {
            prevTrackButton.setEnabled(false);
        }
        else {
            prevTrackButton.setEnabled(true);
        }

        if(currentIndex == trackData.size()-1) {
            nextTrackButton.setEnabled(false);
        }
        else {
            nextTrackButton.setEnabled(true);
        }

        playTrackButton.setTag(LOCAL_STATE.PLAY);

    }

    // used for e.g backpressed
    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("PlayerFragment", "onDetach");

        trackDurationMs = 0;
        currentIndex = 0;

        Intent intent = new Intent(getActivity(), PlayerService.class);
        intent.setAction(PlayerService.PLAYER_ACTION_STOP);
        getActivity().startService(intent);
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

        Picasso.with(getActivity()).load(albumCoverUrl).fit().into(albumCoverImageView, new Callback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError() {
                Log.d("PlayerFragment", "Piccasso error!");
            }
        });

        trackPlayingSeekbar.setMax((int) trackDurationMs);
    }

    private BroadcastReceiver playerServiceCurrentPosMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("PlayerFragment", "onReceive");

            updateButtonStates(currentIndex);
            int currentPositionInMs = intent.getIntExtra("currentPositionInMs", 0);
            trackCurrentTime.setText(formatTime(currentPositionInMs));
            trackPlayingSeekbar.setProgress(currentPositionInMs);
     }
    };


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if ( this.getArguments() != null) {
            Bundle bundle = this.getArguments();
            artistName = bundle.getString("artistName");
            trackData = bundle.getParcelableArrayList("trackData");
            currentIndex = bundle.getInt("currentIndex");
        }

        setShowsDialog(isShownAsDialog);

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        initGui(rootView);

        populateGui(currentIndex);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(playerServiceCurrentPosMessageReceiver, new IntentFilter("PlayerActivity"));

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
        Log.d("PlayerFragment", "playCurrentTrack");

        if(playTrackButton.getTag().equals(LOCAL_STATE.PAUSE)) {
            Intent stopIntent = new Intent(getActivity(), PlayerService.class);
            stopIntent.setAction(PlayerService.PLAYER_ACTION_STOP);
            getActivity().startService(stopIntent);
        }

        trackPreviewURL = trackData.get(currentIndex).getPreviewUrl();

        playTrackButton.setImageResource(android.R.drawable.ic_media_pause);
        playTrackButton.setTag(LOCAL_STATE.PLAY);
        playTrackButton.setOnClickListener(pauseOnClickListener);

        Intent intent = new Intent(getActivity(), PlayerService.class);
        intent.setAction(PlayerService.PLAYER_ACTION_PLAY);

        Bundle bundle = new Bundle();
        bundle.putString("trackPreviewURL", trackPreviewURL);
        bundle.putInt("seekTimeMsec", 1);
        intent.putExtra("playerBundle", bundle);

        getActivity().startService(intent);
    }


    public void pauseCurrentTrack(View view) {
        Log.d("PlayerFragment", "pauseCurrentTrack");

        playTrackButton.setImageResource(android.R.drawable.ic_media_play);
        playTrackButton.setTag(LOCAL_STATE.PAUSE);
        playTrackButton.setOnClickListener(playOnClickListener);

        Intent intent = new Intent(getActivity(), PlayerService.class);
        intent.setAction(PlayerService.PLAYER_ACTION_PAUSE);

        getActivity().startService(intent);
    }

    public void playPreviousTrack(View view) {
        Log.d("PlayerFragment", "playPreviousTrack In" + currentIndex);

        int indexBefore = currentIndex;
        int prevIndex = (currentIndex-1);

        if(prevIndex >= 0 && prevIndex <= (trackData.size()-1) ) {

            Intent intent = new Intent(getActivity(), PlayerService.class);
            intent.setAction(PlayerService.PLAYER_ACTION_STOP);
            getActivity().startService(intent);

            currentIndex = prevIndex;
            populateGui(currentIndex);

            if(playTrackButton.getTag().equals(LOCAL_STATE.PLAY)) {
                lockDownGui();
                playCurrentTrack(view);
            }
            else {
                trackPlayingSeekbar.setProgress(0);
                trackCurrentTime.setText(getString(R.string.track_start_time));
            }
        }
        else {
            currentIndex = indexBefore;
        }

        Log.d("PlayerFragment", "playPreviousTrack Out" + currentIndex);

    }

    private void updateButtonStates(int currentIndex) {

        trackPlayingSeekbar.setEnabled(true);
        playTrackButton.setEnabled(true);

        if(currentIndex != 0) {
            prevTrackButton.setEnabled(true);
        }

        if(currentIndex != (trackData.size()-1)) {
            nextTrackButton.setEnabled(true);
        }
    }

    private void lockDownGui() {
        trackPlayingSeekbar.setEnabled(false);
        playTrackButton.setEnabled(false);
        prevTrackButton.setEnabled(false);
        nextTrackButton.setEnabled(false);
    }

    public void playNextTrack(View view) {
        Log.d("PlayerFragment", "playNextTrack In" + currentIndex);
        int indexBefore = currentIndex;
        int nextIndex = (currentIndex+1);

        if(nextIndex >= 1 && nextIndex <= trackData.size() ) {

            Intent intent = new Intent(getActivity(), PlayerService.class);
            intent.setAction(PlayerService.PLAYER_ACTION_STOP);
            getActivity().startService(intent);

            currentIndex = nextIndex;
            populateGui(currentIndex);

            if(playTrackButton.getTag().equals(LOCAL_STATE.PLAY)) {
                lockDownGui();
                playCurrentTrack(view);
            }
            else {
                trackPlayingSeekbar.setProgress(0);
                trackCurrentTime.setText(getString(R.string.track_start_time));
            }
        }
        else {
            currentIndex = indexBefore;
        }



        Log.d("PlayerFragment", "playNextTrack Out" + currentIndex);
    }

    public void playTrackFrom(int progress) {
        Log.d("PlayerFragment", "playTrackFrom");

        trackPreviewURL = trackData.get(currentIndex).getPreviewUrl();

        Intent playIntent = new Intent(getActivity(), PlayerService.class);
        playIntent.setAction(PlayerService.PLAYER_ACTION_SEEK);
        Bundle bundle = new Bundle();

        if(progress == 0) {
            bundle.putInt("seekTimeMsec", 1);
        }
        else {
            bundle.putInt("seekTimeMsec", progress);
        }
        bundle.putInt("seekTimeMsec", progress);
        bundle.putString("trackPreviewURL", trackPreviewURL);
        playIntent.putExtra("playerBundle", bundle);

        getActivity().startService(playIntent);
    }

    public boolean isShownAsDialog() {
        return isShownAsDialog;
    }

    public void setIsShownAsDialog(boolean isShownAsDialog) {
        this.isShownAsDialog = isShownAsDialog;
    }

    class PlayButtonListener implements ImageButton.OnClickListener {
        @Override
        public void onClick(View v) {
            playCurrentTrack(v);
        }
    }

    class PauseButtonListener implements ImageButton.OnClickListener {
        @Override
        public void onClick(View v) {
            pauseCurrentTrack(v);
        }
    }
    class SeekBarPlayerListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar){
            Log.d("PlayerFragment","onStopTrackingTouch");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar){
            Log.d("PlayerFragment","onStartTrackingTouch");
        }

        @Override
        public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser){
            if(fromUser){
                Log.d("PlayerFragment","onProgressChanged:: "+progress);

                seekBar.setProgress(progress);
                trackCurrentTime.setText(formatTime(progress));
                playTrackFrom(progress);
            }

        }
    }

}

