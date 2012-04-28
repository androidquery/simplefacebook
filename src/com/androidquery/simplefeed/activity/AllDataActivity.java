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

import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.androidquery.simplefeed.PQuery;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.base.MenuActivity;
import com.androidquery.simplefeed.data.Entity;
import com.androidquery.simplefeed.data.PageAdapter;
import com.androidquery.simplefeed.enums.FeedMode;
import com.androidquery.simplefeed.util.AppUtility;
import com.androidquery.simplefeed.util.ImageUtility;
import com.androidquery.util.AQUtility;

public class AllDataActivity extends MenuActivity{

	private StreamAdapter items;
	
	@Override
	protected void init(Bundle savedInstanceState) {
		
		initView();
	}

	@Override
	protected int getContainerView() {
		return R.layout.activity_all;
	}

	public static void start(Activity act){
		
		Intent intent = new Intent(act, AllDataActivity.class);
		
		act.startActivity(intent);
		
	}
	
	
	

    @Override
    protected String makeTitle(long time){
    	return "All Data";
    }
    
	@Override
	protected int getMenu(){
		return R.menu.comment;
	}
    
    
	@Override
	public void refresh(){
		
		
		
	}
	
	private void initView(){
		
		items = new StreamAdapter();
		
		items.add(Arrays.asList(FeedMode.values()), null);
		
		aq.id(R.id.list).adapter(items).itemClicked(this, "itemClicked");
		
	}
	
	public void itemClicked(AdapterView<?> list, View view, int pos, long id) {
		
		FeedMode item = items.getItem(pos);
		
		AQUtility.debug(item.getDisplay(), item.getHandler());
		
		//start(this, item.getHandler(), item.getUrl(), null);
		
		if(FeedMode.NEWS.equals(item) || FeedMode.WALL.equals(item)){
			Entity source = AppUtility.getDefaultSource(item);
			FeedActivity.start(this, source);
		}else{
			start(this, item.getHandler());
		}
		
		
	}
	
    private void start(Activity act, Class<?> handler){
    	
    	Intent intent = new Intent(act, handler); 
    	
    	act.startActivity(intent);
    	
    }
	
    private class StreamAdapter extends PageAdapter<FeedMode>{
		
		public View render(int position, View convertView, ViewGroup parent) {
			
			if(convertView == null){					
				convertView = getLayoutInflater().inflate(R.layout.item_simple, null);					
				//initItemView(convertView);
			}
			
			PQuery aq = aq2.recycle(convertView);
			
			FeedMode item = (FeedMode) getItem(position);
			
			aq.id(R.id.text_name).text(item.getDisplay());
			
			int icon = item.getIcon();
			String tb = ImageUtility.getProfileTb(handle);
			
			if(icon == 0){				
				aq.id(R.id.image_tb).image(tb);
			}else{
				aq.id(R.id.image_tb).image(icon);
			}
			
			return convertView;
			
		}
		
	};
}
