package com.orestis.tudelftlogger.service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.orestis.tudelftlogger.NotificationReceiverActivity;
import com.orestis.tudelftlogger.R;
import com.orestis.tudelftlogger.database.BrowserDataSource;
import com.orestis.tudelftlogger.database.MBrowser;
import com.orestis.tudelftlogger.retrieve.RetrieveCalls;
import com.orestis.tudelftlogger.retrieve.RetrieveLocation;
import com.orestis.tudelftlogger.util.Utilities;

/**
 * My main service that runs every 5 seconds, checking the browser's database.
 * 
 * Performes a check based on timestamps and if a (set of) new entry(/ies) is spotted,
 * it begins copying and gathering the necessary contextual information.
 * 
 * @author Orestis
 *
 */
public class BrowserCheckService extends Service implements SensorEventListener{
	
	// This counter will be used to catch new entries in our list
	public static int count;
	public static int FLAG_SEARCH = 0;
	public static int FLAG_URL = 1;
	public static int ENTRY_TYPE_LOCATION = 0;
	public static int ENTRY_TYPE_BROWSER = 1;
	public static int MINUTES_TO_CANCEL_NOTIFICATION = 5;
	// Calculating two months of Milliseconds, to check for clock offset time.
	private static final long OFFSET_TIME = 60 * 24 * 3600 * 1000;
	
	private BrowserDataSource datasource;
	private Toast myToast;
	private ContentProviderClient contentProviderClient;
	RetrieveLocation location;
	RetrieveCalls calls = new RetrieveCalls();
	Cursor searchCursor, urlCursor;
	private SensorManager sensorManager = null;
    private Sensor sensor = null;
    private float myLight = 0;
    private String ACT_TAG;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("BrowserCheckingService", "running.......................");
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
		
		// Creating the RetrieveLocation() object
		location = new RetrieveLocation();
		// Initializing the toast here in order to control it throughout the app; preserving resources and avoiding multiple toast messages queuing  
        myToast = Toast.makeText(getBaseContext(), "", Toast.LENGTH_SHORT);
		
        // Initializing my data source (database link) and opening it ***Remember to close it!***
		datasource = new BrowserDataSource(this);
							
		//*************************************************************************//
		//**************************** CHECKING SEARCH URI ************************//
		// I will check all timestamps of the searches URI and					   //
		// compare them to the timestamp of the last search entry				   //
		// in my database.														   //
		//*************************************************************************//

		//This is to handle events when the database is empty (first entries)
		boolean myDBempty = true;
		long lastDBSearchTime = 0;
		
		// If the database is empty, it will throw a NullPointerException so myDBempty will remain true..
		try{
			//Opening a cursor for my searches from the db and I pull the last search's timestamp
			datasource.open();
			lastDBSearchTime = datasource.getLastBrowserSearch().getBTimestamp();
			myDBempty = false;
			// ****** This checks if the system time is more than OFFSET_TIME off *********
			// If the condition is satisfied, the service is returned with no further action,
			// letting the user know as well. (Toast may be changed to notification..)
			Long tsLong = System.currentTimeMillis();
			if(Math.abs(tsLong-lastDBSearchTime)>OFFSET_TIME){
				Toast.makeText(getApplicationContext(),
						"There is something wrong with your system time, please fix your clock",
						Toast.LENGTH_SHORT).show();
				return Service.START_STICKY;
			}
		}catch(NullPointerException n){
			Log.v("Search database empty", n.toString());
			//do nothing
		}finally{
			datasource.close();
		}

		//////////////////////APPROACH 1: The use of contentResolver //////////////////////
		
//		Cursor searchCursor = getContentResolver().query(Browser.SEARCHES_URI,
//		        Browser.SEARCHES_PROJECTION, null, null,
//		        null);
//		
//		// For some reason (looking into it) Android is killing my content resolver. So if this is the case,
//		// I resurrect it in "catch"
//		try{
//			searchCursor.moveToLast();
//		}catch(NullPointerException n){
//			searchCursor = getContentResolver().query(Browser.SEARCHES_URI,
//			        Browser.SEARCHES_PROJECTION, null, null,
//			        null);
//			searchCursor.moveToLast();
//		}
		
		
		////////////////////APPROACH 2: The use of contentProviderClient //////////////////////
		// STILL Removing dead content provider. I catch this and re-ask for the browser...
		// It turns out that after the first kill, it does not do that again so, at most, 
		// browser content provider is caught on the second attempt without problems
		try {
			contentProviderClient = getContentResolver().acquireContentProviderClient(Browser.SEARCHES_URI);
			searchCursor = contentProviderClient.query(Browser.SEARCHES_URI,
		     new String[] {Browser.SearchColumns.SEARCH,
					Browser.SearchColumns.DATE}, null, null,
		     Browser.SearchColumns.DATE);
		} catch (RemoteException e) {
			Log.v("REMOTE EXCEPTION", "1st catching of browser.");
			try {
				contentProviderClient = getContentResolver().acquireContentProviderClient(Browser.SEARCHES_URI);
				searchCursor = contentProviderClient.query(Browser.SEARCHES_URI,
				 new String[] {Browser.SearchColumns.SEARCH,
						Browser.SearchColumns.DATE}, null, null,
						Browser.SearchColumns.DATE);
			} catch (RemoteException ed) {
				Log.v("REMOTE EXCEPTION", "2nd catching of browser.");
				e.printStackTrace();
			}
		e.printStackTrace();
		}
		
		String lastURISearchTime = null;
		
		// And this is a check of whether the SEARCHES_URI is empty, ie. there are no searches yet!
		// Still working with NullPointerExceptions....
		try{
			if(searchCursor.getCount()==0){
				// Just do nothing, to avoid the null exception
			}else{
				searchCursor.moveToLast();
				lastURISearchTime = searchCursor.getString(1);
			}
			searchCursor.moveToFirst();
		}catch(NullPointerException n){
			return Service.START_STICKY;
		}
		
		//********************************************************************************//
		//******* I am going to perform a series of condition checks for the storing *****//
		//********************************************************************************//
		if(myDBempty){
			if(searchCursor.getCount()==0){
				Log.v(ACT_TAG, "Nothing to store here (searches)");
				//do nothing, no entries to be stored
			}else{
				// HERE I first check whether there is a location. If there is not, I will let it try again in the next attempt
				// ( after service is re-invoked by alarm)
				if(location.getLocation(this)!=null){
					while (!searchCursor.isAfterLast()) {					
						//ok, some entries found, time to store them
						Log.v("I AM STUCK", "IN empty");
						// BINGO! STORE! Add a flag for search or url
						Log.v("SEARCH_STORE","ATTEMPT");
						store(searchCursor, FLAG_SEARCH);
						Log.v("SEARCH_STORE","SUCCESS");
						searchCursor.moveToNext();
					}
				}else{
					Log.v("STORE(search)", "No location found yet, will try in next attempt");
					// If there is no location, try again later, in my next attempt
					return Service.START_STICKY;
				}
			}
		}else{
			if(searchCursor.getCount()==0){
				Log.v(ACT_TAG, "Nothing to store here (searches)");
				//do nothing, no entries to be stored
			}else if(Long.valueOf(lastURISearchTime)<lastDBSearchTime){
				//again do nothing, this check is done to avoid checking all the other entries as well
				Log.v("Hey!", "I'm up to date!(searches)");
			}
			else
			{
				if(location.getLocation(this)!=null){
					//	HERE I first check whether there is a location or not prior to storing. Location is necessary
					//ok, some entries found, time to compare the timestamps
					// The following [] lines are checking to see how many entries are off my DB, optimizing the checks
					searchCursor.moveToLast();
					String time = searchCursor.getString(1);
					while((Long.valueOf(time)>lastDBSearchTime)&&(!searchCursor.isBeforeFirst())){
						Log.v("I AM STUCK", "IN 1");
						searchCursor.moveToPrevious();
						if(searchCursor.isBeforeFirst()){
							break;
						}
						time = searchCursor.getString(1);
						Log.v("LASTUriTime = ", time);
						Log.v("lastDBSearchTime = ", String.valueOf(lastDBSearchTime));
					}
					searchCursor.moveToNext();
					// Now I only need to store those values that I have ensured are later ones than the ones stored in my db
					while (!searchCursor.isAfterLast()) {
						Log.v("I AM STUCK", "IN 2");
						Log.v("SEARCH_STORE","ATTEMPT");
						store(searchCursor, FLAG_SEARCH);
						Log.v("SEARCH_STORE","SUCCESS");
						
						searchCursor.moveToNext();
					}
				}else{
					Log.v("STORE(search)", "No location found yet, will try in next attempt");
				}
			}
		}
		
		// ALWAYS Close the cursor(s)
		searchCursor.close();
		contentProviderClient.release();
		
		
		//*************************************************************************//
		//**************************** CHECKING URLs URI **************************//
		// I will check all timestamps of the searches URI and					   //
		// compare them to the timestamp of the last url entry				   	   //
		// in my database.														   //
		//*************************************************************************//
		long lastDBUrlTime = 0;
		String lastURIUrlTime = null;
		myDBempty = true;
		try{
			//First, opening a cursor for my searches from the db and I pull the last search
			datasource.open();
			lastDBUrlTime = datasource.getLastBrowserUrl().getBTimestamp();
			//Then I take the foreign key corresponding to the location to the location db
			myDBempty = false;
		}catch(NullPointerException n){
			Log.v("Url Database empty", n.toString());
			//do nothing
		}finally{
			datasource.close();
		}
		
		//////////////////////APPROACH 1: The use of contentResolver //////////////////////
			
//		urlCursor = getContentResolver().query(Browser.BOOKMARKS_URI,
//		        new String[] {Browser.BookmarkColumns.BOOKMARK,
//				Browser.BookmarkColumns.CREATED,
//				Browser.BookmarkColumns.DATE,
//				Browser.BookmarkColumns.URL,
//				Browser.BookmarkColumns.VISITS}, null, null,
//		        null);
		
		//////////////////////APPROACH 2: The use of contentProviderClient //////////////////////
		
		try {
			contentProviderClient = getContentResolver().acquireContentProviderClient(Browser.BOOKMARKS_URI);
			urlCursor = contentProviderClient.query(Browser.BOOKMARKS_URI,
			        new String[] {Browser.BookmarkColumns.DATE,
					Browser.BookmarkColumns.URL},
					Browser.BookmarkColumns.BOOKMARK+"=0", null, Browser.BookmarkColumns.DATE);
		} catch (RemoteException e) {
			Log.v("REMOTE EXCEPTION", "1st catching of browser.");
			e.printStackTrace();
		}
		
		
		urlCursor.moveToLast();
		if(urlCursor.getCount()==0){
			// Just do nothing, to avoid the null exception
		}else{
			lastURIUrlTime = urlCursor.getString(0);
		}
		urlCursor.moveToFirst();
		
		//********************************************************************************//
		//******* I am going to perform a series of condition checks for the storing *****//
		//********************************************************************************//
		if(myDBempty){
			if(urlCursor.getCount()==0){
				Log.v(ACT_TAG, "Nothing to store here (urls)");
				//do nothing, no entries to be stored
			}else if(location.getLocation(this)!=null){
				// HERE I first check whether there is a location or not prior to storing. Location is necessary
				//ok, some entries found, time to store them
				while (!urlCursor.isAfterLast()) {
						// Store it, success.
					Log.v("URL_STORE","ATTEMPT");
					store(urlCursor, FLAG_URL);
					Log.v("URL_STORE","SUCCESS");
					urlCursor.moveToNext();
				}
			}else{
				Log.v("STORE(url)", "No location found yet, will try in next attempt");
			}
		}else{
			if(urlCursor.getCount()==0){
				Log.v(ACT_TAG, "Nothing to store here (urls)");
				//do nothing, no entries to be stored
			}else if(Long.valueOf(lastURIUrlTime)<lastDBUrlTime){
				//again do nothing, this check is done to avoid checking all the other entries as well
				Log.v("Hey!", "I'm up to date!(urls)");
			}else if(location.getLocation(this)!=null){
				//	HERE I first check whether there is a location or not prior to storing. Location is necessary
				//ok, some entries found, time to compare the timestamps
				// The following [] lines are checking to see how many entries are off my DB, optimizing the checks
				urlCursor.moveToLast();
				String visited = urlCursor.getString(0);
				boolean flag_vis = true;
				while(!flag_vis){
					Log.v("I AM STUCK", "IN 3");
					if(Long.valueOf(visited)>lastDBUrlTime){
						flag_vis = false;
					}
					urlCursor.moveToPrevious();
					visited = urlCursor.getString(0);
				}
				
				while (!urlCursor.isAfterLast()) {
					Log.v("I AM STUCK", "IN 4");
					visited = urlCursor.getString(0);
					//String url = urlCursor.getString(1);
					Log.v("URL DB", "It's not a bookmark");
					Log.v("URL_STORE","ATTEMPT");
					store(urlCursor, FLAG_URL);
					Log.v("URL_STORE","SUCCESS");
					urlCursor.moveToNext();
				}
			}else{
				Log.v("STORE(url)", "No location found, will try in next attempt");
			}
		}
		
		// ALWAYS Close the cursor
		urlCursor.close();
		contentProviderClient.release();
		
		// Unregistering the Listener here
		sensorManager.unregisterListener(this);
		Log.v("BrowserCheckingService", ".......................finished");
		return Service.START_STICKY;
	}
	
	
	/**
	 * 
	 * @param myCur <-- the cursor containing the object to be stored
	 * @param flag <-- used to determine whether it is a search or a url stored
	 */
	private void store(Cursor myCur, int flag){
		
		//*********************************************************************//
		// ******************** First I create my location entry,**************//
		//*********************************************************************//
		//in order to get the _id and 
		// store it as a foreign key to the browser database
		Location retrievedLoc = location.getLocation(this);
		// long timestamp = retrievedLoc.getTime();
		Long tsLong = System.currentTimeMillis();
				
		//Timestamp timestamp = new Timestamp(retrievedLoc.getTime());
		
		// Here I store the LOCATION for the entry 
		// Entry type 1 (browser)
		Log.v("TIMESTAMP value =", String.valueOf(tsLong));
		Log.v("LATITUDE value =", String.valueOf(retrievedLoc.getLatitude()));
		Log.v("LONGITUDE value =", String.valueOf(retrievedLoc.getLongitude()));
		Log.v("PROVIDER value =", String.valueOf(retrievedLoc.getProvider()));
					
		// Now that I got my location stored, I will use its key inserted to my new entry
		Long blid = location.createMyLocation(retrievedLoc, this, ENTRY_TYPE_BROWSER, tsLong);
		
		String search, url;
		
		if (flag==FLAG_SEARCH){
			search = myCur.getString(0);
			if(isHighPrivacy()){
				try {
					search = Utilities.SHA1(search);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			url = "null";
		}else{
			search = "null";
			url = myCur.getString(1);
			if(isHighPrivacy()){
				try {
					url = Utilities.SHA1(url);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		
		int last_call = 0;
		// Now that I got the time of the last call, I'm gonna check if it is larger than 3 mins, plus if the time offset is correct..  
		if((tsLong-(calls.retrieveTime(this))<180000)&&(tsLong-(calls.retrieveTime(this))>0)){
			// We're in, that means that the last call is considered to be
			// a possible correlation.
			last_call = 1;
		}
		int uploaded = 0;
		String incentive = null;
		
		//*********************************************************************//
		// ************* Here we store the BROWSER entry **********************//
		//*********************************************************************//
		datasource.open();
		MBrowser mbrowser = datasource.createBrowser(blid, tsLong, search, url, myLight, last_call, uploaded, incentive);
		datasource.close();

		// So, if this procedure has been caused by a search, then 
		if(flag==FLAG_SEARCH){
			myToast.setText("New ENTRY! Please set your search Topic from the Notification bar.");
			myToast.setDuration(Toast.LENGTH_LONG);
			myToast.show();
			createNotification(mbrowser.getBid(),search);
			Log.v("INTENT CREATION for search(id):", mbrowser.getBid()+"");
			Log.v("INTENT CREATION for search:", search);
		}else{
//			myToast.setText("New ENTRY!");
//			myToast.setDuration(Toast.LENGTH_SHORT);
//			myToast.show();
		}
		
	}
	 
	private boolean isHighPrivacy(){
		// I use the sharedpreferences to decide whether to cypher the value or not..
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String privacySetting = sp.getString("privacy", "1");
		
		if(privacySetting.equals("9")){
			return true;
		}else{
			return false;
		}
	}
	
	private void createNotification(long bid, String searchText){
		// Prepare intent which is triggered if the
	    // notification is selected
		Intent intent = new Intent(BrowserCheckService.this, NotificationReceiverActivity.class);
		intent.putExtra("search_id", bid);
		intent.putExtra("search_text", searchText);
		Log.v("Intent TEXT", searchText);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

	    // Build notification
	    Notification noti = new NotificationCompat.Builder(this)
	        .setContentTitle("Search intent")
	        .setContentText("Could you please specify your search topic?")
	        .setSmallIcon(R.drawable.ic_menu_help1)
	        .setContentIntent(pIntent)
	        .addAction(R.drawable.btn_check, "Call", pIntent).build();
	    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	    // Hide the notification after its selected
	    noti.flags |= Notification.FLAG_AUTO_CANCEL;

	    notificationManager.notify(0, noti);

	    
	    // I create the alarm manager which will call the class to dismiss my notification, 
	    // after 5 minutes it was created.
	    // Create the Alarm manager
	    AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    Intent i = new Intent(this, NotificationCancelService.class);
	    
	    PendingIntent pending = PendingIntent.getService(this, 1, i, PendingIntent.FLAG_ONE_SHOT);
	    
	    Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.MINUTE, MINUTES_TO_CANCEL_NOTIFICATION);
	    
	    alarm.set(AlarmManager.RTC, cal.getTimeInMillis(), pending);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		myLight = arg0.values[0];
	}
}
