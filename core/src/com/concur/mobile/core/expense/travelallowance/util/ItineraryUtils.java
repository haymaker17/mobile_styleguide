package com.concur.mobile.core.expense.travelallowance.util;

import com.concur.mobile.core.expense.travelallowance.datamodel.AssignableItinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;

import java.util.Date;
import java.util.List;


/**
 * Created by D023077 on 17.07.2015.
 */
public class ItineraryUtils {
    public Itinerary getItinerary(CompactItinerary compactItinerary) {
        if (compactItinerary == null) {
            return null;
        }

        Itinerary itinerary = new Itinerary();

        itinerary.setName(compactItinerary.getName());
        itinerary.setExpenseReportID(compactItinerary.getExpenseReportID());
        itinerary.setItineraryID(compactItinerary.getItineraryID());

        ItinerarySegment currentItinSegement = null;

        CompactItinerarySegment nextCompactSegment = null;
        int i = 0;
        boolean nextSegmentFinished = false;
        for (CompactItinerarySegment segment : compactItinerary.getSegmentList()) {
            if (i < compactItinerary.getSegmentList().size() - 1) {
                nextCompactSegment = compactItinerary.getSegmentList().get(i+1);

//                //Check consistency of compactItinerarySegments
//                if ( (segment.getDepartureDateTime() == null && nextCompactSegment.getArrivalDateTime() != null) ||     //Arrival w/o Departure
//                        (segment.getDepartureDateTime() != null && nextCompactSegment.getArrivalDateTime() == null)   ){   //Departure w/o Arrival
//                    Toast.makeText(context, "@Inconsistent Segment Sequence@", Toast.LENGTH_SHORT).show();
//                    return null;
//                }
                if (currentItinSegement == null) {
                    currentItinSegement = new ItinerarySegment();
                }

                if (!nextSegmentFinished) {
                    currentItinSegement.setDepartureLocation(segment.getLocation());
                    currentItinSegement.setDepartureDateTime(segment.getDepartureDateTime());
                    currentItinSegement.setBorderCrossDateTime(segment.getBorderCrossingDateTime());
                    if (nextCompactSegment.isSegmentOpen()) {
                        currentItinSegement.setArrivalLocation(nextCompactSegment.getLocation());
                        currentItinSegement.setArrivalDateTime(nextCompactSegment.getArrivalDateTime());
                    } else {
                        currentItinSegement.setArrivalLocation(nextCompactSegment.getLocation());
                        currentItinSegement.setArrivalDateTime(nextCompactSegment.getArrivalDateTime());
                    }

                    itinerary.getSegmentList().add(currentItinSegement);
                    currentItinSegement = null;

                    if (nextCompactSegment.isSegmentOpen()) {
                        nextSegmentFinished = true;
                    } else {
                        nextSegmentFinished = false;
                    }
                } else {
                    nextSegmentFinished = false;
                }
            }

            i++;
        }

        return itinerary;
    }

    public static Message findMessage(List<Message> messageList, ItinerarySegment segment) {
        if (messageList == null || segment == null) {
            return null;
        }

        Date departure = segment.getDepartureDateTime();
        Date arrival = segment.getArrivalDateTime();

        for (Message msg : messageList) {
            if (msg.getSourceObject() != null && msg.getSourceObject() instanceof ItinerarySegment) {
                ItinerarySegment msgSourceObject = (ItinerarySegment) msg.getSourceObject();
                int departureCompare = DateUtils.getDateComparator(false).compare(departure, msgSourceObject.getDepartureDateTime());
                int arrivalCompare = DateUtils.getDateComparator(false).compare(arrival, msgSourceObject.getArrivalDateTime());
                if (departureCompare == 0 && arrivalCompare == 0) {
                    return msg;
                }
            }
        }

        return null;
    }

    public static String createLocationString(AssignableItinerary itin) {
        StringBuffer sb = new StringBuffer();
        boolean firstRun = true;
        for (String s: itin.getArrivalLocations()) {
            if (!firstRun) {
                sb.append(", ");
            }
            int posCountrySep = s.indexOf(",");
            if (posCountrySep > 0) {
                sb.append(s.substring(0, posCountrySep));
            } else {
                sb.append(s);
            }
            firstRun = false;
        }
        return sb.toString();
    }

    public static String createLocationString(Itinerary itin) {
        StringBuffer sb = new StringBuffer();
        boolean firstRun = true;

        for (ItinerarySegment segment : itin.getSegmentList()) {
            if (segment.getArrivalLocation() != null
                    && !StringUtilities.isNullOrEmpty(segment.getArrivalLocation().getName())) {
                String s = segment.getArrivalLocation().getName();
                if (!firstRun) {
                    sb.append(", ");
                }
                int posCountrySep = s.indexOf(",");
                if (posCountrySep > 0) {
                    sb.append(s.substring(0, posCountrySep));
                } else {
                    sb.append(s);
                }

                firstRun = false;
            }
        }
        return sb.toString();
    }
}
