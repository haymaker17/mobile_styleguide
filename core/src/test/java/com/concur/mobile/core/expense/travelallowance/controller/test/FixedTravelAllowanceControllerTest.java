package com.concur.mobile.core.expense.travelallowance.controller.test;

import android.content.Context;
import android.os.Bundle;

import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.service.GetTAFixedAllowancesRequest2;
import com.concur.mobile.core.expense.travelallowance.testutils.FileRequestTaskWrapper;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import testconfig.CoreTestApplication;
import testconfig.CoreTestRunner;

/**
 * Created by D028778 on 19.10.2015.
 */

@Config(application = CoreTestApplication.class, manifest = "AndroidManifest.xml", sdk = 21)
@RunWith(CoreTestRunner.class)
public class FixedTravelAllowanceControllerTest extends TestCase {

    private class FixedTravelAllowanceControllerDouble extends FixedTravelAllowanceController {
        public FixedTravelAllowanceControllerDouble(Context context) {
            super(context);
        }

        @Override
        public void setFixedTAList(List<FixedTravelAllowance> list) {
            super.setFixedTAList(list);
        }
    }

    private static final String TEST_DATA_PATH = "src/test/java/com/concur/mobile/core/expense/travelallowance/testdata";

    @Test
    public void getLocationsAndAllowances() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAFixedAllowancesRequest2(null, null, null));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "FixedAllowances.xml");
        List <FixedTravelAllowance> fixedTravelAllowances = (List<FixedTravelAllowance>) resultData.getSerializable(BundleId.ALLOWANCE_LIST);
        FixedTravelAllowanceControllerDouble controllerDouble = new FixedTravelAllowanceControllerDouble(RuntimeEnvironment.application);
        controllerDouble.setFixedTAList(fixedTravelAllowances);
        List<Object> locationsAndAllowances = controllerDouble.getLocationsAndAllowances();
        assertEquals(53, locationsAndAllowances.size());
        FixedTravelAllowance allowance = (FixedTravelAllowance) locationsAndAllowances.get(52);
        assertEquals("121", allowance.getFixedTravelAllowanceId());
    }

    @Test
    public void hasMultipleGroupsTrue() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAFixedAllowancesRequest2(null, null, null));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "FixedAllowances.xml");
        List<FixedTravelAllowance> fixedTravelAllowances = (List<FixedTravelAllowance>) resultData.getSerializable(BundleId.ALLOWANCE_LIST);
        FixedTravelAllowanceControllerDouble controllerDouble = new FixedTravelAllowanceControllerDouble(RuntimeEnvironment.application);
        controllerDouble.setFixedTAList(fixedTravelAllowances);
        boolean multipleGroups = controllerDouble.hasMultipleGroups();
        assertEquals(true, multipleGroups);
    }

    @Test
    public void hasMultipleGroupsFalse() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAFixedAllowancesRequest2(null, null, null));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "FixedAllowance.xml");
        List <FixedTravelAllowance> fixedTravelAllowances = (List<FixedTravelAllowance>) resultData.getSerializable(BundleId.ALLOWANCE_LIST);
        FixedTravelAllowanceControllerDouble controllerDouble = new FixedTravelAllowanceControllerDouble(RuntimeEnvironment.application);
        controllerDouble.setFixedTAList(fixedTravelAllowances);
        boolean multipleGroups = controllerDouble.hasMultipleGroups();
        assertEquals(false, multipleGroups);
    }

    @Test
    public void getSum() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAFixedAllowancesRequest2(null, null, null));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "FixedAllowance.xml");
        List <FixedTravelAllowance> fixedTravelAllowances = (List<FixedTravelAllowance>) resultData.getSerializable(BundleId.ALLOWANCE_LIST);
        FixedTravelAllowanceControllerDouble controllerDouble = new FixedTravelAllowanceControllerDouble(RuntimeEnvironment.application);
        controllerDouble.setFixedTAList(fixedTravelAllowances);
        double sum = controllerDouble.getSum();
        assertEquals(170.0, sum);
    }

}
