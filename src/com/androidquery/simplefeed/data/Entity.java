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

import com.androidquery.auth.FacebookHandle;
import com.androidquery.simplefeed.util.AppUtility;
import com.androidquery.simplefeed.util.ImageUtility;
import com.androidquery.simplefeed.util.ParseUtility;

public class Entity extends Data implements Serializable, Cloneable{

	
	private static final long serialVersionUID = 1L;
	private static Entity dummy;
	
	private String name;
	private String type;
	private String username;
	private String tb;
	private String mode = "feed";
	private boolean checked;
	
	public Entity(){
		
	}
	
	public Entity(JSONObject jo){
		
		name = jo.optString("name", null);
		type = jo.optString("type", null);
		username = jo.optString("username", null);
		id = jo.optString("id", null);
		
		if(id != null){
			tb = ParseUtility.profileTb(id);// + "#" + name;
		}
	}
	
	public static List<Entity> getItems(JSONObject jo){
		JSONArray data = jo.optJSONArray("data");	
		List<Entity> feed = toList(data);
		return feed;
	}
	
	private static List<Entity> toList(JSONArray ja){
		
		List<Entity> result = new ArrayList<Entity>();
		
		for(int i = 0; i < ja.length(); i++){
			result.add(new Entity(ja.optJSONObject(i)));
		
		}
		
		return result;
	}
	
	
	public static Entity make(JSONObject jo){
		
		if(jo == null) return null;
		return new Entity(jo);
		
	}
	
	public static Entity dummy(){
		
		if(dummy == null){
			dummy = new Entity();
		}
		
		return dummy;
		
	}
	
	
	public String getName() {
		
		if(isMe()){
			return AppUtility.getUserName();
		}
		
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}


	public void setTb(String tb) {
		this.tb = tb;
	}

	public String getTb() {
		return tb;
	}
	
	@Override
	public boolean equals(Object o){		
		if(o == null || !(o instanceof Entity)) return false;
		String oid = ((Entity) o).id;
		if(id == null && oid == null) return true;
		return id != null && id.equals(oid);
	}
	
	@Override
	public int hashCode(){	
		if(id != null) return id.hashCode();
		return super.hashCode();
	}
	
	public static void sortByName(List<Entity> names){
		
		Collections.sort(names, new NameComparator());
		
	}
	
	private static class NameComparator implements Comparator<Entity>{

		@Override
		public int compare(Entity lhs, Entity rhs) {
			
			String name1 = lhs.getName();
			String name2 = rhs.getName();
			
			if(name1 == null) name1 = "";
			if(name2 == null) name2 = "";
			
			return name1.compareToIgnoreCase(name2);
		}
		
	}
	
	@Override
	public String toString(){
		if(name == null) return "";
		return name;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getMode() {
		return mode;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public boolean isChecked() {
		return checked;
	}
	
	public boolean isMe(){
		return "me".equals(id);
	}
	
	public String getTb(FacebookHandle handle){
		
		if(isMe()){
			return ImageUtility.getProfileTb(handle);
		}
		return tb;
		
	}
	
}
