package com.concur.mobile.core.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.concur.core.R;

/**
 * Provides a simple layout view group that permits x,y coordinate positioning for children while not imposing size constraints.
 * 
 * @deprecated - use {@link com.concur.platform.ui.common.view.SimpleLayout} instead.
 * @author andy
 */
public class SimpleLayout extends ViewGroup {

    public SimpleLayout(Context context) {
        super(context);
    }

    public SimpleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        int maxHeight = 0;
        int maxWidth = 0;

        // Find out how big everyone wants to be

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        // Find rightmost and bottom-most child

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                int childRight;
                int childBottom;

                SimpleLayout.SimpleLayoutParams lp = (SimpleLayout.SimpleLayoutParams) child.getLayoutParams();

                childRight = lp.x + child.getMeasuredWidth();
                childBottom = lp.y + child.getMeasuredHeight();

                maxWidth = Math.max(maxWidth, childRight);
                maxHeight = Math.max(maxHeight, childBottom);
            }
        }

        // Account for padding too

        maxWidth += getPaddingLeft() + getPaddingRight();
        maxHeight += getPaddingTop() + getPaddingBottom();

        // Check against minimum height and width

        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec), resolveSize(maxHeight, heightMeasureSpec));
    }

    /**
     * Returns a set of layout parameters with a width of {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}, a height of
     * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT} and with the coordinates (0, 0).
     */
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new SimpleLayoutParams(SimpleLayoutParams.WRAP_CONTENT, SimpleLayoutParams.WRAP_CONTENT, 0, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                SimpleLayout.SimpleLayoutParams lp = (SimpleLayout.SimpleLayoutParams) child.getLayoutParams();

                int childLeft = getPaddingLeft() + lp.x;
                int childTop = getPaddingTop() + lp.y;
                child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(),
                        childTop + child.getMeasuredHeight());

            }
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new SimpleLayout.SimpleLayoutParams(getContext(), attrs);
    }

    // Override to allow type-checking of LayoutParams.

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof SimpleLayout.SimpleLayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new SimpleLayoutParams(p);
    }

    /**
     * An extension of <code>ViewGroup.LayoutParams</code> supporting specific positioning.
     */
    public static class SimpleLayoutParams extends ViewGroup.LayoutParams {

        /**
         * The horizontal, or X, location of the child within the view group.
         */
        public int x;
        /**
         * The vertical, or Y, location of the child within the view group.
         */
        public int y;

        /**
         * Creates a new set of layout parameters with the specified width, height and location.
         * 
         * @param width
         *            the width, either {@link #FILL_PARENT}, {@link #WRAP_CONTENT} or a fixed size in pixels
         * @param height
         *            the height, either {@link #FILL_PARENT}, {@link #WRAP_CONTENT} or a fixed size in pixels
         * @param x
         *            the X location of the child
         * @param y
         *            the Y location of the child
         */
        public SimpleLayoutParams(int width, int height, int x, int y) {
            super(width, height);
            this.x = x;
            this.y = y;
        }

        /**
         * Creates a new set of layout parameters. The values are extracted from the supplied attributes set and context. The XML
         * attributes mapped to this set of layout parameters are:
         * 
         * <ul>
         * <li><code>layout_x</code>: the X location of the child</li>
         * <li><code>layout_y</code>: the Y location of the child</li>
         * <li>All the XML attributes from {@link android.view.ViewGroup.LayoutParams}</li>
         * </ul>
         * 
         * @param c
         *            the application environment
         * @param attrs
         *            the set of attributes fom which to extract the layout parameters values
         */
        public SimpleLayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.SimpleLayout_Layout);
            x = a.getDimensionPixelOffset(R.styleable.SimpleLayout_Layout_layout_x, 0);
            y = a.getDimensionPixelOffset(R.styleable.SimpleLayout_Layout_layout_y, 0);
            a.recycle();
        }

        /**
         * {@inheritDoc}
         */
        public SimpleLayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public SimpleLayoutParams(FrameLayout.LayoutParams source) {
            super(source);
        }

    }
}
