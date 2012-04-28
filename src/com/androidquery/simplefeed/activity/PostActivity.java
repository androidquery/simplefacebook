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



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.simplefeed.R;
import com.androidquery.simplefeed.base.Constants;
import com.androidquery.simplefeed.base.MenuActivity;
import com.androidquery.simplefeed.data.Entity;
import com.androidquery.simplefeed.data.Entry;
import com.androidquery.simplefeed.data.FeedItem;
import com.androidquery.simplefeed.data.Place;
import com.androidquery.simplefeed.util.AppUtility;
import com.androidquery.simplefeed.util.FormatUtility;
import com.androidquery.simplefeed.util.PrefUtility;
import com.androidquery.util.AQUtility;

public class PostActivity extends MenuActivity{

	private Entity entity;
	private FeedItem item;
	private File photo;
	private Bitmap bm;
	private Location location;
	private Place place;
	private List<Entity> tags;
	private String title;
	
	@Override
	protected void init(Bundle state) {
		
		Intent intent = getIntent();
		
		String message = "";
		
		
		photo = (File) intent.getSerializableExtra("photo");
		message = intent.getStringExtra("message");
		
		
		if(state != null){
			if(photo == null){
				photo = (File) state.getSerializable("photo");
			}
			if(message == null){
				message = state.getString("message");
			}
			location = state.getParcelable("location");
			place = (Place) state.getSerializable("place");
			tags = (List<Entity>) state.getSerializable("tags");
		}
		
		
		
		entity = (Entity) intent.getSerializableExtra("entity");
		item = (FeedItem) intent.getSerializableExtra("item");
		
		initView(message);
	
		albumCheck(0);
	}
	
	private void initView(String message){
		
		aq.id(R.id.button_send).clicked(this, "sendClicked");
		aq.id(R.id.button_photo).clicked(this, "photoClicked");
		aq.id(R.id.button_checkin).clicked(this, "checkinClicked");
		aq.id(R.id.button_gallery).clicked(this, "galleryClicked");
		aq.id(R.id.button_tags).clicked(this, "tagsClicked");
		aq.id(R.id.button_remove_image).clicked(this, "removePhotoClicked");
		aq.id(R.id.button_remove_place).clicked(this, "removePlaceClicked");
		aq.id(R.id.button_remove_tags).clicked(this, "removeTagsClicked");
		
		
		String name = AppUtility.getUserName();
		String name2 = null;
		
		String hint = getString(R.string.comment);
		
		if(item != null){
		
			name2 = item.getFrom().getName();
			disableAction();
		}else if(entity != null){
			
			String id = entity.getId();
			if(id == null){
				name2 = getString(R.string.wall);
				hint = getString(R.string.status_update);
			}else{
				name2 = entity.getName();
				disableAction();
			}
			
			
		}
		

		if(name2 == null) name2 = name;
		title = name + " > " + name2;
		
		updateTitle(System.currentTimeMillis());
		
		attachPhoto(photo);
		attachPlace(place);
		attachFriends(tags);
		
		refreshButtons();
		
		aq.id(R.id.edit_input).text(message).getTextView().setHint(hint);
	}

	
	private void disableAction(){
		aq.id(R.id.button_gallery).gone();
		aq.id(R.id.button_photo).gone();
		aq.id(R.id.button_checkin).gone();
		aq.id(R.id.button_tags).gone();
	}
	
	
	@Override
	protected int getContainerView() {
		return R.layout.activity_post;
	}
	 
	
	public final static int REQUEST = 12;
	public static void start(Activity act, Entity entity){
		
		Intent intent = new Intent(act, PostActivity.class);
		intent.putExtra("entity", entity);
		
		act.startActivityForResult(intent, REQUEST);
		
	}
	
	public static void start(Activity act, FeedItem item){
		
		Intent intent = new Intent(act, PostActivity.class);
		intent.putExtra("item", item);
		
		act.startActivityForResult(intent, REQUEST);
		
	}

	public void tagsClicked(View view){
		
		AQUtility.debug("tags clicked");
		
		doneInput();
		
		FriendsActivity.start(this, true);
		
	}
	
	public void sendClicked(View view){
		
		String message = getMessage();
		
		if(item != null){
			
			AQUtility.debug(item.getObjectId());
			
			//comment(item.getItemId(), message);
			comment(item.getActionId(), message);
			
		}else if(entity != null){
			
			String id = entity.getId();
			if(id == null) id = "me";
			
			if(bm != null && photo != null){
				photoPost();
			}else if(location != null){
				checkinPost(message, place, location);
			}else{
				wallPost(id, message);
			}
			
		}
		
	}
	
	private String getMessage(){
		
		String message = aq.id(R.id.edit_input).getEditable().toString();
		return message;
	}
	
	private void checkinPost(String message, Place place, Location loc){
		
		
		String url = "https://graph.facebook.com/me/checkins";
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		params.put("message", message);
		putLocation(params, place, loc);
		putTags(params, tags);
		
		ProgressDialog dialog = makeProgressDialog(getString(R.string.sending));
		
		aq.auth(handle).progress(dialog).ajax(url, params, JSONObject.class, this, "checkinCb");
		
		//progressDialog(true, getString(R.string.sending));
		
		
		
	}
	
	private void putTags(Map<String, Object> params, List<Entity> tags){
	
		if(tags == null || tags.size() == 0) return;
		
		List<String> ids = new ArrayList<String>();
		
		for(Entity tag: tags){
			
			ids.add(tag.getId());
			
		}
		
		String value = TextUtils.join(",", ids);
		
		AQUtility.debug("tags", value);
		params.put("tags", value);
		
	}
	
	private void putWallTags(Map<String, Object> params, List<Entity> tags){
		
		if(tags == null || tags.size() == 0) return;
		
		
		
	}	
	
	
	private void putPhotoTags(Map<String, Object> params, List<Entity> tags){
		
		if(tags == null || tags.size() == 0) return;
		
		try{
		
			JSONArray ja = new JSONArray();
			
			for(Entity tag: tags){
				JSONObject jo = new JSONObject();
				jo.putOpt("tag_uid", tag.getId());
				jo.putOpt("x", 0);
				jo.putOpt("y", 0);
				ja.put(jo);
			}
			
			AQUtility.debug("tags", ja);
			
			params.put("tags", ja.toString());
		
		}catch(Exception e){
			AQUtility.report(e);
		}
		
	}	
	
	private void putLocation(Map<String, Object> params, Place place, Location loc){
		
		if(place == null || loc == null) return;
		
		try{
			JSONObject cord = new JSONObject();
			cord.put("latitude", loc.getLatitude());
			cord.put("longitude", loc.getLongitude());
			
			params.put("place", place.getId());
			params.put("coordinates", cord);
		}catch(Exception e){
			AQUtility.report(e);
		}
	
	}
	
	
	public void checkinCb(String url, JSONObject jo, AjaxStatus status){
		
		AQUtility.debug("checkin", jo);
		
		//progressDialog(false, "");
		
		if(jo != null && jo.has("id")){
			finish(R.string.done, true, null, getMessage());
		}else if(status.getCode() == 403){
			showToast(getString(R.string.no_permission));
		}
	}
	
	private void wallPost(String id, String message){
		
		String url = "https://graph.facebook.com/" + id + "/feed";
		
		
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("message", message);
		
		putWallTags(params, tags);
		
		AQUtility.debug("wall", params);
		
		ProgressDialog dialog = makeProgressDialog(getString(R.string.sending));
		
		aq.auth(handle).progress(dialog).ajax(url, params, JSONObject.class, this, "wallCb");
		
		//progressDialog(true, getString(R.string.sending));
		
	}
	

	
	
	
	public void wallCb(String url, JSONObject jo, AjaxStatus status){
		
		//progressDialog(false, "");
		
		
		AQUtility.debug(jo);
		
		if(jo != null && jo.has("id")){
			finish(R.string.done, true, null, getMessage());
		}else if(status.getCode() == 403){
			showToast(getString(R.string.no_permission));
		}
		
	}
	
	private void finish(int toast, boolean refresh, String itemId, String message){
		
		doneInput();
		
		Intent data = new Intent();
		data.putExtra("toast", getString(toast));
		data.putExtra("refresh", refresh);
		data.putExtra("itemId", itemId);
		data.putExtra("message", message);
		
		//setResult(RESULT_OK, data);
		
		aq.result(this, RESULT_OK, data);
		
		finish();
	}
	

	
	private File makePhotoFile(){
		
		File result = null;
		
		try{
		
			String folder = Environment.getExternalStorageDirectory() + "/simplefb";		
			File file = new File(folder);		
			file.mkdirs();
			
			AQUtility.debug("file", file);
			
			result = new File(file, "photo.jpg");
		}catch(Exception e){
			AQUtility.report(e);
		}
		
		
		return result;
	}
	
	public void photoClicked(View view){
		
		File file = makePhotoFile();
		
		if(file == null) return;
		
	    Uri outputFileUri = Uri.fromFile(file);

	    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
	    
	    startActivityForResult(intent, Constants.ACTIVITY_CAMERA);
		
	}	
	
	protected boolean needProgress(){
		return false;
	}
	
	public void checkinClicked(View view){
		
		AQUtility.debug("checkinClicked");
		
		
		doneInput();
		
		PlaceActivity.start(this);
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle bundle) {
		
		bundle.putSerializable("photo", photo);
		bundle.putString("message", getMessage());
		bundle.putParcelable("location", location);
		bundle.putSerializable("place", place);
		bundle.putSerializable("tags", (Serializable) tags);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		
		AQUtility.debug("p request", requestCode + ":" + resultCode);
		
	    switch(requestCode){
	    	case Constants.ACTIVITY_CAMERA:
	    		if(resultCode == RESULT_OK){
	    			attachPhoto(makePhotoFile());
	    		}
	    		break;
	    	case Constants.ACTIVITY_GALLERY:	
		    	if(resultCode == RESULT_OK){
		    		handleImage(data);
		        }
		    	break;
	    	case Constants.ACTIVITY_CHECKIN:	
		    	if(resultCode == RESULT_OK){
		    		handleCheckin(data);
		        }
		    	break;	
	    	case Constants.ACTIVITY_FRIENDS:	
		    	if(resultCode == RESULT_OK){
		    		handleFriends(data);
		        }
		    	break;	
	    	default:
	    		super.onActivityResult(requestCode, resultCode, data);
	    		break;
	    }
	}
	
	private void attachPhoto(File file){
		
		if(file == null || !file.exists() || file.length() < 10){
			aq.id(R.id.image_box).gone();
			return;
		}
		
		AQUtility.debug("photo", file.length());
		
		
		sendEnable(false);
		BitmapAjaxCallback cb = new BitmapAjaxCallback(){
			
			@Override
			protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
				
				sendEnable(true);
				
				if(photo != null && bm != null){
					iv.setImageBitmap(bm);
					String dim = bm.getWidth() + "x" + bm.getHeight();
					aq.id(R.id.text_dim).text(dim);
					String length = FormatUtility.scientificShort(photo.length()) + "b";
					aq.id(R.id.text_size).text(length);
					PostActivity.this.bm = bm;
				}
				
			}
			
		};
		
		cb.file(file).targetWidth(720);//.targetDim(false);
		
		aq.id(R.id.image_photo).image(cb);
		
		clear();
		
		this.photo = file;
		
		refreshButtons();
	}
	
	
	private void sendEnable(boolean enabled){
		aq.id(R.id.button_send).enabled(enabled);
	}
	
	public void removePhotoClicked(View view){
		
		aq.id(R.id.image_box).gone();
		clear();
		tags = null;
		refreshButtons();
	}
	
	public void removePlaceClicked(View view){
		
		aq.id(R.id.place_box).gone();
		clear();
		tags = null;
		refreshButtons();
	}
	
	public void removeTagsClicked(View view){
		
		aq.id(R.id.tags_box).gone();
		tags = null;
		refreshButtons();
	}
	
    @Override
    protected String makeTitle(long time){
    	
    	if(title != null){ 
    		return title;
    	}
    	
    	return super.makeTitle(time);
    	
    }
    
    private byte[] toBytes(Bitmap bm){
    	
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	bm.compress(CompressFormat.JPEG, 90, baos);
    	return baos.toByteArray();
    	
    }
	
    private Options getSize(File file){
    	
    	BitmapFactory.Options options = null;
    	
		options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    	
        return options;
    }
    
    
	public void galleryClicked(View view){
		
		Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);		             
        startActivityForResult(Intent.createChooser(intent, getString(R.string.photo)), Constants.ACTIVITY_GALLERY);
        
		
	}
	
    private void handleImage(Intent data){
    	
    	Uri uri = data.getData();
        String path = getImagePath(uri);  
        
        if(path == null){
        	showToast(getString(R.string.no_sd));        	
        	return;
        }
        
        File file = new File(path);
        attachPhoto(file);
       
        
    }
    
    private void handleFriends(Intent data){
    	
    	List<Entity> tags = (List<Entity>) data.getSerializableExtra("selected");
    	if(tags == null || tags.size() == 0) return;
    	
    	
    	AQUtility.debug("friends", tags);
    	
    	this.tags = tags;
    	
    	attachFriends(tags);
    	refreshButtons();
    }
    
    private void attachFriends(List<Entity> tags){
    	
    	if(tags != null && tags.size() > 0){  	
    		String meta = tags.get(0).getName();
    		if(tags.size() > 0){
    			meta += " (" + tags.size() + " " + getString(R.string.n_friends) + ")";
    		}
    		aq.id(R.id.text_tags).text(meta);
    	}
    }
    
    private void handleCheckin(Intent data){
    	
    	Location loc = (Location) data.getParcelableExtra("location");
        Place place = (Place) data.getSerializableExtra("place");
    	
        AQUtility.debug(loc, item);
        
        if(loc != null && place != null){
        	
        	clear();
        	
        	this.location = loc;
        	this.place = place;
    
        	attachPlace(place);
        	
        	refreshButtons();
        }
        
    }
    
    private void attachPlace(Place place){
    	
    	if(place != null){  	
    		aq.id(R.id.text_place).text(place.getName());
    		aq.id(R.id.image_place).image(place.getTb());
    	}
    }
    
    private void clear(){
    	
    	photo = null;
    	bm = null;
    	location = null;
    	place = null;
    }
    
    private void refreshButtons(){
    	
    	if(photo != null){
    		aq.id(R.id.button_photo).enabled(true);
    		aq.id(R.id.button_gallery).enabled(true);
    		aq.id(R.id.button_checkin).enabled(false);
    		aq.id(R.id.button_tags).enabled(true);
    		aq.id(R.id.image_box).visible();
    		aq.id(R.id.place_box).gone();
    	}else if(place != null){
    		aq.id(R.id.button_photo).enabled(false);
    		aq.id(R.id.button_gallery).enabled(false);
    		aq.id(R.id.button_checkin).enabled(true);
    		aq.id(R.id.button_tags).enabled(true);
    		aq.id(R.id.image_box).gone();
    		aq.id(R.id.place_box).visible();
    	}else{
    		aq.id(R.id.button_photo).enabled(true);
    		aq.id(R.id.button_gallery).enabled(true);
    		aq.id(R.id.button_checkin).enabled(true);
    		aq.id(R.id.button_tags).enabled(false);
    		aq.id(R.id.image_box).gone();
    		aq.id(R.id.place_box).gone();
    	}
    	
    	if(tags != null){
    		aq.id(R.id.tags_box).visible();
    	}else{
    		aq.id(R.id.tags_box).gone();
    	}
    	
    }
	
	private void doneInput(){	
		
		EditText edit = aq.id(R.id.edit_input).getEditText();		
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
	}
    
    
    
    private String getImagePath(Uri uri) {
    	
        String[] projection = { MediaStore.Images.Media.DATA };
        
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        
        if(cursor == null) return null;
        
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        
        String path = null;
        
        if(cursor.moveToFirst()){
        	path = cursor.getString(column_index);
        }
        
        return path;
    }
    
    

	
	private void comment(String id, String message){
		
		String url = "https://graph.facebook.com/" + id + "/comments";
		
		//progressDialog(true, getString(R.string.sending));
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("message", message);
		
		ProgressDialog dialog = makeProgressDialog(getString(R.string.sending));
		
		
		aq.auth(handle).progress(dialog).ajax(url, params, JSONObject.class, this, "commentCb");
		
		
		
	}
	
	public void commentCb(String url, JSONObject jo, AjaxStatus status){
		
		//progressDialog(false, "");
		
		
		AQUtility.debug(jo);
		
		if(jo != null && jo.has("id")){
			finish(R.string.done, false, item.getId(), getMessage());
		}else if(status.getCode() == 403){
			showToast(getString(R.string.no_permission));
		}
		
	}
	
	private void albumCheck(int expire){
		
		
		String url = "https://graph.facebook.com/me/albums?locale=" + locale;
		
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.weakHandler(this, "albumCb");
		
		aq.auth(handle).ajax(url, JSONObject.class, expire, cb);
		
		
	}
	
	public void albumCb(String url, JSONObject jo, AjaxStatus status){
		
		AQUtility.debug(jo);
		
		
		List<Entry> albums = new ArrayList<Entry>();
		
		albums.add(new Entry(getString(R.string.app_name), "me"));
		
		int position = 0;
		
		if(jo != null){
			
			JSONArray ja = jo.optJSONArray("data");
			for(int i = 0; i < ja.length(); i++){
				JSONObject album = ja.optJSONObject(i);
				String name = album.optString("name");
				String id = album.optString("id");
				if("mobile".equals(album.optString("type", "me"))){
					position = i + 1;
				}
				albums.add(new Entry(name, id));
			}
			
			if(status.expired(HALF_DAY)){				
				albumCheck(-1);				
			}
			
		}
		
		
		ArrayAdapter<Entry> adapter = new ArrayAdapter<Entry>(this, android.R.layout.simple_spinner_item, albums);
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    
		aq.id(R.id.spinner_album).adapter(adapter).setSelection(position).visible();
		
	    
	}	
	
	private String getSelected(){
		
		String result = "me";
		
		Entry entry = (Entry) aq.id(R.id.spinner_album).getSelectedItem();
		if(entry != null){
			result = entry.getValue();
		}
		
		return result;
	}
	
	private void photoPost(){
		
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
			
				try{
					String id = getSelected();
					photoPost(id, getMessage(), photo);
				}catch(Exception e){
					AQUtility.report(e);
					failed();
				}
				return null;
			}
		};
		
		task.execute();
		
		finish(R.string.pending_upload, false, null, null);
		
	}
	
	
	private void photoPost(String id, String message, File file){
		
		String url = "https://graph.facebook.com/" + id + "/photos";
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("message", message);
		
		Options size = getSize(photo);
		if(bm.getWidth() == size.outWidth){
			AQUtility.debug("use file");
			params.put("source", file);
		}else{
			AQUtility.debug("use byte[]");
			params.put("source", toBytes(bm));
		}
		
		
		putLocation(params, place, location);
		
		putPhotoTags(params, tags);
		
		AQUtility.debug("params", params);
		
		
		AQuery aq = new AQuery(getApplicationContext());
		aq.auth(handle).ajax(url, params, JSONObject.class, this, "photoCb");
		
		
	}
	

	
	
	public void photoCb(String url, JSONObject jo, AjaxStatus status){
	
		AQUtility.debug(jo);
		
		if(jo != null && jo.has("id")){
			
			AQuery aq = new AQuery(getApplicationContext());
			String url2 = "https://graph.facebook.com/" + jo.optString("id");
			aq.auth(handle).ajax(url2, JSONObject.class, this, "photoDetailCb");
			
			
		}else{		
			failed();
		}
		
	}
	
	public void photoDetailCb(String url, JSONObject jo, AjaxStatus status){
		AQUtility.debug(jo);
		
		if(jo != null){
			
			String tb = jo.optString("picture");
			FeedItem item = new FeedItem(jo);
			uploaded(tb, item);
		}else{		
			failed();
		}
		
	}
	
	private void failed(){
		Intent intent = new Intent(this, PostActivity.class);
		intent.putExtra("photo", photo);
		intent.putExtra("message", getMessage());
		intent.putExtra("entity", entity);
		intent.putExtra("item", item);
		
		AQUtility.debug("failed input", photo + ":" + getMessage());
		AQUtility.debug("failed input2", intent.getSerializableExtra("photo") + ":" + intent.getStringExtra("message"));
		String failed = getString(R.string.upload_failed);
		notify(failed, getString(R.string.photo_upload), failed, intent);	
	}
	
	private void uploaded(String tb, FeedItem item){
		
		Intent notificationIntent = new Intent(this, ImageActivity.class);
		notificationIntent.putExtra("url", tb);
		notificationIntent.putExtra("item", item);
		notificationIntent.putExtra("album", false);
		String completed = getString(R.string.upload_completed);
		notify(completed, getString(R.string.photo_upload), completed, notificationIntent);
	}
	
	
	private void notify(String ticker, String title, String message, Intent intent){
		
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		
		int icon = R.drawable.launcher;
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, ticker, when);
		
		Context context = getApplicationContext();
		CharSequence contentText = message;
		
		int id = getNotifyId();
		PendingIntent contentIntent = PendingIntent.getActivity(this, id, intent, 0);

		notification.setLatestEventInfo(context, title, contentText, contentIntent);
		
		mNotificationManager.cancelAll();
		
		AQUtility.debug("notify id", id);
		mNotificationManager.notify(id, notification);
		
	}
	
	private int getNotifyId(){
		
		long n = PrefUtility.getLong(Constants.PREF_NID, 1000L);
		
		n++;		
		PrefUtility.put(Constants.PREF_NID, n);
		
		return (int) Math.abs(n % 10000);
		
	}
	
}
