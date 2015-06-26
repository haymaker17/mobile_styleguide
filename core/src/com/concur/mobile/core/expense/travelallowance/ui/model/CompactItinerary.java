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


}
