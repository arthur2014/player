package com.example.arthur.multimediademo.player;


import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.MediaController;

/**
 * Created by arthur on 15-9-15.
 */
public class PlayerController extends MediaController {
    public PlayerController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerController(Context context, boolean useFastForward) {
        super(context, useFastForward);
    }

    public PlayerController(Context context) {
        super(context);
    }

    public void setup(PlayerService player,View view){
        if(view == null || player == null) throw  new NullPointerException();
        mediaPlayer = player;
        setMediaPlayer(control);
        setAnchorView(view);
    }
    private PlayerService mediaPlayer;
    MediaPlayerControl control = new MediaPlayerControl() {
        @Override
        public void start() {
            if(mediaPlayer != null){
                if(mediaPlayer.getState() == PlayerService.IdleState ){
                    mediaPlayer.getPlayer().prepareAsync();
                }else if(mediaPlayer.getState() == PlayerService.PauseState){
                    mediaPlayer.getPlayer().start();
                }
            }
        }

        @Override
        public void pause() {
            if(mediaPlayer != null && isPlaying()){
                mediaPlayer.pause();
            }
        }

        @Override
        public int getDuration() {
            if(mediaPlayer != null && mediaPlayer.getState()!=PlayerService.IdleState){
                return  mediaPlayer.getPlayer().getDuration();
            }
            return 0;
        }

        @Override
        public int getCurrentPosition() {
            if(mediaPlayer != null && mediaPlayer.getState()!=PlayerService.IdleState){
                return mediaPlayer.getPlayer().getCurrentPosition();
            }
            return 0;
        }

        @Override
        public void seekTo(int pos) {
            if(mediaPlayer != null){
                mediaPlayer.getPlayer().seekTo(pos);
            }
        }

        @Override
        public boolean isPlaying() {
            if(mediaPlayer != null){
                return mediaPlayer.getPlayer().isPlaying();
            }
            return false;
        }

        @Override
        public int getBufferPercentage() {

            return 0;
        }

        @Override
        public boolean canPause() {
//            if(isPlaying()){
//                return true;
//            }
            return true;
        }

        @Override
        public boolean canSeekBackward() {
            return false;
        }

        @Override
        public boolean canSeekForward() {
            return false;
        }

        @Override
        public int getAudioSessionId() {
            return 0;
        }
    };
}
