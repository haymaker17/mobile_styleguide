package com.concur.mobile.core.activity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.concur.core.R;

public class Tour extends BaseActivity {

    protected int activeColor;
    protected int inactiveColor;

    protected ViewFlipper flipper;
    protected LinearLayout pageMarkers;
    protected GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tour);

        // Tour is portrait only
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initScreenHeader();

        final Resources resources = getResources();
        activeColor = resources.getColor(R.color.tourPageMarkerActive);
        inactiveColor = resources.getColor(R.color.tourPageMarkerInactive);

        // Find the flipper and the page dots layout
        flipper = (ViewFlipper) findViewById(R.id.tourFlipper);
        pageMarkers = (LinearLayout) findViewById(R.id.tourPageMarkers);

        // Add our pages
        LayoutInflater infl = getLayoutInflater();
        View page = infl.inflate(R.layout.tour_page_1, null);
        // initialize page detail
        initPageDetail(page, R.string.tour_1_top, R.string.tour_1_bottom, R.drawable.tour1, Gravity.CENTER);

        flipper.addView(page, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        page = infl.inflate(R.layout.tour_page_2, null);
        // initialize page detail
        initPageDetail(page, R.string.tour_2_top, R.string.tour_2_bottom, R.drawable.tour2, Gravity.LEFT
                | Gravity.CENTER);

        flipper.addView(page, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        page = infl.inflate(R.layout.tour_page_3, null);
        // initialize page detail
        initPageDetail(page, R.string.tour_3_top, R.string.tour_3_bottom, R.drawable.tour3, Gravity.CENTER);

        flipper.addView(page, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        // Setup the gesture listener
        gestureDetector = new GestureDetector(this, new MultiViewGestureListener());
        flipper.setOnTouchListener(new MultiViewTouchListener());

    }

    protected void initScreenHeader() {
        getSupportActionBar().setTitle(R.string.tour_title);
    }

    protected void initPageDetail(View page, int upperTextId, int bottomTextId, int imgResource, int gravity) {
        // upper text
        TextView txtView = (TextView) page.findViewById(R.id.tourTextTop);
        txtView.setText(getResources().getText(upperTextId).toString());

        // bottom text
        txtView = (TextView) page.findViewById(R.id.tourTextBottom);
        txtView.setText(getResources().getText(bottomTextId).toString());

        // set bottom textview gravity. It is necessary to change gravity for each page's bottom text. Each page has different
        // style of bottom text.
        txtView.setGravity(gravity);

        // image
        ImageView view = (ImageView) page.findViewById(R.id.tourImage);
        view.setImageResource(imgResource);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // We've only registered for orientation changes to prevent this one screen from flipping.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    protected class MultiViewGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {

            final float vThreshold = 250.0f;
            int currentChild = flipper.getDisplayedChild();
            if (vX < -vThreshold) {
                if (currentChild < flipper.getChildCount() - 1) {
                    flipper.setInAnimation(Tour.this, R.anim.slide_in_right);
                    flipper.setOutAnimation(Tour.this, R.anim.slide_out_left);
                    flipper.showNext();

                    pageMarkers.getChildAt(currentChild).setBackgroundColor(inactiveColor);
                    pageMarkers.getChildAt(currentChild + 1).setBackgroundColor(activeColor);
                }

            } else if (vX > vThreshold) {
                if (currentChild > 0) {
                    flipper.setInAnimation(Tour.this, R.anim.slide_in_left);
                    flipper.setOutAnimation(Tour.this, R.anim.slide_out_right);
                    flipper.showPrevious();

                    pageMarkers.getChildAt(currentChild).setBackgroundColor(inactiveColor);
                    pageMarkers.getChildAt(currentChild - 1).setBackgroundColor(activeColor);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return gestureDetector.onTouchEvent(ev);
    }

}
