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
package com.androidquery.simplefeed.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.LocationAjaxCallback;
import com.androidquery.simplefeed.PQuery;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.base.Constants;
import com.androidquery.simplefeed.base.MenuActivity;
import com.androidquery.simplefeed.data.PageAdapter;
import com.androidquery.simplefeed.data.Place;
import com.androidquery.simplefeed.util.JsonUtility;
import com.androidquery.util.AQUtility;

public class PlaceActivity extends MenuActivity{

	private PlacesAdapter places;
	private Location loc;
	private LocationAjaxCallback cb;
	
	@Override
	protected void init(Bundle savedInstanceState) {
		
		initView();
	}
	
	private void initView(){
		
		 
		places = new PlacesAdapter();
    	
    	aq.id(R.id.list);
    	
    	aq.adapter(places).scrolledBottom(this, "scrolledBottom").itemClicked(this, "itemClicked");
    	
    	aq.id(R.id.edit_input).textChanged(this, "searchChanged");
    	aq.id(R.id.button_search).clicked(this, "searchClicked");
    	aq.id(R.id.button_gps).clicked(this, "gpsClicked");
    	
    	refreshButton();
    	
    	ajaxLocation();
	}
	
	public void gpsClicked(View view){
		
		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivityForResult(intent, 2);
		
		
	}
	
	private void stopGps(){
		if(cb != null){
			cb.stop();
			cb = null;
		}
	}
	
	public void ajaxLocation(){
		
		stopGps();
		
		cb = new LocationAjaxCallback();
    	cb.weakHandler(this, "locationCb").timeout(40 * 1000);
    	
    	cb.async(this);
		
	}
	
	private void refreshButton(){
		
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		if(enabled){
    		aq.id(R.id.button_gps).gone();
    	}else{
    		aq.id(R.id.button_gps).visible();
    	}
		
		refreshHint();
		
	}
	
	private void refreshHint(){
		
		/*
		if(loc != null){
			CharSequence time  = FormatUtility.relativeSmartTime(System.currentTimeMillis(), loc.getTime());
			String label = getString(R.string.p_location_update);
			
			String meta = label + ": " + time; 
			aq.id(R.id.text_gps).text(meta);
		}
		*/
	}
	
	public void locationCb(String url, Location loc, AjaxStatus status){
		
		
		AQUtility.debug("cb", loc);
		if(loc != null){
			
			long time = loc.getTime();
			AQUtility.debug("time", new Date(time));
			
			aq.id(R.id.text_progress).gone();
			ajaxPlace(null, loc, -1);
		}else{
			aq.id(R.id.progress).gone();			
			aq.id(R.id.text_progress).visible();
		}
		
		refreshHint();
	}
	
	//https://graph.facebook.com/search?q=coffee&type=place&center=37.76,-122.427&distance=1000&access_token=AAAAAAITEghMBALaQmTPZCTgMLPi3NZC6iZBqF04RQZB2QU0Sp85gRIMGlo9ZCblAV3ZCM9idyrJN6aRVNZCrYovUJvktfRp4hjpjRZCdBOEOd441LSyUsbvL
	private String lastUrl;
	
	private void ajaxPlace(String term, Location loc, long expire){
		
		
		this.loc = loc;
		
		showProgress(true);
		
		String url = "https://graph.facebook.com/search?type=place&distance=10000&limit=100&center=" + loc.getLatitude() + "," + loc.getLongitude() + "&locale=" + locale;
		
		if(term != null && term.length() > 0){
		
			try {
				term = URLEncoder.encode(term, "utf-8");
			} catch(UnsupportedEncodingException e) {
			}
			
			url += "&q=" + term;
		}
		
		lastUrl = url;
		aq.auth(handle).ajax(url, JSONObject.class, expire, this, "placeCb");
		
		
	}
	

	
	public void placeCb(String url, JSONObject jo, AjaxStatus status){
		
		if(!url.equals(lastUrl)) return;
		
		AQUtility.debug("jo", jo);
		showProgress(false);
		aq.id(R.id.progress).gone();
		
		
		if(jo != null){
		
			places.clear();
			
			List<Place> items = Place.getItems(jo);
			
			Place.sortByDistance(loc, items);
			
			String next = JsonUtility.getString(jo, "comments", "paging");		
			places.add(items, next);
			
			/*
			if(status.expired(MONTH)){
				refresh();
			}
			*/
			
			updateTitle(status.getTime().getTime());
			
			aq.id(R.id.list).visible();
			
			AQUtility.debug("done");
			
		}else{
			AQUtility.debug("error!");
		}
	}
	
	public void searchClicked(View view){
		
		
		
		if(loc == null){
			ajaxLocation();
		}else{
		
			String term = aq.id(R.id.edit_input).getEditable().toString();			
			AQUtility.debug("search", term);
			ajaxPlace(term, loc, -1);
		}
		
	}
	
    public void searchChanged(CharSequence s, int start, int before, int count) { 
    	
    	if(s.length() == 0){
    		searchClicked(null);
    	}
    	
	} 
	
	public void itemClicked(AdapterView<?> parent, View v, int pos, long id){
	 
		if(loc == null) return;		
		Place place = places.getItem(pos);
		if(place == null) return;
		
		//ajaxCheckin(item.getId(), "Hello", loc);
		
		
		
		Intent data = new Intent();
		data.putExtra("location", loc);
		data.putExtra("place", place);
		
		AQUtility.debug("finish", loc + ":" + place);
		
		setResult(RESULT_OK, data);
		
		finish();
		
	}
	

	

	@Override
	protected int getContainerView() {
		return R.layout.activity_places;
	}

	public static void start(Activity act){
		
		Intent intent = new Intent(act, PlaceActivity.class);		
		act.startActivityForResult(intent, Constants.ACTIVITY_CHECKIN);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		
		refreshButton();
		
	    super.onActivityResult(requestCode, resultCode, data);
	}

    @Override
    protected String makeTitle(long time){
    	String result = getString(R.string.n_locations);
    	if(time > 0){
    		result += " - " + DateUtils.formatSameDayTime(time, System.currentTimeMillis(), DateFormat.SHORT, DateFormat.SHORT);
    	}
    	return result;
    }
    
	@Override
	protected int getMenu(){
		return R.menu.comment;
	}
    
	@Override
    public void onDestroy(){
    	
		stopGps();
		
		super.onDestroy();
    }
	
	
	@Override
	public void refresh(){
		
		ajaxLocation();
	}
	
    private class PlacesAdapter extends PageAdapter<Place>{
		
		public View render(int position, View convertView, ViewGroup parent) {
			
			Place place = (Place) getItem(position);
			
			if(place == null){
				return getEmptyView(parent);
			}
							
			convertView = aq.inflate(convertView, R.layout.item_place, parent);					
				
			PQuery aq = aq2.recycle(convertView);
			
			
			
			aq.id(R.id.text_name).text(place.getName());
			
			int dist = place.getDistance(loc);
			
			String meta = dist + "m";
			
			String category = place.getCategory();
			String city = place.getCity();
			String country = place.getCountry();
			
			if(category != null){
				meta += ", " + category;
			}
			
			if(city != null){
				meta += ", " + city;
			}
			
			if(country != null){
				meta += ", " + country;
			}
			
			aq.id(R.id.text_meta).text(meta);
			
			return convertView;
			
		}
		
	}

   
	
}
