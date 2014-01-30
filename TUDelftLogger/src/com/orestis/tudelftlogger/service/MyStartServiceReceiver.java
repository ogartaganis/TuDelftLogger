package com.orestis.tudelftlogger.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyStartServiceReceiver extends BroadcastReceiver {

	  @Override
	  public void onReceive(Context context, Intent intent) {
		  Intent service;
		  if(intent.getStringExtra("service").equals("browser")){
			  //Log.v("Starting..", "browser");
			  service = new Intent(context, BrowserCheckService.class);
			  context.startService(service);
			  
		  }else if(intent.getStringExtra("service").equals("location")){
			  Log.v("Starting..", "location");
			  service = new Intent(context, LocationCheckService.class);
			  context.startService(service);
		  }
	  }
}