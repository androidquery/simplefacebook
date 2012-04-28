/*******************************************************************************
 * Copyright 2012 AndroidQuery (tinyeeliu@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Additional Note:
 * 1. You cannot use AndroidQuery's Facebook app account in your own apps.
 * 2. You cannot republish the app as is with advertisements.
 ******************************************************************************/
package com.androidquery.simplefeed.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import com.androidquery.auth.FacebookHandle;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.simplefeed.PQuery;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.data.Entity;
import com.androidquery.simplefeed.enums.FeedMode;
import com.androidquery.simplefeed.util.AppUtility;
import com.androidquery.simplefeed.util.DialogUtility;
import com.androidquery.simplefeed.util.IntentUtility;
import com.androidquery.simplefeed.util.ParseUtility;
import com.androidquery.simplefeed.util.PrefUtility;
import com.androidquery.util.AQUtility;



public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {

	protected FacebookHandle handle;
	protected PQuery aq;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);        
        addPreferencesFromResource(R.xml.settings);
        
        aq = new PQuery(this);
        handle = AppUtility.makeHandle(this);
        
        initView();
    }
   
 
    
    private void initView(){
    	
        Preference p = findPreference("logout");
        p.setOnPreferenceClickListener(this);
        
        p = findPreference("share");
        p.setOnPreferenceClickListener(this);
        p.setTitle(p.getTitle() + " @Facebook");
        
        p = findPreference("share_others");
        p.setOnPreferenceClickListener(this);
        
        
        p = findPreference("review");
        p.setOnPreferenceClickListener(this);
        
        p = findPreference("report");
        p.setOnPreferenceClickListener(this);
        
        p = findPreference("version");
        String version = getVersion();
        p.setSummary(version);
        
        
        p = findPreference("feedback");
        p.setOnPreferenceClickListener(this);
    }
    
    public boolean onPreferenceClick(Preference preference){
    	
    	String name = preference.getKey();
    	
    	AQUtility.debug("pred", name);
    	
    	try{
	    	if("logout".equals(name)){
	    		logout();
	    	}else if("share".equals(name)){
	    		share();
	    	}else if("share_others".equals(name)){
	    		share2();
	    	}else if("review".equals(name)){
	    		review();
	    	}else if("feedback".equals(name)){
	    		feedback();
	    	}else if("report".equals(name)){
	    		report();
	    	}
    	}
    	catch(Exception e){
    		AQUtility.report(e);
    	}
    	
    	return false;
    	
    }
    
    private void share2(){
    	
    	String title = getString(R.string.share_app_message);
    	String link = IntentUtility.getWebMarketUrl(this);
    	
    	String message = title + ":\n\n" + link;
    	
    	
    	IntentUtility.sendShare(this, title, message);
    }
    
    
    private void report(){
    	String title = getString(R.string.report_problem);
    	IntentUtility.sendEmail(this, title);
    }
    
    ///PROFILE_ID/links 	Publish a link on the given profile 	link, message, picture, name, caption, description
    
    private void share(){
    	
    	String message = getString(R.string.share_app_message);
    	String title = getString(R.string.share) + " @Facebook";
    	
    	DialogUtility.askYesNo(this, title, message, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				AlertDialog d = (AlertDialog) dialog;
				EditText edit = (EditText) d.findViewById(R.id.input);
				
				String message = edit.getEditableText().toString().trim();
				
				AQUtility.debug("send message", message);
				
				if(message.length() > 0){
					shareSend(message);
				}
			}
		});
    	
    	
    }
    
	private void shareSend(String message){
		
		String url = "https://graph.facebook.com/me/links";
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		String link = IntentUtility.getWebMarketUrl(this);
		//String message = getString(R.string.share_app_message);
		//String picture = "http://androidquery.appspot.com/z/images/simplefb/share.png";
		
		params.put("message", message);
		params.put("link", link);
		//params.put("picture", picture);
		
		AQUtility.debug("params", params);
		
		progress2(true, getString(R.string.sharing));
		
		aq.auth(handle).ajax(url, params, String.class, new AjaxCallback<String>(){
			
			@Override
			public void callback(String url, String object, AjaxStatus status) {
				
				progress2(false, null);
				
				showToast(getString(R.string.shared));
				
			}
			
		});
		
		
		
	}
    
    protected void showToast(String message) {
      	
    	Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.show();
    	
    }
	
    private void review(){
    	
    	IntentUtility.openMarket(this);
    	
    }
    
	private String getVersion(){		
		return getPackageInfo().versionName;		
	}
	
	private static PackageInfo pi;
	private PackageInfo getPackageInfo(){
		
		if(pi == null){
			try {
				pi = getPackageManager().getPackageInfo(getAppId(), 0);
				
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return pi;
	}
    
	private String getAppId(){
		return getApplicationInfo().packageName;
	}
	
    private void logout(){
    
    	AppUtility.logout(this);
    	
    	Intent intent = new Intent(this, LaunchActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	intent.putExtra("logout", true);
    	
    	PrefUtility.putEnum(FeedMode.NEWS);
    	
    	startActivity(intent);
    	
    }
    
	private void progress2(boolean show, String message){
    	
    	if(show){
    		
			ProgressDialog dia = getProgressDialog();        		
			dia.setMessage(message);
			
    		aq.show(dia);
    	}else{
    		
    		aq.dismiss();
    	}
    	
    }
    
    private ProgressDialog getProgressDialog(){
    	
    	ProgressDialog progress = null;
    	
    	if(progress == null){
	    	ProgressDialog dialog = new ProgressDialog(this);
	        dialog.setIndeterminate(true);
	        dialog.setCancelable(true);
	        dialog.setInverseBackgroundForced(false);
	        dialog.setCanceledOnTouchOutside(true);
	        progress = dialog;
    	}
        return progress;
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	aq.dismiss();
    }

    
    private void feedback(){
    	
    	Entity source = new Entity();
    	source.setId("304050252938418");
    	source.setName(getString(R.string.app_name));
    	source.setTb(ParseUtility.profileTb(source.getId()));
    	
    	FeedActivity.start(this, source);
    	
    }
    

    
	@Override
	public boolean onPreferenceChange(Preference pref, Object value) {
		return true;
		
	}
	
	
	@Override
    public void onResume(){
    	super.onResume();
    }
}    
