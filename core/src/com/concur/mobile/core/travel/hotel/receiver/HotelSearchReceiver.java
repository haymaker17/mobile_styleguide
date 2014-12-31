package com.concur.mobile.core.travel.hotel.receiver;

import org.apache.http.HttpStatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.eva.activity.VoiceSearchActivity;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.travel.activity.AbstractTravelSearchProgress;
import com.concur.mobile.core.travel.hotel.activity.HotelSearchProgress;
import com.concur.mobile.core.travel.hotel.service.HotelSearchReply;
import com.concur.mobile.core.util.Const;

/**
 * A broadcast receiver for handling the result of a hotel search.
 * 
 * @author AndrewK
 */
public class HotelSearchReceiver extends BroadcastReceiver {

    public final static String CLS_TAG = HotelSearchReceiver.class.getSimpleName();

    // A reference to the hotel search activity.
    private AbstractTravelSearchProgress activity;

    // A reference to the hotel search request.
    private ServiceRequest request;

    // Contains the intent that was passed to the receiver's 'onReceive' method.
    private Intent intent;

    /**
     * Constructs an instance of <code>HotelSearchReceiver</code> with a search request object.
     * 
     * @param hotelSearch
     */
    public HotelSearchReceiver(AbstractTravelSearchProgress activity) {
        this.activity = activity;
    }

    /**
     * Sets the hotel search activity associated with this broadcast receiver.
     * 
     * @param activity
     *            the hotel search activity associated with this broadcast receiver.
     */
    public void setActivity(HotelSearchProgress activity) {
        this.activity = activity;
        if (this.activity != null) {
            this.activity.setSearchRequest(request);
            if (this.intent != null) {
                // The 'onReceive' method was called prior to the 'setActivity', so process
                // the intent now.
                onReceive(activity.getApplicationContext(), intent);
            }
        }
    }

    /**
     * Sets the hotel search request object associated with this broadcast receiver.
     * 
     * @param request
     *            the hotel search request object associated with this broadcast receiver.
     */
    public void setRequest(ServiceRequest request) {
        this.request = request;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    public void onReceive(Context context, Intent i) {

        // Does this receiver have a current activity?
        if (activity != null) {
            activity.getApplicationContext().unregisterReceiver(this);
            int serviceRequestStatus = i.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
            if (serviceRequestStatus != -1) {
                if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                    int httpStatusCode = i.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                    if (httpStatusCode != -1) {
                        if (httpStatusCode == HttpStatus.SC_OK) {
                            if (i.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                // It may seem odd to have all these different dismissDialogs around but it is intentional
                                // It looks bad to dismiss a dialog (showing the current screen again, potentially for a
                                // noticeable amount of time) and then change or pop something else up. These are layed
                                // out so to minimize or remove that time gap.
                                HotelSearchReply hotelSearchReply = ((ConcurCore) activity.getApplication())
                                        .getHotelSearchResults();
                                if (hotelSearchReply.hotelChoices != null && hotelSearchReply.hotelChoices.size() > 0) {

                                    // Launch the results intent.
                                    activity.startActivityForResult(activity.getResultsIntent(),
                                            Const.REQUEST_CODE_BOOK_HOTEL);

                                    // NOTE: The client doesn't finish the activity here as it will be finished in the
                                    // onActivityResult method within the default clause.

                                } else if (hotelSearchReply.mwsErrorMessage != null) {
                                    // MOB-15804
                                    if (activity instanceof HotelSearchProgress) {
                                        ((HotelSearchProgress) activity)
                                                .showSearchFailedDialog(hotelSearchReply.mwsErrorMessage);
                                    } else if (activity instanceof VoiceSearchActivity) {
                                        ((VoiceSearchActivity) activity)
                                                .showErrorMessage(hotelSearchReply.mwsErrorMessage);
                                    }
                                } else {
                                    // Launch the no results activity.
                                    activity.startActivity(activity.getNoResultsIntent());

                                    // finish.
                                    if (activity.finishActivityOnNoResults()) {
                                        activity.finish();
                                    }
                                }
                            } else {
                                activity.actionStatusErrorMessage = i.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                        + activity.actionStatusErrorMessage + ".");

                                if (activity.actionStatusErrorMessage != null) {
                                    // MOB-15804
                                    if (activity instanceof HotelSearchProgress) {
                                        ((HotelSearchProgress) activity)
                                                .showSearchFailedDialog(activity.actionStatusErrorMessage);
                                    } else if (activity instanceof VoiceSearchActivity) {
                                        ((VoiceSearchActivity) activity)
                                                .showErrorMessage(activity.actionStatusErrorMessage);
                                    }
                                } else {
                                    // Launch the no results activity.
                                    activity.startActivity(activity.getNoResultsIntent());

                                    // finish.
                                    if (activity.finishActivityOnNoResults()) {
                                        activity.finish();
                                    }
                                }
                            }
                        } else {
                            activity.lastHttpErrorMessage = i.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- " + activity.lastHttpErrorMessage
                                    + ".");

                            // Launch the no results activity.
                            activity.startActivity(activity.getNoResultsIntent());

                            // finish.
                            if (activity.finishActivityOnNoResults()) {
                                activity.finish();
                            }
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");

                        // Launch the no results activity.
                        activity.startActivity(activity.getNoResultsIntent());

                        // finish.
                        if (activity.finishActivityOnNoResults()) {
                            activity.finish();
                        }
                    }
                } else {
                    if (request != null && !request.isCanceled()) {
                        Log.e(Const.LOG_TAG,
                                CLS_TAG + ".onReceive: service request error -- "
                                        + i.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));

                        // Launch the no results activity.
                        activity.startActivity(activity.getNoResultsIntent());

                        // finish.
                        if (activity.finishActivityOnNoResults()) {
                            activity.finish();
                        }
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");

                // Launch the no results activity.
                activity.startActivity(activity.getNoResultsIntent());

                // finish.
                if (activity.finishActivityOnNoResults()) {
                    activity.finish();
                }
            }

            // Reset the search request object.
            activity.onReceiveComplete();

        } else {
            // The new activity has not yet been set on the receiver, defer
            // the processing of this intent until then.
            this.intent = i;
        }
    }

}