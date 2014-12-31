/**
 * 
 */
package com.concur.mobile.core.expense.charge.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.Expense.ExpenseEntrySAXHandler;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * 
 * An extension of <code>GetServiceRequest</code> used to retrieve a list of both cash + card expenses.
 * 
 * @deprecated v9.16 - This has been replaced with the SmartExpense and ExpenseProvider.
 * 
 * @author AndrewK
 */
@Deprecated
public class AllExpenseRequest extends GetServiceRequest {

    private static final String CLS_TAG = AllExpenseRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/Expense/GetAllExpenses";

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        AllExpenseReply reply = new AllExpenseReply();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            reply.xmlReply = readStream(is, encoding);
            ExpenseEntrySAXHandler handler = null;
            try {
                handler = Expense.parseExpenseEntryXml(reply.xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            reply.expenses = handler.getExpenses();
            reply.personalCards = handler.getPersonalCards();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }
}
