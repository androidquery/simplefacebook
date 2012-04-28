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

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class SafeWebView extends WebView{

    public SafeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
    }
    
    public SafeWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
       
    }
    
    @Override
    public void destroy(){
    	try{
    		getSettings().setBuiltInZoomControls(true);
    		super.destroy();
    	}catch(Throwable e){
    	}
    }
}
