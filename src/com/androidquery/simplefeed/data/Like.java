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

import static com.androidquery.simplefeed.util.ParseUtility.profileTb;

import java.io.Serializable;

import org.json.JSONObject;

public class Like implements Serializable, Cloneable{

	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String userId;
	private String tb;
	
	public Like(JSONObject jo){
		
		userId = jo.optString("id");
		name = jo.optString("name");
		
		tb = profileTb(userId);
		
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	

	public void setTb(String tb) {
		this.tb = tb;
	}

	public String getTb() {
		return tb;
	}
	
}
