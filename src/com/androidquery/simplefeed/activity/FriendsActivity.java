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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.simplefeed.PQuery;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.base.Constants;
import com.androidquery.simplefeed.base.MenuActivity;
import com.androidquery.simplefeed.data.Entity;
import com.androidquery.simplefeed.data.PageAdapter;
import com.androidquery.simplefeed.enums.FeedMode;
import com.androidquery.simplefeed.util.JsonUtility;
import com.androidquery.util.AQUtility;



public class FriendsActivity extends MenuActivity {

	private FriendsAdapter friends;
	private List<Entity> items;
	private boolean selectable;
	
	@Override
	protected void init(Bundle savedInstanceState) {
		
		Intent intent = getIntent();
		if(intent != null){
			selectable = intent.getBooleanExtra("selectable", false);
		}
		
		initView();
	}
	
	public static void start(Activity act, boolean selectable){
		
		Intent intent = new Intent(act, FriendsActivity.class);		
		intent.putExtra("selectable", selectable);
		act.startActivityForResult(intent, Constants.ACTIVITY_FRIENDS);
		
	}
	
	private void initView(){
		
		 
		friends = new FriendsAdapter();
    	
    	aq.id(R.id.list);
    	
    	aq.adapter(friends).scrolledBottom(this, "scrolledBottom").itemClicked(this, "itemClicked");
    	
    	ListView lv = aq.getListView();
    	lv.setItemsCanFocus(false);
    	lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    	
    	ajaxFriends(0);
    	
    	aq.id(R.id.edit_input).textChanged(this, "searchChanged");
    	
    	if(selectable){
    		aq.id(R.id.done_box).visible();
    		aq.id(R.id.button_done).clicked(this, "doneClicked");
    	}
	}
	
    public void searchChanged(CharSequence s, int start, int before, int count) { 
	    
    	if(items == null) return;
    	
    	friends.clear();
    	friends.add(filter(s, items), null);
    	
	} 
    
	@Override
	public FeedMode getMode(){
		return FeedMode.FRIENDS;
	}
    
    private List<Entity> filter(CharSequence s, List<Entity> items){
    	
    	String str = null;
    	
    	if(s != null){
    		str = s.toString();
    	}else{
    		str = aq.id(R.id.edit_input).getEditable().toString();
    	}
    	
    	String[] terms = str.split("[\\s]+");
    	
    	
    	List<Entity> result = new ArrayList<Entity>(); 
    	
    	for(Entity item: items){
    		
    		String name = item.getName().toLowerCase();
    		
    		boolean miss = false;
    		
    		for(String term: terms){
    		
	    		if(!name.contains(term)){	    		
	    			miss = true;
	    			break;
	    		}
	    		
    		}

    		if(!miss){
    			result.add(item);
    		}
    	}
    	
    	return result;
    }
	
	public void itemClicked(AdapterView<?> parent, View view, int pos, long id){
	 
		Entity entity = getItem(view);
		if(entity == null) return;
		
		if(!selectable){			
			FeedActivity.start(this, entity);
		}else{
			//CheckBox box = (CheckBox) view;
			PQuery aq = aq2.recycle(view);
			aq.id(R.id.checkbox_select);
			
			boolean checked = aq.isChecked();
			aq.checked(!checked);
			
			entity.setChecked(!checked);
		
		}
		
		
		
	}
	
	private void ajaxFriends(long expire){
		
		showProgress(true);
		
		String url = FeedMode.FRIENDS.getUrl() + "?limit=3000&locale=" + locale;
		aq.auth(handle).ajax(url, JSONObject.class, expire, this, "friendCb");
		
	}
	
	public void friendCb(String url, JSONObject jo, AjaxStatus status){
		
		AQUtility.debug("jo", jo);
		
		showProgress(false);
		
		if(jo != null){
		
			friends.clear();
			
			items = Entity.getItems(jo);
			
			Entity.sortByName(items);
			
			String next = JsonUtility.getString(jo, "comments", "paging");		
			friends.add(filter(null, items), next);
			
			
			if(status.expired(MONTH)){
				AQUtility.debug("expired", status.getTime());
				refresh();
			}
			
			
			updateTitle(status.getTime().getTime());
			
			aq.id(R.id.list).visible();
			
			AQUtility.debug("done");
			
		}else{
			AQUtility.debug("error!");
		}
		
		
	}
	

	@Override
	protected int getContainerView() {
		return R.layout.activity_friends;
	}

	public final static int REQUEST = 18;
	public static void start(Activity act){
		
		Intent intent = new Intent(act, FriendsActivity.class);		
		act.startActivityForResult(intent, REQUEST);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		
	    switch(requestCode){
	    	case PostActivity.REQUEST:
	    		if(data != null){
	    	
	    		}
	    		break;
	    	default:
	    		super.onActivityResult(requestCode, resultCode, data);
	    		break;
	    }
	}

    @Override
    protected String makeTitle(long time){
    	String result = getString(R.string.n_friends);
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
	public void refresh(){
		
		ajaxFriends(-1);
	}
	
	/*
	private void initItemView(View view){
		
		PQuery aq = aq2.recycle(view);
		aq.id(R.id.checkbox_select).clicked(this);
		
	}
	*/
	
	private ArrayList<Entity> getSelected(){
		
		ArrayList<Entity> result = new ArrayList<Entity>();
		
		if(items != null){
		
			for(Entity entity: items){
				
				if(entity.isChecked()){
					result.add(entity);
				}
				
			}
		}
		
		return result;
		
	}
	
	
	public void doneClicked(View view){
		
		
		Intent result = new Intent();
		ArrayList<Entity> selects = getSelected();
		
		result.putExtra("selected", selects);
		
		AQUtility.debug("on stop", selects);
		
		setResult(RESULT_OK, result);
		
		finish();
	}
	
	

	
    private Entity getItem(View view){
    	PQuery aq = aq2.recycle(view).parent(R.id.parent);
    	return (Entity) aq.getTag();
    }
	
    private class FriendsAdapter extends PageAdapter<Entity>{
		
		public View render(int position, View convertView, ViewGroup parent) {
			
			boolean init = convertView == null;
							
			convertView = aq.inflate(convertView, R.layout.item_friend, parent);					
				
			/*
			if(init){
				initItemView(convertView);
			}
			*/
			PQuery aq = aq2.recycle(convertView);
			
			Entity item = (Entity) getItem(position);
			
			aq.tag(item);
			
			aq.id(R.id.text_name).text(item.getName());
			
			String tb = item.getTb();
			
			if(aq.shouldDelay(convertView, parent, tb, 0)){
				aq.id(R.id.image_tb).clear();
			}else{
				aq.id(R.id.image_tb).image(tb, true, true, 0, 0, null, AQuery.FADE_IN_FILE);
			}
			
			if(selectable){
				aq.id(R.id.checkbox_select).checked(item.isChecked()).visible();
			}
			return convertView;
			
		}
		
	}

	
	
}
