package com.concur.mobile.core.expense.travelallowance.controller.test;

import android.app.Activity;
import android.os.Bundle;

import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.service.GetTAItinerariesRequest;
import com.concur.mobile.core.expense.travelallowance.testutils.FileRequestTaskWrapper;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.Message;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import testconfig.CoreTestApplication;
import testconfig.CoreTestRunner;

/**
 * Created by D049515 on 14.07.2015.
 */
@Config(application = CoreTestApplication.class, manifest = "AndroidManifest.xml", sdk = 21)
@RunWith(CoreTestRunner.class)
public class TravelAllowanceItineraryControllerTest extends TestCase {

    Date d_04022015_1000, d_04022015_1100,
         d_04052015_1000, d_04052015_1100;

    ItineraryLocation ma, fra;

    private TravelAllowanceItineraryController controller;

    private List<Itinerary> itineraryList;

    private List<CompactItinerary> compactItineraryList;

    private static final String TEST_DATA_PATH = "src/test/java/com/concur/mobile/core/expense/travelallowance/testdata";


    @Before
    public void setup() {
        this.controller = new TravelAllowanceItineraryController(new Activity());
        this.itineraryList = new ArrayList<Itinerary>();
        createDateTimeAndLocations();
    }

    private void createDateTimeAndLocations() {
        // 4/2/2015 10:00
        Calendar cal_04022015_1000 = Calendar.getInstance();
        cal_04022015_1000.set(2015, Calendar.APRIL, 2, 10, 0);
        d_04022015_1000 = cal_04022015_1000.getTime();

        // 4/2/2015 11:00
        Calendar cal_04022015_1100 = Calendar.getInstance();
        cal_04022015_1100.set(2015, Calendar.APRIL, 2, 11, 0);
        d_04022015_1100 = cal_04022015_1100.getTime();

        // 4/5/2015 10:00
        Calendar cal_04052015_1000 = Calendar.getInstance();
        cal_04052015_1000.set(2015, Calendar.APRIL, 5, 10, 0);
        d_04052015_1000 = cal_04052015_1000.getTime();

        // 4/5/2015 11:00
        Calendar cal_04052015_1100 = Calendar.getInstance();
        cal_04052015_1100.set(2015, Calendar.APRIL, 5, 11, 0);
        d_04052015_1100 = cal_04052015_1100.getTime();

        ma = new ItineraryLocation();
        ma.setCode("ma");
        ma.setName("Mannheim");
        ma.setCountryCode("ger");
        ma.setCountryName("Germany");

        fra = new ItineraryLocation();
        fra.setCode("fra");
        fra.setName("Frankfurt");
        fra.setCountryCode("ger");
        fra.setCountryName("Germany");
    }

    private void createSimpleItineraryTestData() {

        final String ITIN_ID = "Test itin 1";
        final String ITIN_NAME = "Test Itin";
        final String REP_ID = "ReportID1";


        Itinerary itinerary = new Itinerary();
        List<ItinerarySegment> segmList = new ArrayList<ItinerarySegment>();
        itinerary.setName(ITIN_NAME);
        itinerary.setExpenseReportID(REP_ID);
        itinerary.setItineraryID(ITIN_ID);

        // Segment A:
        // FRA 4/2/2015 10:00 --> MA 4/2/2015 11:00
        ItinerarySegment segmA = new ItinerarySegment();
        segmA.setId("A");

        // Departure
        segmA.setDepartureLocation(fra);
        segmA.setDepartureDateTime(d_04022015_1000);

        // Arrival
        segmA.setArrivalLocation(ma);
        segmA.setArrivalDateTime(d_04022015_1100);

        segmList.add(segmA);

        // Segment B:
        // MA 4/5/2015 10:00 --> FRA 4/5/2015 11:00
        ItinerarySegment segmB = new ItinerarySegment();
        segmB.setId("B");

        //Departure
        segmB.setDepartureLocation(ma);
        segmB.setDepartureDateTime(d_04052015_1000);

        //Arrival
        segmB.setArrivalLocation(fra);
        segmB.setArrivalDateTime(d_04052015_1100);

        segmList.add(segmB);

        itinerary.setSegmentList(segmList);

        itineraryList.add(itinerary);

       controller.getItineraryList().addAll(itineraryList);


        // Compact Itinerary Test data
        compactItineraryList = new ArrayList<CompactItinerary>();
        CompactItinerary compItin = new CompactItinerary();
        List<CompactItinerarySegment> compSegList = new ArrayList<>();
        compItin.setItineraryID(ITIN_ID);
        compItin.setName(ITIN_NAME);
        compItin.setExpenseReportID(REP_ID);

        // Comp Segment A:
        // FRA 4/2/2015 10:00
        CompactItinerarySegment compSegA = new CompactItinerarySegment();
        compSegA.setLocation(fra);
        compSegA.setDepartureDateTime(d_04022015_1000);
        compSegA.setIsSegmentOpen(true);
        compSegList.add(compSegA);

        // Comp Segment B:
        // MA 4/2/2015 11:00 - 4/5/2015 10:00
        CompactItinerarySegment compSegB = new CompactItinerarySegment();
        compSegB.setLocation(ma);
        compSegB.setArrivalDateTime(d_04022015_1100);
        compSegB.setDepartureDateTime(d_04052015_1000);
        compSegB.setIsSegmentOpen(false);
        compSegList.add(compSegB);

        // Comp Segment C:
        // FRA 4/5/2015 11:00
        CompactItinerarySegment compSegC = new CompactItinerarySegment();
        compSegC.setLocation(fra);
        compSegC.setArrivalDateTime(d_04052015_1100);
        compSegC.setIsSegmentOpen(true);
        compSegList.add(compSegC);

        compItin.setSegmentList(compSegList);
        compactItineraryList.add(compItin);

    }

    @Test
    public void getItineraryListTest() {
        assertNotNull(controller.getItineraryList());
        assertEquals(0, controller.getItineraryList().size());
    }

    /**
     * Test 1 is testing a consistent segment chain with no interruption or open segments in between.
     */
    @Test
    public void getCompactItineraryListTest1() {
        createSimpleItineraryTestData();
        List<CompactItinerary> accualData = controller.getCompactItineraryList();

        assertEquals(1, accualData.size());
        int i = 0;
        for (CompactItinerary acctualCompItin : accualData) {
            CompactItinerary expectedCompItin = compactItineraryList.get(i);
            i++;
            assertEquals(expectedCompItin, acctualCompItin);
        }
    }

    @Test
    public void areAllMandatoryFieldsFilledTestTrue() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAItinerariesRequest(null, null, null, false));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "ItineraryReadXMLWithTimeZoneOffset.xml");
        List<Itinerary> itineraries = (List<Itinerary>) resultData.getSerializable(BundleId.ITINERARY_LIST);
        Itinerary itinerary = itineraries.get(0);
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        TravelAllowanceItineraryController controller = new TravelAllowanceItineraryController(activity);
        boolean result = controller.areAllMandatoryFieldsFilled(itinerary, true);
        assertEquals(true, result);
    }

    @Test
    public void areAllMandatoryFieldsFilledTestFalse() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAItinerariesRequest(null, null, null, false));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "ItineraryReadXMLWithTimeZoneOffset.xml");
        List<Itinerary> itineraries = (List<Itinerary>) resultData.getSerializable(BundleId.ITINERARY_LIST);
        Itinerary itinerary = itineraries.get(0);
        itinerary.getSegmentList().get(0).setArrivalDateTime(null);
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        TravelAllowanceItineraryController controller = new TravelAllowanceItineraryController(activity);
        boolean result = controller.areAllMandatoryFieldsFilled(itinerary, true);
        assertEquals(false, result);
    }

    @Test
    public void hasErrorsTestFalse() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAItinerariesRequest(null, null, null, false));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "ItineraryReadXMLWithTimeZoneOffset.xml");
        List<Itinerary> itineraries = (List<Itinerary>) resultData.getSerializable(BundleId.ITINERARY_LIST);
        Itinerary itinerary = itineraries.get(0);
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        TravelAllowanceItineraryController controller = new TravelAllowanceItineraryController(activity);
        boolean result = controller.hasErrors(itinerary);
        assertEquals(false, result);
    }

    @Test
    public void hasErrorsTestTrue() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAItinerariesRequest(null, null, null, false));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "ItineraryReadXMLWithTimeZoneOffset.xml");
        List<Itinerary> itineraries = (List<Itinerary>) resultData.getSerializable(BundleId.ITINERARY_LIST);
        Itinerary itinerary = itineraries.get(0);
        itinerary.getSegmentList().get(0).setMessage(new Message(Message.Severity.ERROR, Message.MSG_UI_MISSING_DATES));
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        TravelAllowanceItineraryController controller = new TravelAllowanceItineraryController(activity);
        boolean result = controller.hasErrors(itinerary);
        assertEquals(true, result);
    }

    @Test
    public void resetMessagesTest() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAItinerariesRequest(null, null, null, false));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "ItineraryReadXMLWithTimeZoneOffset.xml");
        List<Itinerary> itineraries = (List<Itinerary>) resultData.getSerializable(BundleId.ITINERARY_LIST);
        Itinerary itinerary = itineraries.get(0);
        itinerary.getSegmentList().get(0).setMessage(new Message(Message.Severity.ERROR, Message.MSG_UI_MISSING_DATES));
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        TravelAllowanceItineraryController controller = new TravelAllowanceItineraryController(activity);
        controller.resetMessages(itinerary);
        Message resultMsg = itinerary.getSegmentList().get(0).getMessage();
        assertNull(resultMsg);
    }

    @Test
    public void areDatesOverlappingTestFalse() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAItinerariesRequest(null, null, null, false));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "ItineraryReadXMLWithTimeZoneOffset.xml");
        List<Itinerary> itineraries = (List<Itinerary>) resultData.getSerializable(BundleId.ITINERARY_LIST);
        Itinerary itinerary = itineraries.get(0);
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        TravelAllowanceItineraryController controller = new TravelAllowanceItineraryController(activity);
        boolean result = controller.areDatesOverlapping(itinerary, true);
        assertEquals(false, result);
    }

    @Test
    public void areDatesOverlappingTestTrue1() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAItinerariesRequest(null, null, null, false));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "ItineraryReadXMLWithTimeZoneOffset.xml");
        List<Itinerary> itineraries = (List<Itinerary>) resultData.getSerializable(BundleId.ITINERARY_LIST);
        Itinerary itinerary = itineraries.get(0);
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTime(itinerary.getSegmentList().get(0).getArrivalDateTime());
        cal.add(Calendar.MONTH, -1);
        itinerary.getSegmentList().get(1).setDepartureDateTime(cal.getTime());
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        TravelAllowanceItineraryController controller = new TravelAllowanceItineraryController(activity);
        boolean result = controller.areDatesOverlapping(itinerary, true);
        assertEquals(true, result);
    }

    @Test
    public void areDatesOverlappingTestTrue2() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAItinerariesRequest(null, null, null, false));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "ItineraryReadXMLWithTimeZoneOffset.xml");
        List<Itinerary> itineraries = (List<Itinerary>) resultData.getSerializable(BundleId.ITINERARY_LIST);
        Itinerary itinerary = itineraries.get(0);
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTime(itinerary.getSegmentList().get(0).getArrivalDateTime());
        cal.add(Calendar.MONTH, 1);
        itinerary.getSegmentList().get(0).setDepartureDateTime(cal.getTime());
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        TravelAllowanceItineraryController controller = new TravelAllowanceItineraryController(activity);
        boolean result = controller.areDatesOverlapping(itinerary, true);
        assertEquals(true, result);
    }

    @Test
    public void resetSegmentMessageTest() {
        Message resultMsg = null;
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAItinerariesRequest(null, null, null, false));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "ItineraryReadXMLWithTimeZoneOffset.xml");
        List<Itinerary> itineraries = (List<Itinerary>) resultData.getSerializable(BundleId.ITINERARY_LIST);
        Itinerary itinerary = itineraries.get(0);
        itinerary.getSegmentList().get(0).setMessage(new Message(Message.Severity.ERROR, Message.MSG_UI_OVERLAPPING_SUCCESSOR));
        itinerary.getSegmentList().get(1).setMessage(new Message(Message.Severity.ERROR, Message.MSG_UI_OVERLAPPING_PREDECESSOR));
        TravelAllowanceItineraryController controller = new TravelAllowanceItineraryController(activity);
        controller.resetSegmentMessage(itinerary, 0);
        resultMsg = itinerary.getSegmentList().get(0).getMessage();
        assertNull(resultMsg);
        resultMsg = itinerary.getSegmentList().get(1).getMessage();
        assertNull(resultMsg);
        itinerary.getSegmentList().get(0).setMessage(new Message(Message.Severity.ERROR, Message.MSG_UI_OVERLAPPING_SUCCESSOR));
        itinerary.getSegmentList().get(1).setMessage(new Message(Message.Severity.ERROR, Message.MSG_UI_OVERLAPPING_PREDECESSOR));
        controller.resetSegmentMessage(itinerary, 1);
        resultMsg = itinerary.getSegmentList().get(0).getMessage();
        assertNull(resultMsg);
        resultMsg = itinerary.getSegmentList().get(1).getMessage();
        assertNull(resultMsg);
        itinerary.getSegmentList().get(0).setMessage(new Message(Message.Severity.ERROR, Message.MSG_UI_MISSING_DATES));
        itinerary.getSegmentList().get(1).setMessage(new Message(Message.Severity.ERROR, Message.MSG_UI_MISSING_DATES));
        controller.resetSegmentMessage(itinerary, 1);
        resultMsg = itinerary.getSegmentList().get(0).getMessage();
        assertNotNull(resultMsg);
        resultMsg = itinerary.getSegmentList().get(1).getMessage();
        assertNull(resultMsg);
    }

    @Test
    public void getSegmentPositionById() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAItinerariesRequest(null, null, null, false));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "ItineraryReadXMLWithTimeZoneOffset.xml");
        List<Itinerary> itineraries = (List<Itinerary>) resultData.getSerializable(BundleId.ITINERARY_LIST);
        Itinerary itinerary = itineraries.get(0);
        int pos = controller.getSegmentPositionById(itinerary, "gWlC24nTF3TceHtrT5LoTXK0oJv$spVlA");
        assertEquals(0, pos);
    }
}
