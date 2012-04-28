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
package com.androidquery.simplefeed.callback;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.simplefeed.util.DataUtility;
import com.androidquery.util.AQUtility;

public abstract class AbstractObjectAjaxCallback<T extends Serializable> extends AjaxCallback<T>{


	
	
	@Override
	public T fileGet(String url, File file, AjaxStatus status) {
		
		T result = null;
		
		try{		
			result = (T) DataUtility.toObject(Object.class, new FileInputStream(file));			
		}catch(Exception e){
			AQUtility.report(e);
		}
		
		return result;
	}
	
	
	
	@Override
	public void filePut(String url, T object, File file, byte[] data){
		
		if(object == null) return;
		
		byte[] od = DataUtility.toBytes(object);
		AQUtility.storeAsync(file, od, 0);
	}
	
	
	
}
