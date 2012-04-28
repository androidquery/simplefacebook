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

import java.text.DateFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;

import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.base.MenuActivity;
import com.androidquery.simplefeed.data.FeedItem;
import com.androidquery.simplefeed.fragments.CommentFragment;

public class CommentActivity extends MenuActivity{

	@Override
	protected void init(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int getContainerView() {
		return R.layout.activity_comment;
	}

	public static void start(Activity act, FeedItem item){
		
		Intent intent = new Intent(act, CommentActivity.class);
		intent.putExtra("item", item);
		
		act.startActivity(intent);
		
	}
	
	public static void start(Activity act, String itemId){
		
		FeedItem item = new FeedItem();
		item.setId(itemId);
		
		start(act, item);
		
	}

    @Override
    protected String makeTitle(long time){
    	String result = getString(R.string.comment);
    	if(time > 0){
    		result += " - " + DateUtils.formatSameDayTime(time, System.currentTimeMillis(), DateFormat.SHORT, DateFormat.SHORT);
    	}
    	return result;
    }
    
	@Override
	protected int getMenu(){
		return R.menu.comment;
	}
    
    
	@Override
	public void refresh(){
		
		CommentFragment ff = (CommentFragment) getFragment(R.id.frag_comment);
    	ff.refresh();
		
	}
	
}
