package com.concur.mobile.core.expense.travelallowance.datamodel.test;

import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by D023077 on 11.09.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)

public class MealProvisionTest extends TestCase {

    /**
     * Check if meal provision is created and returns the input parameter
     */
    @Test
    public void mealProvision() {
        MealProvision mealProvision = new MealProvision();
        assertNotNull(mealProvision);

        mealProvision = new MealProvision("XLV", "value");
        assertNotNull(mealProvision);
        assertEquals("XLV", mealProvision.getCode());
        assertEquals("value", mealProvision.getDescription());

        mealProvision.setCode("VEX");
        assertEquals("VEX", mealProvision.getCode());

        mealProvision.setDescription("vexologie");
        assertEquals("vexologie", mealProvision.getDescription());
    }

    /**
     * Check all possibilities of equals-implementation
     */
    @Test
    public void equals(){
        MealProvision mealProvision = new MealProvision("XLV", "value");
        assertEquals(true, mealProvision.equals(mealProvision));

        Object o = new Object();
        assertEquals(false, mealProvision.equals(o));

        MealProvision mealProvision1 = new MealProvision("VEX", "vexologie");
        assertEquals(false, mealProvision.equals(mealProvision1));

        mealProvision1.setCode("XLV");
        assertEquals(false, mealProvision.equals(mealProvision1));

        mealProvision1.setCode("VEX");
        mealProvision1.setDescription("value");
        assertEquals(false, mealProvision.equals(mealProvision1));

        mealProvision1.setCode("XLV");
        assertEquals(true, mealProvision.equals(mealProvision1));
    }

    @Test
    public void hashCodeTest(){
        MealProvision mealProvision = new MealProvision("XLV", "value");
        assertEquals(114670031, mealProvision.hashCode());
    }

    @Test
    public void compareTo(){
        MealProvision mealProvision  = new MealProvision("XLV", "value");
        MealProvision mealProvision1 = new MealProvision("VEX", "vexologie");
        mealProvision1 = null;
        assertEquals(1, mealProvision.compareTo(mealProvision1));

        mealProvision1 = new MealProvision(null, "vexologie");
        assertEquals(1, mealProvision.compareTo(mealProvision1));

        mealProvision1.setCode("VEX");
        assertEquals(2, mealProvision.compareTo(mealProvision1));

        mealProvision1.setCode("XLV");
        assertEquals(0, mealProvision.compareTo(mealProvision1));

        mealProvision1.setCode("ABC");
        assertEquals(23, mealProvision.compareTo(mealProvision1));

        mealProvision.setCode(null);
        assertEquals(-1, mealProvision.compareTo(mealProvision1));

        mealProvision1.setCode(null);
        assertEquals(0, mealProvision.compareTo(mealProvision1));

    }

    @Test
    public void toStringTest(){
        MealProvision mealProvision  = new MealProvision("XLV", "value");
        assertEquals("value", mealProvision.toString());

        mealProvision.setDescription("");
        assertEquals("XLV", mealProvision.toString());

        mealProvision.setDescription(null);
        assertEquals("XLV", mealProvision.toString());

    }
}