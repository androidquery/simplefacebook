package com.androidquery.facebook.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.androidquery.util.AQUtility;

public class ParseUtility {

	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
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
}
