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
    public static final String PLAYER_ACTION_PLAY = "PLAY";
    public static final String PLAYER_ACTION_PAUSE = "PAUSE";
    public static final String PLAYER_ACTION_STOP = "STOP";

    MediaPlayer mMediaPlayer = null;

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(PLAYER_ACTION_PLAY)) {

            //TODO: I only need one mediaplayer instance, right?
            //Toast.makeText(super.getApplicationContext(), "Play intent", Toast.LENGTH_SHORT).show();
            Log.d("PlayerService ", "play service");
            mMediaPlayer = new MediaPlayer();

            //String url = "https://p.scdn.co/mp3-preview/e7b1e7e641fa64be0d1c650357653fc9f0f8302c";

            Bundle playerBundle = intent.getExtras().getBundle("playerBundle");
            String url = "";
            if (playerBundle != null) {
                url = playerBundle.getString("trackPreviewURL");
            }

            mMediaPlayer.setOnPreparedListener(PlayerService.this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mMediaPlayer.setDataSource(url);
               // mMediaPlayer.setDataSource(this, absolute);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mMediaPlayer.prepareAsync(); // prepare async to not block main thread

        }
        else if(intent.getAction().equals(PLAYER_ACTION_PAUSE)) {
            mMediaPlayer.pause();
        }
        else if(intent.getAction().equals(PLAYER_ACTION_STOP)) {
            mMediaPlayer.stop();
        }
        return Service.START_STICKY;
    }




    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer mMediaPlayer) {


        Log.d("PlayerService ", "play service");
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