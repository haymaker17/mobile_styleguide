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

public class FirstRunExpItTravelTour extends AbsExpTour {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected ViewFlipper setPages(){
        // Add our pages
        LayoutInflater infl = getLayoutInflater();
        View page = infl.inflate(R.layout.first_run_exp_tour_page_1, null);

        flipper.addView(page, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        page = infl.inflate(R.layout.first_run_exp_tour_page_2, null);

        flipper.addView(page, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        page = infl.inflate(R.layout.first_run_exp_tour_page_3, null);

        flipper.addView(page, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        page = infl.inflate(R.layout.first_run_exp_tour_page_4, null);

        flipper.addView(page, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return flipper;
    }

    @Override
    protected void setPreference() {
        Context ctx = ConcurCore.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Preferences.setFirstRunExpUpgradeExpenseItTravel(prefs);
        //we need to show coach mark update PREF_APP_UPGRADE when you dismiss coach mark.
    }
}
