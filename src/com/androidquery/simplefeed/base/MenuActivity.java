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

import java.util.Arrays;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.simplefeed.PQuery;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.activity.FeedActivity;
import com.androidquery.simplefeed.activity.LaunchActivity;
import com.androidquery.simplefeed.activity.SettingsActivity;
import com.androidquery.simplefeed.data.Entity;
import com.androidquery.simplefeed.enums.FeedMode;
import com.androidquery.simplefeed.util.AppUtility;
import com.androidquery.simplefeed.util.PrefUtility;
import com.androidquery.util.AQUtility;

public abstract class MenuActivity extends BaseActivity{

	protected int getMenu(){
		return 0;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState){
	
		super.onCreate(savedInstanceState);
	
		if(isActionBar()){
	    	initActionBar();
	    }
	}
	

	

	private FeedMode currentMode;
	private int currentIndex;
	
	private void initActionBar(){
		
		ActionBar bar = getActionBar();
		if(bar == null) return;
		
		bar.setCustomView(R.layout.action_custom);
		bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		
		PQuery aq = aq2.recycle(bar.getCustomView());
		
		currentMode = getMode();
		
		AQUtility.debug("mode", currentMode);
		
		
		if(currentMode != null){
		
			List<FeedMode> modes = Arrays.asList(FeedMode.values());		
			ArrayAdapter<FeedMode> adapter = new ArrayAdapter<FeedMode>(this, R.layout.action_modes, modes);
			
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    
			currentIndex = modes.indexOf(currentMode);
			aq.id(R.id.spinner_action_bar).adapter(adapter).setSelection(currentIndex).visible().itemSelected(this, "modeSelected");
			
			if(!isTablet()){
				aq.id(R.id.text_action_title).gone();
			}else{
				aq.id(R.id.text_action_title).visible();
			}
		}else{
			aq.id(R.id.text_action_title).visible();
			aq.id(R.id.spinner_action_bar).gone();
		}
		
		Entity entity = getSource();
		
		
		if(entity != null){
			String tb = entity.getTb(handle);
			if(tb != null){
				aq.id(R.id.image_action_bar).image(tb);
			}			
			aq.id(R.id.text_action_title).text(entity.getName());
		}else{
			aq.id(R.id.text_action_title).gone();
		}
		
	}
	
	
	public FeedMode getMode(){
		return null;
	}
	
	public Entity getSource(){
		return AppUtility.getDefaultSource();
	}
	
	
	
	public void modeSelected(AdapterView<?> parent, View view, int position, long id){
		
		FeedMode[] modes = FeedMode.values();
		FeedMode item = modes[position];
		
		if(item.equals(currentMode)){
			return;
		}
		
		if(FeedMode.NEWS.equals(item) || FeedMode.WALL.equals(item)){
			Entity source = AppUtility.getDefaultSource(item);
			FeedActivity.startLaunch(this, source, item);
		}else{
			startMode(this, item.getHandler());
		}
		
		
	}
	
	@Override
	public void onResume(){
		
		super.onResume();
		
		aq.id(R.id.spinner_action_bar).setSelection(currentIndex);
		
	}
	
    private void startMode(Activity act, Class<?> handler){
    	
    	Intent intent = new Intent(act, handler);     	
    	act.startActivity(intent);
    	
    }
	
    private View refreshView;
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
		int id = getMenu();
		
    	try{
    	
    		if(id != 0){
    		
		        MenuInflater inflater = getMenuInflater();
		        inflater.inflate(id, menu);
		        
		        
		        if(!PrefUtility.isTestDevice()){
		        	menu.removeItem(R.id.debug);
		        }
		        
		        if(isActionBar()){
		        	
		        	MenuItem mi = menu.findItem(R.id.refresh);
		        			        	
		        	if(mi != null){
		        		
		        		refreshView = mi.getActionView();
		        		aq2.recycle(refreshView);
		        		aq2.id(R.id.button_action_refresh).clicked(this, "refreshClicked");
		        		
			        	
		        		if(isBusy()){
		        			showActionProgress(true);
		        		}
		        	}
		        	
		        }
		        
    		}
	        
    	}catch(Exception e){
    		AQUtility.report(e);
    	}
        
        return true;
    }
	
    public void showInitProgress(boolean progress){

    	if(progress){
    		aq.id(R.id.progress_init).visible();
    		aq.id(R.id.content_init).gone();
    	}else{
    		aq.id(R.id.progress_init).gone();
    		aq.id(R.id.content_init).visible();
    	}
    	
    	this.progress = progress;
    }
    
    
    private boolean progress;
    public void showProgress(boolean progress){
		
    	
    	this.progress = progress;
    	
    	if(isActionBar()){
    		
    		showActionProgress(progress);
    	}else{
			setProgressBarIndeterminateVisibility(progress);	    	
		}
		
		if(!progress){
			showInitProgress(false);
		}
		
	}
    
    public boolean isBusy(){
    	return progress;
    }
	
	
	public void refreshClicked(View view){
		
		refresh();
	}
	
	private void showActionProgress(boolean progress){
		
		aq2.recycle(refreshView);
		
		if(progress){
			aq2.id(R.id.button_action_refresh).gone();
			aq2.id(R.id.progress_action).visible();
		}else{
			aq2.id(R.id.button_action_refresh).visible();
			aq2.id(R.id.progress_action).gone();
		}
	}
	
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	
    	try{
    	
	        MenuItem mi = menu.findItem(R.id.mode);
	        
	        if(mi != null){
		        FeedMode mode = AppUtility.getDefaultMode();
		        
		        if(FeedMode.NEWS.equals(mode)){
		        	mi.setTitle(FeedMode.WALL.getDisplay());
		        }else{
		        	mi.setTitle(FeedMode.NEWS.getDisplay());
		        }
		        
	        }
    	}catch(Exception e){
    		AQUtility.report(e);
    	}
        
        return true;
    }
	
    public boolean onOptionsItemSelected(MenuItem item) {
        
        switch (item.getItemId()) {
	        case R.id.refresh:
	        	refresh();
	            return true;
	        case R.id.debug:
	        	showDialog(R.id.debug);
	            return true; 
	        case R.id.mode:
	        	modeChange();
	            return true;  
	        case R.id.settings:
	        	settings();
	            return true;     
	        default:
	            return false;
        }
    }
    
    private void settings(){
    	
    	Intent intent = new Intent(this, SettingsActivity.class);
    	startActivity(intent);
    	
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
        
    	if(id == R.id.debug){    	
	    	return makeDebugDialog();
    	}
    	
    	return null;
    }
    
    private Dialog makeDebugDialog(){
    	
    	return new AlertDialog.Builder(this)    	
        .setItems(R.array.debug_items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            	
            	switch(which){
            		case 0:          			
            			AQUtility.cleanCacheAsync(MenuActivity.this, 0, 0);            			
            			BitmapAjaxCallback.clearCache();
            			break;
            		case 1:
            			BitmapAjaxCallback.clearCache();         			
            			break;	
            		case 2:
            			//locale("zh_CN");   
            			locale();
            			break;	
            		default:
            	}
            	
            	
            }
        })
        .create();
    }    
	
    private static int count = 0;
    private void locale(){
		
    	String lang = "";
    	
    	if(count % 2 == 0){
    		lang = "zh";    		
    	}
    	
    	count++;
		
		AppUtility.setLocale(lang);
		AppUtility.resetLocale(this);
		
		Intent intent = new Intent(this, LaunchActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    	
    	startActivity(intent);
    	
	}    
    
}
