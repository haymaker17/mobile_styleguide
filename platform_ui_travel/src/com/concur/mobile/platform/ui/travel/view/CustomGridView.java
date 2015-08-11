package com.concur.mobile.platform.ui.travel.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridView;
import android.widget.LinearLayout;
import com.concur.mobile.platform.ui.travel.util.ViewUtil;

/**
 * Created by tejoa on 21/07/2015.
 * CustomGridView with out default scrolling
 */
public class CustomGridView extends GridView {

    private float lastMotionY;

    public CustomGridView(Context context) {
        super(context);
    }

    public CustomGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        if (mode == MeasureSpec.UNSPECIFIED) {
            int height = getLayoutParams().height;
            if (height > 0)
                setMeasuredDimension(getMeasuredWidth(), height);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        float dy = y - lastMotionY;
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            lastMotionY = y;
            break;
        case MotionEvent.ACTION_MOVE:
            if (ViewUtil.canScroll(this, false, (int) dy, (int) x, (int) y)) {
                lastMotionY = y;
                return false;
            }
            break;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public void drawableStateChanged() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                View lastChild = getChildAt(getChildCount() - 1);
                if (lastChild != null) {
                    int height = lastChild.getBottom(); //Math.max(, getColumnWidth());
                    float child = getAdapter().getCount();
                    float col = getNumColumns();
                    int rows = (int) Math.ceil(child / col);
                    if (rows > 2) {
                        height = (int) (((rows + 1) * getColumnWidth() * 0.6 + 20) + (getHorizontalSpacing() * rows
                                - 1));
                    } else {
                        height = (int) (((rows + 1) * getColumnWidth() * 0.7) + (getHorizontalSpacing() * rows - 1));
                    }
                    setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height));

                }
            }
        });
    }

}
