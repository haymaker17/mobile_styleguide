package com.concur.mobile.core.expense.ta.service;

import java.io.Serializable;


public class FixedAllowanceAmounts implements Serializable {
    private String taDayKey;
    private Double allowanceAmount;
    private Double aboveLimitAmount;
    
    public String getTaDayKey() {
        return taDayKey;
    }
    
    public void setTaDayKey(String taDayKey) {
        this.taDayKey = taDayKey;
    }
    
    public Double getAllowanceAmount() {
        return allowanceAmount;
    }
    
    public void setAllowanceAmount(Double allowanceAmount) {
        this.allowanceAmount = allowanceAmount;
    }
    
    public Double getAboveLimitAmount() {
        return aboveLimitAmount;
    }
    
    public void setAboveLimitAmount(Double aboveLimitAmount) {
        this.aboveLimitAmount = aboveLimitAmount;
    }
}
