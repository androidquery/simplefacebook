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
package com.androidquery.simplefeed.data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.androidquery.util.AQUtility;

public abstract class PageAdapter<T> extends BaseAdapter{

	private int page;
	private String nextUrl;
	private List<T> items;
	private boolean loading;
	private boolean loadable;
	
	public PageAdapter() {
		items = new ArrayList<T>();
	}

	public void prepend(List<T> adds, String nextUrl){
		
		if(adds == null || items.size() == 0){
			add(adds, nextUrl);
			return;
		}
		
		LinkedHashSet<T> set = new LinkedHashSet<T>(adds);
		
		for(T item: items){
			if(!set.contains(item)){
				set.add(item);
			}
		}
		
		items = new ArrayList<T>(set);
		notifyDataSetChanged();
	}
	
	public void add(List<T> adds, String nextUrl){
		
		if(adds == null) return;
		
		items.addAll(adds);
		
		page++;
		this.nextUrl = nextUrl;
		
		notifyDataSetChanged();
		
	}
	
	public void add(int pos, T add){
		
		items.add(pos, add);
		
		notifyDataSetChanged();
		
	}
	
	public void add(T add){
		
		items.add(add);
		
		notifyDataSetChanged();
		
	}
	
	public boolean remove(T item){
		
		boolean result = items.remove(item);		
		notifyDataSetChanged();
		return result;
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
	public T getItem(int pos) {
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
		
		if(this.loading != loading){
			this.loading = loading;
			refreshLoading();
		}
		
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
	

	private void refreshLoading(){
		
		if(loadingView == null) return;
		
		if(loading){
			loadingView.setVisibility(View.VISIBLE);
		}else{
			loadingView.setVisibility(View.GONE);
		}
		
		notifyDataSetChanged();
		
	}
	
	private View loadingView;
	protected View getLoadingView(LayoutInflater li, int id){	
		
		if(loadingView == null){
			loadingView = li.inflate(id, null);
		}
		refreshLoading();
		return loadingView;
	}
	
	private static View emptyView;
	public static View getEmptyView(View parent){
		if(emptyView == null){
        	emptyView = new View(parent.getContext());
        }
		return emptyView;
	}
	
	@Override
	public final View getView(int position, View convertView, ViewGroup parent){
		
		View view = null;
		
		try{
			//AQUtility.time("render");
			view = render(position, convertView, parent);
			//AQUtility.timeEnd("render",0 );
		}catch(Exception e){
			AQUtility.report(e);
		}
		
		if(view == null) view = getEmptyView(parent);
		
		return view;
	}
	
	protected abstract View render(int position, View convertView, ViewGroup parent);
	
}
