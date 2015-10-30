package com.concur.mobile.core.expense.travelallowance.datamodel;

import java.io.Serializable;

/**
 * Representation of travel allowance configuration of the user (employee)
 *
 * Created by D023077 on 01.07.2015.
 */

public class TravelAllowanceConfiguration implements Serializable{

    private static final long serialVersionUID = -8125890049086381735L;

    public static final String FIXED = "FIXED";

    /**
     * Indicates, whether border cross time should be used or not
     */
    private boolean useBorderCrossTime;

    private boolean useLodgingType;

    private  boolean useOvernight;

    /**
     * All itineraries which should be combined in one expense report must have the same configuration code
     */
    private String configCode;

    /**
     * Indicates whether ProvidedBreakfast is editable (more precise: should appear)
     */
    private String deductForProvidedBreakfast;

    /**
     * Indicates whether ProvidedLunch is editable (more precise: should appear)
     */
    private String deductForProvidedLunch;

    /**
     * Indicates whether ProvidedDinner is editable (more precise: should appear)
     */
    private String deductForProvidedDinner;

    /**
     * Indicates that the Breakfast-Provided should be defaulted with 'Provided'
     */
    private String defaultBreakfastToProvided;

    /**
     * Indicates that the Lunch-Provided should be defaulted with 'Provided'
     */
    private String defaultLunchToProvided;

    /**
     * Indicates that the Dinner-Provided should be defaulted with 'Provided'
     */
    private String defaultDinnerToProvided;

    /**
     * Which type of allowances are allowed
     */
    private String lodgingTat;

    /**
     * If this String is filled the meals provision isn't only a Yes/No decision,
     * but a 'picking' from a code list, which is identified by the content of String
     */
    private String mealDeductionList;

    private String mealsTat;

    private  String singleRowCheck;

    private  String tacKey;

    private boolean displayWizard;


    public String getConfigCode() {
        return configCode;
    }

    public void setConfigCode(String configCode) {
        this.configCode = configCode;
    }

    public String getDeductForProvidedBreakfast() {
        return deductForProvidedBreakfast;
    }

    public void setDeductForProvidedBreakfast(String deductForProvidedBreakfast) {
        this.deductForProvidedBreakfast = deductForProvidedBreakfast;
    }

    public String getDeductForProvidedLunch() {
        return deductForProvidedLunch;
    }

    public void setDeductForProvidedLunch(String deductForProvidedLunch) {
        this.deductForProvidedLunch = deductForProvidedLunch;
    }

    public String getDeductForProvidedDinner() {
        return deductForProvidedDinner;
    }

    public void setDeductForProvidedDinner(String deductForProvidedDinner) {
        this.deductForProvidedDinner = deductForProvidedDinner;
    }

    public String getMealDeductionList() {
        return mealDeductionList;
    }

    public void setMealDeductionList(String mealDeductionList) {
        this.mealDeductionList = mealDeductionList;
    }

    public String getDefaultBreakfastToProvided() {
        return defaultBreakfastToProvided;
    }

    public void setDefaultBreakfastToProvided(String defaultBreakfastToProvided) {
        this.defaultBreakfastToProvided = defaultBreakfastToProvided;
    }

    public String getDefaultLunchToProvided() {
        return defaultLunchToProvided;
    }

    public void setDefaultLunchToProvided(String defaultLunchToProvided) {
        this.defaultLunchToProvided = defaultLunchToProvided;
    }

    public String getDefaultDinnerToProvided() {
        return defaultDinnerToProvided;
    }

    public void setDefaultDinnerToProvided(String defaultDinnerToProvided) {
        this.defaultDinnerToProvided = defaultDinnerToProvided;
    }

    public String getLodgingTat() {
        return lodgingTat;
    }

    public void setLodgingTat(String lodgingTat) {
        this.lodgingTat = lodgingTat;
    }

    public String getMealsTat() {
        return mealsTat;
    }

    public void setMealsTat(String mealsTat) {
        this.mealsTat = mealsTat;
    }

    public String getSingleRowCheck() {
        return singleRowCheck;
    }

    public void setSingleRowCheck(String singleRowCheck) {
        this.singleRowCheck = singleRowCheck;
    }

    public String getTacKey() {
        return tacKey;
    }

    public void setTacKey(String tacKey) {
        this.tacKey = tacKey;
    }

    public boolean isUseBorderCrossTime() {
        return useBorderCrossTime;
    }

    public void setUseBorderCrossTime(boolean useBorderCrossTime){
        this.useBorderCrossTime = useBorderCrossTime;
    }

    public boolean isUseLodgingType() {
        return useLodgingType;
    }

    public void setUseLodgingType(boolean useLodgingType) {
        this.useLodgingType = useLodgingType;
    }

    public boolean isUseOvernight() {
        return useOvernight;
    }

    public void setUseOvernight(boolean useOvernight) {
        this.useOvernight = useOvernight;
    }

    public boolean getDisplayWizard() {
        return displayWizard;
    }

    public void setDisplayWizard(boolean displayWizard) {
        this.displayWizard = displayWizard;
    }
}
