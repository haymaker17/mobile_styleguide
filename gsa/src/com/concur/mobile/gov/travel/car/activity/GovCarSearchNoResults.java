package com.concur.mobile.gov.travel.car.activity;

import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.core.travel.car.activity.CarSearchNoResults;
import com.concur.mobile.core.util.Const;

public class GovCarSearchNoResults extends CarSearchNoResults {

    private static final String CLS_TAG = GovCarSearchNoResults.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: in gov proj.!");
    }
}
