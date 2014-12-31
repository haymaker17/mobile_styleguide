package com.concur.mobile.core.expense.report.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;

public class ItemizeHotelRequest extends PostServiceRequest {

    private static final String CLS_TAG = ItemizeHotelRequest.class.getSimpleName();

    /**
     * Contains the service end-point.
     */
    public static final String SERVICE_END_POINT = "/Mobile/Expense/ItemizeHotel";

    // A reference to the expense report entry being itemized.
    public ExpenseReportEntryDetail expRepEntDet;

    public boolean combineAmounts;

    public String checkIn;

    public String checkOut;

    public String nights;

    public Double roomRate;

    public Double roomTax;

    public Double otherTax1;

    public Double otherTax2;

    public String additionalExpKey1;

    public Double additionalAmount1;

    public String additionalExpKey2;

    public Double additionalAmount2;

    @Override
    public String buildRequestBody() {
        if (requestBody == null) {
            if (expRepEntDet != null) {
                StringBuilder sb = new StringBuilder();

                sb.append("<HotelItemization>");

                // Add additional charges if any
                if (additionalExpKey1 != null && additionalAmount1 != null) {
                    sb.append("<AdditionalCharges>");

                    sb.append("<ExpenseCharge>");
                    addElement(sb, "Amount", additionalAmount1);
                    addElement(sb, "ExpKey", additionalExpKey1);
                    sb.append("</ExpenseCharge>");

                    if (additionalExpKey2 != null && additionalAmount2 != null) {

                        sb.append("<ExpenseCharge>");
                        addElement(sb, "Amount", additionalAmount2);
                        addElement(sb, "ExpKey", additionalExpKey2);
                        sb.append("</ExpenseCharge>");

                    }
                    sb.append("</AdditionalCharges>");
                }

                // If combining amounts, add other values to roomRate and set them to null.
                if (combineAmounts) {
                    double dRate = (roomRate == null ? 0.0 : roomRate);
                    double dTax = (roomTax == null ? 0.0 : roomTax);
                    double dOtherTax1 = (otherTax1 == null ? 0.0 : otherTax1);
                    double dOtherTax2 = (otherTax2 == null ? 0.0 : otherTax2);
                    roomRate = dRate + dTax + dOtherTax1 + dOtherTax2;

                    roomTax = null;
                    otherTax1 = null;
                    otherTax2 = null;
                }

                addElement(sb, "CheckInDate", checkIn);
                addElement(sb, "CheckOutDate", checkOut);
                addElement(sb, "NumberOfNights", nights);

                if (otherTax1 != null) {
                    addElement(sb, "OtherRoomTax1", otherTax1);
                }

                if (otherTax2 != null) {
                    addElement(sb, "OtherRoomTax2", otherTax2);
                }

                addElement(sb, "RoomRate", roomRate);

                if (roomTax != null) {
                    addElement(sb, "RoomTax", roomTax);
                }

                sb.append("</HotelItemization>");

                requestBody = sb.toString();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildRequestBody: expRepEntDet is null!");
            }
        }
        return requestBody;
    }

    @Override
    protected HttpEntity getPostEntity(ConcurService concurService) throws ServiceRequestException {
        HttpEntity entity = null;
        try {
            entity = new StringEntity(buildRequestBody(), Const.HTTP_BODY_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException unSupEncExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getPostEntity: unsupported encoding exception!", unSupEncExc);
            throw new ServiceRequestException(unSupEncExc.getMessage());
        }
        return entity;
    }

    @Override
    protected String getServiceEndpointURI() {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(SERVICE_END_POINT);
        strBldr.append('/');
        strBldr.append(Const.MOBILE_EXPENSE_USER);
        strBldr.append('/');
        strBldr.append(expRepEntDet.reportEntryKey);
        return strBldr.toString();
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        ItemizeHotelReply reply = new ItemizeHotelReply();

        // Parse the response or log an error.
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String xmlReply = readStream(is, encoding);
            try {
                reply = ItemizeHotelReply.parseReply(xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
