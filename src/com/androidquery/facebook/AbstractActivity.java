package com.androidquery.facebook;

import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.auth.FacebookHandle;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.facebook.enums.FeedMode;
import com.androidquery.facebook.util.PrefUtility;
import com.androidquery.util.AQUtility;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

public class AbstractActivity extends FragmentActivity{

	protected AQuery aq;
	protected FacebookHandle handle;
	
	@Override
    public void onCreate(Bundle savedInstanceState){
				
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminate(true);
        
        aq = new AQuery(this);
        setupHandle();
        
        
    }
	
	private static String APP_ID = "251003261612555";
	private static String PERMISSIONS = "read_stream,publish_stream";
	
	private void setupHandle(){
		
		handle = new FacebookHandle(this, APP_ID, PERMISSIONS);
		handle.setLoadingMessage("Connecting Facebook");
		//handle.unauth();
		
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	try{
    	
	        MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.menu, menu);
	        
	        
	        /*
	        if(PrefUtility.isCustomer()){
	        	menu.removeItem(R.id.d_settings);
	        	menu.removeItem(R.id.debug);
	        }
	        
	        
	        */
    	}catch(Exception e){
    		AQUtility.report(e);
    	}
        
        return true;
    }
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	
    	try{
    	
	        MenuItem mi = menu.findItem(R.id.mode);
	        
	        FeedMode mode = getMode();
	        
	        if(FeedMode.NEWS.equals(mode)){
	        	mi.setTitle(FeedMode.WALL.getDisplay());
	        }else{
	        	mi.setTitle(FeedMode.NEWS.getDisplay());
	        }
    	}catch(Exception e){
    		AQUtility.report(e);
    	}
        
        return true;
    }
	
	public void refresh(){
		
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
	        case R.id.logout:
	        	logout();
	            return true;      
	        default:
	            return false;
        }
    }
    
    private void logout(){
    	
    	handle.unauth();
    	
    	Intent intent = new Intent(this, FeedActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	intent.putExtra("logout", true);
    	
    	startActivity(intent);
    	
    }

    protected static final String NEWS_MODE = "news";
    protected static final String WALL_MODE = "wall";
    protected static final String MODE_KEY = "aq.facebook.mode";
    
    protected void modeChange(){
    	
    	FeedMode mode = getMode();
    	
    	if(FeedMode.NEWS.equals(mode)){
    		mode = FeedMode.WALL;
    	}else{
    		mode = FeedMode.NEWS;
    	}
    	
    	//PrefUtility.put(MODE_KEY, mode);
    	PrefUtility.putEnum(mode);
    	
    	modeChange(mode);
    	
    }
    
    protected FeedMode getMode(){
    	return PrefUtility.getEnum(FeedMode.class, FeedMode.NEWS);
    }
    
    protected void modeChange(FeedMode mode){
    	
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
            			AQUtility.cleanCacheAsync(AbstractActivity.this, 0, 0);            			
            			BitmapAjaxCallback.clearCache();
            			break;
            		case 1:
            			BitmapAjaxCallback.clearCache();         			
            			break;	
            		
            		default:
            	}
            	
            	
            }
        })
        .create();
    }    
    
    private void ajaxProfile(){
    	
    	String url = "https://graph.facebook.com/me";
    	
    	aq.auth(handle).ajax(url, JSONObject.class, 0, this, "profileCb");
    	
    }
    
    private static final String USER_NAME = "aq.fb.user.name"; 
    public void profileCb(String url, JSONObject jo, AjaxStatus status){
    	
    	AQUtility.debug(jo);
    	if(jo != null){
    		PrefUtility.put(USER_NAME, jo.optString("name", null));
    		updateTitle();
    	}
    	
    }
    
    protected String makeTitle(){
    	return getUserName("Simple FB");
    }
    
    protected void checkProfile(){
    	
    	if(isTaskRoot() && getUserName(null) == null){
            ajaxProfile();
        }
    	
    	updateTitle();
    	
    }
    
    protected void showToast(String message) {
      	
    	Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.show();
    	
    }
    
    
    protected void updateTitle(){
    	setTitle(makeTitle());
    }
    
    
    protected String getUserName(String fallback){
    	return PrefUtility.get(USER_NAME, fallback);    
    }
    
    private boolean progress;
    public void showProgress(boolean progress){
		
    	this.progress = progress;
		setProgressBarIndeterminateVisibility(progress);
		
	}
    
    public boolean isBusy(){
    	return progress;
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	aq.dismiss();
    }
    
}
