package com.concur.mobile.core.expense.travelallowance.controller;

import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Michael Becherer on 06-Jul-15.
 */
public class ItineraryUpdateTestData {

    private CompactItinerary compactItinerary;

    public ItineraryUpdateTestData() {
        this.compactItinerary = new CompactItinerary();
        compactItinerary.setName("Test Expense Report");

        List<CompactItinerarySegment> segments = new ArrayList<CompactItinerarySegment>();
        CompactItinerarySegment departure = new CompactItinerarySegment();
        CompactItinerarySegment arrival = new CompactItinerarySegment();
        CompactItinerarySegment destination = new CompactItinerarySegment();

        ItineraryLocation location = new ItineraryLocation();
        location.setCode("1234");
        location.setName("@Heidelberg@");
        location.setCountryName("@Germany@");
        location.setCountryCode("DE");

        ItineraryLocation destLocation = new ItineraryLocation();
        destLocation.setCode("4321");
        destLocation.setName("@Seattle@");
        destLocation.setCountryCode("US");
        destLocation.setCountryName("@United States@");

        Calendar calendar = Calendar.getInstance();
        calendar.set(2015, 7, 1, 1, 0);

        departure.setDepartureDateTime(calendar.getTime());
        departure.setLocation(location);
        segments.add(departure);

        calendar.set(2015, 7, 1, 9, 0);
        destination.setArrivalDateTime(calendar.getTime());
        destination.setLocation(destLocation);
        calendar.set(2015, 7, 3, 2, 0);
        destination.setDepartureDateTime(calendar.getTime());
        segments.add(destination);

        calendar.set(2015, 7, 3, 10, 0);
        arrival.setArrivalDateTime(calendar.getTime());
        arrival.setLocation(location);
        segments.add(arrival);

        compactItinerary.setSegmentList(segments);
    }

    public CompactItinerary getCompactItinerary() {
        return compactItinerary;
    }

}
