/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.gov.travel.air.activity;

import android.content.Intent;

import com.concur.mobile.core.travel.air.activity.AirChoiceListItem;
import com.concur.mobile.core.travel.air.activity.AirResultsList;
import com.concur.mobile.core.travel.air.data.AirChoice;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.gov.util.Const;

/**
 * @author Chris N. Diaz
 * 
 */
public class GovAirResultsList extends AirResultsList {

    /**
     * Default constructor.
     */
    public GovAirResultsList() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.activity.travel.AirResultsList#getFlightDetailIntent()
     */
    @Override
    protected Intent getFlightDetailIntent() {
        Intent resultsIntent = new Intent(this, GovAirFlightDetail.class);

        Intent i = getIntent();
        String authNum = i.getStringExtra(Const.EXTRA_GOV_EXISTING_TA_NUMBER);
        String perdiemLocId = i.getStringExtra(Const.EXTRA_GOV_PER_DIEM_LOC_ID);
        if (i.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            resultsIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, i.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        // Pass the auth number and per diem loc id.
        resultsIntent.putExtra(Const.EXTRA_GOV_EXISTING_TA_NUMBER, authNum);
        resultsIntent.putExtra(Const.EXTRA_GOV_PER_DIEM_LOC_ID, perdiemLocId);

        return resultsIntent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.activity.travel.AirResultsList#createListItem(com.concur.core.data.travel.AirChoice)
     */
    @Override
    protected AirChoiceListItem createListItem(AirChoice airChoice) {
        return new GovAirChoiceListItem(airChoice);
    }

}
