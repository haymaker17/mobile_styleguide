/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.charge.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

public class MobileExpenseListRequest extends GetServiceRequest {

    public static final String CLS_TAG = MobileExpenseListRequest.class.getSimpleName();
    public static final String SERVICE_END_POINT = "/Mobile/GovTravelManager/GetTMUnappliedExpenses";

    @Override
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService)
        throws IOException
    {
        MobileExpenseListReply reply = new MobileExpenseListReply();
        // Parse the response or log an error.
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            reply.xmlReply = readStream(is, encoding);
            reply = MobileExpenseListReply.parseXmlResponse(reply.xmlReply);
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }
}
