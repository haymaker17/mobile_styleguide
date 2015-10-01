package com.concur.mobile.core.expense.travelallowance.datamodel.test;

import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.datamodel.SynchronizationStatus;
import com.concur.mobile.core.expense.travelallowance.util.Message;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

/**
 * Created by D023077 on 15.09.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)

public class ItineraryTest extends TestCase {

    private Message testMessage  = new Message(Message.Severity.ERROR, "Test");
    private Message testMessage2 = new Message(Message.Severity.INFO, "Test warning");
    private ItinerarySegment testSegment = new ItinerarySegment();

    @Test
    public void setterGetterTest() {
        Itinerary itinerary = createItinerary();

        assertEquals("Itinerary", itinerary.getName());
        assertTrue(itinerary.isLocked());
        assertEquals("4713", itinerary.getExpenseReportID());
        assertEquals("0815", itinerary.getItineraryID());
        assertEquals(testMessage, itinerary.getMessage());
        assertEquals(new ArrayList<ItinerarySegment>(), itinerary.getSegmentList());
        assertEquals(SynchronizationStatus.SYNCHRONIZED, itinerary.getSyncStatus());
        assertNull(itinerary.getSegment("123"));

    }

    @Test
    public void equalsTest(){
        Itinerary itineraryBase = createItinerary();

        Itinerary itineraryComp;
        itineraryComp = null;
        Object o = new Object();

        assertTrue(itineraryBase.equals(itineraryBase));

        assertFalse(itineraryBase.equals(itineraryComp));
        assertFalse(itineraryBase.equals(o));

        itineraryComp = createItinerary();
        assertTrue(itineraryBase.equals(itineraryComp));

        itineraryComp.setName("XLV Itinerary");
        assertFalse(itineraryBase.equals(itineraryComp));
        itineraryComp.setName("Itinerary");

        itineraryComp.setLocked(false);
        assertFalse(itineraryBase.equals(itineraryComp));
        itineraryComp.setLocked(true);

        itineraryComp.setExpenseReportID("4742");
        assertFalse(itineraryBase.equals(itineraryComp));
        itineraryComp.setExpenseReportID("4713");

        itineraryComp.setItineraryID("0842");
        assertFalse(itineraryBase.equals(itineraryComp));
        itineraryComp.setItineraryID("0815");

        //Currently message isn't part of the comparison
//        itineraryComp.setMessage(testMessage2);
//        assertFalse(itineraryBase.equals(itineraryComp));
//        itineraryComp.setMessage(testMessage);

        itineraryComp.setSyncStatus(SynchronizationStatus.PENDING);
        assertFalse(itineraryBase.equals(itineraryComp));
        itineraryComp.setSyncStatus(SynchronizationStatus.SYNCHRONIZED);
    }

    @Test
    public void hashCodeTest(){
        Itinerary it = createItinerary();
        it.setSyncStatus(null); //hashCode of SyncStatus seems to be unstable
        assertEquals(-1633270713, it.hashCode());
    }

    @Test
    public void toStringTest(){
        assertEquals("Itinerary{itineraryID='0815', name='Itinerary'}", createItinerary().toString());
    }

    @Test
    public void field(){
        assertEquals("itineraryID", Itinerary.Field.ID.getName());
        assertEquals("name", Itinerary.Field.NAME.getName());
        assertEquals("expenseReportID", Itinerary.Field.EXPENSE_REPORT_ID.getName());
        assertEquals("locked", Itinerary.Field.LOCKED.getName());
    }

    private Itinerary createItinerary(){
        Itinerary it = new Itinerary();

        it.setName("Itinerary");
        it.setLocked(true);
        it.setExpenseReportID("4713");
        it.setItineraryID("0815");
        it.setMessage(testMessage);
//        it.setSegmentList();
        it.setSyncStatus(SynchronizationStatus.SYNCHRONIZED);

        return it;
    }

}