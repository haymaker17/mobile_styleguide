package com.concur.mobile.core.travel.hotel.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.dialog.AlertDialogFragment;
import com.concur.mobile.core.travel.activity.SegmentList;
import com.concur.mobile.core.travel.service.CancelSegment;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>AsyncReplyListener</code> for the purposes of handling the results of cancelling Hotel segment.
 * 
 * @author TejoA
 */

public class HotelCancelSegmentListerner implements AsyncReplyListener {

    public AlertDialogFragment hotelCancelledSucessFrag;
    public AlertDialogFragment hotelCancelledFailureFrag;
    public BaseAsyncResultReceiver hotelCancelReceiver;
    public String segmentCancelErrorMessage;
    private String cancellationNumber;
    // public boolean isEntireTripCancelled;
    private String itineraryLocator;
    protected android.support.v4.app.FragmentManager fm;

    /**
     * @param itineraryLocator
     *            the itineraryLocator to set
     */
    public void setItineraryLocator(String itineraryLocator) {
        this.itineraryLocator = itineraryLocator;
    }

    /**
     * @param fm
     *            the FragmentManager to set
     */
    public void setFm(android.support.v4.app.FragmentManager fm) {
        this.fm = fm;
    }

    @Override
    public void onRequestSuccess(Bundle resultData) {

        boolean isSuccess = resultData.getBoolean(CancelSegment.IS_SUCCESS);

        if (isSuccess) {
            // isEntireTripCancelled = resultData.getBoolean(CancelSegment.IS_ENTIRE_TRIP_CANCELLED);
            cancellationNumber = resultData.getString(CancelSegment.CANCELLATION_NUMBER);
            showHotelCancelledSucessFrag();
        } else {
            segmentCancelErrorMessage = resultData.getString(CancelSegment.ERROR_MESSAGE);
            Log.e(Const.LOG_TAG, "CancelSegmentListener.onRequestSuccess - " + segmentCancelErrorMessage);
            // show the error message
            showHotelCancelledFailureFrag();

        }
    }

    @Override
    public void onRequestFail(Bundle resultData) {
        showHotelCancelledFailureFrag();
    }

    @Override
    public void cleanup() {
        hotelCancelReceiver = null;
    }

    public void showHotelCancelledSucessFrag() {
        hotelCancelledSucessFrag = new AlertDialogFragment();
        hotelCancelledSucessFrag.setTitle(R.string.dlg_hotel_cancel_succeeded_title);
        if (cancellationNumber != null) {
            hotelCancelledSucessFrag.setMessage(com.concur.mobile.base.util.Format.localizeText(
                    ConcurCore.getContext(), R.string.dlg_hotel_cancel_succeeded_message_Cancellation_No,
                    new Object[] { cancellationNumber }));
        } else {
            hotelCancelledSucessFrag.setMessage(R.string.dlg_hotel_cancel_succeeded_message);
        }
        hotelCancelledSucessFrag.setPositiveButtonText(R.string.okay);
        hotelCancelledSucessFrag.setPositiveButtonListener(new AlertDialogFragment.OnClickListener() {

            @Override
            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                // Set the result to be 'OK'.
                activity.setResult(Activity.RESULT_OK);
                if (activity instanceof SegmentList) {
                    // refresh the itinerary
                    ((SegmentList) activity).sendItineraryRequest(itineraryLocator);
                } else {
                    // Just finish the activity.
                    activity.finish();
                }

                dialog.dismiss();
            }

            @Override
            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            }
        });
        hotelCancelledSucessFrag.show(fm, null);
    }

    private void showHotelCancelledFailureFrag() {
        hotelCancelledFailureFrag = new AlertDialogFragment();

        hotelCancelledFailureFrag.setTitle(R.string.dlg_hotel_cancel_failed_title);
        hotelCancelledFailureFrag.setMessage(segmentCancelErrorMessage);
        hotelCancelledFailureFrag.setPositiveButtonText(R.string.okay);
        hotelCancelledFailureFrag.setPositiveButtonListener(new AlertDialogFragment.OnClickListener() {

            @Override
            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                dialog.dismiss();
            }

            @Override
            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            }
        });
        hotelCancelledFailureFrag.show(fm, null);

    }

    @Override
    // no cancellation allowed
    public void onRequestCancel(Bundle resultData) {
    }

}
