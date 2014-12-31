package com.concur.mobile.core.widget;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import com.concur.core.R;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>Dialog</code> permitting the end-user to swipe left/right through multiple views.
 * 
 * @deprecated - use {@link com.concur.platform.ui.common.view.MultiViewDialog} instead.
 */
public class MultiViewDialog extends Dialog implements OnClickListener {

    private static final String CLS_TAG = MultiViewDialog.class.getSimpleName();

    /**
     * Contains the image close button.
     */
    protected ImageView close;

    /**
     * Contains the list of views to be displayed.
     */
    protected List<View> views;

    /**
     * Contains the context used to inflate the main view.
     */
    protected Context context;

    /**
     * Contains the <code>ViewSwitcher</code> object used to switch.
     */
    protected ViewSwitcher viewSwitcher;

    /**
     * Contains the <code>GestureDetector</code> object used for swiping.
     */
    protected GestureDetector gestureDetector;

    /**
     * Contains the main view.
     */
    protected View view;

    public MultiViewDialog(Context context, int style, List<View> views) {
        super(context, style);
        this.views = views;
        this.context = context;

        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.multi_view_dialog, null);

        // Set up the view pager.
        viewSwitcher = (ViewSwitcher) view.findViewById(R.id.view_switcher);
        if (viewSwitcher != null) {
            for (View v : views) {
                viewSwitcher.addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".<init>: unable to locate 'view_pager' view.");
        }
        // Set up the list of image views that will be used to indicate the current view.
        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.view_bullets);
        if (viewGroup != null) {
            if (views != null) {
                for (int viewInd = 0; viewInd < views.size(); ++viewInd) {
                    ImageView imgView = new ImageView(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.rightMargin = 4;
                    // Set the left margin for all views in-between the first and last.
                    if (viewInd > 0) {
                        params.leftMargin = 4;
                    }
                    imgView.setLayoutParams(params);
                    imgView.setScaleType(ScaleType.CENTER);
                    imgView.setImageResource(((viewInd == 0) ? R.drawable.dot_on : R.drawable.dot_off));
                    viewGroup.addView(imgView);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".<init>: unable to locate 'view_bullets' view!");
        }
        close = (ImageView) view.findViewById(R.id.close);
        if (close != null) {
            close.setOnClickListener(this);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".<init>: unable to locate 'close' button!");
        }

        // Hide the swipe message if only one view available.
        if (views.size() == 1) {
            hideSwipeForMoreMessage();
            hidePagerBullets();
        }

        gestureDetector = new GestureDetector(context, new MultiViewGestureListener());
        view.setOnTouchListener(new MultiViewTouchListener());

        setContentView(view);
    }

    protected void showSwipeForMoreMessage() {
        View v = view.findViewById(R.id.swipe_message);
        if (v != null) {
            Animation fadeInAnim = AnimationUtils.loadAnimation(context, R.anim.fade_in);
            fadeInAnim.setDuration(600L);
            v.setVisibility(View.VISIBLE);
            v.startAnimation(fadeInAnim);
        }
    }

    protected void hideSwipeForMoreMessage() {
        View v = view.findViewById(R.id.swipe_message);
        if (v != null) {
            Animation fadeOutAnim = AnimationUtils.loadAnimation(context, R.anim.fade_out);
            fadeOutAnim.setDuration(600L);
            v.setVisibility(View.INVISIBLE);
            v.startAnimation(fadeOutAnim);
        }
    }

    protected void hidePagerBullets() {
        View v = view.findViewById(R.id.view_bullets);
        if (v != null) {
            v.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == close) {
            dismiss();
        }
    }

    /**
     * Will update the set of bullets such that the current position reflects a highlighted bullet.
     * 
     * @param position
     *            the position of the current view.
     */
    protected void updatePagerBullets(int position) {
        // Set up the list of image views that will be used to indicate the current view.
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.view_bullets);
        if (viewGroup != null) {
            for (int childInd = 0; childInd < viewGroup.getChildCount(); ++childInd) {
                View view = viewGroup.getChildAt(childInd);
                if (view instanceof ImageView) {
                    ImageView imgView = (ImageView) view;
                    int dotResId = (childInd == position) ? R.drawable.dot_on : R.drawable.dot_off;
                    imgView.setImageResource(dotResId);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".updatePagerBullets: found non ImageView child!");
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updatePagerBullets: unable to locate 'view_bullets' view!");
        }
    }

    protected class MultiViewGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {

            final float vThreshold = 250.0f;
            if (vX < -vThreshold) {
                if (views.indexOf(viewSwitcher.getCurrentView()) != (views.size() - 1)) {
                    viewSwitcher.setInAnimation(context, R.anim.slide_in_right);
                    viewSwitcher.setOutAnimation(context, R.anim.slide_out_left);
                    viewSwitcher.showNext();
                    if (views.indexOf(viewSwitcher.getCurrentView()) == (views.size() - 1)) {
                        hideSwipeForMoreMessage();
                    }
                    updatePagerBullets(1);
                }
            } else if (vX > vThreshold) {
                if (views.indexOf(viewSwitcher.getCurrentView()) != 0) {
                    viewSwitcher.setInAnimation(context, R.anim.slide_in_left);
                    viewSwitcher.setOutAnimation(context, R.anim.slide_out_right);
                    viewSwitcher.showPrevious();
                    if (views.indexOf(viewSwitcher.getCurrentView()) != (views.size() - 1)) {
                        showSwipeForMoreMessage();
                    }
                    updatePagerBullets(0);
                }
            }
            return true;
        }

    }

    public class MultiViewTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            // Let the gesture detector see everything but we don't care about the return.
            // In fact, we want it to always return true because if the gesture detector returns
            // false it will break the gesture and not properly handle later events.
            gestureDetector.onTouchEvent(event);

            // Consume the touch
            return true;

        }

    }

    /**
     * An extension of <code>ViewPager.SimpleOnPageChangeListener</code> to handle page switches between advanced fields and
     * results views.
     */
    public class MultiViewPagerListener extends ViewPager.SimpleOnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            updatePagerBullets(position);
        }

    }

    /**
     * An extension of <code>PagerAdapter</code> to handle animated, end-user controlled switching between views.
     */
    class MultiViewPagerAdapter extends PagerAdapter {

        private List<View> views;

        /**
         * Constructs an instance of <code>MultiViewPagerAdapter</code>.
         * 
         * @param views
         *            the list of views.
         */
        MultiViewPagerAdapter(List<View> views) {
            this.views = views;
        }

        @Override
        public void destroyItem(View view, int arg1, Object object) {
            ((ViewPager) view).removeView((View) object);
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public Object instantiateItem(View pager, int position) {
            View view = views.get(position);
            ((ViewPager) pager).addView(view);
            return view;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }

    }

}
