package com.orestis.tudelftlogger.service;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This class is used to
 * 
 * 1) activate LocationCheckService, an Alarm Service 
 * which checks  every 1 hr for location updates.
 * 
 * 2) register the service that checks when the screen
 * goes on and off again (RegisterReceiverService).
 * 
 * @author Orestis
 *
 */
public class MyBootReceiver extends BroadcastReceiver {

  // Restart service every 1hr by default, or dif. set by user(?)
  private static final long REPEAT_TIME = 1000 * 3600;
//	private static final long REPEAT_TIME = 1000 * 60;

  @Override
  public void onReceive(Context context, Intent intent) {
	  
	Log.v("TEST A", "OK");
	  
	// I am going to take advantage of the received boot prompt
	// to register my screen receiver. For that I will launch 
	// another service to do.
	Intent registerReceiverService = new Intent(context, RegisterReceiverService.class);
	context.startService(registerReceiverService);
	
	Log.v("TEST B", "OK");
		  
	// Create the Alarm manager
    AlarmManager service = (AlarmManager) context
        .getSystemService(Context.ALARM_SERVICE);
    Intent i = new Intent(context, MyStartServiceReceiver.class);
    i.putExtra("service", "location");
    PendingIntent pending = PendingIntent.getBroadcast(context, 1, i,
        PendingIntent.FLAG_CANCEL_CURRENT);
    
    // Start 30 seconds after boot completed
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.SECOND, 30);
    
    // InexactRepeating allows Android to optimize the energy consumption
    service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
        cal.getTimeInMillis(), REPEAT_TIME, pending);

    // service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
    // REPEAT_TIME, pending);

  }
} 