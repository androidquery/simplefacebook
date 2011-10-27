package com.androidquery.facebook.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;

import com.androidquery.facebook.MainApplication;
import com.androidquery.util.AQUtility;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;


public class PrefUtility {

	private static SharedPreferences pref;
	private static Boolean debugLog;
	
	public static SharedPreferences getPref(){
		if(pref == null){
			pref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getContext());  
		}
			
		return pref;
	}
	
	
	public static void put(String name, String value){
		SharedPreferences.Editor edit = getPref().edit();
		edit.putString(name, value);
		edit.commit();	
	}
	
	public static void put(String name, Long value){
		SharedPreferences.Editor edit = getPref().edit();
		edit.putLong(name, value);
		edit.commit();	
	}
	
	public static Long getLong(String name, Long defaultValue){
		return getPref().getLong(name, defaultValue);
	}
	
	public static String get(String name, String defaultValue){
		return getPref().getString(name, defaultValue);
	}
	
	public static <T extends Enum<T>> void putEnum(Enum<T> value){
		String key = value.getClass().getName();
		put(key, value.toString());
		enums.put(key, value);
	}
	
	
	private static Map<String, Object> enums = new HashMap<String, Object>();
	
	
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T getEnum(Class<T> cls, T defaultValue){
		
		String key = cls.getName();
		
		T result = (T) enums.get(key);
		if(result == null){
			result = PrefUtility.getPrefEnum(cls, defaultValue);
			enums.put(key, result);
		}
		
		return result;
	}
	
	private static <T extends Enum<T>> T getPrefEnum(Class<T> cls, T defaultValue){
		
		T result = null;
		
		String pref = get(cls.getName(), null);
		
		if(pref != null){
			try{
				result = Enum.valueOf(cls, pref); 
			}catch(Exception e){
				AQUtility.report(e);
			}
		}
		
		if(result == null){
			result = defaultValue;
		}
		
		return result;
	}
	
}
