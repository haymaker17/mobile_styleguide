package com.concur.mobile.gov.travel.rail.activity;

import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.core.travel.rail.activity.RailSearchNoResults;
import com.concur.mobile.core.util.Const;

public class GovRailSearchNoResults extends RailSearchNoResults {

    private static final String CLS_TAG = GovRailSearchNoResults.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: in gov proj.!");
    }

}
