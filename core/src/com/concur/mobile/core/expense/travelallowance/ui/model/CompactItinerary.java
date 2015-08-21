package com.concur.mobile.core.expense.travelallowance.ui.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by D049515 on 26.06.2015.
 */
public class CompactItinerary implements Serializable {


    private static final long serialVersionUID = 4727952120738953352L;

    private String name;
    private List<CompactItinerarySegment> segmentList;
    private String itineraryID;
    private String expenseReportID;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CompactItinerarySegment> getSegmentList() {
        if (segmentList == null) {
            segmentList = new ArrayList<CompactItinerarySegment>();
        }
        return segmentList;
    }

    public void setSegmentList(List<CompactItinerarySegment> segmentList) {
        this.segmentList = segmentList;
    }

    public String getItineraryID() {
        return itineraryID;
    }

    public void setItineraryID(String itineraryID) {
        this.itineraryID = itineraryID;
    }

    public String getExpenseReportID() {
        return expenseReportID;
    }

    public void setExpenseReportID(String expenseReportID) {
        this.expenseReportID = expenseReportID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompactItinerary that = (CompactItinerary) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (segmentList != null ? !segmentList.equals(that.segmentList) : that.segmentList != null)
            return false;
        if (itineraryID != null ? !itineraryID.equals(that.itineraryID) : that.itineraryID != null)
            return false;
        return !(expenseReportID != null ? !expenseReportID.equals(that.expenseReportID) : that.expenseReportID != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (segmentList != null ? segmentList.hashCode() : 0);
        result = 31 * result + (itineraryID != null ? itineraryID.hashCode() : 0);
        result = 31 * result + (expenseReportID != null ? expenseReportID.hashCode() : 0);
        return result;
    }
}
