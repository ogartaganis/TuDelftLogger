package com.orestis.tudelftlogger;

import com.orestis.tudelftlogger.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class AboutActivity extends Activity {
	String EMAIL = " ogartaganis@gmail.com";
	String WEBPAGE = " www.example.com";
	private SpannableString aboutEmail, aboutWebpage;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
        aboutEmail = new SpannableString(EMAIL);
        aboutWebpage = new SpannableString(WEBPAGE);
        Linkify.addLinks(aboutEmail, Linkify.ALL);
        Linkify.addLinks(aboutWebpage, Linkify.ALL);

        TextView textEmail = (TextView)findViewById(R.id.textEmail);
        textEmail.setText(aboutEmail);
        ((TextView) findViewById(R.id.textEmail)).setMovementMethod(LinkMovementMethod.getInstance());
        
        TextView textWebpage = (TextView)findViewById(R.id.textWebpage);
        textWebpage.setText(aboutWebpage);
        ((TextView) findViewById(R.id.textWebpage)).setMovementMethod(LinkMovementMethod.getInstance());
        
    }
}