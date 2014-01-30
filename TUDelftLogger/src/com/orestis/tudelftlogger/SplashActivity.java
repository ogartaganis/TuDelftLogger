package com.orestis.tudelftlogger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;


/**
 * This is the Splash activity which is loaded when the application is invoked.
 * It then calls UIActivity!
 */
public class SplashActivity extends Activity
{
	// Set the display time, in milliseconds (or extract it out as a configurable parameter)
	private final int SPLASH_DISPLAY_LENGTH = 1500;
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		// Obtain the sharedPreference, default to true if not available
		boolean isSplashEnabled = sp.getBoolean("showsplash", true);

		if (isSplashEnabled)
		{
			new Handler().postDelayed(new Runnable()
			{
				public void run()
				{
					//Finish the splash activity so it can't be returned to.
					SplashActivity.this.finish();
					// Create an Intent that will start the main activity.
					// If the user is logged in, we go straight to the TransactionActivity.
					Intent mainIntent = new Intent(SplashActivity.this, UIActivity.class);
					SplashActivity.this.startActivity(mainIntent);
				}
			}, SPLASH_DISPLAY_LENGTH);
		}
		else
		{
			// if the splash is not enabled, then finish the activity immediately and go to main.
			finish();
			Intent mainIntent = new Intent(SplashActivity.this, UIActivity.class);
			SplashActivity.this.startActivity(mainIntent);
		}
	}
}