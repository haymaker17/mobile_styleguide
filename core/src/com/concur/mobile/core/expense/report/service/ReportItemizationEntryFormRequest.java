/**
 * 
 */
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

/**
 * An extension of <code>GetServiceRequest</code> for retrieving an itemization entry editing form.
 * 
 * @author andy
 */
public class ReportItemizationEntryFormRequest extends GetServiceRequest {

    public static final String CLS_TAG = ReportItemizationEntryFormRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Expense/ReportEntryItemizeForm";

    // Contains whether to include a form definition.
    public boolean withFormDef;

    // The EXP_KEY
    public String expKey;

    // The report key.
    public String reportKey;

    // The parent report entry key.
    public String parentEntryKey;

    // The entry key
    public String entryKey;

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
        strBldr.append((withFormDef) ? 'Y' : 'N');
        strBldr.append('/');
        strBldr.append(expKey);
        strBldr.append('/');
        strBldr.append(reportKey);
        strBldr.append('/');
        strBldr.append(parentEntryKey);
        if (entryKey != null) {
            strBldr.append('/');
            strBldr.append(entryKey);
        }
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
        ReportItemizationEntryFormReply reply = new ReportItemizationEntryFormReply();

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
