/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.gov.travel.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.concur.mobile.core.eva.activity.VoiceSearchActivity;
import com.concur.mobile.core.travel.activity.BookTravelDialogFragment;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.util.TravelBookingCache;
import com.concur.mobile.gov.util.TravelBookingCache.BookingSelection;

/**
 * <code>DialogFragment</code> to prompt user to book something,
 * i.e. a Hotel, Flight, Car, Train, etc.
 * 
 * @author sunill--for gov
 * 
 */
public class GovBookTravelDialogFragment extends BookTravelDialogFragment {

    @Override
    protected void addMicButton(Class<? extends VoiceSearchActivity> voiceActivity, View view,
        BookTravelDialogFragment dialogFragment) {
        // No operation.
    }

    @Override
    public Intent getAirSearchIntent(Activity act) {
        Intent i = null;
        GovAppMobile app = clearData(act);
        // Check whether user has permission to book air via mobile.
        if (ViewUtil.isAirUser(act)) {
            // Check for a complete travel profile.
            if (ViewUtil.isTravelProfileComplete(act)) {
                i = new Intent(act, TravelAuthType.class);
                app.trvlBookingCache.setSelectedBookingType(BookingSelection.AIR);
                i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_TRIPS);
            } else {
                act.showDialog(Const.DIALOG_TRAVEL_PROFILE_INCOMPLETE);
            }
        } else {
            act.showDialog(Const.DIALOG_TRAVEL_NO_AIR_PERMISSION);
        }
        return i;
    }

    @Override
    public Intent getCarSearchIntent(Activity act) {
        GovAppMobile app = clearData(act);
        Intent i = new Intent(act, TravelAuthType.class);
        app.trvlBookingCache.setSelectedBookingType(BookingSelection.CAR);
        i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_TRIPS);
        return i;
    }

    @Override
    public Intent getHotelSearchIntent(Activity act) {
        GovAppMobile app = clearData(act);
        Intent i = new Intent(act, TravelAuthType.class);
        app.trvlBookingCache.setSelectedBookingType(BookingSelection.HOTEL);
        i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_TRIPS);
        return i;
    }

    @Override
    public Intent getRailSearchIntent(Activity act) {
        GovAppMobile app = clearData(act);
        Intent i = new Intent(act, TravelAuthType.class);
        app.trvlBookingCache.setSelectedBookingType(BookingSelection.RAIL);
        i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_TRIPS);
        return i;
    }

    private GovAppMobile clearData(Activity act) {
        GovAppMobile app = ((GovAppMobile) act.getApplication());
        app.trvlBookingCache = new TravelBookingCache();
        return app;
    }

} // BookTravelDialogFragment class

