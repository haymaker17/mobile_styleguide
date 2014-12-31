/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ServiceReply</code> for handling the response to an attendee form request.
 * 
 * @author andy
 */
public class AttendeeFormReply extends ServiceReply {

    // private static final String CLS_TAG = AttendeeFormReply.class.getSimpleName();

    /**
     * Contains a reference to the parsed report attendee.
     */
    public ExpenseReportAttendee attendee;

    /**
     * Will parse the XML response body to an attendee search request.
     * 
     * @param responseXml
     *            the response XML body.
     * @return an instance of <code>SearchAttendeesReply</code> containing the list of attendees.
     */
    public static AttendeeFormReply parseXMLReply(String responseXml) {

        AttendeeFormReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ExpenseReportAttendee.ExpenseReportAttendeeSAXHandler handler = new ExpenseReportAttendee.ExpenseReportAttendeeSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = new AttendeeFormReply();
            List<ExpenseReportAttendee> attendees = handler.getReportAttendees();
            if (attendees != null) {
                if (attendees.size() > 0) {
                    reply.attendee = attendees.get(0);
                    if (reply.attendee != null) {
                        reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
                    } else {
                        reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
                    }
                } else {
                    reply.mwsStatus = Const.REPLY_STATUS_FAILURE;
                }
            } else {
                reply.mwsStatus = Const.REPLY_STATUS_FAILURE;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

}
