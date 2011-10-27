package com.androidquery.facebook.data;

import java.util.ArrayList;
import java.util.List;

import com.androidquery.facebook.MainApplication;
import com.androidquery.facebook.R;
import com.androidquery.util.AQUtility;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

public abstract class PageAdapter<T> extends BaseAdapter{

	private int page;
	private String nextUrl;
	private List<T> items;
	private boolean loading;
	private boolean loadable;
	
	public PageAdapter() {
		//super(context, layoutId);
		items = new ArrayList<T>();
	}

	public void add(List<T> adds, String nextUrl){
		
		items.addAll(adds);
		
		page++;
		this.nextUrl = nextUrl;
		
		notifyDataSetChanged();
		
	}
	
	public void add(int pos, T add){
		
		items.add(pos, add);
		
		notifyDataSetChanged();
		
	}
	
	public String getNext(){
		return nextUrl;
	}
	
	public int getPage(){
		return page;
	}
	
	public void clear(){
		items.clear();
		page = 0;
		nextUrl = null;
		
		notifyDataSetInvalidated();
	}

	@Override
	public int getCount() {		
		int count = items.size();
		if(count > 0 && loadable) count++;
		return count;
	}

	@Override
	public Object getItem(int pos) {
		if(pos >= items.size()){
			return null;
		}
		return items.get(pos);
	}

	@Override
	public long getItemId(int pos) {		
		return 0;
	}
	
	@Override
	public boolean isEmpty(){
		return items.size() == 0;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
		refreshLoading();
		
	}

	public boolean isLoading() {
		return loading;
	}

	public boolean isLoading(int position) {
		return loadable && position == items.size();
	}

	public void setLoadable(boolean loadable) {
		this.loadable = loadable;
	}

	public boolean isLoadable() {
		return loadable;
	}
	
	@Override
	public int getViewTypeCount(){
		if(loadable) return 2;
		return 1;
	}
	
	@Override
	public int getItemViewType(int position){
		
		if(isLoading(position)){
			return 1;
		}
		
		return 0;
	}
	
	private static LayoutInflater lf;
	private static LayoutInflater getInflater(){
		if(lf == null){
			Context context = MainApplication.getContext();
			lf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		return lf;
	}
	
	protected View inflate(View convertView, int id){
		
		if(convertView == null){
			convertView = getInflater().inflate(id ,null);
		}
		
		return convertView;
		
	}
	
	private void refreshLoading(){
		
		if(loadingView == null) return;
		
		if(loading){
			loadingView.setVisibility(View.VISIBLE);
		}else{
			loadingView.setVisibility(View.GONE);
		}
		
	}
	
	private View loadingView;
	protected View getLoadingView(View convertView, int id){	
		loadingView = inflate(convertView, id);	
		refreshLoading();
		return loadingView;
	}
}
