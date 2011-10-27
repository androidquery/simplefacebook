package com.androidquery.facebook.data;

import org.json.JSONObject;
import static com.androidquery.facebook.util.ParseUtility.*;

public class Like {

	private String name;
	private String userId;
	private String tb;
	
	public Like(JSONObject jo){
		
		userId = jo.optString("id");
		name = jo.optString("name");
		
		tb = profileTb(userId);
		
		
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
	

	public void setTb(String tb) {
		this.tb = tb;
	}

	public String getTb() {
		return tb;
	}
	
}
