package com.concur.mobile.core.expense.travelallowance.datamodel.test;

import com.concur.mobile.core.expense.travelallowance.datamodel.AssignableItinerary;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by D023077 on 17.09.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)

public class AssignableItineraryTest extends TestCase {

    @Test
    public void setterGetterTest(){
        AssignableItinerary assignableItinerary = createAssignableItinerary();

        assertEquals("4711", assignableItinerary.getItineraryID());
        assertEquals("First Itinerary", assignableItinerary.getName());
        assertEquals(referenceDate(0), assignableItinerary.getStartDateTime());
        assertEquals(referenceDate(3), assignableItinerary.getEndDateTime());

        assertNotNull(assignableItinerary.getArrivalLocations());

        assignableItinerary = createAssignableItinerary(); //Create "fresh" instance
        addArrivalLocations(assignableItinerary);
        assertEquals(3, assignableItinerary.getArrivalLocations().size());

    }

    private AssignableItinerary createAssignableItinerary (){
        AssignableItinerary assignableItinerary = new AssignableItinerary();

        assignableItinerary.setItineraryID("4711");
        assignableItinerary.setName("First Itinerary");
        assignableItinerary.setStartDateTime(referenceDate(0));
        assignableItinerary.setEndDateTime((referenceDate(3)));

        return assignableItinerary;

    }

    private void addArrivalLocations(AssignableItinerary aI){

        aI.addArrivalLocation("Erlangen");
        aI.addArrivalLocation("Bamberg");
        aI.addArrivalLocation("Nürnberg");

    }

    private Date referenceDate(int offset){
        int day = 14 + offset;

        Calendar cal = Calendar.getInstance();
        cal.set(2015, Calendar.SEPTEMBER, day, 17, 2, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();

    }

}
