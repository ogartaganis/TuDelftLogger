package com.orestis.tudelftlogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.orestis.tudelftlogger.database.BrowserDataSource;
import com.orestis.tudelftlogger.database.MBrowser;
import com.orestis.tudelftlogger.database.MLocation;
import com.orestis.tudelftlogger.database.Results;
import com.orestis.tudelftlogger.util.Utilities;


/**
 * This is the main UI class of the application. It is what is displayed after the splash (logo) screen.
 * It handles a 
 * 
 **/
public class UIActivity extends ExpandableListActivity{
	private static final String DATE = "DATE";
	private static final String VALUE = "VALUE";
	private static final String TYPE = "TYPE";
	private static final String UPLOADED = "UPLOADED";
	private static final String ID = "ID";
	private static final String TYPE_BROWSER = "browser";
	private static final int MENU_UPLOAD = 0;
	private static final int MENU_DELETE = 1;
	private static final int MENU_UPLOAD_GROUP = 2;
	private static final int OPTIONS_MENU_PREFS = 11;
	private static final int OPTIONS_MENU_PLACES = 12;
	private static final int OPTIONS_MENU_ABOUT = 13;
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";
	private static final String PASSWORD = "12345678";
	private BaseExpandableListAdapter mAdapter;
	private BrowserDataSource datasource;
	private Results results;
	private List<Map<String, String>> groupData;
	private List<List<Map<String, String>>> childData;
	Button buttonCheck;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_ui);
	    
	    final LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
	    getStarted();
	      	    
	    //****************************************//
	    // 1) Querying the database
	    results = new Results();
	    datasource = new BrowserDataSource(this);
		datasource.open();
	    
		// Now fetching all my data from the database
	    results = datasource.getAllBrowserDatesAndEntries();
	    
	    // Always close database
	    datasource.close();
	    groupData = new ArrayList<Map<String, String>>();
	    childData = new ArrayList<List<Map<String, String>>>();
 
		groupData = results.getGroup();
		childData = results.getChildren();
	       
		//***************************************//
		
		// 2) Building the adapter with the data 
	    mAdapter = new SimpleExpandableListAdapter(
	    		this,
	            groupData,
	            0,
	            null,
	            new int[] { },
	            childData,
	            0,
	            null,
	            new int[] { }
	            ){
		            @Override
		            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		                final View v = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
		
		                // Populate your custom view here
		                ((ImageView)v.findViewById(R.id.image_type)).setImageDrawable( (Drawable) (((Map<String,String>)getChild(groupPosition, childPosition)).get(TYPE).equals("URL")?getResources().getDrawable(R.drawable.url_small):getResources().getDrawable(R.drawable.search_small)) );
		                ((TextView)v.findViewById(R.id.text)).setText( (String) ((Map<String,String>)getChild(groupPosition, childPosition)).get(VALUE) );
		                ((ImageView)v.findViewById(R.id.image_uploaded)).setImageDrawable( (Drawable) (((Map<String,String>)getChild(groupPosition, childPosition)).get(UPLOADED).equals("0")?getResources().getDrawable(R.drawable.not_yet_small):getResources().getDrawable(R.drawable.btn_check)));
		
		                return v;
		            }
		
		            @Override
		            public View newChildView(boolean isLastChild, ViewGroup parent) {
		                 return layoutInflater.inflate(R.layout.row_children_layout, null, false);
		            }
		            
		            @Override
		            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		            	final View v = super.getGroupView(groupPosition, isExpanded, convertView, parent);
		            	
		            	// Populate my custom view here
		            	((TextView)v.findViewById(R.id.text_group)).setText( (String) ((Map<String,String>)getGroup(groupPosition)).get(DATE) );
		            	((ImageView)v.findViewById(R.id.image_uploaded)).setImageDrawable( (Drawable) (((Map<String,String>)getGroup(groupPosition)).get(UPLOADED).equals("0")?getResources().getDrawable(R.drawable.not_yet):getResources().getDrawable(R.drawable.btn_check)));
		            	
		            	return v;
		            }
		            
		            @Override
		            public View newGroupView(boolean isExpanded, ViewGroup parent){
		            	return layoutInflater.inflate(R.layout.row_group_layout, null, false);
		            }
		           
		        };
	    
		setListAdapter(mAdapter);
	    registerForContextMenu(this.getExpandableListView());
	    
	    // This button will implement the functionality of uploading all non-uploaded values
	    buttonCheck = (Button) findViewById(R.id.buttonCheck);
	    buttonCheck.setOnClickListener(new OnClickListener() {
        	public void onClick(View arg0) {
        		if(isNetworkAvailable()){
        			Dialog myDialog = myTextDialog();
            		myDialog.show();
        		}else{
                	Toast.makeText(UIActivity.this, 
                			getString(R.string.check_network_connection),
                			Toast.LENGTH_SHORT).show();
                }
        	}
        });
	    buttonCheck.setEnabled(false);
	    buttonCheck.setText(getString(R.string.button_no_new_entries));
	    for(int j=0; j<mAdapter.getGroupCount();j++){
    		String uplG =(String) ((Map<String,String>)mAdapter.getGroup(j)).get(UPLOADED);
			if(uplG.equals("0")){
				buttonCheck.setEnabled(true);
				buttonCheck.setText(getString(R.string.button_upload_all));
			}
	    }
	    
	    Button buttonLocation = (Button) findViewById(R.id.buttonLocation);
	    buttonLocation.setOnClickListener(new OnClickListener() {
        	public void onClick(View arg0) {
        		startActivity(new Intent(UIActivity.this, UILocationActivity.class));
        	}
        });
	 }
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)	{
		super.onCreateContextMenu(menu, v, menuInfo);
		
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
		// First create a context menu for child items
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) 
		{
			String uplC =(String) ((Map<String,String>)mAdapter.getChild(group, child)).get(UPLOADED);
			if(uplC.equals("0")){
				// Array created earlier when we built the expandable list
				String page =(String) ((Map<String,String>)mAdapter.getChild(group, child)).get(VALUE);
				menu.setHeaderTitle(page);
				// Here I omit the menu if the entry is already uploaded. Do I want that as a user?
				menu.add(0, MENU_UPLOAD, 0, "Upload entry");
				menu.add(0, MENU_DELETE, 0, "Delete entry");
			}
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP)
		{
			String uplG =(String) ((Map<String,String>)mAdapter.getGroup(group)).get(UPLOADED);
			if(uplG.equals("0")){
				String page = (String) ((Map<String,String>)mAdapter.getGroup(group)).get(DATE);
				menu.setHeaderTitle(page);
				menu.add(0, MENU_UPLOAD_GROUP, 0, "Upload whole day");
			}
		}
	}
	
	public boolean onContextItemSelected(final MenuItem menuItem) {
	  ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuItem.getMenuInfo();

	  // I am going to use this array to pass an array of uploaded elements in order to simplify
	  // the procedure of updating my databases' entries' field "uploaded" for the proper elements
	  int[] idS;

	  int groupPos = 0, childPos = 0;

	  groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
	  childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
 
	  // Pull values from the array we built when we created the list
//	  String author = (String) ((Map<String,String>)mAdapter.getGroup(groupPos)).get(DATE);
	  
//	  rowId = Integer.parseInt(mListStringArray[groupPos][childPos * 3 + 3]);
	  String date = (String) ((Map<String,String>)mAdapter.getGroup(groupPos)).get(DATE);

	  switch (menuItem.getItemId()) 
	  {
	  	// When the user chooses to upload one entry 
	  	case MENU_UPLOAD:
	  		if(!isNetworkAvailable()){
	    		Toast.makeText(UIActivity.this, 
            			getString(R.string.check_network_connection),
            			Toast.LENGTH_SHORT).show();
	    		return true;
	    	}
	      int id = Integer.parseInt(((String) ((Map<String,String>)mAdapter.getChild(groupPos, childPos)).get(ID)));
	      idS = new int[] {id};
	      // Invoke my class to execute in an Asynchronous way the operation
	      SendJSONObjectOperationSingle mAsync = new SendJSONObjectOperationSingle();
	      mAsync.myObj = writeJSONBrowserSingleDate(id);
	      mAsync.date = date;
	      mAsync.idS = idS;
	      mAsync.finished = true;
	      mAsync.execute();
	      
	      return true;

	    //When the user chooses to delete an entry
	    case MENU_DELETE:
	    	// Builder to warn the user for entry deletion!
			AlertDialog.Builder builder = new AlertDialog.Builder(UIActivity.this);
	        builder.setIcon(android.R.drawable.ic_dialog_alert);
	        builder.setTitle(R.string.caution);
	        builder.setMessage(R.string.confirm_entry_deletion_message);
	        builder.setPositiveButton(R.string.yes, new Dialog.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuItem.getMenuInfo();

	            	int groupPos = 0, childPos = 0;

	            	int type = ExpandableListView.getPackedPositionType(info.packedPosition);
	            	if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) 
	            	{
	            		groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
	            		childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
	            	}
	            	String page = (String) ((Map<String,String>)mAdapter.getChild(groupPos, childPos)).get(VALUE) ;
	            	Toast.makeText(getApplicationContext(), "DELETE entry: "+page, Toast.LENGTH_SHORT).show();
	      	      	datasource.open();
//	      	      	datasource.deleteBrowser(Integer.parseInt(((String) ((Map<String,String>)mAdapter.getChild(groupPos, childPos)).get(ID))));
	      	      	datasource.whiteDeleteBrowser(Integer.parseInt(((String) ((Map<String,String>)mAdapter.getChild(groupPos, childPos)).get(ID))));
	      	      	datasource.close();
	      	      
	      	      	refreshLists();
	            }
	        });
	        builder.setNegativeButton("Cancel", new Dialog.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	// Do nothing.
	            }
	        });
	        builder.show();
	      return true;

	    // When the user chooses to upload the whole day:
	    case MENU_UPLOAD_GROUP:
	    	if(!isNetworkAvailable()){
	    		Toast.makeText(UIActivity.this, 
            			getString(R.string.check_network_connection),
            			Toast.LENGTH_SHORT).show();
	    		return true;
	    	}
	    	ArrayList<JSONObject> jsonObjectChildren = new ArrayList<JSONObject>();
	    	JSONObject jsonObjectGroup = new JSONObject();
	    	idS = new int[mAdapter.getChildrenCount(groupPos)];
	    	for(int i = 0; i<mAdapter.getChildrenCount(groupPos);i++){
	    		int idC = Integer.parseInt(((String) ((Map<String,String>)mAdapter.getChild(groupPos, i)).get(ID)));
	    		
	    		// I am filling my idS array with the ids of the elements to be uploaded
	    		// EDIT: I will fill the ids of the elements to be uploaded
	    		String uplC =(String) ((Map<String,String>)mAdapter.getChild(groupPos, i)).get(UPLOADED);
				if(uplC.equals("0")){
					idS[i] = idC;
					try {
						jsonObjectGroup.accumulate(date, writeJSONBrowserSingleDate(idC));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
					idS[i] = 0;
	    		
//	    		jsonObjectChildren.add(writeJSONBrowser(idC));
	    	}
	    	// Invoke my class to execute in an Asynchronous way the operation
	    	SendJSONObjectOperationSingle mAsyncG = new SendJSONObjectOperationSingle();
		    mAsyncG.myObj = jsonObjectGroup;
		    mAsyncG.date = date;
		    mAsyncG.idS = idS;
		    mAsyncG.finished = true;
		    mAsyncG.execute();
	      return true;
	    default:
	      return super.onContextItemSelected(menuItem);
	  }
	  
	}
	
	public void refreshLists(){
		Log.v("SIZE BEFOREg", String.valueOf(groupData.size()));
		Log.v("SIZE BEFOREc", String.valueOf(childData.get(0).size()));
		groupData.clear();
		childData.clear();
		
			    
		Log.v("SIZE INBETWEENg", String.valueOf(groupData.size()));
//		Log.v("SIZE INBETWEENc", String.valueOf(childData.get(0).size()));
		// Now fetching all my data from the database
		datasource.open();
		Results results1 = new Results();
		results1 = datasource.getAllBrowserDatesAndEntries();
	    datasource.close();
	    
	    groupData.addAll(results1.getGroup());
	    childData.addAll(results1.getChildren());
//		groupData = results1.getGroup();
//		childData = results1.getChildren();
		
		Log.v("SIZE AFTERg", String.valueOf(groupData.size()));
		Log.v("SIZE AFTERc", String.valueOf(childData.get(0).size()));
		
		runOnUiThread(new Runnable() {
		    public void run() {
		    	mAdapter.notifyDataSetChanged();
		    }
		});
	}
	
	//*******************************************************************//
	// ***************** JSONOBject - related methods *******************//
	//*******************************************************************//
		
	public JSONObject writeJSONBrowserSingleDate(int bid) {
		
		JSONObject objectBrow = new JSONObject();
		JSONObject objectLoc = new JSONObject();
		
		datasource.open();
		MBrowser mbrowser = datasource.getBrowser(bid);
		MLocation mlocation = datasource.getLocation(mbrowser.getBlid());
		datasource.close();
		
		try {
			objectBrow.put("timestamp", mbrowser.getBTimestamp());
			objectBrow.put("url", mbrowser.getUrl());
			objectBrow.put("search", mbrowser.getSearch());
			objectBrow.put("last_call", mbrowser.getLastCall());
			objectBrow.put("light", mbrowser.getLight());
			objectBrow.put("incentive", mbrowser.getIncentive());
			if(!isHighPrivacy())
				objectLoc.put("coordinates", mlocation.getLocationLat()+"/ "+mlocation.getLocationLong());
			objectLoc.put("close_to", mlocation.getCloseTo());
			objectLoc.put("meters", mlocation.getMeters());
			objectLoc.put("provider", mlocation.getProvider());
			objectBrow.put("location", objectLoc);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			System.out.println(objectBrow.toString(0));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objectBrow;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_ui, menu);
        menu.add(0, OPTIONS_MENU_PREFS, 0, "Preferences").setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, OPTIONS_MENU_PLACES, 0, "My Places").setIcon(android.R.drawable.ic_menu_myplaces);
        menu.add(0, OPTIONS_MENU_ABOUT, 0, "About").setIcon(android.R.drawable.ic_menu_info_details);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
          case OPTIONS_MENU_PREFS:
             startActivity(new Intent(this, Preferences.class));
             break;
          case OPTIONS_MENU_PLACES:
        	  startActivity(new Intent(this, PlacesActivity.class));
        	  break;
          case OPTIONS_MENU_ABOUT:
        	  startActivity(new Intent(this, AboutActivity.class));
        	  break;
       }
       return false;
    }
	
	private boolean isHighPrivacy(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String privacySetting = sp.getString("privacy", "1");
		
		if(privacySetting.equals("9")){
			return true;
		}else{
			return false;
		}
	}
	
	// Fast Implementation - Self-explanatory to read the results from the server
	private StringBuilder inputStreamToString(InputStream is) throws IOException {
	    String line = "";
	    StringBuilder total = new StringBuilder();
	    
	    // Wrap a BufferedReader around the InputStream
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    // Read response until the end
	    while ((line = rd.readLine()) != null) { 
	        total.append(line); 
	    }
	    
	    // Return full string
	    return total;
	}
	
	/**
     * Isolating this to overcome NullPointerException()
     * @return Dialog to be shown
     */
    private Dialog myTextDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(UIActivity.this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(R.string.confirm);
        builder.setMessage(R.string.confirm_total_upload_message);
        builder.setPositiveButton(R.string.yes, new Dialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	// Invoke my class to execute in an Asynchronous way the operation
        		SendJSONObjectOperationAll mAsyncG = new SendJSONObjectOperationAll();
        		mAsyncG.execute();
        		}
        });
        builder.setNegativeButton("Cancel", new Dialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	// Do nothing.
            }
        });
        return builder.create();
     }
    
    private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null;
	}
    
    @Override
    public void onResume(){
    	// I want to send an intent here to stop the browser check service because
    	// 	its database access collides with my application access and database is locked
    	Intent sideIntent = new Intent();
		sideIntent.setAction("catch app launch");
		sideIntent.putExtra("check", false);
		sendBroadcast(sideIntent);
		
    	super.onResume();
    }
    @Override
    public void onPause(){
    	// I want to send an intent here to stop the browser check service because
    	// 	its database access collides with my application access and database is locked
    	Intent sideIntent = new Intent();
		sideIntent.setAction("catch app launch");
		sideIntent.putExtra("check", true);
		sendBroadcast(sideIntent);
		
    	super.onResume();
    }
    
    /**
	 * @author orestis
	 *
	 *	The Class that will be used to perform the Asynchronous operation
	 *	of exporting the user's data to the server, for the choice of
	 * 	"upload all latest" entries. First we check which entries need
	 * 	to be uploaded and then we perform the upload and corresponding 
	 * 	entries in my database update.
	 *	
	 */
	private class SendJSONObjectOperationAll extends AsyncTask<String, Void, String>{
		private ProgressDialog pd;
		private volatile boolean boolresult = false;
		String serviceURL;
		String resultString;
	    String[] resultArray;
	    private JSONObject jsonObjectGroup;
	    private String date;
	    private int[] idS;
	    private boolean finished;
	    
		protected void onPreExecute() {
			pd = new ProgressDialog(UIActivity.this);
			pd.setTitle("Uploading all new entries");
			pd.setMessage(getResources().getString(R.string.progress_message));
			pd.setIndeterminate(true);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setCancelable(true);
			pd.setOnCancelListener(new OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	                // actually could set running = false; right here, but I'll
	                // stick to contract.
	                cancel(true);
	            }
	        });
			pd.show();
		}
		
		protected void onCancelled(){
			// Nothin'.. just cancel and return to previous screen
		}
		
		@Override
		protected String doInBackground(String... param){
//			Repeatedly sending data until done
			for(int j=0; j<mAdapter.getGroupCount();j++){
        		String uplG =(String) ((Map<String,String>)mAdapter.getGroup(j)).get(UPLOADED);
    			if(uplG.equals("0")){
    				date = (String) ((Map<String,String>)mAdapter.getGroup(j)).get(DATE);
    				
    				// ********************************************************************* //
    				// The behaviour (and code) from now on is the same as "MENU_UPLOAD_GROUP"
    				// The way we operate here is that we iterate each date to see which days need to be uploaded
    				// and then we follow the same way as in the "MENU_UPLOAD_GROUP" 
    				// to add which entries need to be uploaded from that date
    				ArrayList<JSONObject> jsonObjectChildren = new ArrayList<JSONObject>();
    		    	jsonObjectGroup = new JSONObject();
    		    	idS = new int[mAdapter.getChildrenCount(j)];
    		    	for(int i = 0; i<mAdapter.getChildrenCount(j);i++){
    		    		int idC = Integer.parseInt(((String) ((Map<String,String>)mAdapter.getChild(j, i)).get(ID)));
    		    		
    		    		// I am filling my idS array with the ids of the elements to be uploaded
    		    		// EDIT: I will fill the ids of the elements to be uploaded
    		    		String uplC =(String) ((Map<String,String>)mAdapter.getChild(j, i)).get(UPLOADED);
    					if(uplC.equals("0")){
    						idS[i] = idC;
    						try {
    							jsonObjectGroup.accumulate(date, writeJSONBrowserSingleDate(idC));
    						} catch (JSONException e) {
    							// TODO Auto-generated catch block
    							e.printStackTrace();
    						}
    					}
    					else
    						idS[i] = 0;
    		    		
//    		    		jsonObjectChildren.add(writeJSONBrowser(idC));
    		    	}
    			
    			
	    			// Retrieving my device's uid
					TelephonyManager tManager = (TelephonyManager)UIActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
					String uid = tManager.getDeviceId();
	
					 // I create the string entity in order to embed it to the httppost
				    StringEntity se = null;
					try {
						try {
							se = new StringEntity(Utilities.formatSendURL(PASSWORD, date, uid, TYPE_BROWSER, jsonObjectGroup));
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
	//				Log.v("UID", uid);
	//				Log.v("URL", serviceURL);
					
					// Following 3 lines intended to boost connection speed
					// retrieved snippet from
					// http://stackoverflow.com/questions/3046424/http-post-requests-using-httpclient-take-2-seconds-why
					HttpParams params = new BasicHttpParams();
					params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
					DefaultHttpClient httpclient = new DefaultHttpClient(params);
					
					// **********************************************************************//
					// ************************ Code from Google I/O app: *******************//
					// ********************* http://code.google.com/p/iosched/ **************//
					// ******************* Adds gzip compression to the message *************//
					httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
						  public void process(HttpRequest request, HttpContext context) {
						    // Add header to accept gzip content
						    if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
						      request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
						    }
						  }
						});

						httpclient.addResponseInterceptor(new HttpResponseInterceptor() {
						  public void process(HttpResponse response, HttpContext context) {
						    // Inflate any responses compressed with gzip
						    final HttpEntity entity = response.getEntity();
						    final Header encoding = entity.getContentEncoding();
						    if (encoding != null) {
						      for (HeaderElement element : encoding.getElements()) {
						        if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
						          response.setEntity(new InflatingEntity(response.getEntity()));
						          break;
						        }
						      }
						    }
						  }
						});
					// ********************************************************************* //
					HttpPost httppost = new HttpPost(Utilities.getURLPath(UIActivity.this));
									
					httppost.setEntity(se);
					httppost.setHeader("Accept", "application/json");
					httppost.setHeader("Content-type", "application/json");
					
					try {
					    // Execute HTTP Post Request
						Log.v("CHECKPOINT","1");
					    HttpResponse response = httpclient.execute(httppost);
					    Log.v("CHECKPOINT","2");
					    StringBuilder result = inputStreamToString(response.getEntity().getContent());
					    resultString = result.toString();
	//				    // Parsing the result through Utilities functions
					    resultArray = Utilities.formatResponse(resultString);
					    Log.v("RESULT", resultString);
					    boolresult = true;
	
					} catch (ClientProtocolException e) {
						cancel(true);
					    // TODO Auto-generated catch block
					} catch (IOException e) {
						Log.v("The Exception", e.toString());
						cancel(true);
					    // TODO Auto-generated catch block
					} catch (RuntimeException e) {
						cancel(true);
					}
				
					// Following snippet updates all browser entities' value to 1 for the ones that were uploaded 
					if(resultArray[0].equals("0")){
						for(int i=0;i<idS.length;i++){
							if(idS[i]!=0){
								datasource.open();
								datasource.updateUploadedBrowser(idS[i],1);
								datasource.close();
							}
						}
					}
					refreshLists();
    			}
			}
				
			return null;
		}

		protected void onPostExecute(final String unused) {
	        if (pd.isShowing()) {
	        	pd.dismiss();
	        }
	        
	        // Checking this flag over here ensures that if no result was received from the server
	        // we won't try to read the variables (NullPointerException avoided)
	        if(!boolresult){
	        	Toast.makeText(UIActivity.this, 
						getResources().getString(R.string.progress_ioexception),
			   			Toast.LENGTH_SHORT).show();
	        } else {
	        	buttonCheck.setEnabled(false);
	        	buttonCheck.setText("All entries uploaded!");
	        	AlertDialog.Builder builder = new AlertDialog.Builder(UIActivity.this);
				builder.setTitle("The result was:");
				builder.setMessage(resultArray[1]);
			    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// Dismiss
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
	        	
				Log.v("Result CODE", resultArray[0]+"");
				Log.v("Result MESSAGE", resultArray[1]+"");
	        }
	    }
	}
	
    
	/**
	 * @author orestis
	 *
	 *	The Class that will be used to perform the Asynchronous operation
	 *	of exporting the user's data to the server. 
	 *	It handles all exports, since the Object is created prior to the call
	 *	off the Class and is set as a variable before execution
	 *	(SendJSONObjectOperation.myObj = ... )
	 */
	private class SendJSONObjectOperationSingle extends AsyncTask<String, Void, String>{
		private ProgressDialog pd;
		private volatile boolean boolresult = false;
		String serviceURL;
		String resultString;
	    String[] resultArray;
	    private JSONObject myObj;
	    private String date;
	    private int[] idS;
	    private boolean finished;
	    
		protected void onPreExecute() {
			pd = new ProgressDialog(UIActivity.this);
			pd.setTitle("Uploading "+date+" entries");
			pd.setMessage(getResources().getString(R.string.progress_message));
			pd.setIndeterminate(true);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setCancelable(true);
			pd.setOnCancelListener(new OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	                // actually could set running = false; right here, but I'll
	                // stick to contract.
	                cancel(true);
	            }
	        });
			pd.show();
		}
		
		protected void onCancelled(){
			// Nothin'.. just cancel and return to previous screen
		}
		
		@Override
		protected String doInBackground(String... param){
			
				// Retrieving my device's uid
				TelephonyManager tManager = (TelephonyManager)UIActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
				String uid = tManager.getDeviceId();

				// ****************** STRING ENTITY ********************//
//				  I create the string entity in order to embed it to the httppost
			    StringEntity se = null;
				try {
					try {
						se = new StringEntity(Utilities.formatSendURL(PASSWORD, date, uid, TYPE_BROWSER, myObj));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// ********************* MULTIPART ENTITY **************** //
				// It was meant to be used to add object (which would also //
				// be compressed) to the entity sent.					   //
				// Chose not to use it for now, a string was sufficient.   //
				// Keeping it tho for future use.						   //
				
//				
//				MultipartEntity entity = new MultipartEntity();
//				try {
//					entity.addPart("foo",  new ByteArrayBody(Utilities.gZipString(Utilities.formatSendURL(PASSWORD, date, uid, myObj)),"foo.txt"));
//				} catch (UnsupportedEncodingException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (JSONException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
				
//				Log.v("UID", uid);
//				Log.v("URL", serviceURL);
				
				// Following 3 lines intended to boost connection speed
				// retrieved snippet from
				// http://stackoverflow.com/questions/3046424/http-post-requests-using-httpclient-take-2-seconds-why
				HttpParams params = new BasicHttpParams();
				params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
				DefaultHttpClient httpclient = new DefaultHttpClient(params);
				
				// **********************************************************************//
				// ************************ Code from Google I/O app: *******************//
				// ********************* http://code.google.com/p/iosched/ **************//
				// ******************* Adds gzip compression to the message *************//
				httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
					  public void process(HttpRequest request, HttpContext context) {
					    // Add header to accept gzip content
					    if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
					      request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
					    }
					  }
					});

					httpclient.addResponseInterceptor(new HttpResponseInterceptor() {
					  public void process(HttpResponse response, HttpContext context) {
					    // Inflate any responses compressed with gzip
					    final HttpEntity entity = response.getEntity();
					    final Header encoding = entity.getContentEncoding();
					    if (encoding != null) {
					      for (HeaderElement element : encoding.getElements()) {
					        if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
					          response.setEntity(new InflatingEntity(response.getEntity()));
					          break;
					        }
					      }
					    }
					  }
					});
				// ********************************************************************* //
				
				HttpPost httppost = new HttpPost(Utilities.getURLPath(UIActivity.this));
								
				httppost.setEntity(se);
//				httppost.setEntity(entity);
				httppost.setHeader("Accept", "application/json");
				httppost.setHeader("Content-type", "application/json");
				
				try {
				    // Execute HTTP Post Request
					Log.v("CHECKPOINT","1");
				    HttpResponse response = httpclient.execute(httppost);
				    Log.v("CHECKPOINT","2");
				    StringBuilder result = inputStreamToString(response.getEntity().getContent());
				    resultString = result.toString();
//				    // Parsing the result through Utilities functions
				    resultArray = Utilities.formatResponse(resultString);
				    Log.v("RESULT", resultString);
				    boolresult = true;

				} catch (ClientProtocolException e) {
					cancel(true);
				} catch (IOException e) {
					cancel(true);
				} catch (RuntimeException e) {
					cancel(true);
				}
			
			return null;
		}

		protected void onPostExecute(final String unused) {
	        if (pd.isShowing()) {
	        	pd.dismiss();
	        }
	        
	        // Checking this flag over here ensures that if no result was received from the server
	        // we won't try to read the variables (NullPointerException avoided)
	        if(!boolresult){
	        	Toast.makeText(UIActivity.this, 
						getResources().getString(R.string.progress_ioexception),
			   			Toast.LENGTH_SHORT).show();
	        } else {
	        	if(finished){
		        	AlertDialog.Builder builder = new AlertDialog.Builder(UIActivity.this);
				    builder.setTitle("The result was:");
					builder.setMessage(resultArray[1]);
			        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// Dismiss
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
	        	}
				Log.v("Result CODE", resultArray[0]+"");
				Log.v("Result MESSAGE", resultArray[1]+"");
				
				// Following snippet updates all browser entities' value to 1 for the ones that were uploaded 
				if(resultArray[0].equals("0")){
					for(int i=0;i<idS.length;i++){
						if(idS[i]!=0){
							datasource.open();
							datasource.updateUploadedBrowser(idS[i],1);
							datasource.close();
						}
						refreshLists();
					}
				}
	        }
	    }
	}
	
	 /**
     * This is plainly for the intro screen:
     */
    private void getStarted() {
    	
    	// Reading from the SharedPreferences
    	// This means that the user launches the application for the first time or has not chosen to not show this again
    	// I will create an alert message to prompt the user to create a password phrase
    	SharedPreferences userDetails = PreferenceManager.getDefaultSharedPreferences(UIActivity.this);
        boolean gettingStarted = userDetails.getBoolean("getting_started", true);
        if (gettingStarted){
        	showGettingStartedDialog(0);
        }
        
        return;
     }
    
    private void showGettingStartedDialog(int choice){
    	final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(UIActivity.this);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	switch (choice) {
    		case 0: 
    			builder.setTitle(getString(R.string.welcome_message));
    			builder.setMessage(getString(R.string.welcome_message_text));
    	    	
    	        builder.setPositiveButton(getString(R.string.skip), new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					
    	        	}
    			});
    	        
    	        builder.setNegativeButton(getString(R.string.next),  new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					showGettingStartedDialog(1);
    				}
    	        });
    	        
    	        builder.show();
    	        break;
    	        
    		case 1: 
    			builder.setTitle(getString(R.string.getting_started1_3));
    			View layout = View.inflate(this, R.layout.initial_screen1_3, null);
    			final RadioButton radiobuttonLow = (RadioButton)layout.findViewById(R.id.radio0);
    			final RadioButton radiobuttonHigh = (RadioButton)layout.findViewById(R.id.radio1);
    			
    			radiobuttonLow.setOnCheckedChangeListener(new OnCheckedChangeListener(){
	                @Override
	                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	                    if (isChecked) radiobuttonHigh.setChecked(false);
	                    if (isChecked) 
	                    	sharedPrefs.edit().putString("privacy", "1").commit();
	                }   
	            });
    			
    			radiobuttonHigh.setOnCheckedChangeListener(new OnCheckedChangeListener(){
	                @Override
	                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	                    if (isChecked) radiobuttonLow.setChecked(false);
	                    if (isChecked) 
	                    	sharedPrefs.edit().putString("privacy", "9").commit();
	                }   
	            });
    			
    			builder.setPositiveButton(getString(R.string.back), new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					showGettingStartedDialog(0);
    	        	}
    			});
    	        
    	        builder.setNeutralButton(getString(R.string.skip), new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					
    	        	}
    			});
    	        
    	        
    	        builder.setNegativeButton(getString(R.string.next),  new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					showGettingStartedDialog(2);
    				}
    	        });
    	        builder.setView(layout);
    	        builder.show();
    	        break;
    	        
    		case 2:
    			layout = View.inflate(this, R.layout.initial_screen2_3, null);
    			builder.setTitle(getString(R.string.getting_started2_3));
    	    	
    			builder.setPositiveButton(getString(R.string.back), new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					showGettingStartedDialog(1);
    	        	}
    			});
    	        
    	        builder.setNeutralButton(getString(R.string.skip), new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					
    	        	}
    			});
    	        
    	        builder.setNegativeButton(getString(R.string.next),  new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					showGettingStartedDialog(3);
    				}
    	        });
    	        
    	        builder.setView(layout);
    	        builder.show();
    	        break;
    	        
    		case 3:
    			layout = View.inflate(this, R.layout.initial_screen3_3, null);
    			builder.setTitle(getString(R.string.getting_started3_3));
    			CheckBox doNotShowAgain = (CheckBox) layout.findViewById(R.id.checkBox1);
    			doNotShowAgain.setOnCheckedChangeListener(new OnCheckedChangeListener(){
	                @Override
	                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	                    if (isChecked) 
	                    	sharedPrefs.edit().putBoolean("getting_started", false).commit();
	                    else if(!isChecked)
	                    	sharedPrefs.edit().putBoolean("getting_started", true).commit();
	                }   
	            });
    			
    	        builder.setPositiveButton(getString(R.string.back), new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					showGettingStartedDialog(2);
    	        	}
    			});
    	        
    	        builder.setNeutralButton(getString(R.string.close), new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					
    	        	}
    			});
    	        
    	        builder.setNegativeButton(getString(R.string.welcome_go_to_places),  new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					startActivity(new Intent(UIActivity.this, PlacesActivity.class));
    				}
    	        });
    	        
    	        builder.setView(layout);
    	        builder.show();
    	        break;
    	}
    	
    }
	
	/**
     * Simple {@link HttpEntityWrapper} that inflates the wrapped
     * {@link HttpEntity} by passing it through {@link GZIPInputStream}.
     */
    private static class InflatingEntity extends HttpEntityWrapper {
            public InflatingEntity(HttpEntity wrapped) {
                    super(wrapped);
            }

            @Override
            public InputStream getContent() throws IOException {
                    return new GZIPInputStream(wrappedEntity.getContent());
            }

            @Override
            public long getContentLength() {
                    return -1;
            }
    }

}

