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


import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView.BufferType;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.simplefeed.PQuery;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.activity.CommentActivity;
import com.androidquery.simplefeed.activity.FeedActivity;
import com.androidquery.simplefeed.activity.NotificationActivity;
import com.androidquery.simplefeed.activity.PostActivity;
import com.androidquery.simplefeed.base.BaseFragment;
import com.androidquery.simplefeed.base.Constants;
import com.androidquery.simplefeed.data.Comment;
import com.androidquery.simplefeed.data.Entity;
import com.androidquery.simplefeed.data.Feed;
import com.androidquery.simplefeed.data.FeedItem;
import com.androidquery.simplefeed.data.PageAdapter;
import com.androidquery.simplefeed.enums.FeedMode;
import com.androidquery.simplefeed.util.AppUtility;
import com.androidquery.simplefeed.util.DialogUtility;
import com.androidquery.simplefeed.util.DialogUtility.ActionItem;
import com.androidquery.simplefeed.util.IconUtility;
import com.androidquery.simplefeed.util.ImageUtility;
import com.androidquery.simplefeed.util.IntentUtility;
import com.androidquery.simplefeed.util.PrefUtility;
import com.androidquery.util.AQUtility;

public class FeedFragment extends BaseFragment implements OnClickListener{

	private PageAdapter<FeedItem> items;
	private boolean logout;
	private FeedItem actionItem;
	private Entity source;
	private View header;
	private int selectedColor;
	
	
    @Override
    protected void init(){
    	
    	Intent intent = act.getIntent();
    	if(intent != null){
    		logout = intent.getBooleanExtra("logout", false);
    		source = (Entity) intent.getSerializableExtra("source");
    	}
    	    
    	if(source == null){
    		source = AppUtility.getDefaultSource();
    	}
    	
        initView();
        
        if(logout){
        	showLogin(true);
        }else{
        	initAjax(0);
        }
        
    }
    
   
    

    
    public void modeChange(FeedMode mode){
    	
    	if("me".equals(source.getId())){
    		source = AppUtility.getDefaultSource();
    	}
    	
    	initAjax(0);
    	
    }
    
    public void initAjax(long expire){
    	
    	ajaxHome(expire);
    	
    	if(act.isRoot()){
    		ajaxNoti(expire);
    	}
    }
    
    public void ajaxNoti(long expire){
    	
		String url = "https://graph.facebook.com/me/notifications?include_read=1&locale=" + locale;		
		aq.auth(handle).ajax(url, JSONObject.class, expire, this, "notiCb");
    	
    }
    
    public void notiCb(String url, JSONObject jo, AjaxStatus status){
    	
    	AQUtility.debug("noti", jo);
    	
    	if(jo != null){
    	
    		int count = 0;
    		
    		PQuery aq = aq2.recycle(header);
    		
    		/*
    		JSONObject sum = jo.optJSONObject("summary");
    		
    		if(sum != null){
    			count = sum.optInt("unseen_count", 0); 		    			
    		}*/
    		
    		JSONArray ja = jo.optJSONArray("data");
    		for(int i = 0; i < ja.length(); i++){
    			JSONObject noti = ja.optJSONObject(i);
    			if(noti.optInt("unread", 0) != 0){
    				count++;
    			}
    		}
    		
    		  		
			String message = count + " " + getString(R.string.n_notifications);    		
			aq.id(R.id.text_noti).text(message);
			int colorId = R.color.noti;			
			int tf = Typeface.BOLD;
			if(count == 0){
				colorId = R.color.grey;
				tf = Typeface.NORMAL;
			}
			
			aq.textColor(getResources().getColor(colorId)).getTextView().setTypeface(null, tf);
			
    		
    	}
    	
    }
    
    
    public void itemClicked(AdapterView<?> parent, View view, int pos, long id){
    	
    	FeedItem item = getItem(view);
    	if(item == null) return;
    	
    	actionItem = item;
    	
    	showContentDetail(item, false);


    }
    
    private void showContentDetail(FeedItem item, boolean init){
    	
    	
    	if(act.isTablet()){
    		
    		boolean content = item.isContentLink();
    		boolean comment = item.isCommentable();
    		
    		AQUtility.debug("clicked", item.getLink());
    		AQUtility.debug(content, comment);
    		
    		
			DetailFragment df = (DetailFragment) getFragment(R.id.frag_detail);
    		df.setItem(item);
		
			CommentFragment cf = (CommentFragment) getFragment(R.id.frag_comment);
    		cf.setItem(item);
		
    		if(content || comment){
    			act.aq.id(R.id.empty_box).gone();
    		}else{
    			act.aq.id(R.id.empty_box).visible();
    		}
    		
    		items.notifyDataSetChanged();
    		
    		PrefUtility.put(Constants.PREF_ACTION_ITEM, item.getId());
    		
    	}else{
    		if(!init){
    			
    			boolean deleteOk = FeedMode.WALL.getUrl().equals(getUrl());
    			
    			List<ActionItem> actions = DialogUtility.makeActions(act, source, item, deleteOk);
    			
    			if(actions.size() > 0){
    			
	    			Dialog dialog = DialogUtility.makeDialog(act, actions, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							action(which);
						}
						
					});
	    			aq.show(dialog);
	    			
	    			
    			}
    		}
    	}
    	
    	
    	
    }
    
    private Fragment getFragment(int id){
    	return act.getFragment(id);
    }
    
    
    public void contentClicked(FeedItem item){
    	
    	if(act.isTablet()){
    		actionItem = item;        	
        	showContentDetail(item, false);
    	}else{    	
    		IntentUtility.launchActivity(act, item);
    	}
    }
    
    private void initItemView(View view){

    	PQuery aq = aq2.recycle(view);
    	
		aq.id(R.id.content_tb).clicked(this);
		aq.id(R.id.content_name).clicked(this);
		aq.id(R.id.button_comment).clicked(this);
		aq.id(R.id.tb).clicked(this);
		aq.id(R.id.name).clicked(this);
		aq.id(R.id.name2).clicked(this);
		
		if(act.isTablet()){
			aq.id(R.id.button_like).clicked(this);
			aq.id(R.id.button_source1).clicked(this);
			aq.id(R.id.button_source2).clicked(this);
		}
		
    }
    
    private FeedItem getItem(View view){
    	PQuery aq = aq2.recycle(view).parent(R.id.parent);
    	return (FeedItem) aq.getTag();
    }
    
    @Override
    public void onClick(View view){
    	
    	
    	
    	try{
    	
	    	int id = view.getId();
	    	
	    	FeedItem item = getItem(view);
	    	AQUtility.debug("click", item);
	    	if(item == null) return;
	    	
	    	actionItem = item;
	    	
	    	switch(id){
	    		case R.id.button_like:
	    			like(item);
	    			view.setEnabled(false);
	    			break;
	    		case R.id.content_tb:
	    		case R.id.content_name:
	    			contentClicked(item);
	    			break;
	    		case R.id.button_comment:
	    			comments();
	    			break;
	    		case R.id.tb:
	    		case R.id.name:	
	    		case R.id.button_source1:
	    			AQUtility.debug("source1");
	    			userWall(false);
	    			break;	
	    		case R.id.name2:	
	    		case R.id.button_source2:
	    			userWall(true);
	    			break;	
	    	}
    	}catch(Exception e){
    		AQUtility.report(e);
    	}
    	
    }
    
    private void userWall(boolean toUser){
    	
    	if(actionItem == null) return;
    	
    	
		Entity source = null;
		
		if(toUser){
			source = actionItem.getTo();
		}else{
			source = actionItem.getFrom();
		}
		
		if(source.getId() != null && !source.equals(this.source)){
    		FeedActivity.start(act, source);
    	}
    		
    	
    	
    }
    

    
    
    private void comments(){
    	
    	if(actionItem == null) return;
    	
    	CommentFragment cf = (CommentFragment) getFragment(R.id.frag_comment);
    	if(cf != null){
        	showContentDetail(actionItem, false);
    	}else{    	
    		CommentActivity.start(act, actionItem);
    	}
    	
    }
    
    
    private void initView(){
    	
    	items = new FeedAdapter();    	
    	items.setLoadable(true);
    	
    	aq.id(R.id.list);
    	
    	if(act.isRoot()){
    		ListView lv = aq.getListView();
    		header = aq.inflate(null, R.layout.header_notification, null);
    		lv.addHeaderView(header);   	 		
    	}
    	
    	aq.adapter(items).scrolledBottom(this, "scrolledBottom").itemClicked(this, "itemClicked");
    	
    	if(header != null){
    		aq.id(header).clicked(this, "notiClicked");
    	}
    	
    	selectedColor = getResources().getColor(R.color.selected);
    	
    }
    
    
    public void notiClicked(View view){
    	
    	AQUtility.debug("clicked");
    	NotificationActivity.start(act);
    }
    
    
    public void login(View view){
    	
    	
    	initAjax(0);
    	
    	
    }
    
    public void scrolledBottom(AbsListView view, int scrollState){
    	
    	AQUtility.debug("bottom");
    	
    	ajaxMore();
    }
    
    
    public static void startIntent(Activity act, String url){
    	
    	Intent intent = new Intent(act, FeedActivity.class); 
    	intent.putExtra("url", url);
    	
    	act.startActivity(intent);
    	
    }
    
    
    private String getUrl(){
    	
    	String id = source.getId();
    	if(id == null) id = "me";
    	
    	return "https://graph.facebook.com/" + id + "/" + source.getMode();
    	
    }
    
    private String lastHome;
	private void ajaxHome(long expire){
		
		String url = getUrl() + "?locale=" + locale;
	
		showProgress(true);
		lastHome = url;
		
		//aq.auth(handle).ajax(url, JSONObject.class, expire, this, "homeCb");
		aq.auth(handle).transformer(transformer).ajax(url, Feed.class, expire, this, "homeCb");
	}	
	

	
	public void homeCb(String url, Feed feed, AjaxStatus status){
		
		if(!isActive() || !url.equals(lastHome)) return;
		
		showProgress(false);
		
		if(feed != null){
			
			showLogin(false);
			
			String next = feed.getNext();
			items.prepend(feed, next);			
			items.setLoadable(next != null);
			
			if(status.expired(TEN_MIN)){				
				refresh();				
			}
			
			act.updateTitle(status.getTime().getTime());			
			aq.id(R.id.list).visible();
			
			
			if(act.isTablet() && items.getCount() > 0 && actionItem == null){	
				int index = findSelected(feed);
				actionItem = feed.get(index);
				ListView lv = aq.getListView();
				lv.smoothScrollToPositionFromTop(index, 0);
				showContentDetail(actionItem, true);
			}
			
		}else{
			
			int code = status.getCode();
			
			if(code != AjaxStatus.AUTH_ERROR){
				askYesNo(act, getString(R.string.connection_failed), getString(R.string.retry) + "?", getString(R.string.retry), getString(R.string.cancel));
			}else{
				showLogin(true);
			}
		}
		
		
	}	
	
	/*
	public void homeCb(String url, JSONObject jo, AjaxStatus status){
		
		if(!isActive() || !url.equals(lastHome)) return;
		
		showProgress(false);
		
		if(jo != null){
			
			showLogin(false);
			
			JSONArray data = jo.optJSONArray("data");
			
			String next = JsonUtility.getString(jo, "paging", "next");
			
			List<FeedItem> feed = toList(data);
			
			items.prepend(feed, next);
			
			items.setLoadable(next != null);
			
			if(status.expired(TEN_MIN)){				
				refresh();				
			}
			
			act.updateTitle(status.getTime().getTime());
			
			aq.id(R.id.list).visible();
			
			
			if(act.isTablet() && items.getCount() > 0 && actionItem == null){	
				int index = findSelected(feed);
				actionItem = feed.get(index);
				ListView lv = aq.getListView();
				lv.smoothScrollToPositionFromTop(index, 0);
				showContentDetail(actionItem, true);
			}
			
		}else{
			
			int code = status.getCode();
			
			if(code != AjaxStatus.AUTH_ERROR){
				askYesNo(act, getString(R.string.connection_failed), getString(R.string.retry) + "?", getString(R.string.retry), getString(R.string.cancel));
			}else{
				showLogin(true);
			}
		}
		
		
	}
	*/
	private void ajaxMore(){
		
		
		String url = items.getNext();
		
		AQUtility.debug("next", url);
		
		if(url == null || items.isLoading() || !items.isLoadable()) return;
		
		showProgress(true);
		items.setLoading(true);
		
		
		//aq.auth(handle).ajax(url, JSONObject.class, TEN_MIN, this, "moreCb");
		
		aq.auth(handle).transformer(transformer).ajax(url, Feed.class, TEN_MIN, this, "moreCb");
		
	}
	
	/*
	private List<FeedItem> toList(JSONArray ja){
		
		List<FeedItem> result = new ArrayList<FeedItem>();
		
		for(int i = 0; i < ja.length(); i++){
			result.add(new FeedItem(ja.optJSONObject(i)));
		
		}
		
		return result;
	}*/
	
	private int findSelected(List<FeedItem> items){
		
		String lastId = PrefUtility.get(Constants.PREF_ACTION_ITEM, null);
		
		if(lastId != null){
			for(int i = 0; i < items.size(); i++){
				if(lastId.equals(items.get(i).getId())){
					return i;
				}
			}
		}
		
		
		return 0;
	}
	
	
	private void showLogin(boolean show){
		
		AQuery aq = new AQuery(act);
		
		if(show){
			aq.id(R.id.login_panel).visible();
			aq.id(R.id.content_init).gone();
			aq.id(R.id.progress_init).gone();
		}else{
			aq.id(R.id.login_panel).gone();
			aq.id(R.id.content_init).visible();
			aq.id(R.id.progress_init).gone();
		}
		
	}
	
	@Override
	public void refresh(){
		
		if(act.isBusy()) return;
		
		initAjax(-1);
		
		
	}
	
	public void moreCb(String url, Feed feed, AjaxStatus status){

		showProgress(false);
		items.setLoading(false);
		
		if(feed != null){
					
			String next = feed.getNext();	
			items.add(feed, next);
			
			if(next == null){
				items.setLoadable(false);
			}
		}
		
	}
	
	/*
	public void moreCb(String url, JSONObject jo, AjaxStatus status){

		
		if(!isActive()) return;
		
		showProgress(false);
		items.setLoading(false);
		
		if(jo != null){
			
			JSONArray data = jo.optJSONArray("data");			
			String next = JsonUtility.getString(jo, "paging", "next");		
			items.add(toList(data), next);
			
			if(next == null){
				items.setLoadable(false);
			}
		}
		
	}
*/
	private void comment(){
		
		PostActivity.start(act, actionItem);
		
	}
	
	
	private void refreshView(){
		if(items != null){
			items.notifyDataSetChanged();
		}
	}
	
	
	public void updated(String str){
		
		if(str != null){
			
			if(FeedMode.WALL.equals(AppUtility.getDefaultMode())){
				
				if(items.getCount() > 0){
					FeedItem old = (FeedItem) items.getItem(0);
					FeedItem item = new FeedItem();
					item.setMessage(str);
					item.setTime(System.currentTimeMillis());
					item.setFrom(old.getFrom());
					items.add(0, item);
				}else{
					refresh();
				}
				
				
			}else{
				refresh();
			}
		}
		
		
	}
	
	
	
	
	
	
	public void commented(String itemId, String str){
		
		AQUtility.debug("commented", itemId + ":" + str);
		
		if(str != null && itemId != null && actionItem != null && itemId.equals(actionItem.getId())){
			
			AQUtility.debug("commentCb", str);
			
			Comment comment = new Comment();
			comment.setMessage(str);
			comment.setName(AppUtility.getUserName());
			String pic = "https://graph.facebook.com/me/picture";
			pic = handle.getNetworkUrl(pic);
			comment.setTb(ImageUtility.getProfileTb(handle));
			actionItem.getComments().add(0, comment);
			actionItem.setCommentCount(actionItem.getCommentCount() + 1);
			
			refreshView();
			
		}
		
	}
	
	

	
	

	
	
    private void action(int id){
    	
    	switch(id){
			case R.string.like:          			
				like(actionItem);
				break;
			case R.string.comment:
				comment();      			
				break;	
			case R.string.see_comments:
				comments(); 
				break;
			case R.id.name:
				userWall(false); 
				break;	
			case R.id.name2:
				userWall(true); 
				break;	
			case R.id.content_tb:
			case R.id.content_name:
				if(actionItem != null){
					IntentUtility.launchActivity(act, actionItem);
				}
				break;	
			case R.string.delete:
				delete();
				break;
			default:
		}
    	
    }    
    
    private void delete(){
    	
    	if(actionItem == null) return;
    	
    	String objectId = actionItem.getId();
    	
    	showProgress(true);
    	
    	String url = "https://graph.facebook.com/" + objectId + "?method=delete";
    	aq.auth(handle).ajax(url, new HashMap<String, Object>(), String.class, this, "deleteCb");
    
    	
    }
    
    public void deleteCb(String url, String str, AjaxStatus status){
    	
    	showProgress(false);
    	
    	AQUtility.debug("delete", str);
    	
    	if("true".equals(str)){
    		
    		aq.invalidate(getUrl());    		
    		items.remove(actionItem);
    		act.showToast(getString(R.string.done));
    	
    	}else if(status.getCode() == 403){
    		act.showToast(getString(R.string.no_permission));
    	}
    	
    }
    
    
    protected void askYesNo(Activity context, String title, String question, String yes, String no){
    	
        Dialog dialog =  new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(question)
            .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	refresh();
                }
            })
            .setNegativeButton(no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                
                }
            })
            .create();
    	
        
        dialog.show();
    	
    }
    
    
    
    private class FeedAdapter extends PageAdapter<FeedItem>{
		
		public View render(int position, View convertView, ViewGroup parent) {
			
			
			if(isLoading(position)){
				return getLoadingView(act.getLayoutInflater(), R.layout.item_progress);
			}
			
			FeedItem item = (FeedItem) getItem(position);
			if(item == null) return getEmptyView(parent);
			
			
			boolean init = convertView == null;
			
			convertView = aq.inflate(convertView, R.layout.item_feed, parent);
			
			if(init){									
				initItemView(convertView);
			}
			
			Entity from = item.getFrom();
			Entity to = item.getTo();
			
			PQuery aq = aq2.recycle(convertView);
			aq.tag(item);
			
			
			if(act.isTablet() && item.equals(actionItem)){
				aq.backgroundColor(selectedColor);
				
				
				String tb1 = item.getFrom().getTb();
				if(tb1 != null){
					aq.id(R.id.button_source1).image(tb1);
				}else{
					aq.id(R.id.button_source1).gone();
				}
				
				String tb2 = item.getTo().getTb();
				if(tb2 != null && !from.equals(to)){
					aq.id(R.id.button_source2).image(tb2);
				}else{
					aq.id(R.id.button_source2).gone();
				}
				
				if(item.isLikeable()){
					aq.id(R.id.button_like).enabled(!item.isLiked()).visible();
				}else{
					aq.id(R.id.button_like).gone();
				}
				
				
				aq.id(R.id.action_box).visible();
				
			}else{
				aq.background(0);
				aq.id(R.id.action_box).gone();
			}
			
			
			
			
			aq.id(R.id.name).text(from.getName());
			
			if(to.getName() != null && !from.equals(to)){
				aq.id(R.id.to_symbol).visible();
				aq.id(R.id.name2).text(to.getName()).visible();
			}else{
				aq.id(R.id.name2).gone();
				aq.id(R.id.to_symbol).gone();
			}
			
			aq.id(R.id.desc).text(item.getDesc2(), true);
			aq.id(R.id.meta).text(item.getMeta());
			
			String tb = from.getTb();
			
			
			if(aq.shouldDelay(convertView, parent, tb, 0, false)){
				aq.id(R.id.tb).clear();
			}else{
				aq.id(R.id.tb).image(tb, true, true, 0, R.drawable.ic_menu_report_image, null, AQuery.FADE_IN_FILE);
			}
			
			String contentTb = item.getContentTb();
			String contentDesc = item.getContentDesc();
			
			String type = item.getType();
			
			if(contentTb != null || contentDesc != null){
				
				aq.id(R.id.content_box).visible();
				
				int icon = 0;
				
				
				if(contentTb != null && !"checkin".equals(type)){
				
					int tbw = item.getCTbWidth();
					if(tbw == 0) tbw = 90;						
					
					tbw = (int) (tbw / 1.5);
					
					aq.id(R.id.content_tb).width(tbw, true).height(tbw, true);
					
					aq.invisible();
					
					
					if(aq.shouldDelay(convertView, parent, contentTb, 0, false)){						
						aq.clear();
						icon = item.getActionIcon();
					}else{
						
						float ratio = AQuery.RATIO_PRESERVE;
						if(contentDesc == null || contentDesc.length() <= 10){
							ratio = 1.0f;
						}
						
						
						if("photo".equals(item.getType())){
							aq.auth(handle);
						}
						
						AQUtility.debug(item.getName(), contentTb);
						aq.image(contentTb, true, true, 0, R.drawable.ic_menu_report_image, null, AQuery.FADE_IN_FILE, ratio);
						icon = item.getActionIcon();
					}
					
					aq.id(R.id.image_box).visible();
				}else{
					aq.id(R.id.image_box).gone();
				}
				
				aq.id(R.id.content_action);
				if(icon != 0){
					aq.visible().image(IconUtility.getCached(icon));
				}else{
					aq.gone();
				}
				
				aq.id(R.id.content_name).text(item.getContentName2(), true);
				aq.id(R.id.content_desc).text(item.getContentDesc2(), true);
				aq.id(R.id.content_meta).text(item.getContentMeta(), BufferType.NORMAL, true);					
				
				
			}else{
				aq.id(R.id.content_box).gone();
			}
			
			int likeCount = item.getLikeCount();
			if(likeCount > 0){
				aq.id(R.id.like_count).text(item.getLikeString()).visible();
			}else{
				aq.id(R.id.like_count).gone();
			}
			
			int commentCount = item.getCommentCount();
			if(commentCount > 0){
				aq.id(R.id.comment_count).text(item.getCommentString()).visible();
			}else{
				aq.id(R.id.comment_count).gone();
			}
			
			String commentTb = null;
			
			List<Comment> comments = item.getComments();
			Comment comment = null;
			if(comments != null && comments.size() > 0){
				comment = comments.get(0);
				commentTb = comment.getTb();
			}
			
			if(commentTb != null){
				
				aq.id(R.id.comment_box).visible();
				aq.id(R.id.comment_desc).text(comment.getMessage2(), true);	
				
				aq.id(R.id.comment_tb);
				
				
				if(aq.shouldDelay(convertView, parent, commentTb, 0, false)){						
					aq.clear();
				}else{
					aq.image(commentTb, true, true, 0, R.drawable.ic_menu_report_image, null, AQuery.FADE_IN_FILE);
				}
				
				aq.id(R.id.button_comment);
				
			}else{
				aq.id(R.id.comment_box).gone();
			}
			
			
			return convertView;
			
		}
		
	};
	
	
	@Override
	protected int getContainerView() {
		return R.layout.fragment_feed;
	}

	private String getModeDisplay(){
		
		String url = getUrl();
		
		if(url.endsWith("/feed")){
			return FeedMode.WALL.getDisplay();
		}else if(url.endsWith("/home")){
			return FeedMode.NEWS.getDisplay();
		}
		
		return null;
		
	}
	
    public String makeTitle(long time){
    	
    	String result = source.getName();
    	
    	if(act.isRoot()){    	
    		
    		String mode = getModeDisplay();
    		if(mode != null){
    			result += " - " + mode;
    		}
    	}
    	
    	if(time > 0){
    		result += " - " + DateUtils.formatSameDayTime(time, System.currentTimeMillis(), DateFormat.SHORT, DateFormat.SHORT);
    	}
    	
    	return result;
    }
	
}
