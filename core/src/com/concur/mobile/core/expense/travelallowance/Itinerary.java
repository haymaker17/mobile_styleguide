package com.concur.mobile.core.expense.travelallowance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Itinerary {

    String itinKey;
    String name;
    boolean shortDistanceTrip;
    String empKey;
    String tacKey;
    String tacName;
    Date departDate;
    String departLocation;
    Date arrivalDate;
    String arrivalLocation;
    boolean isLocked;
    boolean areAllRowsLocked;
    String rptKey;

    List<ItineraryRow> itineraryRows = new ArrayList<ItineraryRow>();

    public String getRptKey() {
        return rptKey;
    }

    public void setRptKey(String rptKey) {
        this.rptKey = rptKey;
    }

    public String getItinKey() {
        return itinKey;
    }

    public void setItinKey(String itinKey) {
        this.itinKey = itinKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isShortDistanceTrip() {
        return shortDistanceTrip;
    }

    public void setShortDistanceTrip(boolean shortDistanceTrip) {
        this.shortDistanceTrip = shortDistanceTrip;
    }

    public String getEmpKey() {
        return empKey;
    }

    public void setEmpKey(String empKey) {
        this.empKey = empKey;
    }

    public String getTacKey() {
        return tacKey;
    }

    public void setTacKey(String tacKey) {
        this.tacKey = tacKey;
    }

    public String getTacName() {
        return tacName;
    }

    public void setTacName(String tacName) {
        this.tacName = tacName;
    }

    public Date getDepartDateTime() {
        return departDate;
    }

    public void setDepartDateTime(Date departDate) {
        this.departDate = departDate;
    }

    public String getDepartLocation() {
        return departLocation;
    }

    public void setDepartLocation(String departLocation) {
        this.departLocation = departLocation;
    }

    public Date getArrivalDateTime() {
        return arrivalDate;
    }

    public void setArrivalDateTime(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getArrivalLocation() {
        return arrivalLocation;
    }

    public void setArrivalLocation(String arrivalLocation) {
        this.arrivalLocation = arrivalLocation;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean isAreAllRowsLocked() {
        return areAllRowsLocked;
    }

    public void setAreAllRowsLocked(boolean areAllRowsLocked) {
        this.areAllRowsLocked = areAllRowsLocked;
    }

    public List<ItineraryRow> getItineraryRows() {
        return itineraryRows;
    }

    public void setItineraryRows(List<ItineraryRow> itineraryRows) {
        this.itineraryRows = itineraryRows;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Itinerary\n");
        sb.append("\tItinKey=" + itinKey);
        sb.append("\n\tName=" + name);
        sb.append("\n\tShortDistanceTrip=" + shortDistanceTrip);
        sb.append("\n\tEmpKey=" + empKey);
        sb.append("\n\t...");
        sb.append("\n\tRows:");
        for (ItineraryRow row : itineraryRows) {
            sb.append("\n\t\tRow:");
            sb.append("\n\t\t\tIrKey:" + row.irKey);
            sb.append("\n\t\t\tArrivalDateTime:" + row.arrivalDateTime);
        }
        return sb.toString();
    }

}
