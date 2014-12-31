package com.concur.mobile.core.travel.service;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.service.parser.MWSResponseParser;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;

/**
 * AsyncTask used for Trip approval action request (approve or reject) to server.
 * 
 * @author RatanK
 * 
 */
public class TripApproval extends CoreAsyncRequestTask {

    private String action;
    private String comments;
    private String travellerCompanyId;
    private String travellerUserId;
    private String tripIdForAction;

    private MWSResponseParser mwsRespParser;

    public TripApproval(Context context, int id, BaseAsyncResultReceiver receiver, String action, String comments,
            String travellerCompanyId, String travellerUserId, String tripIdForAction) {

        super(context, id, receiver);

        this.action = action;
        this.comments = comments;
        this.travellerCompanyId = travellerCompanyId;
        this.travellerUserId = travellerUserId;
        this.tripIdForAction = tripIdForAction;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TripApprovalActionV2";
    }

    @Override
    protected String getPostBody() {
        // form the the request XML
        StringBuilder sb = new StringBuilder();

        sb.append("<ApproveAction>");
        FormatUtil.addXMLElement(sb, "Action", action);
        FormatUtil.addXMLElementEscaped(sb, "Comment", comments);
        FormatUtil.addXMLElement(sb, "CompanyId", travellerCompanyId);
        FormatUtil.addXMLElement(sb, "ItinLocator", tripIdForAction);
        FormatUtil.addXMLElement(sb, "UserId", travellerUserId);
        sb.append("</ApproveAction>");

        return sb.toString();
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        // register the parser of interest
        mwsRespParser = new MWSResponseParser();
        parser.registerParser(mwsRespParser, "MWSResponse");

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

        MWSResponseStatus reqStatus = mwsRespParser.getRequestTaskStatus();

        resultData.putBoolean(IS_SUCCESS, reqStatus.isSuccess());
        resultData.putString(ERROR_MESSAGE, reqStatus.getResponseMessage());

        return RESULT_OK;
    }

}
