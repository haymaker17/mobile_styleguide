package com.concur.mobile.core.expense.travelallowance.datamodel.test;

import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.LodgingType;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by D023077 on 14.09.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)

public class FixedTravelAllowanceTest  extends TestCase {

    private MealProvision mealProvisionProvided    = new MealProvision(MealProvision.PROVIDED_CODE, "Provided");
    private MealProvision mealProvisionNotProvided = new MealProvision(MealProvision.NOT_PROVIDED_CODE, "Not Provided");

    private LodgingType lodgingTypeXLV = new LodgingType("XLV", "XLV Lodging");
    private LodgingType lodgingTypeVEX = new LodgingType("VEX", "VEX Lodging");

    @Test
    public void constructorTest(){
        String id = "XLV_0815";
        assertNotNull(new FixedTravelAllowance());
        FixedTravelAllowance fixedTravelAllowance = new FixedTravelAllowance(id);
        assertEquals(id, fixedTravelAllowance.getFixedTravelAllowanceId());
    }

    @Test
    public void setterGetterTest(){
        FixedTravelAllowance fixedTravelAllowance = createFixedTA();

        assertEquals("XLV_08", fixedTravelAllowance.getFixedTravelAllowanceId());
        assertEquals(referenceDate(), fixedTravelAllowance.getDate());
        assertEquals(23.15, fixedTravelAllowance.getAmount());
        assertEquals("EUR", fixedTravelAllowance.getCurrencyCode());
        assertEquals(true, fixedTravelAllowance.getExcludedIndicator());
        assertEquals("Erlangen", fixedTravelAllowance.getLocationName());
        assertEquals(mealProvisionProvided, fixedTravelAllowance.getBreakfastProvision());
        assertEquals(mealProvisionProvided, fixedTravelAllowance.getLunchProvision());
        assertEquals(mealProvisionNotProvided, fixedTravelAllowance.getDinnerProvision());
        assertEquals(true, fixedTravelAllowance.getOvernightIndicator());
        assertEquals(lodgingTypeXLV, fixedTravelAllowance.getLodgingType());
        assertFalse(fixedTravelAllowance.isLocked());

    }

    @Test
    public void equalsTest(){
        FixedTravelAllowance fixedTravelAllowanceBase = createFixedTA();

        FixedTravelAllowance fixedTravelAllowanceComp;
        fixedTravelAllowanceComp = null;
        Object o = new Object();

        assertTrue( fixedTravelAllowanceBase.equals(fixedTravelAllowanceBase));

        assertFalse( fixedTravelAllowanceBase.equals(fixedTravelAllowanceComp));
        assertFalse( fixedTravelAllowanceBase.equals(o));

        fixedTravelAllowanceComp = createFixedTA();
        assertTrue( fixedTravelAllowanceBase.equals(fixedTravelAllowanceComp));

        fixedTravelAllowanceComp.setExcludedIndicator(false);
        assertFalse( fixedTravelAllowanceBase.equals(fixedTravelAllowanceComp));
        fixedTravelAllowanceComp.setExcludedIndicator(true);

        fixedTravelAllowanceComp.setOvernightIndicator(false);
        assertFalse( fixedTravelAllowanceBase.equals(fixedTravelAllowanceComp));
        fixedTravelAllowanceComp.setOvernightIndicator(true);

        fixedTravelAllowanceComp.setLocked(true);
        assertFalse( fixedTravelAllowanceBase.equals(fixedTravelAllowanceComp));
        fixedTravelAllowanceComp.setLocked(false);

        fixedTravelAllowanceComp.setFixedTravelAllowanceId("XLV_4711");
        assertFalse( fixedTravelAllowanceBase.equals(fixedTravelAllowanceComp));
        fixedTravelAllowanceComp.setFixedTravelAllowanceId("XLV_08");

        fixedTravelAllowanceComp.setCurrencyCode("USD");
        assertFalse(fixedTravelAllowanceBase.equals(fixedTravelAllowanceComp));
        fixedTravelAllowanceComp.setCurrencyCode("EUR");

        fixedTravelAllowanceComp.setLocationName("Bamberg");
        assertFalse(fixedTravelAllowanceBase.equals(fixedTravelAllowanceComp));
        fixedTravelAllowanceComp.setLocationName("Erlangen");

        fixedTravelAllowanceComp.setBreakfastProvision(mealProvisionNotProvided);
        assertFalse(fixedTravelAllowanceBase.equals(fixedTravelAllowanceComp));
        fixedTravelAllowanceComp.setBreakfastProvision(mealProvisionProvided);

        fixedTravelAllowanceComp.setLunchProvision(mealProvisionNotProvided);
        assertFalse(fixedTravelAllowanceBase.equals(fixedTravelAllowanceComp));
        fixedTravelAllowanceComp.setLunchProvision(mealProvisionProvided);

        fixedTravelAllowanceComp.setDinnerProvision(mealProvisionProvided);
        assertFalse(fixedTravelAllowanceBase.equals(fixedTravelAllowanceComp));
        fixedTravelAllowanceComp.setDinnerProvision(mealProvisionNotProvided);

        fixedTravelAllowanceComp.setLodgingType(lodgingTypeVEX);
        assertFalse(fixedTravelAllowanceBase.equals(fixedTravelAllowanceComp));
        fixedTravelAllowanceComp.setLodgingType(lodgingTypeXLV);

    }

//    @Test
    public void hashCodeTest(){
        FixedTravelAllowance fixedTravelAllowance = createFixedTA();
        assertEquals(-1013283212, fixedTravelAllowance.hashCode());
    }

    @Test
    public void compareToTest(){
        FixedTravelAllowance fixedTravelAllowanceBase = createFixedTA();
        FixedTravelAllowance fixedTravelAllowanceComp = createFixedTA();

        assertEquals(-1, fixedTravelAllowanceBase.compareTo(null));

        assertEquals(0, fixedTravelAllowanceBase.compareTo(fixedTravelAllowanceComp));

        fixedTravelAllowanceComp.setDate(null);
        assertEquals(1, fixedTravelAllowanceBase.compareTo(fixedTravelAllowanceComp));

        fixedTravelAllowanceBase.setDate(null);
        assertEquals(0, fixedTravelAllowanceBase.compareTo(fixedTravelAllowanceComp));

        fixedTravelAllowanceComp.setDate(referenceDate());
        assertEquals(-1, fixedTravelAllowanceBase.compareTo(fixedTravelAllowanceComp));

    }

    private FixedTravelAllowance createFixedTA(){
        FixedTravelAllowance fixedTravelAllowance = new FixedTravelAllowance();

        fixedTravelAllowance.setFixedTravelAllowanceId("XLV_08");

        fixedTravelAllowance.setDate(referenceDate());

        fixedTravelAllowance.setAmount(23.15);

        fixedTravelAllowance.setCurrencyCode("EUR");

        fixedTravelAllowance.setExcludedIndicator(true);

        fixedTravelAllowance.setLocationName("Erlangen");

        fixedTravelAllowance.setBreakfastProvision(mealProvisionProvided);

        fixedTravelAllowance.setLunchProvision(mealProvisionProvided);

        fixedTravelAllowance.setDinnerProvision(mealProvisionNotProvided);

        fixedTravelAllowance.setOvernightIndicator(true);

        fixedTravelAllowance.setLodgingType(lodgingTypeXLV);

        fixedTravelAllowance.setLocked(false);

        return fixedTravelAllowance;
    }

    private Date referenceDate(){
        Calendar cal = Calendar.getInstance();
        cal.set(2015, Calendar.SEPTEMBER, 14, 17, 2, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }
}
