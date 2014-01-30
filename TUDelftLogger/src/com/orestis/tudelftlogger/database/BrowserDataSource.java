package com.orestis.tudelftlogger.database;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Standard methods create/get for databases, according to category of entry.
 * The individual custom objects (MBrowser, MLocation, MPlaces) 
 * are defined in their own classes.
 * 
 * Class MySQLiteHelper is useful to translate the aliases to strings as they appear in the database.
 * 
 * @author Orestis
 *
 */
public class BrowserDataSource {
	private String DELETED = "-deleted-";

	// Database fields
			private SQLiteDatabase database;
			private MySQLiteHelper dbHelper;
			private String[] allLocationColumns = { MySQLiteHelper.COLUMN_LID,
					MySQLiteHelper.COLUMN_ENTRY_TYPE,
					MySQLiteHelper.COLUMN_LTIMESTAMP,
					MySQLiteHelper.COLUMN_LOCATION_LAT,
					MySQLiteHelper.COLUMN_LOCATION_LONG,
					MySQLiteHelper.COLUMN_PROVIDER,
					MySQLiteHelper.COLUMN_CLOSE_TO,
					MySQLiteHelper.COLUMN_METERS,
					MySQLiteHelper.COLUMN_LUPLOADED};
			private String[] allBrowserColumns = { MySQLiteHelper.COLUMN_BID,
					MySQLiteHelper.COLUMN_BLID,
					MySQLiteHelper.COLUMN_BTIMESTAMP,
					MySQLiteHelper.COLUMN_SEARCH,
					MySQLiteHelper.COLUMN_URL,
					MySQLiteHelper.COLUMN_LIGHT,
					MySQLiteHelper.COLUMN_LASTCALL,
					MySQLiteHelper.COLUMN_UPLOADED,
					MySQLiteHelper.COLUMN_INCENTIVE};
			private String[] allPlacesColumns = { MySQLiteHelper.COLUMN_PID,
					MySQLiteHelper.COLUMN_TAG,
					MySQLiteHelper.COLUMN_ADDRESS,
					MySQLiteHelper.COLUMN_PLACES_LAT,
					MySQLiteHelper.COLUMN_PLACES_LONG};
			
			
			public BrowserDataSource(Context context) {
				dbHelper = new MySQLiteHelper(context);
			}

			public void open() throws SQLException {
				database = dbHelper.getWritableDatabase();
			}

			public void close() {
				dbHelper.close();
			}
			
			
			// ****************************** LOCATION *********************************** //
						
			/**
			 * entry type: 0 (location) and 1 (browser)
			 */
			public MLocation createLocation(int entry_type, long ltimestamp, double location_lat, double location_long, String provider, String close_to, double meters, int luploaded){
				Log.v("IN","CREATE LOCATION");
				ContentValues values = new ContentValues();
				values.put(MySQLiteHelper.COLUMN_ENTRY_TYPE, entry_type);
				values.put(MySQLiteHelper.COLUMN_LTIMESTAMP, ltimestamp);
				values.put(MySQLiteHelper.COLUMN_LOCATION_LAT, location_lat);
				values.put(MySQLiteHelper.COLUMN_LOCATION_LONG, location_long);
				values.put(MySQLiteHelper.COLUMN_PROVIDER, provider);
				values.put(MySQLiteHelper.COLUMN_CLOSE_TO, close_to);
				values.put(MySQLiteHelper.COLUMN_METERS, meters);
				values.put(MySQLiteHelper.COLUMN_LUPLOADED, luploaded);
				
				long insertId = database.insert(MySQLiteHelper.TABLE_LOCATION, null, values);
				Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATION,
						allLocationColumns, MySQLiteHelper.COLUMN_LID + " = " + insertId,
						null, null, null, null);
				cursor.moveToFirst();
				MLocation newLocation = cursorToLocation(cursor);
				Log.v("OUT","CREATE LOCATION");
				cursor.close();
				return newLocation;
			}
			
			public MLocation getLocation(long lid){
				Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATION,
						allLocationColumns, MySQLiteHelper.COLUMN_LID + " = " + lid,
						null, null, null, null);
				cursor.moveToFirst();
				MLocation myLocation = cursorToLocation(cursor);
				cursor.close();
				return myLocation;
			}
			
			public Results getAllLocationDatesAndEntries(){
				Results results = new Results();
				List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
				List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
				Calendar c1 = Calendar.getInstance();
				String timestamp = null, timestamp_old = null, dayTimestamp = null;
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMinimumIntegerDigits(2);
				NumberFormat nf1 = NumberFormat.getInstance();
				nf1.setMaximumFractionDigits(3);
				
				Cursor cursor2 = database.query(MySQLiteHelper.TABLE_LOCATION,
						new String[] {MySQLiteHelper.COLUMN_LTIMESTAMP,
						MySQLiteHelper.COLUMN_CLOSE_TO,
						MySQLiteHelper.COLUMN_METERS,
						MySQLiteHelper.COLUMN_LUPLOADED,
						MySQLiteHelper.COLUMN_LID},
						MySQLiteHelper.COLUMN_ENTRY_TYPE+"=?",
						new String[] {"0"},
						null, null, null);
				cursor2.moveToFirst();
				
				// Initialize my values to the first cursor values to compare for the next step
				// My aim is to group everything in the following rule:
				// (date1) --> entry1, entry2, entry3
				// (date2) --> entry4, entry5
				// ALSO TRYING FIRST, TO AVOID Exception because of empty database!
				Long time;
				try{
					time = cursor2.getLong(0);
				}catch(RuntimeException r){
					Log.v("DATABASE", "empty..");
					return results;
				}
				
				c1.setTimeInMillis(time);
				//Fixing bloody month error in calendar
				int month = c1.get(Calendar.MONTH)+1;
				// Setting my "old" value to the first value, just for the first iteration
				timestamp_old = String.valueOf(
						c1.get(Calendar.DAY_OF_MONTH)+"."+
						month+"."+
						c1.get(Calendar.YEAR));
				
				// GROUP:
				// Creating the MAP, inserting the first timestamp and inserting it as the first value to the LIST
				Map<String, String> curGroupMap = new HashMap<String, String>();
				curGroupMap.put("DATE", timestamp_old);
				groupData.add(curGroupMap);
				// This is going to be used for the group uploaded feature
				// I set the value to 1 and if at least one of the children is NOT uploaded, it will be overwritten
				curGroupMap.put("UPLOADED", "1");
				
				// CHILDREN:
				// Creating my LIST of childMAPS, that will be stored in accordance with the first group date
				List<Map<String, String>> children = new ArrayList<Map<String, String>>();
				Log.v("TEST1", "HERE WE START");
				while(!cursor2.isAfterLast()){
					time = cursor2.getLong(0);
					c1.setTimeInMillis(time);
					//Fixing bloody month error in calendar
					month = c1.get(Calendar.MONTH)+1;
					
					timestamp = String.valueOf(
    						c1.get(Calendar.DAY_OF_MONTH)+"."+
    						month+"."+
    						c1.get(Calendar.YEAR));
					// if different,
					// GROUP: it will create a new hashmap and will be inserted in the list
					// CHILDREN: it will create a new list "children" of hashmaps and insert the first value
					if(!timestamp.equals(timestamp_old)){
						// GROUP BLOCK
						curGroupMap = new HashMap<String, String>();
						curGroupMap.put("DATE", timestamp);
						groupData.add(curGroupMap);
						// This is going to be used for the uploaded feature. Since the key is unique, then the value will be overwritten later
						if(String.valueOf(cursor2.getInt(3)).equals("0"))
							curGroupMap.put("UPLOADED", "0");
						else
							curGroupMap.put("UPLOADED", "1");
						
						// CHILDREN BLOCK
						Map<String, String> curChildMap = new HashMap<String,String>();
						// Adding either url or search..
						dayTimestamp = nf.format(c1.get(Calendar.HOUR_OF_DAY))+":"+
										nf.format(c1.get(Calendar.MINUTE));
						curChildMap.put("TIME", dayTimestamp);

						// The ID is going to be used in the functions of backtracking and deleting/exporting the entry!
						curChildMap.put("ID", String.valueOf(cursor2.getInt(4)));
						curChildMap.put("UPLOADED", String.valueOf(cursor2.getInt(3)));
						curChildMap.put("TAG", String.valueOf(cursor2.getString(1)));
						curChildMap.put("METERS", "("+nf1.format(cursor2.getFloat(2)/1000)+" km)");
						
						// I will create a new LIST of children and I will insert the new value in
						childData.add(children);  // First order of business, add the previous, complete list in my list of lists
						children = new ArrayList<Map<String, String>>(); // Then I create a fresh list
						children.add(curChildMap); // And insert the first element!
						
						timestamp_old = timestamp;
					}else{
						// If the two timestamps are the same, then add the hashmap to the existing list of hashmaps!
						Map<String, String> curChildMap = new HashMap<String,String>();
						// Adding either url or search..
						dayTimestamp = nf.format(c1.get(Calendar.HOUR_OF_DAY))+":"+
								nf.format(c1.get(Calendar.MINUTE));
						curChildMap.put("TIME", dayTimestamp);
						
						// The ID is going to be used in the functions of backtracking and deleting/exporting the entry!
						curChildMap.put("ID", String.valueOf(cursor2.getInt(4)));
						curChildMap.put("UPLOADED", String.valueOf(cursor2.getInt(3)));
						curChildMap.put("TAG", String.valueOf(cursor2.getString(1)));
						curChildMap.put("METERS", "("+nf1.format(cursor2.getFloat(2)/1000)+" km)");
						
						// Now adding this hashmap to my "children" list of hashmaps
						children.add(curChildMap);
						
						// This is going to be used for the uploaded feature. Since the key is unique, then the value is now overwritten
						if(String.valueOf(cursor2.getInt(3)).equals("0"))
							curGroupMap.put("UPLOADED", "0");
					}
//					childData.add(children);
					cursor2.moveToNext();
				}
				childData.add(children);
				cursor2.close();
				
				results.setGroup(groupData);
				results.setChildren(childData);
				
				return results;
			}
			
			public void updateUploadedLocation(long lid, int uploaded){
				ContentValues values = new ContentValues();
				values.put(MySQLiteHelper.COLUMN_LUPLOADED, uploaded);
				database.update(MySQLiteHelper.TABLE_LOCATION,
						values,
						MySQLiteHelper.COLUMN_LID+" = "+lid,
						null);
			}
			
			// Debugging purposes only
			public void updateResetUploadedLocation(){
				ContentValues values = new ContentValues();
				values.put(MySQLiteHelper.COLUMN_LUPLOADED, 0);
				database.update(MySQLiteHelper.TABLE_LOCATION,
						values,
						null,
						null);
			}
			
			private MLocation cursorToLocation(Cursor cursor){
				MLocation location = new MLocation();
				location.setLid(cursor.getLong(0));
				location.setEntryType(cursor.getInt(1));
				location.setLTimestamp(cursor.getLong(2));
				location.setLocationLat(cursor.getDouble(3));
				location.setLocationLong(cursor.getDouble(4));
				location.setProvider(cursor.getString(5));
				location.setCloseTo(cursor.getString(6));
				location.setMeters(cursor.getLong(7));
				location.setLUploaded(cursor.getInt(8));
				
				return location;
			}
			
			
			// ********************************* BROWSER *********************************** //
			
			public MBrowser createBrowser(long blid, long btimestamp, String search, String url, float light, int last_call, int uploaded, String incentive){
				ContentValues values = new ContentValues();
				values.put(MySQLiteHelper.COLUMN_BLID, blid);
				values.put(MySQLiteHelper.COLUMN_BTIMESTAMP, btimestamp);
				values.put(MySQLiteHelper.COLUMN_SEARCH, search);
				values.put(MySQLiteHelper.COLUMN_URL, url);
				values.put(MySQLiteHelper.COLUMN_LIGHT, light);
				values.put(MySQLiteHelper.COLUMN_LASTCALL, last_call);
				values.put(MySQLiteHelper.COLUMN_UPLOADED, uploaded);
				values.put(MySQLiteHelper.COLUMN_INCENTIVE, incentive);
				
				long insertId = database.insert(MySQLiteHelper.TABLE_BROWSER, null, values);
				Cursor cursor = database.query(MySQLiteHelper.TABLE_BROWSER,
						allBrowserColumns, MySQLiteHelper.COLUMN_BID + " = " + insertId,
						null, null, null, null);
				cursor.moveToFirst();
				MBrowser newBrowser = cursorToBrowser(cursor);
				cursor.close();
				return newBrowser;
			}
			
			public MBrowser getBrowser(long bid){
				Cursor cursor = database.query(MySQLiteHelper.TABLE_BROWSER,
						allBrowserColumns, MySQLiteHelper.COLUMN_BID + " = " + bid,
						null, null, null, null);
				cursor.moveToFirst();
				MBrowser myBrowser = cursorToBrowser(cursor);
				cursor.close();
				return myBrowser;
				
			}
			
			public List<MBrowser> getAllBrowsers(){
				List<MBrowser> browsers = new ArrayList<MBrowser>();
				
				Cursor cursor = database.query(MySQLiteHelper.TABLE_BROWSER,
						allBrowserColumns, null, null, null, null, null);
				
				cursor.moveToFirst();
				while(!cursor.isAfterLast()){
					MBrowser browser = cursorToBrowser(cursor);
					browsers.add(browser);
					cursor.moveToNext();
				}
				cursor.close();
				return browsers;
			}
			
			// Noooot needed, for now..
			
//			public List<String> getAllBrowserDates(){
//				HashSet<String> datesMap = new HashSet<String>();
//				Calendar c1 = Calendar.getInstance();
//				
//				/// WORK ONLY WITH LOCATION CURSOR!!! Retrieve the dates associated with 
//				
//				Cursor cursor3 = database.query(MySQLiteHelper.TABLE_BROWSER,
//						new String[] {MySQLiteHelper.COLUMN_BTIMESTAMP},
//						null,
//						null, null, null, null);
//				cursor3.moveToFirst();
//				while(!cursor3.isAfterLast()){
//					Long time = cursor3.getLong(0);
//					c1.setTimeInMillis(time);
//					//Fixing bloody month error in calendar
//					int month = c1.get(Calendar.MONTH)+1;
//					
//					String timestamp = String.valueOf(
//    						c1.get(Calendar.DAY_OF_MONTH)+"."+
//    						month+"."+
//    						c1.get(Calendar.YEAR));
//				
//					datesMap.add(timestamp);
//				
//					cursor3.moveToNext();
//				}
//				cursor3.close();
//				
//				List<String> dates = new ArrayList<String>(datesMap);
//				
//				return dates;
//				
//			}
									
			public Results getAllBrowserDatesAndEntries(){
				Results results = new Results();
				List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
				List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
				Calendar c1 = Calendar.getInstance();
				String timestamp = null, timestamp_old = null;
				
				Cursor cursor3 = database.query(MySQLiteHelper.TABLE_BROWSER,
						new String[] {MySQLiteHelper.COLUMN_BTIMESTAMP,
						MySQLiteHelper.COLUMN_SEARCH,
						MySQLiteHelper.COLUMN_URL,
						MySQLiteHelper.COLUMN_UPLOADED,
						MySQLiteHelper.COLUMN_BID},
						null,
						null, null, null, null);
				cursor3.moveToFirst();
				
				// Initialize my values to the first cursor values to compare for the next step
				// My aim is to group everything in the following rule:
				// (date1) --> entry1, entry2, entry3
				// (date2) --> entry4, entry5
				// ALSO TRYING FIRST, TO AVOID Exception because of empty database!
				Long time;
				try{
					time = cursor3.getLong(0);
				}catch(RuntimeException r){
					Log.v("DATABASE", "empty..");
					return results;
				}
				
				c1.setTimeInMillis(time);
				//Fixing bloody month error in calendar
				int month = c1.get(Calendar.MONTH)+1;
				// Setting my "old" value to the first value, just for the first iteration
				timestamp_old = String.valueOf(
						c1.get(Calendar.DAY_OF_MONTH)+"."+
						month+"."+
						c1.get(Calendar.YEAR));
				
				// GROUP:
				// Creating the MAP, inserting the first timestamp and inserting it as the first value to the LIST
				Map<String, String> curGroupMap = new HashMap<String, String>();
				curGroupMap.put("DATE", timestamp_old);
				groupData.add(curGroupMap);
				// This is going to be used for the group uploaded feature
				// I set the value to 1 and if at least one of the children is NOT uploaded, it will be overwritten
				curGroupMap.put("UPLOADED", "1");
				
				// CHILDREN:
				// Creating my LIST of childMAPS, that will be stored in accordance with the first group date
				List<Map<String, String>> children = new ArrayList<Map<String, String>>();
				Log.v("TEST1", "HERE WE START");
				while(!cursor3.isAfterLast()){
					time = cursor3.getLong(0);
					c1.setTimeInMillis(time);
					//Fixing bloody month error in calendar
					month = c1.get(Calendar.MONTH)+1;
					
					timestamp = String.valueOf(
    						c1.get(Calendar.DAY_OF_MONTH)+"."+
    						month+"."+
    						c1.get(Calendar.YEAR));
					// if different,
					// GROUP: it will create a new hashmap and will be inserted in the list
					// CHILDREN: it will create a new list "children" of hashmaps and insert the first value
					if(!timestamp.equals(timestamp_old)){
						// GROUP BLOCK
						curGroupMap = new HashMap<String, String>();
						curGroupMap.put("DATE", timestamp);
						groupData.add(curGroupMap);
						// This is going to be used for the uploaded feature. Since the key is unique, then the value will be overwritten later
						if(String.valueOf(cursor3.getInt(3)).equals("0"))
							curGroupMap.put("UPLOADED", "0");
						else
							curGroupMap.put("UPLOADED", "1");
						
						// CHILDREN BLOCK
						Map<String, String> curChildMap = new HashMap<String,String>();
						// Adding either url or search..
						if(cursor3.getString(1).equals("null")){
							curChildMap.put("TYPE", "URL");
							curChildMap.put("VALUE", cursor3.getString(2));
						}
						else{
							curChildMap.put("TYPE", "SEARCH");
							curChildMap.put("VALUE", cursor3.getString(1));
						}
						// The ID is going to be used in the functions of backtracking and deleting/exporting the entry!
						curChildMap.put("ID", String.valueOf(cursor3.getInt(4)));
						curChildMap.put("UPLOADED", String.valueOf(cursor3.getInt(3)));
						
						// I will create a new LIST of children and I will insert the new value in
						childData.add(children);  // First order of business, add the previous, complete list in my list of lists
						children = new ArrayList<Map<String, String>>(); // Then I create a fresh list
						children.add(curChildMap); // And insert the first element!
						
						timestamp_old = timestamp;
					}else{
						// If the two timestamps are the same, then add the hashmap to the existing list of hashmaps!
						Map<String, String> curChildMap = new HashMap<String,String>();
						// Adding either url or search..
						if(cursor3.getString(1).equals("null")){
							curChildMap.put("TYPE", "URL");
							curChildMap.put("VALUE", cursor3.getString(2));
						}
						else{
							curChildMap.put("TYPE", "SEARCH");
							curChildMap.put("VALUE", cursor3.getString(1));
						}
						// The ID is going to be used in the functions of backtracking and deleting/exporting the entry!
						curChildMap.put("ID", String.valueOf(cursor3.getInt(4)));
						curChildMap.put("UPLOADED", String.valueOf(cursor3.getInt(3)));
						
						// Now adding this hashmap to my "children" list of hashmaps
						children.add(curChildMap);
						
						// This is going to be used for the uploaded feature. Since the key is unique, then the value is now overwritten
						if(String.valueOf(cursor3.getInt(3)).equals("0"))
							curGroupMap.put("UPLOADED", "0");
					}
//					childData.add(children);
					cursor3.moveToNext();
				}
				childData.add(children);
				cursor3.close();
				
				results.setGroup(groupData);
				results.setChildren(childData);
				
				return results;
			}
			
				
			public void updateUploadedBrowser(long bid, int uploaded){
				ContentValues values = new ContentValues();
				values.put(MySQLiteHelper.COLUMN_UPLOADED, uploaded);
				database.update(MySQLiteHelper.TABLE_BROWSER,
						values,
						MySQLiteHelper.COLUMN_BID+" = "+bid,
						null);
			}
			
			
			// Debugging purposes only
			public void updateResetUploadedBrowser(){
				ContentValues values = new ContentValues();
				values.put(MySQLiteHelper.COLUMN_UPLOADED, 0);
				database.update(MySQLiteHelper.TABLE_BROWSER,
						values,
						null,
						null);
			}
			
			public void updateIncentive(long bid, String incentive){
				ContentValues values = new ContentValues();
				values.put(MySQLiteHelper.COLUMN_INCENTIVE, incentive);
				database.update(MySQLiteHelper.TABLE_BROWSER,
						values,
						MySQLiteHelper.COLUMN_BID +" = " +bid,
						null);
			}
			
			/**
			 * This method returns the last search
			 * @return MBrowser object containing the last entry of searches
			 */
			public MBrowser getLastBrowserSearch(){
				Cursor cursor = database.query(MySQLiteHelper.TABLE_BROWSER,
						allBrowserColumns,
						MySQLiteHelper.COLUMN_URL+"=?",
						new String[] {"null"},
						null, null, null);
								
				cursor.moveToLast();
				MBrowser browser = null;
				if(!cursor.isAfterLast()){
					browser = cursorToBrowser(cursor);
				}
				cursor.close();
				return browser;
			}
			
			/**
			 * This method returns the last stored url
			 * @return MBrowser object containing the last entry of urls
			 */
			public MBrowser getLastBrowserUrl(){
				Cursor cursor = database.query(MySQLiteHelper.TABLE_BROWSER,
						allBrowserColumns,
						MySQLiteHelper.COLUMN_SEARCH+"=?",
						new String[] {"null"},
						null, null, null);
								
				cursor.moveToLast();
				
				MBrowser browser = null;
				if(!cursor.isAfterLast()){
					browser = cursorToBrowser(cursor);
				}
				cursor.close();
				return browser;
			}
			
			
			public void whiteDeleteBrowser(long bid){
				MBrowser myBrowser = getBrowser(bid);
				ContentValues values = new ContentValues();
				if(myBrowser.getSearch().equals("null")){
					values.put(MySQLiteHelper.COLUMN_URL, DELETED);
				}
				else{
					values.put(MySQLiteHelper.COLUMN_SEARCH, DELETED);
				}
				database.update(MySQLiteHelper.TABLE_BROWSER,
						values,
						MySQLiteHelper.COLUMN_BID+" = "+bid,
						null);
			}
			
			public void deleteBrowser(int bid){
				database.delete(MySQLiteHelper.TABLE_BROWSER, MySQLiteHelper.COLUMN_BID
						+ " = " + bid, null);
			}
			
			private MBrowser cursorToBrowser(Cursor cursor){
				MBrowser browser = new MBrowser();
				browser.setBid(cursor.getLong(0));
				browser.setBlid(cursor.getLong(1));
				browser.setBTimestamp(cursor.getLong(2));
				browser.setSearch(cursor.getString(3));
				browser.setUrl(cursor.getString(4));
				browser.setLight(cursor.getFloat(5));
				browser.setLastCall(cursor.getInt(6));
				browser.setUploaded(cursor.getInt(7));
				browser.setIncentive(cursor.getString(8));
				
				return browser;
			}
			
			
			// ********************************* PLACES *********************************** //
			
			
			public MPlace createPlace(String tag, String address, double places_lat, double places_long){
				ContentValues values = new ContentValues();
				values.put(MySQLiteHelper.COLUMN_TAG, tag);
				values.put(MySQLiteHelper.COLUMN_ADDRESS, address);
				values.put(MySQLiteHelper.COLUMN_PLACES_LAT, places_lat);
				values.put(MySQLiteHelper.COLUMN_PLACES_LONG, places_long);
				
				long insertId = database.insert(MySQLiteHelper.TABLE_PLACES, null, values);
				Cursor cursor = database.query(MySQLiteHelper.TABLE_PLACES,
						allPlacesColumns, MySQLiteHelper.COLUMN_PID + " = " +insertId,
						null, null, null, null);
				cursor.moveToFirst();
				MPlace newPlace = cursorToPlace(cursor);
				cursor.close();
				return newPlace;
			}
			
			public List<MPlace> getAllPlaces(){
				List<MPlace> places = new ArrayList<MPlace>();
				
				Cursor cursor = database.query(MySQLiteHelper.TABLE_PLACES,
						allPlacesColumns, null, null, null, null, null);
				cursor.moveToFirst();
				while(!cursor.isAfterLast()){
					MPlace place = cursorToPlace(cursor);
					places.add(place);
					cursor.moveToNext();
				}
				cursor.close();
				return places;
			}
			
			public void deletePlace(MPlace place){
				long pid = place.getPid();
				database.delete(MySQLiteHelper.TABLE_PLACES, MySQLiteHelper.COLUMN_PID
						+ " = " + pid, null);
			}
			
			private MPlace cursorToPlace(Cursor cursor){
				MPlace place = new MPlace();
				place.setPid(cursor.getLong(0));
				place.setTag(cursor.getString(1));
				place.setAddress(cursor.getString(2));
				place.setLat(cursor.getDouble(3));
				place.setLong(cursor.getDouble(4));
				
				return place;
			}
			
}
