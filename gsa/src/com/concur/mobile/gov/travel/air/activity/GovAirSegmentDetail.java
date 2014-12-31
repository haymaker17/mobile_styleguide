package com.concur.mobile.gov.travel.air.activity;

import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.core.travel.air.activity.AirSegmentDetail;
import com.concur.mobile.core.util.Const;

public class GovAirSegmentDetail extends AirSegmentDetail {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void showFlightStatus() {
        // TODO
        Log.d(Const.LOG_TAG, " for gov1.1 release do nothing");
    }
}
