package com.concur.mobile.core.expense.travelallowance.service.parser;

import android.content.Context;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceControlData;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.LodgingType;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvisionEnum;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Michael Becherer on 26-Jun-15.
 */
public class GetTAFixedAllowancesResponseParser extends BaseParser {

    private static final String FIXED_ALLOWANCE_ROW = "FixedAllowanceRow";
    private static final String PROVIDED_MEAL = "ProvidedMeal";
    private static final String PROVIDED_MEAL_VALUES = "ProvidedMealValues";
    private static final String LODGING_TYPE_VALUES = "LodgingTypeValues";
    private static final String LODGING_TYPE = "LodgingType";
    private static final String LUNCH_PROVIDED_LABEL = "LunchProvidedLabel";
    private static final String DINNER_PROVIDED_LABEL = "DinnerProvidedLabel";
    private static final String BREAKFAST_PROVIDED_LABEL = "BreakfastProvidedLabel";

    private static final String CONTROL = "Control";


    private List<FixedTravelAllowance> fixedTravelAllowances;
    private FixedTravelAllowance currentAllowance;
    private Context context;
    private String currentStartTag;
    private SimpleDateFormat dateFormat;
    private CodeList<MealProvision> mealCodes;
    private CodeList<LodgingType> lodgingTypeCodes;

    private FixedTravelAllowanceControlData controlData;

    private Map<String, String> mealProvisionLabels;

    private List<Map<String, String>> providedMealsValuesRaw;
    private Map<String, String> providedMealRaw;

    private List<Map<String, String>> lodgingTypeValuesRaw;
    private Map<String, String> lodgingTypeRaw;

    private boolean readingProvidedMealValues = false;
    private boolean readingLodgingTypeValues = false;

    public GetTAFixedAllowancesResponseParser(Context context) {
        this.context = context;
        this.fixedTravelAllowances = new ArrayList<FixedTravelAllowance>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        this.mealProvisionLabels = new HashMap<String, String>();
    }

    /**
     * @return  Returns the parsed {@link FixedTravelAllowance} list.
     */
    public List<FixedTravelAllowance> getFixedTravelAllowances() {
        return this.fixedTravelAllowances;
    }

    public Map<String, String> getMealProvisionLabels() {
        if (mealProvisionLabels == null) {
            mealProvisionLabels = new HashMap<String, String>();
        }
        return mealProvisionLabels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startTag(String tag) {
        super.startTag(tag);

        if (FIXED_ALLOWANCE_ROW.equals(tag)) {
            this.currentStartTag = tag;
            currentAllowance = new FixedTravelAllowance();
        }
//        if (PROVIDED_MEAL_VALUES.equals(tag)) {
//            this.mealCodes = new CodeList<MealProvision>(tag);
//            this.currentStartTag = tag;
//        }
        if (LODGING_TYPE_VALUES.equals(tag)) {
            this.lodgingTypeCodes = new CodeList<LodgingType>(tag);
            this.currentStartTag = tag;
        }

        if (CONTROL.equals(tag)) {
            this.currentStartTag = tag;
        }

        // Handle provided meals values
        if (PROVIDED_MEAL_VALUES.equals(tag)) {
            readingProvidedMealValues = true;
            providedMealsValuesRaw = new ArrayList<Map<String, String>>();
        }

        if (PROVIDED_MEAL.equals(tag)) {
            providedMealRaw = new HashMap<String, String>();
        }

        // Handle lodging type values
        if (LODGING_TYPE_VALUES.equals(tag)) {
            readingProvidedMealValues = true;
            providedMealsValuesRaw = new ArrayList<Map<String, String>>();
        }

        if (PROVIDED_MEAL.equals(tag)) {
            providedMealRaw = new HashMap<String, String>();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleText(String tag, String text) {

        if (CONTROL.equals(currentStartTag)) {
            if (readingProvidedMealValues) {
                providedMealRaw.put(tag, text);
            } else {
                controlData.putControlData(tag, text);
            }
        }

        if (BREAKFAST_PROVIDED_LABEL.equals(tag) || LUNCH_PROVIDED_LABEL.equals(tag)
                || DINNER_PROVIDED_LABEL.equals(tag)) {
            mealProvisionLabels.put(tag, text);
        }

        if (LODGING_TYPE_VALUES.equals(currentStartTag)) {
            if (tag.equals("Code") && lodgingTypeCodes != null) {
                lodgingTypeCodes.setCode(text);
            }
            if (tag.equals("Value") && lodgingTypeCodes != null) {
                lodgingTypeCodes.setValue(text);
            }
        }

        if (PROVIDED_MEAL_VALUES.equals(currentStartTag)) {
            if (tag.equals("Code") && mealCodes != null) {
                mealCodes.setCode(text);
            }
            if (tag.equals("Value") && mealCodes != null) {
                mealCodes.setValue(text);
            }
        }

        if (FIXED_ALLOWANCE_ROW.equals(currentStartTag)) {
            if (tag.equals("TaDayKey")) {
                currentAllowance.setFixedTravelAllowanceId(text);
            }
            if (tag.equals("AllowanceDate")) {
                try {
                    currentAllowance.setDate(dateFormat.parse(text));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (tag.equals("AllowanceAmount")) {
                currentAllowance.setAmount(Double.parseDouble(text));
            }
            if (tag.equals("MealsRateCrnCode")) {
                currentAllowance.setCurrencyCode(text);
            }
            if (tag.equals("MarkedExcluded")) {
                currentAllowance.setExcludedIndicator(StringUtilities.toBoolean(text));
            }
            if (tag.equals("Location")) {
                currentAllowance.setLocationName(text);
            }
            if (tag.equals("BreakfastProvided")) {
                if (mealCodes != null && mealCodes.containsKey(text)) {
                    currentAllowance.setBreakfastProvision(mealCodes.get(text));
                } else {
                    currentAllowance.setBreakfastProvision(MealProvisionEnum.fromCode(text, context));
                }
            }
            if (tag.equals("LunchProvided")) {
                if (mealCodes != null && mealCodes.containsKey(text)) {
                    currentAllowance.setLunchProvision(mealCodes.get(text));
                } else {
                    currentAllowance.setLunchProvision(MealProvisionEnum.fromCode(text, context));
                }
            }
            if (tag.equals("DinnerProvided")) {
                if (mealCodes != null && mealCodes.containsKey(text)) {
                    currentAllowance.setDinnerProvision(mealCodes.get(text));
                } else {
                    currentAllowance.setDinnerProvision(MealProvisionEnum.fromCode(text, context));
                }
            }
            if (tag.equals("Overnight")) {
                currentAllowance.setOvernightIndicator(StringUtilities.toBoolean(text));
            }
            if (tag.equals("LodgingType")) {
                if (lodgingTypeCodes != null && lodgingTypeCodes.containsKey(text)) {
                    currentAllowance.setLodgingType(lodgingTypeCodes.get(text));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endTag(String tag) {
        super.endTag(tag);

        if (FIXED_ALLOWANCE_ROW.equals(tag)) {
            fixedTravelAllowances.add(currentAllowance);
        }
        if (PROVIDED_MEAL.equals(tag) && mealCodes != null) {
            mealCodes.put(new MealProvision(mealCodes.getCode(), mealCodes.getValue()));
        }
        if (LODGING_TYPE.equals(tag) && lodgingTypeCodes != null) {
            lodgingTypeCodes.put(new LodgingType(lodgingTypeCodes.getCode(), lodgingTypeCodes.getValue()));
        }

        if (PROVIDED_MEAL_VALUES.equals(tag)) {
            readingProvidedMealValues = false;
        }

        if (PROVIDED_MEAL.equals(tag)) {
            providedMealsValuesRaw.add(providedMealRaw);
        }
    }

    public void setControlData(FixedTravelAllowanceControlData controlData) {
        this.controlData = controlData;
    }
}