package com.concur.mobile.core.expense.travelallowance.util.test;

import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.util.ItineraryUtils;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Created by D023077 on 17.07.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)

public class ItineraryUtilsTest extends TestCase {

    private ItineraryUtils itineraryUtils;

    private static List<ItineraryLocation> locations = new ArrayList<ItineraryLocation>();

    // Create a list of location objects to be used during test
    @BeforeClass
    public static void classSetup(){
        ItineraryLocation location;

        location = new ItineraryLocation();
        location.setName("Frankfurt");
        location.setCode("0001");
        location.setCountryCode("DE");
        location.setCountryName("Germany");
        locations.add(location);

        location = new ItineraryLocation();
        location.setName("München");
        location.setCode("0002");
        location.setCountryCode("DE");
        location.setCountryName("Germany");
        locations.add(location);

        location = new ItineraryLocation();
        location.setName("New York");
        location.setCode("0003");
        location.setCountryCode("US");
        location.setCountryName("United States of America");
        locations.add(location);

        location = new ItineraryLocation();
        location.setName("Mannheim");
        location.setCode("0004");
        location.setCountryCode("DE");
        location.setCountryName("Germany");
        locations.add(location);

    }

    @Before
    public void setup(){
        itineraryUtils = new ItineraryUtils();
    }

    /** compactItinarary with
     *      Departure
     *      Stop (complete)
     *      Stop (arrival only)
     *      Stop (departure only)
     *      Stop
     *      Arrival
     * Expected Itinerary with 4 segments
     * Both objects are created in parallel, i.e. as soon as a compactItinerarySegment is created
     * the corresponding itinerarySegment is updated and added if appropriate.
     */
    @Test
    public void getItineraryWithCompactItineraryComplexSegment(){

        CompactItinerary compactItinerary = new CompactItinerary();
        Itinerary expectedItinerary = new Itinerary();

        setValueHeader(compactItinerary, expectedItinerary, "1234", "XLV Test Itinerary", "4711");


        List<CompactItinerarySegment> compactItinerarySegments = new ArrayList<CompactItinerarySegment>();
        List<ItinerarySegment> itinerarySegments = new ArrayList<ItinerarySegment>();
        ItinerarySegment itinerarySegment;

        itinerarySegment = new ItinerarySegment();

        // First Stop (Departure)
        CompactItinerarySegment compactItinerarySegment = new CompactItinerarySegment();

        compactItinerarySegment.setLocation(locations.get(0));

        Calendar cal = Calendar.getInstance();
        cal.set(2015, Calendar.JULY, 15);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
//        cal.set(Calendar.MILLISECOND, 0);
        compactItinerarySegment.setDepartureDateTime(cal.getTime());
        compactItinerarySegment.setIsSegmentOpen(true);

        compactItinerarySegments.add(compactItinerarySegment);

        itinerarySegment.setDepartureLocation(compactItinerarySegment.getLocation());
        itinerarySegment.setDepartureDateTime(compactItinerarySegment.getDepartureDateTime());

        //Stop (Arrival and Departure)
        compactItinerarySegment = new CompactItinerarySegment();
        compactItinerarySegment.setLocation(locations.get(1));

        cal.set(2015, Calendar.JULY, 15);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 31);
        compactItinerarySegment.setArrivalDateTime(cal.getTime());

        cal.set(2015, Calendar.JULY, 15);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 11);
        compactItinerarySegment.setDepartureDateTime(cal.getTime());
        compactItinerarySegment.setIsSegmentOpen(false);

        compactItinerarySegments.add(compactItinerarySegment);

        itinerarySegment.setArrivalLocation(compactItinerarySegment.getLocation());
        itinerarySegment.setArrivalDateTime(compactItinerarySegment.getArrivalDateTime());

        itinerarySegments.add(itinerarySegment);

        itinerarySegment = new ItinerarySegment();
        itinerarySegment.setDepartureLocation(compactItinerarySegment.getLocation());
        itinerarySegment.setDepartureDateTime(compactItinerarySegment.getDepartureDateTime());


        //Stop (Arrival)
        compactItinerarySegment = new CompactItinerarySegment();
        compactItinerarySegment.setLocation(locations.get(2));

        cal.set(2015, Calendar.JULY, 15);
        cal.set(Calendar.HOUR_OF_DAY, 16);
        cal.set(Calendar.MINUTE, 22);
        compactItinerarySegment.setArrivalDateTime(cal.getTime());
        compactItinerarySegment.setIsSegmentOpen(true);

        compactItinerarySegments.add(compactItinerarySegment);

        itinerarySegment.setArrivalLocation(compactItinerarySegment.getLocation());
        itinerarySegment.setArrivalDateTime(compactItinerarySegment.getArrivalDateTime());

        itinerarySegments.add(itinerarySegment);
        itinerarySegment = new ItinerarySegment();

        //Stop (Departure)
        compactItinerarySegment = new CompactItinerarySegment();
        compactItinerarySegment.setLocation(locations.get(1));

        cal.set(2015, Calendar.JULY, 16);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 12);
        compactItinerarySegment.setDepartureDateTime(cal.getTime());
        compactItinerarySegment.setIsSegmentOpen(true);

        compactItinerarySegments.add(compactItinerarySegment);

        itinerarySegment.setDepartureLocation(compactItinerarySegment.getLocation());
        itinerarySegment.setDepartureDateTime(compactItinerarySegment.getDepartureDateTime());

        //Stop (Arrival and Departure)
        compactItinerarySegment = new CompactItinerarySegment();
        compactItinerarySegment.setLocation(locations.get(3));

        cal.set(2015, Calendar.JULY, 16);
        cal.set(Calendar.HOUR_OF_DAY, 3);
        cal.set(Calendar.MINUTE, 43);
        compactItinerarySegment.setArrivalDateTime(cal.getTime());

        cal.set(2015, Calendar.JULY, 16);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        compactItinerarySegment.setDepartureDateTime(cal.getTime());
        compactItinerarySegment.setIsSegmentOpen(false);

        compactItinerarySegments.add(compactItinerarySegment);

        itinerarySegment.setArrivalLocation(compactItinerarySegment.getLocation());
        itinerarySegment.setArrivalDateTime(compactItinerarySegment.getArrivalDateTime());

        itinerarySegments.add(itinerarySegment);

        itinerarySegment = new ItinerarySegment();
        itinerarySegment.setDepartureLocation(compactItinerarySegment.getLocation());
        itinerarySegment.setDepartureDateTime(compactItinerarySegment.getDepartureDateTime());

        //Stop (Arrival)
        compactItinerarySegment = new CompactItinerarySegment();
        compactItinerarySegment.setLocation(locations.get(0));

        cal.set(2015, Calendar.JULY, 16);
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 41);
        compactItinerarySegment.setArrivalDateTime(cal.getTime());
        compactItinerarySegment.setIsSegmentOpen(true);

        compactItinerarySegments.add(compactItinerarySegment);

        //Add segment list to CompactItinerary
        compactItinerary.setSegmentList(compactItinerarySegments);

        itinerarySegment.setArrivalLocation(compactItinerarySegment.getLocation());
        itinerarySegment.setArrivalDateTime(compactItinerarySegment.getArrivalDateTime());

        itinerarySegments.add(itinerarySegment);

        expectedItinerary.setSegmentList(itinerarySegments);

        //Call method to be tested and compare results
        Itinerary itinerary = itineraryUtils.getItinerary(compactItinerary);
        assertEquals(expectedItinerary,itinerary);
    }

    @Test
    public void getItineraryNullCompactItineraryNull(){
        CompactItinerary compactItinerary = null;

        Itinerary itinerary = itineraryUtils.getItinerary(compactItinerary);
        assertNull(itinerary); //If compactItinerary is null itinerary should also be null

    }

    @Test
    public void getItineraryWithCompactSegmentNull(){
        CompactItinerary compactItinerary = new CompactItinerary();
        Itinerary expectedItinerary = new Itinerary();

        setValueHeader(compactItinerary, expectedItinerary, "4321", "XLV Test Itinerary 2", "4712");

        Itinerary itinerary = itineraryUtils.getItinerary(compactItinerary);
        assertEquals(expectedItinerary, itinerary);

    }

    private void setValueHeader(CompactItinerary compactItinerary, Itinerary itinerary, String reportID, String name, String itineraryID){
        compactItinerary.setExpenseReportID(reportID);
        itinerary.setExpenseReportID(reportID);

        compactItinerary.setItineraryID(itineraryID);
        itinerary.setItineraryID(itineraryID);

        compactItinerary.setName(name);
        itinerary.setName(name);

    }

}
