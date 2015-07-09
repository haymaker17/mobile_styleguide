package com.concur.mobile.core.expense.travelallowance.controller;

import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;

import java.util.ArrayList;
import java.util.Date;
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

    public ItineraryUpdateController() {
        this.compactItinerary = new CompactItinerary();
        compactItinerary.setSegmentList(new ArrayList<CompactItinerarySegment>());

        this.useMockData = true;
    }

    public void refreshCompactItinerary(String expenseReportName) {
        if (useMockData) {
            ItineraryUpdateTestData testData = new ItineraryUpdateTestData();
            this.compactItinerary = testData.getCompactItinerary();
            return;
        }
        this.compactItinerary = new CompactItinerary();
        compactItinerary.setName(expenseReportName);
        compactItinerary.setSegmentList(new ArrayList<CompactItinerarySegment>());
        CompactItinerarySegment departure = new CompactItinerarySegment();
        departure.setDepartureDateTime(new Date());
        CompactItinerarySegment arrival = new CompactItinerarySegment();
        arrival.setArrivalDateTime(new Date());
        compactItinerary.getSegmentList().add(departure);
        compactItinerary.getSegmentList().add(arrival);
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
        //TODO: Implementation
    }

    public Itinerary getItinerary() {
        Itinerary itinerary = new Itinerary();

        itinerary.setName(compactItinerary.getName());

        //List<CompactItinerarySegment> segmentList = compactItinerary.getSegmentList();


        ItinerarySegment currentItinSegement = null;

        CompactItinerarySegment nextCompactSegment = null;
        int i = 0;
        boolean nextSegmentFinished = false;
        for (CompactItinerarySegment segment : compactItinerary.getSegmentList()) {
            if (i < compactItinerary.getSegmentList().size()) {
                nextCompactSegment = compactItinerary.getSegmentList().get(i+1);
                if (currentItinSegement == null) {
                    currentItinSegement = new ItinerarySegment();
                }

                if (!nextSegmentFinished) {
                    currentItinSegement.setDepartureLocation(segment.getLocation());
                    currentItinSegement.setDepartureDateTime(segment.getDepartureDateTime());
                    currentItinSegement.setBorderCrossDateTime(segment.getBorderCrossingDateTime());
                    currentItinSegement.setArrivalLocation(nextCompactSegment.getLocation());
                    currentItinSegement.setArrivalDateTime(nextCompactSegment.getDepartureDateTime());

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
