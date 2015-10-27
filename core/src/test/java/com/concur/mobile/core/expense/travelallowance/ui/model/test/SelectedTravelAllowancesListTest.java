package com.concur.mobile.core.expense.travelallowance.ui.model.test;

import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.ICode;
import com.concur.mobile.core.expense.travelallowance.datamodel.LodgingType;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.ui.model.SelectedTravelAllowancesList;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;

import testconfig.CoreTestApplication;
import testconfig.CoreTestRunner;

/**
 * Created by D049515 on 26.10.2015.
 */
@Config(application = CoreTestApplication.class, manifest = "AndroidManifest.xml", sdk = 21)
@RunWith(CoreTestRunner.class)
public class SelectedTravelAllowancesListTest extends TestCase {


    private SelectedTravelAllowancesList list;
    private FixedTravelAllowance ta1, ta2, ta3;
    private Collection<FixedTravelAllowance> allowanceList;


    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.list = new SelectedTravelAllowancesList();
        this.ta1 = new FixedTravelAllowance("ta1");
        this.ta2 = new FixedTravelAllowance("ta2");
        this.ta3 = new FixedTravelAllowance("ta3");

        allowanceList = new ArrayList<>();
        allowanceList.add(ta1);
        allowanceList.add(ta2);
        allowanceList.add(ta3);
    }

    @Test
    public void initialTest() {
        assertNotNull(list);
        assertEquals(0, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

    }

    @Test
    public void addBreakfastSelectTest() {
        ta1.setBreakfastProvision(new MealProvision("m1", "Meal"));
        ta2.setBreakfastProvision(new MealProvision("m1", "Meal"));
        ta3.setBreakfastProvision(new MealProvision("m2", "Meal"));

        list.add(ta1);
        assertEquals(1, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

        list.add(ta2);
        assertEquals(2, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

        list.add(ta3);
        assertEquals(3, list.size());
        assertTrue(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());
    }

    @Test
    public void addLunchSelectTest() {
        ta1.setLunchProvision(new MealProvision("m1", "Meal"));
        ta2.setLunchProvision(new MealProvision("m1", "Meal"));
        ta3.setLunchProvision(new MealProvision("m2", "Meal"));

        list.add(ta1);
        assertEquals(1, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

        list.add(ta2);
        assertEquals(2, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

        list.add(ta3);
        assertEquals(3, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertTrue(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());
    }

    @Test
    public void addDinnerSelectTest() {
        ta1.setDinnerProvision(new MealProvision("m1", "Meal"));
        ta2.setDinnerProvision(new MealProvision("m1", "Meal"));
        ta3.setDinnerProvision(new MealProvision("m2", "Meal"));

        list.add(ta1);
        assertEquals(1, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

        list.add(ta2);
        assertEquals(2, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

        list.add(ta3);
        assertEquals(3, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertTrue(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());
    }


    @Test
    public void addLodgingSelectTest() {
        ta1.setLodgingType(new LodgingType("m1", "Lodging"));
        ta2.setLodgingType(new LodgingType("m1", "Lodging"));
        ta3.setLodgingType(new LodgingType("m2", "Lodging"));

        list.add(ta1);
        assertEquals(1, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

        list.add(ta2);
        assertEquals(2, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

        list.add(ta3);
        assertEquals(3, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertTrue(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());
    }


    @Test
    public void addOvernightSelectTest() {
        ta1.setOvernightIndicator(false);
        ta2.setOvernightIndicator(false);
        ta3.setOvernightIndicator(true);

        list.add(ta1);
        assertEquals(1, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

        list.add(ta2);
        assertEquals(2, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

        list.add(ta3);
        assertEquals(3, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertTrue(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());
    }


    @Test
    public void addOnlyLastDayTest() {
        ta1.setIsLastDay(true);
        ta2.setIsLastDay(true);

        list.add(ta1);
        assertEquals(1, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertTrue(list.hasOnlyLastDays());

        list.add(ta2);
        assertEquals(2, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertTrue(list.hasOnlyLastDays());

    }

    @Test
    public void testConstructor() {
        list = new SelectedTravelAllowancesList(allowanceList);

        assertEquals(3, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

        ta1.setBreakfastProvision(new MealProvision("m1", "Meal"));
        ta2.setBreakfastProvision(new MealProvision("m2", "Meal"));

        list = new SelectedTravelAllowancesList(allowanceList);
        assertEquals(3, list.size());
        assertTrue(list.isBreakfastMultiSelected());

    }

    @Test
    public void testAddAll() {

        list.addAll(allowanceList);
        assertEquals(3, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

        ta1.setBreakfastProvision(new MealProvision("m1", "Meal"));
        ta2.setBreakfastProvision(new MealProvision("m2", "Meal"));

        list = new SelectedTravelAllowancesList();
        list.addAll(allowanceList);
        assertEquals(3, list.size());
        assertTrue(list.isBreakfastMultiSelected());

    }


    @Test
    public void testAddAllIndex() {

        list.addAll(0, allowanceList);
        assertEquals(3, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

        ta1.setBreakfastProvision(new MealProvision("m1", "Meal"));
        ta2.setBreakfastProvision(new MealProvision("m2", "Meal"));

        list = new SelectedTravelAllowancesList();
        list.addAll(0, allowanceList);
        assertEquals(3, list.size());
        assertTrue(list.isBreakfastMultiSelected());

    }


    @Test
    public void addIndexBreakfastSelectTest() {
        ta1.setBreakfastProvision(new MealProvision("m1", "Meal"));
        ta2.setBreakfastProvision(new MealProvision("m1", "Meal"));
        ta3.setBreakfastProvision(new MealProvision("m2", "Meal"));

        list.add(0, ta1);
        assertEquals(1, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

        list.add(1, ta2);
        assertEquals(2, list.size());
        assertFalse(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());

        list.add(2, ta3);
        assertEquals(3, list.size());
        assertTrue(list.isBreakfastMultiSelected());
        assertFalse(list.isLunchMultiSelected());
        assertFalse(list.isDinnerMultiSelected());
        assertFalse(list.isLodgingTypeMultiSelected());
        assertFalse(list.isOvernightMultiSelected());
        assertFalse(list.hasOnlyLastDays());
    }

    @Test
    public void getTemplateTest() {
        list.add(ta1);
        list.add(ta2);
        list.add(ta3);

        assertEquals(ta1, list.getTemplate());

        ta1.setIsLastDay(true);
        ta2.setIsLastDay(true);

        assertEquals(ta3, list.getTemplate());

        ta3.setIsLastDay(true);

        assertEquals(ta1, list.getTemplate());
    }


    @Test
    public void nullTest() {

        assertEquals(0, list.size());
        list.add(null);
        assertEquals(0, list.size());
        assertNull(list.getTemplate());

        try {
            list = new SelectedTravelAllowancesList(null);
            fail("NPE expected.");
        } catch (NullPointerException npe) {

        }

        ta1.setBreakfastProvision(new MealProvision("m1", "Meal"));

        list.add(ta1);
        list.add(ta2);

        assertEquals(true, list.isBreakfastMultiSelected());

        assertFalse(list.add(null));
        assertEquals(2, list.size());

        list.add(0, null);
        assertEquals(2, list.size());

        assertFalse(list.addAll(null));
        assertEquals(2, list.size());

        assertFalse(list.addAll(0, null));
        assertEquals(2, list.size());

        list = new SelectedTravelAllowancesList();
        allowanceList.add(null);
        list.addAll(allowanceList);
        assertEquals(4, list.size());

        assertEquals(ta1, list.getTemplate());

        Collection<FixedTravelAllowance> nullList = new ArrayList<>();
        nullList.add(null);
        list = new SelectedTravelAllowancesList(nullList);
        assertEquals(1, list.size());
        assertNull(list.getTemplate());
    }

}
