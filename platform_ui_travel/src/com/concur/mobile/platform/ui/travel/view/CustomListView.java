package com.concur.mobile.platform.ui.travel.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by tejoa on 21/07/2015.
 * CustomListView with out default scrolling
 */
public class CustomListView extends ListView {

    private android.view.ViewGroup.LayoutParams params;
    private int old_count = 0;

    public CustomListView(Context context) {
        super(context);
    }

    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getCount() != old_count) {
            old_count = getCount();

            params = getLayoutParams();
            params.height = getCount() * (old_count > 0 ? getChildAt(0).getHeight() : 0) + 20;
            setLayoutParams(params);
        }

        super.onDraw(canvas);
    }

}
