package com.concur.mobile.gov.travel.rail.activity;

import android.content.Intent;
import android.os.Bundle;

import com.concur.mobile.core.travel.rail.activity.RailSearchResultsFares;
import com.concur.mobile.core.util.Flurry;

public class GovRailSearchResultsFares extends RailSearchResultsFares {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Intent getResultantIntent() {
        Intent intent = new Intent(this, GovRailSearchDetail.class);
        Intent callingIntent = getIntent();
        if (callingIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, callingIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        return intent;
    }
}
