package com.iam360.iam360.util;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Mariel on 8/12/2016.
 */
public class MyViewPager extends ViewPager {

    private boolean enabled = false;

    public MyViewPager(Context context) {
        super(context);
        this.enabled = true;
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.enabled) return super.onInterceptTouchEvent(ev);
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (this.enabled) return super.onTouchEvent(ev);
        return false;
    }

    public void setPageEnable(boolean enable) {
        this.enabled = enable;
    }
}
