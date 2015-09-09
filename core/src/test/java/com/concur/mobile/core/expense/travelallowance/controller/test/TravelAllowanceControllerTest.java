package com.concur.mobile.core.expense.travelallowance.controller.test;

import android.app.Activity;

import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceController;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by D049515 on 28.08.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)
public class TravelAllowanceControllerTest extends TestCase {

    private TravelAllowanceController controller;

    @Before
    public void setup() {
        controller = new TravelAllowanceController(new Activity());
    }

    @Test
    public void getterTest() {
        assertNotNull(controller.getFixedTravelAllowanceController());
        assertNotNull(controller.getTAConfigController());
        assertNotNull(controller.getTaItineraryController());
    }

}
