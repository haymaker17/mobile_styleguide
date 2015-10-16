package com.concur.mobile.core.expense.travelallowance.datamodel.test;

import com.concur.mobile.core.expense.travelallowance.datamodel.CodeListManager;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by D023077 on 16.09.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)

public class CodeListManagerTest extends TestCase {
    private CodeListManager codeListManager = CodeListManager.getInstance();

    @Before
    public void setup() {
        codeListManager.clearAll();
    }

    @Test
    public void updateItineraryLocation (){
        ItineraryLocation itLoc = createItineraryLocation();

        assertNull(codeListManager.updateItineraryLocation(null));

        assertEquals(itLoc, codeListManager.updateItineraryLocation(itLoc));
    }

    /**
     * Create at least two entries in the manager
     * get all entries
     * get the last entry
     * delete all entries
     */
    @Test
    public void clearAll(){
        ItineraryLocation itLoc = createItineraryLocation();

        codeListManager.updateItineraryLocation(itLoc);

        itLoc.setCode("LocCode42");
        itLoc.setName("Location42");

        codeListManager.updateItineraryLocation(itLoc);

        assertEquals(2, codeListManager.getItineraryLocations().size());

        assertTrue(itLoc.equals(codeListManager.getItineraryLocation("LocCode42")));
        assertNull(codeListManager.getItineraryLocation(null));

        codeListManager.clearAll();
        assertEquals(0, codeListManager.getItineraryLocations().size());

    }

    @Test
    public void getItineraryLocations(){

    }

    private ItineraryLocation createItineraryLocation(){
        Long defaultTimeZoneOffset = Long.valueOf(9);
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
