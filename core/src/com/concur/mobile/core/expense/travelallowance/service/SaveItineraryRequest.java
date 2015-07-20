package com.concur.mobile.core.expense.travelallowance.service;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceConfigurationController;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.service.parser.GetTAItinerariesResponseParser;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;

public class SaveItineraryRequest extends CoreAsyncRequestTask {

    public static final String LOG_TAG = GetTAItinerariesRequest.class.getSimpleName();

    private GetTAItinerariesResponseParser itinParser;
    private Itinerary itinerary;
    private ItinerarySegment itinerarySegment;
    private Context context;

    public SaveItineraryRequest(Context context, BaseAsyncResultReceiver receiver, Itinerary itinerary) {
        super(context, 0, receiver);
        this.context = context;
        this.itinerary = itinerary;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/ValidateAndSaveItinerary";
    }

    @Override
    protected String getPostBody() {
        TravelAllowanceConfigurationController taConfig = ((ConcurCore) context.getApplicationContext())
                .getTAConfigController();
        StringBuilder sb = new StringBuilder();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        sb.append("<Itinerary>");
        //FormatUtil.addXMLElementEscaped(sb, "ItinKey", itinerary.getItineraryID());
        FormatUtil.addXMLElementEscaped(sb, "Name", itinerary.getName());
        FormatUtil.addXMLElementEscaped(sb, "ItinKey", itinerary.getItineraryID());
        FormatUtil.addXMLElementEscaped(sb, "TacKey", taConfig.getTravelAllowanceConfigurationList().getTacKey());
        FormatUtil.addXMLElementEscaped(sb, "RptKey", itinerary.getExpenseReportID());

       // FormatUtil.addXMLElementEscaped(sb, "ShortDistanceTrip", "N");
        // FormatUtil.addXMLElementEscaped(sb, "RptKey", itinerary.getExpenseReportID());


        if (itinerary.getSegmentList().isEmpty()) {
            sb.append("<ItineraryRows/>");
        } else {
            sb.append("<ItineraryRows>");
            for (ItinerarySegment segment : itinerary.getSegmentList()) {

                sb.append("<ItineraryRow>");

                FormatUtil.addXMLElementEscaped(sb, "IrKey", segment.getId());
                FormatUtil.addXMLElementEscaped(sb, "DepartLnKey", segment.getDepartureLocation().getCode());
                FormatUtil.addXMLElementEscaped(sb, "DepartDateTime",
                        dateTimeFormat.format(segment.getDepartureDateTime()));

                FormatUtil.addXMLElementEscaped(sb, "ArrivalLnKey", segment.getArrivalLocation().getCode());
//                FormatUtil.addXMLElementEscaped(sb, "ArrivalRlKey", itineraryRow.getArrivalRlKey());
                FormatUtil.addXMLElementEscaped(sb, "ArrivalDateTime",
                        dateTimeFormat.format(segment.getArrivalDateTime()));
                FormatUtil.addXMLElementEscaped(sb, "ArrivalRlKey", segment.getArrivalLocation().getRateLocationKey());

                FormatUtil.addXMLElementEscaped(sb, "BorderCrossDateTime",
                        dateTimeFormat.format(segment.getArrivalDateTime()));


                sb.append("</ItineraryRow>");
            }
            sb.append("</ItineraryRows>");
        }
        sb.append("</Itinerary>");

        return sb.toString();
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        // register the parser of interest
        itinParser = new GetTAItinerariesResponseParser();
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
       // core.setTAItinerary(itinParser.getItinerary());

        return RESULT_OK;
    }
}
