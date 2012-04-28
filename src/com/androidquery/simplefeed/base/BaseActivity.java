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
package com.androidquery.simplefeed.base;

import java.util.Locale;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.Window;
import android.widget.Toast;

import com.androidquery.auth.FacebookHandle;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.Transformer;
import com.androidquery.service.MarketService;
import com.androidquery.simplefeed.PQuery;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.callback.PTransformer;
import com.androidquery.simplefeed.enums.FeedMode;
import com.androidquery.simplefeed.util.AppUtility;
import com.androidquery.simplefeed.util.PrefUtility;
import com.androidquery.util.AQUtility;
import com.flurry.android.FlurryAgent;

public abstract class BaseActivity extends FragmentActivity{

	public static long TEN_MIN = 10 * 60 * 1000;
	public static long DAY = 24 * 3600 * 1000;
	public static long HALF_DAY = 12 * 3600 * 1000;
	public static long MONTH = 30 * DAY;
	
	
	public PQuery aq;
	protected PQuery aq2;
	protected FacebookHandle handle;
	protected Transformer transformer;
	protected String locale = getLocale();
	
	@Override
    public void onCreate(Bundle savedInstanceState){
				
        super.onCreate(savedInstanceState);
        
        
        makeHandle();
        
        aq = new PQuery(this);
        //aq.hardwareAccelerated11();
        
        
        
        aq2 = new PQuery(this);
        
        transformer = new PTransformer();
        
        initView();

        init(savedInstanceState);
        
        if(isTaskRoot()){	        	
			MarketService ms = new MarketService(this);
			ms.level(MarketService.MINOR).checkVersion();
		}
        
    }
	

	
	
	private void makeHandle(){
		 
		handle = AppUtility.makeHandle(this);
		 
		boolean sso;
		 
		if(!PrefUtility.contains(Constants.PREF_SSO)){
			sso = PrefUtility.getEnum(FeedMode.class, null) == null;
			PrefUtility.put(Constants.PREF_SSO, sso);
			AQUtility.debug("first loaded sso", sso);
		}else{
			sso = PrefUtility.getBoolean(Constants.PREF_SSO, true); 
		}
		
		if(sso){
			handle.sso(Constants.ACTIVITY_SSO);
		}
	}
	
	protected boolean needProgress(){
		return true;
	}
	
	public boolean isActionBar(){
		return android.os.Build.VERSION.SDK_INT >= 11;
	}
	
	private void initView(){
		boolean full = fullScreen();
        
        if(!full){
        	//requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        }
        
        //boolean progress = needProgress();
        if(!isActionBar()){
        	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        	setProgressBarIndeterminate(true);
        }
        setContentView(getContainerView());
		
        if(!isActionBar()){
        	setProgressBarIndeterminateVisibility(false);           
        }
        
		if(!full){
			//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.actionbar);
		}
	}
	
	protected boolean fullScreen(){
		return false;
	}
	
	
    protected abstract void init(Bundle savedInstanceState);
    
    protected abstract int getContainerView();
	
    public Fragment getFragment(int id){ 	
    	return getSupportFragmentManager().findFragmentById(id);
    }
	

	
	public void refresh(){
		
	}
	
	@Override
	public void onStart(){	  
		
		super.onStart();
		
		try{
		
			if(!PrefUtility.isTestDevice()){
				FlurryAgent.onStartSession(this, "6JBJG2RKWTU13F25PXVE");
			}
		}catch(Exception e){
			AQUtility.report(e);
		}
	}
	
	@Override
    public void onStop(){
    	
		super.onStop();
		
    	try{
    	
	    	if(!PrefUtility.isTestDevice()){
	    		FlurryAgent.onEndSession(this);
	    	}
	    	
    	}catch(Exception e){
    		AQUtility.report(e);
    	}
    }
	
    protected void modeChange(){
    	
    	FeedMode mode = AppUtility.getDefaultMode();
    	
    	if(FeedMode.NEWS.equals(mode)){
    		mode = FeedMode.WALL;
    	}else{
    		mode = FeedMode.NEWS;
    	}
    	
    	//PrefUtility.put(MODE_KEY, mode);
    	PrefUtility.putEnum(mode);
    	
    	modeChange(mode);
    	
    }
    
    
    
    protected void modeChange(FeedMode mode){
    	
    }
    
    
    
    private void ajaxProfile(){
    	
    	String url = "https://graph.facebook.com/me";
    	
    	aq.auth(handle).ajax(url, JSONObject.class, 0, this, "profileCb");
    	
    }
    
    
    public void profileCb(String url, JSONObject jo, AjaxStatus status){
    	
    	AQUtility.debug(jo);
    	if(jo != null){
    		PrefUtility.put(AppUtility.USER_NAME, jo.optString("name", null));
    		updateTitle(0);
    	}
    	
    }
    
    protected String makeTitle(long time){
    	return AppUtility.getUserName(getString(R.string.app_name));
    }
    
    public void checkProfile(){
    	
    	if(isTaskRoot() && AppUtility.getUserName(null) == null){
            ajaxProfile();
        }
    	
    	
    	
    }
    
    public void showToast(String message) {
      	
    	if(message == null || message.length() == 0) return;
    	
    	try{
	    	Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
	    	toast.setGravity(Gravity.CENTER, 0, 0);
	    	toast.show();
    	}catch(Exception e){
    		AQUtility.report(e);
    	}
    }
    
    public abstract void showProgress(boolean progress);
    public abstract boolean isBusy();
    
    public void updateTitle(long time){
    	
    	setTitle(makeTitle(time));
    	
    }
    

    
    @Override
    public void onDestroy(){
    	
    	super.onDestroy();
    	aq.dismiss();
    	
    	if(isTaskRoot()){
    		AQUtility.cleanCacheAsync(this);
    	}
    	
    }
    
    /*
	public void progressDialog(boolean show, String message){
    	
    	if(show){
    		
			ProgressDialog dia = makeProgressDialog();        		
			dia.setMessage(message);
			
    		aq.show(dia);
    	}else{
    		
    		aq.dismiss();
    	}
    	
    }*/

    public ProgressDialog makeProgressDialog(String message){
    	
    	
    	ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setInverseBackgroundForced(false);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setMessage(message);
        
        return dialog;
	        
    }
    
    protected String getLocale(){
    	return Locale.getDefault().toString();
    }
    
    public boolean isRoot(){
    	return false;
    }
    
    private static Boolean tablet;
    public boolean isTablet(){
    	
    	if(tablet == null){
    		tablet = "true".equals(PrefUtility.getConfig(R.string.tablet));
    	}
    	
    	return tablet;
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	AQUtility.debug("on act result");
    	
    	aq.forward(this, requestCode, resultCode, data);
    	
    	switch(requestCode) {
    		
	    	case Constants.ACTIVITY_SSO: {
	    		handle.onActivityResult(requestCode, resultCode, data);	    		
	    		break;
	    	}
	    	
    	}
    }
}
