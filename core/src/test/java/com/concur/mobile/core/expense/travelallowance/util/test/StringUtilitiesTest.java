package com.concur.mobile.core.expense.travelallowance.util.test;

import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.TestClass;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by D023077 on 17.09.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)

public class StringUtilitiesTest extends TestCase{

    @Test
    public void isNullOrEmpty(){
        String test = "";

        assertTrue(StringUtilities.isNullOrEmpty(StringUtilities.EMPTY_STRING));
        assertTrue(StringUtilities.isNullOrEmpty(test));

        test = "Test";
        assertFalse(StringUtilities.isNullOrEmpty(test));

    }

    @Test
    public void toBoolean(){

        assertTrue(StringUtilities.toBoolean("Y"));
        assertTrue(StringUtilities.toBoolean("y"));

        assertFalse(StringUtilities.toBoolean(null));
        assertFalse(StringUtilities.toBoolean("Yes"));

    }

}
