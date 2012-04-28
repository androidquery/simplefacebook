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

import java.io.Serializable;
import java.util.Arrays;

import com.androidquery.simplefeed.util.FormatUtility;
import com.androidquery.util.AQUtility;

public abstract class Data implements Serializable, Cloneable{


	private static final long serialVersionUID = 1L;

	protected long time;
	protected String id;
	
    private String cachedTime;
    private long lastTime;
    
    public String getCachedTime(long now){
    	
    	long diff = now - lastTime;
    	
    	if(cachedTime == null || diff > 300000){    	
    		cachedTime = FormatUtility.relativeSmartTime(now, time) + "";
    		lastTime = now;
    	}
    	
    	return cachedTime;
    }
    
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public boolean resolved(){
		return id == null || id.indexOf('@') == -1;
	}
	
	public String getResolve(){
		
		String[] splits = id.split("\\_");
		
		AQUtility.debug(Arrays.asList(splits));
		
		for(int i = 0; i < splits.length; i++){
			String frag = splits[i];
			if(frag.startsWith("@")){
				return frag.substring(1);
			}
		}
		
		return null;
	}
	
	public String resolve(String resolve, String value){	
		id = id.replace("@" + resolve, value);
		return id;
	}
}
