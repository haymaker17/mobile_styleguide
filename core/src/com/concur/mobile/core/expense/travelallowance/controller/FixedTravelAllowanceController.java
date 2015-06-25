package com.concur.mobile.core.expense.travelallowance.controller;

import android.content.Context;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowanceTestData;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Michael Becherer on 23-Jun-15.
 */
public class FixedTravelAllowanceController {

    private static final String CLASS_TAG = FixedTravelAllowanceController.class.getSimpleName();

    /**
     * The list of registered listeners being notified from this controller as soon as data
     * requests have been answered.
     */
    private List<IServiceRequestListener> listeners;

    /**
     * Use mocked data
     */
    private boolean useMockData = true;

    /**
     * The context needed in order to access resources
     */
    private Context context;

    /**
     * The list of travel allowances this controller is dealing with
     */
    private List<FixedTravelAllowance> fixedTravelAllowances;

    /**
     * Denotes, whether valid fixed travel allowance data is available with this object
     */
    private boolean isDataAvailable;

    /**
     * Creates an instance and initializes the object
     */
    public FixedTravelAllowanceController(Context context) {
        this.fixedTravelAllowances = new ArrayList<FixedTravelAllowance>();
        this.isDataAvailable = false;
        this.context = context;
        this.listeners = new ArrayList<IServiceRequestListener>();
    }

    /**
     * Groups fixed travel allowances according to their natural sorting order from where a
     * list is derived, which contains two kind of objects: locations and allowances.
     * The list will be lead by the first location found in the sorted list followed by its
     * allowances pointing to the same location until the next different location will lead
     * the next group of allowances within the result list.
     *
     * @return The sorted and grouped list containing locations and allowances. If there is no
     * data the method will return an empty list.
     */
    public List<Object> getLocationsAndAllowances() {

        if (!isDataAvailable) {
            getData();
        }

        List<String> sortedLocations = new ArrayList<String>();
        List<FixedTravelAllowance> fixedTAList = new ArrayList<FixedTravelAllowance>(this.fixedTravelAllowances);
        Collections.sort(fixedTAList);
        Map<String, List<FixedTravelAllowance>> fixedTAGroups = new HashMap<String, List<FixedTravelAllowance>>();
        List<Object> locationAndTAList = new ArrayList<Object>();

        for (FixedTravelAllowance allowance : fixedTAList) {
            List<FixedTravelAllowance> taList;
            if (fixedTAGroups.containsKey(allowance.getLocationName())) {
                taList = fixedTAGroups.get(allowance.getLocationName());
                taList.add(allowance);
            } else {
                taList = new ArrayList<FixedTravelAllowance>();
                taList.add(allowance);
                fixedTAGroups.put(allowance.getLocationName(), taList);
                sortedLocations.add(allowance.getLocationName());
            }
        }

        if (fixedTAGroups.keySet().size() > 1) {
            for (String key : sortedLocations) {
                locationAndTAList.add(key);
                for (FixedTravelAllowance value : fixedTAGroups.get(key)) {
                    locationAndTAList.add(value);
                }
            }
        } else {
            locationAndTAList.addAll(fixedTAList);
        }

        return locationAndTAList;
    }

    /**
     * Checks, whether the fixed travel allowances are associated with several different
     * Locations
     *
     * @return true, if the fixed travel allowances are associated with several different
     * locations
     */
    public boolean hasMultipleGroups() {

        if (!isDataAvailable) {
            getData();
        }

        boolean multipleGroups = false;
        Iterator<FixedTravelAllowance> it = fixedTravelAllowances.iterator();
        while (multipleGroups == false && it.hasNext()) {
            FixedTravelAllowance allowance = it.next();
            if (allowance.getLocationName() != fixedTravelAllowances.get(0).getLocationName()) {
                multipleGroups = true;
            }
        }

        return multipleGroups;
    }

    /**
     * Builds a string based on the meals provisions (breakfast, lunch and dinner), such
     * as "Provided: Breakfast, Lunch Business Meal: Dinner" for the given fixed travel allowance.
     * Therefore the provision characteristics of breakfast, lunch and dinner (e.g. "Provided")
     * are used for grouping. The order within the result is given by the rule:
     * breakfast before lunch before dinner.
     * With parameter maxGroups the maximum number of groups can be specified.
     *
     * @param allowance The allowance holding the data
     * @param maxGroups The maximum level of string concatenation. Supports values greater 0.
     * @return The textual representation of the meal provisions. Empty String, if
     * there is nothing.
     */
    public String mealsProvisionToText(FixedTravelAllowance allowance, int maxGroups) {

        if (this.context == null) {
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
        for (MealProvision key : sortedProvisions) {
            j++;
            if (j > maxGroups) {
                break;
            }
            int i = 0;
            resultString = resultString + key + ": ";
            for (String value : provisionMap.get(key)) {
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

    /**
     * Retrieves the data for fixed travel allowances
     */
    private void getData() {
        if (useMockData) {
            FixedTravelAllowanceTestData testData = new FixedTravelAllowanceTestData();
            fixedTravelAllowances = testData.getAllowances(false);
            isDataAvailable = true;
        }
    }
}
