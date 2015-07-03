package com.concur.mobile.core.expense.travelallowance.controller;

import com.concur.mobile.core.expense.travelallowance.datamodel.LodgingType;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the control data object which holds the control data coming together with the fixed TAs for a specific expense report.
 * 
 * There are different types of control data. 1. Flags: All yes (Y) and no (N) flags are converted to boolean. 2. Strings: There
 * are also some localized labels from the control data. 3. Code Lists: Code lists are stored in HashMaps.
 * 
 * In case an enhancement is needed because of new fields in the control data coming from backend following has to be done: First
 * identify the type of data (Flag, String, Code List).
 *
 * In case it's a flag or string a public String constant for the name of the
 * attribute is needed. Keep the same name as in the backend. This simplifies the add logic. The constant needs to be added in the
 * #attributeKeys list (@see initAttributeKeys)Usually the data is added by a parser. The parser can call #putControlData.
 *
 * If there is a new code list, a new HashMap for that code list is needed. A parser can set the HashMap by a setter method.
 * 
 * @author Patricius Komarnicki Created on 03.07.2015.
 */
public class FixedTravelAllowanceControlData implements Serializable {

    private static final long serialVersionUID = -8059706149423619816L;

    public static final String SHOW_BREAKFAST_PROVIDED_CHECKBOX = "ShowBreakfastProvidedCheckBox";
    public static final String SHOW_BREAKFAST_PROVIDED_PICKLIST = "ShowBreakfastProvidedPickList";
    public static final String SHOW_LUNCH_PROVIDED_CHECKBOX = "ShowLunchProvidedCheckBox";
    public static final String SHOW_LUNCH_PROVIDED_PICKLIST = "ShowLunchProvidedPickList";
    public static final String SHOW_DINNER_PROVIDED_CHECKBOX = "ShowDinnerProvidedCheckBox";
    public static final String SHOW_DINNER_PROVIDED_PICKLIST = "ShowDinnerProvidedPickList";

    public static final String SHOW_OVERNIGHT_AS_NIGHT_ALLOWANCE = "ShowOvernightAsNightAllowance";
    public static final String SHOW_OVERNIGHT_CHECKBOX = "ShowOvernightCheckBox";

    public static final String SHOW_USER_ENTRY_OF_BREAKFAST_AMOUNT = "ShowUserEntryOfBreakfastAmount";
    public static final String SHOW_USER_ENTRY_OF_MEALS_AMOUNT = "ShowUserEntryOfMealsAmount";

    public static final String SHOW_ABOVE_LIMIT = "ShowAboveLimit";
    public static final String SHOW_MEALS_BASE_AMOUNT = "ShowMealsBaseAmount";
    public static final String SHOW_MEALS_BASE_AMOUNT_IN_RATE_CURRENCY = "ShowMealsBaseAmountInRateCurrency";
    public static final String SHOW_LODGING_TYPE_PICKLIST = "ShowLodgingTypePickList";

    public static final String SHOW_PERCENT_RULE_CHECKBOX = "ShowPercentRuleCheckBox";
    public static final String SHOW_EXTENDED_TRIP_CHECKBOX = "ShowExtendedTripCheckBox";
    public static final String SHOW_MUNICIPALITY_CHECKBOX = "ShowMunicipalityCheckBox";
    public static final String SHOW_EXCLUDE_CHECKBOX = "ShowExcludeCheckBox";
    public static final String SHOW_ALLOWANCE_AMOUNT = "ShowAllowanceAmount";

    public static final String BREAKFAST_PROVIDED_LABEL = "BreakfastProvidedLabel";
    public static final String DINNER_PROVIDED_LABEL = "DinnerProvidedLabel";
    public static final String LUNCH_PROVIDED_LABEL = "LunchProvidedLabel";


    private List<String> attributeKeys;
    private Map<String, Boolean> controlMap;
    private Map<String, String> labels;
    private Map<String, MealProvision> providedMealValues;
    private Map<String, LodgingType> lodgingTypeValues;


    /**
     * Constructor with initialization logic.
     */
    public FixedTravelAllowanceControlData() {
        this.controlMap = new HashMap<String, Boolean>();
        this.labels = new HashMap<String, String>();
        initAttributeKeys();
    }

    /**
     * In case there is a new constant, this method needs to be adjusted.
     */
    private void initAttributeKeys() {
        attributeKeys = new ArrayList<String>();
        attributeKeys.add(SHOW_BREAKFAST_PROVIDED_CHECKBOX);
        attributeKeys.add(SHOW_BREAKFAST_PROVIDED_PICKLIST);
        attributeKeys.add(SHOW_LUNCH_PROVIDED_CHECKBOX);
        attributeKeys.add(SHOW_LUNCH_PROVIDED_PICKLIST);
        attributeKeys.add(SHOW_DINNER_PROVIDED_CHECKBOX);
        attributeKeys.add(SHOW_DINNER_PROVIDED_PICKLIST);
        attributeKeys.add(SHOW_OVERNIGHT_AS_NIGHT_ALLOWANCE);
        attributeKeys.add(SHOW_OVERNIGHT_CHECKBOX);
        attributeKeys.add(SHOW_USER_ENTRY_OF_BREAKFAST_AMOUNT);
        attributeKeys.add(SHOW_USER_ENTRY_OF_MEALS_AMOUNT);
        attributeKeys.add(SHOW_ABOVE_LIMIT);
        attributeKeys.add(SHOW_MEALS_BASE_AMOUNT);
        attributeKeys.add(SHOW_MEALS_BASE_AMOUNT_IN_RATE_CURRENCY);
        attributeKeys.add(SHOW_LODGING_TYPE_PICKLIST);
        attributeKeys.add(SHOW_PERCENT_RULE_CHECKBOX);
        attributeKeys.add(SHOW_EXTENDED_TRIP_CHECKBOX);
        attributeKeys.add(SHOW_MUNICIPALITY_CHECKBOX);
        attributeKeys.add(SHOW_EXCLUDE_CHECKBOX);
        attributeKeys.add(SHOW_ALLOWANCE_AMOUNT);
        attributeKeys.add(BREAKFAST_PROVIDED_LABEL);
        attributeKeys.add(DINNER_PROVIDED_LABEL);
        attributeKeys.add(LUNCH_PROVIDED_LABEL);
    }

    /**
     * 
     * @return All known flag and string attributes for control data. Code list atributes are not included.
     */
    public List<String> getAttributeKeys() {
        return attributeKeys;
    }

    /**
     * To put a control data which is a flag or a string.
     * 
     * @param key
     *            The key of the control data is the name of the field. For the key a constant is needed. There is no check
     *            whether the key is known so this method accepts all keys.
     * @param value
     *            The value of the control data.
     */
    public synchronized void putControlData(String key, String value) {
        if (isBoolean(value)) {
            controlMap.put(key, stringToBoolean(value));
        } else {
            labels.put(key, value);
        }
    }

    /**
     * 
     * @param key
     *            The key for the requested control data.
     * @return The control flag for a specific key. In case the key is unknown the method returns false.
     */
    public synchronized boolean getControlValue(String key) {
        Boolean bool = controlMap.get(key);
        if (bool == null) {
            bool = false;
        }
        return bool;
    }

    /**
     *
     * @param key
     *            The key for the requested control data.
     * @return The String for a specific key. In case the key is unknown the method returns an empty String.
     */
    public synchronized String getLabel(String key) {
        String label = labels.get(key);
        if (label == null) {
            label = "";
        }
        return label;
    }



    private boolean isBoolean(String value) {
        if (StringUtilities.isNullOrEmpty(value)) {
            return false;
        }

        if (value.equalsIgnoreCase("y") || value.equalsIgnoreCase("n")) {
            return true;
        }

        return false;
    }

    private Boolean stringToBoolean(String value) {
        if (value.equalsIgnoreCase("y")) {
            return true;
        } else {
            return false;
        }
    }

    public Map<String, MealProvision> getProvidedMealValues() {
        if (providedMealValues == null) {
            providedMealValues = new HashMap<String, MealProvision>();
        }
        return providedMealValues;
    }

    /**
     * To set code list of provided meal values.
     * 
     * @param providedMealValues
     *            The list of meal values.
     */
    public void setProvidedMealValues(Map<String, MealProvision> providedMealValues) {
        this.providedMealValues = providedMealValues;
    }

    /**
     *
     * @return The lodging type values.
     */
    public Map<String, LodgingType> getLodgingTypeValues() {
        if (lodgingTypeValues == null) {
            lodgingTypeValues = new HashMap<String, LodgingType>();
        }
        return lodgingTypeValues;
    }

    /**
     * To set the lodging type values.
     * 
     * @param lodgingTypeValues
     *            The lodging type values.
     */
    public void setLodgingTypeValues(Map<String, LodgingType> lodgingTypeValues) {
        this.lodgingTypeValues = lodgingTypeValues;
    }
}
