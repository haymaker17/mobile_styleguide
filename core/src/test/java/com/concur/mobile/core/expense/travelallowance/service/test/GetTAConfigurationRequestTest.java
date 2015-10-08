package com.concur.mobile.core.expense.travelallowance.service.test;

/**
 * Created by D028778 on 06-Oct-15.
 */

import android.os.Bundle;

import com.concur.mobile.core.expense.travelallowance.datamodel.TravelAllowanceConfiguration;
import com.concur.mobile.core.expense.travelallowance.service.GetTAConfigurationRequest;
import com.concur.mobile.core.expense.travelallowance.testutils.FileRequestTaskWrapper;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by D028778 on 06-Oct-15.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)
public class GetTAConfigurationRequestTest extends TestCase {

    private static final String TEST_DATA_PATH = "src/test/java/com/concur/mobile/core/expense/travelallowance/testdata";

    @Test
    public void parseTravelAllowanceConfiguration() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAConfigurationRequest(null, null));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "TAConfigRead.xml");
        assertNotNull(resultData);
        TravelAllowanceConfiguration taConfig = (TravelAllowanceConfiguration) resultData.getSerializable(BundleId.TRAVEL_ALLOWANCE_CONFIGURATION);
        assertEquals("TAC100007", taConfig.getConfigCode());
    }
}
