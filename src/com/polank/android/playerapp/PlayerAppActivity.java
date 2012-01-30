package com.polank.android.playerapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class PlayerAppActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void startPlayer(View v) {
    	Intent i = new Intent(this, PlayerAppService.class);
        
        startService(i);
      }
      
      public void stopPlayer(View v) {
        stopService(new Intent(this, PlayerAppService.class));
      }
}