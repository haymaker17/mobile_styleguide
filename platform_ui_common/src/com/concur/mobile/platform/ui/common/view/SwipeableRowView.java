package com.concur.mobile.platform.ui.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;

/**
 * Created by OlivierB on 13/05/2015.
 */
public class SwipeableRowView<C extends ViewGroup, B extends ViewGroup> extends FrameLayout {

    private C contentView;
    private B buttonView;
    // --- Used to avoid infinite loop on dispatch
    private MotionEvent dispatchHistory;
    private boolean slideToLeft;

    public enum SwipeState {
        OPEN_LEFT,
        OPEN_RIGHT,
        CLOSE
    }

    private SwipeState state = SwipeState.CLOSE;

    public SwipeableRowView(Context context, C contentView, B buttonView, boolean slideToLeft) {
        super(context);
        init(context, contentView, buttonView, slideToLeft);
    }

    public SwipeableRowView(Context context, C contentView, B buttonView, boolean slideToLeft, AttributeSet attrs) {
        super(context, attrs);
        init(context, contentView, buttonView, slideToLeft);
    }

    private void init(Context context, C contentView, B buttonView, boolean slideToLeft) {
        this.slideToLeft = slideToLeft;
        setLayoutParams(new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        this.contentView = contentView;
        this.buttonView = buttonView;
        if (slideToLeft) {
            contentView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            addView(contentView);
            buttonView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
            addView(buttonView);
            buttonView.setX(context.getResources().getDisplayMetrics().widthPixels);
        } else {
            buttonView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
            addView(buttonView);
            contentView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            addView(contentView);
            // --- force calculation of the view width before render
            buttonView.measure(context.getResources().getDisplayMetrics().widthPixels,
                    context.getResources().getDisplayMetrics().heightPixels);
            // --- place the view on the left of the screen
            buttonView.setX(-buttonView.getMeasuredWidth());
        }
    }

    public B getButtonView() {
        return buttonView;
    }

    public void applyMargin(int x) {
        contentView.layout(x, contentView.getTop(), contentView.getWidth() + x, getMeasuredHeight());
        buttonView.layout(x, buttonView.getTop(), buttonView.getWidth() + x, buttonView.getBottom());
        if (x != 0) {
            state = SwipeState.OPEN_RIGHT;
        } else {
            state = SwipeState.CLOSE;
        }
    }

    public boolean isOpen() {
        return contentView.getX() != 0;
    }

    public float getXPos() {
        return contentView.getX();
    }

    public int getMaxRange() {
        return buttonView.getMeasuredWidth();
    }

    /**
     * Enables RowSwipeGestureListener to interact with the button's view (to handle both swipe & tap on it)
     *
     * @param ev
     * @return
     */
    @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (state != SwipeState.CLOSE && (dispatchHistory == null || !dispatchHistory.equals(ev))) {
            // --- we only intercept & dispatch tap on button view
            if (state == SwipeState.OPEN_RIGHT && ev.getRawX() > buttonView.getX()
                    || state == SwipeState.OPEN_LEFT && ev.getRawX() < buttonView.getX()) {
                dispatchHistory = ev;
                return ((ListView) this.getParent()).dispatchTouchEvent(ev);
            }
        } else {
            // --- dispatch ongoing: clean history and do nothing
            dispatchHistory = null;
        }
        return true;
    }
}
