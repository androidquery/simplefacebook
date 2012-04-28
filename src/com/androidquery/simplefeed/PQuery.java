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

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

import com.androidquery.AbstractAQuery;
import com.androidquery.simplefeed.callback.WebPageAjaxCallback;
import com.androidquery.simplefeed.ui.LayoutString;
import com.androidquery.simplefeed.ui.SlimTextView;
import com.androidquery.simplefeed.util.PrefUtility;
import com.androidquery.util.AQUtility;

public class PQuery extends AbstractAQuery<PQuery>{

	public PQuery(View root) {
		super(root);
	}

	public PQuery(Activity act){
		super(act);
	}
	
	public PQuery(Activity act, View root){
		super(act, root);
	}
	
	
	public PQuery text(CharSequence text, TextView.BufferType type, boolean gone){
			
		
		if(view instanceof TextView){			
			if(gone && (text == null || text.length() == 0)){
				gone();
			}else{
				TextView tv = (TextView) view;
				tv.setText(text, type);
				visible();
			}
		}
				
		return this;
	}
	
	private static final boolean LAYER = android.os.Build.VERSION.SDK_INT >= 14;
	public PQuery text(LayoutString text, boolean gone){
			
		
		if(view instanceof SlimTextView){			
			if(gone && (text == null || text.length() == 0)){
				gone();
			}else{
				SlimTextView tv = (SlimTextView) view;
				if(LAYER){
					//tv.setLayerType(text.getLayer(), null);
					//tv.setDrawingCacheEnabled(true);
				}
				
				tv.setLayoutText(text);
				visible();				
			}
		}else{
			text(text);
		}
		
		
		return this;
	}
	
	

	
	

	
	public PQuery web(String url){
		
		
		if(view instanceof WebView){
			WebView wv = (WebView) view;
			WebPageAjaxCallback cb = new WebPageAjaxCallback();
			cb.header("User-Agent", MainApplication.MOBILE_AGENT);
			cb.web(wv).url(url).fileCache(true).progress(progress);
			progress = null;
			invoke(cb);
		}
		
		return this;
	}
	
	private static final String FW_CB = "aq.fw.cb";
	private static final String FW_ID = "aq.fw.id";
	
	public void start(Activity act, Intent intent, int requestCode, Object handler, String method){
		
		//String clsName = handler.getClass().getName();
		
		intent.putExtra(FW_CB, method);
		
		if(handler instanceof Fragment){
			Fragment frag = (Fragment) handler;
			intent.putExtra(FW_ID, frag.getId());
		}
		
		act.startActivityForResult(intent, requestCode);
		
	}
	
	private final static Class<?>[] FW_SIG = new Class[]{int.class, int.class, Intent.class};
	public void forward(FragmentActivity act, int requestCode, int resultCode, Intent data){
		
		if(data == null) return;
		
		String method = data.getStringExtra(FW_CB);
		
		
		AQUtility.debug("being forwarded!", method);
		
		if(method == null) return;
		
		int id = data.getIntExtra(FW_ID, -1);
		if(id != -1){
			
			Fragment f = act.getSupportFragmentManager().findFragmentById(id);
			
			AQUtility.debug("fr", f);
			
			if(f != null){
				AQUtility.invokeHandler(f, method, false, true, FW_SIG, requestCode, resultCode, data);
			}
			
			
		}
	}
	
	public void result(Activity act, int resultCode, Intent data){
		
		Intent input = act.getIntent();
		
		if(input != null){		
			data.putExtra(FW_CB, input.getStringExtra(FW_CB));
			data.putExtra(FW_ID, input.getIntExtra(FW_ID, -1));
		}
		
		act.setResult(resultCode, data);
		
	}
	
	public PQuery webImage(String url){
		return webImage(url, true, false, 0xFF000000);
	}
	
	public PQuery webImage(String url, boolean zoom, boolean control, int color){
		
		if(view instanceof WebView){
			if(PrefUtility.isTestDevice()){
				debug((WebView) view);
			}
			super.webImage(url, zoom, control, color);
		}
		
		return this;
	}
	
	private void debug(WebView wv){
		wv.setWebChromeClient(new WebChromeClient() {
			  public boolean onConsoleMessage(ConsoleMessage cm) {
			    AQUtility.debug(cm.message() + " -- From line "
			                         + cm.lineNumber() + " of "
			                         + cm.sourceId() );
			    return true;
			  }
			});
	}
}
