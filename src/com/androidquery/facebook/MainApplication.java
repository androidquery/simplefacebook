package com.androidquery.facebook;


import java.io.File;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.facebook.util.ErrorReporter;
import com.androidquery.util.AQUtility;



public class MainApplication extends Application{

	private static Context context;
	private static String MOBILE_AGENT = "Mozilla/5.0 (Linux; U; Android 2.2) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533";		
	
	
	@Override
    public void onCreate() {     
          
		System.err.println("FB started");
		
        context = getApplicationContext();
        
        ErrorReporter.installReporter(context);
        
        /*
        File ext = Environment.getExternalStorageDirectory();
		File cacheDir = new File(ext, "simplefacebook");		
		AQUtility.setCacheDir(cacheDir);
        */
        AQUtility.setCacheDir(null);
        
        AjaxCallback.setNetworkLimit(8);
        AjaxCallback.setAgent(MOBILE_AGENT);
        
        BitmapAjaxCallback.setIconCacheLimit(40);
        BitmapAjaxCallback.setCacheLimit(80);
        BitmapAjaxCallback.setPixelLimit(400 * 400);
        BitmapAjaxCallback.setMaxPixelLimit(2000000);
        
        //ErrorReporter.installReporter();
        
        
        //if(PrefUtility.isDebugLog()){
        	AQUtility.setDebug(true);
        //}
        
        super.onCreate();
    }
	
	public static Context getContext(){
		return context;
	}
	
	@Override
	public void onLowMemory(){	
    	BitmapAjaxCallback.clearCache();
    }
	
}
