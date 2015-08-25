package com.concur.mobile.core.expense.travelallowance.expensedetails;

import android.content.Context;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceControlData;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.ICode;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by D049515 on 15.07.2015.
 */
public class ExpenseEntryTAFieldFactory {

    private static final String MEALS = "FXMLS";
    private static final String LODGING = "FXLDG";
    private static final String OVERNIGHT = "OVRNT";


    private ExpenseReportEntryDetail expRepEntryDetail;

    private Context context;

    private List<ExpenseReportFormField> formFields;

    private FixedTravelAllowanceController controller;

    public ExpenseEntryTAFieldFactory(Context context, ExpenseReportEntryDetail expRepEntryDetail) {
        this(context, expRepEntryDetail, null);
    }

    public ExpenseEntryTAFieldFactory(Context context, ExpenseReportEntryDetail expRepEntryDetail, FixedTravelAllowanceController controller) {
        this.expRepEntryDetail = expRepEntryDetail;
        this.context = context;
        this.formFields = new ArrayList<ExpenseReportFormField>();
        if (controller == null) {
            ConcurCore app = (ConcurCore) context.getApplicationContext();
            this.controller = app.getTaController().getFixedTravelAllowanceController();
        } else {
            this.controller = controller;
        }
    }

    public List<ExpenseReportFormField> getFormFields() {

        String expTypeCode = expRepEntryDetail.expKey;
        if (MEALS.equals(expTypeCode)) {
            createMealsFields();
            if (showOvernightOnDailyAllowanceMeals()) {
                createOvernightField();
            }
        }

        if (LODGING.equals(expTypeCode)) {
            createLodgingTypeField();
            createOvernightField();
        }

        return formFields;
    }

    private void createMealsFields() {
        FixedTravelAllowanceControlData controlData = controller.getControlData();
        FixedTravelAllowance ta = controller.getFixedTA(expRepEntryDetail.taDayKey);

        List<ExpenseReportFormField> list = new ArrayList<ExpenseReportFormField>();

        if (ta != null) {
            String breakfastLabel = controlData.getLabel(FixedTravelAllowanceControlData.BREAKFAST_PROVIDED_LABEL);
            String lunchLabel = controlData.getLabel(FixedTravelAllowanceControlData.LUNCH_PROVIDED_LABEL);
            String dinnerLabel = controlData.getLabel(FixedTravelAllowanceControlData.DINNER_PROVIDED_LABEL);

            ICode breakfast = ta.getBreakfastProvision();
            if (breakfast != null && controller.showBreakfastProvision()) {
                ExpenseReportFormField field1 = new ExpenseReportFormField("1", breakfastLabel,
                        breakfast.getDescription(), ExpenseReportFormField.AccessType.RO, ExpenseReportFormField.ControlType.EDIT,
                        ExpenseReportFormField.DataType.VARCHAR, true);
                list.add(field1);
            }

            ICode lunch = ta.getLunchProvision();
            if (lunch != null && controller.showLunchProvision()) {
                ExpenseReportFormField field2 = new ExpenseReportFormField("2", lunchLabel, lunch.getDescription(),
                        ExpenseReportFormField.AccessType.RO, ExpenseReportFormField.ControlType.EDIT, ExpenseReportFormField.DataType.VARCHAR,
                        true);
                list.add(field2);
            }

            ICode dinner = ta.getDinnerProvision();
            if (dinner != null && controller.showDinnerProvision()) {
                ExpenseReportFormField field3 = new ExpenseReportFormField("3", dinnerLabel, dinner.getDescription(),
                        ExpenseReportFormField.AccessType.RO, ExpenseReportFormField.ControlType.EDIT, ExpenseReportFormField.DataType.VARCHAR,
                        true);
                list.add(field3);
            }
        }

        if (ta == null || list.isEmpty()) {
            String noAdjustments = context.getString(R.string.ta_no_adjustments);
            list.add(new ExpenseReportFormField("1", null, noAdjustments, ExpenseReportFormField.AccessType.RO,
                    ExpenseReportFormField.ControlType.EDIT, ExpenseReportFormField.DataType.VARCHAR, true));
        }

        formFields.addAll(list);

    }

    private void createLodgingTypeField() {
        FixedTravelAllowance ta = controller.getFixedTA(expRepEntryDetail.taDayKey);

        // Lodging Type
        if (ta != null && ta.getLodgingType() != null) {
            ICode lodgingType = ta.getLodgingType();

            // Get lodging type field label
            String lodgingTypeLabel = controller.getControlData().getLabel(FixedTravelAllowanceControlData.LODGING_TYPE_LABEL);

            ExpenseReportFormField field1 = new ExpenseReportFormField("1", lodgingTypeLabel,
                    lodgingType.getDescription(), ExpenseReportFormField.AccessType.RO, ExpenseReportFormField.ControlType.EDIT,
                    ExpenseReportFormField.DataType.VARCHAR, true);
            formFields.add(field1);
        }
    }

    private void createOvernightField() {
        FixedTravelAllowance ta = controller.getFixedTA(expRepEntryDetail.taDayKey);

        // Overnight Indicator
        boolean showOvernight = controller.getControlData().getControlValue(FixedTravelAllowanceControlData.SHOW_OVERNIGHT_CHECKBOX);

        if (ta != null && showOvernight) {
            // Overnight indicator label
            String overnightLabel = controller.getControlData().getLabel(FixedTravelAllowanceControlData.OVERNIGHT_LABEL);

            // Overnight indicator text (yes / no)
            String overNightIndicator = "";
            if (ta.getOvernightIndicator()) {
                overNightIndicator = context.getString(R.string.general_yes);
            } else {
                overNightIndicator = context.getString(R.string.general_no);
            }

            ExpenseReportFormField field2 = new ExpenseReportFormField("2", overnightLabel, overNightIndicator,
                    ExpenseReportFormField.AccessType.RO, ExpenseReportFormField.ControlType.EDIT,
                    ExpenseReportFormField.DataType.VARCHAR, true);
            formFields.add(field2);
        }
    }

    private boolean showOvernightOnDailyAllowanceMeals() {
        FixedTravelAllowanceControlData controlData = controller.getControlData();
        boolean showCheckBox = controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_OVERNIGHT_CHECKBOX);
       // boolean showLodgingTypePickList = controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LODGING_TYPE_PICKLIST);
        boolean hasLodgeInItemization = false;
        List<ExpenseReportEntry> itemization = expRepEntryDetail.getItemizations();
        if (itemization != null) {
            for (ExpenseReportEntry entry : itemization) {
                if (OVERNIGHT.equals(entry.expKey)) {
                    hasLodgeInItemization = true;
                }
            }
        }
        if (showCheckBox
                && hasLodgeInItemization) {
            return true;
        } else {
            return false;
        }
    }

}
