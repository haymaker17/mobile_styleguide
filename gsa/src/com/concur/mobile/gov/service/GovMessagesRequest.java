package com.concur.mobile.gov.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.http.HttpStatus;

import android.content.ContentValues;
import android.util.Log;

import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

public class GovMessagesRequest extends GetServiceRequest {

    public static final String CLS_TAG = GovMessagesRequest.class.getSimpleName();
    public static final String SERVICE_END_POINT = "/Mobile/MobileSession/GovWarningMessages";

    @Override
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

    @Override
    protected boolean isSessionRequired() {
        return false;
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService)
        throws IOException
    {
        GovMessagesReply reply = new GovMessagesReply();
        // Parse the response or log an error.
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            reply.xmlReply = readStream(is, encoding);
            reply = GovMessagesReply.parseXml(reply.xmlReply);
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
            // DB insertion
            MobileDatabase mdb = concurService.getMobileDatabase();
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            reply.lastRefreshTime = now;
            ContentValues contentValues = reply.getContentVals(reply);
            if (!(mdb.insertGovWarningMsgs(contentValues))) {
                Log.e(Const.LOG_TAG, CLS_TAG + " .processResponse: GovMessagesReply insertion is failed");
            }
        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }
}
