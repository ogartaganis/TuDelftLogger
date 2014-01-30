package com.orestis.tudelftlogger;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;

import com.orestis.tudelftlogger.database.BrowserDataSource;

public class NotificationReceiverActivity extends Activity {
	String searchText;
	Long searchId;
	List<RadioButton> radioButtons;
	String searchTopic = "default";
	boolean flagCustom = false;
	private BrowserDataSource datasource;
	
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_notification_receiver);
	    
	    datasource = new BrowserDataSource(this);
	    
	    myPersonDialog().show();
	  }
	  
	  private Dialog myPersonDialog() {
		  	searchId = (Long) getIntent().getExtras().get("search_id");
		    searchText = (String) getIntent().getExtras().get("search_text");
		    Log.v("INTENT RECEIVED for search(id):", searchId+"");
			Log.v("INTENT RECEIVED for search:", searchText);
		  
		  	AlertDialog.Builder builder = new AlertDialog.Builder(this);		  	
	        final View layout = View.inflate(this, R.layout.notification_dialog, null);
	        
	        radioButtons = new ArrayList<RadioButton>();
	        radioButtons.add( (RadioButton)layout.findViewById(R.id.radio0));
	        radioButtons.add( (RadioButton)layout.findViewById(R.id.radio1));
	        radioButtons.add( (RadioButton)layout.findViewById(R.id.radio2));
	        radioButtons.add( (RadioButton)layout.findViewById(R.id.radio3));
	        radioButtons.add( (RadioButton)layout.findViewById(R.id.radio4));
	        radioButtons.add( (RadioButton)layout.findViewById(R.id.radio5));
	        radioButtons.add( (RadioButton)layout.findViewById(R.id.radio6));
	        radioButtons.add( (RadioButton)layout.findViewById(R.id.radio7));
	        radioButtons.add( (RadioButton)layout.findViewById(R.id.radio8));
	        radioButtons.add( (RadioButton)layout.findViewById(R.id.radio9));
	        radioButtons.add( (RadioButton)layout.findViewById(R.id.radio10));
	        
	        // 	For the custom topic editText field
	        final InputMethodManager imm = (InputMethodManager)getSystemService(
				      Context.INPUT_METHOD_SERVICE);
	        final EditText customAmount = (EditText) layout.findViewById(R.id.customTopic);
	       
	        for(RadioButton button: radioButtons) {
	        	button.setOnCheckedChangeListener(new OnCheckedChangeListener(){
	                @Override
	                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	                    if (isChecked) processRadioButtonClick(buttonView);
	                    if (isChecked) 
	                    	if(radioButtons.get(10)!=buttonView){
	                    		flagCustom = false;
	                    		searchTopic = buttonView.getText().toString();
	                    		// Hiding the soft keyboard
	                       		imm.hideSoftInputFromWindow(customAmount.getWindowToken(), 0);
	                	 		customAmount.setFocusable(false);
	        		           	customAmount.setFocusableInTouchMode(false);
	                    	}else{
	                    		flagCustom = true;
	                    		// Making the soft keyboard appear
	                	 		imm.showSoftInput(getCurrentFocus(), 0);
	                	 		customAmount.setFocusable(true);
	        		          	customAmount.setFocusableInTouchMode(true);
	        		           	customAmount.requestFocus();
	                    	}
	                }   
	            });
	        }
	        
	        customAmount.setOnClickListener(new View.OnClickListener(){
	        	public void onClick(View v){
	        		radioButtons.get(radioButtons.size()-1).setChecked(true);
	        		// Making the soft keyboard appear
        	 		imm.showSoftInput(getCurrentFocus(), 0);
        	 		customAmount.setFocusable(true);
		          	customAmount.setFocusableInTouchMode(true);
		           	customAmount.requestFocus();
	        	}
	        });
            
	        builder.setView(layout);	        

	        builder.setTitle("What was the topic of your search?");
	        builder.setPositiveButton("Add", new Dialog.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
                    if(flagCustom)
                    	searchTopic = customAmount.getText().toString();
                    Log.v("GROUP CHOICE", searchTopic);
	            	Log.v("aaSEARCH ID", searchId+"");
                    Log.v("aaSEARCH TEXT", searchText+"");
                    
                    // Now store the choice along with the related entry
                    datasource.open();
                    datasource.updateIncentive(searchId, searchTopic);
                    datasource.close();
                    
                    
                    NotificationReceiverActivity.this.finish();
	            }
	         
	        });
	        builder.setNegativeButton("Cancel", new Dialog.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	               // Do nothing
	            	NotificationReceiverActivity.this.finish();
	            }
	        });
	        
	        Dialog myDialog = builder.create();
	        
	        // I am setting onClick listener to catch the "back button press" by the user
	        // to disable the whole -transparent- activity.
	        // That way when the user wants to quickly dismiss the dialog without dealing with
	        // its contents, the activity will not linger behind.
	        myDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
		    {
		        @Override
		        public void onCancel(DialogInterface dialog)
		        {
		        	NotificationReceiverActivity.this.finish();
		        }
		    });
	        
	        return myDialog;
	     }
	  
	  // This method has the simple functionality of UN-checking all OTHER radiobuttons than the one selected.
	  private void processRadioButtonClick(CompoundButton buttonView)
	  {
          for (RadioButton button : radioButtons){
              if (button != buttonView ) button.setChecked(false);
          }
      }
	} 