package com.concur.mobile.gov.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

/**
 * This class is extension of vertical scroll view.
 * This class required to find out scroll position of the scroll-view and when it will reach to bottom.
 * 
 * @author sunill
 * 
 */
public class GovScrollViewExtension extends ScrollView {

    private IScrollViewListener listener;

    public GovScrollViewExtension(Context context) {
        super(context);
    }

    public GovScrollViewExtension(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GovScrollViewExtension(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnScrollViewListener(IScrollViewListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onScrollChanged(int horizontalScroll, int verticalScroll, int oldXScroll, int oldYScroll)
    {
        /*
         * As we know ScrollView contains only one child. So get that child. and then calculate the bottom of the child.
         * then calculate screen height, as we are doing vertical scroll calculate or get 'Y' position.
         * Once you get all of this you can calculate the difference as shown bellow. If difference is 0 means we have
         * already reached to bottom of the scroll bar.
         */
        View view = (View) getChildAt(getChildCount() - 1);
        int diff = (view.getBottom() - (getHeight() + getScrollY()));
        // if diff is zero, then we've already reached to bottom of the scroll bar
        if (diff <= 3)
        {
            listener.onScrollChanged(horizontalScroll, verticalScroll, oldXScroll, oldYScroll);
        }

        super.onScrollChanged(horizontalScroll, verticalScroll, oldXScroll, oldYScroll);
    }
}