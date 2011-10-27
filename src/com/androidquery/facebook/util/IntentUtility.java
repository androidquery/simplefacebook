package com.androidquery.facebook.util;

import com.androidquery.util.AQUtility;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class IntentUtility {

	/*
    public static boolean openMarket(Activity act) {
    	try{
	    	Intent intent = new Intent(Intent.ACTION_VIEW);
	    	intent.setData(Uri.parse(AppStoreUtility.getMarketMobileUrl()));
	    	act.startActivity(intent);
	    	return true;
    	}catch(Exception e){
    		AQUtility.report(e);
    		return false;
    	}
    }
    */
    
    public static boolean openBrowser(Activity act, String url) {
    
    	
    	try{
   
	    	if(url == null) return false;
	    	
	    	Uri uri = Uri.parse(url);
	    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);	    	
	    	act.startActivity(intent);
    	
	    	return true;
    	}catch(Exception e){
    		AQUtility.report(e);
    		return false;
    	}
    }
    
    public static void sendEmail(Activity act){
    	
    	Intent i = new Intent(Intent.ACTION_SEND);
    	
    	i.setType("text/plain");
    	i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"support@vikispot.com"});
    	i.putExtra(Intent.EXTRA_SUBJECT, "Feedback from User");
    	
    	try {
    	    act.startActivity(Intent.createChooser(i, "Send feedback email with..."));
    	} catch (android.content.ActivityNotFoundException ex) {
    	    //Toast.makeText(MyActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
    	}
    }
    
    
}
