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
package com.androidquery.simplefeed.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.simplefeed.PQuery;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.data.Entity;
import com.androidquery.simplefeed.data.FeedItem;
import com.androidquery.util.AQUtility;

public class DialogUtility {

	
	public static List<ActionItem> makeActions(Activity act, Entity source, FeedItem item, boolean delete){
		
		List<ActionItem> result = new ArrayList<ActionItem>();
		

		if(item.isLikeable()){
			result.add(new ActionItem(R.string.like, R.drawable.ics_like, R.string.like));
		}
		
		if(item.isCommentable()){
			result.add(new ActionItem(R.string.comment, R.drawable.ics_comment_write, R.string.comment));
			result.add(new ActionItem(R.string.see_comments, R.drawable.ic_menu_start_conversation, R.string.see_comments, item.getCommentCount()));
		}
		
		Entity from = item.getFrom();
		Entity to = item.getTo();
		
		if(from.getId() != null && !from.equals(source)){
			result.add(new ActionItem(R.id.name, from.getTb(), "@ " + from.getName()));
		}
		
		if(to.getId() != null && !to.equals(from) && !to.equals(source)){
			result.add(new ActionItem(R.id.name2, to.getTb(), "@ " + to.getName()));
		}
		
		/*
		Place place = item.getPlace();
		
		if(place != null){
			result.add(new Item(R.string.n_checkins, place.getTb(), "@ " + place.getName()));
		}
		*/
		
		String type = item.getType();
		String ctb = item.getContentTb();
		String link = item.getLink();
		
		AQUtility.debug("type", type);
		
		if(link != null){
		
			if(ctb == null) ctb = from.getTb();
			if("photo".equals(type)){
				result.add(new ActionItem(R.id.content_tb, ctb, act.getString(R.string.view_photo)));
			}else if("link".equals(type) && !link.contains("facebook.com")){				
				result.add(new ActionItem(R.id.content_name, ctb, act.getString(R.string.view_web)));
			}else if("video".equals(type)){
				result.add(new ActionItem(R.id.content_tb, ctb, act.getString(R.string.play_video)));
			}
			
		}
		
		if(delete && item.isRemovable()){
			result.add(new ActionItem(R.string.delete, R.drawable.ic_menu_delete, R.string.delete));
		}
		
		return result;
		
	}
	
    public static Dialog makeDialog(final Activity act, final List<ActionItem> items, final OnClickListener listener){
    	
    	final PQuery aq = new PQuery(act);
    	
    	ArrayAdapter<ActionItem> adapter = new ArrayAdapter<DialogUtility.ActionItem>(act, R.layout.item_dialog, items){
    		@Override
    		public View getView(int position, View convertView, ViewGroup parent) {
    			
    			convertView = aq.inflate(convertView, R.layout.item_dialog, parent);    			
    			aq.recycle(convertView);
    	
    			ActionItem item = getItem(position);
    			
    			String name = item.name;
    			String tb = item.tb;
    			
    			if(name == null && item.text > 0){
    				aq.id(R.id.tb).image(item.icon).background(0).getImageView().setScaleType(ScaleType.FIT_CENTER);
    				String text = convertView.getContext().getString(item.text);
    				if(item.count > 0){
    					text += " (" + item.count + ")";
    				}
    				aq.id(R.id.name).text(text);
    			}else if(name != null){
    				aq.id(R.id.tb).background(R.color.ph).image(tb, true, true, 0, 0, null, AQuery.FADE_IN_NETWORK, 1.0f);
    				String text = item.name;// + " (" + act.getString(R.string.wall) + ")";
    				aq.id(R.id.name).text(text);
    			}
    			
    			
    			/*
    			String tb = item.tb;
    			
    			if(tb == null){
    				aq.id(R.id.tb).image(item.icon).background(0).getImageView().setScaleType(ScaleType.FIT_CENTER);
    				String text = convertView.getContext().getString(item.text);
    				if(item.count >= 0){
    					text += " (" + item.count + ")";
    				}
    				aq.id(R.id.name).text(text);
    			}else{
    				aq.id(R.id.tb).background(R.color.ph).image(tb, true, true, 0, 0, null, AQuery.FADE_IN_NETWORK, 1.0f);
    				String text = item.name;// + " (" + act.getString(R.string.wall) + ")";
    				aq.id(R.id.name).text(text);
    			}
    			*/
    			return convertView;
    		}
    	};
    	
    	OnClickListener ocl = new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				ActionItem item = items.get(which);				
				listener.onClick(dialog, item.id);
				
			}
			
		};
    	
    	
    	AlertDialog dia = new AlertDialog.Builder(act)    	
        .setAdapter(adapter, ocl)
        .create();
    	
    	dia.setCanceledOnTouchOutside(true);
    	dia.setInverseBackgroundForced(false);
    	
    	
    	return dia;
    }    
	
    
    public static class ActionItem{
    	
    	private int icon;
		private int text;
    	private String tb;
    	private String name;
    	private int id;
    	private int count = -1;
    	
    	public ActionItem(int id, int icon, int text){
    		this.id = id;
    		this.icon = icon;
    		this.text = text;
    	}
    	
    	public ActionItem(int id, int icon, int text, int count){
    		this.id = id;
    		this.icon = icon;
    		this.text = text;
    		this.count = count;
    	}
    	
    	public ActionItem(int id, String tb, String name){
    		this.id = id;
    		this.tb = tb;
    		this.name = name;
    	}
    	
    	public int getIcon() {
			return icon;
		}

		public void setIcon(int icon) {
			this.icon = icon;
		}

		public int getText() {
			return text;
		}

		public void setText(int text) {
			this.text = text;
		}

		public String getTb() {
			return tb;
		}

		public void setTb(String tb) {
			this.tb = tb;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}
    }
    
    
	public static void askYesNo(Activity act, String title, String message, final DialogInterface.OnClickListener listener){
		
		View view = act.getLayoutInflater().inflate(R.layout.dialog_comment, null);
		
		TextView tv = (TextView) view.findViewById(R.id.input);
		tv.setText(message);
		
		
		AlertDialog dialog = new AlertDialog.Builder(act)
		.setTitle(title)
		.setView(view)
		.setPositiveButton(R.string.send, new Dialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				listener.onClick(dialog, which);
				
				dialog.dismiss();
			}
		})
		.setNeutralButton(R.string.cancel, new Dialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				dialog.dismiss();
				
			}
		})
		.create();
		
		dialog.show();
		
	}
    
}
