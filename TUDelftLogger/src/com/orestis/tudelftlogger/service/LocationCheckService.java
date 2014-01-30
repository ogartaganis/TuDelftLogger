package com.orestis.tudelftlogger.service;

import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.orestis.tudelftlogger.UIActivity;
import com.orestis.tudelftlogger.retrieve.RetrieveLocation;

/**
 * This class is a service which will check for location
 * every 1hr approx and store in
 * the database.
 * @author Orestis
 *
 */
public class LocationCheckService extends Service{
	RetrieveLocation location = new RetrieveLocation();
	private static int ENTRY_TYPE_LOCATION = 0;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		checkExportStatus();
					
		Location retrievedLoc = location.getLocation(this);
		long timestamp = 0;
			
		try{
			timestamp = retrievedLoc.getTime();
			Log.d("Location", "Location available and about to be logged.");
		}catch(NullPointerException n){
			Log.d("Location", "Location not available yet, will repeat in one hour as planned by its service.");
			return Service.START_STICKY;
		}
			
		//****** ONLY if there IS a location, my code will reach this point.
		
		location.createMyLocation(retrievedLoc, this, ENTRY_TYPE_LOCATION, timestamp);
				
		Toast.makeText(getApplication(),
				"New Location",
				Toast.LENGTH_SHORT).show();
			
		Log.v("END", "of location service");
		return Service.START_STICKY;
	}
	
	public void checkExportStatus(){

		Calendar c1 = Calendar.getInstance();
		c1.setTimeInMillis(System.currentTimeMillis());
		//Fixing bloody month error in calendar
		int month = c1.get(Calendar.MONTH)+1;
		// Setting my "old" value to the first value, just for the first iteration
		String date = String.valueOf(
				c1.get(Calendar.DAY_OF_MONTH)+"."+
						month);
		int day = c1.get(Calendar.DAY_OF_MONTH);
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean today = sharedPrefs.getBoolean(date, false);
		if((c1.get(Calendar.HOUR_OF_DAY)>19)&&(!today)&&(day/2==0)){
			sharedPrefs.edit().putBoolean(date, true).commit();
			// Prepare intent which is triggered if the
			// notification is selected
			Intent exportIntent = new Intent(LocationCheckService.this, UIActivity.class);
			PendingIntent pIntent = PendingIntent.getActivity(this, 0, exportIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			
			// Build notification
			Notification noti = new NotificationCompat.Builder(this)
			.setContentTitle("TUD Export reminder")
			.setContentText("This is to remind you to upload latest entries to our server.")
			.setSmallIcon(android.R.drawable.ic_dialog_info)
			.setContentIntent(pIntent).build();
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			// Hide the notification after its selected
			noti.flags |= Notification.FLAG_AUTO_CANCEL;
			
			notificationManager.notify(0, noti);
		}
	}
}