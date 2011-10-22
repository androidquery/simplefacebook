package com.androidquery.facebook;

import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.AQUtility;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class FeedActivity extends AbstractActivity {
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ajaxHome();
    }
    
	public void ajaxHome(){
	    
		String url = "https://graph.facebook.com/me/home";
		aq.auth(handle)//;.progress(R.id.progress)
		.ajax(url, JSONObject.class, this, "homeCb");
	        
	}	
	
	public void homeCb(String url, JSONObject jo, AjaxStatus status){
		
		AQUtility.debug(jo);
		
		if(jo != null){
			
			//AQUtility.debug(jo);
			
		}
		
	}
	
    
}