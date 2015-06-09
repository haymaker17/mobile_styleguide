package com.concur.mobile.core.expense.travelallowance;

import java.util.Date;

public class FixedAllowanceRow {
    private boolean isFirstDay;
    private boolean isLastDay;
    private boolean isLocked;
    private boolean isReadOnly;
    private String taDayKey;
    private String itinKey;
    private boolean markedExcluded;
    private String fixedRptKey;
    private boolean inUseLock;
    private Date allowanceDate;
    private boolean overnight;
    private boolean applyExtendedTripRule;
    private boolean applyPercentRule;
    private String lodgingType;
    private boolean withinMunicipalArea;
    private Double allowanceAmount;
    private String breakfastProvided;
    private String lunchProvided;
    private String dinnerProvided;
    private Double breakfastTransactionAmount;
    private Double breakfastPostedAmount;
    private String location;
    private Double aboveLimitAmount;
    private Double mealsBaseAmount;
    
    public boolean getIsFirstDay() {
        return isFirstDay;
    }
    
    public void setIsFirstDay(boolean isFirstDay) {
        this.isFirstDay = isFirstDay;
    }
    
    public boolean getIsLastDay() {
        return isLastDay;
    }
    
    public void setIsLastDay(boolean isLastDay) {
        this.isLastDay = isLastDay;
    }
    
    public boolean getIsLocked() {
        return isLocked;
    }
    
    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }
    
    public boolean getIsReadOnly() {
        return isReadOnly;
    }
    
    public void setIsReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }
    
    public String getTaDayKey() {
        return taDayKey;
    }
    
    public void setTaDayKey(String taDayKey) {
        this.taDayKey = taDayKey;
    }
    
    public String getItinKey() {
        return itinKey;
    }
    
    public void setItinKey(String itinKey) {
        this.itinKey = itinKey;
    }
    
    public boolean getMarkedExcluded() {
        return markedExcluded;
    }
    
    public void setMarkedExcluded(boolean markedExcluded) {
        this.markedExcluded = markedExcluded;
    }
    
    public String getFixedRptKey() {
        return fixedRptKey;
    }
    
    public void setFixedRptKey(String fixedRptKey) {
        this.fixedRptKey = fixedRptKey;
    }
    
    public boolean getInUseLock() {
        return inUseLock;
    }
    
    public void setInUseLock(boolean inUseLock) {
        this.inUseLock = inUseLock;
    }
    
    public Date getAllowanceDate() {
        return allowanceDate;
    }
    
    public void setAllowanceDate(Date allowanceDate) {
        this.allowanceDate = allowanceDate;
    }
    
    public boolean getOvernight() {
        return overnight;
    }
    
    public void setOvernight(boolean overnight) {
        this.overnight = overnight;
    }
    
    public boolean getApplyExtendedTripRule() {
        return applyExtendedTripRule;
    }
    
    public void setApplyExtendedTripRule(boolean applyExtendedTripRule) {
        this.applyExtendedTripRule = applyExtendedTripRule;
    }
    
    public boolean getApplyPercentRule() {
        return applyPercentRule;
    }
    
    public void setApplyPercentRule(boolean applyPercentRule) {
        this.applyPercentRule = applyPercentRule;
    }
    
    public String getLodgingType() {
        return lodgingType;
    }
    
    public void setLodgingType(String lodgingType) {
        this.lodgingType = lodgingType;
    }
    
    public boolean getWithinMunicipalArea() {
        return withinMunicipalArea;
    }
    
    public void setWithinMunicipalArea(boolean withinMunicipalArea) {
        this.withinMunicipalArea = withinMunicipalArea;
    }
    
    public Double getAllowanceAmount() {
        return allowanceAmount;
    }
    
    public void setAllowanceAmount(Double allowanceAmount) {
        this.allowanceAmount = allowanceAmount;
    }
    
    public String getBreakfastProvided() {
        return breakfastProvided;
    }
    
    public void setBreakfastProvided(String breakfastProvided) {
        this.breakfastProvided = breakfastProvided;
    }
    
    public String getLunchProvided() {
        return lunchProvided;
    }
    
    public void setLunchProvided(String lunchProvided) {
        this.lunchProvided = lunchProvided;
    }
    
    public String getDinnerProvided() {
        return dinnerProvided;
    }
    
    public void setDinnerProvided(String dinnerProvided) {
        this.dinnerProvided = dinnerProvided;
    }
    
    public Double getBreakfastTransactionAmount() {
        return breakfastTransactionAmount;
    }
    
    public void setBreakfastTransactionAmount(Double breakfastTransactionAmount) {
        this.breakfastTransactionAmount = breakfastTransactionAmount;
    }
    
    public Double getBreakfastPostedAmount() {
        return breakfastPostedAmount;
    }
    
    public void setBreakfastPostedAmount(Double breakfastPostedAmount) {
        this.breakfastPostedAmount = breakfastPostedAmount;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public Double getAboveLimitAmount() {
        return aboveLimitAmount;
    }
    
    public void setAboveLimitAmount(Double aboveLimitAmount) {
        this.aboveLimitAmount = aboveLimitAmount;
    }
    
    public Double getMealsBaseAmount() {
        return mealsBaseAmount;
    }
    
    public void setMealsBaseAmount(Double mealsBaseAmount) {
        this.mealsBaseAmount = mealsBaseAmount;
    }
}
