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
}
