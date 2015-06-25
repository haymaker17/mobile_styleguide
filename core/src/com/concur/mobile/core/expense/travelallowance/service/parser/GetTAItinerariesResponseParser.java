package com.concur.mobile.core.expense.travelallowance.service.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;

public class GetTAItinerariesResponseParser extends BaseParser {

    private static final String ITINERARY_TAG = "Itinerary";

    private static final String ITINERARY_SEGMENT_TAG = "ItineraryRow";

    private List<Itinerary> itineraries;

    private Itinerary itinerary;

    private List<ItinerarySegment> segments;
    private ItinerarySegment itinSegment;
    private ItineraryLocation departureLocation;
    private ItineraryLocation arrivalLocation;




    public GetTAItinerariesResponseParser() {
        this.itineraries = new ArrayList<Itinerary>();
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
            itinerary = new Itinerary();
        }
        if (ITINERARY_SEGMENT_TAG.equals(tag)) {
            itinSegment = new ItinerarySegment();
            departureLocation = new ItineraryLocation();
            arrivalLocation = new ItineraryLocation();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endTag(String tag) {
        super.endTag(tag);
        if (ITINERARY_TAG.equals(tag)) {
           itinerary.setSegmentList(segments);
           segments = null;
            itinSegment = null;
           itineraries.add(itinerary);
        }
        if (ITINERARY_SEGMENT_TAG.equals(tag)) {
            if (segments == null) {
                segments = new ArrayList<ItinerarySegment>();
            }
            itinSegment.setArrivalLocation(arrivalLocation);
            itinSegment.setDepartureLocation(departureLocation);
            segments.add(itinSegment);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleText(String tag, String text) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            if (itinSegment == null) {
                if ("ItinKey".equals(tag)) {
                    itinerary.setItineraryID(text);
                }
                if ("Name".equals(tag)) {
                    itinerary.setName(text);
                }

            } else {
                if ("IrKey".equals(tag)) {
                    itinSegment.setId(text);
                }
                if ("DepartDateTime".equals(tag)) {
                    itinSegment.setDepartureDateTime(format.parse(text));
                }
                if ("ArrivalDateTime".equals(tag)) {
                    itinSegment.setArrivalDateTime(format.parse(text));
                }
                if("BorderCrossDateTime".equals(tag)) {
                    itinSegment.setBorderCrossDateTime(format.parse(text));
                }

                if ("ArrivalLocation".equals(tag)) {
                    arrivalLocation.setName(text);
                }
                if ("DepartLocation".equals(tag)) {
                    departureLocation.setName(text);
                }

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
