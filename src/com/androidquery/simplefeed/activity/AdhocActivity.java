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

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.androidquery.AQuery;
import com.androidquery.auth.FacebookHandle;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.simplefeed.PQuery;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.data.Comment;
import com.androidquery.simplefeed.data.FeedItem;
import com.androidquery.simplefeed.data.PageAdapter;
import com.androidquery.simplefeed.util.AppUtility;
import com.androidquery.simplefeed.util.JsonUtility;
import com.androidquery.util.AQUtility;

public class AdhocActivity extends Activity{


	private PQuery aq;
	private PQuery aq2;
	
	@Override
    public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_adhoc);
		
		aq = new PQuery(this);
		aq2 = new PQuery(this);
		
		initView();
		
		
	}
	
	private void initView(){
		
		//12-15 18:57:48.805: W/AQuery(15543): get:https://graph.facebook.com/107065679322731_325851500777480?locale=en_US&access_token=BAAEUiDdRZCLIBALBHKB7lV6WBa4WBeT2QpCn6zvqXjnTVv2Qh8Fwgbb0KOFsggp7dYbwFZAjQDKxZArcEp0KP0uZAdxH9PUmnO0MleRxmz0Se9FXDqEzQn6olKlDwKcZD

		FacebookHandle handle = AppUtility.makeHandle(this);
		
		String url = "https://graph.facebook.com/107065679322731_325851500777480";
		aq.auth(handle).ajax(url, JSONObject.class, this, "jsonCb");
		
	}
	
	public void jsonCb(String url, JSONObject jo, AjaxStatus status){
		
		AQUtility.debug(jo);
		
		FeedItem item = new FeedItem(jo);
		
		String next = JsonUtility.getString(jo, "comments", "paging");
		
		CommentAdapter comments = new CommentAdapter();
		
		comments.add(item.getComments(), next);
		
		aq.id(R.id.list).adapter(comments);
		
	}
	
	
	 
    private class CommentAdapter extends PageAdapter<Comment>{
		
		public View render(int position, View convertView, ViewGroup parent) {
			
			
			if(convertView == null){					
				convertView = getLayoutInflater().inflate(R.layout.item_adhoc, null);		
			}
			
			PQuery aq = aq2.recycle(convertView);
			
			Comment item = (Comment) getItem(position);
			
			if(item.getName() != null){
				
				String msg = item.getMessage();
				char[] chars = new char[msg.length() * 2];
				
				for(int i = 0; i < chars.length; i++){
					if(i % 2 == 0){
						char c = (char) ('吧' + i);
						chars[i] = c;
					}else{
						chars[i] = ' ';
					}
				}
				
				item.setMessage(new String(chars));
				
				item.setName(null);
				
				AQUtility.debug("init");
			}
			
			aq.id(R.id.desc).setLayerType11(AQuery.LAYER_TYPE_SOFTWARE, null).text(item.getMessage2(), true);
			
			return convertView;
			
		}
		
	};
}
