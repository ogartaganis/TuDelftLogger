package com.orestis.tudelftlogger.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * General utilities' class holder. 
 * 
 * IMPORTANT: Set YOUR OWN IP Address for the server.
 * 
 * @author Orestis
 *
 */
public class Utilities {
	static final String serviceIP = "1.1.1.1";  // <----- This is where the researcher must set their own IP address.
	static final String port = "5051";
	
	static final String concatStr = "&";
	static final String textStr = "text="; 
	static final String passwordStr = "password=";
	static final String userIdStr = "user_id=";
	static final String dateStr = "date=";
	static final String typeStr = "entry_type=";
	static final String codeTag = "CODE=";
	static final String messageTag = "MESSAGE=";

	/**
	 * @author orestis
	 * 
	 * @param password will be used to authenticate the incoming post to my server 
	 * @param date will be used for file name at the user's folder
	 * @param guid will be used for the creation/access of the user's folder
	 * @param myObj will be the contents of the post object, the actual data of the user's export
	 * @return The final String to be sent to the server
	 * @throws JSONException
	 * @throws UnsupportedEncodingException 
	 */
	public static String formatSendURL(String password, String date, String guid, String type, JSONObject myObj) throws JSONException, UnsupportedEncodingException {
		return passwordStr+password+
				concatStr+dateStr+date+
				concatStr+userIdStr+guid+
				concatStr+typeStr+type+
				concatStr+textStr+URLEncoder.encode(myObj.toString(1),"UTF-8");
	}
	
	public static byte[] gZipString(String stringToBeZipped){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzos = null;

		try {
		    gzos = new GZIPOutputStream(baos);
		    gzos.write(stringToBeZipped.getBytes("UTF-8"));
		}  catch (IOException ignore) {}
		finally {
		    if (gzos != null) try { gzos.close(); } catch (IOException ignore) {};
		}

		byte[] fooGzippedBytes = baos.toByteArray();
		
		return fooGzippedBytes;
	}
	
	public static String[] formatResponse(String response){
		String[] res = new String[2];
		System.out.println("RESPONSE: "+response);
		
		response = "dummy&"+response;
		
		int endIdx = response.indexOf(concatStr);
		while(endIdx != -1 && endIdx != response.length()){
			// (***)
			response = response.substring(endIdx+1);
			if(response.startsWith(codeTag)){
				endIdx = response.indexOf(concatStr);
				if(endIdx == -1){
					endIdx = response.length();
				}
				String codeStr = response.substring(0, endIdx);
				System.out.println("codeTag: "+codeStr);
				int codeSize = codeStr.length() - codeTag.length();
				res[0] = codeStr.substring(codeTag.length(), codeTag.length()+codeSize);
				System.out.println("ERROR CODE: "+res[0]);
			}else if(response.startsWith(messageTag)){
				endIdx = response.indexOf(concatStr);
				if(endIdx == -1){
					endIdx = response.length();
				}
				String messageStr = response.substring(0, endIdx);
				System.out.println("messageStr: "+messageStr);
				int messageSize = messageStr.length() - messageTag.length();
				res[1] = messageStr.substring(messageTag.length(), messageTag.length()+messageSize);
				System.out.println("ERROR MESSAGE: "+res[1]);
			}else{
				endIdx = response.indexOf(concatStr);
			}
		}
		System.out.println("TEST" + res[0]+ "TESTB" + res[1]);
		return res;
	}
	
	public static String getURLPath(Context context){
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String sendIP = sharedPrefs.getString("ipAddress", serviceIP);
		if(sendIP.equals(""))
			sendIP = serviceIP;
		String serviceURL = "http://"+sendIP+":"+port+"/upload?";
		return serviceURL;
	}	
	
    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }
  
}
