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

/**
 * An extension of <code>GetServiceRequest</code> for getting an exchange rate between two currencies on a particular date.
 * 
 * @author andy
 */
public class ExchangeRateRequest extends GetServiceRequest {

    private static final String CLS_TAG = ExchangeRateRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/Expense/ExchangeRate";

    /**
     * Contains the source currency code.
     */
    public String fromCrnCode;

    /**
     * Contains the destination currency code.
     */
    public String toCrnCode;

    /**
     * Contains the date for the exchange rate.
     */
    public Calendar date;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(SERVICE_END_POINT);
        strBldr.append('/');
        strBldr.append(fromCrnCode);
        strBldr.append('/');
        strBldr.append(toCrnCode);
        strBldr.append('/');
        if (date == null) {
            date = Calendar.getInstance();
        }
        strBldr.append(Format.safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY, date));
        return strBldr.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        ExchangeRateReply reply = new ExchangeRateReply();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String xmlReply = readStream(is, encoding);
            try {
                reply = ExchangeRateReply.parseReply(xmlReply);
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
