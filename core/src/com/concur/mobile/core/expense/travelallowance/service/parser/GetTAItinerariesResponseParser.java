package com.concur.mobile.core.expense.travelallowance.service.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.datamodel.SynchronizationStatusEnum;

public class GetTAItinerariesResponseParser extends BaseParser {

    private static final String CLASS_TAG = GetTAItinerariesResponseParser.class
        .getSimpleName();

    private static final String ITINERARY_TAG = "Itinerary";

    private static final String ITINERARY_SEGMENT_TAG = "ItineraryRow";

    private static final String SUCCESS = "SUCCESS";

    private List<Itinerary> itineraries;

    private Itinerary currentItinerary;

    private Map<String, String> currentItineraryRow;

    private String currentStartTag;

    SimpleDateFormat dateFormat;


    public GetTAItinerariesResponseParser() {
        this.itineraries = new ArrayList<Itinerary>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    /**
     * @return Returns the parsed {@link Itinerary} list.
     */
    public List<Itinerary> getItineraryList() {
        return itineraries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startTag(String tag) {
        super.startTag(tag);
        if (ITINERARY_TAG.equals(tag)) {
            currentStartTag = ITINERARY_TAG;
            currentItinerary = new Itinerary();
        }
        if (ITINERARY_SEGMENT_TAG.equals(tag)) {
            currentStartTag = ITINERARY_SEGMENT_TAG;
            currentItineraryRow = new HashMap<String, String>();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endTag(String tag) {
        super.endTag(tag);
        if (ITINERARY_TAG.equals(tag)) {
           itineraries.add(currentItinerary);
        }
        if (ITINERARY_SEGMENT_TAG.equals(tag)) {
            currentItinerary.getSegmentList().add(createItinerarySegment());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public void handleText(String tag, String text) {

        // Handle Itinerary
        if (ITINERARY_TAG.equals(currentStartTag)) {
			if ("ItinKey".equals(tag)) {
				currentItinerary.setItineraryID(text);
			}
			if ("Name".equals(tag)) {
				currentItinerary.setName(text);
			}
		}

        // Handle Itinerary Segment
		if (ITINERARY_SEGMENT_TAG.equals(currentStartTag)) {
			currentItineraryRow.put(tag, text);
		}
	}

	private ItinerarySegment createItinerarySegment() {

		ItinerarySegment seg = new ItinerarySegment();

		try {
            // Departure
			seg.setId(currentItineraryRow.get("IrKey"));
			seg.setDepartureDateTime(dateFormat.parse(currentItineraryRow
                    .get("DepartDateTime")));
            ItineraryLocation depLoc = new ItineraryLocation();
            depLoc.setName(currentItineraryRow.get("DepartLocation"));
            depLoc.setCode(currentItineraryRow.get("DepartLnKey"));
            String departureOffset = null;
            try {
                departureOffset = currentItineraryRow.get("DepartureTimeZoneOffset");
                if (departureOffset != null) {
                    long departureOffsetMinutes = Long.parseLong(departureOffset, 10);
                    depLoc.setTimeZoneOffset(departureOffsetMinutes);
                }
            } catch (NumberFormatException e) {
                Log.e(CLASS_TAG, "Not a Number: " + departureOffset);
            }
            seg.setDepartureLocation(depLoc);

            // Arrival
            seg.setArrivalDateTime(dateFormat.parse(currentItineraryRow
                    .get("ArrivalDateTime")));
            ItineraryLocation arrLoc = new ItineraryLocation();
            arrLoc.setName(currentItineraryRow.get("ArrivalLocation"));
            arrLoc.setCode(currentItineraryRow.get("ArrivalLnKey"));
            arrLoc.setRateLocationKey(currentItineraryRow.get("ArrivalRlKey"));
            String arrivalOffset = null;
            try {
                arrivalOffset = currentItineraryRow.get("ArrivalTimeZoneOffset");
                if (arrivalOffset != null) {
                    long arrivalOffsetMinutes = Long.parseLong(arrivalOffset, 10);
                    arrLoc.setTimeZoneOffset(arrivalOffsetMinutes);
                }
            } catch (NumberFormatException e) {
                Log.e(CLASS_TAG, "Not a Number: " + arrivalOffset);
            }
            seg.setArrivalLocation(arrLoc);

            // Border Crossing
            String borderCrossDateTime = currentItineraryRow.get("BorderCrossDateTime");
            if (borderCrossDateTime != null) {
                seg.setBorderCrossDateTime(dateFormat.parse(borderCrossDateTime));
            }

            // Status
            String status = currentItineraryRow.get("Status");
            if (status != null) {
                seg.setSyncStatus(SynchronizationStatusEnum.fromCode(status));
            }

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return seg;
	}

}
