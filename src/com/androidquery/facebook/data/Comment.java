package com.androidquery.facebook.data;

import org.json.JSONObject;
import static com.androidquery.facebook.util.ParseUtility.*;

public class Comment {

	private String name;
	private String itemId;
	private String userId;
	private String message;
	private String tb;
	private long time;
	
	public Comment(){		
	}
	
	public Comment(JSONObject jo){
		
		itemId = jo.optString("id");
		
		JSONObject from = jo.optJSONObject("from");
		
		name = from.optString("name");
		userId = from.optString("id");
		
		message = jo.optString("message", null);
		
		tb = profileTb(userId);
		
		time = parseTime(jo.optString("created_time", null));
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getItemId() {
		return itemId;
	}

	public void setTb(String tb) {
		this.tb = tb;
	}

	public String getTb() {
		return tb;
	}
	
}
