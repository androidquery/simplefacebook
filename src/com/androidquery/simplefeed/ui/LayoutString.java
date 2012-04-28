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
package com.androidquery.simplefeed.ui;

import android.text.Layout;

import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;

public class LayoutString implements CharSequence{

	Layout layout;
	int width;
	int height;
	String str;
	
	
	public LayoutString(String str){
		
		this.str = str;
	}

	@Override
	public char charAt(int index) {		
		return str.charAt(index);
	}

	@Override
	public int length(){
		return str.length();
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return str.subSequence(start, end);
	}
	
	@Override
	public String toString(){
		return str;
	}
	
	public int getLayer(){
		
		if(true || str.length() > 40){
			AQUtility.debug("soft");
			return AQuery.LAYER_TYPE_SOFTWARE;
		}else{

			AQUtility.debug("hard");
			return AQuery.LAYER_TYPE_HARDWARE;
		}
		
	}
	
}
