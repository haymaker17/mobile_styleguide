/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.gov.travel.air.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.air.activity.AirFlightDetail;
import com.concur.mobile.core.travel.air.data.Flight;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.StyleableSpannableStringBuilder;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.travel.activity.GovSegmentList;
import com.concur.mobile.gov.travel.activity.OpenOrExistingAuthListItem;
import com.concur.mobile.gov.travel.activity.TDYPerDiemLocationItem;
import com.concur.mobile.gov.travel.service.PerDiemRateReply;
import com.concur.mobile.gov.util.TravelBookingCache;

/**
 * @author Chris N. Diaz
 * 
 */
public class GovAirFlightDetail extends AirFlightDetail {

    private static final String CLS_TAG = GovAirFlightDetail.class.getSimpleName();

    private static final int BOOK_AIR_DOC = 1;

    private Bundle bundle;

    private Trip selectedTrip;

    private boolean isCreatedAuthAvial = false;

    private String existingTANumber;
    private String perdiemLocationID;

    private TDYPerDiemLocationItem selectedPerDiemItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        GovAppMobile app = (GovAppMobile) getApplication();
        TravelBookingCache cache = app.trvlBookingCache;
        if (cache.isGenerateAuthUsed()) {
            if (!isCreatedAuthAvial) {
                existingTANumber = null;
            }

        } else {
            OpenOrExistingAuthListItem item = cache.getSelectedAuthItem();
            if (item == null) {
                existingTANumber = null;
            } else {
                existingTANumber = item.getItem().taNumber;
            }
        }
        PerDiemRateReply reply = cache.getPerDiemRateReply();
        if (reply == null || reply.rateList == null || reply.rateList.size() <= 0) {
            perdiemLocationID = null;
        } else {
            perdiemLocationID = reply.rateList.get(0).tabRow;
        }
        // get perdiem item.
        selectedPerDiemItem = cache.getSelectedPerDiemItem();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.activity.travel.AirFlightDetail#sendAirSellRequest()
     */
    @Override
    protected void sendAirSellRequest() {
        doReservation();
    }

    private void doReservation() {
        if (GovAppMobile.isConnected()) {
            GovService govService = (GovService) getConcurService();
            registerAirSellReceiver();
            String tripName = "";
            Flight fstSegFstFlt = airChoice.segments.get(0).flights.get(0);
            Flight fstSegLstFlg = airChoice.segments.get(0).flights.get(airChoice.segments.get(0).flights.size() - 1);
            tripName = Format.localizeText(this, R.string.air_reservation_default_trip_name,
                fstSegFstFlt.startIATA, fstSegLstFlg.endIATA);
            String reasonCodeId = (reasonCode != null) ? reasonCode.id : "";
            String violationText = (justificationText != null) ? justificationText : "";
            String ffProgramId = (curAffinityChoice != null) ? curAffinityChoice.id : null;
            boolean refundableOnly = getIntent().getBooleanExtra(Const.EXTRA_SEARCH_REFUNDABLE_ONLY, false);

            List<TravelCustomField> tcfs = getTravelCustomFields();
            airSellRequest = govService.sendAirSellRequest(getUserId(), Integer.parseInt(curCardChoice.id),
                airChoice.fareId, ffProgramId, refundableOnly, tripName, reasonCodeId, violationText, tcfs,
                existingTANumber, perdiemLocationID, selectedPerDiemItem);
            if (airSellRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendAirSellRequest: unable to create request to book air!");
                unregisterAirSellReceiver();
            } else {
                // Set the request object on the receiver.
                airSellReceiver.setServiceRequest(airSellRequest);
                showDialog(BOOKING_PROGRESS_DIALOG);
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    @Override
    protected void initTotalPrice() {
        View totalPriceView = findViewById(R.id.total_price);
        if (totalPriceView != null) {
            // Set the field title.
            TextView txtView = (TextView) totalPriceView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.air_search_flight_detail_total_price);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initTotalPrice: unable to locate 'field_name' text view!");
            }
            // Set the field value.
            txtView = (TextView) totalPriceView.findViewById(R.id.field_value);
            if (txtView != null) {
                StyleableSpannableStringBuilder spanStrBldr = new StyleableSpannableStringBuilder();
                String formattedAmtStr = FormatUtil.formatAmount(airChoice.fare,
                    getResources().getConfiguration().locale, airChoice.crnCode, true, true);
                int textAppearanceResourceId = ViewUtil.getFormFieldValueStyle(airChoice.violations);
                spanStrBldr.appendWithStyle(new TextAppearanceSpan(this, textAppearanceResourceId), formattedAmtStr);
                if (airChoice.refundable != null && airChoice.refundable) {
                    String refundStr = "  R";
                    spanStrBldr.appendWithStyle(new TextAppearanceSpan(this, com.concur.gov.R.style.RefundableText), refundStr);
                }
                txtView.setText(spanStrBldr);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initTotalPrice: unable to locate 'field_value' text view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initTotalPrice: unable to locate total price group!");
        }
    }

    @Override
    protected void onHandleSuccessReservation(AirFlightDetail activity, Intent intent) {
        bundle = intent.getExtras();
        locateTripId(activity, intent);
        flurryEvents(activity);
        activity.showDialog(BOOKING_SUCCEEDED_DIALOG);
    }

    @Override
    protected void flurryEvents(AirFlightDetail activity) {
        // Flurry Notification
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_AIR);
        Intent launchIntent = activity.getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            params.put(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        EventTracker.INSTANCE.track(Flurry.CATEGORY_BOOK, Flurry.EVENT_NAME_RESERVE, params);
    }

    @Override
    protected CharSequence getBookingSucceededDialogMessage() {
        GovAppMobile app = (GovAppMobile) getApplication();
        if (app.trvlBookingCache.isGroupAuthUsed()) {
            return getText(com.concur.gov.R.string.gov_travel_booking_success_groupauth_msg);
        } else {
            return super.getBookingSucceededDialogMessage();
        }

    }

    @Override
    protected void onBookingSucceeded() {
        super.onBookingSucceeded();
    }

    @Override
    protected void onHandleSuccessItineraryList(Trip trip, TravelBaseActivity activity) {
        selectedTrip = trip;
        if (selectedTrip != null) {
            // set Auth number for future.
            sendItineraryRequest(selectedTrip.itinLocator);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onHandleSuccessItineraryList.selectedTrip==null");
            finish();
        }
    }

    @Override
    protected Intent getSegmentIntent() {
        return new Intent(GovAirFlightDetail.this, GovSegmentList.class);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case BOOK_AIR_DOC: {
            // After successfull booking remove all the cache data
            GovAppMobile app = (GovAppMobile) getApplication();
            app.trvlBookingCache = new TravelBookingCache();
            if (resultCode == RESULT_OK) {
                setResult(resultCode, data);
                finish();
            } else {
                if (selectedTrip != null) {
                    sendItineraryRequest(selectedTrip.itinLocator);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onActivityResult.selectedTrip==null");
                    finish();
                }
            }
            break;
        }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * restore any receiver store for this activity into Retainer.
     * */
    protected void restoreReceivers() {
        super.restoreReceivers();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
    }
}
