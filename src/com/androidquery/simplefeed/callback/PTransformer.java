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

import org.json.JSONObject;
import org.json.JSONTokener;

import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.Transformer;
import com.androidquery.simplefeed.data.Feed;
import com.androidquery.util.AQUtility;

public class PTransformer implements Transformer{

	@Override
	public <T> T transform(String url, Class<T> type, String encoding, byte[] data, AjaxStatus status) {
		
		if(type.equals(Feed.class)){
			
			Feed result = null;
	    	
	    	try {    		
	    		String str = new String(data, encoding);
	    		JSONObject jo = (JSONObject) new JSONTokener(str).nextValue();
	    		result = new Feed(jo);
			} catch (Exception e) {	  		
				AQUtility.debug(e);
			}
			return (T) result;
		}
		
		return null;
	}

	
	
}
