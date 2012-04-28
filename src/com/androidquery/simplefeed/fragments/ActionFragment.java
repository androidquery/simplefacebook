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


import java.util.List;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.androidquery.simplefeed.PQuery;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.base.BaseFragment;
import com.androidquery.simplefeed.data.FeedItem;
import com.androidquery.simplefeed.data.PageAdapter;
import com.androidquery.simplefeed.util.DialogUtility;
import com.androidquery.simplefeed.util.DialogUtility.ActionItem;
import com.androidquery.util.AQUtility;

public class ActionFragment extends BaseFragment{

	private FeedItem item;
	private PageAdapter<ActionItem> actions;
	
    @Override
    protected void init(){
    	
    	initView();
    	
    	Intent intent = act.getIntent();
    	FeedItem item = (FeedItem) intent.getSerializableExtra("item");
    	
    	AQUtility.debug("action init", item);
    	if(item != null){
    		setItem(item);  		
    	}
    }
    
    private void initView(){
    	
    	//actions = new ActionAdapter();
    	
    	//aq.id(R.id.grid);
    	
    	//aq.adapter(actions).itemClicked(this, "itemClicked");
    	
    	
    }
    
   
    
	@Override
	protected int getContainerView() {
		return R.layout.fragment_action;
	}

	
	public void setItem(FeedItem newItem){
		
		if(item != null && item.getId().equals(newItem.getId())){
			return;
		}
		
		this.item = newItem;
		
		actions.clear();
		
		List<ActionItem> items = DialogUtility.makeActions(act, null, newItem, false);
		
		AQUtility.debug("actions", items);
		
		actions.add(items, null);
		
	}

	
	public void refresh(){
		
		
	}
	
	 
    private class ActionAdapter extends PageAdapter<ActionItem>{
		
		public View render(int position, View convertView, ViewGroup parent) {
			
			
			if(convertView == null){					
				convertView = act.getLayoutInflater().inflate(R.layout.item_action, null);					
				//initItemView(convertView);
			}
			
			PQuery aq = aq2.recycle(convertView);
			
			ActionItem item = (ActionItem) getItem(position);
			
			String name = item.getName();
			String tb = item.getTb();
			
			if(name == null && item.getText() > 0){
				//aq.id(R.id.tb).image(item.getIcon()).background(0).getImageView().setScaleType(ScaleType.FIT_CENTER);
				
				String text = convertView.getContext().getString(item.getText());				
				aq.id(R.id.button).text(text);
				
				Button button = aq.getButton();
				button.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(item.getIcon()), null, null, null);
				
			}else if(name != null){
				//aq.id(R.id.tb).background(R.color.ph).image(tb, true, true, 0, 0, null, AQuery.FADE_IN_NETWORK, 1.0f);
				String text = item.getName();// + " (" + act.getString(R.string.wall) + ")";
				aq.id(R.id.button).text(text);
			}
			
			return convertView;
			
		}
		
	};
	
	
	
}
