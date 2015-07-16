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

    private boolean useMockData;

    /**
     * Stage for new itineraries
     */
    private CompactItinerary compactItinerary;

    private Context context;

    public ItineraryUpdateController(Context context) {
        this.compactItinerary = new CompactItinerary();
        compactItinerary.setSegmentList(new ArrayList<CompactItinerarySegment>());
        this.context = context;

        this.useMockData = true;
    }

    public void refreshCompactItinerary(String itineraryId) {
        ConcurCore app = (ConcurCore) context.getApplicationContext();
        TravelAllowanceItineraryController controller = app.getTaItineraryController();
        this.compactItinerary = controller.getCompactItinerary(itineraryId);
        if (this.compactItinerary == null) {//Create Mode
            ItineraryUpdateTestData testData = new ItineraryUpdateTestData();
            this.compactItinerary = testData.getCompactItinerary();
        }
    }

    public CompactItinerary getCompactItinerary(){
        return this.compactItinerary;
    }

    public List<CompactItinerarySegment> getCompactItinerarySegments() {
        List<CompactItinerarySegment> segments = null;
        if (this.compactItinerary != null) {
            segments = compactItinerary.getSegmentList();
        }
        return segments;
    }

    public CompactItinerarySegment getCompactItinerarySegment(int position) {
        CompactItinerarySegment segment = null;
        if (compactItinerary != null && compactItinerary.getSegmentList() != null
                && position < this.compactItinerary.getSegmentList().size()) {
            segment = this.compactItinerary.getSegmentList().get(position);
        }
        return segment;
    }

    public void executeSave(String expRepKey) {

        Itinerary itinerary = getItinerary(expRepKey);
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

        SaveItineraryRequest request = new SaveItineraryRequest(context, receiver, getItinerary(expRepKey));
        request.execute();
    }

    public Itinerary getItinerary(String repId) {
        Itinerary itinerary = new Itinerary();

        itinerary.setName(compactItinerary.getName());
        itinerary.setExpenseReportID(repId);
        itinerary.setItineraryID(compactItinerary.getItineraryID());

        //List<CompactItinerarySegment> segmentList = compactItinerary.getSegmentList();


        ItinerarySegment currentItinSegement = null;

        CompactItinerarySegment nextCompactSegment = null;
        int i = 0;
        boolean nextSegmentFinished = false;
        for (CompactItinerarySegment segment : compactItinerary.getSegmentList()) {
            if (i < compactItinerary.getSegmentList().size() - 1) {
                nextCompactSegment = compactItinerary.getSegmentList().get(i+1);

                //Check consistency of compactItinerarySegments
                if ( (segment.getDepartureDateTime() == null && nextCompactSegment.getArrivalDateTime() != null) ||     //Arrival w/o Departure
                     (segment.getDepartureDateTime() != null && nextCompactSegment.getArrivalDateTime() == null)   ){   //Departure w/o Arrival
                    Toast.makeText(context, "@Inconsistent Segment Sequence@", Toast.LENGTH_SHORT).show();
                    return null;
                }
                if (currentItinSegement == null) {
                    currentItinSegement = new ItinerarySegment();
                }

                if (!nextSegmentFinished) {
                    currentItinSegement.setDepartureLocation(segment.getLocation());
                    currentItinSegement.setDepartureDateTime(segment.getDepartureDateTime());
                    currentItinSegement.setBorderCrossDateTime(segment.getBorderCrossingDateTime());
                    if (nextCompactSegment.isSegmentOpen()) {
                        currentItinSegement.setArrivalLocation(nextCompactSegment.getLocation());
                        currentItinSegement.setArrivalDateTime(nextCompactSegment.getArrivalDateTime());
                    } else {
                        currentItinSegement.setArrivalLocation(nextCompactSegment.getLocation());
                        currentItinSegement.setArrivalDateTime(nextCompactSegment.getArrivalDateTime());
                    }

                    itinerary.getSegmentList().add(currentItinSegement);
                    currentItinSegement = null;

                    if (nextCompactSegment.isSegmentOpen()) {
                        nextSegmentFinished = true;
                    } else {
                        nextSegmentFinished = false;
                    }
                } else {
                    nextSegmentFinished = false;
                }
            }

            i++;
        }


        return itinerary;
    }

//    public Itinerary getItinerary() {
//        Itinerary itinerary = new Itinerary();
//
//        itinerary.setName(compactItinerary.getName());
//
//        //List<CompactItinerarySegment> segmentList = compactItinerary.getSegmentList();
//
//        CompactItinerarySegment previousSegment = null;
//
//        ItinerarySegment currentItinSegement = null;
//
//        for (CompactItinerarySegment segment : compactItinerary.getSegmentList()) {
//            if (currentItinSegement == null) {
//                currentItinSegement = new ItinerarySegment();
//            }
//
//            if (previousSegment == null) {
//                currentItinSegement.setDepartureLocation(segment.getLocation());
//                currentItinSegement.setDepartureDateTime(segment.getDepartureDateTime());
//                currentItinSegement.setBorderCrossDateTime(segment.getBorderCrossingDateTime());
//            } else {
//                if (currentItinSegement.getDepartureLocation() == null && currentItinSegement.getArrivalLocation() == null) {
//                    currentItinSegement.setDepartureLocation(previousSegment.getLocation());
//                    currentItinSegement.setDepartureDateTime(previousSegment.getDepartureDateTime());
//                    currentItinSegement.setArrivalLocation(segment.getLocation());
//                    currentItinSegement.setArrivalDateTime(segment.getArrivalDateTime());
//                } else {
//                    currentItinSegement.setArrivalLocation(segment.getLocation());
//                    currentItinSegement.setArrivalDateTime(segment.getArrivalDateTime());
//                }
//
////                currentItinSegement.setBorderCrossDateTime(segment.getBorderCrossingDateTime());
//            }
//
//            if (currentItinSegement.getDepartureLocation() != null && currentItinSegement.getArrivalLocation() != null) {
//                itinerary.getSegmentList().add(currentItinSegement);
//                currentItinSegement = null;
//            }
//
//            previousSegment = segment;
//        }
//
//
//        return itinerary;
//    }

}
