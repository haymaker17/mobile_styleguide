package com.concur.mobile.core.expense.travelallowance.controller;

import android.content.Context;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Michael Becherer on 23-Jun-15.
 */
public class FixedTravelAllowanceController {
    /**
     * Builds a string based on the meals provisions (breakfast, lunch and dinner), such
     * as "Provided: Breakfast, Lunch Business Meal: Dinner" for the given fixed travel allowance.
     * Therefore the provision characteristics of breakfast, lunch and dinner (e.g. "Provided")
     * are used for grouping. The order within the result is given by the rule:
     * breakfast before lunch before dinner.
     * With parameter maxGroups the maximum number of groups can be specified.
     *
     * @param allowance The allowance holding the data
     * @param context The activity context in order to retrieve the language dependent resources
     * @param maxGroups The maximum level of string concatenation. Supports values greater 0.
     *
     * @return The textual representation of the meal provisions. Empty String, if
     * there is nothing.
     */
    public String mealsProvisionToText(FixedTravelAllowance allowance, Context context, int maxGroups) {

        if (context == null) {
            return StringUtilities.EMPTY_STRING;
        }

        if (maxGroups < 1) {
            return StringUtilities.EMPTY_STRING;
        }

        String resultString = StringUtilities.EMPTY_STRING;
        List<String> mealsList;
        List<MealProvision> sortedProvisions = new ArrayList<MealProvision>();
        Map<MealProvision, List<String>> provisionMap = new HashMap<MealProvision, List<String>>();

        if (!StringUtilities.isNullOrEmpty(allowance.getBreakfastProvision().getCode())
                && !allowance.getBreakfastProvision().getCode().equals(MealProvision.NOT_PROVIDED_CODE)) {
            mealsList = new ArrayList<String>();
            mealsList.add(context.getString(R.string.itin_breakfast));
            provisionMap.put(allowance.getBreakfastProvision(), mealsList);
            sortedProvisions.add(allowance.getBreakfastProvision());
        }

        if (!StringUtilities.isNullOrEmpty(allowance.getLunchProvision().getCode())
                && !allowance.getLunchProvision().getCode().equals(MealProvision.NOT_PROVIDED_CODE)) {
            if (provisionMap.containsKey(allowance.getLunchProvision())) {
                mealsList = provisionMap.get(allowance.getLunchProvision());
                mealsList.add(context.getString(R.string.itin_lunch));
            } else {
                mealsList = new ArrayList<String>();
                mealsList.add(context.getString(R.string.itin_lunch));
                provisionMap.put(allowance.getLunchProvision(), mealsList);
                sortedProvisions.add(allowance.getLunchProvision());
            }
        }

        if (!StringUtilities.isNullOrEmpty(allowance.getDinnerProvision().getCode())
                && !allowance.getDinnerProvision().getCode().equals(MealProvision.NOT_PROVIDED_CODE)) {
            if (provisionMap.containsKey(allowance.getDinnerProvision())) {
                mealsList = provisionMap.get(allowance.getDinnerProvision());
                mealsList.add(context.getString(R.string.itin_dinner));
            } else {
                mealsList = new ArrayList<String>();
                mealsList.add(context.getString(R.string.itin_dinner));
                provisionMap.put(allowance.getDinnerProvision(), mealsList);
                sortedProvisions.add(allowance.getDinnerProvision());
            }
        }

        int j = 0;
        for (MealProvision key: sortedProvisions) {
            j++;
            if (j > maxGroups) {
                break;
            }
            int i = 0;
            resultString = resultString + key + ": ";
            for (String value: provisionMap.get(key)){
                i++;
                resultString = resultString + value;
                if (i < provisionMap.get(key).size()) {
                    resultString = resultString + ", ";
                }
            }
            if (j < sortedProvisions.size()) {
                resultString = resultString + " ";
            }
        }
        return resultString;
    }
}
