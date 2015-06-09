package com.concur.mobile.core.expense.travelallowance;

import java.util.HashMap;

import android.util.Log;

import com.concur.mobile.core.util.Const;

public class GetTAItinerariesResultParser extends ReflectionActionResponseParser {

    private Itinerary itinerary;
    private ItineraryRow currentRow;

    private static HashMap<String, PropType> itineraryProps;
    private static HashMap<String, PropType> itineraryRowProps;

    static {
        itineraryProps = new HashMap<String, GetTAItinerariesResultParser.PropType>();
        itineraryProps.put("ItinKey", PropType.STRING);
        itineraryProps.put("Name", PropType.STRING);
        itineraryProps.put("EmpKey", PropType.STRING);
        itineraryProps.put("TacKey", PropType.STRING);
        itineraryProps.put("TacName", PropType.STRING);
        itineraryProps.put("DepartLocation", PropType.STRING);
        itineraryProps.put("ArrivalLocation", PropType.STRING);

        itineraryProps.put("ShortDistanceTrip", PropType.BOOLEAN);
        itineraryProps.put("IsLocked", PropType.BOOLEAN);
        itineraryProps.put("AreAllRowsLocked", PropType.BOOLEAN);

        itineraryProps.put("DepartDateTime", PropType.DATE);
        itineraryProps.put("ArrivalDateTime", PropType.DATE);

        itineraryRowProps = new HashMap<String, GetTAItinerariesResultParser.PropType>();
        itineraryRowProps.put("IrKey", PropType.STRING);
        itineraryRowProps.put("ArrivalLocation", PropType.STRING);
        itineraryRowProps.put("ArrivalLnKey", PropType.STRING);
        itineraryRowProps.put("DepartLocation", PropType.STRING);
        itineraryRowProps.put("DepartLnKey", PropType.STRING);
        itineraryRowProps.put("ArrivalRlKey", PropType.STRING);
        itineraryRowProps.put("ArrivalRateLocation", PropType.STRING);
        itineraryRowProps.put("Status", PropType.STRING);
        itineraryRowProps.put("StatusText", PropType.STRING);

        itineraryRowProps.put("IsRowLocked", PropType.BOOLEAN);
        itineraryRowProps.put("IsArrivalRateLocationEditable", PropType.BOOLEAN);

        itineraryRowProps.put("ArrivalDateTime", PropType.DATE);
        itineraryRowProps.put("DepartDateTime", PropType.DATE);
        itineraryRowProps.put("BorderCrossDateTime", PropType.DATE);
    }

    public GetTAItinerariesResultParser() {
        itinerary = new Itinerary();
    }

    public Itinerary getItinerary() {
        return itinerary;
    }

    @Override
    public void startTag(String tag) {
        super.startTag(tag);
        if ("ItineraryRow".equals(tag)) {
            currentRow = new ItineraryRow();
        }
    }

    @Override
    public void endTag(String tag) {
        super.endTag(tag);
        if ("ItineraryRow".equals(tag)) {
            itinerary.getItineraryRows().add(currentRow);
        }
    }

    @Override
    public void handleText(String tag, String text) {
        try {
            if (currentRow == null) {
                PropType propType = itineraryProps.get(tag);
                if (propType != null) {
                    switch (propType) {
                    case STRING:
                        setStringProperty(itinerary, tag, text);
                        break;
                    case DATE:
                        setDateProperty(itinerary, tag, text);
                        break;
                    case BOOLEAN:
                        setBooleanProperty(itinerary, tag, text);
                        break;
                    default:
                        super.handleText(tag, text);
                    }
                }
            } else {
                PropType propType = itineraryRowProps.get(tag);
                if (propType != null) {
                    switch (propType) {
                    case STRING:
                        setStringProperty(currentRow, tag, text);
                        break;
                    case DATE:
                        setDateProperty(currentRow, tag, text);
                        break;
                    case BOOLEAN:
                        setBooleanProperty(currentRow, tag, text);
                        break;
                    default:
                        super.handleText(tag, text);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(Const.LOG_TAG, "exception parsing response", e);
        }
    }
}
