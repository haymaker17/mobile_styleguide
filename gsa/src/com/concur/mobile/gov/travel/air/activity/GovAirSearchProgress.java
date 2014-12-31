/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.gov.travel.air.activity;

import android.content.Intent;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.air.activity.AirSearchProgress;
import com.concur.mobile.core.travel.air.activity.AirSearchResultsSummary;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.util.Const;

/**
 * @author Chris N. Diaz
 * 
 */
public class GovAirSearchProgress extends AirSearchProgress {

    /**
     * Default constructor.
     */
    public GovAirSearchProgress() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.activity.travel.AirSearchProgress#setSearchResultsIntent()
     */
    @Override
    protected void setSearchResultsIntent() {
        resultsIntent.setClass(GovAirSearchProgress.this, GovAirSearchResultsSummary.class);

        Intent i = getIntent();
        String authNum = i.getStringExtra(Const.EXTRA_GOV_EXISTING_TA_NUMBER);
        String perdiemLocId = i.getStringExtra(Const.EXTRA_GOV_PER_DIEM_LOC_ID);
        if (i.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            resultsIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, i.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }

        // Pass the auth number and per diem loc id.
        resultsIntent.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_EXISTING_TA_NUMBER, authNum);
        resultsIntent.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_PER_DIEM_LOC_ID, perdiemLocId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.activity.travel.AirSearchProgress#startSearch()
     */
    @Override
    protected void startSearch() {
        // Make the call
        GovService svc = (GovService) ((ConcurCore) getApplication()).getService();
        if (svc != null) {
            registerReceiver(receiver, airResultsFilter);
            svc.searchForFlights(departLocation.getIATACode(), arriveLocation.getIATACode(),
                departDateTime, returnDateTime, cabinClass, refundableOnly);
        }
    }
}
