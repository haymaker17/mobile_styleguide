/**
 * 
 */
package com.concur.mobile.core.expense.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;

/**
 * An extension of <code>PostServiceRequest</code> to send a search list request to the server.
 * 
 * @author AndrewK
 */
public class SearchListRequest extends PostServiceRequest {

    private static final String CLS_TAG = SearchListRequest.class.getSimpleName();

    /**
     * Contains the service end-point.
     */
    public static final String SERVICE_END_POINT = "/mobile/Expense/SearchListItemsV2";

    /**
     * Whether to include any MRU's at the top of the result list.
     */
    public boolean isMRU;

    /**
     * Contains the query text.
     */
    public String query;

    /**
     * Contains the field id.
     */
    public String fieldId;

    /**
     * Contains the ft code.
     */
    public String ftCode;

    /**
     * Contains the list key.
     */
    public String listKey;

    /**
     * Contains the parent list item key.
     */
    public String parentLiKey;

    /**
     * Contains the report key.
     */
    public String reportKey;

    /**
     * Contains the search by field.
     */
    public String searchBy;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<ListSearchCriteria>");
        addElement(strBldr, "FieldId", ((fieldId != null) ? fieldId : ""));
        addElement(strBldr, "FtCode", ((ftCode != null) ? ftCode : ""));
        if ((!fieldId.equalsIgnoreCase(ListItem.DEFAULT_KEY_CURRENCY))) {
            addElement(strBldr, "IsMru", ((isMRU) ? "Y" : "N"));
        }
        if (listKey != null) {
            addElement(strBldr, "ListKey", listKey);
        }
        if (parentLiKey != null) {
            addElement(strBldr, "ParentLiKey", parentLiKey);
        }
        addElement(strBldr, "Query", ((query != null) ? FormatUtil.escapeForXML(query) : ""));
        if (reportKey != null) {
            addElement(strBldr, "RptKey", reportKey);
        }
        if (searchBy != null) {
            addElement(strBldr, "SearchBy", searchBy);
        }
        strBldr.append("</ListSearchCriteria>");
        return strBldr.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#getPostEntity(com.concur.mobile.service.ConcurService)
     */
    @Override
    protected HttpEntity getPostEntity(ConcurService concurService) throws ServiceRequestException {
        HttpEntity entity = null;
        try {
            entity = new StringEntity(buildRequestBody(), Const.HTTP_BODY_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException uee) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getPostEntity: unsupported encoding exception!", uee);
            throw new ServiceRequestException(uee.getMessage());
        }
        return entity;
    }

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

        SearchListReply reply = new SearchListReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = SearchListReply.parseXMLReply(responseXml);
            } catch (RuntimeException re) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".processResponse: runtime exception during parse", re);
            } finally {
                ConcurCore app = (ConcurCore) concurService.getApplication();
                IExpenseEntryCache expEntCache = app.getExpenseEntryCache();
                expEntCache.putListItemInCacheForMRU(concurService, userId, fieldId);
            }

        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
