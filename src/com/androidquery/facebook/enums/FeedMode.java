package com.androidquery.facebook.enums;


public enum FeedMode {
	
	WALL("Wall", "https://graph.facebook.com/me/feed"),
	NEWS("News", "https://graph.facebook.com/me/home")
	;
	
	private String url;
	private String display;
	
	private FeedMode(String display, String url){
		this.display = display;
		this.url = url;
	}
	
	public String getUrl(){
		return url;
	}
	
	public String getDisplay(){
		return display;
	}
	
}
