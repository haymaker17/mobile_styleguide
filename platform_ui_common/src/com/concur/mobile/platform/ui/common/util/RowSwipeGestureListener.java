package com.concur.mobile.platform.ui.common.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.concur.mobile.platform.ui.common.view.SwipeableRowView;

/**
 * Class to handle horizontal swipes over rows within a list.
 * Each row type MUST be SwipeableRowView
 * <p/>
 * Usage example:
 *
 * @see com.concur.mobile.core.request.activity.RequestListActivity#configureUI()
 * @see com.concur.mobile.core.request.adapter.SortedRequestListAdapter#getView(int, View, ViewGroup)
 * <p/>
 * Created by OlivierB on 11/05/2015.
 */
public abstract class RowSwipeGestureListener<T> extends GestureDetector.SimpleOnGestureListener {

    private static final String CLS_TAG = "SwipeGestureListener";
    private static final int SWIPE_MIN_DISTANCE = 30;
    private static final int SWIPE_DURATION = 200;

    private ListView listView;
    private boolean slideToLeft = true;

    public RowSwipeGestureListener(ListView listView) {
        super();
        this.listView = listView;
    }

    public RowSwipeGestureListener(ListView listView, boolean slideToLeft) {
        super();
        this.listView = listView;
        this.slideToLeft = slideToLeft;
    }

    /**
     * Handles tap on content layout
     *
     * @param item tapped row data
     * @return whether event was handled or not
     */
    public abstract boolean onRowTap(T item);

    /**
     * Handles tap on button(s) layout
     *
     * @param item tapped row data
     * @param view tapped view
     * @return whether event was handled or not
     */
    public abstract boolean onButtonTap(T item, View view);

    /**
     * Called before each swipe
     *
     * @param item tapped row data
     * @return whether we can swipe this row or not
     */
    public abstract boolean isRowSwipeable(T item);

    @Override public boolean onDown(MotionEvent e) {
        Log.d(CLS_TAG, "Gesture action: DOWN");
        return true;
    }

    @Override public boolean onSingleTapUp(MotionEvent e) {
        /** absolute position of the element taped in the list */
        final int position = getDataIndex(listView.pointToPosition(Math.round(e.getX()), Math.round(e.getY())));
        /** taped element's view */
        final SwipeableRowView rowView = (SwipeableRowView) getChildAt(listView, position);

        if (rowView != null) {
            /** taped element's data */
            final T row = (T) listView.getItemAtPosition(position);
            final int swipeMaxRange = rowView.getMaxRange();
            if (rowView.getXPos() != 0 && (!slideToLeft && e.getRawX() < swipeMaxRange || slideToLeft && e.getRawX() > (
                    rowView.getWidth() - swipeMaxRange))) {
                // --- tap over button(s) layout
                return onButtonTap(row, getChildByPos(rowView.getButtonView(), ((Float) e.getRawX()).intValue()));
            } else {
                // --- tap over content layout
                return onRowTap(row);
            }
        }
        Log.d(CLS_TAG, "Gesture action: SINGLE TAP");
        return true;
    }

    @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(CLS_TAG, "Gesture action: FLING");
        boolean hasXSwipe = false;
        if (e1 != null && e2 != null) {
            /** absolute position of the element taped in the list */
            final int position = getDataIndex(listView.pointToPosition(Math.round(e1.getX()), Math.round(e1.getY())));
            /** taped element's view */
            final SwipeableRowView rowView = (SwipeableRowView) getChildAt(listView, position);
            /** taped element's data */
            final T row = (T) listView.getItemAtPosition(position);
            if (rowView != null && isRowSwipeable(row)) {
                /** position of the row on device's screen in pixels */
                final int xPos = ((Float) rowView.getXPos()).intValue();
                final int swipeMaxRange = rowView.getMaxRange();

                // Reverting sign of deltas to make more sense (LtoR & TtoB are positive that way)
                final float deltaX = 0 - (e1.getX() - e2.getX());
                //final float deltaY = 0 - (e1.getY() - e2.getY());

                if (Math.abs(deltaX) > SWIPE_MIN_DISTANCE) {
                    if (rowView.isOpen()) {
                        if (slideToLeft && deltaX < 0 || !slideToLeft && deltaX > 0) {
                            // --- already opened - can't slide further that way
                            return false;
                        }
                        launchFlingAnimation(rowView, xPos, 0, -(int) velocityX);
                        hasXSwipe = true;
                    } else {
                        if (slideToLeft && deltaX > 0 || !slideToLeft && deltaX < 0) {
                            // --- already closed - can't slide further that way
                            return false;
                        }
                        launchFlingAnimation(rowView, xPos, deltaX < 0 ? -swipeMaxRange : swipeMaxRange,
                                -(int) velocityX);
                        hasXSwipe = true;
                    }
                }
            }
        }

        if (hasXSwipe) {
            // --- blocks list swipe (vertical) on row swipe (horizontal)
            return true;
        } else {
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    /**
     * Translate a relative row position (display) to an absolute position (data)
     *
     * @param viewIndex
     * @return dataIndex
     */
    private int getDataIndex(int viewIndex) {
        /** id of the first row displayed */
        final int firstPosition = listView.getFirstVisiblePosition() - listView.getHeaderViewsCount();
        // --- translated value of the position we want
        return viewIndex - firstPosition;
    }

    /**
     * Returns the listView item corresponding to dataIndex in the current display state
     *
     * @param listView
     * @param dataIndex
     * @return listView row
     */
    private View getChildAt(ListView listView, int dataIndex) {
        // Say, first visible position is 8, you want position 10, wantedChild will now be 2
        // So that means your view is child #2 in the ViewGroup:
        if (dataIndex < 0 || dataIndex >= listView.getChildCount()) {
            Log.d(CLS_TAG, "Unable to get view for desired position, because it's not being displayed on screen.");
            return null;
        }
        // Could also check if wantedPosition is between listView.getFirstVisiblePosition() and listView.getLastVisiblePosition() instead.
        return listView.getChildAt(dataIndex);
    }

    /**
     * Get a child in buttonView depending on x
     * !important This logic considers we can't have more than 1 button vertically
     *
     * @param buttonView
     * @param x
     * @return child in buttonView
     */
    private View getChildByPos(ViewGroup buttonView, float x) {
        final SwipeableRowView srv = (SwipeableRowView) buttonView.getParent();
        for (int i = 0; i < buttonView.getChildCount(); i++) {
            final View child = buttonView.getChildAt(i);
            // --- apply offset
            final float childXMin = slideToLeft ? (srv.getXPos() + srv.getWidth()) + child.getX() : 0;
            final float childXMax = slideToLeft ? childXMin + child.getWidth() : buttonView.getWidth();
            if (x > childXMin && x < childXMax) {
                return child;
            }
        }
        return null;
    }

    /**
     * Simulates a fling
     *
     * @param rowView
     * @param from
     * @param to
     * @param velocity
     */
    private void launchFlingAnimation(final View rowView, int from, final int to, int velocity) {
        if (rowView != null) {
            final ValueAnimator flingAnimator = ValueAnimator.ofInt(from, to).setDuration(SWIPE_DURATION);
            flingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    final Integer x = (Integer) valueAnimator.getAnimatedValue();
                    if (rowView instanceof SwipeableRowView) {
                        ((SwipeableRowView) rowView).applyPosition(x);
                    }
                }
            });
            flingAnimator.addListener(new AnimatorListenerAdapter() {

                @Override public void onAnimationEnd(Animator animation) {
                    // done
                    Log.d(CLS_TAG, "- Swipe animation ends -");
                }
            });
            flingAnimator.start();
        }
    }
}
