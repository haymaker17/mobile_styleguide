/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Calendar;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

public class DistanceToDateRequest extends GetServiceRequest {

    private static final String CLS_TAG = DistanceToDateRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/Expense/DistanceToDate";

    public Integer carKey;
    public Calendar date;
    public String excludeRpeKey;

    @Override
    protected String getServiceEndpointURI() {
        StringBuilder sb = new StringBuilder();
        sb.append(SERVICE_END_POINT);
        sb.append('/');
        sb.append(carKey);
        sb.append('/');
        sb.append(Format.safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY, date));
        if (excludeRpeKey != null) {
            sb.append('/');
            sb.append(excludeRpeKey);
        }
        return sb.toString();
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        DistanceToDateReply reply = new DistanceToDateReply();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String xmlReply = readStream(is, encoding);
            try {
                reply = DistanceToDateReply.parseReply(xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
