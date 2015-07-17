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
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael Becherer on 03-Jul-15.
 */
public class ItineraryUpdateController {

    /**
     * The stage for itinerary
     */
    private Itinerary itinerary;

    /**
     * The actual itinerary
     */
    private Context context;

    public ItineraryUpdateController(Context context) {
        this.itinerary = new Itinerary();
        itinerary.setSegmentList(new ArrayList<ItinerarySegment>());
        this.context = context;
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

    public void executeSave(String expRepKey) {

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
                Toast.makeText(context, "@Success@", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                Toast.makeText(context, "@Failed@", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestCancel(Bundle resultData) {
                Toast.makeText(context, "@Canceled@", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void cleanup() {
                Toast.makeText(context, "@Cleanup@", Toast.LENGTH_SHORT).show();
            }
        });

        SaveItineraryRequest request = new SaveItineraryRequest(context, receiver, itinerary);
        request.execute();
    }

}
