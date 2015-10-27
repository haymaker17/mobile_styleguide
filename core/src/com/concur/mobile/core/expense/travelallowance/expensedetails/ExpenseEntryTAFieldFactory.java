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
import com.concur.mobile.core.expense.travelallowance.datamodel.LodgingType;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.view.SpinnerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by D049515 on 15.07.2015.
 */
public class ExpenseEntryTAFieldFactory {

    private static final String BREAKFAST_FIELD_ID = "breakfastField";
    private static final String LUNCH_FIELD_ID = "lunchField";
    private static final String DINNER_FIELD_ID = "dinnerField";
    private static final String OVERNIGHT_FIELD_ID = "overnightField";
    private static final String LODGING_FIELD_ID = "lodgingField";

    private static final String MEALS = "FXMLS";
    private static final String LODGING = "FXLDG";
    private static final String OVERNIGHT = "OVRNT";


    private ExpenseReportEntryDetail expRepEntryDetail;

    private Context context;

    private List<ExpenseReportFormField> formFields;

    private FixedTravelAllowanceController controller;

    private boolean isEditable;

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

        this.isEditable = false;
    }

    public void setIsEditable(boolean isEditable) {
        this.isEditable = isEditable;
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

        ExpenseReportFormField.AccessType accessType = isEditable ? ExpenseReportFormField.AccessType.RW : ExpenseReportFormField.AccessType.RO;
        //ExpenseReportFormField.ControlType controlType = isEditable ? ExpenseReportFormField.ControlType.PICK_LIST : ExpenseReportFormField.ControlType.EDIT;
        ExpenseReportFormField.ControlType controlType = ExpenseReportFormField.ControlType.PICK_LIST;
        //ExpenseReportFormField.DataType dataType = isEditable ? ExpenseReportFormField.DataType.BOOLEAN : ExpenseReportFormField.DataType.VARCHAR;
        ExpenseReportFormField.DataType dataType =  ExpenseReportFormField.DataType.BOOLEAN;


        if (ta != null) {
            String breakfastLabel = controlData.getLabel(FixedTravelAllowanceControlData.BREAKFAST_PROVIDED_LABEL);
            String lunchLabel = controlData.getLabel(FixedTravelAllowanceControlData.LUNCH_PROVIDED_LABEL);
            String dinnerLabel = controlData.getLabel(FixedTravelAllowanceControlData.DINNER_PROVIDED_LABEL);

            ICode breakfast = ta.getBreakfastProvision();
            if (breakfast != null && controller.showBreakfastProvision()) {
                if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX)) {
                    controlType = ExpenseReportFormField.ControlType.EDIT;
                }
                ExpenseReportFormField field1 = new ExpenseReportFormField(BREAKFAST_FIELD_ID, breakfastLabel,
                        breakfast.getDescription(), accessType, controlType, dataType, true);

                if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_PICKLIST)) {

                    field1.setDataType(ExpenseReportFormField.DataType.VARCHAR);
                    field1.setStaticList(getItems(controlData.getProvidedMealValues()));
                    field1.setLiKey(ta.getBreakfastProvision().getCode());
                    field1.setValue(ta.getBreakfastProvision().getDescription());
                } else {
                    field1.setLiKey(convertICodeToConcurBool(breakfast));
                }


                list.add(field1);
            }

            ICode lunch = ta.getLunchProvision();
            if (lunch != null && controller.showLunchProvision()) {
                if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX)) {
                    controlType = ExpenseReportFormField.ControlType.EDIT;
                }
                ExpenseReportFormField field2 = new ExpenseReportFormField(LUNCH_FIELD_ID, lunchLabel, lunch.getDescription(),
                        accessType, controlType, dataType, true);
                if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_PICKLIST)) {
                    field2.setDataType(ExpenseReportFormField.DataType.VARCHAR);
                    field2.setStaticList(getItems(controlData.getProvidedMealValues()));
                    field2.setLiKey(ta.getLunchProvision().getCode());
                    field2.setValue(ta.getLunchProvision().getDescription());
                } else {
                    field2.setLiKey(convertICodeToConcurBool(lunch));
                }
                list.add(field2);
            }

            ICode dinner = ta.getDinnerProvision();
            if (dinner != null && controller.showDinnerProvision()) {
                if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX)) {
                    controlType = ExpenseReportFormField.ControlType.EDIT;
                }
                ExpenseReportFormField field3 = new ExpenseReportFormField(DINNER_FIELD_ID, dinnerLabel, dinner.getDescription(),
                        accessType, controlType, dataType, true);
                if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_PICKLIST)) {
                    field3.setDataType(ExpenseReportFormField.DataType.VARCHAR);
                    field3.setStaticList(getItems(controlData.getProvidedMealValues()));
                    field3.setLiKey(ta.getDinnerProvision().getCode());
                    field3.setValue(ta.getDinnerProvision().getDescription());
                } else {
                    field3.setLiKey(convertICodeToConcurBool(dinner));
                }
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

        FixedTravelAllowanceControlData controlData = controller.getControlData();

        ExpenseReportFormField.AccessType accessType = isEditable ? ExpenseReportFormField.AccessType.RW : ExpenseReportFormField.AccessType.RO;

        // Lodging Type
        if (ta != null && ta.getLodgingType() != null) {
            ICode lodgingType = ta.getLodgingType();

            // Get lodging type field label
            String lodgingTypeLabel = controller.getControlData().getLabel(FixedTravelAllowanceControlData.LODGING_TYPE_LABEL);

            ExpenseReportFormField field1 = new ExpenseReportFormField(LODGING_FIELD_ID, lodgingTypeLabel,
                    lodgingType.getDescription(), accessType, ExpenseReportFormField.ControlType.PICK_LIST,
                    ExpenseReportFormField.DataType.VARCHAR, true);
            field1.setStaticList(getItems(controlData.getLodgingTypeValues()));

            field1.setLiKey(lodgingType.getCode());
            field1.setValue(lodgingType.getDescription());
            formFields.add(field1);
        }
    }

    private void createOvernightField() {
        FixedTravelAllowance ta = controller.getFixedTA(expRepEntryDetail.taDayKey);

        if (ta != null && ta.isLastDay()) {
            // Overnight flag not needed for the last day
            return;
        }

        ExpenseReportFormField.AccessType accessType = isEditable ? ExpenseReportFormField.AccessType.RW : ExpenseReportFormField.AccessType.RO;
        ExpenseReportFormField.ControlType controlType = isEditable ? ExpenseReportFormField.ControlType.CHECKBOX : ExpenseReportFormField.ControlType.EDIT;
        ExpenseReportFormField.DataType dataType = isEditable ? ExpenseReportFormField.DataType.BOOLEAN : ExpenseReportFormField.DataType.VARCHAR;

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

            ExpenseReportFormField field2 = new ExpenseReportFormField(OVERNIGHT_FIELD_ID, overnightLabel, overNightIndicator,
                    accessType, controlType, dataType, true);
            if (isEditable) {
                if (ta.getOvernightIndicator()){
                    field2.setLiKey("Y");
                }else {
                    field2.setLiKey("N");
                }
            }
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


    private SpinnerItem[] getItems(Map<String, ICode> codeMap) {
        if (codeMap == null || codeMap.size() == 0) {
            return new SpinnerItem[0];
        }
        SpinnerItem[] items = new SpinnerItem[codeMap.size()];
        int i = 0;
        for (Map.Entry<String,ICode> entry : codeMap.entrySet()){
            items[i] = new SpinnerItem(entry.getKey(), entry.getValue().getDescription());
            i++;
        }
        return items;
    }


    private String convertICodeToConcurBool (ICode code){
        if (MealProvision.PROVIDED_CODE.equals(code.getCode())){
            return "Y";
        }else if (MealProvision.NOT_PROVIDED_CODE.equals(code.getCode())){
            return "N";
        }
        return null;
    }

    private MealProvision convertConcurBoolToICode (String concurBool){
        if (concurBool.equals("Y")){
            return new MealProvision(MealProvision.PROVIDED_CODE, "");
        }else if (concurBool.equals("N")){
            return new MealProvision(MealProvision.NOT_PROVIDED_CODE, "");
        }
        return null;
    }

    public FixedTravelAllowance generateFromFormFields(List<ExpenseReportFormField> fieldList, String taDayKey) {
        FixedTravelAllowance ta = controller.getFixedTA(taDayKey);
        FixedTravelAllowanceControlData controlData = controller.getControlData();

        if (ta == null || fieldList == null) {
            return null;
        }

        for (ExpenseReportFormField field : fieldList) {
            if (BREAKFAST_FIELD_ID.equals(field.getId())) {
                if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_PICKLIST)) {
                    MealProvision breakfastProvision = new MealProvision(field.getLiKey(), field.getValue());
                    ta.setBreakfastProvision(breakfastProvision);
                } else {
                    ta.setBreakfastProvision(convertConcurBoolToICode(field.getLiKey()));
                }
            }

            if (LUNCH_FIELD_ID.equals(field.getId())) {
                if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_PICKLIST)) {
                    MealProvision lunchProvision = new MealProvision(field.getLiKey(), field.getValue());
                    ta.setLunchProvision(lunchProvision);
                } else {
                    ta.setLunchProvision(convertConcurBoolToICode(field.getLiKey()));
                }
            }

            if (DINNER_FIELD_ID.equals(field.getId())) {
                if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_PICKLIST)) {
                    MealProvision dinnerProvision = new MealProvision(field.getLiKey(), field.getValue());
                    ta.setDinnerProvision(dinnerProvision);
                } else {
                    ta.setDinnerProvision(convertConcurBoolToICode(field.getLiKey()));
                }
            }

            if (OVERNIGHT_FIELD_ID.equals(field.getId())) {
                ta.setOvernightIndicator(StringUtilities.toBoolean(field.getLiKey()));
            }

            if (LODGING_FIELD_ID.equals(field.getId())) {
                LodgingType lodgingType = new LodgingType(field.getLiKey(), field.getValue());
                ta.setLodgingType(lodgingType);
            }
        }

        return ta;
    }


}
