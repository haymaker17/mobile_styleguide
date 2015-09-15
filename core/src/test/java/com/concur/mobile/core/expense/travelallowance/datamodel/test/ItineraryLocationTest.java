package com.concur.mobile.core.expense.travelallowance.datamodel.test;

import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by D023077 on 15.09.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)

public class ItineraryLocationTest extends TestCase {

    private Long defaultTimeZoneOffset = Long.valueOf(11);

    @Test
    public void setterGetterTest() {
        ItineraryLocation itineraryLocation = createItineraryLocation();

        assertEquals("LocCode", itineraryLocation.getCode());
        assertEquals("DE", itineraryLocation.getCountryCode());
        assertEquals("Germany", itineraryLocation.getCountryName());
        assertEquals("XLV Location", itineraryLocation.getName());
        assertEquals("RLKey", itineraryLocation.getRateLocationKey());
        assertEquals(defaultTimeZoneOffset, itineraryLocation.getTimeZoneOffset());

    }

    @Test
    public void equalsTest(){
        ItineraryLocation itineraryLocationBase = createItineraryLocation();

        ItineraryLocation itineraryLocationComp;
        itineraryLocationComp = null;
        Object o = new Object();

        assertTrue(itineraryLocationBase.equals( itineraryLocationBase));

        assertFalse(itineraryLocationBase.equals(itineraryLocationComp));
        assertFalse((itineraryLocationBase.equals(o)));

        itineraryLocationComp = createItineraryLocation();
        assertTrue(itineraryLocationBase.equals(itineraryLocationComp));

        itineraryLocationComp.setCode("LocCode2");
        assertFalse(itineraryLocationBase.equals(itineraryLocationComp));
        itineraryLocationComp.setCode("LocCode");

        itineraryLocationComp.setCountryCode("US");
        assertFalse(itineraryLocationBase.equals(itineraryLocationComp));
        itineraryLocationComp.setCountryCode("DE");

        itineraryLocationComp.setCountryName("USA");
        assertFalse(itineraryLocationBase.equals(itineraryLocationComp));
        itineraryLocationComp.setCountryName("Germany");

        itineraryLocationComp.setName("VEX Location");
        assertFalse(itineraryLocationBase.equals(itineraryLocationComp));
        itineraryLocationComp.setName("XLV Location");

        itineraryLocationComp.setRateLocationKey("RLKey2");
        assertFalse(itineraryLocationBase.equals(itineraryLocationComp));
        itineraryLocationComp.setRateLocationKey("RLKey");

        itineraryLocationComp.setTimeZoneOffset((defaultTimeZoneOffset) - 1);
        assertFalse(itineraryLocationBase.equals(itineraryLocationComp));
        itineraryLocationComp.setTimeZoneOffset(defaultTimeZoneOffset);

    }

    @Test
    public void hashCodeTest(){
        ItineraryLocation itineraryLocation = createItineraryLocation();

        assertEquals(526643666, itineraryLocation.hashCode());
    }

    @Test
    public void cloneTest(){
        ItineraryLocation itineraryLocationSource = createItineraryLocation();

        ItineraryLocation itineraryLocationTarget = itineraryLocationSource.clone();

        assertTrue(itineraryLocationSource.equals(itineraryLocationTarget));

    }

    private ItineraryLocation createItineraryLocation(){
        ItineraryLocation itLoc = new ItineraryLocation();

        itLoc.setCode("LocCode");
        itLoc.setCountryCode("DE");
        itLoc.setCountryName("Germany");
        itLoc.setName("XLV Location");
        itLoc.setRateLocationKey("RLKey");
        itLoc.setTimeZoneOffset(defaultTimeZoneOffset);

        return itLoc;
    }

}
