package com.concur.mobile.core.expense.travelallowance.expensedetails.test;

import android.app.Activity;
import android.content.Context;

import com.concur.core.BuildConfig;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceControlData;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.LodgingType;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.expensedetails.ExpenseEntryTAFieldFactory;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;


import java.util.ArrayList;
import java.util.List;

import testconfig.ActivityDouble;
import testconfig.RoboTestRunner;
import testconfig.StringConstants;

/**
 * Created by D049515 on 16.07.2015.
 */
@Config(constants = BuildConfig.class, manifest = "AndroidManifest.xml", sdk = 21)
@RunWith(RoboTestRunner.class)
public class ExpenseEntryTAFieldFactoryTest extends TestCase {

    private class FixedTAControllerDouble extends FixedTravelAllowanceController {
        /**
         * Creates an instance and initializes the object
         *
         * @param context
         */
        public FixedTAControllerDouble(Context context) {
            super(context);
        }

        @Override
        public void setFixedTAList(List<FixedTravelAllowance> list) {
            super.setFixedTAList(list);
        }
    }

    private class ExpenseReportEntryDetailDouble extends ExpenseReportEntryDetail {

        ArrayList<ExpenseReportEntry> list;

        @Override
        public ArrayList<ExpenseReportEntry> getItemizations() {
            return list;
        }
    }

    private ExpenseEntryTAFieldFactory factory;

    private ExpenseReportEntryDetail expRepEntryDetail;




    private FixedTravelAllowance createTestFixedTAMeals(String breakfast, String lunch, String dinner) {
        FixedTravelAllowance fixedTA = new FixedTravelAllowance();
        fixedTA.setFixedTravelAllowanceId("1");

        if (breakfast != null) {
            if (breakfast.equals("NPR")) {
                fixedTA.setBreakfastProvision(new MealProvision("NPR", StringConstants.GENERAL_NO));
            } else {
                fixedTA.setBreakfastProvision(new MealProvision("PRO", StringConstants.GENERAL_YES));
            }
        }

        if (lunch != null) {
            if (lunch.equals("NPR")) {
                fixedTA.setLunchProvision(new MealProvision("NPR", StringConstants.GENERAL_NO));
            } else {
                fixedTA.setLunchProvision(new MealProvision("PRO", StringConstants.GENERAL_YES));
            }
        }

        if (dinner != null) {
            if (dinner.equals("NPR")) {
                fixedTA.setDinnerProvision(new MealProvision("NPR", StringConstants.GENERAL_NO));
            } else {
                fixedTA.setDinnerProvision(new MealProvision("PRO", StringConstants.GENERAL_YES));
            }
        }
       return fixedTA;
    }

    /**
     * Expense Type is not allowance related and no allowances.
     *
     * Expected output: No fields.
     */
    @Test
    @Config(shadows = {ActivityDouble.class})
    public void testNoDailyAllowanceExpense() {

        expRepEntryDetail = new ExpenseReportEntryDetail();
        expRepEntryDetail.taDayKey = "1";
        expRepEntryDetail.expKey = "xxx";

        factory = new ExpenseEntryTAFieldFactory(new Activity(), expRepEntryDetail, new FixedTravelAllowanceController(new Activity()));

        List<ExpenseReportFormField> fieldList = factory.getFormFields();
        assertEquals(0, fieldList.size());
    }

    /**
     * Expense Type is not allowance related and one allowance existing.
     *
     * Expected output: No fields.
     */
    @Test
    @Config(shadows = {ActivityDouble.class})
    public void testNoDailyAllowanceExpenseWithAllowance() {

        FixedTAControllerDouble controller = new FixedTAControllerDouble(new Activity());
        List<FixedTravelAllowance> taList = new ArrayList<>();
        taList.add(createTestFixedTAMeals("NPR", null, null));
        controller.setFixedTAList(taList);

        expRepEntryDetail = new ExpenseReportEntryDetail();
        expRepEntryDetail.taDayKey = "1";
        expRepEntryDetail.expKey = "xxx";

        factory = new ExpenseEntryTAFieldFactory(new Activity(), expRepEntryDetail, controller);

        List<ExpenseReportFormField> fieldList = factory.getFormFields();
        assertEquals(0, fieldList.size());
    }

    /**
     * Expense Type is Daily Allowance (FXMLS)
     * No Fixed Travel Allowances available.
     *
     * Expected output: One field which shows No adjustments available
     */
    @Test
    @Config(shadows = {ActivityDouble.class})
    public void testNoDailyAllowance() {

        expRepEntryDetail = new ExpenseReportEntryDetail();
        expRepEntryDetail.taDayKey = "1";
        expRepEntryDetail.expKey = "FXMLS";

        factory = new ExpenseEntryTAFieldFactory(new Activity(), expRepEntryDetail,new FixedTravelAllowanceController(new Activity()));

        List<ExpenseReportFormField> fieldList = factory.getFormFields();
        assertEquals(1, fieldList.size());

        ExpenseReportFormField field = fieldList.get(0);
        assertEquals(StringConstants.NO_ADJUSTMEMTS, field.getValue());
    }


    /**
     * Expense Type is Daily Allowance (FXMLS)
     * One fixed TA with breakfast PRO, Lunch NPR and Dinner PRO.
     * Control data YES for all and labels set.
     *
     * Expected output: 3 field which shows the breakfast lunch and dinner
     */
    @Test
    @Config(shadows = {ActivityDouble.class})
    public void testDailyAllowanceAllMeals() {

        FixedTAControllerDouble controller = new FixedTAControllerDouble(new Activity());
        List<FixedTravelAllowance> taList = new ArrayList<>();
        taList.add(createTestFixedTAMeals("PRO", "NPR", "PRO"));
        controller.setFixedTAList(taList);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.BREAKFAST_PROVIDED_LABEL, StringConstants.BREAKFAST_LABEL);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX, "Y");
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.DINNER_PROVIDED_LABEL, StringConstants.DINNER_LABEL);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX, "Y");
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.LUNCH_PROVIDED_LABEL, StringConstants.LUNCH_LABEL);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX, "Y");

        expRepEntryDetail = new ExpenseReportEntryDetail();
        expRepEntryDetail.taDayKey = "1";
        expRepEntryDetail.expKey = "FXMLS";

        // Check if test data is available.
        assertNotNull(controller.getFixedTA("1").getBreakfastProvision());
        assertNotNull(controller.getFixedTA("1").getDinnerProvision());
        assertNotNull(controller.getFixedTA("1").getLunchProvision());

        // Test execution
        factory = new ExpenseEntryTAFieldFactory(new Activity(), expRepEntryDetail, controller);

        // Assert
        List<ExpenseReportFormField> fieldList = factory.getFormFields();
        assertEquals(3, fieldList.size());

        ExpenseReportFormField field = fieldList.get(0);
        assertEquals(StringConstants.BREAKFAST_LABEL, field.getLabel());
        assertEquals(StringConstants.GENERAL_YES, field.getValue());

        field = fieldList.get(1);
        assertEquals(StringConstants.LUNCH_LABEL, field.getLabel());
        assertEquals(StringConstants.GENERAL_NO, field.getValue());

        field = fieldList.get(2);
        assertEquals(StringConstants.DINNER_LABEL, field.getLabel());
        assertEquals(StringConstants.GENERAL_YES, field.getValue());
    }


    /**
     * Expense Type is Daily Allowance (FXMLS)
     * One fixed TA with breakfast PRO and Dinner PRO.
     * Control data YES for show breakfast and breakfast label set.
     *
     * Control data NO for show dinner and dinner label set.
     *
     * Expected output: One field which shows the breakfast
     */
    @Test
    @Config(shadows = {ActivityDouble.class})
    public void testDailyAllowanceOnlyBreakfast() {

        FixedTAControllerDouble controller = new FixedTAControllerDouble(new Activity());
        List<FixedTravelAllowance> taList = new ArrayList<>();
        taList.add(createTestFixedTAMeals("PRO", null, "PRO"));
        controller.setFixedTAList(taList);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.BREAKFAST_PROVIDED_LABEL, StringConstants.BREAKFAST_LABEL);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX, "Y");
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.DINNER_PROVIDED_LABEL, StringConstants.DINNER_LABEL);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX, "N");

        expRepEntryDetail = new ExpenseReportEntryDetail();
        expRepEntryDetail.taDayKey = "1";
        expRepEntryDetail.expKey = "FXMLS";

        // Check if test data is available.
        assertNotNull(controller.getFixedTA("1").getBreakfastProvision());
        assertNotNull(controller.getFixedTA("1").getDinnerProvision());
        assertNull(controller.getFixedTA("1").getLunchProvision());

        // Test execution
        factory = new ExpenseEntryTAFieldFactory(new Activity(), expRepEntryDetail, controller);

        // Assert
        List<ExpenseReportFormField> fieldList = factory.getFormFields();
        assertEquals(1, fieldList.size());

        ExpenseReportFormField field = fieldList.get(0);
        assertEquals(StringConstants.BREAKFAST_LABEL, field.getLabel());
        assertEquals(StringConstants.GENERAL_YES, field.getValue());
    }


    /**
     * Expense Type is Daily Allowance (FXMLS)
     * One fixed TA with breakfast PRO and Dinner PRO.
     * Control data YES for show breakfast and breakfast label set.
     * Control data NO for show dinner and dinner label set.
     *
     * One overnight item in itemization
     * Show overnight checkbox YES and Label set.
     *
     * Expected output: One field which shows the breakfast and one field for overnight.
     */
    @Test
    @Config(shadows = {ActivityDouble.class})
    public void testDailyAllowanceOvernightItemization() {

        FixedTAControllerDouble controller = new FixedTAControllerDouble(new Activity());
        List<FixedTravelAllowance> taList = new ArrayList<>();
        FixedTravelAllowance ta = createTestFixedTAMeals("PRO", null, "PRO");
        ta.setOvernightIndicator(true);
        taList.add(ta);
        controller.setFixedTAList(taList);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.BREAKFAST_PROVIDED_LABEL, StringConstants.BREAKFAST_LABEL);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX, "Y");
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.DINNER_PROVIDED_LABEL, StringConstants.DINNER_LABEL);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX, "N");

        controller.getControlData().putControlData(FixedTravelAllowanceControlData.OVERNIGHT_LABEL, StringConstants.OVERNIGHT);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.SHOW_OVERNIGHT_CHECKBOX, "Y");

        ExpenseReportEntryDetailDouble expRepEntryDetail = new ExpenseReportEntryDetailDouble();
        expRepEntryDetail.taDayKey = "1";
        expRepEntryDetail.expKey = "FXMLS";

        ExpenseReportEntry itemEntry = new ExpenseReportEntry();
        itemEntry.expKey = "OVRNT";
        ArrayList<ExpenseReportEntry> entryList = new ArrayList<>();
        entryList.add(itemEntry);
        expRepEntryDetail.list = entryList;

        // Check if test data is available.
        assertNotNull(controller.getFixedTA("1").getBreakfastProvision());
        assertNotNull(controller.getFixedTA("1").getDinnerProvision());
        assertNull(controller.getFixedTA("1").getLunchProvision());

        // Test execution
        factory = new ExpenseEntryTAFieldFactory(new Activity(), expRepEntryDetail, controller);

        // Assert
        List<ExpenseReportFormField> fieldList = factory.getFormFields();
        assertEquals(2, fieldList.size());

        ExpenseReportFormField field = fieldList.get(0);
        assertEquals(StringConstants.BREAKFAST_LABEL, field.getLabel());
        assertEquals(StringConstants.GENERAL_YES, field.getValue());

        field = fieldList.get(1);
        assertEquals(StringConstants.OVERNIGHT, field.getLabel());
        assertEquals(StringConstants.GENERAL_YES, field.getValue());
    }

    /**
     * Expense Type is Daily Allowance (FXMLS)
     * One fixed TA with breakfast PRO and Dinner PRO.
     * Control data YES for show breakfast and breakfast label set.
     * Control data NO for show dinner and dinner label set.
     *
     * No overnight item in itemization
     * Show overnight checkbox YES and Label set.
     *
     * Expected output: One field which shows the breakfast and no field for overnight.
     */
    @Test
    @Config(shadows = {ActivityDouble.class})
    public void testDailyAllowanceOvernightNoItemization() {

        FixedTAControllerDouble controller = new FixedTAControllerDouble(new Activity());
        List<FixedTravelAllowance> taList = new ArrayList<>();
        FixedTravelAllowance ta = createTestFixedTAMeals("PRO", null, "PRO");
        ta.setOvernightIndicator(true);
        taList.add(ta);
        controller.setFixedTAList(taList);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.BREAKFAST_PROVIDED_LABEL, StringConstants.BREAKFAST_LABEL);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX, "Y");
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.DINNER_PROVIDED_LABEL, StringConstants.DINNER_LABEL);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX, "N");

        controller.getControlData().putControlData(FixedTravelAllowanceControlData.OVERNIGHT_LABEL, StringConstants.OVERNIGHT);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.SHOW_OVERNIGHT_CHECKBOX, "Y");

        ExpenseReportEntryDetailDouble expRepEntryDetail = new ExpenseReportEntryDetailDouble();
        expRepEntryDetail.taDayKey = "1";
        expRepEntryDetail.expKey = "FXMLS";

        // Check if test data is available.
        assertNotNull(controller.getFixedTA("1").getBreakfastProvision());
        assertNotNull(controller.getFixedTA("1").getDinnerProvision());
        assertNull(controller.getFixedTA("1").getLunchProvision());

        // Test execution
        factory = new ExpenseEntryTAFieldFactory(new Activity(), expRepEntryDetail, controller);

        // Assert
        List<ExpenseReportFormField> fieldList = factory.getFormFields();
        assertEquals(1, fieldList.size());

        ExpenseReportFormField field = fieldList.get(0);
        assertEquals(StringConstants.BREAKFAST_LABEL, field.getLabel());
        assertEquals(StringConstants.GENERAL_YES, field.getValue());

    }

    /**
     * Expense Type is Daily Allowance (FXLDG)
     * One fixed TA with breakfast PRO and Dinner PRO.
     * Control data YES for show breakfast and breakfast label set.
     * Control data NO for show dinner and dinner label set.
     *
     * No overnight item in itemization
     * Show overnight checkbox YES and Label set.
     *
     * Lodging type label set and lodging type set
     *
     * Expected output: One field which shows the lodging type and one field for overnight. No fields for meals.
     */
    @Test
    @Config(shadows = {ActivityDouble.class})
    public void testLodingExpense() {

        FixedTAControllerDouble controller = new FixedTAControllerDouble(new Activity());
        List<FixedTravelAllowance> taList = new ArrayList<>();
        FixedTravelAllowance ta = createTestFixedTAMeals("PRO", null, "PRO");
        ta.setOvernightIndicator(true);
        ta.setLodgingType(new LodgingType("LOG", StringConstants.LODGING_TYPE));
        taList.add(ta);
        controller.setFixedTAList(taList);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.BREAKFAST_PROVIDED_LABEL, StringConstants.BREAKFAST_LABEL);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX, "Y");
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.DINNER_PROVIDED_LABEL, StringConstants.DINNER_LABEL);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX, "N");

        controller.getControlData().putControlData(FixedTravelAllowanceControlData.OVERNIGHT_LABEL, StringConstants.OVERNIGHT);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.SHOW_OVERNIGHT_CHECKBOX, "Y");

        controller.getControlData().putControlData(FixedTravelAllowanceControlData.LODGING_TYPE_LABEL, StringConstants.LODGING_TYPE_LABEL);


        ExpenseReportEntryDetailDouble expRepEntryDetail = new ExpenseReportEntryDetailDouble();
        expRepEntryDetail.taDayKey = "1";
        expRepEntryDetail.expKey = "FXLDG";

        // Check if test data is available.
        assertNotNull(controller.getFixedTA("1").getBreakfastProvision());
        assertNotNull(controller.getFixedTA("1").getDinnerProvision());
        assertNull(controller.getFixedTA("1").getLunchProvision());

        // Test execution
        factory = new ExpenseEntryTAFieldFactory(new Activity(), expRepEntryDetail, controller);

        // Assert
        List<ExpenseReportFormField> fieldList = factory.getFormFields();
        assertEquals(2, fieldList.size());

        ExpenseReportFormField field = fieldList.get(0);
        assertEquals(StringConstants.LODGING_TYPE_LABEL, field.getLabel());
        assertEquals(StringConstants.LODGING_TYPE, field.getValue());

        field = fieldList.get(1);
        assertEquals(StringConstants.OVERNIGHT, field.getLabel());
        assertEquals(StringConstants.GENERAL_YES, field.getValue());

    }


    /**
     * Expense Type is Daily Allowance (FXLDG)
     * Lodging type is null in TA
     *
     * Expected output: One field which shows the overnight.
     */
    @Test
    @Config(shadows = {ActivityDouble.class})
    public void testLodingExpenseWithLodgingTypeNull() {

        FixedTAControllerDouble controller = new FixedTAControllerDouble(new Activity());
        List<FixedTravelAllowance> taList = new ArrayList<>();
        FixedTravelAllowance ta = createTestFixedTAMeals(null, null, null);
        ta.setOvernightIndicator(true);
        taList.add(ta);
        controller.setFixedTAList(taList);

        controller.getControlData().putControlData(FixedTravelAllowanceControlData.OVERNIGHT_LABEL, StringConstants.OVERNIGHT);
        controller.getControlData().putControlData(FixedTravelAllowanceControlData.SHOW_OVERNIGHT_CHECKBOX, "Y");

        controller.getControlData().putControlData(FixedTravelAllowanceControlData.LODGING_TYPE_LABEL, StringConstants.LODGING_TYPE_LABEL);


        ExpenseReportEntryDetailDouble expRepEntryDetail = new ExpenseReportEntryDetailDouble();
        expRepEntryDetail.taDayKey = "1";
        expRepEntryDetail.expKey = "FXLDG";

        // Test execution
        factory = new ExpenseEntryTAFieldFactory(new Activity(), expRepEntryDetail, controller);

        // Assert
        List<ExpenseReportFormField> fieldList = factory.getFormFields();
        assertEquals(1, fieldList.size());

        ExpenseReportFormField field = fieldList.get(0);
        assertEquals(StringConstants.OVERNIGHT, field.getLabel());
        assertEquals(StringConstants.GENERAL_YES, field.getValue());
    }


}
