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

import java.text.NumberFormat;

import android.text.format.DateUtils;

import com.androidquery.simplefeed.MainApplication;


public class FormatUtility {

	public final static long MINUTE = 60 * 1000;
	public final static long HOUR = 60 * 60 * 1000;
	public final static long DAY = 24 * 3600 * 1000;
	
	public static String signedShort(long n){		
		String str = scientificShort(n);
		if(n >= 0) str = "+" + str;
		return str;
	}
	
	public static String scientificShort(long n){
		
		String result = "0";
		
		double d = n;
		
		if(n >= 1000000){
			d = d / 1000000.0;
			result = format(d, 0, 1) + "m";
		}else if(n >= 1000){
			d = d / 1000.0;
			result = format(d, 0, 1) + "k";
		}else{
			result = Long.toString(n);
		}
		
		return result;
		
	}
	
	public static String format(double n, int minDigit, int maxDigit){
		
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(minDigit);
		nf.setMaximumFractionDigits(maxDigit);
		
		return nf.format(n);
	}
	
	public static CharSequence relativeSmartTime(long now, long time){	
		
		long diff = now - time;
		
		if(diff < DAY){
			return relativeTime(now, time);
		}else{
			return relativePreciseTime(now, time);
		}
		
		
	}
	
	
	public static CharSequence relativeTime(long now, long time){	
		
		if(time == 0){
			return"";
		}
		
		time = Math.min(now - 1000, time);
		
		return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.SECOND_IN_MILLIS, 0);		
	}
	
	public static CharSequence relativePreciseTime(long now, long time){	
		
		if(time == 0){
			return"";
		}
		
		time = Math.min(now - 1000, time);
		
		
		return DateUtils.getRelativeDateTimeString(MainApplication.getContext(), time, DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
	}
	
	/*
	public static String durationTime(long duration){
		StringBuilder sb = new StringBuilder();
		TimeUtils.formatDuration(duration, sb);
		return sb.toString();
	}*/
	
	
	public static String durationTime(long duration){
		
		//return DateUtils.formatElapsedTime(duration / 1000);
		long n = duration / 1000;
		
		long s = n % 60;
		n = n / 60;
		
		long m = n % 60;
		n = n / 60;
		
		long h = n % 24;
		n = n / 24;
				
		long d = n % 365;
		long y = d / 365;
		
		StringBuffer sb = new StringBuffer();
		
		append(sb, y, "y");
		append(sb, d, "d");
		append(sb, h, "h");
		append(sb, m, "m");
		//append(sb, s, "s");
		sb.append(s);
		sb.append("s");
		
		return sb.toString();
	}
	
	private static void append(StringBuffer sb, long n, String unit){
		if(n > 0){
			sb.append(n);
			sb.append(unit);
			sb.append(" ");
		}
	}
	
}
