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
package com.androidquery.simplefeed.callback;

import java.util.Map;

import android.content.Context;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.simplefeed.data.WebPage;
import com.androidquery.simplefeed.util.ParseUtility;
import com.androidquery.simplefeed.util.PatternUtility;
import com.androidquery.util.AQUtility;

public class WebPageAjaxCallback extends AbstractObjectAjaxCallback<WebPage>{

	private static final String VIEWPORT_REG = PatternUtility.makeTagPattern("meta", "name", "viewport");
	
	
	private ImageView ready;
	private WebView wv;
	
	public WebPageAjaxCallback(){
		type(WebPage.class);
	}
	
	
	@Override
	public WebPage transform(String url, byte[] data, AjaxStatus status) {
		
		WebPage wp = null;
		
		try{
		
			String body = new String(data, "UTF-8");
			boolean mobile = isMobile(url, body);
			
			AQUtility.debug("mobile", mobile);
			
			if(mobile){
				//body = patchScale(body);
				//body = stripScript(body);
				//body = PatternUtility.replaceYoutube(body);
			}
			
			wp = new WebPage();
			
			wp.setMobile(mobile);
			
			wp.setHtml(body);
			wp.setOriginal(url);
			wp.setUrl(status.getRedirect());
		
		}catch(Exception e){
			AQUtility.report(e);
		}
		
		
		return wp;
		
	}
	
	public WebPageAjaxCallback web(WebView wv){
		this.wv = wv;
		return this;
	}
	
	@Override
	public void callback(String url, WebPage wp, AjaxStatus status) {
		
		AQUtility.debug("web cb");
		
		if(wp != null){
			setHtmlCheckMobile(wv, wp);
		}
		
		setReady(ready, wp);
		
	}
	
	//12-14 01:12:17.980: W/AQuery(16415): java.lang.IllegalArgumentException: Invalid % sequence: %Sl in fragment at index 170: http://www.lee.com/store/LEE_STORE_US/en_US/category/women/jeans.html#count=16&sort=iphrase%20bundle%20taxonomy%20sequence_ASCENDING&pageNumber=0&filter1=Slender%20Secret%Slender%20Secret


	@Override
	public void async(Context context){
		
		String url = getUrl();
		
		if(!isValid(url)){
			AQUtility.debug("invalid url", url);
			wv.loadUrl(url);
			wv.setVisibility(View.VISIBLE);
			return;
		}
		
		if(wv != null){
			if(url.equals(wv.getTag(AQuery.TAG_URL))){
				return;
			}
			wv.setTag(AQuery.TAG_URL, url);
		}
		if(ready != null){
			if(url.equals(ready.getTag(AQuery.TAG_URL))){
				return;
			}
			ready.setTag(AQuery.TAG_URL, url);
		}
		
		super.async(context);
	}
	
	private boolean isValid(String url){
		try{
			java.net.URI.create(url);
			return true;
		}catch(Exception e){
		}
		return false;
	}
	
	/*
	public void async(Context context, String url, boolean fileCache, boolean refresh){
		
		if(wv != null){
			if(url.equals(wv.getTag())){
				return;
			}
			wv.setTag(url);
		}
		if(ready != null){
			if(url.equals(ready.getTag())){
				return;
			}
			ready.setTag(url);
		}
		
		url(url).fileCache(fileCache).refresh(refresh);
		async(context);
	}
	*/

	
	public static String patchScale(String html){
		String replacement = "<meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=3.0; user-scalable=1;\"/>";
		return html.replaceAll(VIEWPORT_REG, replacement);
	}
	
	private static String stripScript(String html){
		
		String pattern = "<script[^>]*>[^>]*/script>";
		
		html = html.replaceAll(pattern, "");
		
		return html;
	}
	
	private static void setHtmlCheckMobile(WebView wv, WebPage wp){
    	
		if(wv == null || !wp.getOriginal().equals(wv.getTag(AQuery.TAG_URL))){
			return;
		}
		
		
		try{
			
			if(wp.getWidth() > 300){
				int scale = checkScale(wv, wp.getWidth());
				if(scale > 100){
					wv.setInitialScale(scale);
				}
			}
			
			setWebPage(wv, wp);
			wv.setTag(AQuery.TAG_URL, wp);
			
			
			
		}catch(Exception e){
			AQUtility.report(e);
		}
    }
	

	
	
	public static int checkScale(WebView wv, long width){
		
		float bodyWidth = width;
		
		float viewWidth = wv.getWidth();
		
		if(bodyWidth > viewWidth - 50){
			return 100;
		}
		
		
		float scale = viewWidth / bodyWidth;
		
		int zoom = (int) (scale * 100f);
		
		zoom = Math.max(100, zoom);
		
		return zoom;
	}
	
	public static void setWebPage(WebView wv, WebPage wp){
		
		try{
			
			wv.setVisibility(View.VISIBLE);
			
			AQUtility.debug("load", wp.getUrl());
			wv.loadDataWithBaseURL(wp.getUrl(), wp.getHtml(), "text/html", "utf-8", null);
			
		}catch(Exception e){
			AQUtility.report(e);
		}
	}
	
	private static void setReady(ImageView imageView, WebPage wp){
    	
		if(imageView == null || !wp.getOriginal().equals(imageView.getTag(AQuery.TAG_URL))){
			return;
		}
		
		try{
			
			
			//imageView.setImageResource(R.drawable.ic_online);
			
	    	
	    	
		}catch(Exception e){
			AQUtility.report(e);
		}
    }
	
	public static boolean isMobile(String url, String body){
		if(url.contains("http://m.")){
			return true;
		}
		
		String viewport = PatternUtility.match(body, VIEWPORT_REG);
		if(viewport == null) return false;
		
		try{
			Map<String, String> atts = PatternUtility.toAttributes(viewport);
			String content = atts.get("content");
			return isMobile(content);
		}catch(Exception e){
			AQUtility.report(e);
		}
		
		return true;
		
	}
	
	private static boolean isMobile(String viewport){
		if(viewport == null) return false;
			
		Map<String, String> atts = PatternUtility.splitQuery(viewport, "[,;]");
		String width = atts.get("width");
		
		AQUtility.debug("width param", width);
		
		if("device-width".equals(width)){
			return true;
		}
		
		Long n = ParseUtility.toNumber(width);
		if(n == null || n > 500){
			return false;
		}
		
		return true;
		
	}
	
}
