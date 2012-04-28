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
package com.androidquery.simplefeed;


import android.app.Application;
import android.content.Context;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.simplefeed.util.ErrorReporter;
import com.androidquery.simplefeed.util.PrefUtility;
import com.androidquery.util.AQUtility;
import com.bugsense.trace.BugSenseHandler;



public class MainApplication extends Application implements Thread.UncaughtExceptionHandler{

	
	public static final String MOBILE_AGENT = "Mozilla/5.0 (Linux; U; Android 2.2) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533";		
	
	
	@Override
    public void onCreate() {     
        
		AQUtility.setContext(this);
        
		boolean test = PrefUtility.isTestDevice();
		System.err.println("test:" + test);
		
        if(test){
        	AQUtility.setDebug(true);
        }
        
        bugTracking();
        ErrorReporter.installReporter(AQUtility.getContext());
        
        AQUtility.setExceptionHandler(this);
        
        AQUtility.setCacheDir(null);
        
        AjaxCallback.setNetworkLimit(8);
        //AjaxCallback.setAgent(MOBILE_AGENT);
        
        BitmapAjaxCallback.setIconCacheLimit(200);
        BitmapAjaxCallback.setCacheLimit(80);
        BitmapAjaxCallback.setPixelLimit(400 * 400);
        BitmapAjaxCallback.setMaxPixelLimit(2000000);
        
        
        
        
        super.onCreate();
    }
	
	private static final String API_KEY = "81009b75";
	private void bugTracking(){
		
		try{
			AQUtility.debug("tracking!");
			BugSenseHandler.setup(this, API_KEY);	
		}catch(Exception e){
			AQUtility.debug(e);
		}
	}
	
	
	
	@Override
	public void onLowMemory(){	
    	BitmapAjaxCallback.clearCache();
    }
	
	public static Context getContext(){
		return AQUtility.getContext();
	}
	public static String get(int id){
		return getContext().getString(id);
	}
	

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		ErrorReporter.report(ex, true);
	}
	
}
