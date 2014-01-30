package com.orestis.tudelftlogger.service;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This receiver handles the "Screen off - on" actions
 * and launches the corresponding services 
 * (BrowserCheckService)
 * 
 * Puts as a parameter in the intent
 *  the boolean value of screenOff or not
 * 
 * @author Orestis
 *
 */
public class MyScreenReceiver extends BroadcastReceiver {
	 
    // Restart service every 5 seconds
    private static final long REPEAT_TIME = 1000 * 5;
 
    @Override
    public void onReceive(Context context, Intent intent) {
                
        // Create the Alarm manager, according to
    	// user preferences repeat rate
        AlarmManager alarmService = (AlarmManager) context
            .getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, MyStartServiceReceiver.class);
        i.putExtra("service", "browser");
        //context.startService(i);
        PendingIntent pending = PendingIntent.getBroadcast(context, 2, i,
            PendingIntent.FLAG_CANCEL_CURRENT);
        
        // Start 1 second after received
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 1);
        
//        // InexactRepeating allows Android to optimize the energy consumption
//        alarmService.setInexactRepeating(AlarmManager.RTC_WAKEUP,
//            cal.getTimeInMillis(), REPEAT_TIME, pending);
        
        
        // At this point I check what kind of action screen signal I got..
        // .. and act accordingly.
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
        	Log.v("BROWSER CHECK", "ON");
        	
        	// Fetch every "REPEAT_TIME" seconds
            // InexactRepeating allows Android to optimize the energy consumption
            alarmService.setInexactRepeating(AlarmManager.RTC_WAKEUP,
            	cal.getTimeInMillis(), REPEAT_TIME, pending);
            
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
        	Log.v("BROWSER CHECK", "OFF");
        	alarmService.cancel(pending);
        } else if (intent.getAction().equals("catch app launch")){
        	if(intent.getBooleanExtra("check", false)){
        		Log.v("SUCCESS!", "BROWSER CHECK IS AGAIN ON");
        		alarmService.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    	cal.getTimeInMillis(), REPEAT_TIME, pending);
        	}else{
	        	Log.v("SUCCESS!", "BROWSER CHECK IS NOW OFF");
	        	alarmService.cancel(pending);        		
        	}
        		
        }
        
    }
 
}