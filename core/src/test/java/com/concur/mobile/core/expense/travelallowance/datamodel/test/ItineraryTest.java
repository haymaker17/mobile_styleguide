package com.concur.mobile.core.expense.travelallowance.datamodel.test;

import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by d049515 on 24.07.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)
public class ItineraryTest extends TestCase {

    @Test
    public void cloneTest() {
        Itinerary itin = new Itinerary();
        itin.setName("original");
        itin.setExpenseReportID("originErID");
        itin.setLocked(true);
        itin.setItineraryID("origItinID");

        ItinerarySegment seg = new ItinerarySegment();
        seg.setId("origSegmID");

        List<ItinerarySegment> seglist = new ArrayList<ItinerarySegment>();
        seglist.add(seg);
        itin.setSegmentList(seglist);


        try {
            Itinerary itinClone = (Itinerary) itin.clone();
            assertEquals(itin.getName(), itinClone.getName());
        } catch (CloneNotSupportedException e) {
            fail();
        }

    }
}
