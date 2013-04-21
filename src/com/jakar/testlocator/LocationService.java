package com.jakar.testlocator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.SmsManager;

public class LocationService extends Service{
	
	Context context;
	WakeLock wl;
	

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "lock");
		wl.acquire();
		context = this;
		final String who = intent.getStringExtra("who");
		final LocationManager locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		final LocationListener listener = new LocationListener(){

			// start location changed
			
			public void onLocationChanged(Location loc) {
				double lat = loc.getLatitude();
				double longitude = loc.getLongitude();
				if (who.equals("me")){
					Intent i = new Intent(context.getPackageName()+".LocationReceived");
					i.putExtra("lat", String.valueOf(lat));
					i.putExtra("longitude", String.valueOf(longitude));
					i.putExtra("accuracy", String.valueOf(loc.getAccuracy()));
					context.sendBroadcast(i);
					Notification notif = new Notification();
					NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
					notif.tickerText = "Location Found!";
					notif.icon = R.drawable.ic_launcher;
					notif.flags = Notification.FLAG_AUTO_CANCEL;
					notif.when = System.currentTimeMillis();
					Intent notificationIntent = new Intent(context, TestLocatorActivity.class);
					notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					notificationIntent.putExtra("lat", String.valueOf(lat));
					notificationIntent.putExtra("longitude", String.valueOf(longitude));
					notificationIntent.putExtra("accuracy", String.valueOf(loc.getAccuracy()));
					PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
					notif.setLatestEventInfo(context, "Location Found!", "Click to open.", contentIntent);
					nm.notify(0, notif);
				} else {
					SmsManager smsMan = SmsManager.getDefault();
					smsMan.sendTextMessage(who, null, "http://maps.google.com/maps?q=loc:"+lat+","+longitude, null, null);
					smsMan.sendTextMessage(who, null, "Latitude: "+lat+"\nLongitude: "+longitude, null, null);
				}
				locMan.removeUpdates(this);
				try {
					wl.release();
				} catch (Exception e){
					e.printStackTrace();
				}
				stopSelf();
			}
			
			public void onProviderDisabled(String provider){
				
		    				
			}
			
			public void onProviderEnabled(String provider) {
				//Log.i(tag, "GPS IS ON");
			}
			
			public void onStatusChanged(String provider, int status, Bundle extras){
				switch(status) {
					case LocationProvider.OUT_OF_SERVICE:
					case LocationProvider.TEMPORARILY_UNAVAILABLE:
					case LocationProvider.AVAILABLE:
						break;
				}
			} 
		
		};
		locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
		locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
		
		return 2;
	}

}
