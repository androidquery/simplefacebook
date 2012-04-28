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
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;



/**
 * Example of how to write a custom subclass of View. LabelView
 * is used to draw simple text views. Note that it does not handle
 * styled text or right-to-left writing systems.
 *
 */
public class SlimTextView extends TextView {
	
	private LayoutString ls;

    public SlimTextView(Context context) {
    	
        super(context);
        
        init();
    }

    
    public SlimTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public SlimTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    private void init() {
    	
    	TextPaint paint = getPaint();
    	paint.setColor(getTextColors().getDefaultColor());
    	paint.setTextSize(getTextSize());
    	
    }
    
    public void setLayoutText(LayoutString stl){
    	
    	if(ls == stl) return;
    	
    	checkLayout(getWidth());
    	
    	if(ls != null && stl != null && (ls.height != stl.height || ls.width != stl.width)){
    		requestLayout();
    	}
    	
    	invalidate();
    	
    	ls = stl;
    	
    }

    private void fillLayout(LayoutString stl, int width){
    	
    	//AQUtility.debug(this.getTextColors().getDefaultColor() + ":" + getPaint().getColor());
    	
    	TextPaint paint = getPaint();
    	//paint.setColor(getTextColors().getDefaultColor());
    	
    	//int width = measureWidth(widthMeasureSpec, str);
    	Layout layout =  new StaticLayout(stl.str, paint, width, Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
    	int count = layout.getLineCount();
    	//AQUtility.debug("count", count);
    	
    	int height = layout.getLineTop(count);
    	//AQUtility.debug("dim", width + "x" + height);
    	
    	stl.width = width;
    	stl.height = height;
    	stl.layout = layout;
    	
    	//AQUtility.debug("fill layout");
    	
    }
    


    private void checkLayout(int width){
    	
    	if(width != 0 && ls.width != width){
    		fillLayout(ls, width);
    	}
    	
    }
    

    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        
    	
    	if(ls == null){
    		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    		return;
    	}
        
    	//AQUtility.debug("on measure");
    	
    	
    	checkLayout(measureWidth(widthMeasureSpec, ls.str));
    	setMeasuredDimension(ls.width, ls.height);
    	
    	
    		
    }

    
    private int measureWidth(int measureSpec, String str) {
    	
    	
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
            //AQUtility.debug(result, str);
        }        
        else {
            // Measure the text
            result = (int) getPaint().measureText(str) + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }

        
        return result;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        
    	if(ls == null){
    		super.onDraw(canvas);
    	}else{
    		checkLayout(getWidth());
    		ls.layout.draw(canvas);
    	}
    	
    }
}
