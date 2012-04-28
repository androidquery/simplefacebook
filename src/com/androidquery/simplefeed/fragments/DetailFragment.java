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
package com.androidquery.simplefeed.fragments;

import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.base.BaseFragment;
import com.androidquery.simplefeed.data.FeedItem;
import com.androidquery.simplefeed.util.IntentUtility;
import com.androidquery.simplefeed.util.PatternUtility;
import com.androidquery.util.AQUtility;

public class DetailFragment extends BaseFragment{

	private FeedItem item;
	
	@Override
	protected void init() {
		
		initView();
	}
	
	private void initView(){

		
		aq.id(R.id.button_comment).clicked(this, "showAll");
		aq.id(R.id.button_expand).clicked(this, "showDetail");
		aq.id(R.id.button_full).clicked(this, "launch");
		aq.id(R.id.button_video).clicked(this, "launch");
		

		
		WebView wv = aq.id(R.id.web).getWebView();

		WebSettings ws = wv.getSettings();
		ws.setJavaScriptCanOpenWindowsAutomatically(false);
		ws.setJavaScriptEnabled(true);
		
		wv.setWebViewClient(new WebViewClient(){
			
			private long loaded = System.currentTimeMillis();
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				
				loaded = System.currentTimeMillis();
				
			}
			
			/*
			@Override
			public void onLoadResource(WebView view, String url) {
				AQUtility.debug("trying to load", url);
			}
			*/
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				AQUtility.debug("trying to open", url);
				
				long now = System.currentTimeMillis();
				
				long diff = now - loaded;
				
				if(diff > 500){
					IntentUtility.openBrowser(act, url);
					return true;
				}
				
				return false;
			}
			
		});
		
	}
	
	public void launch(View view){
		
		IntentUtility.launchActivity(act, item);
		
	}
	
	public void showAll(View view){
		
		show(R.id.frag_comment, true);
		show(R.id.frag_detail, true);
		getView().requestLayout();
		
		aq.id(R.id.button_comment).gone();
		aq.id(R.id.button_expand).visible();
		aq.id(R.id.button_full).visible();
		
		AQUtility.debug("showAll");
	}
	
	public void showComment(View view){
		
		show(R.id.frag_comment, true);
		show(R.id.frag_detail, false);
		getView().requestLayout();
		
		aq.id(R.id.button_comment).gone();
		aq.id(R.id.button_expand).gone();
		aq.id(R.id.button_full).gone();
		
		AQUtility.debug("showComment");
	}
	
	public void showDetail(View view){
		
		show(R.id.frag_comment, false);
		show(R.id.frag_detail, true);
		getView().requestLayout();
		
		aq.id(R.id.button_comment).visible();
		aq.id(R.id.button_expand).gone();
		aq.id(R.id.button_full).visible();
		
		AQUtility.debug("showDetail");
		
	}
	
    private void show(int fragment, boolean show){
    	View view = act.findViewById(fragment);
		if(show){
			view.setVisibility(View.VISIBLE);
		}else{
			view.setVisibility(View.GONE);
		}
    }
    
	private void show(int id){
		
		int[] views = new int[]{R.id.web, R.id.image_box, R.id.video_box};
		
		for(int view: views){
			if(view == id){
				aq.id(view).visible();
			}else{
				aq.id(view).gone();
			}
		}
		
		
	}	
	
    protected void adjust(FeedItem item){
    	
    	boolean comment = item.isCommentable();
    	boolean content = item.isContentLink();
    	int cc = item.getCommentCount();
    	
    	if(content){
    		
    		if(comment && cc > 0){
    			
    			showAll(null);
    		}else{
    			showDetail(null);
    		}
    		
    	}else{
    		showComment(null);
    	}
    	
    }
	


	public void setItem(FeedItem newItem){
		
		if(item != null && item.getId().equals(newItem.getId())){
			return;
		}
		
		this.item = newItem;
		
		adjust(item);
		
		if(!item.isContentLink()) return;
		
		
		
		String type = item.getType();
		String link = item.getLink();
		
		AQUtility.debug(type, link);
		
		boolean handled = false;
		
		if("photo".equals(type)){
		
			String pic = item.getContentTb();
			
			if(pic != null){
				pic = pic.replaceAll("_s.", "_n.");		
				pic = pic.replaceAll("type=album", "type=normal");
				
				pic = handle.getNetworkUrl(pic);
				
		    	aq.id(R.id.web_image).clear().progress(R.id.progress).webImage(pic);
		    	show(R.id.image_box);				
				handled = true;
			}
			
			
		
		}else if("swf".equals(type) || "video".equals(type)){
		
			AQUtility.debug("source", item.getSource());
			
			String key = PatternUtility.extractYoutubeUrlKey(item.getSource());
			String pic = PatternUtility.makeYoutubeImg(key);
			
			AQUtility.debug(key, pic);
			
			if(pic != null){	
		    	aq.id(R.id.web_video).clear().progress(R.id.progress).webImage(pic, false, false, 0xFF000000);		    	
		    	show(R.id.video_box);
		    	handled = true;
			}
			
			
		}
		
		if(!handled){
			show(R.id.web);			
			aq.id(R.id.web).clear().gone().progress(R.id.progress).web(link);			
		}
		
		
	}
	
	
	@Override
	protected int getContainerView() {
		return R.layout.fragment_detail;
	}

}
