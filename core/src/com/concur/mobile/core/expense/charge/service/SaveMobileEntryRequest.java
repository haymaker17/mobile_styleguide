/**
 * 
 */
package com.concur.mobile.core.expense.charge.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.util.Log;

import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.MobileEntry;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>PostServiceRequest</code> to handle saving mobile entries.
 * 
 * @author AndrewK
 */
public class SaveMobileEntryRequest extends PostServiceRequest {

    private static final String CLS_TAG = SaveMobileEntryRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/Expense/SaveMobileEntry";

    /**
     * Contains the local key of the mobile entry.
     */
    public String localKey;

    /**
     * Contains the mobile entry key for the entry to be saved. If this value is <code>null</code>, then this mobile entry is
     * being saved for the first time.
     */
    public String mobileEntryKey;

    /**
     * Contains a reference to the mobile entry being saved.
     */
    public MobileEntry mobileEntry;

    /**
     * Contains whether or not an existing receipt image should be cleared.
     */
    public boolean clearImage;

    /**
     * Contains a reference to the file that has the entire request.
     */
    public File requestFile;

    /**
     * Contains the absolute path of the receipt image file.
     */
    public String filePath;

    /**
     * Contains whether the receipt image file should be deleted after the attempted save.
     */
    public boolean deleteReceiptFile;

    // A flag set by the offline upload activity to indicate that this is being uploaded now.
    // This lets us differentiate between upload and editing an offline expense when online.
    public boolean forceUpload;

    public String expKey;

    /**
     * Gets the mobile entry key of the mobile entry being saved.
     * 
     * @return returns the mobile entry key of the mobile entry being saved. If the value is <code>null</code>; then this mobile
     *         entry is being saved for the first time.
     */
    public String getMobileEntryKey() {
        return mobileEntryKey;
    }

    /**
     * Gets the instance of <code>MobileEntry</code> being saved.
     * 
     * @return returns the instance of <code>MobileEntry</code> being saved.
     */
    public MobileEntry getMobileEntry() {
        return mobileEntry;
    }

    /**
     * Gets whether an image associated with this mobile entry will be cleared upon successful completion of this request.
     * 
     * @return returns <code>true</code> if the receipt image will be cleared; <code>false</code> otherwise.
     */
    public boolean getClearImage() {
        return clearImage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    public String getServiceEndpointURI() {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(SERVICE_END_POINT);
        if (clearImage) {
            strBldr.append("/Y");
        }
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
            // Create a simple string entity.
            entity = new StringEntity(buildRequestBody(), Const.HTTP_BODY_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException unSupEncExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getPostEntity: unsupported encoding exception!", unSupEncExc);
            throw new ServiceRequestException(unSupEncExc.getMessage());
        }
        return entity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {

        SaveMobileEntryReply reply = new SaveMobileEntryReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = SaveMobileEntryReply.parseXMLReply(responseXml);
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

    @Override
    public String buildRequestBody() {
        String requestBody = null;

        if (mobileEntry != null) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("<MobileEntry>");
            // Mobile entries associated with a smart expense retain their CT keys based on
            // the paired card transaction. The CT key is not retained in the DB, but
            // rather reset upon smart expense edit based on matched card transaction.
            if (mobileEntry.getCctKey() != null && mobileEntry.getCctKey().length() > 0
                    && mobileEntry.getEntryType() != Expense.ExpenseEntryType.SMART_CORPORATE) {
                strBldr.append("<CctKey>");
                strBldr.append(mobileEntry.getCctKey());
                strBldr.append("</CctKey>");
            }
            strBldr.append("<Comment>");
            if (mobileEntry.hasComment()) {
                strBldr.append(FormatUtil.escapeForXML(mobileEntry.getComment()));
            }
            strBldr.append("</Comment>");
            strBldr.append("<CrnCode>");
            strBldr.append(mobileEntry.getCrnCode());
            strBldr.append("</CrnCode>");
            strBldr.append("<ExpKey>");
            strBldr.append(mobileEntry.getExpKey());
            strBldr.append("</ExpKey>");
            strBldr.append("<LocationName>");
            strBldr.append(FormatUtil.escapeForXML(mobileEntry.getLocationName()));
            strBldr.append("</LocationName>");
            if (mobileEntry.getMeKey() != null && mobileEntry.getMeKey().length() > 0) {
                strBldr.append("<MeKey>");
                strBldr.append(mobileEntry.getMeKey());
                strBldr.append("</MeKey>");
            }
            if (mobileEntry.getPctKey() != null && mobileEntry.getPctKey().length() > 0
                    && mobileEntry.getEntryType() != Expense.ExpenseEntryType.SMART_PERSONAL) {
                strBldr.append("<PctKey>");
                strBldr.append(mobileEntry.getPctKey());
                strBldr.append("</PctKey>");
            }
            if (mobileEntry.getReceiptImageId() != null && mobileEntry.getReceiptImageId().length() > 0 && !clearImage) {
                strBldr.append("<ReceiptImageId>");
                strBldr.append(mobileEntry.getReceiptImageId());
                strBldr.append("</ReceiptImageId>");
            }
            strBldr.append("<TransactionAmount>");
            strBldr.append(mobileEntry.getTransactionAmount());
            strBldr.append("</TransactionAmount>");
            strBldr.append("<TransactionDate>");
            strBldr.append(Format.safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY,
                    mobileEntry.getTransactionDateCalendar()));
            strBldr.append("</TransactionDate>");
            strBldr.append("<VendorName>");
            strBldr.append(FormatUtil.escapeForXML(mobileEntry.getVendorName()));
            strBldr.append("</VendorName>");
            strBldr.append("</MobileEntry>");
            requestBody = strBldr.toString();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildSaveMobileEntryPostBodyXML: mobile entry is null!");
        }

        return requestBody;
    }

    public String getExpKey() {
        return expKey;
    }

    public void setExpKey(String expKey) {
        this.expKey = expKey;
    }
}
