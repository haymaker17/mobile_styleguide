package com.concur.mobile.core.expense.travelallowance.datamodel.test;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.test.InstrumentationTestCase;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvisionEnum;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Created by D023077 on 10.09.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)

public class MealProvisionEnumTest extends TestCase {
    //public class MealProvisionEnumTest extends TestCase {

    /**
     * Check if the correct resource IDs are returned
     */
    @Test
    public void getResourceId() {
        assertEquals(R.string.general_yes, MealProvisionEnum.PROVIDED.getResourceId());
        assertEquals(R.string.general_no,  MealProvisionEnum.NOT_PROVIDED.getResourceId());
    }

    /**
     * Check if the correct codes are returned
     */
    @Test
    public void getCode() {
        assertEquals("PRO", MealProvisionEnum.PROVIDED.getCode());
        assertEquals("NPR", MealProvisionEnum.NOT_PROVIDED.getCode());
    }

    /**
     * Check if the correct entry is selected without using the context
     */
    @Test
    public void fromCodeWithoutContext(){
        assertEquals(MealProvisionEnum.PROVIDED,     MealProvisionEnum.fromCode("PRO"));
        assertEquals(MealProvisionEnum.NOT_PROVIDED, MealProvisionEnum.fromCode("NPR"));
        assertNull(MealProvisionEnum.fromCode("XLV"));
    }

}
