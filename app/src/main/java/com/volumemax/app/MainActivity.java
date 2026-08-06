package com.volumemax.app;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends Activity {

    private AudioManager audioManager;
    private TextView volumeText;
    private MediaPlayer mediaPlayer;
    private static final int[] BLOCKED_KEYS = {
        KeyEvent.KEYCODE_BACK,
        KeyEvent.KEYCODE_HOME,
        KeyEvent.KEYCODE_APP_SWITCH,
        KeyEvent.KEYCODE_POWER,
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_MUTE,
        KeyEvent.KEYCODE_SETTINGS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Manter a tela ligada
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Tela cheia
        hideSystemUI();

        setContentView(R.layout.activity_main);

        volumeText = findViewById(R.id.volumeText);

        // Inicializar AudioManager
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Maximizar volume
        maximizeVolume();

        // Tocar áudio automaticamente
        playAudio();

        // Definir orientação como paisagem para dificultar navegação
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void playAudio() {
        try {
            // Tentar tocar áudio da pasta raw (coloque o arquivo audio.mp3 em res/raw/)
            int resId = getResources().getIdentifier("audio", "raw", getPackageName());
            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(this, resId);
                if (mediaPlayer != null) {
                    mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build());
                    mediaPlayer.setLooping(true); // Loop infinito
                    mediaPlayer.start();
                    volumeText.setText("🎵 Volume Máximo - Tocando! 🎵");
                }
            } else {
                // Tentar do asset
                playFromAsset();
            }
        } catch (Exception e) {
            try {
                playFromAsset();
            } catch (Exception e2) {
                volumeText.setText("Coloque o arquivo audio.mp3 na pasta res/raw/");
            }
        }
    }

    private void playFromAsset() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getAssets().openFd("audio/gemido-whatsapp.mp3"));
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
            volumeText.setText("🎵 Volume Máximo - Tocando! 🎵");
        } catch (IOException e) {
            volumeText.setText("Coloque gemido-whatsapp.mp3 em app/src/main/assets/audio/");
        }
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().getInsetsController().hide(android.view.WindowInsets.Type.statusBars() | android.view.WindowInsets.Type.navigationBars());
        } else {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    private void maximizeVolume() {
        try {
            // Tentar definir o volume máximo para música
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            );
            
            volumeText.setText("Volume no MÁXIMO!");
            volumeText.setTextSize(32);
        } catch (Exception e) {
            volumeText.setText("Maximize o volume manualmente");
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        
        // Verificar se a tecla está bloqueada
        for (int blocked : BLOCKED_KEYS) {
            if (keyCode == blocked) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    // Se for volume, maximizar novamente
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                        maximizeVolume();
                    }
                    return true;
                }
            }
        }
        
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        // Bloquear botão voltar
        return;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Maximizar volume sempre que tocar na tela
        maximizeVolume();
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
            maximizeVolume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        maximizeVolume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
