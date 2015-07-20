package com.concur.mobile.core.expense.travelallowance.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by D049515 on 23.06.2015.
 */
public class Itinerary implements Serializable{

    private static final long serialVersionUID = -3254062517635418907L;


    private String itineraryID;
    private String name;
    private String expenseReportID;
    private List<ItinerarySegment> segmentList;



    public String getItineraryID() {
        return itineraryID;
    }

    public void setItineraryID(String itineraryID) {
        this.itineraryID = itineraryID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpenseReportID() {
        return expenseReportID;
    }

    public void setExpenseReportID(String expenseReportID) {
        this.expenseReportID = expenseReportID;
    }

    public List<ItinerarySegment> getSegmentList() {
        if (segmentList == null) {
            segmentList = new ArrayList<ItinerarySegment>();
        }
        return segmentList;
    }

    public void setSegmentList(List<ItinerarySegment> segmentList) {
        this.segmentList = segmentList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Itinerary itinerary = (Itinerary) o;

        if (itineraryID != null ? !itineraryID.equals(itinerary.itineraryID) : itinerary.itineraryID != null)
            return false;
        if (name != null ? !name.equals(itinerary.name) : itinerary.name != null) return false;
        if (expenseReportID != null ? !expenseReportID.equals(itinerary.expenseReportID) : itinerary.expenseReportID != null)
            return false;

        //Compare segment lists of both objects
        if ( (segmentList == null && itinerary.segmentList != null )||
             (segmentList != null && itinerary.segmentList == null )){
            return false;
        }
        if (segmentList != null && itinerary.segmentList != null) {
            if (segmentList.size() != itinerary.getSegmentList().size()) {
                return false;
            }
            if (!segmentList.containsAll(itinerary.getSegmentList())) {
                return false;
            }
        }

    return true;

    }

    @Override
    public int hashCode() {
        int result = itineraryID != null ? itineraryID.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (expenseReportID != null ? expenseReportID.hashCode() : 0);
        result = 31 * result + (segmentList != null ? segmentList.hashCode() : 0);
        return result;
    }
}
