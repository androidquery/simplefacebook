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


import org.json.JSONObject;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.TextView.BufferType;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.simplefeed.PQuery;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.activity.FeedActivity;
import com.androidquery.simplefeed.activity.PostActivity;
import com.androidquery.simplefeed.base.BaseFragment;
import com.androidquery.simplefeed.base.Constants;
import com.androidquery.simplefeed.data.Comment;
import com.androidquery.simplefeed.data.Entity;
import com.androidquery.simplefeed.data.FeedItem;
import com.androidquery.simplefeed.data.PageAdapter;
import com.androidquery.simplefeed.util.AppUtility;
import com.androidquery.simplefeed.util.ErrorReporter;
import com.androidquery.simplefeed.util.IconUtility;
import com.androidquery.simplefeed.util.IntentUtility;
import com.androidquery.simplefeed.util.JsonUtility;
import com.androidquery.util.AQUtility;

public class CommentFragment extends BaseFragment{

	private static long TEN_MIN = 10 * 60 * 1000;
	private FeedItem item;
	private PageAdapter<Comment> comments;
	private boolean drawContent;
	private View header;
	private boolean maxLines = true;
	
    @Override
    protected void init(){
    	
    	initView();
    	
    	Intent intent = act.getIntent();
    	FeedItem item = (FeedItem) intent.getSerializableExtra("item");
    	
    	
    	if(item != null){
    		drawContent = true;
    		maxLines = false;
    		item.setCommentable(true);
    		setItem(item);  		
    	}
    	
    }
    
    private void initView(){
    	
    	comments = new CommentAdapter();
    	
    	aq.id(R.id.list);
    	
    	ListView lv = aq.getListView();
    	
    	header = aq.inflate(null, R.layout.fragment_comment_header, null);
    
    	lv.addHeaderView(header);
    	
    	aq.adapter(comments).scrolledBottom(this, "scrolledBottom").itemClicked(this, "itemClicked");
    	
    	aq.id(R.id.button_send).clicked(this, "send");
    	
    	aq.id(R.id.content_tb).clicked(this, "contentClicked");
    	aq.id(R.id.content_name).clicked(this, "contentClicked");
    	
    	aq.id(R.id.edit_comment).clicked(this, "editClicked");
    	
    }
    
    public void likeClicked(View view){
    	AQUtility.debug("likeClicked");
    	
    	like(item);
    	
    }
    
    @Override
    public void likeCb(String url, String str, AjaxStatus status){
    	
    	super.likeCb(url, str, status);
    	
    	if(item != null){
    		aq.id(R.id.button_like).enabled(false);
    		item.setLiked(true);
    	}
    	
    }
    
    public void fromClicked(View view){
    	AQUtility.debug("fromClicked");
    	
    	if(item != null){ 	
    		Entity source = item.getFrom();   
    		if(source.getId() != null){
    			FeedActivity.start(act, source);
    		}
    	}
    }
    
    public void toClicked(View view){
    	AQUtility.debug("toClicked");
    	if(item != null){ 	
    		Entity source = item.getTo();   
    		if(source.getId() != null){
    			FeedActivity.start(act, source);
    		}
    	}
    }
    
    //12-08 00:58:39.960: W/AQuery(12570): http://www.facebook.com/notes/sunset-liu/test-note/2825427477667:null

    public void editClicked(View view){
    	
    	AQUtility.debug("edit clicked");
    	//PostActivity.start(act, item);
    	
    	Intent intent = new Intent(act, PostActivity.class);
		intent.putExtra("item", item);
		
		//act.startActivityForResult(intent, Constants.ACTIVITY_POST);
    	aq.start(act, intent, Constants.ACTIVITY_POST, this, "postCb");
		
    }
    
    public void postCb(int requestCode, int resultCode, Intent data){
    	
    	AQUtility.debug("getting result!");
    	
    	if(data == null) return;
    	
		String toast = data.getStringExtra("toast");
		act.showToast(toast);
		
		String itemId = data.getStringExtra("itemId");
		String message = data.getStringExtra("message");
		if(itemId != null){
			commented(itemId, message);
		}
		
    	
    }
    
    
    public void contentClicked(View view){
    	
    	if(item == null) return;  	
    	IntentUtility.launchActivity(act, item);
    }
    
	private void commented(String itemId, String str){
		
		
		if(str != null && item != null){
			
			//act.showToast(getString(R.string.commented));
			
			AQUtility.debug("commentCb", str);
			
			Comment comment = new Comment();
			comment.setMessage(str);
			comment.setName(AppUtility.getUserName());
			String pic = "https://graph.facebook.com/me/picture";
			pic = handle.getNetworkUrl(pic);
			comment.setTb(getProfileTb());
			item.getComments().add(0, comment);
			item.setCommentCount(item.getCommentCount() + 1);
			comment.setTime(System.currentTimeMillis());
			
			aq.id(R.id.list).getListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);	
			
			
			comments.add(comment);
			
			aq.id(R.id.edit_comment).clear();
			
			
		}
		
		
	}
	
	
	
	private String getProfileTb(){
		String pic = "https://graph.facebook.com/me/picture";
		pic = handle.getNetworkUrl(pic);
		return pic;
	}
    
	@Override
	protected int getContainerView() {
		return R.layout.fragment_comment;
	}

	
	public void setItem(FeedItem newItem){
		
		if(newItem == null) return;
		
		if(item != null && item.getId().equals(newItem.getId())){
			return;
		}
		
		if(!newItem.isCommentable()){
			return;
		}
		
		this.item = newItem;
		
		renderHeader(item);
		
		comments.clear();
		
		ajaxComments(0);
	}
	
	private void renderHeader(FeedItem item){
		
		String subject = item.getSubject();
		
		if(subject != null && !act.isTablet()){
			aq.id(R.id.normal_box).gone();
			aq.id(R.id.note_box).visible();
			renderNote(item);
		}else{
			if(!"note".equals(item.getApp())){
				aq.id(R.id.normal_box).visible();
				aq.id(R.id.note_box).gone();
				renderNormal(item);
			}
		}
		
	}
	
	//<img 
	private String patchImg(String html){
		
		return html.replaceAll("<img ", "<img style=\"max-width:100%;\"");
		
	}
	
	private void renderNote(FeedItem item){
	
		PQuery aq = super.aq.id(header).find(R.id.note_box);

		Entity from = item.getFrom();
		
		aq.id(R.id.name).text(from.getName());
		aq.id(R.id.subject).text(item.getSubject());
		
		aq.id(R.id.meta).text(item.getCachedTime(System.currentTimeMillis()));
		
		
		String tb = from.getTb();
		
		aq.id(R.id.tb).image(tb, true, true, 0, R.drawable.ic_menu_report_image, null, AQuery.FADE_IN_NETWORK);
		
		
		
		String body = item.getMessage();
		body = patchImg(body);
		
		AQUtility.debug("body", body);
		
		WebView wv = aq.id(R.id.web).visible().getWebView();
		wv.loadDataWithBaseURL(null, body, "text/html", "utf-8", null);
		
		aq.id(R.id.content_box).gone();
		renderCount();
		
		
	}
	
	
	private void renderNormal(FeedItem item){
		
		Entity from = item.getFrom();
		String name = from.getName();
		
		if(name == null){		
			return;
		}
		
		if(act.isTablet()){
			aq.id(R.id.action_box).visible();
		}
		
		aq.id(R.id.list).visible();
		
		PQuery aq = super.aq.id(header).find(R.id.normal_box);
		
		aq.visible();
		
		aq.id(R.id.name).text(name);
		
		String desc = item.getDesc();
		String subject = item.getSubject();
		
		if(act.isTablet() && subject != null){
			desc = subject;
		}
		
		aq.id(R.id.desc).text(desc, BufferType.SPANNABLE, true);
		if(maxLines){
			aq.getTextView().setMaxLines(2);
		}
		
		aq.id(R.id.meta).text(item.getCachedTime(System.currentTimeMillis()));
		
		String tb = from.getTb();
		
		aq.id(R.id.tb).image(tb, true, true, 0, R.drawable.ic_menu_report_image, null, AQuery.FADE_IN_NETWORK);
		
		
		String contentTb = item.getContentTb();
		String contentDesc = item.getContentDesc();
		
		if(drawContent && (contentTb != null || contentDesc != null)){
			
			aq.id(R.id.content_box).visible();
			
			int icon = 0;
			
			if(contentTb != null){
			
				int tbw = item.getCTbWidth();
				if(tbw == 0) tbw = 90;						
				
				aq.id(R.id.content_tb).width(tbw, false).height(tbw / 2, false);
				aq.tag(item);
				
				aq.image(contentTb, true, true, 0, R.drawable.ic_menu_report_image, null, AQuery.FADE_IN_NETWORK, AQuery.RATIO_PRESERVE);
				icon = item.getActionIcon();
				
				aq.id(R.id.content_ph).visible();
			}else{
				aq.id(R.id.content_tb).clear().gone();
				aq.id(R.id.content_ph).gone();
			}
			
			aq.id(R.id.content_action);
			if(icon != 0){
				aq.visible().image(IconUtility.getCached(icon));
			}else{
				aq.gone();
			}
			
			aq.id(R.id.content_name).text(item.getContentName(), BufferType.SPANNABLE, true).tag(item);
			aq.id(R.id.content_desc).text(contentDesc, BufferType.SPANNABLE, true);
			aq.id(R.id.content_meta).text(item.getContentMeta(), BufferType.SPANNABLE, true);					
			
		}else{
			aq.id(R.id.content_box).gone();
		}
		

		renderCount();
	}
	
	
	private void renderCount(){
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
	}
	
	public void refresh(){
		
		ajaxComments(-1);
		
	}
	
	private void ajaxComments(long expire){
		
		if(item == null) return;
		
		if(item.resolved()){			
			ajaxDetail(item, expire);
		}else{
			ajaxResolve(item, expire);
		}
		
		
		
	}
	
	private void ajaxResolve(final FeedItem item, final long expire){
		
		AQUtility.debug("resolve start");
		
		final String resolve = item.getResolve();
		
		String url = "https://graph.facebook.com/" + resolve;
		
		aq.auth(handle).ajax(url, JSONObject.class, 0, new AjaxCallback<JSONObject>(){
			
			@Override
			public void callback(String url, JSONObject jo, AjaxStatus status) {
			
				if(jo != null){
					String value = jo.optString("id");
					item.resolve(resolve, value);
					ajaxDetail(item, expire);
				}
				
			}
		});
		
	}
	
	
	
	private void ajaxDetail(FeedItem item, long expire){
		
		showProgress(true);
		
		String url = "https://graph.facebook.com/"+ item.getDetailId() + "?locale=" + locale;
		
		aq.auth(handle).ajax(url, JSONObject.class, expire, this, "detailCb");
		
	}
	
	//11-26 18:46:31.429: W/AQuery(24688): jo:{"position":2,"picture":"http:\/\/photos-f.ak.fbcdn.net\/hphotos-ak-ash4\/311015_2677708751265_1512652234_2758351_2054808874_s.jpg","id":"2677708751265","icon":"http:\/\/static.ak.fbcdn.net\/rsrc.php\/v1\/yz\/r\/StEh3RhPvjk.gif","height":540,"source":"http:\/\/a6.sphotos.ak.fbcdn.net\/hphotos-ak-ash4\/s720x720\/311015_2677708751265_1512652234_2758351_2054808874_n.jpg","likes":{"data":[{"id":"1448780323","name":"Wendy Lin"}],"paging":{"next":"https:\/\/graph.facebook.com\/2677708751265\/likes?access_token=AAAEUiDdRZCLIBAMyuPHhGxbX04kuBcZC55NHZAZBKxZAOwGyZCpSGDUre8lonCjtoBiM4O92sx0t5VawnJrTiDne0PVmkVCe4ZD&limit=25&offset=25&__after_id=1448780323"}},"link":"http:\/\/www.facebook.com\/photo.php?pid=2758351&id=1512652234","width":720,"images":[{"source":"http:\/\/hphotos-iad1.fbcdn.net\/hphotos-ash4\/336196_2677708751265_1512652234_2758351_2054808874_o.jpg","width":2048,"height":1536},{"source":"http:\/\/a6.sphotos.ak.fbcdn.net\/hphotos-ak-ash4\/s720x720\/311015_2677708751265_1512652234_2758351_2054808874_n.jpg","width":720,"height":540},{"source":"http:\/\/photos-f.ak.fbcdn.net\/hphotos-ak-ash4\/311015_2677708751265_1512652234_2758351_2054808874_a.jpg","width":180,"height":135},{"source":"http:\/\/photos-f.ak.fbcdn.net\/hphotos-ak-ash4\/311015_2677708751265_1512652234_2758351_2054808874_s.jpg","width":130,"height":97},{"source":"http:\/\/photos-f.ak.fbcdn.net\/hphotos-ak-ash4\/311015_2677708751265_1512652234_2758351_2054808874_t.jpg","width":75,"height":56}],"from":{"id":"1512652234","name":"Peter Liu"},"created_time":"2011-11-25T13:06:17+0000","updated_time":"2011-11-25T13:06:20+0000"}

	public void detailCb(String url, JSONObject jo, AjaxStatus status){
		
		AQUtility.debug("jo", jo);
		
		showProgress(false);
		aq.id(R.id.progress).gone();
		
		if(jo != null){
		
			comments.clear();
			
			item = new FeedItem(jo);
			
			renderHeader(item);
			
			String next = JsonUtility.getString(jo, "comments", "paging");
			
			comments.add(item.getComments(), next);
			//items.setLoadable(feed.size() >= 25);
			
			
			if(status.expired(TEN_MIN)){
				refresh();
			}
			
			act.updateTitle(status.getTime().getTime());
			
			aq.id(R.id.list).visible();
			

			aq.id(R.id.text_error).gone();
			
			AQUtility.debug("done");
			
		}else if(status.getCode() == AjaxStatus.TRANSFORM_ERROR || (status.getCode() >= 400 && status.getCode() < 500)){	
			
			aq.id(R.id.text_error).visible();
			ErrorReporter.report("Cannot get comment:" + url + ":" + item.getLink() + ":" + item.getMessage() + ":" + item.getName() + ":" + item.getId());
		}
		
		
	}
	 
    private class CommentAdapter extends PageAdapter<Comment>{
		
		public View render(int position, View convertView, ViewGroup parent) {
			
			/*
			if(isLoading(position)){
				return getLoadingView(convertView, R.layout.item_progress);
			}
			*/
			
			if(convertView == null){					
				convertView = act.getLayoutInflater().inflate(R.layout.item_comment, null);					
				//initItemView(convertView);
			}
			
			PQuery aq = aq2.recycle(convertView);
			
			Comment item = (Comment) getItem(position);
			
			aq.id(R.id.name).text(item.getName());
			aq.id(R.id.desc);
			//aq.setLayerType11(AQuery.LAYER_TYPE_SOFTWARE, null);
			aq.text(item.getMessage2(), true);
			
			String tb = item.getTb();
			
			
			if(aq.shouldDelay(convertView, parent, tb, 0, false)){
				aq.id(R.id.tb).clear();
			}else{
				aq.id(R.id.tb).image(tb, true, true, 0, R.drawable.ic_menu_report_image, null, AQuery.FADE_IN_NETWORK);
			}
			
			aq.id(R.id.meta).text(item.getCachedTime(System.currentTimeMillis()));
			
			return convertView;
			
		}
		
	};
}
