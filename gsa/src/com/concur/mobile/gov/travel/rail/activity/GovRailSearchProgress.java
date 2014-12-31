package com.concur.mobile.gov.travel.rail.activity;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.core.travel.rail.activity.RailSearchProgress;
import com.concur.mobile.core.travel.rail.data.RailStation;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;

public class GovRailSearchProgress extends RailSearchProgress {

    private static final String CLS_TAG = GovRailSearchProgress.class.getSimpleName();

    public GovRailSearchProgress() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void initValues() {
        Intent intent = getIntent();

        // Setup our future intents while we are here
        noResultsIntent = (Intent) getIntent().clone();
        noResultsIntent.setClass(this, GovRailSearchNoResults.class);
        resultsIntent = (Intent) getIntent().clone();
        resultsIntent.setClass(this, GovRailSearchResults.class);
        if (intent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            resultsIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, intent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        // From rail station.
        Bundle bundle = intent.getBundleExtra(Const.EXTRA_SEARCH_LOC_FROM);
        if (bundle != null) {
            currentDepLocation = new RailStation(bundle);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initValues: intent missing from railstation bundle!");
        }
        // To rail station.
        bundle = intent.getBundleExtra(Const.EXTRA_SEARCH_LOC_TO);
        if (bundle != null) {
            currentArrLocation = new RailStation(bundle);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initValues: intent missing to railstation bundle!");
        }
        // Depart date/time.
        depDateTime = (Calendar) intent.getSerializableExtra(Const.EXTRA_SEARCH_DT_DEPART);
        if (depDateTime == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initValues: intent missing departure date/time!");
        }
        // Return date/time.
        retDateTime = (Calendar) intent.getSerializableExtra(GovRailSearch.RET_DATETIME);
        roundTrip = (retDateTime != null);
    }
}
