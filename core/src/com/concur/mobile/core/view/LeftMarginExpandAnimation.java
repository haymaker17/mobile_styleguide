package com.concur.mobile.core.view;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout.LayoutParams;

import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>Animation</code> that will expand/shrink the left margin.
 * 
 * @deprecated - use {@link com.concur.platform.ui.common.view.LeftMarginExpandAnimation} instead.
 */
public class LeftMarginExpandAnimation extends Animation {

    private static final String CLS_TAG = LeftMarginExpandAnimation.class.getSimpleName();

    private View mAnimatedView;
    private LayoutParams mViewLayoutParams;
    private int mMarginStart, mMarginEnd;
    private boolean mIsVisibleAfter = false;
    private boolean mWasEndedAlready = false;

    /**
     * Initialize the animation
     * 
     * @param view
     *            The layout we want to animate
     * @param duration
     *            The duration of the animation, in ms
     */
    public LeftMarginExpandAnimation(View view, int duration, int marginWidth) {

        setDuration(duration);
        mAnimatedView = view;
        mViewLayoutParams = (LayoutParams) view.getLayoutParams();

        // decide to show or hide the view
        mIsVisibleAfter = (view.getVisibility() == View.VISIBLE);

        Log.d(Const.LOG_TAG, CLS_TAG + ".<init>: mIsVisibleAfter: " + Boolean.toString(mIsVisibleAfter));

        mMarginStart = mViewLayoutParams.leftMargin;
        mMarginEnd = (mMarginStart == 0 ? (mMarginStart + marginWidth) : 0);

        Log.d(Const.LOG_TAG, CLS_TAG + ".<init>: view.getWidth() -> " + view.getWidth());

        view.setVisibility(View.VISIBLE);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);

        if (interpolatedTime < 1.0f) {

            // Calculating the new left margin, and setting it
            mViewLayoutParams.leftMargin = mMarginStart + (int) ((mMarginEnd - mMarginStart) * interpolatedTime);

            // Invalidating the layout, making us seeing the changes we made
            mAnimatedView.requestLayout();

            // Making sure we didn't run the ending before (it happens!)
        } else if (!mWasEndedAlready) {
            mViewLayoutParams.leftMargin = mMarginEnd;
            mAnimatedView.requestLayout();

            // if (mIsVisibleAfter) {
            // mAnimatedView.setVisibility(View.GONE);
            // }
            mWasEndedAlready = true;
        }
    }
}
