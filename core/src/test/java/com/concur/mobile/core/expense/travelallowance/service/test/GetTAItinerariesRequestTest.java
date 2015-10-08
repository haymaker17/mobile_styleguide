package com.concur.mobile.core.expense.travelallowance.service.test;

import android.os.Bundle;

import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.service.GetTAItinerariesRequest;
import com.concur.mobile.core.expense.travelallowance.testutils.FileRequestTaskWrapper;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

/**
 * Created by D028778 on 06-Oct-15.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)
public class GetTAItinerariesRequestTest extends TestCase {

    private static final String TEST_DATA_PATH = "src/test/java/com/concur/mobile/core/expense/travelallowance/testdata";

    @Test
    public void parseItinerariesWithTimezoneOffset() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAItinerariesRequest(null, null, null, false));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "ItineraryReadXMLWithTimeZoneOffset.xml");
        assertNotNull(resultData);
        List<Itinerary> itineraries = (List<Itinerary>) resultData.getSerializable(BundleId.ITINERARY_LIST);
        assertEquals(1, itineraries.size());
        assertEquals(2, itineraries.get(0).getSegmentList().size());
        assertEquals(60L, itineraries.get(0).getSegmentList().get(0).getDepartureLocation().getTimeZoneOffset().longValue());
    }
}
