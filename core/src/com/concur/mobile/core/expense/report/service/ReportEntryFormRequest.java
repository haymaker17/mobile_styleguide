package com.concur.mobile.core.expense.report.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

public class ReportEntryFormRequest extends GetServiceRequest {

    public static final String CLS_TAG = ReportEntryFormRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Expense/ReportEntryFormV4";

    // The EXP_KEY
    public String expKey;

    // The report key.
    public String reportKey;

    // The entry key
    public String entryKey;

    @Override
    protected String getServiceEndpointURI() {
        StringBuilder sb = new StringBuilder();
        sb.append(SERVICE_END_POINT);
        sb.append('/').append(expKey);

        if (reportKey != null) {
            sb.append('/').append(reportKey);

            if (entryKey != null) {
                sb.append('/').append(entryKey);
            }
        }

        String serverURI = sb.toString();
        return serverURI;
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {

        ReportEntryFormReply reply = new ReportEntryFormReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            reply.xmlReply = readStream(is, encoding);

            try {
                reply.entryDetail = ExpenseReportEntryDetail.parseReportEntryDetailXml(reply.xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }

            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;

        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
