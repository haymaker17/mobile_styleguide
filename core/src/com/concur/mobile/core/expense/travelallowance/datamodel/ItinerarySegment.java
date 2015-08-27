package com.concur.mobile.core.expense.travelallowance.datamodel;

//import com.concur.mobile.core.expense.travelallowance.util.DateComparator;
import android.util.Log;

import com.concur.mobile.core.expense.travelallowance.util.DateComparator;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.expense.travelallowance.util.Message;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by D049515 on 23.06.2015.
 */
public class ItinerarySegment implements Serializable, IDatePeriodUTC, Comparable<ItinerarySegment> {

    private static final String CLASS_TAG = ItinerarySegment.class.getSimpleName();
    private static final long serialVersionUID = -5416179865430781088L;

    private String id;
    private ItineraryLocation departureLocation;
    private Date departureDateTime;
    private ItineraryLocation arrivalLocation;
    private Date arrivalDateTime;
    private Date borderCrossDateTime;
    private boolean locked;
    private Message message;

    /**
     * Describes the possible fields in this model. Might be used in {@link Message}s
     */
    public enum Field {
        ID("Id"),
        DEPARTURE_LOCATION("departureLocation"),
        DEPARTURE_DATE_TIME("departureDateTime"),
        ARRIVAL_LOCATION("arrivalLocation"),
        ARRIVAL_DATE_TIME("arrivalDateTime"),
        BORDER_CROSS_DATE_TIME("borderCrossDateTime"),
        LOCKED("locked");

        private String name;

        Field(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ItineraryLocation getDepartureLocation() {
        return departureLocation;
    }

    public void setDepartureLocation(ItineraryLocation departureLocation) {
        this.departureLocation = departureLocation;
    }

    public Date getDepartureDateTime() {
        return departureDateTime;
    }

    public void setDepartureDateTime(Date departureDateTime) {
        this.departureDateTime = departureDateTime;
    }

    public ItineraryLocation getArrivalLocation() {
        return arrivalLocation;
    }

    public void setArrivalLocation(ItineraryLocation arrivalLocation) {
        this.arrivalLocation = arrivalLocation;
    }

    public Date getArrivalDateTime() {
        return arrivalDateTime;
    }

    public void setArrivalDateTime(Date arrivalDateTime) {
        this.arrivalDateTime = arrivalDateTime;
    }

    public Date getBorderCrossDateTime() {
        return borderCrossDateTime;
    }

    public void setBorderCrossDateTime(Date borderCrossDateTime) {
        this.borderCrossDateTime = borderCrossDateTime;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * Setter method
     * @param message The message object to be set
     */
    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * Getter method
     * @return The message object
     */
    public Message getMessage() {
        if (message != null) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "getMessage", message.toString()));
        }
        return this.message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getStartDateUTC() {
        return this.departureDateTime;
//        if (departureDateTime == null) {
//            return null;
//        }
//        long departureTimeZoneOffset = 0;
//        if (departureLocation != null && departureLocation.getTimeZoneOffset() != null) {
//            departureTimeZoneOffset = departureLocation.getTimeZoneOffset();
//        }
//        Date dateUTC = new Date();
//        dateUTC.setTime(departureDateTime.getTime() - DateUtils.convertMinutesToMilliseconds(departureTimeZoneOffset));
//        return dateUTC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getEndDateUTC() {
        return this.arrivalDateTime;
//        if (arrivalDateTime == null) {
//            return null;
//        }
//        long arrivalTimeZoneOffset = 0;
//        if (arrivalLocation != null && arrivalLocation.getTimeZoneOffset() != null) {
//            arrivalTimeZoneOffset = arrivalLocation.getTimeZoneOffset();
//        }
//        Date dateUTC = new Date();
//        dateUTC.setTime(arrivalDateTime.getTime() - DateUtils.convertMinutesToMilliseconds(arrivalTimeZoneOffset));
//        return dateUTC;
    }

    @Override
    public int compareTo(ItinerarySegment o) {

        if (o == null){
            return -1;
        }
        //First handle the cases tha at least one Departure date is null
        if (this.getDepartureDateTime() == null) {
            if (o.getDepartureDateTime() == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (o.getDepartureDateTime() == null) {
            return 1;
        }

        DateComparator dateComparator = new DateComparator();
        return dateComparator.compare(this.getStartDateUTC(), o.getStartDateUTC());

        /*
        long diff = this.getStartDateUTC().getTime() -  o.getStartDateUTC().getTime();
        if (diff < 0){
            return -1;
        }else if (diff == 0) {
            return 0;
        } else {
            return 1;
        }

*/

       //return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItinerarySegment that = (ItinerarySegment) o;

        if (locked != that.locked) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (departureLocation != null ? !departureLocation.equals(that.departureLocation) : that.departureLocation != null)
            return false;
        if (departureDateTime != null ? !departureDateTime.equals(that.departureDateTime) : that.departureDateTime != null)
            return false;
        if (arrivalLocation != null ? !arrivalLocation.equals(that.arrivalLocation) : that.arrivalLocation != null)
            return false;
        if (arrivalDateTime != null ? !arrivalDateTime.equals(that.arrivalDateTime) : that.arrivalDateTime != null)
            return false;
        if (borderCrossDateTime != null ? !borderCrossDateTime.equals(that.borderCrossDateTime) : that.borderCrossDateTime != null)
            return false;
        return true;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (departureLocation != null ? departureLocation.hashCode() : 0);
        result = 31 * result + (departureDateTime != null ? departureDateTime.hashCode() : 0);
        result = 31 * result + (arrivalLocation != null ? arrivalLocation.hashCode() : 0);
        result = 31 * result + (arrivalDateTime != null ? arrivalDateTime.hashCode() : 0);
        result = 31 * result + (borderCrossDateTime != null ? borderCrossDateTime.hashCode() : 0);
        result = 31 * result + (locked ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ItinerarySegment{" +
                "arrivalDateTime=" + arrivalDateTime +
                ", departureDateTime=" + departureDateTime +
                ", id='" + id + '\'' +
                '}';
    }
}