package com.concur.mobile.gov.travel.rail.activity;

import android.content.Intent;
import android.os.Bundle;

import com.concur.mobile.core.travel.rail.activity.RailSearchResults;
import com.concur.mobile.core.util.Flurry;

public class GovRailSearchResults extends RailSearchResults {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Intent getResultIntent() {
        Intent intent = new Intent(this, GovRailSearchResultsFares.class);
        Intent callingIntent = getIntent();
        if (callingIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, callingIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        return intent;
    }
}
