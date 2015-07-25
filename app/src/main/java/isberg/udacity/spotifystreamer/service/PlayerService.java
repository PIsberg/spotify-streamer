package isberg.udacity.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {

    private MediaPlayer mediaPlayer = null;

    public static final String PLAYER_ACTION_PLAY = "PLAY";
    public static final String PLAYER_ACTION_PAUSE = "PAUSE";
    public static final String PLAYER_ACTION_STOP = "STOP";

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(PlayerService.this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        String actionCommand = intent.getAction();
        Log.d("PlayerService", "onStartCommand:" + actionCommand);

        if(mediaPlayer == null) {
            initMediaPlayer();
        }

        if (actionCommand.equals(PLAYER_ACTION_PLAY)) {

            String url = getUrl(intent);
            int seekTimeMsec = getSeekTime(intent);

            try {
                mediaPlayer.setDataSource(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(seekTimeMsec > 0) {
                mediaPlayer.seekTo(seekTimeMsec);
            }

            mediaPlayer.prepareAsync(); // prepare async to not block main thread
        }
        else if(actionCommand.equals(PLAYER_ACTION_PAUSE)) {
            mediaPlayer.pause();
        }
        else if(actionCommand.equals(PLAYER_ACTION_STOP)) {
            mediaPlayer.stop();
        }

        //mMediaPlayer.getCurrentPosition()

        return Service.START_STICKY;
    }

    private String getUrl(Intent intent) {
        String url = "";
        Bundle playerBundle = intent.getExtras().getBundle("playerBundle");

        if (playerBundle != null) {
            url = playerBundle.getString("trackPreviewURL");
        }

        return url;
    }

    private int getSeekTime(Intent intent) {
        int seekTimeMsec = 0;
        Bundle playerBundle = intent.getExtras().getBundle("playerBundle");

        if (playerBundle != null) {
            seekTimeMsec = playerBundle.getInt("seekTimeMsec");
        }

        return seekTimeMsec;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer mMediaPlayer) {
        Log.d("PlayerService ", "onPrepared");
        mMediaPlayer.start();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }
}