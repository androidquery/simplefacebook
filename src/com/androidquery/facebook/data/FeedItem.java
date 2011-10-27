package com.androidquery.facebook.data;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.net.Uri;

import com.androidquery.AQuery;
import com.androidquery.facebook.R;
import com.androidquery.facebook.util.FormatUtility;
import com.androidquery.facebook.util.IntentUtility;
import com.androidquery.util.AQUtility;

import static com.androidquery.facebook.util.ParseUtility.*;

public class FeedItem implements Serializable, Cloneable{

	private static final long serialVersionUID = 1L;
	
	private String name;
	private String desc;
	private String tb;
	private String itemId;
	private String userId;
	
	private long time;
	private int likeCount;
	private int commentCount;
	
	private String contentTb;
	private String contentName;
	private String contentDesc;
	private String contentMeta;
	private String link;
	private String type;
	
	private int actionIcon;
	
	private List<Comment> comments;
	private List<Like> likes;
	
	public FeedItem(){
		comments = new ArrayList<Comment>();
		likes = new ArrayList<Like>();
	}
	
	public FeedItem(JSONObject jo){
		
		itemId = jo.optString("id");
		
		JSONObject from = jo.optJSONObject("from");
		
		name = from.optString("name");
		userId = from.optString("id");
		desc = jo.optString("message", null);
		
		
		if(desc == null){
			desc = jo.optString("story");
		}
		
		
		tb = profileTb(userId);
		
		contentTb = jo.optString("picture", null);
		
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
			comments = parseComments(jocomments.optJSONArray("data"));
		}
		
		link = jo.optString("link", null);
		type = jo.optString("type", null);
	
		parseIcon(link, type);
	
	}
	
	private List<Like> parseLikes(JSONArray data){
		
		List<Like> result = new ArrayList<Like>();
		if(data == null) return result;
		
		for(int i = 0; i < data.length(); i++){
			result.add(new Like(data.optJSONObject(i)));
		}
		
		return result;
	}
	
	private List<Comment> parseComments(JSONArray data){
		
		List<Comment> result = new ArrayList<Comment>();
		if(data == null) return result;
		
		for(int i = 0; i < data.length(); i++){
			result.add(new Comment(data.optJSONObject(i)));
		}
		
		return result;
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
	
	
	
	

	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getTb() {
		return tb;
	}

	public void setTb(String tb) {
		this.tb = tb;
	}

	

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
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

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getItemId() {
		return itemId;
	}
	
    private String cachedTime;
    private long lastTime;
    
    public String getCachedTime(long now){
    	
    	long diff = now - lastTime;
    	
    	if(cachedTime == null || diff > 300000){    	
    		cachedTime = FormatUtility.relativeSmartTime(now, time) + "";
    		lastTime = now;
    	}
    	
    	return cachedTime;
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
		return comments;
	}

	public void setLikes(List<Like> likes) {
		this.likes = likes;
	}

	public List<Like> getLikes() {
		return likes;
	}
    
	@Override
	public FeedItem clone(){
		return clone();
	}
}
