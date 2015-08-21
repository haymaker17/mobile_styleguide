package com.concur.mobile.core.expense.travelallowance.ui.model;

import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by D049515 on 26.06.2015.
 */
public class CompactItinerarySegment implements Serializable {


    private static final long serialVersionUID = 4838630230258758378L;


    private ItineraryLocation location;


    private Date arrivalDateTime;
    private Date departureDateTime;
    private Date borderCrossingDateTime;

    private boolean isSegmentOpen;
    private boolean displayBorderCrossing;


    public ItineraryLocation getLocation() {
        return location;
    }

    public void setLocation(ItineraryLocation location) {
        this.location = location;
    }

    public Date getArrivalDateTime() {
        return arrivalDateTime;
    }

    public void setArrivalDateTime(Date arrivalDateTime) {
        this.arrivalDateTime = arrivalDateTime;
    }

    public Date getDepartureDateTime() {
        return departureDateTime;
    }

    public void setDepartureDateTime(Date departureDateTime) {
        this.departureDateTime = departureDateTime;
    }

    public Date getBorderCrossingDateTime() {
        return borderCrossingDateTime;
    }

    public void setBorderCrossingDateTime(Date borderCrossingDateTime) {
        this.borderCrossingDateTime = borderCrossingDateTime;
    }

    public boolean isSegmentOpen() {
        return isSegmentOpen;
    }

    public void setIsSegmentOpen(boolean isSegmentOpen) {
        this.isSegmentOpen = isSegmentOpen;
    }

    public boolean isDisplayBorderCrossing() {
        return displayBorderCrossing;
    }

    public void setDisplayBorderCrossing(boolean displayBorderCrossing) {
        this.displayBorderCrossing = displayBorderCrossing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompactItinerarySegment that = (CompactItinerarySegment) o;

        if (isSegmentOpen != that.isSegmentOpen) return false;
        if (displayBorderCrossing != that.displayBorderCrossing) return false;
        if (location != null ? !location.equals(that.location) : that.location != null)
            return false;
        if (arrivalDateTime != null ? !arrivalDateTime.equals(that.arrivalDateTime) : that.arrivalDateTime != null)
            return false;
        if (departureDateTime != null ? !departureDateTime.equals(that.departureDateTime) : that.departureDateTime != null)
            return false;
        return !(borderCrossingDateTime != null ? !borderCrossingDateTime.equals(that.borderCrossingDateTime) : that.borderCrossingDateTime != null);

    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (arrivalDateTime != null ? arrivalDateTime.hashCode() : 0);
        result = 31 * result + (departureDateTime != null ? departureDateTime.hashCode() : 0);
        result = 31 * result + (borderCrossingDateTime != null ? borderCrossingDateTime.hashCode() : 0);
        result = 31 * result + (isSegmentOpen ? 1 : 0);
        result = 31 * result + (displayBorderCrossing ? 1 : 0);
        return result;
    }
}
