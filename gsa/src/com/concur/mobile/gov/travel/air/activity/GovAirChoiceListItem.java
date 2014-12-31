/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.gov.travel.air.activity;

import android.view.View;
import android.widget.ImageView;

import com.concur.gov.R;
import com.concur.mobile.core.travel.air.activity.AirChoiceListItem;
import com.concur.mobile.core.travel.air.data.AirChoice;
import com.concur.mobile.gov.travel.data.GovAirChoice;

/**
 * @author Chris N. Diaz
 * 
 */
public class GovAirChoiceListItem extends AirChoiceListItem {

    /**
     * @param airChoice
     */
    public GovAirChoiceListItem(AirChoice airChoice) {
        super(airChoice);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.activity.travel.AirChoiceListItem#setPreferenceRanking(android.view.View)
     */
    @Override
    protected void setPreferenceRanking(View rowView) {

        if (airChoice instanceof GovAirChoice) {

            GovRateType rateType = ((GovAirChoice) airChoice).rateType;
            ImageView iv = (ImageView) rowView.findViewById(R.id.rateType);

            if (rateType != null && iv != null) {
                if (rateType == GovRateType.LIMITED_CAPACITY
                    || rateType == GovRateType.CONTRACT
                    || rateType == GovRateType.CONTRACT_BUSINESS) {
                    iv.setImageResource(R.drawable.rate_three_star);
                    iv.setVisibility(View.VISIBLE);
                } else if (rateType == GovRateType.ME_TOO) {
                    iv.setImageResource(R.drawable.rate_two_star);
                    iv.setVisibility(View.VISIBLE);
                } else {
                    iv.setVisibility(View.GONE);
                }

            } else if (iv != null) {
                iv.setVisibility(View.GONE);
            }

        } else {
            super.setPreferenceRanking(rowView);
        }
    }

}
