package isberg.udacity.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {

    private MediaPlayer mediaPlayer;

    public static final String PLAYER_ACTION_PLAY = "PLAY";
    public static final String PLAYER_ACTION_PAUSE = "PAUSE";
    public static final String PLAYER_ACTION_STOP = "STOP";
    public static final String PLAYER_ACTION_SEEK = "SEEK";

    private final int BROADCAST_INTERVAL_MS = 300;
    private TimerTask broadCastCurrentTimeTimerTask;
    private Handler handler = new Handler();
    private Timer broadCastCurrentTimeTimer;


    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(PlayerService.this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        String actionCommand = intent.getAction();
        Log.d("PlayerService", "onStartCommand: " + actionCommand);

        if(mediaPlayer == null) {
            if (actionCommand.equals(PLAYER_ACTION_PLAY) && mediaPlayer == null) {

                initMediaPlayer();
                pendingSeekTime = getSeekTime(intent);
                String url = getUrl(intent);
                try {
                    if (url != null && !url.isEmpty()) {
                        mediaPlayer.setDataSource(url);
                        mediaPlayer.prepareAsync(); // prepare async to not block main thread
                    } else {
                        Log.e("PlayerService", "Preview URL is null or empty");
                        stopSelf(); // Stop the service if no URL
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    stopSelf();
                }
            }
        }
        else {
            if (actionCommand.equals(PLAYER_ACTION_PLAY) && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            } else if (actionCommand.equals(PLAYER_ACTION_PAUSE) &&  mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else if (actionCommand.equals(PLAYER_ACTION_SEEK)) {
                updateSeekTime(intent);
            } else if (actionCommand.equals(PLAYER_ACTION_STOP) && mediaPlayer.isPlaying()) {
                cancelBroadCastCurrentTime();
                terminateMediaPlayer();
            }
        }
        return Service.START_STICKY;
    }

    private void cancelBroadCastCurrentTime() {
        broadCastCurrentTimeTimerTask.cancel();
        broadCastCurrentTimeTimer.cancel();
        broadCastCurrentTimeTimer = null;
    }

    private void terminateMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer = null;
    }

    private void updateSeekTime(Intent intent) {
        int seekTimeMsec = getSeekTime(intent);
        if (seekTimeMsec >= 0) {
            mediaPlayer.seekTo(seekTimeMsec);
        }
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

    private int pendingSeekTime = -1;

    public void onPrepared(MediaPlayer mMediaPlayer) {
        Log.d("PlayerService", "onPrepared");
        if(broadCastCurrentTimeTimer == null) {
            broadCastCurrentTimeTimer = new Timer();
            runBroadCastCurrentTimeTimerTask();
            broadCastCurrentTimeTimer.schedule(broadCastCurrentTimeTimerTask, 0, BROADCAST_INTERVAL_MS);
        }
        if (pendingSeekTime >= 0) {
            mMediaPlayer.seekTo(pendingSeekTime);
            pendingSeekTime = -1;
        }
        mMediaPlayer.start();

        // Notify PlayerActivity that the asynch start of the mediaplayer is finished

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    // Continuesly notifiy the PlayerActivity about the current tracks playing time
    public void runBroadCastCurrentTimeTimerTask() {
        broadCastCurrentTimeTimerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
                            long currentPosition = mediaPlayer.getCurrentPosition();
                            Log.d("PlayerService", "currentPosition: " + currentPosition);
                            Intent intent = new Intent("PlayerActivity");
                            intent.putExtra("currentPositionInMs", mediaPlayer.getCurrentPosition());
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        }
                    }
                });
            }
        };
    }
}