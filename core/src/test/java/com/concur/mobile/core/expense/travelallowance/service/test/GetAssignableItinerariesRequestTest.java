package com.concur.mobile.core.expense.travelallowance.service.test;

import android.os.Bundle;

import com.concur.mobile.core.expense.travelallowance.datamodel.AssignableItinerary;
import com.concur.mobile.core.expense.travelallowance.service.GetAssignableItinerariesRequest;
import com.concur.mobile.core.expense.travelallowance.testutils.FileRequestTaskWrapper;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;


@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)
public class GetAssignableItinerariesRequestTest extends TestCase {

    private static final String TEST_DATA_PATH = "src/test/java/com/concur/mobile/core/expense/travelallowance/testdata";

    @Test
    public void assignableItinerariesParserTest() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetAssignableItinerariesRequest(null, null, null));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "AssignableItineraries.xml");
        assertNotNull(resultData);
        List<AssignableItinerary> itineraries = (List<AssignableItinerary>) resultData.getSerializable(BundleId.ASSIGNABLE_ITINERARIES);
        assertEquals(4, itineraries.size());
    }
}
