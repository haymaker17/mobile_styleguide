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
 * This controller is the glue between the backend service layer and the travel allowance itinerary UI.
 * 
 * This controller is instantiated in {@code ConcurCore}. The instance can be referenced via getTaItineraryController().
 * 
 * The #refreshItineraries method start the backend service request task to refresh the itinerary list which is also managed by
 * this controller.
 * 
 * Consumers can register an {@code IServiceRequestListener} to this controller. As soon as the backend service request has done
 * his job all registered listener will be notified. The consumer can afterwards get the refreshed itinerary list via
 * #getItineraryList.
 *
 * The itinerary list UI needs an own UI model. The method #getCompactItineraryList transforms the data model into the UI model.
 * 
 * @author Patricius Komarnicki
 */
public class TravelAllowanceItineraryController {

    public static final String CONTROLLER_TAG = TravelAllowanceItineraryController.class.getName();

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
                expenseReportKey, isManager);

        getItinerariesRequest.execute();
    }

    private synchronized void notifyListener(boolean isFailed) {
        for(IServiceRequestListener listener : listeners) {
            if (isFailed) {
                listener.onRequestFail(CONTROLLER_TAG);
            } else {
                listener.onRequestSuccess(CONTROLLER_TAG);
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
