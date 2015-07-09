package com.concur.mobile.core.expense.travelallowance.controller;

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

}
