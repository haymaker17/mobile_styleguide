package com.concur.mobile.core.expense.travelallowance.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by D049515 on 21.10.2015.
 */
public class CustomViewPager extends ViewPager {
    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isEnabled()) {
            return super.onTouchEvent(ev);
        } else {
            return false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isEnabled()) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }
}
