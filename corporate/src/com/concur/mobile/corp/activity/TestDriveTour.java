package com.concur.mobile.corp.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.concur.breeze.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.corp.ConcurMobile;

public class TestDriveTour extends BaseActivity {

    protected int activeColor;
    protected int inactiveColor;

    protected ViewFlipper flipper;
    protected LinearLayout pageMarkers;
    protected GestureDetector gestureDetector;

    protected long startTime = 0L;
    protected long upTime = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test_drive_tour);

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
        View page = infl.inflate(R.layout.test_drive_tour_page_1, null);
        // init subview of page.
        initPageDetail(page);

        flipper.addView(page, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        page = infl.inflate(R.layout.test_drive_tour_page_2, null);

        // on-click listener of launch button
        Button launch = (Button) page.findViewById(R.id.td_tour_launch);
        launch.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                gotoHome(false);
            }
        });

        flipper.addView(page, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        // Setup the gesture listener
        gestureDetector = new GestureDetector(this, new MultiViewGestureListener());
        flipper.setOnTouchListener(new MultiViewTouchListener());

        if (savedInstanceState != null) {
            upTime = savedInstanceState.getLong(Const.ACTIVITY_STATE_UPTIME, 0L);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start the time the user has spent on this screen.
        startTime = System.nanoTime();
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int orientation = display.getRotation();
        // TODO MOB-16854 : required API check. Please remove this API check once you upgrade your api level to 9.
        if (Build.VERSION.SDK_INT < 9) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            if (orientation == Surface.ROTATION_180) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save the time the user spent on this screen, but
        // perhaps put the app in the background.
        upTime += (System.nanoTime() - startTime) / 1000000000L; // Convert to
                                                                 // seconds.
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the uptime so we know how long the user has been on this screen,
        // even if it has been destroyed.
        outState.putLong(Const.ACTIVITY_STATE_UPTIME, upTime);
    }

    private void initPageDetail(View page) {
        View v = page.findViewById(R.id.td_tour_topic1);
        TextView tv = (TextView) v.findViewById(R.id.universal_tour_text);
        tv.setText(getString(R.string.test_drive_tour_welcome_pg1_text1));

        v = page.findViewById(R.id.td_tour_topic2);
        tv = (TextView) v.findViewById(R.id.universal_tour_text);
        tv.setText(getString(R.string.test_drive_tour_welcome_pg1_text2));

        v = page.findViewById(R.id.td_tour_topic3);
        tv = (TextView) v.findViewById(R.id.universal_tour_text);
        tv.setText(getString(R.string.test_drive_tour_welcome_pg1_text3));

    }

    protected void initScreenHeader() {
        getSupportActionBar().setTitle(R.string.test_drive_title);
    }

    protected class MultiViewGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {

            final float threshold = 250.0f;
            int currentChild = flipper.getDisplayedChild();
            if (velocityX < -threshold) {
                if (currentChild < flipper.getChildCount() - 1) {
                    flipper.setInAnimation(TestDriveTour.this, R.anim.slide_in_right);
                    flipper.setOutAnimation(TestDriveTour.this, R.anim.slide_out_left);
                    flipper.showNext();

                    pageMarkers.getChildAt(currentChild).setBackgroundColor(inactiveColor);
                    pageMarkers.getChildAt(currentChild + 1).setBackgroundColor(activeColor);
                }

            } else if (velocityX > threshold) {
                if (currentChild > 0) {
                    flipper.setInAnimation(TestDriveTour.this, R.anim.slide_in_left);
                    flipper.setOutAnimation(TestDriveTour.this, R.anim.slide_out_right);
                    flipper.showPrevious();

                    pageMarkers.getChildAt(currentChild).setBackgroundColor(inactiveColor);
                    pageMarkers.getChildAt(currentChild - 1).setBackgroundColor(activeColor);
                }

            }
            return true;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.test_drive, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.menuSkip: {
            gotoHome(true);
            break;

        }

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 
     * @param skippedTour
     *            <code>true</code> if the user pressed the "Skip" or BACK button to skip the tour.
     */
    protected void gotoHome(boolean skippedTour) {
        // Prior to the starting the home screen, initialize the system/user
        // configuration information.
        ((ConcurMobile) this.getApplication()).initSystemConfig();
        ((ConcurMobile) this.getApplication()).initUserConfig();

        Intent i = new Intent(this, Home.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        setResult(Activity.RESULT_OK);

        startActivity(i);

        logUptime(skippedTour);

        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            gotoHome(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public class MultiViewTouchListener implements View.OnTouchListener {

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

    public void onCancel(FragmentActivity activity, DialogInterface dialog) {
    }

    private void logUptime(boolean skippedTour) {
        // Analytics stuff.
        Map<String, String> params = new HashMap<String, String>();
        params.put("Result:", (skippedTour ? "Skipped" : "Completed"));
        upTime = ((System.nanoTime() - startTime) / 1000000000L) + upTime; // Convert nanoseconds to seconds.
        params.put("Seconds on Tour:", Flurry.formatDurationEventParam(upTime));
        EventTracker.INSTANCE.track(Flurry.CATEGORY_TEST_DRIVE, Flurry.PARAM_VALUE_TOUR, params);
    }
}
