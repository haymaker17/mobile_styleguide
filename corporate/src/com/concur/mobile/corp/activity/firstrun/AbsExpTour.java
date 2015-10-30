package com.concur.mobile.corp.activity.firstrun;

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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import com.concur.breeze.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.corp.ConcurMobile;
import com.concur.mobile.corp.activity.Home;

/**
 * Base activity for tour.
 * Created by sunill on 9/22/15.
 */
public class AbsExpTour extends BaseActivity {


    protected ViewFlipper flipper;
    protected LinearLayout pageMarkers;
    protected GestureDetector gestureDetector;

    protected Button launch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.first_run_exp_tour);

        // Tour is portrait only
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final Resources resources = getResources();

        // Find the flipper and the page dots layout
        flipper = (ViewFlipper) findViewById(R.id.tourFlipper);
        flipper =setPages();
        setPageMaker();
        // Setup the gesture listener
        gestureDetector = new GestureDetector(this, new MultiViewGestureListener());
        flipper.setOnTouchListener(new MultiViewTouchListener());

        // on-click listener of launch button
        launch = (Button) findViewById(R.id.exp_it_travel_continue);
        setButtonListner();
    }

    protected void setPageMaker(){
        pageMarkers = (LinearLayout) findViewById(R.id.tourPageMarkers);
        for(int i=0;i<flipper.getChildCount();i++){
            pageMarkers.getChildAt(i).setVisibility(View.VISIBLE);
        }
    }

    protected void setButtonListner(){
        launch.setText(getString(R.string.next));
        launch.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                flipper.showNext();
                showContinue(0,false);
            }
        });
    }
    /*Override this method in each class*/
    protected ViewFlipper setPages(){
        return flipper;
    }

    //Show continue button
    protected void showContinue(int currentChild,boolean buttonHit) {
        launch.setText(getString(R.string.get_started));
        if(currentChild==1 && buttonHit){
            pageMarkers.getChildAt(currentChild-1).setBackground(getResources().getDrawable(R.drawable.home_tour_white_dot));
            pageMarkers.getChildAt(currentChild).setBackground(getResources().getDrawable(R.drawable.home_tour_blue_dot));
        }else{
            pageMarkers.getChildAt(currentChild).setBackground(getResources().getDrawable(R.drawable.home_tour_white_dot));
            pageMarkers.getChildAt(currentChild + 1).setBackground(getResources().getDrawable(R.drawable.home_tour_blue_dot));
        }
        launch.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                gotoHome(false);
            }
        });
    }

    //Show next button
    protected void showNext(final int currentChild) {
        launch = (Button) findViewById(R.id.exp_it_travel_continue);
        if(currentChild==1){
            launch.setText(getString(R.string.next));
        }
        pageMarkers.getChildAt(currentChild).setBackground(getResources().getDrawable(R.drawable.home_tour_white_dot));
        pageMarkers.getChildAt(currentChild - 1).setBackground(getResources().getDrawable(R.drawable.home_tour_blue_dot));
        launch.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                flipper.showNext();
                showContinue(currentChild,true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Multi view gesture listener for swipe tour pages
     * */
    protected class MultiViewGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {

            final float threshold = 250.0f;
            int currentChild = flipper.getDisplayedChild();
            if (Math.abs(event1.getY() - event2.getY()) > threshold)
                return false;
            if (velocityX < -threshold) {
                if (currentChild < flipper.getChildCount() - 1) {
                    setFlipForShowNext(currentChild,AbsExpTour.this);
                }

            } else if (velocityX > threshold) {
                if (currentChild > 0) {
                    setFlipForShowPrevious(currentChild,AbsExpTour.this);
                }

            }
            return true;
        }

    }

    protected void setFlipForShowNext(int currentChild, Context ctx){
        flipper.setInAnimation(ctx, R.anim.slide_in_right);
        flipper.setOutAnimation(ctx, R.anim.slide_out_left);
        showContinue(currentChild,false);
        flipper.showNext();
    }

    protected void setFlipForShowPrevious(int currentChild,Context ctx){
        flipper.setInAnimation(ctx, R.anim.slide_in_left);
        flipper.setOutAnimation(ctx, R.anim.slide_out_right);
        showNext(currentChild);
        flipper.showPrevious();
    }

    /**
     * @param skippedTour <code>true</code> if the user pressed the "Skip".
     */
    protected void gotoHome(boolean skippedTour) {
        // Prior to the starting the home screen, initialize the system/user
        // configuration information.
        ((ConcurMobile) this.getApplication()).initSystemConfig();
        ((ConcurMobile) this.getApplication()).initUserConfig();

        boolean launchExpList = getIntent().getBooleanExtra(Home.LAUNCH_EXPENSE_LIST, false);

        Intent i = new Intent(this, Home.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(Home.LAUNCH_EXPENSE_LIST, launchExpList);
        setResult(Activity.RESULT_OK);
        setPreference();
        startActivity(i);

        logUptime(skippedTour);

        finish();
    }

    protected void setPreference(){
        return;

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

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

    protected void logUptime(boolean skippedTour) {
        // Analytics stuff.
       // TODO not required at this moment
    }
}