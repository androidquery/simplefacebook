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

import static com.androidquery.simplefeed.util.ParseUtility.isYT;
import static com.androidquery.simplefeed.util.ParseUtility.parseTime;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.net.Uri;

import com.androidquery.simplefeed.MainApplication;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.ui.LayoutString;
import com.androidquery.simplefeed.util.AppUtility;
import com.androidquery.simplefeed.util.FormatUtility;

public class FeedItem extends Data {

	private static final long serialVersionUID = 1L;
	
	private Entity from;
	private Entity to;
	
	private String itemName;
	private String objectId;
	private String message;
	private String name;
	private String story;
	private String subject;
	
	private int likeCount;
	private int commentCount;
	
	private String contentTb;
	private String contentName;
	private String contentDesc;
	private String contentMeta;
	private String link;
	private String type;
	private String source;
	private String title;
	
	private int actionIcon;
	private String appId;
	
	private List<Comment> comments;
	private List<Like> likes;
	
	private Place place;
	
	private boolean commentable;
	private boolean likeable;
	private boolean liked;
	
	private boolean unread;
	
	public FeedItem(){
		comments = new ArrayList<Comment>();
		likes = new ArrayList<Like>();
	}
	
	public FeedItem(JSONObject jo){
		
		id = jo.optString("id");
		
		from = Entity.make(jo.optJSONObject("from"));
		
		parseTo(jo);
		
		itemName = jo.optString("name", null);
		
		message = jo.optString("message", null);
		story = jo.optString("story", null);
		name = jo.optString("name", null);
		
		title = jo.optString("title", null);
		unread = jo.optInt("unread", 1) == 1;
		
		parseAction(jo);
		parsePlace(jo);
		
		contentTb = jo.optString("picture", null);
		source = jo.optString("source", null);
		
		subject = jo.optString("subject", null);
		
		contentName = jo.optString("name", "");
		contentMeta = jo.optString("caption", null);
		contentDesc = jo.optString("description", null);
	
		time = parseTime(jo.optString("created_time", null));
		
		JSONObject jolikes = jo.optJSONObject("likes");
		if(jolikes != null){
			likeCount = jolikes.optInt("count", 0);
			likes = parseLikes(jolikes.optJSONArray("data"));
		}
		
		JSONObject jocomments = jo.optJSONObject("comments");
		if(jocomments != null){
			commentCount = jocomments.optInt("count", 0);
			comments = parseComments(jocomments.optJSONArray("data"), commentCount);
		}
		
		link = jo.optString("link", null);
		type = jo.optString("type", null);
		objectId = jo.optString("object_id", null);
		
		parseIcon(link, type);
	
		parseApp(jo);
		parsePhoto(jo);
	}
	
	private void parsePhoto(JSONObject jo){
		
		if(contentTb == null && objectId != null && "photo".equals(type)){
			contentTb = "https://graph.facebook.com/" + objectId + "/picture?type=album";
		}
		
	}
	
	private void parseApp(JSONObject jo){
		
		jo = jo.optJSONObject("application");
		if(jo != null){
			appId = jo.optString("id");
		}
		
	}
	
	public String getMessage() {
		return message;
	}

	private void parsePlace(JSONObject jo){
		
		JSONObject p = jo.optJSONObject("place");
		if(p != null){
			place = new Place(p);
		}
		
	}
	
	private void parseTo(JSONObject jo){
		
		JSONObject to = jo.optJSONObject("to");
		
		if(to != null){
			JSONArray data = to.optJSONArray("data");
			if(data != null && data.length() > 0){
				this.to = Entity.make(data.optJSONObject(0));
			}else if(to.has("name") && to.has("id")){
				this.to = new Entity(to);
			}
		}
	}
	
	private List<Like> parseLikes(JSONArray data){
		
		List<Like> result = new ArrayList<Like>();
		if(data == null) return result;
		
		for(int i = 0; i < data.length(); i++){
			result.add(new Like(data.optJSONObject(i)));
		}
		
		return result;
	}
	
	private List<Comment> parseComments(JSONArray data, int count){
		
		List<Comment> result = new ArrayList<Comment>();
		 
		if(data != null){
			for(int i = 0; i < data.length(); i++){
				result.add(new Comment(data.optJSONObject(i)));
			}
		}
		
		
		if(count > 0 && result.size() == 0){
			Comment dummy = new Comment();
			dummy.setMessage(count + " " + MainApplication.get(R.string.comments));
			String tb = getFrom().getTb();
			dummy.setTb(tb);
			result.add(dummy);
		}
		
		return result;
	}
	
	private void parseAction(JSONObject jo){
		
		JSONArray ja = jo.optJSONArray("actions");
		
		if(ja != null){
			
			for(int i = 0; i < ja.length(); i++){
				
				JSONObject act = ja.optJSONObject(i);
				String name = act.optString("name");
				
				if("Like".equals(name)){
					likeable = true;
				}else if("Comment".equals(name)){
					commentable = true;
				}
				
			}
			
		}
		
		if("photo".equals(jo.optString("type"))){
			likeable = true;
			commentable = true;
		}
		
	}
	
	
	private void parseIcon(String link, String type){
		
		if(link == null) return;
		
		if(isYT(link)){
			actionIcon = R.drawable.ic_media_play;
		}else if("photo".equals(type)){
			actionIcon = R.drawable.ic_media_fullscreen;
		}else if(!link.contains("facebook.com")){
			actionIcon = R.drawable.ic_menu_forward;
    	}
		
	}
	
	

	public String getDesc() {

		if(message != null) return message;
		if(story != null) return story;
		if(name != null) return name;
		
		return null;
	}

	public String getName(){
		return name;
	}

	public String getContentTb() {
		return contentTb;
	}

	public void setContentTb(String contentTb) {
		this.contentTb = contentTb;
	}

	public String getContentName() {
		return contentName;
	}

	public void setContentName(String contentName) {
		this.contentName = contentName;
	}

	public String getContentDesc() {
		return contentDesc;
	}

	public void setContentDesc(String contentDesc) {
		this.contentDesc = contentDesc;
	}

	public String getContentMeta() {
		return contentMeta;
	}

	public void setContentMeta(String contentMeta) {
		this.contentMeta = contentMeta;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	

	
    private int ctbWidth = -1;
    public int getCTbWidth(){
    	
    	
    	if(contentTb != null && ctbWidth == -1){
    				
    		String w = param(contentTb, "w");    		
    		ctbWidth = toInt(w);
    		
    		//AQUtility.debug(ctbWidth, contentTb);
    	}
    	
    	
    	return ctbWidth;
    }
    
    private int toInt(String str){
    	
    	int result = 0;
    	
    	if(str != null){
    		try{
    			result = Integer.parseInt(str);
    		}catch(Exception e){    			
    		}
    	}
    	return result;
    }
    
    private String param(String url, String name){
    	
    	try{
    		Uri uri = Uri.parse(url);
    		return uri.getQueryParameter(name);
    	}catch(Exception e){
    		return null;
    	}
    }

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	public int getLikeCount() {
		return likeCount;
	}
	
	private String ls;
	public String getLikeString() {
		if(ls == null){
			ls = " " + FormatUtility.scientificShort(likeCount);
		}
		return ls;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public int getCommentCount() {
		return commentCount;
	}
	
	private String cs;
	public String getCommentString() {
		if(cs == null){
			cs = " " + FormatUtility.scientificShort(commentCount);
		}
		return cs;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLink() {
		return link;
	}

	public void setActionIcon(int actionIcon) {
		this.actionIcon = actionIcon;
	}

	public int getActionIcon() {
		return actionIcon;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public List<Comment> getComments() {
		if(comments == null){
			comments = new ArrayList<Comment>();
		}
		return comments;
	}

	public void setLikes(List<Like> likes) {
		this.likes = likes;
	}

	public List<Like> getLikes() {
		if(likes == null){
			likes = new ArrayList<Like>();
		}
		return likes;
	}
    
	@Override
	public FeedItem clone(){
		return clone();
	}
	
	private transient LayoutString contentName2;
	public LayoutString getContentName2(){
		
		if(contentName == null) return null;
		
		if(contentName2 == null){
			contentName2 = new LayoutString(contentName);
		}
		
		return contentName2;
	}
	
	
	private transient LayoutString desc2;
	public LayoutString getDesc2(){
		
		String desc = getDesc();
		
		if(desc == null) return null;
		
		if(desc2 == null){
			desc2 = new LayoutString(desc);
		}
		
		return desc2;
	}
	
	private transient LayoutString contentDesc2;
	public LayoutString getContentDesc2(){
		
		if(contentDesc == null) return null;
		
		if(contentDesc2 == null){
			contentDesc2 = new LayoutString(contentDesc);
		}
		
		return contentDesc2;
	}


	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getObjectId() {
		return objectId;
	}
	
	public static List<FeedItem> getItems(JSONObject jo){
		JSONArray data = jo.optJSONArray("data");	
		List<FeedItem> feed = toList(data);
		return feed;
	}
	
	private static List<FeedItem> toList(JSONArray ja){
		
		List<FeedItem> result = new ArrayList<FeedItem>();
		
		for(int i = 0; i < ja.length(); i++){
			result.add(new FeedItem(ja.optJSONObject(i)));
		
		}
		
		return result;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getItemName() {
		return itemName;
	}

	public void setFrom(Entity from) {
		this.from = from;
	}

	public Entity getFrom() {
		if(from == null) return Entity.dummy();
		return from;
	}

	public void setTo(Entity to) {
		this.to = to;
	}

	public Entity getTo() {
		if(to == null) return Entity.dummy();
		return to;
	}
	
	public boolean isFrom(Entity source){
		
		if(from == null || source == null) return false;
		String sid = source.getId();
		if(sid == null) return false;
		return sid.equals(from.getId());
	}

	public void setCommentable(boolean commentable) {
		this.commentable = commentable;
	}

	public boolean isCommentable() {
		return commentable;
	}

	public void setLikeable(boolean likeable) {
		this.likeable = likeable;
	}

	public boolean isLikeable() {
		return likeable;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setUnread(boolean unread) {
		this.unread = unread;
	}

	public boolean isUnread() {
		return unread;
	}
	
	public String getActionId(){
		
		if("photo".equals(type) && objectId != null) return objectId;
		return id;
	}

	public Place getPlace() {
		return place;
	}

	private transient String meta;
	public String getMeta(){
		
		if(meta == null){
			meta = getCachedTime(System.currentTimeMillis());
			if(place != null){
				meta += " @" + place.getName();
			}
		}
		
		return meta;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}
	
	public String getAppId() {
		return appId;
	}
	
	public String getApp(){
		
		if("2347471856".equals(appId)){
			return "note";
		}
		return null;
	}
	
	public String getDetailId(){
		
		if("note".equals(getApp())){
			String[] splits = id.split("\\_");
			return splits[splits.length - 1];
		}
		
		return id;
	}
	
	public boolean isRemovable(){
		return AppUtility.APP_ID.equals(appId);
	}

	public void setLiked(boolean liked) {
		this.liked = liked;
	}

	public boolean isLiked() {
		return liked;
	}
	
	public boolean isContentLink(){
		
		if(link == null) return false;
		if("photo".equals(type)) return true;
		if(link.contains("facebook.com")) return false;
		return true;
		
	}
	
	@Override
	public boolean equals(Object o){
		FeedItem item = (FeedItem) o;
		if(item.id == null) return super.equals(item);
		return item.id.equals(id);
		
	}
	
	
	@Override
	public int hashCode(){
		if(id == null) return 0;
		return id.hashCode();
	}
}
