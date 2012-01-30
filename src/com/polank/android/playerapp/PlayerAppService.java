package com.polank.android.playerapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/***
 * 
 * Play data and output metadata of stream, notification in status bar and stops when phone is being called
 * Service closes when phone is called and when player is stopped.
 */

public class PlayerAppService extends Service {
	private boolean isPlaying = false;
	MediaPlayer mPlay;
	Notification notification;
	NotificationManager mNotificationManager;
	MediaMetadataRetriever myRetriever;

	EndCallListener callListener = new EndCallListener();
//	TelephonyManager mTM = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		TelephonyManager mTM = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);

		mTM.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);

		
		mPlay = new MediaPlayer();

		try {
			//mPlay.setDataSource("http://usa4-vn.mixstream.net:9270"); //fluid instrumental
			mPlay.setDataSource("http://usa7-vn.mixstream.net:8132"); //fluid vocal
			//mPlay.setDataSource("http://kexp-mp3-2.cac.washington.edu:8000/"); // kexp 128kb
			//mPlay.setDataSource("http://usa7-vn.mixstream.net/listen/8132.m3u");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// seems to keep internet connection active when screen is manually turned off 
		mPlay.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);//SCREEN_DIM_WAKE_LOCK);
		
		//this.getSystemService(this.WIFI_SERVICE);
		play();
		//metaData();
		
		return(START_NOT_STICKY);
	}
	
	private class EndCallListener extends PhoneStateListener {
	    @Override
	    public void onCallStateChanged(int state, String incomingNumber) {
	        if(TelephonyManager.CALL_STATE_RINGING == state) {
	        	stop();
	        }
	        if(TelephonyManager.CALL_STATE_OFFHOOK == state) {
	           // Log.i(LOG_TAG + "Listener", "OFFHOOK, number: " + incomingNumber);
	        }
	        if(TelephonyManager.CALL_STATE_IDLE == state) {
	            //Log.i(LOG_TAG + "Listener", "IDLE, number: " + incomingNumber);
	        }
	    }
	}


	
	
	
	private void metaData()  {
		// TODO Auto-generated method stub
		URL url = null;
		try {
			url = new URL("http://usa7-vn.mixstream.net:8132");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		IcyStreamMeta ISM = new IcyStreamMeta(url);
		
		String songName = null;
		try {
			songName = ISM.getTitle();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		myRetriever = new MediaMetadataRetriever();
		myRetriever.setDataSource("http://usa7-vn.mixstream.net/listen/8132.m3u");
		String songName = myRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
		myRetriever.release();
		
		
		if(songName == null){
			songName = mPlay.toString();
		}
		*/
		
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, songName, duration);
		toast.show();
	}

	private void ongoingStatus() {
		/*
		myRetriever = new MediaMetadataRetriever();
		myRetriever.setDataSource("http://kexp-mp3-2.cac.washington.edu:8000/");
		String songName = myRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
		myRetriever.release();
		
		if(songName == null){
			songName = "yup";
		}*/
		
		int icon = R.drawable.ic_launcher;        // icon from resources
		CharSequence tickerText = "Fluid Streaming";              // ticker-text
		long when = System.currentTimeMillis();         // notification time
		Context context = getApplicationContext();      // application Context
		CharSequence contentTitle = "Fluid Streamer";  // message title
		CharSequence contentText = "songName";//"Artist Name - Song";      // message text

		Intent notificationIntent = new Intent(this, PlayerAppActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		// the next two lines initialize the Notification, using the configurations above
		notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) getSystemService(ns);
		
		int id = 1;
		mNotificationManager.notify(id, notification);
	}

	private void play() {
		if(!isPlaying) {
			mPlay.prepareAsync();
			mPlay.setOnPreparedListener(new OnPreparedListener() {
				
				public void onPrepared(MediaPlayer mp) {
					mPlay.start();	
				}
			});
			isPlaying=true;
			ongoingStatus();
		} 
	}

	@Override
	public void onDestroy() {
		stop();
	}
	
	private void stop() {
		if(isPlaying) {
			mPlay.stop();
			isPlaying=false;
			mNotificationManager.cancel(1);
			
		}
		stopSelf();  //stop service when MediaPlayer instance stops
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
