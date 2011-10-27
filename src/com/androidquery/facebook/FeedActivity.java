package com.androidquery.facebook;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.facebook.data.Comment;
import com.androidquery.facebook.data.FeedItem;
import com.androidquery.facebook.data.Like;
import com.androidquery.facebook.data.PageAdapter;
import com.androidquery.facebook.enums.FeedMode;
import com.androidquery.facebook.util.FormatUtility;
import com.androidquery.facebook.util.IconUtility;
import com.androidquery.facebook.util.IntentUtility;
import com.androidquery.facebook.util.ParseUtility;
import com.androidquery.facebook.util.PrefUtility;
import com.androidquery.util.AQUtility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

public class FeedActivity extends AbstractActivity {
    
	private static long TEN_MIN = 10 * 60 * 1000;
	
	private PageAdapter<FeedItem> items;
	private long lastTime;
	private boolean logout;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_list);
        
        init();
        initView();
        
        if(!logout){
        	ajaxHome(0);        
        	checkProfile();
        }
    }
    
    private void init(){
    	
    	Intent intent = getIntent();
    	if(intent != null){
    		logout = intent.getBooleanExtra("logout", false);
    	}
    	    
    }
    
    @Override
    public void onNewIntent(Intent intent){
    	
    	if(intent == null) return;
    	
    	logout = intent.getBooleanExtra("logout", false);
    	
    	if(logout){
    		aq.id(R.id.login_panel).visible();
    		aq.id(R.id.list).gone();
    		showProgress(false);
    	}
    	
    	
    }
    
    
    @Override
    protected String makeTitle(){
    	String result = getUserName("Simple FB");
    	if(lastTime > 0){
    		result += " - " + getMode().getDisplay() + " - " + DateUtils.formatSameDayTime(lastTime, System.currentTimeMillis(), DateFormat.SHORT, DateFormat.SHORT);
    	}
    	return result;
    }
    
    private FeedItem actionItem;
    public void itemClicked(AdapterView<?> parent, View v, int pos, long id){
    	
    	FeedItem item = (FeedItem) items.getItem(pos);
    	if(item == null) return;
    	
    	actionItem = item;
    	
    	Dialog dialog = makeActionDialog();
    	aq.show(dialog);
    	
    }
    
    
    public void ctbClicked(View view){
    	
    	
    	FeedItem item = (FeedItem) view.getTag();
    	if(item == null) return;
    	
    	String url = item.getLink();
    	if(url == null) return;
    	
    	AQUtility.debug("clicked", url);
    	
    	if(ParseUtility.isYT(url)){
    		IntentUtility.openBrowser(this, url);
    	}else if("photo".equals(item.getType())){
    		String tb = item.getContentTb();    		
    		if(tb != null){
    			tb = tb.replaceAll("_s.", "_n.");
    			//IntentUtility.openBrowser(this, tb);
    			Intent intent = new Intent(this, ImageActivity.class);	 
    			intent.putExtra("url", tb);
    			intent.putExtra("title", item.getName());
    	    	startActivity(intent);
    		}
    	}else if(!url.contains("facebook.com")){
    		IntentUtility.openBrowser(this, url);
    	}
    	
    }
    
    private void initItemView(View view){

    	AQuery aq = listAq.recycle(view);
    	
    	OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ctbClicked(v);
			}
		};
    	
		aq.id(R.id.content_tb).clicked(listener);
		aq.id(R.id.content_name).clicked(listener);
    }
    
    
    private AQuery listAq = new AQuery(this);
    private void initView(){
    	
    	items = new FeedAdapter();
    	
    	items.setLoadable(true);
    	aq.id(R.id.list).adapter(items).scrolledBottom(this, "scrolledBottom").itemClicked(this, "itemClicked");
    	
    	aq.id(R.id.login_button).clicked(this, "login");
    
    }
    
    public void login(View view){
    	
    	
    	ajaxHome(0);
    	
    	
    }
    
    public void scrolledBottom(AbsListView view, int scrollState){
    	
    	ajaxMore();
    }
    
    @Override
    public void modeChange(FeedMode mode){
    	
    	ajaxHome(0);
    	
    }
    
    private String getUrl(){
    	
    	return getMode().getUrl();
    	
    }
    
    
	private void ajaxHome(long expire){
	    
		String url = getUrl() + "?limit=25";

		showProgress(true);
		aq.auth(handle).ajax(url, JSONObject.class, expire, this, "homeCb");
	    
	}	
	
	private void ajaxMore(){
		
		String url = items.getNext();
		if(url == null || items.isLoading() || !items.isLoadable()) return;
		
		showProgress(true);		
		aq.auth(handle).ajax(url, JSONObject.class, TEN_MIN, this, "moreCb");
	}
	
	
	private List<FeedItem> toList(JSONArray ja){
		
		List<FeedItem> result = new ArrayList<FeedItem>();
		
		for(int i = 0; i < ja.length(); i++){
			result.add(new FeedItem(ja.optJSONObject(i)));
		}
		
		return result;
	}
	
	@Override
	public void showProgress(boolean progress){
		
		super.showProgress(progress);
		items.setLoading(progress);	
		if(progress){
			aq.id(R.id.login_progress).visible();
			aq.id(R.id.login_button).gone();
		}else{
			aq.id(R.id.login_progress).gone();
			aq.id(R.id.login_button).visible();
		}
	}
	
	public void homeCb(String url, JSONObject jo, AjaxStatus status){
		
		
		AQUtility.debug(jo);
		
		showProgress(false);
		
		items.clear();
		
		if(jo != null){
			
			JSONArray data = jo.optJSONArray("data");
			
			String next = jo.optJSONObject("paging").optString("next", null);
			
			List<FeedItem> feed = toList(data);
			
			items.add(feed, next);
			items.setLoadable(feed.size() >= 25);
			
			
			if(status.expired(TEN_MIN)){
				refresh();
			}
			
			lastTime = status.getTime().getTime();
			updateTitle();
			
			aq.id(R.id.login_panel).gone();
			aq.id(R.id.list).visible();
		}
		
		
	}
	
	@Override
	public void refresh(){
		
		if(isBusy()) return;
		
		ajaxHome(-1);
		
		
	}
	
	public void moreCb(String url, JSONObject jo, AjaxStatus status){

		
		AQUtility.debug(jo);
		
		showProgress(false);
		
		if(jo != null){
			
			JSONArray data = jo.optJSONArray("data");			
			String next = jo.optJSONObject("paging").optString("next", null);			
			items.add(toList(data), next);
			
		}
		
	}
	

	
	private void status(){
		ask("Status Update", true);
	}
	
	private void comment(){
		ask("Comment", false);
	}
	
	private void ask(String title, final boolean status){
		
		View view = getLayoutInflater().inflate(R.layout.comment_dialog, null);
		
		AlertDialog dialog = new AlertDialog.Builder(this)
		.setTitle(title)
		.setView(view)
		.setNeutralButton("Send", new Dialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				AlertDialog d = (AlertDialog) dialog;
				EditText edit = (EditText) d.findViewById(R.id.input);
				
				String message = edit.getEditableText().toString().trim();
				
				if(status){
					status(message);
				}else{
					comment(message);
				}
				dialog.dismiss();
			}
		})
		.setPositiveButton("Cancel", new Dialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				dialog.dismiss();
				
			}
		})
		.create();
		
		dialog.show();
		
	}
	
	private void status(final String message){
		
		String url = "https://graph.facebook.com/me/feed";
		
		showProgress(true);
		
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("message", message);
		
		aq.auth(handle).ajax(url, params, String.class, new AjaxCallback<String>(){
			
			@Override
			public void callback(String url, String object, AjaxStatus status) {
				statusCb(url, object, status, message);
			}
			
		});
		
		
		
	}
	
	private void refreshView(){
		aq.id(R.id.list).dataChanged();
	}
	
	public void statusCb(String url, String str, AjaxStatus status, String message){
		
		showProgress(false);
		
		if(str != null){
			showToast("Updated");
			if(FeedMode.WALL.equals(getMode())){
				
				if(items.getCount() > 0){
					FeedItem old = (FeedItem) items.getItem(0);
					FeedItem item = new FeedItem();
					item.setName(old.getName());
					item.setDesc(message);
					item.setTb(old.getTb());
					item.setTime(System.currentTimeMillis());
					items.add(0, item);
				}else{
					refresh();
				}
				
				
			}
		}
		
		
	}
	
	private void comment(final String message){
		
		if(actionItem == null) return;	
		
		String id = actionItem.getItemId();
		
		String url = "https://graph.facebook.com/" + id + "/comments";
		
		showProgress(true);
		
		final FeedItem commentItem = actionItem;
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("message", message);
		
		aq.auth(handle).ajax(url, params, String.class, new AjaxCallback<String>(){
			
			@Override
			public void callback(String url, String object, AjaxStatus status) {
				commentCb(url, object, status, commentItem, message);
			}
			
		});
		
		
		
	}
	
	public void commentCb(String url, String str, AjaxStatus status, FeedItem commentItem, String message){
		
		showProgress(false);
		
		
		if(str != null){
			showToast("Commented");
			
				
			AQUtility.debug("commentCb", str);
			
			Comment comment = new Comment();
			comment.setMessage(message);
			comment.setName(getUserName("Me"));
			String pic = "https://graph.facebook.com/me/picture";
			pic = handle.getNetworkUrl(pic);
			comment.setTb(getProfileTb());
			commentItem.getComments().add(0, comment);
			commentItem.setCommentCount(commentItem.getCommentCount() + 1);
			
			refreshView();
			
		}
		
		
	}
	
	private String getProfileTb(){
		String pic = "https://graph.facebook.com/me/picture";
		pic = handle.getNetworkUrl(pic);
		return pic;
	}
	
	
	private void like(){
		
		if(actionItem == null) return;
		
		String id = actionItem.getItemId();
		
		String url = "https://graph.facebook.com/" + id + "/likes";
		
		showProgress(true);
		
		Map<String, Object> params = new HashMap<String, Object>();
		aq.auth(handle).ajax(url, params, String.class, this, "likeCb");
		
	}
	
	public void likeCb(String url, String str, AjaxStatus status){
		
		showProgress(false);
		
		AQUtility.debug("likeCb", str);
		if(str != null){
			showToast("Liked");
		}
		
		
	}
	
	
    private Dialog makeActionDialog(){
    	
    	return new AlertDialog.Builder(this)    	
        .setItems(R.array.action_items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            	
            	switch(which){
            		case 0:          			
            			like();
            			break;
            		case 1:
            			comment();      			
            			break;	
            		
            		default:
            	}
            	
            	
            }
        })
        .create();
    }    
    
    public boolean onOptionsItemSelected(MenuItem item) {
        
        switch (item.getItemId()) {
	        case R.id.status:
	        	status();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	AQUtility.cleanCacheAsync(this);
    }
    
    
    private class FeedAdapter extends PageAdapter<FeedItem>{
		
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if(isLoading(position)){
				return getLoadingView(convertView, R.layout.progress_item);
			}
			
			if(convertView == null){					
				convertView = getLayoutInflater().inflate(R.layout.feed_item, null);					
				initItemView(convertView);
			}
			
			AQuery aq = listAq.recycle(convertView);
			
			FeedItem item = (FeedItem) getItem(position);
			
			aq.id(R.id.name).text(item.getName());
			aq.id(R.id.desc).text(item.getDesc(), true);
			
			aq.id(R.id.meta).text(item.getCachedTime(System.currentTimeMillis()));
			
			String tb = item.getTb();
			
			
			if(aq.shouldDelay(convertView, parent, tb, 0)){
				aq.id(R.id.tb).clear();
			}else{
				aq.id(R.id.tb).image(tb, true, true, 0, 0, null, AQuery.FADE_IN_NETWORK);
			}
			
			String contentTb = item.getContentTb();
			String contentDesc = item.getContentDesc();
			
			if(contentTb != null || contentDesc != null){
				
				aq.id(R.id.content_box).visible();
				
				int icon = 0;
				
				if(contentTb != null){
				
					int tbw = item.getCTbWidth();
					if(tbw == 0) tbw = 90;						
					
					aq.id(R.id.content_tb).width(tbw, false).height(tbw, false);
					aq.tag(item);
					
					
					
					if(aq.shouldDelay(convertView, parent, contentTb, 0)){						
						aq.clear();
					}else{
						aq.image(contentTb, true, true, 0, 0, null, AQuery.FADE_IN_NETWORK, AQuery.RATIO_PRESERVE);
						icon = item.getActionIcon();
					}
					
				}else{
					aq.id(R.id.content_tb).clear().gone();
				}
				
				aq.id(R.id.content_action);
				if(icon != 0){
					aq.visible().image(IconUtility.getCached(icon));
				}else{
					aq.gone();
				}
				
				
				aq.id(R.id.content_name).text(item.getContentName(), true).tag(item);
				aq.id(R.id.content_desc).text(contentDesc, true);
				aq.id(R.id.content_meta).text(item.getContentMeta(), true);					
				
				
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
			String commentDesc = null;
			
			List<Comment> comments = item.getComments();
			
			if(comments.size() > 0){
				Comment comment = comments.get(0);
				commentTb = comment.getTb();
				commentDesc = comment.getMessage();
			}
			
			if(commentTb != null){
				
				aq.id(R.id.comment_box).visible();
				aq.id(R.id.comment_desc).text(commentDesc);	
				
				aq.id(R.id.comment_tb);
				
				if(aq.shouldDelay(convertView, parent, commentTb, 0)){						
					aq.clear();
				}else{
					aq.image(commentTb, true, true, 0, 0, null, AQuery.FADE_IN_NETWORK);
				}
				
			}else{
				aq.id(R.id.comment_box).gone();
			}
			
			return convertView;
			
		}
		
	};
	
	
}