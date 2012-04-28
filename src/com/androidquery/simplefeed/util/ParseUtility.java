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
package com.androidquery.simplefeed.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.androidquery.util.AQUtility;

public class ParseUtility {

	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	public static long parseTime(String time){
		
		if(time == null) return 0;
		
		Date date;
		try {
			date = df.parse(time);
			return date.getTime();
		} catch (ParseException e) {
			AQUtility.report(e);
		}
		
		return 0;
	}
	
	public static boolean isYT(String url){
		
		if(url == null) return false;
		
		return url.startsWith("http://www.youtube.com");
		
	}
	
	public static String profileTb(String id){
    	String url = "http://graph.facebook.com/" + id + "/picture";
    	return url;
    }
	
	public static boolean isNumber(String str){
		
		return toNumber(str) != null;
		
	}
	
	public static Long toNumber(String str){
		
		try{
			return new Long(str);
		}catch(Exception e){
			return null;
		}
		
	}
	
	public static String resolveId(String id){
		if(id == null) return null;
		if(isNumber(id)) return id;
		return "@" + id;
		
	}
	
}
