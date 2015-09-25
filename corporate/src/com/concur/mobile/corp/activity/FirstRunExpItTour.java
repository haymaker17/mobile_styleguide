package com.concur.mobile.corp.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.concur.breeze.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;

@EventTracker.EventTrackerClassName(getClassName = Flurry.SCREEN_NAME_NEW_FEATURE_EXPENSE_IT)
public class FirstRunExpItTour extends AbsExpTour {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected ViewFlipper setPages() {
        // Add our pages
        LayoutInflater infl = getLayoutInflater();
        View page = infl.inflate(R.layout.first_run_exp_tour_page_1, null);

        flipper.addView(page, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        page = infl.inflate(R.layout.first_run_exp_tour_page_2, null);

        flipper.addView(page, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return flipper;
    }

    @Override
    protected void setPreference() {
        Context ctx = ConcurCore.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Preferences.setFirstRunExpUpgradeExpenseIt(prefs);
        //we need to show coach mark update PREF_APP_UPGRADE when you dismiss coach mark.
    }

    @Override
    protected void setButtonListner(){
        launch.setText(getString(R.string.get_started));
        launch.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                gotoHome(false);
            }
        });
    }

    @Override
    protected void setFlipForShowNext(int currentChild, Context ctx){
        flipper.setInAnimation(ctx, R.anim.slide_in_right);
        flipper.setOutAnimation(ctx, R.anim.slide_out_left);
        pageMarkers.getChildAt(currentChild).setBackgroundColor(inactiveColor);
        pageMarkers.getChildAt(currentChild + 1).setBackgroundColor(activeColor);
        flipper.showNext();
    }

    @Override
    protected void setFlipForShowPrevious(int currentChild, Context ctx){
        flipper.setInAnimation(ctx, R.anim.slide_in_left);
        flipper.setOutAnimation(ctx, R.anim.slide_out_right);
        pageMarkers.getChildAt(currentChild).setBackgroundColor(inactiveColor);
        pageMarkers.getChildAt(currentChild - 1).setBackgroundColor(activeColor);
        flipper.showPrevious();
    }
}
