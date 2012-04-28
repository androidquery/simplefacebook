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
package com.androidquery.simplefeed.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.androidquery.simplefeed.util.JsonUtility;

public class Feed extends ArrayList<FeedItem>{

	private static final long serialVersionUID = 1L;
	
	private String next;
	
	public Feed(JSONObject jo){
		
		next = JsonUtility.getString(jo, "paging", "next");
		
		JSONArray data = jo.optJSONArray("data");
		parseList(data);
		
	}
	
	private void parseList(JSONArray ja){
		
		for(int i = 0; i < ja.length(); i++){
			add(new FeedItem(ja.optJSONObject(i)));
		}
		
	}

	public String getNext() {
		return next;
	}
	
}
