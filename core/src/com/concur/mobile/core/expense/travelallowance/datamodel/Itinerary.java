package com.concur.mobile.core.expense.travelallowance.datamodel;

import com.concur.mobile.core.expense.travelallowance.util.Message;

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
    private SynchronizationStatus syncStatus;
    private boolean locked;
    private List<ItinerarySegment> segmentList;
    private Message message;

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

    public SynchronizationStatus getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(SynchronizationStatus syncStatus) {
        this.syncStatus = syncStatus;
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
        return this.message;
    }

    public ItinerarySegment getSegment(String segmentId) {
        ItinerarySegment resultSegment = null;
        for (ItinerarySegment segment : getSegmentList()) {
            if (segment.getId() != null && segment.getId().equals(segmentId)) {
                resultSegment = segment;
            }
        }
        return resultSegment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Itinerary)) return false;

        Itinerary itinerary = (Itinerary) o;

        if (locked != itinerary.locked) return false;
        if (itineraryID != null ? !itineraryID.equals(itinerary.itineraryID) : itinerary.itineraryID != null)
            return false;
        if (name != null ? !name.equals(itinerary.name) : itinerary.name != null) return false;
        if (expenseReportID != null ? !expenseReportID.equals(itinerary.expenseReportID) : itinerary.expenseReportID != null)
            return false;
        if (syncStatus != itinerary.syncStatus) return false;
        return equalsSegmentList(itinerary);
    }

    @Override
    public int hashCode() {
        int result = itineraryID != null ? itineraryID.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (expenseReportID != null ? expenseReportID.hashCode() : 0);
        result = 31 * result + (syncStatus != null ? syncStatus.hashCode() : 0);
        result = 31 * result + (locked ? 1 : 0);
        result = 31 * result + (segmentList != null ? segmentList.hashCode() : 0);
        return result;
    }

    /**
     * Compares the itinerary lists.
     * @param itinerary The itinerary to be compared with this
     * @return true, if the segments lists are equal
     */
    private boolean equalsSegmentList(Itinerary itinerary) {
        //Compare segment lists of both objects
        if ( (segmentList == null && itinerary.segmentList != null )||
             (segmentList != null && itinerary.segmentList == null )){
            return true;
        }
        if (segmentList != null && itinerary.segmentList != null) {
            if (segmentList.size() != itinerary.getSegmentList().size()) {
                return true;
            }
            if (!segmentList.containsAll(itinerary.getSegmentList())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Itinerary{" +
                "itineraryID='" + itineraryID + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
