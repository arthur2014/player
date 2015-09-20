package com.example.arthur.multimediademo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.arthur.multimediademo.player.PlayerActivity;


public class MainActivity extends Activity implements View.OnClickListener{

    private final String TAG ="MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.player).setOnClickListener(this);
        findViewById(R.id.recorder).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.player:
                Log.w(TAG, "click-player button");
                startPlayer();
                break;
            case R.id.recorder:
                Log.w(TAG, "click-recorder button");
                break;
            case R.id.audiotrack:
                startAudioTrackActivity();
        }
    }

    private  void startPlayer(){
        Intent intent =new Intent(this, PlayerActivity.class);
        startActivity(intent);
    }

    private void startAudioTrackActivity(){
        Intent intent =new Intent(this, PlayerActivity.class);
        startActivity(intent);
    }
}
