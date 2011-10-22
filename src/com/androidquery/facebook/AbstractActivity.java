package com.androidquery.facebook;

import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.auth.FacebookHandle;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class AbstractActivity extends FragmentActivity{

	protected AQuery aq;
	protected FacebookHandle handle;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aq = new AQuery(this);
        setupHandle();
    }
	
	private static String APP_ID = "251003261612555";
	private static String PERMISSIONS = "read_stream,publish_stream";
	
	private void setupHandle(){
		
		//CookieSyncManager.createInstance(this);
		//CookieManager.getInstance().removeAllCookie();		
		//storeToken("aq.fb.token", null);
		
		handle = new FacebookHandle(this, APP_ID, PERMISSIONS);
		
		
	}
	
	
}
