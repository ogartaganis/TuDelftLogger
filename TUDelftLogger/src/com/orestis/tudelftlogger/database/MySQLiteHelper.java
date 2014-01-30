package com.orestis.tudelftlogger.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper{

	public static final String TABLE_LOCATION = "location";
	public static final String COLUMN_LID = "lid";
	public static final String COLUMN_ENTRY_TYPE = "entry_type";
	public static final String COLUMN_LTIMESTAMP = "ltimestamp";
	public static final String COLUMN_LOCATION_LAT = "location_lat";
	public static final String COLUMN_LOCATION_LONG = "location_long";
	public static final String COLUMN_PROVIDER = "provider";
	public static final String COLUMN_CLOSE_TO = "close_to";
	public static final String COLUMN_METERS = "meters";
	public static final String COLUMN_LUPLOADED = "luploaded";
	
	public static final String TABLE_BROWSER = "browser";
	public static final String COLUMN_BID = "bid";
	public static final String COLUMN_BLID = "blid";
	public static final String COLUMN_BTIMESTAMP = "btimestamp";
	public static final String COLUMN_SEARCH = "search";
	public static final String COLUMN_URL = "url";
	public static final String COLUMN_LIGHT = "light";
	public static final String COLUMN_LASTCALL = "last_call";
	public static final String COLUMN_UPLOADED = "uploaded";
	public static final String COLUMN_INCENTIVE = "incentive";
	
	public static final String TABLE_PLACES = "places";
	public static final String COLUMN_PID = "pid";
	public static final String COLUMN_TAG = "tag";
	public static final String COLUMN_ADDRESS = "address";
	public static final String COLUMN_PLACES_LAT = "places_lat";
	public static final String COLUMN_PLACES_LONG = "places_long";
	
	public static final String DATABASE_NAME = "browser.db";
	public static final int DATABASE_VERSION = 1;
	
	// Database creation sql statement
	private static final String CREATE_TABLE_LOCATION = "create table "
			+ TABLE_LOCATION + "("
			+ COLUMN_LID + " integer primary key autoincrement, "
			+ COLUMN_ENTRY_TYPE + " integer not null, "
			+ COLUMN_LTIMESTAMP + " real not null, "
			+ COLUMN_LOCATION_LAT + " real not null, "
			+ COLUMN_LOCATION_LONG + " real not null, "
			+ COLUMN_PROVIDER + " text not null, "
			+ COLUMN_CLOSE_TO + " text not null, "
			+ COLUMN_METERS + " real not null, "
			+ COLUMN_LUPLOADED+ " integer not null);";
	
	// Database creation sql statement
	private static final String CREATE_TABLE_BROWSER = "create table "
			+ TABLE_BROWSER + "(" 
			+ COLUMN_BID + " integer primary key autoincrement, "
			+ COLUMN_BLID + " integer not null, "
			+ COLUMN_BTIMESTAMP + " real not null, "
			+ COLUMN_SEARCH + " text, "
			+ COLUMN_URL + " text, "
			+ COLUMN_LIGHT + " real not null, "
			+ COLUMN_LASTCALL +	" integer not null, "
			+ COLUMN_UPLOADED +" integer not null, "
			+ COLUMN_INCENTIVE + " text);";
	
	private static final String CREATE_TABLE_PLACES = "create table "
			+ TABLE_PLACES + "("
			+ COLUMN_PID + " integer primary key autoincrement, "
			+ COLUMN_TAG + " text not null, "
			+ COLUMN_ADDRESS + " text not null, "
			+ COLUMN_PLACES_LAT + " real not null, "
			+ COLUMN_PLACES_LONG + " real not null);";
	
	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE_LOCATION);
		database.execSQL(CREATE_TABLE_BROWSER);
		database.execSQL(CREATE_TABLE_PLACES);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_BROWSER);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);
		
		onCreate(db);
	}
}
