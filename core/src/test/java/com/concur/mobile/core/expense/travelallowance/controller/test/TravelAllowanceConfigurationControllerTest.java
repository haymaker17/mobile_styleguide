package com.concur.mobile.core.expense.travelallowance.controller.test;

import android.app.Activity;

import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceConfigurationController;

import junit.framework.TestCase;

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
public class TravelAllowanceConfigurationControllerTest extends TestCase {

    private TravelAllowanceConfigurationController controller;

    @Before
    public void setup() {
        this.controller = new TravelAllowanceConfigurationController(new Activity());
    }

    @Test
    public void test() {
        //TODO: Remove dummy test case as soon as the first real test case is in here.
        assertTrue(false);
    }

}
