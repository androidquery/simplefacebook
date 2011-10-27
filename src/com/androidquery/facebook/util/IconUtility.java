package com.androidquery.facebook.util;

import java.util.HashMap;
import java.util.Map;

import com.androidquery.facebook.MainApplication;

import android.graphics.drawable.Drawable;

public class IconUtility {

	private static Map<Integer, Drawable> cache = new HashMap<Integer, Drawable>();
	
	public static Drawable getCached(int resId){
		
		if(resId == 0) return null;
		
		Drawable result = cache.get(resId);
		
		if(result == null){
			result = MainApplication.getContext().getResources().getDrawable(resId);
			cache.put(resId, result);
		}
		
		return result;
		
		
	}
	
}
