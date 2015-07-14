package com.concur.mobile.core.expense.travelallowance.ui.model;

import com.concur.mobile.core.expense.travelallowance.datamodel.IDatePeriod;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by D049515 on 26.06.2015.
 */
public class CompactItinerarySegment implements Serializable, IDatePeriod {


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

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getStartDate() {
        return departureDateTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getEndDate() {
        return arrivalDateTime;
    }
}
