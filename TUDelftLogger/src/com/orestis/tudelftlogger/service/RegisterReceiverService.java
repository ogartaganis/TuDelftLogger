package com.orestis.tudelftlogger.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * This class registers a service to catch intent actions screen on and off.
 * This is done because it is not by default in the android manifest menu, like
 * broadcast intent receiver.
 * 
 * @author Orestis
 *
 */
public class RegisterReceiverService extends Service{

	@Override
    public void onCreate() {
        super.onCreate();
        // register receiver that handles screen on and screen off logic
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction("catch app launch");
        BroadcastReceiver mReceiver = new MyScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
