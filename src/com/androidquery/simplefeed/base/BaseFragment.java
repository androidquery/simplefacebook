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
package com.androidquery.simplefeed.base;


import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidquery.auth.FacebookHandle;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.Transformer;
import com.androidquery.simplefeed.PQuery;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.data.FeedItem;
import com.androidquery.simplefeed.util.PrefUtility;
import com.androidquery.util.AQUtility;

public abstract class BaseFragment extends Fragment {

	protected static long TEN_MIN = 10 * 60 * 1000;
	
	
	protected BaseActivity act;
	protected PQuery aq;
	protected PQuery aq2;
	protected FacebookHandle handle;
	protected boolean debug = PrefUtility.isTestDevice();
	protected String locale;
	protected Transformer transformer;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
    	View view = inflater.inflate(getContainerView(), container, false);        	
		
    	act = (BaseActivity) getActivity();
    	
    	aq = new PQuery(act, view);
		aq2 = new PQuery(act, view);
		
		locale = act.locale;		
		handle = act.handle;
		transformer = act.transformer;
    	
		return view;
		
    }
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState){
    	
    	
    	super.onActivityCreated(savedInstanceState);
    	
    	try{
    		init();		
		}catch(Exception e){
			AQUtility.report(e);
		}
    }
    
    protected abstract void init();
    
    protected abstract int getContainerView();
	
    protected boolean isActive(){
    	return act != null && !act.isFinishing();
    }
    
    public void refresh(){
    	
    }
    
    protected void showProgress(boolean progress){
    	act.showProgress(progress);
    }
    
	protected void like(FeedItem actionItem){
		
		if(actionItem == null) return;
		
		//String id = actionItem.getItemId();
		String id = actionItem.getActionId();
		
		String url = "https://graph.facebook.com/" + id + "/likes";
		
		showProgress(true);
		
		Map<String, Object> params = new HashMap<String, Object>();
		aq.auth(handle).ajax(url, params, String.class, this, "likeCb");
		
		actionItem.setLiked(true);
	}
	
	public void likeCb(String url, String str, AjaxStatus status){
		
		showProgress(false);
		
		AQUtility.debug("likeCb", str);
		if(str != null){
			act.showToast(getString(R.string.liked));
		}
		
		
		
	}
    
    
}
