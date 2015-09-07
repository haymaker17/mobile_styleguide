package com.concur.mobile.core.expense.ta.service;

import java.util.ArrayList;
import java.util.List;

public class FixedAllowances {
    private List<FixedAllowanceRow> rows = new ArrayList<FixedAllowanceRow>();
    private boolean showUserEntryOfBreakfastAmount;
    private boolean showUserEntryOfMealsAmount;
    private boolean showBreakfastProvidedCheckBox;
    private boolean showLunchProvidedCheckBox;
    private boolean showDinnerProvidedCheckBox;
    private boolean showBreakfastProvidedPickList;
    private boolean showLunchProvidedPickList;
    private boolean showDinnerProvidedPickList;
    private boolean showOvernightCheckBox;
    private boolean showOvernightAsNightAllowance;
    private boolean showAboveLimit;
    private boolean showMealsBaseAmount;
    private boolean showLodgingTypePickList;
    private boolean showPercentRuleCheckBox;
    private boolean showExtendedTripCheckBox;
    private boolean showMunicipalityCheckBox;
    private boolean showExcludeCheckBox;
    private boolean showAllowanceAmount;
    private String excludeLabel;
    private String lodgingTypeLabel;
    private String applyPercentRuleLabel;
    private String applyExtendedTripRuleLabel;
    private String municipalAreaLabel;
    private String overnightLabel;
    private String breakfastProvidedLabel;
    private String lunchProvidedLabel;
    private String dinnerProvidedLabel;
    
    public List<FixedAllowanceRow> getRows() {
        return rows;
    }
    
    public void setRows(List<FixedAllowanceRow> rows) {
        this.rows = rows;
    }
    
    public boolean getShowUserEntryOfBreakfastAmount() {
        return showUserEntryOfBreakfastAmount;
    }
    
    public void setShowUserEntryOfBreakfastAmount(boolean showUserEntryOfBreakfastAmount) {
        this.showUserEntryOfBreakfastAmount = showUserEntryOfBreakfastAmount;
    }
    
    public boolean getShowUserEntryOfMealsAmount() {
        return showUserEntryOfMealsAmount;
    }
    
    public void setShowUserEntryOfMealsAmount(boolean showUserEntryOfMealsAmount) {
        this.showUserEntryOfMealsAmount = showUserEntryOfMealsAmount;
    }
    
    public boolean getShowBreakfastProvidedCheckBox() {
        return showBreakfastProvidedCheckBox;
    }
    
    public void setShowBreakfastProvidedCheckBox(boolean showBreakfastProvidedCheckBox) {
        this.showBreakfastProvidedCheckBox = showBreakfastProvidedCheckBox;
    }
    
    public boolean getShowLunchProvidedCheckBox() {
        return showLunchProvidedCheckBox;
    }
    
    public void setShowLunchProvidedCheckBox(boolean showLunchProvidedCheckBox) {
        this.showLunchProvidedCheckBox = showLunchProvidedCheckBox;
    }
    
    public boolean getShowDinnerProvidedCheckBox() {
        return showDinnerProvidedCheckBox;
    }
    
    public void setShowDinnerProvidedCheckBox(boolean showDinnerProvidedCheckBox) {
        this.showDinnerProvidedCheckBox = showDinnerProvidedCheckBox;
    }
    
    public boolean getShowBreakfastProvidedPickList() {
        return showBreakfastProvidedPickList;
    }
    
    public void setShowBreakfastProvidedPickList(boolean showBreakfastProvidedPickList) {
        this.showBreakfastProvidedPickList = showBreakfastProvidedPickList;
    }
    
    public boolean getShowLunchProvidedPickList() {
        return showLunchProvidedPickList;
    }
    
    public void setShowLunchProvidedPickList(boolean showLunchProvidedPickList) {
        this.showLunchProvidedPickList = showLunchProvidedPickList;
    }
    
    public boolean getShowDinnerProvidedPickList() {
        return showDinnerProvidedPickList;
    }
    
    public void setShowDinnerProvidedPickList(boolean showDinnerProvidedPickList) {
        this.showDinnerProvidedPickList = showDinnerProvidedPickList;
    }
    
    public boolean getShowOvernightCheckBox() {
        return showOvernightCheckBox;
    }
    
    public void setShowOvernightCheckBox(boolean showOvernightCheckBox) {
        this.showOvernightCheckBox = showOvernightCheckBox;
    }
    
    public boolean getShowOvernightAsNightAllowance() {
        return showOvernightAsNightAllowance;
    }
    
    public void setShowOvernightAsNightAllowance(boolean showOvernightAsNightAllowance) {
        this.showOvernightAsNightAllowance = showOvernightAsNightAllowance;
    }
    
    public boolean getShowAboveLimit() {
        return showAboveLimit;
    }
    
    public void setShowAboveLimit(boolean showAboveLimit) {
        this.showAboveLimit = showAboveLimit;
    }
    
    public boolean getShowMealsBaseAmount() {
        return showMealsBaseAmount;
    }
    
    public void setShowMealsBaseAmount(boolean showMealsBaseAmount) {
        this.showMealsBaseAmount = showMealsBaseAmount;
    }
    
    public boolean getShowLodgingTypePickList() {
        return showLodgingTypePickList;
    }
    
    public void setShowLodgingTypePickList(boolean showLodgingTypePickList) {
        this.showLodgingTypePickList = showLodgingTypePickList;
    }
    
    public boolean getShowPercentRuleCheckBox() {
        return showPercentRuleCheckBox;
    }
    
    public void setShowPercentRuleCheckBox(boolean showPercentRuleCheckBox) {
        this.showPercentRuleCheckBox = showPercentRuleCheckBox;
    }
    
    public boolean getShowExtendedTripCheckBox() {
        return showExtendedTripCheckBox;
    }
    
    public void setShowExtendedTripCheckBox(boolean showExtendedTripCheckBox) {
        this.showExtendedTripCheckBox = showExtendedTripCheckBox;
    }
    
    public boolean getShowMunicipalityCheckBox() {
        return showMunicipalityCheckBox;
    }
    
    public void setShowMunicipalityCheckBox(boolean showMunicipalityCheckBox) {
        this.showMunicipalityCheckBox = showMunicipalityCheckBox;
    }
    
    public boolean getShowExcludeCheckBox() {
        return showExcludeCheckBox;
    }
    
    public void setShowExcludeCheckBox(boolean showExcludeCheckBox) {
        this.showExcludeCheckBox = showExcludeCheckBox;
    }
    
    public boolean getShowAllowanceAmount() {
        return showAllowanceAmount;
    }
    
    public void setShowAllowanceAmount(boolean showAllowanceAmount) {
        this.showAllowanceAmount = showAllowanceAmount;
    }
    
    public String getExcludeLabel() {
        return excludeLabel;
    }
    
    public void setExcludeLabel(String excludeLabel) {
        this.excludeLabel = excludeLabel;
    }
    
    public String getLodgingTypeLabel() {
        return lodgingTypeLabel;
    }
    
    public void setLodgingTypeLabel(String lodgingTypeLabel) {
        this.lodgingTypeLabel = lodgingTypeLabel;
    }
    
    public String getApplyPercentRuleLabel() {
        return applyPercentRuleLabel;
    }
    
    public void setApplyPercentRuleLabel(String applyPercentRuleLabel) {
        this.applyPercentRuleLabel = applyPercentRuleLabel;
    }
    
    public String getApplyExtendedTripRuleLabel() {
        return applyExtendedTripRuleLabel;
    }
    
    public void setApplyExtendedTripRuleLabel(String applyExtendedTripRuleLabel) {
        this.applyExtendedTripRuleLabel = applyExtendedTripRuleLabel;
    }
    
    public String getMunicipalAreaLabel() {
        return municipalAreaLabel;
    }
    
    public void setMunicipalAreaLabel(String municipalAreaLabel) {
        this.municipalAreaLabel = municipalAreaLabel;
    }
    
    public String getOvernightLabel() {
        return overnightLabel;
    }
    
    public void setOvernightLabel(String overnightLabel) {
        this.overnightLabel = overnightLabel;
    }
    
    public String getBreakfastProvidedLabel() {
        return breakfastProvidedLabel;
    }
    
    public void setBreakfastProvidedLabel(String breakfastProvidedLabel) {
        this.breakfastProvidedLabel = breakfastProvidedLabel;
    }
    
    public String getLunchProvidedLabel() {
        return lunchProvidedLabel;
    }
    
    public void setLunchProvidedLabel(String lunchProvidedLabel) {
        this.lunchProvidedLabel = lunchProvidedLabel;
    }
    
    public String getDinnerProvidedLabel() {
        return dinnerProvidedLabel;
    }
    
    public void setDinnerProvidedLabel(String dinnerProvidedLabel) {
        this.dinnerProvidedLabel = dinnerProvidedLabel;
    }
}
