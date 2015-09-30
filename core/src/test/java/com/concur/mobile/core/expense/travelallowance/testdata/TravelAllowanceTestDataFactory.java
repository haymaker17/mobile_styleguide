package com.concur.mobile.core.expense.travelallowance.testdata;

import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.datamodel.LodgingType;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Michael Becherer on 08-Sep-15.
 */
public class TravelAllowanceTestDataFactory {

    /**
     * Creates a round trip itinerary with two segments.
     *
     * @return The round trip itinerary
     */
    public Itinerary createRoundTripItinerary() {
        ItineraryLocation location1 = createItineraryLocation("ItineraryLocation_1", "Heidelberg", "DE", "Germany");
        ItineraryLocation location2 = createItineraryLocation("ItineraryLocation_2", "Seattle", "US", "United States");
        ItinerarySegment segment1 = createItinerarySegment("ItinerarySegment_1", createDate(2015, Calendar.JANUARY, 1, 1, 1),
                location1, createDate(2015, Calendar.JANUARY, 1, 11, 2), location2);
        ItinerarySegment segment2 = createItinerarySegment("ItinerarySegment_2", createDate(2015, Calendar.JANUARY, 2, 1, 1),
                location2, createDate(2015, Calendar.JANUARY, 2, 10, 2), location1);
        List<ItinerarySegment> segments = new ArrayList<ItinerarySegment>();
        segments.add(segment1);
        segments.add(segment2);
        return createItinerary("Itinerary_1", "Heidelberg - Seattle - Heidelberg", segments);
    }

    /**
     * Creates a list of Fixed Travel Allowances
     *
     * @return The list of allowances
     */
    public List<FixedTravelAllowance> createFixedTravelAllowanceList() {
        List<FixedTravelAllowance> allowances = new ArrayList<FixedTravelAllowance>();
        MealProvision notProvided = new MealProvision("NPR", "Not Provided");
        MealProvision provided =  new MealProvision("PRO", "Provided");
        MealProvision business = new MealProvision("BSE", "Business Meal");
        LodgingType hotel = new LodgingType("HOTEL", "Hotel");
        LodgingType motel = new LodgingType("MOTEL", "Motel");
        Calendar cal = Calendar.getInstance();

        for (int i = 1; i <= 15; i++){
            FixedTravelAllowance allowance = new FixedTravelAllowance("FixedTravelAllowance_" + i);
            cal.set(2015, Calendar.JANUARY, i, 0, 0, 0);
            allowance.setDate(new Date(cal.getTimeInMillis()));
            allowance.setAmount(new Double(i));
            allowance.setCurrencyCode("USD");
            allowance.setBreakfastProvision(notProvided);
            allowance.setLunchProvision(notProvided);
            allowance.setDinnerProvision(notProvided);
            allowance.setLocationName("Chicago, IL");
            allowances.add(allowance);
        }

        allowances.get(0).setLodgingType(hotel);
        allowances.get(1).setBreakfastProvision(provided);
        allowances.get(1).setLodgingType(hotel);
        allowances.get(2).setLunchProvision(provided);
        allowances.get(2).setLodgingType(hotel);
        allowances.get(3).setBreakfastProvision(provided);
        allowances.get(3).setLodgingType(hotel);
        allowances.get(3).setLunchProvision(provided);
        allowances.get(4).setDinnerProvision(provided);
        allowances.get(4).setLodgingType(hotel);

        allowances.get(5).setBreakfastProvision(provided);
        allowances.get(5).setDinnerProvision(provided);
        allowances.get(5).setLodgingType(hotel);
        allowances.get(5).setLocationName("San Francisco, CA");
        allowances.get(6).setLunchProvision(provided);
        allowances.get(6).setDinnerProvision(provided);
        allowances.get(6).setLodgingType(hotel);
        allowances.get(6).setLocationName("San Francisco, CA");
        allowances.get(7).setBreakfastProvision(provided);
        allowances.get(7).setLunchProvision(provided);
        allowances.get(7).setDinnerProvision(provided);
        allowances.get(7).setLodgingType(hotel);
        allowances.get(7).setLocationName("San Francisco, CA");

        allowances.get(8).setExcludedIndicator(true);
        allowances.get(8).setLodgingType(motel);
        allowances.get(8).setLocationName("Los Angeles, CA");
        allowances.get(9).setLodgingType(motel);
        allowances.get(9).setBreakfastProvision(provided);
        allowances.get(9).setExcludedIndicator(true);
        allowances.get(9).setLocationName("Los Angeles, CA");

        allowances.get(10).setBreakfastProvision(provided);
        allowances.get(10).setDinnerProvision(business);
        allowances.get(10).setLodgingType(hotel);
        allowances.get(10).setLocationName("Seattle, WA");
        allowances.get(11).setBreakfastProvision(business);
        allowances.get(11).setLodgingType(motel);
        allowances.get(11).setDinnerProvision(provided);
        allowances.get(11).setLocationName("Seattle, WA");
        allowances.get(12).setLunchProvision(provided);
        allowances.get(12).setLodgingType(hotel);
        allowances.get(12).setDinnerProvision(business);
        allowances.get(12).setLocationName("Seattle, WA");

        allowances.get(13).setOvernightIndicator(true);
        allowances.get(13).setBreakfastProvision(provided);
        allowances.get(13).setLunchProvision(business);
        allowances.get(13).setLocationName("New York");
        allowances.get(14).setOvernightIndicator(true);
        allowances.get(14).setBreakfastProvision(business);
        allowances.get(14).setLunchProvision(business);
        allowances.get(14).setDinnerProvision(business);
        allowances.get(14).setLocationName("New York");

        return allowances;
    }

    /**
     * Creates an itinerary with the given parameters
     *
     * @param id The identifier of the itinerary
     * @param name The name of the itinerary
     * @param itinerarySegments The list of itinerary segments
     * @return The created itinerary
     */
    public Itinerary createItinerary(String id, String name,
                                     List<ItinerarySegment> itinerarySegments) {
        Itinerary itinerary = new Itinerary();
        itinerary.setItineraryID(id);
        itinerary.setName(name);
        itinerary.setSegmentList(itinerarySegments);
        return itinerary;
    }

    /**
     * Creates an {@code ItineraryLocation} with the given parameters
     *
     * @param code The coded representation of the {@code ItineraryLocation}
     * @param name The human readable name of the {@code ItineraryLocation}
     * @param countryCode The coded representation of the country of the {@code ItineraryLocation}
     * @param countryName The human readable name of the country of the {@code ItineraryLocation}
     * @return The created {@code ItineraryLocation}
     */
    public ItineraryLocation createItineraryLocation(String code, String name, String countryCode,
                                                     String countryName) {
        ItineraryLocation location = new ItineraryLocation();
        location.setCode(code);
        location.setName(name);
        location.setCountryCode(countryCode);
        location.setCountryName(countryName);
        return location;
    }

    /**
     * Creates a {@code Calendar}. Sets the year, month, day of the month, hour of day
     * and minute fields. Seconds are set to zero. Returns the corresponding {@code Date} object.
     * Note: The month value is 0-based.
     *
     * @param year The year to be set
     * @param month The month to be set. Use constants such as {@code Calendar.March}
     * @param day The day to be set
     * @param hourOfDay The hour to be set
     * @param minute The minute to be set
     * @return The calendar object converted into a {@code Date} object
     */
    public Date createDate(int year, int month, int day, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hourOfDay, minute, 0);
        return calendar.getTime();
    }

    public ItinerarySegment createItinerarySegment(String id,
                                                   Date departureDate,
                                                   ItineraryLocation departureLocation,
                                                   Date arrivalDate,
                                                   ItineraryLocation arrivalLocation) {
        ItinerarySegment itinerarySegment = new ItinerarySegment();
        itinerarySegment.setId(id);
        itinerarySegment.setDepartureDateTime(departureDate);
        itinerarySegment.setDepartureLocation(departureLocation);
        itinerarySegment.setArrivalDateTime(arrivalDate);
        itinerarySegment.setArrivalLocation(arrivalLocation);
        return itinerarySegment;
    }
}
