package com.concur.mobile.core.expense.travelallowance.controller;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.service.SaveItineraryRequest;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.service.CoreAsyncRequestTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael Becherer on 03-Jul-15.
 */
public class ItineraryUpdateController {

    public static final String CONTROLLER_TAG_UPDATE = ItineraryUpdateController.class.getName() + ".Update";

    private static final String CLASS_TAG = ItineraryUpdateController.class.getSimpleName();

    /**
     * The stage for itinerary
     */
    private Itinerary itinerary;

    /**
     * The actual itinerary
     */
    private Context context;

    /**
     * The list of registered listeners being notified from this controller as soon as data
     * requests have been answered.
     */
    private List<IServiceRequestListener> listeners;

    public ItineraryUpdateController(Context context) {
        this.itinerary = new Itinerary();
        itinerary.setSegmentList(new ArrayList<ItinerarySegment>());
        this.context = context;
        this.listeners = new ArrayList<IServiceRequestListener>();
    }

    public void refreshItinerary(String itineraryId) {
        ConcurCore app = (ConcurCore) context.getApplicationContext();
        TravelAllowanceItineraryController controller = app.getTaItineraryController();
        this.itinerary = controller.getItinerary(itineraryId);
        if (this.itinerary == null) {//Create Mode
            //TODO QMB: Replace convenience mode based on mock
            ItineraryUpdateTestData testData = new ItineraryUpdateTestData();
            this.itinerary = testData.getItinerary();
        }
    }

    public Itinerary getItinerary(){
        return this.itinerary;
    }

    public List<ItinerarySegment> getItinerarySegments() {
        List<ItinerarySegment> segments = null;
        if (this.itinerary != null) {
            segments = itinerary.getSegmentList();
        }
        return segments;
    }

    public ItinerarySegment getItinerarySegment(int position) {
        ItinerarySegment segment = null;
        if (itinerary != null && itinerary.getSegmentList() != null
                && position < this.itinerary.getSegmentList().size()) {
            segment = this.itinerary.getSegmentList().get(position);
        }
        return segment;
    }

    public void executeUpdate(String expRepKey) {

        if (itinerary == null) {
            return;
        }

        List<ItinerarySegment> periods = itinerary.getSegmentList();
        if (!DateUtils.hasSubsequentDates(false, true, 1, periods)) {
            Toast.makeText(context, "@Dates of this itinerary are not consistent@", Toast.LENGTH_SHORT).show();
            return;
        }

        BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                itinerary = (Itinerary) resultData.getSerializable(CoreAsyncRequestTask.IS_SUCCESS);
                notifyListenerAfterUpdate(false);
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                notifyListenerAfterUpdate(true);
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

    public synchronized void registerListener(IServiceRequestListener listener) {
        listeners.add(listener);
    }

    public synchronized void unregisterListener(IServiceRequestListener listener) {
        listeners.remove(listener);
    }

    private synchronized void notifyListenerAfterUpdate(boolean isFailed) {
        for(IServiceRequestListener listener : listeners) {
            if (isFailed) {
                listener.onRequestFail(CONTROLLER_TAG_UPDATE);
            } else {
                listener.onRequestSuccess(CONTROLLER_TAG_UPDATE);
            }
        }
    }
}
