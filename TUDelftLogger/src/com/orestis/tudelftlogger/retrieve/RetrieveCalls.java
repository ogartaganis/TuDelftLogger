package com.orestis.tudelftlogger.retrieve;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.provider.CallLog;
import android.util.Log;

/**
 * Self explanatory class
 * 
 * @author Orestis
 *
 */
public class RetrieveCalls{
	
	public long retrieveTime(Context context){
		long time = 0;
		try{
			ContentResolver mcr = context.getContentResolver();
			
			String[] projection = new String[] {CallLog.Calls.DATE};
			Cursor cursor = mcr.query(
			        CallLog.Calls.CONTENT_URI, projection, null, null, null);
			cursor.moveToLast();
			try{
				time = Long.valueOf(cursor.getString(0));
			}catch(CursorIndexOutOfBoundsException c){
				// Being in here means that there are no calls in the user's mobile phone 
				// Rare, but still a possibility
				// I do no further action, return the time as null
				return time;
			}finally{
				cursor.close();
			}
		}catch(NullPointerException n) {
			Log.v("CALLS", "Second attempt for calls");
			ContentResolver mcr = context.getContentResolver();
			
			String[] projection = new String[] {CallLog.Calls.DATE};
			Cursor cursor = mcr.query(
			        CallLog.Calls.CONTENT_URI, projection, null, null, null);
			cursor.moveToLast();
			try{
				time = Long.valueOf(cursor.getString(0));
			}catch(CursorIndexOutOfBoundsException c){
				// Being in here means that there are no calls in the user's mobile phone 
				// Rare, but still a possibility
				// I do no further action, return the time as null
				return time;
			}finally{
				cursor.close();
			}
		}
		return time;
	}
}
  