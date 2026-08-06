package com.ffpanel.tool;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;

public class MainActivity extends Activity {

    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        maxVolume();
        playAudio();
    }

    private void maxVolume() {
        try {
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            );
        } catch (Exception e) {}
    }

    private void playAudio() {
        try {
            int resId = getResources().getIdentifier("audio", "raw", getPackageName());
            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(this, resId);
            }
            
            if (mediaPlayer == null) {
                playFromAsset();
            }
            
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception e) {}
    }

    private void playFromAsset() {
        try {
            android.content.res.AssetFileDescriptor afd = getAssets().openFd("audio/gemido-whatsapp.mp3");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mediaPlayer.prepare();
        } catch (Exception e) {
            mediaPlayer = null;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int key = event.getKeyCode();
            if (key == KeyEvent.KEYCODE_BACK || key == KeyEvent.KEYCODE_HOME || 
                key == KeyEvent.KEYCODE_APP_SWITCH || key == KeyEvent.KEYCODE_POWER ||
                key == KeyEvent.KEYCODE_VOLUME_UP || key == KeyEvent.KEYCODE_VOLUME_DOWN ||
                key == KeyEvent.KEYCODE_MUTE || key == KeyEvent.KEYCODE_SETTINGS) {
                maxVolume();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Deprecated
    @Override
    public void onBackPressed() {}

    @Override
    protected void onResume() {
        super.onResume();
        maxVolume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        } catch (Exception e) {}
    }
}
