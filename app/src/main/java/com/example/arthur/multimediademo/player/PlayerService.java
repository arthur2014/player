package com.example.arthur.multimediademo.player;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.arthur.multimediademo.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

public class PlayerService extends Service {
    private final String TAG = "PlayerService";
    private MediaPlayer mPlayer = null;

    public static final int PlayingState =200;
    public static final int PauseState =201;
    public static final int IdleState =203;
    public static final int StopState =204;
    private int state=IdleState;

    private SurfaceHolder viewHolder;
    private MediaController controller;
    private SurfaceView surfaceView;

    @Override
    public void onCreate() {
        super.onCreate();
        initPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void initPlayer() {

        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setOnErrorListener(errorListener);
            mPlayer.setOnPreparedListener(prepareListener);
            mPlayer.setOnCompletionListener(completedListener);
            mPlayer.setOnBufferingUpdateListener(updateListener);
            mPlayer.setOnSeekCompleteListener(seekCompleteListener);
        }

    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return new PlayerBinder();
    }
    public void playLocalMusic() throws IOException{
        if(state == IdleState) {
            Log.v(TAG, "set raw file source...");
            AssetFileDescriptor assfileDescriptor = getResources().openRawResourceFd(R.raw.music);
            FileDescriptor fileDescriptor = assfileDescriptor.getFileDescriptor();
            long offset = assfileDescriptor.getStartOffset();
            long length = assfileDescriptor.getLength();
            mPlayer.setDataSource(fileDescriptor, offset, length);
            mPlayer.prepareAsync();
            Log.v(TAG, "media player prepareAsync...");
        }else if(state == PauseState){
            mPlayer.start();
            state = PlayingState;
        }else if(state == StopState){
            mPlayer.prepareAsync();
        }
    }

    public void playNetVideo(){
//        VideoView
    }
    public void playLocalVideo(SurfaceHolder holder) throws IOException{
        if(holder == null) throw  new NullPointerException();
        viewHolder = holder;
        Log.v(TAG, "media player play local video ...");
        if(state == StopState){
            mPlayer.prepareAsync();
        }else if(state == PauseState){
            mPlayer.start();
            state = PlayingState;
        }else {
            mPlayer.setDisplay(holder);
            String storage = Environment.getExternalStorageDirectory().getAbsolutePath();
            String videoLocation = storage + "/DCIM/100MEDIA/VIDEO0001.mp4";
            File videoFile = new File(videoLocation);
            if (!videoFile.exists()) {
                Toast.makeText(this, "file is not exist ", Toast.LENGTH_LONG).show();
                return;
            }

            Uri uri = Uri.fromFile(videoFile);
            mPlayer.setDataSource(this, uri);
            mPlayer.prepareAsync();
        }
    }
    public void playNetMusic() throws IOException{
        if(state == PlayerService.IdleState){
            String url = "http://www.baidu.com?";
//            mPlayer.setDataSource(url);
//            mPlayer.prepareAsync();
        }
    }
    public int getState(){
        return state;
    }
    public void pause(){
        if(mPlayer.isPlaying()) {
            mPlayer.pause();
            state = PauseState;
        }
    }
    public void stop(){
        mPlayer.stop();
        state = StopState;
    }

    public void reset(){
        mPlayer.reset();
        state = IdleState;
    }

    public MediaPlayer getPlayer(){
        return  mPlayer;
    }
    private MediaPlayer.OnSeekCompleteListener seekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            if(!mp.isLooping()){
                mp.release();
                mp.reset();
            }
        }
    };
    private MediaPlayer.OnBufferingUpdateListener updateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            Log.v(TAG, "onBufferingUpdate was called");
        }
    };
    private MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.v(TAG, "what:" + what + "MediaPlayer:--" + mp.getTrackInfo());
            return false;
        }
    };
    private MediaPlayer.OnPreparedListener prepareListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.v(TAG,"media player has prepared...");
            mp.start();
            state = PlayingState;
            broadCastPlayingState();
            Log.v(TAG, "media player has started...");
        }
    };

    private MediaPlayer.OnCompletionListener completedListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.v(TAG, "media player has completed playing...");
            mp.release();

        }
    };

    public class PlayerBinder extends Binder{

        public PlayerService getPlayer(){
            return PlayerService.this;
        }

    }

    public static String ActionPlayingState = "com.example.mediaplayer.playing_state";
    private void broadCastPlayingState(){
        Intent intent =new Intent(ActionPlayingState);
        sendBroadcast(intent);
    }
}
