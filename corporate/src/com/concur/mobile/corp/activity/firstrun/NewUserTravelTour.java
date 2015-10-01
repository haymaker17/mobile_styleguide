package com.concur.mobile.corp.activity.firstrun;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.concur.breeze.R;

public class NewUserTravelTour extends AbsExpTour {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected ViewFlipper setPages() {
        // Add our pages
        LayoutInflater infl = getLayoutInflater();
        View page = infl.inflate(R.layout.new_user_exp_tour_page_3, null);

        flipper.addView(page, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return flipper;
    }

    @Override
    protected void setPreference() {
        //do not do anything
        return;

    }

    @Override
    protected void setButtonListner() {
        launch.setText(getString(R.string.get_started));
        launch.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                gotoHome(false);
            }
        });
    }

    @Override
    protected void setFlipForShowNext(int currentChild, Context ctx) {
        flipper.setInAnimation(ctx, R.anim.slide_in_right);
        flipper.setOutAnimation(ctx, R.anim.slide_out_left);
        pageMarkers.getChildAt(currentChild).setBackgroundColor(inactiveColor);
        pageMarkers.getChildAt(currentChild + 1).setBackgroundColor(activeColor);
        flipper.showNext();
    }

    @Override
    protected void setFlipForShowPrevious(int currentChild, Context ctx) {
        flipper.setInAnimation(ctx, R.anim.slide_in_left);
        flipper.setOutAnimation(ctx, R.anim.slide_out_right);
        pageMarkers.getChildAt(currentChild).setBackgroundColor(inactiveColor);
        pageMarkers.getChildAt(currentChild - 1).setBackgroundColor(activeColor);
        flipper.showPrevious();
    }
}