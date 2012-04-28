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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.location.Location;

import com.androidquery.simplefeed.util.ParseUtility;

public class Place implements Serializable, Cloneable{

	private static final long serialVersionUID = 1L;
	
	private double latitude;
	private double longitude;
	private String city;
	private String country;
	private String name;
	private String id;
	private String category;
	private String tb;
	
	public Place(){
		
	}
	
	public Place(JSONObject jo){
		
		name = jo.optString("name");
		id = jo.optString("id");
		category = jo.optString("category");
		
		JSONObject lo = jo.optJSONObject("location");
		
		if(lo != null){
			latitude = lo.optDouble("latitude", 0);
			longitude = lo.optDouble("longitude", 0);
			city = lo.optString("city", null);
			country = lo.optString("country", null);
		}
		
		if(id != null){
			tb = ParseUtility.profileTb(id);// + "#" + name;
		}
	}
	
	public static List<Place> getItems(JSONObject jo){
		JSONArray data = jo.optJSONArray("data");	
		List<Place> feed = toList(data);
		return feed;
	}
	
	private static List<Place> toList(JSONArray ja){
		
		List<Place> result = new ArrayList<Place>();
		
		for(int i = 0; i < ja.length(); i++){
			result.add(new Place(ja.optJSONObject(i)));
		
		}
		
		return result;
	}
	
	
	public static Place make(JSONObject jo){
		
		if(jo == null) return null;
		return new Place(jo);
		
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
	
	public static void sortByDistance(Location loc, List<Place> items){
		
		Collections.sort(items, new PlaceComparator(loc));
		
	}
	
	private static class PlaceComparator implements Comparator<Place>{

		private Location loc;
		
		public PlaceComparator(Location loc){
			this.loc = loc;
		}
		
		
		@Override
		public int compare(Place lhs, Place rhs) {
			
			int d1 = lhs.getDistance(loc);
			int d2 = rhs.getDistance(loc);
			
			return d1 - d2;
			
		}
		
	}
	
	
	
	private int distance = -1;
	
	public int getDistance(Location loc){
		
		if(distance == -1){
			distance = (int) distFrom(getLatitude(), getLongitude(), loc.getLatitude(), loc.getLongitude());
		}
		
		return distance;
	}
	
	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
		    
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2-lat1);
		double dLng = Math.toRadians(lng2-lng1);
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
           Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
           Math.sin(dLng/2) * Math.sin(dLng/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = earthRadius * c;

		int meterConversion = 1609;
		return new Double(dist * meterConversion);
		 
		 
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	public void setTb(String tb) {
		this.tb = tb;
	}

	public String getTb() {
		return tb;
	}
}
