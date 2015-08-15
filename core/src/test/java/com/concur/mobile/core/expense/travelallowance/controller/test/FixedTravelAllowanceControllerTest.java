package com.concur.mobile.core.expense.travelallowance.controller.test;

import android.app.Activity;

import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by D049515 on 14.07.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)
public class FixedTravelAllowanceControllerTest extends TestCase {

    private FixedTravelAllowanceController controller;

    @Before
    public void setup() {
        this.controller = new FixedTravelAllowanceController(new Activity());
    }

    @Test
    public void test() {
        //TODO: Remove dummy test case as soon as the first real test case is in here.
        assertTrue(false);
    }
}
