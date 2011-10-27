package com.androidquery.facebook;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.facebook.data.FeedItem;
import com.androidquery.facebook.data.PageAdapter;
import com.androidquery.facebook.enums.FeedMode;
import com.androidquery.facebook.util.FormatUtility;
import com.androidquery.facebook.util.IconUtility;
import com.androidquery.facebook.util.IntentUtility;
import com.androidquery.facebook.util.PrefUtility;
import com.androidquery.util.AQUtility;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

public class ImageActivity extends Activity {
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.image_activity);
        
        initView();
    }
    
    private void initView(){
    	
    	
    	Intent intent = getIntent();
    	if(intent == null) return;
    	
    	String url = intent.getStringExtra("url");
    	String title = intent.getStringExtra("title");
    	
    	if(title != null){
    		setTitle(title);
    	}
    	
    	WebView wv = (WebView) findViewById(R.id.web);
    	
    	//String html = "<img style=\"min-width:600px;\" src=\""  + url + "\" />";
    	
    	//AQUtility.debug(html);
    	
    	WebSettings ws = wv.getSettings();
    	ws.setSupportZoom(true);
    	ws.setDefaultZoom(WebSettings.ZoomDensity.FAR);
    	ws.setBuiltInZoomControls(true);
    	
    	AQuery aq = new AQuery(wv);
    	aq.setOverScrollMode9(AQuery.OVER_SCROLL_NEVER);
    	
    	//wv.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    	wv.loadUrl(url);
    	
    	wv.setBackgroundColor(Color.parseColor("#000000"));
    	
    	
    	
    }
    
}