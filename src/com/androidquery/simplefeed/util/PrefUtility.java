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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.androidquery.simplefeed.MainApplication;
import com.androidquery.simplefeed.R;
import com.androidquery.util.AQUtility;


public class PrefUtility {

	private static SharedPreferences pref;
	
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
	
	public static void put(String name, Boolean value){
		SharedPreferences.Editor edit = getPref().edit();
		edit.putBoolean(name, value);
		edit.commit();	
	}
	
	public static boolean contains(String name){		
		return getPref().contains(name);
	}
	
	public static boolean getBoolean(String name, boolean defaultValue){		
		return getPref().getBoolean(name, defaultValue);
	}
	
	public static Long getLong(String name, Long defaultValue){
		return getPref().getLong(name, defaultValue);
	}
	
	public static String get(String name, String defaultValue){
		return getPref().getString(name, defaultValue);
	}
	
	public static <T extends Enum<T>> void putEnum(Enum<T> value){
		String key = value.getClass().getName();
		put(key, value.name());
		enums.put(key, value);
	}
	
	public static void clearEnum(Class<?> cls){
		String key = cls.getName();
		put(key, (String) null);
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
				clearEnum(cls);
				AQUtility.report(e);
			}
		}
		
		if(result == null){
			result = defaultValue;
		}
		
		return result;
	}
	
	//12-30 03:04:49.125: W/System.err(20443): device:ffffffff-b588-0cd1-ffff-ffffb12a7939

	private static String[] deviceIds = {"ffffffff-b588-0cd1-ffff-ffffb12a7939", "00000000-582e-8c83-ffff-ffffb12a7939", "ffffffff-a7af-71df-0033-c5870033c587", "00000000-2e56-36d7-ffff-ffffb12a7939"};
	private static Boolean testDevice;
	
	public static boolean isTestDevice(){
		
		if(testDevice == null){
			testDevice = isEmulator() || isTestDevice(getDeviceId());
		}
		
		return testDevice;
	}
	
	public static boolean isEmulator(){
		return "sdk".equals(Build.PRODUCT);
	}
	
	private static boolean isTestDevice(String deviceId){
		
		for(int i = 0; i < deviceIds.length; i++){
			if(deviceIds[i].equals(deviceId)){
				return true;
			}
		}
		return false;
	}
	
	private static String deviceId;
	public static String getDeviceId(){
		
		if(deviceId == null){
		
			Context context = MainApplication.getContext();
			
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	
		    String tmDevice, tmSerial, tmPhone, androidId;
		    tmDevice = "" + tm.getDeviceId();
		    tmSerial = "" + tm.getSimSerialNumber();
		    androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	
		    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
		    deviceId = deviceUuid.toString();
		    
		}
		
		System.err.println("device:" + deviceId);
		
	    return deviceId;
	}
	
	
	public static String getConfig(int id){
		
		try{
			return MainApplication.get(R.string.tablet);
		}catch(Exception e){
			AQUtility.report(e);
		}
		return null;
	}
}
