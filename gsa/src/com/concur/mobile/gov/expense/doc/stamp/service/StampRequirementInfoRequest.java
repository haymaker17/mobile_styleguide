/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.stamp.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.expense.doc.stamp.data.ReasonCodeReqdResponse;

public class StampRequirementInfoRequest extends GetServiceRequest {

    public static final String CLS_TAG = StampRequirementInfoRequest.class.getSimpleName();
    public static final String SERVICE_END_POINT = "/Mobile/GovTravelManager/GetTMStampRequirementInfo/";
    public String stampName;
    public String docName;
    public String docType;
    public String userId;
    public String travId;
    public String stampReqUserId;

    @Override
    protected String getServiceEndpointURI() {
        return buildRequestBody();
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService)
        throws IOException
    {
        StampRequirementInfoReply reply = new StampRequirementInfoReply();
        // Parse the response or log an error.
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            reply.xmlReply = readStream(is, encoding);
            reply = StampRequirementInfoReply.parseXmlResponse(reply.xmlReply);
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
            // DB insertion
            MobileDatabase mdb = concurService.getMobileDatabase();
            String userID = concurService.prefs.getString(Const.PREF_USER_ID, null);
            ReasonCodeReqdResponse stampReqResponse = reply.reqdResponse;
            stampReqResponse.userId = userID;
            stampReqResponse.docName = docName;
            stampReqResponse.docType = docType;
            stampReqResponse.travId = travId;
            // TODO COMMENT...
            stampReqResponse.stampReqUserId = stampReqUserId;
            ContentValues contentValues = stampReqResponse.getContentVals(stampReqResponse);
            if (!(mdb.insertStampDocumentReqInfo(contentValues))) {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + " .processResponse: insertStampDocumentReqInfo insertion is failed");
            }
        } else {
            // Log the error. and show what is the response contains.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

    protected String buildRequestBody() {
        String returnVal = null;
        StringBuilder body = new StringBuilder();
        body.append(SERVICE_END_POINT);
        String name = Uri.encode(stampName, Const.HTTP_BODY_CHARACTER_ENCODING);
        body.append(name);
        returnVal = body.toString();
        return returnVal;
    }
}
