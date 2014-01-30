package com.orestis.tudelftlogger;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.orestis.tudelftlogger.database.BrowserDataSource;
import com.orestis.tudelftlogger.database.MPlace;
import com.orestis.tudelftlogger.util.ActionItem;
import com.orestis.tudelftlogger.util.QuickAction;

public class PlacesActivity extends Activity {
	private BrowserDataSource datasource;
	private ListView placesListView;
	private Dialog myDialog;
	private static int ITEM_DELETE = 21;
	private static int ITEM_EDIT = 22;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_places);
	    
	    final EditText addressEditText = (EditText) findViewById(R.id.editText2);

	    datasource = new BrowserDataSource(this);
	    refreshPlacesList();
	    
	    Button setButton = (Button) findViewById(R.id.setButton);
	    setButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(addressEditText.getText().toString().equals("")){
					Toast.makeText(getApplicationContext(), "Please don't leave address field blank", Toast.LENGTH_SHORT).show();
				}else{
					reverseGeocode();
				}
			}
	    	
	    });
	}
	  
	public void reverseGeocode(){
		EditText addressEditText = (EditText) findViewById(R.id.editText2);
		
		String address = "";
		address = addressEditText.getText().toString();
		Log.d("ADDRESS", address);
		
		Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
		try {
			List<Address> addressResult = geocoder.getFromLocationName(address, 3);
			if (!addressResult.isEmpty()) {
		          Address resultAddress = addressResult.get(0);
		          myDialog = confirmAddressDialog(addressResult);
		          myDialog.show();
//		          confirmAddressDialog(addressResult).show();
			} else {
				Toast.makeText(this, "Could not understand the address, did you include [Address, City, ZipCode] correctly?", Toast.LENGTH_LONG).show();
				Log.d("Address List", "empty");
			}
		} catch (IOException e) {
			Log.d("Contact Location Lookup Failed", e.getMessage());
			e.printStackTrace();
		}
	}
	  
	private Dialog confirmAddressDialog(final List<Address> addressResult){
		  
		AlertDialog.Builder builder = new AlertDialog.Builder(PlacesActivity.this);
		final View layout = View.inflate(this, R.layout.address_dialog, null);
		ListView myAddressList = (ListView) layout.findViewById(R.id.listView1);
		final List<String> addressList = new ArrayList<String>();
		
		// Iterate through the results to list them to the user and let him select the most relevant choice
		for(int i=0; i<addressResult.size();i++){
			// Build a string to present to hum
			StringBuilder st = new StringBuilder();
			for(int j=0; j<addressResult.get(i).getMaxAddressLineIndex()+1;j++){
				st.append(addressResult.get(i).getAddressLine(j)+"\n");
			}
			// Calling this trick to cut the final "\n" I put for presentation reasons to the appended string		  
			addressList.add(st.substring(0, st.length()-1).toString());
		}
	      
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					R.layout.list_addresses, addressList);
		myAddressList.setAdapter(adapter);
	    myAddressList.setOnItemClickListener(new OnItemClickListener() {
	    	public void onItemClick(AdapterView<?> parent, View view,
	    			int position, long id) {
	    		// User clicked on a choice, meaning that his/her address is correct.
				// Add this address to the database, along with its tag.
				Toast.makeText(getApplicationContext(), addressList.get(position), Toast.LENGTH_SHORT).show();
//				AutoCompleteTextView tagEditText = (AutoCompleteTextView) findViewById(R.id.editText1);
				RadioGroup radioGroupTags = (RadioGroup) findViewById(R.id.radioGroupTags);
				RadioButton radioTag;
				
				// get selected radio button from radioGroup
				int selectedId = radioGroupTags.getCheckedRadioButtonId();
	 
				// find the radiobutton by returned id
				radioTag = (RadioButton) findViewById(selectedId);
				
				StringBuilder stb = new StringBuilder();
				for(int j=0; j<addressResult.get(position).getMaxAddressLineIndex()+1;j++){ 
					stb.append(addressResult.get(position).getAddressLine(j)+" ");
				}
				datasource.open();
				datasource.createPlace(radioTag.getText().toString(),
						stb.toString(),
						addressResult.get(position).getLatitude(),
						addressResult.get(position).getLongitude());
				datasource.close();
				refreshPlacesList();
//				InputMethodManager imm = (InputMethodManager)getSystemService(
//					      Context.INPUT_METHOD_SERVICE);
//				imm.hideSoftInputFromWindow(tagEditText.getWindowToken(), 0);
				myDialog.dismiss();
	    	}
	    });
	      
	    builder.setIcon(android.R.drawable.ic_menu_myplaces);
	    builder.setTitle("Did you mean..");
	    builder.setView(layout);
	    builder.setNegativeButton("Try Again", new Dialog.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	        	// Do nothing.
	        }
	    });
	    
	    return builder.create();
	} 
	  
	private void refreshPlacesList(){
		datasource.open();
		final List<MPlace> values = datasource.getAllPlaces();
		datasource.close();
		List<String> names = new ArrayList<String>(); 
		for(int i=0;i<values.size();i++){
			names.add(values.get(i).getTag()+":\n"+values.get(i).getAddress());
		}
	  	
		placesListView = (ListView) findViewById(R.id.placesList);
	  	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	  			R.layout.list_places, names);
	  	
	  	placesListView.setAdapter(adapter);
	  	
	  	placesListView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					MPlace selectedPlace = values.get(position);
					Log.d("PLACE ADDRESS: ", selectedPlace.getAddress());
				}
	  	});
	  	
	  	placesListView.setOnItemLongClickListener(new OnItemLongClickListener(){
				public boolean onItemLongClick(AdapterView<?> parent, View view,
						final int position, long id){
					final QuickAction mQuickAction  = new QuickAction(PlacesActivity.this);
	      		ActionItem deletePlace      = new ActionItem(ITEM_DELETE,
	      				getResources().getDrawable(android.R.drawable.ic_menu_delete));
//	      		ActionItem editPlace      = new ActionItem(ITEM_EDIT,
//	      				getResources().getDrawable(android.R.drawable.ic_menu_edit));
//	      		mQuickAction.addActionItem(editPlace);
	      		mQuickAction.addActionItem(deletePlace);
	      	
	      		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
	      			public void onItemClick(QuickAction quickAction, int pos, int actionId) {
	      				if (actionId == ITEM_DELETE) {
		        				// Builder to warn the user for trip deletion!
		        				AlertDialog.Builder builder = new AlertDialog.Builder(PlacesActivity.this);
		        		        builder.setIcon(android.R.drawable.ic_dialog_alert);
		        		        builder.setTitle(R.string.caution);
		        		        builder.setMessage(R.string.confirm_place_deletion_message);
		        		        builder.setPositiveButton(R.string.yes, new Dialog.OnClickListener() {
		        		            public void onClick(DialogInterface dialog, int which) {
		        		            	MPlace selectedPlace = values.get(position);
		        		            	datasource.open();
		        						datasource.deletePlace(selectedPlace);
		        						datasource.close();
		        						refreshPlacesList();
		        		            }
		        		        });
		        		        builder.setNegativeButton("Cancel", new Dialog.OnClickListener() {
		        		            public void onClick(DialogInterface dialog, int which) {
		        		            	// Do nothing.
		        		            }
		        		        });
		        		        builder.show();
	      				}
//	      				else if(actionId == ITEM_EDIT){
//	      					
//	      				}
	      			}
	      		});
	      		mQuickAction.show(parent);
	      		mQuickAction.setAnimStyle(QuickAction.ANIM_GROW_FROM_CENTER);
	      		return true;
				}
	  	});
	  }
	} 