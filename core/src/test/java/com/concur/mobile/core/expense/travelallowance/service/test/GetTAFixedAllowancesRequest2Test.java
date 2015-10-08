package com.concur.mobile.core.expense.travelallowance.service.test;

import android.os.Bundle;

import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceControlData;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.service.GetTAFixedAllowancesRequest2;
import com.concur.mobile.core.expense.travelallowance.testutils.FileRequestTaskWrapper;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

/**
 * Created by D028778 on 07-Oct-15.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)
public class GetTAFixedAllowancesRequest2Test extends TestCase {

    private static final String TEST_DATA_PATH = "src/test/java/com/concur/mobile/core/expense/travelallowance/testdata";

    @Test
    public void parseFixedAllowances() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAFixedAllowancesRequest2(null, null, null));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "FixedAllowances.xml");
        assertNotNull(resultData);
        FixedTravelAllowanceControlData controlData = (FixedTravelAllowanceControlData) resultData.getSerializable(BundleId.FIXED_TRAVEL_ALLOWANCE_CONTROL_DATA);
        assertEquals(true, controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_PICKLIST));
        assertEquals("Overnight", controlData.getLabel(FixedTravelAllowanceControlData.OVERNIGHT_LABEL));
        List <FixedTravelAllowance> fixedTravelAllowances = (List<FixedTravelAllowance>) resultData.getSerializable(BundleId.ALLOWANCE_LIST);
        assertEquals(48, fixedTravelAllowances.size());
    }
}
