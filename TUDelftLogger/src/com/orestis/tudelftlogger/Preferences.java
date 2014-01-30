package com.orestis.tudelftlogger;

import com.orestis.tudelftlogger.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Preferences extends PreferenceActivity {

//   private CheckBoxPreference showSplash;
	private CheckBoxPreference gettingStarted;
	private ListPreference privacySetting;
	private EditTextPreference ipAddressSetting;

	@Override
   	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.layout.activity_preferences);

      // handle to preferences doesn't come from findViewById!
//      showSplash = (CheckBoxPreference) getPreferenceScreen().findPreference("showsplash");
		gettingStarted = (CheckBoxPreference) getPreferenceScreen().findPreference("getting_started");
		privacySetting = (ListPreference) getPreferenceScreen().findPreference("privacy");
		ipAddressSetting = (EditTextPreference) getPreferenceScreen().findPreference("ipAddress");
		
		ipAddressSetting.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				myWarningDialog().show();
				return false;
			}
			
		});
      

      setCheckBoxSummary(gettingStarted);

      // listen to see if user changes pref, so we can update display of current value
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
      
//      prefs.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
//         public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
//            if (key.equals("showsplash")) {
//               setCheckBoxSummary(showSplash);
//            }
//         }
//      });

	    prefs.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
	       public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
	          if (key.equals("getting_started")) {
	             setCheckBoxSummary(gettingStarted);
	          }
	       }
	    });
	}
   
	private Dialog myWarningDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Preferences.this);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle(R.string.confirm_ip_mess_title);
		builder.setMessage(R.string.confirm_ip_mess_message);
		builder.setPositiveButton(R.string.cont, new Dialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		builder.setNegativeButton("Cancel", new Dialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				 ipAddressSetting.getDialog().dismiss();
			}
		});
	       return builder.create();
	    }
	
	   private void setCheckBoxSummary(CheckBoxPreference pref) {
	      if (pref.isChecked()) {
	         pref.setSummary("Enabled");
	      } else {
	         pref.setSummary("Disabled");
	      }
	}
}