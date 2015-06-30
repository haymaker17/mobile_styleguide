package com.concur.mobile.core.expense.travelallowance.service.parser;

import android.content.Context;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.LodgingType;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvisionEnum;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael Becherer on 26-Jun-15.
 */
public class GetTAFixedAllowancesResponseParser extends BaseParser {

    private static final String FIXED_ALLOWANCE_ROW = "FixedAllowanceRow";
    private static final String PROVIDED_MEAL = "ProvidedMeal";
    private static final String PROVIDED_MEAL_VALUES = "ProvidedMealValues";
    private static final String LODGING_TYPE_VALUES = "LodgingTypeValues";
    private static final String LODGING_TYPE = "LodgingType";

    private List<FixedTravelAllowance> fixedTravelAllowances;
    private FixedTravelAllowance currentAllowance;
    private Context context;
    private String currentStartTag;
    private SimpleDateFormat dateFormat;
    private CodeList<MealProvision> mealCodes;
    private CodeList<LodgingType> lodgingTypeCodes;

    public GetTAFixedAllowancesResponseParser(Context context) {
        this.context = context;
        this.fixedTravelAllowances = new ArrayList<FixedTravelAllowance>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    /**
     * @return  Returns the parsed {@link FixedTravelAllowance} list.
     */
    public List<FixedTravelAllowance> getFixedTravelAllowances() {

        return this.fixedTravelAllowances;
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
        if (PROVIDED_MEAL_VALUES.equals(tag)) {
            this.mealCodes = new CodeList<MealProvision>(tag);
            this.currentStartTag = tag;
        }
        if (LODGING_TYPE_VALUES.equals(tag)) {
            this.lodgingTypeCodes = new CodeList<LodgingType>(tag);
            this.currentStartTag = tag;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleText(String tag, String text) {

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
            if (tag.equals("TaDaKey")) {
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
                } else {
                    currentAllowance.setLodgingType(new LodgingType(text, StringUtilities.EMPTY_STRING));
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
    }

}