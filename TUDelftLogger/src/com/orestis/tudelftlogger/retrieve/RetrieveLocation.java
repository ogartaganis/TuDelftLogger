package com.orestis.tudelftlogger.retrieve;

import java.util.ArrayList;
import java.util.List;

import com.orestis.tudelftlogger.database.BrowserDataSource;
import com.orestis.tudelftlogger.database.MLocation;
import com.orestis.tudelftlogger.database.MPlace;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Again, self explanatory. 
 * 
 * IMPORTANT: GPS has not been implemented as a possible provider,
 * for the purposes of this project, network accuracy was sufficient.
 * 
 * @author Orestis
 *
 */
public class RetrieveLocation implements LocationListener {
	private LocationManager locationManager;
	private String provider, providerOpt;
	double latitude;
	double longitude;
	private static int ENTRY_TYPE_LOCATION = 0;
	private static int ENTRY_TYPE_BROWSER = 1;
	private BrowserDataSource datasource;
	private static String UNDEFINED = "undefined places";
	private static String CLOSEST = "closest_";
	private static SharedPreferences sharedPrefs;

  /**
   * 
   * @param context
   * @return an object of type "Location" as exposed by the system, not my class
   * 		 Edit: my class was finally renamed MLocation
   */
  public Location getLocation(Context context){
	  Log.v("GET LOCATION", "Checking for location");
	  
	  // Get the location manager
	  locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	  sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	  
	  // Define the criteria how to select the location provider -> use
	  // default
	  Criteria criteria = new Criteria();
	  provider = locationManager.getBestProvider(criteria, true);
//	  provider = LocationManager.NETWORK_PROVIDER;
	  	  
	  locationManager.requestLocationUpdates(provider, 400, 1, this);
	  Location location;
	  location = locationManager.getLastKnownLocation(provider);
	  
//	  if(sharedPrefs.getFloat("timeLimit", 0)<20){
//		  location = locationManager.getLastKnownLocation(providerOpt);
//	  }else{
//		  location = locationManager.getLastKnownLocation(provider);
//	  }
	
	// Initialize the location fields
	  if (location != null) {
		  System.out.println("Provider " + provider + " has been selected.");
		  onLocationChanged(location);
	  } else {
		  
//		  float timeLimit = sharedPrefs.getFloat("timeLimit", 0);
//		  sharedPrefs.edit().putFloat("timeLimit", timeLimit+1).commit();
//		  System.out.println("No location found, try "+String.valueOf(timeLimit));
		  System.out.println("No location found");
	  }
	  
	  return location;
  }
  
  public long createMyLocation(Location retrievedLoc, Context context, int entryType, long timestamp){
	  	  
	  // FIRST, I see if there is any "Places" stored from the user.
	  // if there are, I check distances from those to check if the user is 
	  // closeby any of those distances.
	  String close_to = UNDEFINED;
	  float meters = -1;
	  datasource = new BrowserDataSource(context);
	  datasource.open();
	  List<MPlace> myPlaces= new ArrayList<MPlace>();
	  myPlaces = datasource.getAllPlaces();
	  if(!(myPlaces.size()==0)){
		  float minDistance = checkDistances(retrievedLoc.getLatitude(),
				  retrievedLoc.getLongitude(),
				  myPlaces.get(0).getLat(),
				  myPlaces.get(0).getLong());
		  int idX = 0;
		  if(myPlaces.size()>1){
			  for(int i=1; i<myPlaces.size();i++){
				  float distance;
				  distance = checkDistances(retrievedLoc.getLatitude(),
						  retrievedLoc.getLongitude(),
						  myPlaces.get(i).getLat(),
						  myPlaces.get(i).getLong());
				  if(distance<minDistance){
					  minDistance = distance;
					  idX = i;
				  }
			  }
		  }
		  // Now I will check if the two points are close enough (<100 meters) to be considered the same.
		  // If YES, I will fill the tag with this, closest tag. 
		  // If NOT, I will indicate which is the closest Place with adding a "closest_" prefix
		  if(minDistance<100){
			  close_to = myPlaces.get(idX).getTag();
			  meters = minDistance;
		  }else if ((minDistance>100)&&(minDistance<1000)) {
			  StringBuilder st = new StringBuilder();
			  st.append(CLOSEST);
			  st.append(myPlaces.get(idX).getTag());
			  close_to = st.toString();
			  meters = minDistance;
		  }else{
			  close_to = "other";
			  meters = minDistance;
		  }
	  }			
				
	  // Storing the location in the db
	  // Entry type 0 (location) or Entry type 1 (browser)
	  // Last field is uploaded (default value, 0)
	  MLocation newLoc = datasource.createLocation(entryType,
			  timestamp,
			  retrievedLoc.getLatitude(),
			  retrievedLoc.getLongitude(),
			  retrievedLoc.getProvider(),
			  close_to, meters, 0);
	  datasource.close();
//	  sharedPrefs.edit().putFloat("timeLimit", 0).commit();
	  locationManager.removeUpdates(this);
	  locationManager = null;
	  
	  return newLoc.getLid();
  }	
  
  private float checkDistances(double lat1, double long1, double lat2, double long2){
	  float [] results = new float[1];
      	Location.distanceBetween(lat1, long1, lat2, long2, results);
      	
      	return results[0];
  }

  @Override
  public void onLocationChanged(Location location) {
    latitude = location.getLatitude();
    longitude = location.getLongitude();
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
    // TODO Auto-generated method stub
  }

  @Override
  public void onProviderEnabled(String provider) {
	  System.out.println("Enabled new provider " + provider);

  }

  @Override
  public void onProviderDisabled(String provider) {
	  System.out.println("Disabled new provider " + provider);
  }
} 