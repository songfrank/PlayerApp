package com.polank.android.playerapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

public class PlayerAppActivity extends Activity {
	
    Button bStartVocal, bStartInstrumental;
	ProgressDialog dialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        bStartVocal = (Button) findViewById(R.id.bStartVocal);
        bStartInstrumental = (Button) findViewById(R.id.bStartInstrumental);
        

       
     }
    
    public void dialogManager() {
        dialog = ProgressDialog.show(this, "", "Processing Stream..", true);
        
        Handler handler = null;
        handler = new Handler(); 
        handler.postDelayed(new Runnable(){ 
             public void run(){
                 dialog.cancel();
                 dialog.dismiss();
             }
        }, 5000);

    }
    
    
    public void startPlayerInstrumental(View v) {
    	dialogManager();
    	Intent i = new Intent(this,PlayerAppService.class);
    	i.setFlags(1); // attempt to pass url to service
    	
    	
    	bStartInstrumental.setEnabled(false);
    	bStartVocal.setEnabled(false);
    	

        startService(i);
      }
    
    public void startPlayerVocal(View v) {
    	dialogManager();
    	Intent i = new Intent(this,PlayerAppService.class);
    	i.setFlags(2); // attempt to pass url to service
    	
    	bStartInstrumental.setEnabled(false);
    	bStartVocal.setEnabled(false);
    	
        startService(i);
      }
    
      public void stopPlayer(View v) {
        stopService(new Intent(this, PlayerAppService.class));
       	bStartInstrumental.setEnabled(true);
    	bStartVocal.setEnabled(true);
    	//finish();
      }
      
   /*   public void homeLink (View v) {
      	Uri uri = Uri.parse("http://www.fluid-radio.co.uk/");
      	 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
      	 startActivity(intent);
      }
*/
      

}