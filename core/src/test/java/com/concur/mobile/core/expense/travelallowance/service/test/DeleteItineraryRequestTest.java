package com.concur.mobile.core.expense.travelallowance.service.test;

import android.os.Bundle;

import com.concur.mobile.core.expense.travelallowance.service.DeleteItineraryRequest;
import com.concur.mobile.core.expense.travelallowance.testutils.FileRequestTaskWrapper;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by D028778 on 07-Oct-15.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)
public class DeleteItineraryRequestTest extends TestCase {

    private static final String TEST_DATA_PATH = "src/test/java/com/concur/mobile/core/expense/travelallowance/testdata";

    @Test
    public void parseDeleteItinerary() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new DeleteItineraryRequest(null, null, null));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "DeleteItinerarySuccess.xml");
        assertNotNull(resultData);
        boolean isSuccess = resultData.getBoolean(BundleId.IS_SUCCESS);
        assertEquals(true, isSuccess);
    }
}