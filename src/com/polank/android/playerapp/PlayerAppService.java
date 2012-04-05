package com.polank.android.playerapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;



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
	URL url;
	CharSequence tickerText;
	CharSequence contentTitle;
	int icon = R.drawable.ic_launcher;
	int id = 1; // unique notification id
	Context context;
	CharSequence contentText;
	long when;
	PendingIntent contentIntent;

	private int m_interval = 60000; 
	private Handler m_handler;

	IcyStreamMeta ISM;
	String songName;
	String artistName;


	EndCallListener callListener = new EndCallListener();
//	TelephonyManager mTM = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = getApplicationContext(); 
				
		m_handler = new Handler();
		
		
		TelephonyManager mTM = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
		mTM.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
		
		
		if(intent.getFlags() == 1) {
			try {
				url = new URL("http://usa4-vn.mixstream.net:9270");
				tickerText = "Fluid Instrumental";
				contentTitle = "Fluid Instrumental"; 
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		else if (intent.getFlags() == 2) {
			try {
				url = new URL("http://usa7-vn.mixstream.net:8132");
				tickerText = "Fluid Vocal";              
				contentTitle = "Fluid Vocal";  
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		
		ISM = new IcyStreamMeta(url);


		
		Intent notificationIntent = new Intent(this, PlayerAppActivity.class);
		contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		when = System.currentTimeMillis();         // notification time, not being passed to notification
		notification = new Notification(icon, tickerText, when);
			
		mPlay = new MediaPlayer();
		try {
			mPlay.setDataSource(url.toString()); 
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// seems to keep internet connection active when screen is manually turned off 
		mPlay.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);//SCREEN_DIM_WAKE_LOCK);
		play();
		
		
		//this.getSystemService(this.WIFI_SERVICE); // thought about fixing wifi to 3g vv stream stop
		
		return(START_NOT_STICKY); // purpose?
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

	Runnable ongoingStatus = new Runnable() {
			public void run() {
			try {
				ISM.refreshMeta();
				songName = ISM.getTitle();
				artistName = ISM.getArtist();
			} catch (IOException e) {
				e.printStackTrace();
				songName = "err";
				artistName = "err";
			}
			
			contentText = artistName + " - " + songName;  // message text
			
			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			
			notification.flags = Notification.FLAG_ONGOING_EVENT;
			String ns = Context.NOTIFICATION_SERVICE;
			mNotificationManager = (NotificationManager) getSystemService(ns);
			
			mNotificationManager.notify(id, notification);
			

			m_handler.postDelayed(ongoingStatus, m_interval);

			}
	};
	
	private void play() {
		if(!isPlaying) {
			mPlay.prepareAsync();
			mPlay.setOnPreparedListener(new OnPreparedListener() {
				
				public void onPrepared(MediaPlayer mp) {
					mPlay.start();	
				}
			});
			isPlaying=true;
			ongoingStatus.run();
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
			m_handler.removeCallbacks(ongoingStatus);
		}
		stopSelf();  //stop service when MediaPlayer instance stops
	}

	// This code appears to serve no purpose
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
