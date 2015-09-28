package com.concur.mobile.core.expense.travelallowance.datamodel.test;

import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.util.Message;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by D023077 on 24.07.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)

public class ItinerarySegmentTest extends TestCase{

    private ItinerarySegment itinSegmBase;

    private static final int EQUAL =  0;
    private static final int LESS  = -1;
    private static final int MORE  =  1;

    @Before
    public void setup(){
        itinSegmBase = new ItinerarySegment();

        Calendar cal = Calendar.getInstance();
        cal.set(2015, Calendar.JULY, 15);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 15);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        itinSegmBase.setDepartureDateTime(cal.getTime());
    }

    /**
     * The departure of the base object is the same as of the test object, so expect 0
     */
    @Test
    public void compareToEqual(){
        Calendar cal = Calendar.getInstance();
        cal.set(2015, Calendar.JULY, 15);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 15);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        ItinerarySegment itinSegmComp = new ItinerarySegment();
        itinSegmComp.setDepartureDateTime(cal.getTime());

        assertEquals(EQUAL, itinSegmBase.compareTo(itinSegmComp));
    }

    /**
     * The departure of the base object is before the test object, so expect -1
     */
    @Test
    public void compareToLater(){
        Calendar cal = Calendar.getInstance();
        cal.set(2015, Calendar.JULY, 16);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 15);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        ItinerarySegment itinSegmComp = new ItinerarySegment();
        itinSegmComp.setDepartureDateTime(cal.getTime());

        assertEquals(LESS, itinSegmBase.compareTo(itinSegmComp));
    }

    /**
     * The departure of the base object is after the test object, so expect 1
     */
    @Test
    public void compareToBefore(){
        Calendar cal = Calendar.getInstance();
        cal.set(2015, Calendar.JULY, 14);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 15);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        ItinerarySegment itinSegmComp = new ItinerarySegment();
        itinSegmComp.setDepartureDateTime(cal.getTime());

        assertEquals(MORE, itinSegmBase.compareTo(itinSegmComp));

    }

    @Test
    public void compareToNullSegment(){
        ItinerarySegment itinSegmComp = null;

        assertEquals(LESS, itinSegmBase.compareTo(itinSegmComp));
    }

    @Test
    public void compareToBothDeparturesEqualsNull(){

        itinSegmBase.setDepartureDateTime(null);

        ItinerarySegment itinSegmComp = new ItinerarySegment();

        assertEquals(EQUAL, itinSegmBase.compareTo(itinSegmComp));
    }

    @Test
    public void compareToCompDepartureEqualsNull(){
        ItinerarySegment itinSegmComp = new ItinerarySegment();

        assertEquals(MORE, itinSegmBase.compareTo(itinSegmComp));
    }

    @Test
    public void compareToBaseDepartureEqualsNull(){
        ItinerarySegment itinSegmComp = new ItinerarySegment();
        itinSegmComp.setDepartureDateTime(itinSegmBase.getDepartureDateTime());
        itinSegmBase.setDepartureDateTime(null);

        assertEquals(LESS, itinSegmBase.compareTo(itinSegmComp));
    }

    @Test
    public void hashCodeTest(){
        assertEquals(613445870, itinSegmBase.hashCode());
    }

    @Test
    public void toStringTest(){
        assertEquals("ItinerarySegment{arrivalDateTime=null, departureDateTime=Wed Jul 15 08:15:00 CEST 2015, id='null'}", itinSegmBase.toString());
    }

    @Test
    public void id(){
        String id = "XLV_0815";
        itinSegmBase.setId(id);
        assertEquals(id, itinSegmBase.getId());
    }

    @Test
    public void setGetMessage(){

        Message message = new Message(Message.Severity.ERROR, "XLV_Code");
        itinSegmBase.setMessage(message);
        assertEquals(message, itinSegmBase.getMessage());
    }

    @Test
    public void isLocked(){
        itinSegmBase.setLocked(true);
        assertEquals(true, itinSegmBase.isLocked());
    }

    @Test
    public void fieldEnum(){
        assertEquals("Id", ItinerarySegment.Field.ID.getName());
    }

    @Test
    public void getEndDateUTC() {
        Date date;
        date = null;
        assertEquals(date, itinSegmBase.getEndDateUTC());
    }
}
