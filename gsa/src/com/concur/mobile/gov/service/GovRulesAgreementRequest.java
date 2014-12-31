package com.concur.mobile.gov.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

public class GovRulesAgreementRequest extends GetServiceRequest {

    public static final String CLS_TAG = GovRulesAgreementRequest.class.getSimpleName();
    public static final String SERVICE_END_POINT = "/mobile/Home/AgreeToSafeHarbor/";

    public Boolean isAgree;

    @Override
    protected String getServiceEndpointURI() {
        return buildRequestBody();
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService)
        throws IOException
    {
        ActionStatusServiceReply reply = new ActionStatusServiceReply();

        // Parse the response or log an error.
        int statusCode = response.getResponseCode();
        if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String xmlReply = readStream(is, encoding);
            reply = ActionStatusServiceReply.parseReply(xmlReply);
            if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                Log.e(Const.LOG_TAG, "GovRulesAgreementRequest: StatusCode: "
                    + statusCode
                    + ", StatusLine: " + response.getResponseCode() + ", response: "
                    + ((reply.mwsErrorMessage != null) ? reply.mwsErrorMessage : "null") + ".");
            }
        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

    protected String buildRequestBody() {
        String returnVal = null;
        StringBuilder body = new StringBuilder();
        body.append(SERVICE_END_POINT);
        body.append(Boolean.toString(isAgree));
        returnVal = body.toString();
        return returnVal;
    }
}
