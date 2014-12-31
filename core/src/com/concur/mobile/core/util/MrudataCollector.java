package com.concur.mobile.core.util;

import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;

/**
 * This class holds old and new data for MRU. It also helps user to know whether we need to update MRU or not.
 * 
 * @author sunill
 * 
 */
public class MrudataCollector {

    protected String oldExpType, newExpType;

    protected String oldCurType, newCurType;

    protected String oldLoc, newLoc;

    protected ExpenseReportEntry expRepEnt;

    protected String expRepEntryKey;

    public MrudataCollector() {
    }

    public MrudataCollector(String expRepEntryKey) {
        this.expRepEntryKey = expRepEntryKey;
    }

    public void clearData() {
        oldCurType = null;
        oldExpType = null;
        newCurType = null;
        newExpType = null;
    }

    public String getOldExpType() {
        return oldExpType;
    }

    public void setOldExpType(String oldExpType) {
        oldExpType = (oldExpType != null) ? oldExpType.trim() : null;
        if (this.oldExpType == null || this.oldExpType.length() == 0) {
            this.oldExpType = oldExpType;
        }
    }

    public String getNewExpType() {
        return newExpType;
    }

    public void setNewExpType(String newExpType) {
        newExpType = (newExpType != null) ? newExpType.trim() : null;
        if (newExpType != null && newExpType.length() > 0) {
            this.newExpType = newExpType;
        } else {
            this.newExpType = null;
        }
    }

    public String getOldCurType() {
        return oldCurType;
    }

    public void setOldCurType(String oldCurType) {
        oldCurType = (oldCurType != null) ? oldCurType.trim() : null;
        if (this.oldCurType == null || this.oldCurType.length() == 0) {
            this.oldCurType = oldCurType;
        }
    }

    public String getNewCurType() {
        return newCurType;
    }

    public void setNewCurType(String newCurType) {
        newCurType = (newCurType != null) ? newCurType.trim() : null;
        if (newCurType != null && newCurType.length() > 0) {
            this.newCurType = newCurType;
        } else {
            this.newCurType = null;
        }
    }

    public String getOldLoc() {
        return oldLoc;
    }

    public void setOldLoc(String oldLoc) {
        oldLoc = (oldLoc != null) ? oldLoc.trim() : null;
        if (this.oldLoc == null || this.oldLoc.length() == 0) {
            this.oldLoc = oldLoc;
        }
    }

    public String getNewLoc() {
        return newLoc;
    }

    public void setNewLoc(String newLoc) {
        newLoc = (newLoc != null) ? newLoc.trim() : null;
        if (newLoc != null && newLoc.length() > 0) {
            this.newLoc = newLoc;
        } else {
            this.newLoc = null;
        }
    }

    public boolean isNewExpType() {
        return compareValues(newExpType, oldExpType);
    }

    public boolean isNewCurType() {
        return compareValues(newCurType, oldCurType);
    }

    public boolean isNewLocation() {
        return compareValues(newLoc, oldLoc);
    }

    private boolean compareValues(String newValue, String oldValue) {
        if ((newValue != null && newValue.length() > 0) && (oldValue != null && oldValue.length() > 0)) {
            // Both value set and one of them is changed.
            return !oldValue.equalsIgnoreCase(newValue);
        } else if ((newValue != null && newValue.length() > 0) && (oldValue == null || oldValue.length() == 0)) {
            // no old value and new value set.
            return true;
        } else if ((oldValue != null && oldValue.length() > 0) && (newValue == null || newValue.length() == 0)) {
            // old value cleared.
            return false;
        } else {
            // never had a value.
            return false;
        }
    }
}
