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

import greendroid.widget.PageIndicator;
import greendroid.widget.PagedAdapter;
import greendroid.widget.PagedView;
import greendroid.widget.PagedView.OnPagedViewChangeListener;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView.BufferType;

import com.androidquery.callback.AjaxStatus;
import com.androidquery.simplefeed.PQuery;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.base.MenuActivity;
import com.androidquery.simplefeed.data.FeedItem;
import com.androidquery.simplefeed.data.PageAdapter;
import com.androidquery.util.AQUtility;

public class ImageActivity extends MenuActivity {
   
	
	private List<FeedItem> photos;
	private PagedAdapter adapter;
	private PagedView pv;
	private PageIndicator pi;
	private FeedItem item;
	
	
	@Override
	protected void init(Bundle savedInstanceState) {
		
		initView();
		
    	Intent intent = getIntent();
    	if(intent == null) return;
    	
    	String url = intent.getStringExtra("url");
    	
    	if(url == null) return;
    	
    	item = (FeedItem) intent.getSerializableExtra("item");
    	if(item != null){
    		
    		if("photo".equals(item.getType())){
				url = handle.getNetworkUrl(url);
			}
    		
    		
    		if(item.getSource() == null){
    			item.setSource(url);
    			item.setItemName(item.getDesc());   		
    		}
    		
    		photos.add(item);
    		adapter.notifyDataSetChanged();
    		
    		boolean fetchAlbum = intent.getBooleanExtra("album", true);
    		
    		String album = extractAlbum(item);
    		if(fetchAlbum && album != null){
    			ajaxPhoto(album, HALF_DAY);
    		}
    	}
    	
    	
	}
	
	
	@Override
	protected int getMenu(){
		return R.menu.image;
	}
	
	@Override
	protected boolean fullScreen(){
		return true;
	}
	
	//http://www.facebook.com/photo.php?fbid=2415495516098&set=a.1109269421262.17138.1512652234&type=1
	//11-12 18:14:51.300: W/AQuery(5870): clicked:http://www.facebook.com/photo.php?fbid=2563475363381&set=t.577702740&type=1
	//11-12 18:14:51.343: W/AQuery(5870): get:https://graph.facebook.com/577702740&type=1/photos?access_token=AAAEUiDdRZCLIBAMyuPHhGxbX04kuBcZC55NHZAZBKxZAOwGyZCpSGDUre8lonCjtoBiM4O92sx0t5VawnJrTiDne0PVmkVCe4ZD

	private String extractAlbum(FeedItem item){
		
		String link = item.getLink();
		if(link == null) return null;
		
		String result = null;
		
		try{
			
			Uri uri = Uri.parse(link);
			
			String set = uri.getQueryParameter("set");	
			
			if(set != null){
				String[] splits = set.split("\\.");			
				return splits[1];
			}
			
		}catch(Exception e){
			AQUtility.report(e);
		}
		
		
		return result;
		
	}
	

	@Override
	protected int getContainerView() {
		return R.layout.activity_image;
	}
	
	private void ajaxPhoto(String album, long expire){
	    
		String url = "https://graph.facebook.com/" + album + "/photos";
		aq.auth(handle).ajax(url, JSONObject.class, expire, this, "photoCb");
	    
	}	
	
	public void photoCb(String url, JSONObject jo, AjaxStatus status){
		
		AQUtility.debug("photo", jo);
		
		List<FeedItem> news = FeedItem.getItems(jo);
		
		remove(item, news);
		
		photos.addAll(news);
		
		if(photos.size() > 1){
			pi.setDotCount(photos.size());
			pi.setVisibility(View.VISIBLE);
		}
		
		adapter.notifyDataSetChanged();
		
	}
	
	private void remove(FeedItem target, List<FeedItem> items){
		
		for(int i = 0; i < items.size(); i++){
			
			FeedItem item = items.get(i);
			
			if(item.getId().equals(target.getObjectId())){
				
				AQUtility.debug("remove!");
				items.remove(i);
				//target.setItemName(item.getItemName());
				break;
			}
			
		}
		
		
	}
	
	private LruCache<String, View> photoViews = new LruCache<String, View>(5);
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		
		super.onConfigurationChanged(newConfig);
		
		photoViews.evictAll();
		
	}
	
	@Override
	public void refresh(){
		
		photoViews.evictAll();		
		adapter.notifyDataSetChanged();
	}
	
		
	private void initView(){
		
		
		photos = new ArrayList<FeedItem>();
		
		pv = (PagedView) findViewById(R.id.paged);
		
		pi = (PageIndicator) findViewById(R.id.page_indicator);
		
		pv.setOnPageChangeListener(new OnPagedViewChangeListener() {
			
			@Override
			public void onStopTracking(PagedView pagedView) {
			}
			
			@Override
			public void onStartTracking(PagedView pagedView) {
			}
			
			@Override
			public void onPageChanged(PagedView pagedView, int previousPage, int newPage) {			
				pi.setActiveDot(newPage);
			}
		});
		
		
		adapter = new PagedAdapter() {
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				
				if(position >= photos.size()) return PageAdapter.getEmptyView(parent);
				FeedItem item = photos.get(position);				
				
				String url = item.getSource();
				if(url == null) return PageAdapter.getEmptyView(parent);
				
				
				String name = item.getItemName();

				View cached = photoViews.get(url);
				
				if(cached == null){
					convertView = aq.inflate(null, R.layout.item_photo, parent);					
					photoViews.put(url, convertView);
				}else{				
					
					convertView = cached;
					return convertView;
				}
				
				PQuery aq = aq2.recycle(convertView);
				
				
				aq.id(R.id.web).progress(R.id.progress).invisible();
				
				AQUtility.debug("image load url", url);
				
				aq.webImage(url, true, false, 0xFF000000);
				
				aq.id(R.id.text).text(name, BufferType.NORMAL, true);
				
				convertView.setDrawingCacheEnabled(true);
				
				return convertView;
			}
			
			@Override
			public long getItemId(int position) {
				return position;
			}
			
			@Override
			public Object getItem(int position) {
				return photos.get(position);
			}
			
			@Override
			public int getCount() {
				return photos.size();
			}
		};
		
		pv.setAdapter(adapter);
		
	}


    
}
