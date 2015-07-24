package com.concur.mobile.core.expense.travelallowance.controller;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.datamodel.SynchronizationStatus;
import com.concur.mobile.core.expense.travelallowance.service.AbstractItineraryDeleteRequest;
import com.concur.mobile.core.expense.travelallowance.service.DeleteItineraryRequest;
import com.concur.mobile.core.expense.travelallowance.service.GetTAItinerariesRequest;
import com.concur.mobile.core.expense.travelallowance.service.SaveItineraryRequest;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.Message;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

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
public class TravelAllowanceItineraryController extends BaseController {

    public static final String CONTROLLER_TAG = TravelAllowanceItineraryController.class.getName();

    private static final String CLASS_TAG = TravelAllowanceItineraryController.class.getSimpleName();

    private BaseAsyncResultReceiver receiver;

    //private List<IServiceRequestListener> listeners;

    private GetTAItinerariesRequest getItinerariesRequest;

    private Context context;

    private List<Itinerary> itineraryList;

    public TravelAllowanceItineraryController(Context context) {
       // this.listeners = new ArrayList<IServiceRequestListener>();
        this.context = context;
    }



    public void refreshItineraries(String expenseReportKey, boolean isManager) {

        this.itineraryList = new ArrayList<Itinerary>();

        if (getItinerariesRequest != null && getItinerariesRequest.getStatus() != AsyncTask.Status.FINISHED) {
            // There is already an async task which is not finished yet. Return silently and let the task finish his work first.
            return;
        }

        receiver = new BaseAsyncResultReceiver(new Handler());

        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                itineraryList = getItinerariesRequest.getItineraryList();
                notifyListener(ControllerAction.REFRESH, true, resultData);
                Log.d(CLASS_TAG, "Request success.");
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                notifyListener(ControllerAction.REFRESH, false, resultData);
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

//    private synchronized void notifyListener(boolean isFailed) {
//        for(IServiceRequestListener listener : listeners) {
//            if (isFailed) {
//                listener.onRequestFail(CONTROLLER_TAG);
//            } else {
//                listener.onRequestSuccess(CONTROLLER_TAG);
//            }
//        }
//    }

//    public synchronized void registerListener(IServiceRequestListener listener) {
//        listeners.add(listener);
//    }

//    public synchronized void unregisterListener(IServiceRequestListener listener) {
//        listeners.remove(listener);
//    }

    public List<Itinerary> getItineraryList() {
        if (itineraryList == null) {
            return new ArrayList<Itinerary>();
        }
        return itineraryList;
    }

    public CompactItinerary getCompactItinerary(String compactItineraryId) {
        if (StringUtilities.isNullOrEmpty(compactItineraryId)) {
            return null;
        }
        for (CompactItinerary compItinerary : getCompactItineraryList()) {
            if (compactItineraryId.equals(compItinerary.getItineraryID())) {
                return compItinerary;
            }
        }
        return null;
    }

    public Itinerary getItinerary(String itineraryId) {
        if (StringUtilities.isNullOrEmpty(itineraryId)) {
            return null;
        }
        for (Itinerary itinerary : itineraryList) {
            if (itineraryId.equals(itinerary.getItineraryID())) {
                return itinerary;
            }
        }
        return null;
    }

    public List<CompactItinerary> getCompactItineraryList() {
        List<CompactItinerary> result = new ArrayList<CompactItinerary>();

        for (Itinerary itinerary : getItineraryList()) {
            CompactItinerary compactItinerary = new CompactItinerary();
            compactItinerary.setName(itinerary.getName());
            compactItinerary.setItineraryID(itinerary.getItineraryID());
            compactItinerary.setExpenseReportID(itinerary.getExpenseReportID());

            int position = 0;
            for (ItinerarySegment segment : itinerary.getSegmentList()) {

                if (position == 0) {
                    // Treat the very first segment separately because this will be always the open start segment
                    CompactItinerarySegment firstCompactSegment = new CompactItinerarySegment();
                    firstCompactSegment.setLocation(segment.getDepartureLocation());
                    firstCompactSegment.setDepartureDateTime(segment.getDepartureDateTime());
                    firstCompactSegment.setIsSegmentOpen(true);
                    compactItinerary.getSegmentList().add(firstCompactSegment);
                }

                // Get the next itinerary segment if there is one.
                ItinerarySegment nextSegment = null;
                if (position + 1 < itinerary.getSegmentList().size()) {
                    nextSegment = itinerary.getSegmentList().get(position + 1);
                }

                if (nextSegment != null) {
                    // Check the to location of the current segment and the from location of the next segment
                    if (segment.getArrivalLocation().equals(nextSegment.getDepartureLocation())) {
                        // Create a closed compact segment
                        CompactItinerarySegment compactSegment = new CompactItinerarySegment();
                        compactSegment.setLocation(segment.getArrivalLocation());
                        compactSegment.setDepartureDateTime(nextSegment.getDepartureDateTime());
                        compactSegment.setArrivalDateTime(segment.getArrivalDateTime());
                        compactSegment.setBorderCrossingDateTime(segment.getBorderCrossDateTime());
                        compactSegment.setIsSegmentOpen(false);
                        compactItinerary.getSegmentList().add(compactSegment);
                    } else {
                        // Create two open compact segments
                        CompactItinerarySegment compactSegmentA = new CompactItinerarySegment();
                        compactSegmentA.setLocation(segment.getArrivalLocation());
                        compactSegmentA.setArrivalDateTime(segment.getArrivalDateTime());
                        compactSegmentA.setIsSegmentOpen(true);
                        compactItinerary.getSegmentList().add(compactSegmentA);

                        CompactItinerarySegment compactSegmentB = new CompactItinerarySegment();
                        compactSegmentB.setLocation(nextSegment.getDepartureLocation());
                        compactSegmentB.setDepartureDateTime(nextSegment.getDepartureDateTime());
                        compactSegmentB.setBorderCrossingDateTime(nextSegment.getBorderCrossDateTime());
                        compactSegmentB.setIsSegmentOpen(true);
                        compactItinerary.getSegmentList().add(compactSegmentB);
                    }
                }

                if (nextSegment == null) {
                    // Last segment
                    CompactItinerarySegment lastCompactSegment = new CompactItinerarySegment();
                    lastCompactSegment.setArrivalDateTime(segment.getArrivalDateTime());
                    lastCompactSegment.setLocation(segment.getArrivalLocation());
                    lastCompactSegment.setIsSegmentOpen(true);
                    compactItinerary.getSegmentList().add(lastCompactSegment);
                }

                position++;
            }

            result.add(compactItinerary);
        }

        return result;
    }

    public void executeUpdate(Itinerary itinerary) {
        if (itinerary == null) {
            return;
        }

        BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                Itinerary resultItinerary = (Itinerary) resultData.getSerializable(BundleId.ITINERARY);
                setItinerary(resultItinerary);
                if (resultItinerary.getSyncStatus() == SynchronizationStatus.FAILED) {
                    notifyListener(ControllerAction.UPDATE, false, resultData);
                } else {
                    notifyListener(ControllerAction.UPDATE, true, resultData);
                }
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                notifyListener(ControllerAction.UPDATE, false, resultData);
            }

            @Override
            public void onRequestCancel(Bundle resultData) {

            }

            @Override
            public void cleanup() {

            }
        });

        SaveItineraryRequest request = new SaveItineraryRequest(context, receiver, itinerary);
        request.execute();

    }

    /**
     * Adds the itinerary to the #itineraryList in case the passed itinerary doesn't exist yet. Else an existing itinerary will be
     * replaced by the passed itinerary.
     */
    private synchronized void setItinerary(Itinerary itinerary) {

        Itinerary currentItin = null;
        int insertPosition = itineraryList.size();
        for (int i = 0; i < itineraryList.size(); i++) {
            currentItin = itineraryList.get(i);
            if (currentItin.getItineraryID() != null && currentItin.getItineraryID().equals(itinerary.getItineraryID())) {
                insertPosition = i;
            }
        }

        if (insertPosition < itineraryList.size()) {
            itineraryList.set(insertPosition, itinerary);
        } else {
            itineraryList.add(itinerary);
        }
    }

    public boolean checkItinerarySegmentsConsistency(Itinerary itinerary){
        if (itinerary == null){
            return false;
        }
        for (ItinerarySegment segment : itinerary.getSegmentList()){
            if (checkInitialLocations(segment) == true){
                return true;
            }
        }
        return false;
    }

    private boolean checkInitialLocations(ItinerarySegment segment){
        if (segment.getArrivalLocation()   == null || segment.getArrivalLocation().getCode()   == StringUtilities.EMPTY_STRING ||
            segment.getDepartureLocation() == null || segment.getDepartureLocation().getCode() == StringUtilities.EMPTY_STRING    ){
            return  true;
        }else {
            return false;
        }
    }

    public void executeDeleteItinerary(final Itinerary itinerary) {
        BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                boolean isSuccess = resultData.getBoolean(AbstractItineraryDeleteRequest.IS_SUCCESS, false);
                if (isSuccess) {
                    itineraryList.remove(itinerary);
                    notifyListener(ControllerAction.DELETE, true, null);
                } else {
                    Message msg = (Message) resultData
                            .getSerializable(AbstractItineraryDeleteRequest.RESULT_BUNDLE_ID_MESSAGE);
                    if (msg != null) {
                        Itinerary itin = getItinerary(itinerary.getItineraryID());
                        if (itin != null) {
                            itin.setMessage(msg);
                            itin.setSyncStatus(SynchronizationStatus.FAILED);
                        }
                    }
                    notifyListener(ControllerAction.DELETE, false, resultData);
                }
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                Itinerary itin = getItinerary(itinerary.getItineraryID());
                if (itin != null) {
                    Message msg = new Message(Message.Severity.ERROR, "@ Delete Failed @");
                    itin.setMessage(msg);
                    itin.setSyncStatus(SynchronizationStatus.FAILED);
                    resultData.putSerializable(AbstractItineraryDeleteRequest.RESULT_BUNDLE_ID_MESSAGE, msg);
                }
                notifyListener(ControllerAction.DELETE, false, resultData);
            }

            @Override
            public void onRequestCancel(Bundle resultData) {

            }

            @Override
            public void cleanup() {

            }
        });

        DeleteItineraryRequest deleteRequest = new DeleteItineraryRequest(context, receiver, itinerary.getItineraryID());
        deleteRequest.execute();
    }
}
