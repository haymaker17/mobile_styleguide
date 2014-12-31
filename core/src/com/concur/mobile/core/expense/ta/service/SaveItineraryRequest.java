package com.concur.mobile.core.expense.ta.service;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.util.FormatUtil;

public class SaveItineraryRequest extends CoreAsyncRequestTask {

    public static final String LOG_TAG = GetItinerariesRequest.class.getSimpleName();

    private GetTAItinerariesResultParser itinParser;
    private Itinerary itinerary;
    private ItineraryRow itineraryRow;

    public SaveItineraryRequest(Context context, int id, BaseAsyncResultReceiver receiver, Itinerary itinerary,
            ItineraryRow itineraryRow) {
        super(context, id, receiver);
        this.itinerary = itinerary;
        this.itineraryRow = itineraryRow;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/ValidateAndSaveItinerary";
    }

    @Override
    protected String getPostBody() {
        StringBuilder sb = new StringBuilder();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        sb.append("<Itinerary>");
        FormatUtil.addXMLElementEscaped(sb, "ItinKey", itinerary.getItinKey());
        FormatUtil.addXMLElementEscaped(sb, "Name", itinerary.getName());
        FormatUtil.addXMLElementEscaped(sb, "TacKey", itinerary.getTacKey());
        FormatUtil.addXMLElementEscaped(sb, "TacName", itinerary.getTacName());
        FormatUtil.addXMLElementEscaped(sb, "ShortDistanceTrip", itinerary.isShortDistanceTrip() ? "Y" : "N");
        FormatUtil.addXMLElementEscaped(sb, "RptKey", itinerary.getRptKey());
        if (itineraryRow == null) {
            sb.append("<ItineraryRows/>");
        } else {
            sb.append("<ItineraryRows>");
            sb.append("<ItineraryRow>");

            FormatUtil.addXMLElementEscaped(sb, "IrKey", itineraryRow.getIrKey());
            FormatUtil.addXMLElementEscaped(sb, "DepartLnKey", itineraryRow.getDepartLnKey());
            FormatUtil.addXMLElementEscaped(sb, "DepartDateTime",
                    dateTimeFormat.format(itineraryRow.getDepartDateTime()));

            FormatUtil.addXMLElementEscaped(sb, "ArrivalLnKey", itineraryRow.getArrivalLnKey());
            FormatUtil.addXMLElementEscaped(sb, "ArrivalRlKey", itineraryRow.getArrivalRlKey());
            FormatUtil.addXMLElementEscaped(sb, "ArrivalDateTime",
                    dateTimeFormat.format(itineraryRow.getArrivalDateTime()));

            sb.append("</ItineraryRow>");
            sb.append("</ItineraryRows>");
        }
        sb.append("</Itinerary>");
        return sb.toString();
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        // register the parser of interest
        itinParser = new GetTAItinerariesResultParser();
        parser.registerParser(itinParser, "Itinerary");

        try {
            parser.parse();
        } catch (XmlPullParserException e) {
            result = RESULT_ERROR;
            e.printStackTrace();
        } catch (IOException e) {
            result = RESULT_ERROR;
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected int onPostParse() {
        resultData.putBoolean(IS_SUCCESS, true);
        ConcurCore core = (ConcurCore) ConcurCore.getContext();
        core.setTAItinerary(itinParser.getItinerary());

        return RESULT_OK;
    }
}
