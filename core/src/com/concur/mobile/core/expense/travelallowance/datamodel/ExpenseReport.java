package com.concur.mobile.core.expense.travelallowance.datamodel;

import java.io.Serializable;
import java.util.List;

/**
 * Representation of an Expense Report
 *
 * @author Michael Becherer
 */
public class ExpenseReport implements Serializable, Comparable<ExpenseReport>{

    private static final long serialVersionUID = 1L;

    /**
     * The identifier of this expense report
     */
    private String id;

    /**
     * The travel allowances (rows) associated with this expense report
     */
    private List<FixedTravelAllowance> allowances;

    /**
     * Creates an instance of an ExpenseReport
     * @param id The identifier of this expense report
     */
    public ExpenseReport(String id) {
        this.id = id;
    }

    /**
     * Getter method
     * @return The identifier of this expense report
     */
    public String getId() {
        return id;
    }

    /**
     * Setter method
     * @param id The identifier of this expense report
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter method
     * @return The travel allowances (rows) associated with this expense report
     */
    public List<FixedTravelAllowance> getAllowances() {
        return allowances;
    }

    /**
     * Setter method
     * @param allowances The travel allowances (rows) associated with this expense report
     */
    public void setAllowances(List<FixedTravelAllowance> allowances) {
        this.allowances = allowances;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseReport)) return false;

        ExpenseReport that = (ExpenseReport) o;

        if (allowances != null ? !allowances.equals(that.allowances) : that.allowances != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (allowances != null ? allowances.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(ExpenseReport another) {
        return 0;
    }
}
