package com.concur.mobile.core.expense.report.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.charge.data.AttendeesEntryMap;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;

public class AddToReportRequest extends PostServiceRequest {

    private static final String CLS_TAG = AddToReportRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/Expense/AddToReportV5";

    public String reportKey;

    public String reportName;

    public ArrayList<String> meKeys;

    public ArrayList<String> pctKeys;

    public ArrayList<String> cctKeys;

    public ArrayList<Expense> smartCorpExpenses;

    public ArrayList<Expense> smartPersExpenses;

    public ArrayList<String> smartExpIds;

    public List<AttendeesEntryMap> attendeesEntryMaps;

    public boolean hasPersonalCardTransactions() {
        return (pctKeys != null && pctKeys.size() > 0);
    }

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

    @Override
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        AddToReportReply reply = new AddToReportReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }

            String responseXml = readStream(is, encoding);
            try {
                reply = AddToReportReply.parseXMLReply(responseXml);
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

        if (requestBody == null) {
            StringBuilder body = new StringBuilder();

            body.append("<AddToReportMap>");

            if (attendeesEntryMaps != null && attendeesEntryMaps.size() > 0) {
                body.append("<AttendeesMap xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">");
                for (AttendeesEntryMap attEntMap : attendeesEntryMaps) {
                    body.append("<AttendeeEntryMap>");
                    body.append("<Attendees xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">");
                    if (attEntMap.attendees != null) {
                        for (ExpenseReportAttendee expRepAtt : attEntMap.attendees) {
                            body.append("<Attendee xmlns=''>");
                            ViewUtil.addXmlElement(body, "Amount", expRepAtt.amount);
                            ViewUtil.addXmlElement(body, "AtnKey", expRepAtt.atnKey);
                            ViewUtil.addXmlElement(body, "InstanceCount", expRepAtt.instanceCount);
                            ViewUtil.addXmlElementYN(body, "IsAmountEdited", expRepAtt.isAmountEdited);
                            ViewUtil.addXmlElement(body, "VersionNumber", expRepAtt.versionNumber);
                            body.append("</Attendee>");
                        }
                    }
                    body.append("</Attendees>");
                    ViewUtil.addXmlElement(body, "MeKey", attEntMap.meKey);
                    body.append("</AttendeeEntryMap>");
                }
                body.append("</AttendeesMap>");
            }
            if (reportKey != null) {
                body.append("<RptKey>").append(reportKey).append("</RptKey>");
            } else if (reportName != null) {
                body.append("<ReportName>").append(FormatUtil.escapeForXML(reportName)).append("</ReportName>");
            } else {
                // Generate a report name
                Context ctx = ConcurCore.getContext();
                StringBuilder rptName = new StringBuilder(ctx.getText(R.string.auto_report_name)).append(" ");
                rptName.append(DateFormat.getDateFormat(ctx).format(new Date()));
                body.append("<ReportName>").append(rptName).append("</ReportName>");
            }
            // append receipt capture
            if (smartExpIds != null && smartExpIds.size() > 0) {
                body.append("<SmartExpenseIds xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">");
                final int c = smartExpIds.size();
                for (int i = 0; i < c; i++) {
                    body.append("<a:string>").append(smartExpIds.get(i)).append("</a:string>");
                }
                body.append("</SmartExpenseIds>");
            }
            body.append("</AddToReportMap>");
            requestBody = body.toString();
        }
        return requestBody;
    }

}
