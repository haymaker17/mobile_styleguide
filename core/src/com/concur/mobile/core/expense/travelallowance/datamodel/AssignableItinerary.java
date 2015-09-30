package com.concur.mobile.core.expense.travelallowance.datamodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Patricius Komarnicki on 07.09.2015.
 */
public class AssignableItinerary {

    private String itineraryID;
    private String name;
    private Date startDateTime;
    private Date endDateTime;
    private List<String> arrivalLocations;

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

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    public List<String> getArrivalLocations() {
        if (arrivalLocations == null) {
            arrivalLocations = new ArrayList<String>();
        }
        return arrivalLocations;
    }

    public void addArrivalLocation(String arrivalLocation) {
        if (arrivalLocations == null) {
            arrivalLocations = new ArrayList<String>();
        }
        this.arrivalLocations.add(arrivalLocation);
    }
}
