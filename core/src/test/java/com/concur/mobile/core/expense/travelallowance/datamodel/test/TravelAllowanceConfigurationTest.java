package com.concur.mobile.core.expense.travelallowance.datamodel.test;

import com.concur.mobile.core.expense.travelallowance.datamodel.TravelAllowanceConfiguration;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by D023077 on 15.09.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)

public class TravelAllowanceConfigurationTest extends TestCase {

    @Test
    public void setterGetterTest() {
        TravelAllowanceConfiguration travelAllowanceConfiguration = new TravelAllowanceConfiguration();

        travelAllowanceConfiguration.setConfigCode("XLV");
        assertEquals("XLV", travelAllowanceConfiguration.getConfigCode());

        travelAllowanceConfiguration.setDeductForProvidedBreakfast("DeductBreakfast");
        assertEquals("DeductBreakfast", travelAllowanceConfiguration.getDeductForProvidedBreakfast());

        travelAllowanceConfiguration.setDeductForProvidedLunch("DeductLunch");
        assertEquals("DeductLunch", travelAllowanceConfiguration.getDeductForProvidedLunch());

        travelAllowanceConfiguration.setDeductForProvidedDinner("DeductDinner");
        assertEquals("DeductDinner", travelAllowanceConfiguration.getDeductForProvidedDinner());

        travelAllowanceConfiguration.setDefaultBreakfastToProvided("NPR");
        assertEquals("NPR", travelAllowanceConfiguration.getDefaultBreakfastToProvided());

        travelAllowanceConfiguration.setDefaultLunchToProvided("NPR");
        assertEquals("NPR", travelAllowanceConfiguration.getDefaultLunchToProvided());

        travelAllowanceConfiguration.setDefaultDinnerToProvided("NPR");
        assertEquals("NPR", travelAllowanceConfiguration.getDefaultDinnerToProvided());

        travelAllowanceConfiguration.setLodgingTat("Tata");
        assertEquals("Tata", travelAllowanceConfiguration.getLodgingTat());

        travelAllowanceConfiguration.setMealDeductionList("MealDeductionList");
        assertEquals("MealDeductionList", travelAllowanceConfiguration.getMealDeductionList());

        travelAllowanceConfiguration.setMealsTat("MealTata");
        assertEquals("MealTata", travelAllowanceConfiguration.getMealsTat());

        travelAllowanceConfiguration.setSingleRowCheck("Yes");
        assertEquals("Yes", travelAllowanceConfiguration.getSingleRowCheck());

        travelAllowanceConfiguration.setTacKey("4712");
        assertEquals("4712", travelAllowanceConfiguration.getTacKey());

        travelAllowanceConfiguration.setUseBorderCrossTime(true);
        assertTrue(travelAllowanceConfiguration.isUseBorderCrossTime());

        travelAllowanceConfiguration.setUseLodgingType(true);
        assertTrue(travelAllowanceConfiguration.isUseLodgingType());

        travelAllowanceConfiguration.setUseOvernight(true);
        assertTrue(travelAllowanceConfiguration.isUseOvernight());

    }

    }
