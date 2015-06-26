package com.concur.mobile.core.expense.travelallowance.controller;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.service.GetTAItinerariesRequest;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TravelAllowanceItineraryController {

    private static final String CLASS_TAG = TravelAllowanceItineraryController.class.getSimpleName();

    private List<IServiceRequestListener> listeners;

    private GetTAItinerariesRequest getItinerariesRequest;

    private Context context;

    private List<Itinerary> itineraryList;

    public TravelAllowanceItineraryController(Context context) {
        this.listeners = new ArrayList<IServiceRequestListener>();
        this.context = context;
    }



    public void refreshItineraries(String expenseReportKey, boolean isManager) {
        if (getItinerariesRequest != null && getItinerariesRequest.getStatus() != AsyncTask.Status.FINISHED) {
            // There is already an async task which is not finished yet. Return silently and let the task finish his work first.
            return;
        }

        BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(
                new Handler());
        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                itineraryList = getItinerariesRequest.getItineraryList();
                notifyListener(false);
                Log.d(CLASS_TAG, "Request success.");
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                notifyListener(true);
                Log.d(CLASS_TAG, "Request failed.");
            }

            @Override
            public void onRequestCancel(Bundle resultData) {
                // Not needed yet.
                return;
            }

            @Override
            public void cleanup() {
                // Not needed yet.
                return;
            }
        });

        getItinerariesRequest = new GetTAItinerariesRequest(context, receiver,
                expenseReportKey, true);

        getItinerariesRequest.execute();
    }

    private synchronized void notifyListener(boolean isFailed) {
        for(IServiceRequestListener listener : listeners) {
            if (isFailed) {
                listener.onRequestFail();
            } else {
                listener.onRequestSuccess();
            }
        }
    }

    public synchronized void registerListener(IServiceRequestListener listener) {
        listeners.add(listener);
    }

    public synchronized void unregisterListener(IServiceRequestListener listener) {
        listeners.remove(listener);
    }

    public List<Itinerary> getItineraryList() {
        if (itineraryList == null) {
            return new ArrayList<Itinerary>();
        }
        return itineraryList;
    }

    public List<CompactItinerary> getCompactItineraryList() {
        List<CompactItinerary> result = new ArrayList<CompactItinerary>();

        for (Itinerary itinerary : getItineraryList()) {
            CompactItinerary compactItinerary = new CompactItinerary();
            compactItinerary.setName(itinerary.getName());

            int position = 0;
            for (ItinerarySegment segment : itinerary.getSegmentList()) {
                CompactItinerarySegment compactSegment = new CompactItinerarySegment();
                if (position == 0) {
                    CompactItinerarySegment firstCompactSegment = new CompactItinerarySegment();
                    firstCompactSegment.setLocation(segment.getDepartureLocation());
                    firstCompactSegment.setDepartureDateTime(segment.getDepartureDateTime());
                    firstCompactSegment.setBorderCrossingDateTime(segment.getBorderCrossDateTime());
                    compactItinerary.getSegmentList().add(firstCompactSegment);
                }

                ItinerarySegment nextSegment = null;
                if (position + 1 < itinerary.getSegmentList().size()) {
                    nextSegment = itinerary.getSegmentList().get(position + 1);
                }

                if (nextSegment != null) {
                    compactSegment.setLocation(segment.getArrivalLocation());
                    compactSegment.setDepartureDateTime(segment.getArrivalDateTime());
                    compactSegment.setArrivalDateTime(nextSegment.getDepartureDateTime());
                    compactSegment.setBorderCrossingDateTime(segment.getBorderCrossDateTime());
                }

                if (nextSegment == null) {
                    // Last segment
                    compactSegment.setDepartureDateTime(segment.getArrivalDateTime());
                    compactSegment.setLocation(segment.getArrivalLocation());
                    compactSegment.setBorderCrossingDateTime(segment.getBorderCrossDateTime());
                }

                compactItinerary.getSegmentList().add(compactSegment);

                position++;
            }

            result.add(compactItinerary);
        }

        return result;
    }
}
