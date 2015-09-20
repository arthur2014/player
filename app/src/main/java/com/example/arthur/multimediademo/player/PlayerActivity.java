package com.example.arthur.multimediademo.player;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arthur.multimediademo.R;

import java.io.File;
import java.io.IOException;

public class PlayerActivity extends Activity implements View.OnClickListener, SurfaceHolder.Callback {
    private static String TAG = "PlayerActivity";

    private TextView label;

    private ProgressDialog proDialog;
    private Button play;
    private Button stop;
    private SurfaceHolder surfaceHolder;
    private  SurfaceView surfaceView;
    private  PlayerController controller;

    private Handler handler;
    private final int UpdateUI = 99;
    private final int ShowDialog = 100;
    private final int CloseDialog = 101;
    private final int LocalMusic=103;
    private final int NetMusic=104;
    private final int LocalVideo=105;
    private final int NetVideo=106;
    private int playType=LocalMusic;

    private int state;

    private boolean EnableMediaController =true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initRessource();

    }

    private PlayerService mPlayer;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(TAG,"onServiceConnected");
            mPlayer = ((PlayerService.PlayerBinder)service).getPlayer();
            state =mPlayer.getState();
            if(EnableMediaController){
                mPlayer.getPlayer().setDisplay(surfaceHolder);
                String storage = Environment.getExternalStorageDirectory().getAbsolutePath();
                String videoLocation = storage + "/DCIM/100MEDIA/VIDEO0001.mp4";
                File videoFile = new File(videoLocation);
                if (!videoFile.exists()) {
                    Toast.makeText(PlayerActivity.this, "file is not exist ", Toast.LENGTH_LONG).show();
                    return;
                }

                Uri uri = Uri.fromFile(videoFile);
                try {
                    mPlayer.getPlayer().setDataSource(PlayerActivity.this, uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                mPlayer.getPlayer().prepareAsync();
                controller = new PlayerController(PlayerActivity.this);
                controller.setup(mPlayer,surfaceView);
                controller.setEnabled(true);
                controller.show();
            }
        }
        private boolean isInPlaybackState() {
            return (mPlayer != null &&
                    state != PlayerService.IdleState );
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayer =null;
            state = -1;
        }
    };

    private void initRessource(){
        label = (TextView) findViewById(R.id.label);
        play = (Button) findViewById(R.id.play);
        play.setOnClickListener(this);
        stop = (Button)findViewById(R.id.Stop);
        stop.setOnClickListener(this);

        initProDialog();
        initHandler();
        initSurfaceView();

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI();
            }
        };
        IntentFilter filter = new IntentFilter(PlayerService.ActionPlayingState);
        registerReceiver(receiver,filter);

    }
    private void initHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case ShowDialog:
                        proDialog.show();
                    case CloseDialog:
                        proDialog.hide();
                    case UpdateUI:
                        updateUI();
                }
            }
        };
    }

    private void updateUI(){
        Log.v(TAG,"Media player state:"+mPlayer.getState());
        state = mPlayer.getState();
        switch (state){
            case PlayerService.IdleState:
            case PlayerService.PauseState:
            case PlayerService.StopState:
                play.setText("Play");
                break;
            case PlayerService.PlayingState:
                play.setText("Pause");
                break;
        }
    }
    private void initSurfaceView() {
        proDialog.setMessage("Ready for SurfaceView...");
        surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    private void initProDialog() {
        this.proDialog = new ProgressDialog(this);
//        this.proDialog.setTitle("Progress Dialog:");
        this.proDialog.setCanceledOnTouchOutside(false);
        this.proDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        if(mPlayer!=null){
            unbindService(mConnection);
            mPlayer=null;
        }
        super.onDestroy();
    }

    private void sendMessage(int what) {
        handler.obtainMessage(what).sendToTarget();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(EnableMediaController){
            controller.show();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case  R.id.item_audio_local:
                playType = LocalMusic;
                break;
            case R.id.item_audio_net:
                playType = NetMusic;
                break;
            case R.id.item_video_local:
                playType = LocalVideo;
                break;
            case R.id.item_video_net:
                playType = NetVideo;

        }
        if(mPlayer.getState() == PlayerService.PlayingState){
            mPlayer.pause();
            mPlayer.reset();
            sendMessage(UpdateUI);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(EnableMediaController){
            return;
        }else {
            try {
                switch (v.getId()) {
                    case R.id.play:
                        if (mPlayer.getState() == PlayerService.PlayingState) {
                            Log.w(TAG, "click-play local music button:pause");
                            pause();
                        } else {
                            Log.w(TAG, "click-play local music button:play");
                            play();
                        }
                        break;
                    case R.id.Stop:
                        stop();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int currentValume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        switch (keyCode){
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentValume - 2, 1);
                return true;
            case  KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentValume + 2, 1);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void pause(){
        if(mPlayer!=null){
            mPlayer.pause();
            sendMessage(UpdateUI);
        }
    }

    private void stop(){
        if(mPlayer!=null){
            mPlayer.stop();
            sendMessage(UpdateUI);
        }
    }
    private void play() throws IOException{
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(onAudioFocusChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        if(result == AudioManager.AUDIOFOCUS_REQUEST_FAILED){

        }
        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            _play();
        }

    }
    AudioManager.OnAudioFocusChangeListener  onAudioFocusChangeListener =new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if(focusChange == AudioManager.AUDIOFOCUS_LOSS){
                pause();
                sendMessage(UpdateUI);
            }
        }
    };
    private void _play() throws  IOException{
        switch (playType){
            case LocalMusic:
                mPlayer.playLocalMusic();
                break;
            case NetMusic:
                mPlayer.playNetMusic();
                break;
            case LocalVideo:
                mPlayer.playLocalVideo(surfaceHolder);
                break;
            case NetVideo:
                mPlayer.playNetVideo();
        }
        sendMessage(UpdateUI);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(TAG, "surface has been Created...");
        Intent intent =new Intent(this, PlayerService.class);
        if(mPlayer==null){
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
