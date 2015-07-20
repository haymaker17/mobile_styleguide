package com.concur.mobile.core.expense.travelallowance.util;

import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;


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
}
