package com.concur.mobile.core.expense.travelallowance.util.test;

import com.concur.mobile.core.expense.travelallowance.datamodel.AssignableItinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.util.ItineraryUtils;
import com.concur.mobile.core.expense.travelallowance.util.Message;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    /**
     * To test the straightforward cases
     *
     * Case 1: Tow different segments and two messages with reference to these segemnts.
     * Expected output: These two segments should be found by findMessage method
     *
     * Case 2: A third segment for which no message exists.
     * Expected output: No message should be found.
     */
    @Test
    public void findMessageTest() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2015, Calendar.APRIL, 2, 10, 10);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(2015, Calendar.APRIL, 3, 10, 10);

        Calendar cal3 = Calendar.getInstance();
        cal3.set(2015, Calendar.APRIL, 3, 11, 15);

        ItinerarySegment seg1 = new ItinerarySegment();
        seg1.setDepartureDateTime(cal1.getTime());
        seg1.setArrivalDateTime(cal2.getTime());

        ItinerarySegment seg2 = new ItinerarySegment();
        seg2.setDepartureDateTime(cal2.getTime());
        seg2.setArrivalDateTime(cal3.getTime());

        Message msg1 = new Message(Message.Severity.ERROR, "Test1");
        msg1.setSourceObject(seg1);

        Message msg2 = new Message(Message.Severity.ERROR, "Test2");
        msg2.setSourceObject(seg2);

        List<Message> msgList = new ArrayList<>();
        msgList.add(msg1);
        msgList.add(msg2);

        // Case 1
        Message foundMsg = ItineraryUtils.findMessage(msgList, seg1);
        assertNotNull(foundMsg);
        assertEquals("Test1", foundMsg.getCode());

        foundMsg = ItineraryUtils.findMessage(msgList, seg2);
        assertNotNull(foundMsg);
        assertEquals("Test2", foundMsg.getCode());

        // Case 2
        ItinerarySegment seg3 = new ItinerarySegment();
        seg3.setDepartureDateTime(cal1.getTime());
        seg3.setArrivalDateTime(cal3.getTime());

        foundMsg = ItineraryUtils.findMessage(msgList, seg3);
        assertNull(foundMsg);

    }

    /**
     * To test some error cases with null values
     *
     * Case 1: passed message list is null. Passed segment not null.
     * Expected output: null
     *
     * * Case 2: passed message list is not null. Passed segment is null.
     * Expected output: null
     *
     * Case 3: Arrival date is null and departure date is equal
     * Expected output: No NPE and message should be found.
     *
     * Case 4: Departure date is null and arrival date is equal
     * Expected output: No NPE and message should be found.
     *
     */
    @Test
    public void findMessageNegativeTest() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2015, Calendar.APRIL, 2, 10, 10);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(2015, Calendar.APRIL, 3, 10, 10);

        Calendar cal3 = Calendar.getInstance();
        cal3.set(2015, Calendar.APRIL, 3, 11, 15);

        ItinerarySegment seg1 = new ItinerarySegment();
        seg1.setDepartureDateTime(cal1.getTime());
        seg1.setArrivalDateTime(cal2.getTime());

        ItinerarySegment seg2 = new ItinerarySegment();
        seg2.setDepartureDateTime(cal2.getTime());
        seg2.setArrivalDateTime(cal3.getTime());

        Message msg1 = new Message(Message.Severity.ERROR, "Test1");
        msg1.setSourceObject(seg1);

        Message msg2 = new Message(Message.Severity.ERROR, "Test2");
        msg2.setSourceObject(seg2);

        List<Message> msgList = new ArrayList<>();
        msgList.add(msg1);
        msgList.add(msg2);

        // Case 1
        Message foundMsg = ItineraryUtils.findMessage(null, seg1);
        assertNull(foundMsg);

        // Case 2
        foundMsg = ItineraryUtils.findMessage(msgList, null);
        assertNull(foundMsg);

        // Case 3
        seg1.setArrivalDateTime(null);
        foundMsg = ItineraryUtils.findMessage(msgList, seg1);
        assertNotNull(foundMsg);
        assertEquals("Test1", foundMsg.getCode());

        // Case 4
        seg1.setDepartureDateTime(null);
        foundMsg = ItineraryUtils.findMessage(msgList, seg1);
        assertNotNull(foundMsg);
        assertEquals("Test1", foundMsg.getCode());

    }

    @Test
    public void createLocationStringForAssignableItinerary() {
        AssignableItinerary assignableItinerary = new AssignableItinerary();

        assignableItinerary.addArrivalLocation("Erlangen");
        assignableItinerary.addArrivalLocation("Nürnberg");
        assignableItinerary.addArrivalLocation("Bamberg");
        assignableItinerary.addArrivalLocation("Erlangen");

        assertEquals("Erlangen; Nürnberg; Bamberg; Erlangen", ItineraryUtils.createLocationString(assignableItinerary));
    }

    @Test
    public void createLocationStringForItinerary() {
        Itinerary itinerary = new Itinerary();
        List<ItinerarySegment> segmentList = new ArrayList<ItinerarySegment>();
        ItinerarySegment itinerarySegment; new ItinerarySegment();

        for (ItineraryLocation itLoc: locations ) {
            itinerarySegment =  new ItinerarySegment();
            itinerarySegment.setArrivalLocation(itLoc);
            segmentList.add(itinerarySegment);
        }

        itinerary.setSegmentList(segmentList);

        assertEquals("Frankfurt; München; New York; Mannheim", ItineraryUtils.createLocationString(itinerary));
    }

}
