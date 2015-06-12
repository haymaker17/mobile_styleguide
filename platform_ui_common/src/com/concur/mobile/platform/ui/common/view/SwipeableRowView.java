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

    /**
     * Moves child views of this layout to x position
     *
     * @param x
     */
    public void applyPosition(int x) {
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

    /**
     * @return current x position set
     */
    public float getXPos() {
        return contentView.getX();
    }

    /**
     * @return max swipeable range
     */
    public int getMaxRange() {
        return buttonView.getMeasuredWidth();
    }

    /**
     * Enables RowSwipeGestureListener to interact with the button's view (to handle both swipe & tap on it)
     *
     * @param event
     * @return whether event was handled or not
     */
    @Override public boolean onInterceptTouchEvent(MotionEvent event) {
        if (state != SwipeState.CLOSE && (dispatchHistory == null || !dispatchHistory.equals(event))) {
            // --- we only intercept & dispatch tap on button view
            if (state == SwipeState.OPEN_RIGHT && event.getRawX() > buttonView.getX()
                    || state == SwipeState.OPEN_LEFT && event.getRawX() < buttonView.getX()) {
                dispatchHistory = event;
                return ((ListView) this.getParent()).dispatchTouchEvent(event);
            }
        } else {
            // --- dispatch ongoing: clean history and do nothing
            dispatchHistory = null;
        }
        return true;
    }
}
