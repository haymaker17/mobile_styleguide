package com.concur.mobile.core.expense.travelallowance.controller;

import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Michael Becherer on 06-Jul-15.
 */
public class ItineraryUpdateTestData {

    private Itinerary itinerary;

    public ItineraryUpdateTestData() {
        this.itinerary = new Itinerary();
        itinerary.setName("02.06.15 Claim");

        List<ItinerarySegment> segments = new ArrayList<ItinerarySegment>();
        ItinerarySegment segment1 = new ItinerarySegment();
        ItinerarySegment segment2 = new ItinerarySegment();

        ItineraryLocation location1 = new ItineraryLocation();
        location1.setCode("1234");
        location1.setName("@Heidelberg@");
        location1.setCountryName("@Germany@");
        location1.setCountryCode("DE");

        ItineraryLocation location2 = new ItineraryLocation();
        location2.setCode("4321");
        location2.setName("@Seattle@");
        location2.setCountryCode("US");
        location2.setCountryName("@United States@");

        Calendar departureCalendar = Calendar.getInstance();
        Calendar arrivalCalendar = Calendar.getInstance();

        departureCalendar.set(2015, 7, 1, 1, 0);
        arrivalCalendar.set(2015, 7, 1, 9, 0);
        segment1.setDepartureDateTime(departureCalendar.getTime());
        segment1.setDepartureLocation(location1);
        segment1.setArrivalLocation(location2);
        segment1.setArrivalDateTime(arrivalCalendar.getTime());
        segments.add(segment1);

        departureCalendar.set(2015, 7, 3, 21, 0);
        arrivalCalendar.set(2015, 7, 4, 4, 0);

        segment2.setDepartureDateTime(departureCalendar.getTime());
        segment2.setDepartureLocation(location2);
        segment2.setArrivalLocation(location1);
        segment2.setArrivalDateTime(arrivalCalendar.getTime());
        segments.add(segment2);

        itinerary.setSegmentList(segments);
    }

    public Itinerary getItinerary() {
        return itinerary;
    }

}
