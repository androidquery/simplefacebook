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
package com.androidquery.simplefeed.fragments;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.simplefeed.PQuery;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.activity.CommentActivity;
import com.androidquery.simplefeed.activity.FeedActivity;
import com.androidquery.simplefeed.base.BaseFragment;
import com.androidquery.simplefeed.data.Entity;
import com.androidquery.simplefeed.data.FeedItem;
import com.androidquery.simplefeed.data.PageAdapter;
import com.androidquery.simplefeed.util.ErrorReporter;
import com.androidquery.simplefeed.util.IntentUtility;
import com.androidquery.simplefeed.util.JsonUtility;
import com.androidquery.simplefeed.util.ParseUtility;
import com.androidquery.simplefeed.util.PrefUtility;
import com.androidquery.util.AQUtility;

public class NotificationFragment extends BaseFragment{

	private PageAdapter<FeedItem> notis;
	
    @Override
    protected void init(){
    	
    	initView();
    	
    	
    }
    
    private void initView(){
    	
    	notis = new NotificationAdapter();
    	
    	aq.id(R.id.list);
    	
    	aq.adapter(notis).scrolledBottom(this, "scrolledBottom").itemClicked(this, "itemClicked");
    	
    	
    	ajaxNoti(0);
    }
    

    
    public void itemClicked(AdapterView<?> parent, View v, int pos, long id){
    	
    	FeedItem item = (FeedItem) notis.getItem(pos);
    	if(item == null) return;
    	
    	//TODO debug
    	if(PrefUtility.isTestDevice()){
    		//item.setLink("http://www.facebook.com/groups/av8dbuy/");
    		//item.setLink("http://www.facebook.com/events/248459785218011/");
    		//item.setLink("http://www.facebook.com/hsu.mingchin/posts/10150431527981089?cmntid=10150431631416089");
    		//item.setLink("http://www.facebook.com/event.php?eid=257399824315939&view=wall");
    		//https://graph.facebook.com/0_276142965771432?locale=fr_FR
    		//item.setLink("http://www.facebook.com/photo.php?fbid=101s50467124669668&set=a.287473759667.144316.208428464667&type=1");
    		//item.setLink("http://www.facebook.com/photo.php?v=10150448332401110");
    	}
    	
    	
    	String link = item.getLink();   
    	if(link == null) return;
    	
    	Uri uri = Uri.parse(link);   
    	
    	String fbId = extractItemId(item, link, uri);

    	AQUtility.debug(item.getLink(), fbId);
    	AQUtility.debug(item.getTo(), item.getTo().getName());
    	
    	ajaxRead(item);
    	
    	
    	if(fbId != null){
    		CommentActivity.start(act, fbId);  
    		return;
    	}
    	
    	fbId = extractUserId(item, link, uri);
    	
    	AQUtility.debug("userid", fbId);
    	
    	if(fbId != null){
    		Entity source = new Entity();
    		source.setId(fbId);
    		source.setName(fbId);
    		FeedActivity.start(act, source);     	
    		return;
    	}
    	
    	if(openBrowser(item, link, uri)){
    		IntentUtility.openBrowser(act, link);
    		return;
    	}
    	
    	act.showToast(getString(R.string.marked_read));
    	
    	if(shouldIgnore(item, link, uri)){
    		return;
    	}
    	
    	ErrorReporter.report("can't open:" + item.getType() + ":" + item.getLink() + ":" + item.getDesc());

    }
    
    private boolean openBrowser(FeedItem item, String link, Uri uri){
    	
    	if(link.startsWith("http://apps.facebook.com")){
    		return true;
    	}else if(link.contains("/groups/")){
    		return true;
    	}
    	
    	return false;
    }
    
    private boolean shouldIgnore(FeedItem item, String link, Uri uri){
    	if(link.startsWith("http://www.facebook.com/games")){
    		return true;
    	}
    	return false;
    }
    
    private String extractItemId(FeedItem item, String link, Uri uri){
    	
    	String result = null;
    	
    	try{
    	 
    		result = uri.getQueryParameter("fbid");
    		if(result == null) result = getPostId(item, link, uri);
    		if(result == null) result = getStoryId(item, link);
    		if(result == null) result = getNoteId(item, link, uri);
    		if(result == null) result = getVideoId(item, link, uri);
    	}catch(Exception e){
    		AQUtility.report(e);
    	}
    	
    	return result;
    }
    
    private String extractUserId(FeedItem item, String link, Uri uri){
    	
    	String result = null;
    	
    	try{
    		    		
    		result = getProfileId(link, uri);
    		if(result == null) result = getWallId(link);
    		if(result == null) result = getGroupId(link, uri);
    		if(result == null) result = getEventId(link, uri);
    		if(result == null) result = getAliasId(link, uri);
    	}catch(Exception e){
    		AQUtility.report(e);
    	}
    	
    	return result;
    }
    
    private String getWallId(String link){
    	
    	if(link.contains("sk=wall")){
    		return "me";
    	}
    	
    	return null;
    }
    
    private String getProfileId(String link, Uri uri){
    	
    	if(link.contains("profile")){
    		return uri.getQueryParameter("id");
    	}
    	
    	return null;
    }
    
    private String getVideoId(FeedItem item, String link, Uri uri){
    	
    	String result = null;
    	
    	try{
        	 	    	
    		if(link.contains("photo.php")){
    			result = uri.getQueryParameter("v");   		
    		}
    		
    	}catch(Exception e){
    		AQUtility.report(e);
    	}
    	
    	return result;
    }
    
    
    private String getStoryId(FeedItem item, String link){
    	
    	String result = null;
    	
    	try{
        	
    		Uri uri = Uri.parse(link);   	    		
    		String sid = uri.getQueryParameter("story_fbid");
    		String id = uri.getQueryParameter("id");
    		
    		
    		if(sid != null && id != null){
    			result = id + "_" + sid;
    		}
    		
    	}catch(Exception e){
    		AQUtility.report(e);
    	}
    	
    	return result;
    }
    
    //http://www.facebook.com/ricky.f.cho/posts/333420366675124?from_close_friend=1:null 
    private String getPostId(FeedItem item, String link, Uri uri){
    	
    	List<String> paths = uri.getPathSegments();
    	
    	AQUtility.debug("paths", paths);
    	
    	if(paths.size() < 3) return null;
    	
    	if(!"posts".equals(paths.get(paths.size() - 2))) return null;
    	
    	String last = ParseUtility.resolveId(paths.get(0)) + "_" + paths.get(paths.size() - 1);
    	
    	return last;
    	
    }
    
    //12-08 00:58:39.960: W/AQuery(12570): http://www.facebook.com/notes/sunset-liu/test-note/2825427477667:null
    private String getNoteId(FeedItem item, String link, Uri uri){
    	
    	List<String> paths = uri.getPathSegments();
    	
    	AQUtility.debug("paths", paths);
    	
    	if(paths.size() < 3) return null;
    	
    	if(!"notes".equals(paths.get(0))) return null;
    	
    	String last = paths.get(paths.size() - 1);
    	
    	return last;
    	
    }
    
    //http://www.facebook.com/event.php?eid=257399824315939&view=wall
    //"http://www.facebook.com/events/248459785218011/"
    private String getEventId(String link, Uri uri){
    	
    	List<String> paths = uri.getPathSegments();
    	
    	AQUtility.debug(paths);

    	String eid = uri.getQueryParameter("eid");
    	if(eid != null){
    		return eid;
    	}
    	
    	if(paths.size() < 2) return null;
    	
    	if("events".equals(paths.get(0))){
    		return paths.get(1);
    	}
    	
    	
		return null;
		
    }
    
    
    
    
    
    
    //12-02 11:07:22.527: W/AQuery(24527): http://www.facebook.com/groups/304970649523214/:null
    private String getGroupId(String link, Uri uri){
    	
    	if(link.indexOf("group.php") > 0){
    		String gid = uri.getQueryParameter("gid");
    		if(gid != null) return gid;
    	}
    	
    	if(link.charAt(link.length() - 1) == '/'){
    		link = link.substring(0, link.length() - 1);
    	}
    	
    	AQUtility.debug("check link", link);
    	
    	String[] splits = link.split("/");
    	if(splits.length < 2) return null;
		
    	
    	String last = null;
    	
    	if("groups".equals(splits[splits.length - 2])){
    		last = splits[splits.length - 1];
    	}else if("groups".equals(splits[splits.length - 3])){
    		last = splits[splits.length - 2];
    	}
    	
    	
    	if(last == null) return null;
    	
    	try{
			
			Long.parseLong(last);			
			return last;
			
		}catch(NumberFormatException e){
		}catch(Exception e){
			AQUtility.report(e);
		}
    	
		return null;
		
    }
    
    //12-02 11:41:19.023: W/AQuery(25185): http://www.facebook.com/hanshop.uk:null
    private String getAliasId(String link, Uri uri){
    	
    	String query = uri.getQuery();
    	
    	
    	if(query == null || query.length() == 0){
	    	
	    	String[] splits = link.split("/");
	    	if(splits.length < 2) return null;
	    	
	    	if(splits[splits.length - 2].equalsIgnoreCase("www.facebook.com")){
	    		return splits[splits.length - 1];
	    	}
	    	
    	}
    	return null;
    }
   
	@Override
	protected int getContainerView() {
		return R.layout.fragment_notification;
	}

	public void refresh(){
		ajaxNoti(-1);
	}
	
	private void ajaxNoti(long expire){
		
		act.showProgress(true);
		
		String url = getNotiUrl();
		aq.auth(handle).ajax(url, JSONObject.class, expire, this, "notiCb");
		
	}
	
	private String getNotiUrl(){
		String url = "https://graph.facebook.com/me/notifications?include_read=1&locale=" + locale;
		return url;
	}
	
	
	public void notiCb(String url, JSONObject jo, AjaxStatus status){
		
		AQUtility.debug("jo", jo);
		
		act.showProgress(false);
		
		if(jo != null){
		
			notis.clear();
			
			List<FeedItem> items = FeedItem.getItems(jo);
			
			String next = JsonUtility.getString(jo, "comments", "paging");//jo.optJSONObject("comments").optJSONObject("paging").optString("next", null);			
			notis.add(items, next);
			
			
			if(status.expired(TEN_MIN)){
				refresh();
			}
			
			
			act.updateTitle(status.getTime().getTime());
			
			aq.id(R.id.list).visible();
			
			AQUtility.debug("done");
			
		}else{
			AQUtility.debug("error!");
		}
		
		
	}
	 
	private void ajaxRead(FeedItem item){
		
		if(!item.isUnread()) return;
		
		item.setUnread(false);
		
		String url = "https://graph.facebook.com/" + item.getId() + "?unread=0";
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("unread", "0");
		
		
		aq.auth(handle).ajax(url, params, String.class, this, "readCb");
		
	}
	
	
	public void readCb(String url, String str, AjaxStatus status){
		
		AQUtility.debug("jo", str);
		
		if(str != null){
			aq.invalidate(getNotiUrl());		
			notis.notifyDataSetChanged();
		}
	}
	
	
	
    private class NotificationAdapter extends PageAdapter<FeedItem>{
		
		public View render(int position, View convertView, ViewGroup parent) {
			
			
			//if(isLoading(position)){
			//	return getLoadingView(R.layout.item_progress);
			//}
			
			boolean init = convertView == null;
			
			convertView = aq.inflate(convertView, R.layout.item_notification, parent);
			
			if(init){									
				//initItemView(convertView);
			}
			
			PQuery aq = aq2.recycle(convertView);
			
			FeedItem item = (FeedItem) getItem(position);
			aq.tag(item);
			
			
			Entity from = item.getFrom();
			//Entity to = item.getTo();
			
			aq.id(R.id.text_name).text(from.getName());
			
			aq.id(R.id.text_title).text(item.getTitle());
			
			aq.id(R.id.text_desc).text(item.getDesc2(), true);
			//aq.id(R.id.text_desc).text(item.getDesc());
			
			aq.id(R.id.text_meta).text(item.getCachedTime(System.currentTimeMillis()));
			
			String tb = from.getTb();
			
			
			if(aq.shouldDelay(convertView, parent, tb, 0, false)){
				aq.id(R.id.image_tb).clear();
			}else{
				aq.id(R.id.image_tb).image(tb, true, true, 0, R.drawable.ic_menu_report_image, null, AQuery.FADE_IN_FILE);
			}
			
			if(!item.isUnread()){
				convertView.setBackgroundColor(0xFFEEEEEE);
			}else{
				convertView.setBackgroundColor(0xFFFFFFFF);
			}
			
			
			return convertView;
			
		}
		
	};
}
