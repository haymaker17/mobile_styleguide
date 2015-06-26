package com.concur.mobile.core.expense.travelallowance.service.parser;

import android.content.Context;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.LodgingType;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.concur.core.R;

/**
 * Created by Michael Becherer on 26-Jun-15.
 */
public class GetTAFixedAllowancesResponseParser extends BaseParser {

    private static final String CONTROL_TAG = "Control";
    private static final String FIXED_ALLOWANCE_ROW = "FixedAllowanceRow";
    private static final String PROVIDED_MEAL = "ProvidedMeal";
    private static final String PROVIDED_MEAL_VALUES = "ProvidedMealValues";

    private List<FixedTravelAllowance> fixedTravelAllowances;
    private FixedTravelAllowance currentAllowance;
    private MealProvision currentMealProvision;
    private Context context;
    private String currentStartTag;
    private SimpleDateFormat dateFormat;
    private Map<String, MealProvision> mealProvisions;

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
            this.currentStartTag = tag;
            this.mealProvisions = new HashMap<String, MealProvision>();
        }

        if (PROVIDED_MEAL.equals(tag)) {
            this.currentStartTag = tag;
            this.currentMealProvision = new MealProvision();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleText(String tag, String text) {

        if (PROVIDED_MEAL.equals(currentStartTag)) {
            if (tag.equals("Code")) {
                currentMealProvision.setCode(text);
            }
            if (tag.equals("Value")) {
                currentMealProvision.setCodeDescription(text);
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
                if (mealProvisions != null && !mealProvisions.isEmpty()) {
                    currentAllowance.setBreakfastProvision(mealProvisions.get(text));
                } else {
                    if (MealProvision.NOT_PROVIDED_CODE.equals(text)) {
                       currentAllowance.setBreakfastProvision(new MealProvision(text, context.getText(R.string.itin_meal_not_provided).toString()));
                    } else if (MealProvision.PROVIDED_CODE.equals(text)) {
                        currentAllowance.setBreakfastProvision(new MealProvision(text, context.getText(R.string.itin_meal_PRO).toString()));
                    }
                }
            }
            if (tag.equals("LunchProvided")) {
                if (mealProvisions != null && !mealProvisions.isEmpty()) {
                    currentAllowance.setLunchProvision(mealProvisions.get(text));
                } else {
                    if (MealProvision.NOT_PROVIDED_CODE.equals(text)) {
                        currentAllowance.setLunchProvision(new MealProvision(text, context.getText(R.string.itin_meal_not_provided).toString()));
                    } else if (MealProvision.PROVIDED_CODE.equals(text)) {
                        currentAllowance.setLunchProvision(new MealProvision(text, context.getText(R.string.itin_meal_PRO).toString()));
                    }
                }
            }
            if (tag.equals("DinnerProvided")) {
                if (mealProvisions != null && !mealProvisions.isEmpty()) {
                    currentAllowance.setDinnerProvision(mealProvisions.get(text));
                } else {
                    if (MealProvision.NOT_PROVIDED_CODE.equals(text)) {
                        currentAllowance.setDinnerProvision(new MealProvision(text, context.getText(R.string.itin_meal_not_provided).toString()));
                    } else if (MealProvision.PROVIDED_CODE.equals(text)) {
                        currentAllowance.setDinnerProvision(new MealProvision(text, context.getText(R.string.itin_meal_PRO).toString()));
                    }
                }
            }
            if (tag.equals("Overnight")) {
                currentAllowance.setOvernightIndicator(StringUtilities.toBoolean(text));
            }
            if (tag.equals("LodgingType")) {
                LodgingType lodgingType = new LodgingType();
                lodgingType.setCode(text);
                //Todo: Get the right value!
                lodgingType.setCodeDescription("@Missing Backend Value@");
                currentAllowance.setLodgingType(lodgingType);
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
        if (PROVIDED_MEAL.equals(tag)) {
            mealProvisions.put(currentMealProvision.getCode(), currentMealProvision);
        }
    }



}

