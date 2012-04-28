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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.base.MenuActivity;
import com.androidquery.simplefeed.data.Entity;
import com.androidquery.simplefeed.enums.FeedMode;
import com.androidquery.simplefeed.fragments.CommentFragment;
import com.androidquery.simplefeed.fragments.FeedFragment;
import com.androidquery.simplefeed.util.AppUtility;
import com.androidquery.simplefeed.util.PrefUtility;
import com.androidquery.util.AQUtility;

public class FeedActivity extends MenuActivity {
    
	private boolean logout;
	private Entity source;
	
    protected void init(Bundle savedInstanceState){
    
    	Intent intent = getIntent();
    	if(intent != null){
    		logout = intent.getBooleanExtra("logout", false);
    		source = (Entity) intent.getSerializableExtra("source");
    	}
    	    
    	if(source == null){
    		source = AppUtility.getDefaultSource();
    	}
    	
        initView();
        
        AQUtility.debug("fed act logout", logout);
        
        if(!logout){
        	checkProfile();
        }else{
        	aq.id(R.id.login_panel).visible();
        }
        
    }
    
    public Entity getSource(){
    	return source;
    }
    
    private void initView(){
    	aq.id(R.id.login_button).clicked(this, "loginClicked");        
    }
    
    
    protected int getContainerView(){
    	return R.layout.activity_feed;
    }
    
    @Override
    public void onNewIntent(Intent intent){
    	
    	if(intent == null) return;
    	
    	logout = intent.getBooleanExtra("logout", false);
    	
    	if(logout){
    		showLogin();
    	}
    	
    	
    }
    
    private void showLogin(){
    	aq.id(R.id.login_panel).visible();
		aq.id(R.id.list).gone();
		showProgress(false);
    }
    
    
    @Override
    protected String makeTitle(long time){
    	
    	FeedFragment ff = (FeedFragment) getFragment(R.id.frag_feed);
    	return ff.makeTitle(time);
    	
    }
    
    
    public void loginClicked(View view){
    	
    	initAjax();
    }
    

    
    
    @Override
    public void modeChange(FeedMode mode){
    	
    	FeedFragment ff = (FeedFragment) getFragment(R.id.frag_feed);
    	ff.modeChange(mode);
    	
    }
  
    
    public static void start(Activity act, Entity source){
    	
    	if(source == null) return;
    	String id = source.getId();
    	if(id == null) return;
    	
    	Intent intent = new Intent(act, FeedActivity.class);    	
    	intent.putExtra("source", source);
    	
    	act.startActivity(intent);
    	
    }
    
    public static void startLaunch(Activity act, Entity source, FeedMode mode){
    	
    	if(source == null || mode == null) return;
    	String id = source.getId();
    	if(id == null) return;
    	
	    Intent intent = new Intent(act, LaunchActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		PrefUtility.putEnum(mode);
		
		act.startActivity(intent);
    }
	
    
    private void initAjax(){
    	FeedFragment ff = (FeedFragment) getFragment(R.id.frag_feed);
    	ff.initAjax(0);
    }
    
    
	@Override
	public void refresh(){
		
		FeedFragment ff = (FeedFragment) getFragment(R.id.frag_feed);
    	ff.refresh();
		
    	CommentFragment cf = (CommentFragment) getFragment(R.id.frag_comment);
    	if(cf != null){
    		cf.refresh();
    	}
    	
	}
	
	@Override
	protected int getMenu(){
		
		if("me".equals(source.getId())){		
			return R.menu.feed_me;
		}else{
			return R.menu.feed_other;
		}
		
		
	}
    
	@Override
	public FeedMode getMode(){
		if(source.isMe()) return AppUtility.getDefaultMode();
		return null;
	}
   
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
		
        switch (item.getItemId()) {
	        case R.id.status:
	            PostActivity.start(this, new Entity());
	        	return true;
	        case R.id.comment:
	        	if(source != null){
	        		PostActivity.start(this, source);
	        	}
	            return true; 
	          
	        case R.id.all_data:
	        	AllDataActivity.start(this);
	            return true; 
	        
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
	

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		
		FeedFragment ff = (FeedFragment) getFragment(R.id.frag_feed);
		
	    switch(requestCode){
	    	case PostActivity.REQUEST:
	    		if(data != null){
	    			String toast = data.getStringExtra("toast");
	    			showToast(toast);
	    			//boolean refresh = data.getBooleanExtra("refresh", false);
	    			//if(refresh) refresh();
	    			String itemId = data.getStringExtra("itemId");
	    			String message = data.getStringExtra("message");
	    			
	    			if(itemId != null){
	    				ff.commented(itemId, message);
	    			}else{
	    				ff.updated(message);
	    			}
	    		}
	    		break;
	    		
	    	case NotificationActivity.REQUEST:
	    		
	    		ff.ajaxNoti(-1);
	    		
	    		break;
	    	default:
	    		super.onActivityResult(requestCode, resultCode, data);
	    		break;
	    }
	}
	
	
    
    @Override
	public void showProgress(boolean progress){
		
		super.showProgress(progress);
		if(progress){
			aq.id(R.id.login_progress).visible();
			aq.id(R.id.login_button).gone();
		}else{
			aq.id(R.id.login_progress).gone();
			aq.id(R.id.login_button).visible();
		}
		
	}

	
	
}
