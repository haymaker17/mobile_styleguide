/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ServiceReply</code> to handle a request for default attendee information.
 */
public class DefaultAttendeeReply extends ServiceReply {

    private static final String CLS_TAG = DefaultAttendeeReply.class.getSimpleName();

    /**
     * Contains a reference to the parsed default attendee.
     */
    public ExpenseReportAttendee attendee;

    /**
     * Will parse the XML response body to an attendee search request.
     * 
     * @param responseXml
     *            the response XML body.
     * @return an instance of <code>SearchAttendeesReply</code> containing the list of attendees.
     */
    public static DefaultAttendeeReply parseReply(String responseXml) {

        DefaultAttendeeReply reply = new DefaultAttendeeReply();
        reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;

        if (responseXml.trim().length() > 0) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            try {
                SAXParser parser = factory.newSAXParser();
                ExpenseReportAttendee.ExpenseReportAttendeeSAXHandler handler = new ExpenseReportAttendee.ExpenseReportAttendeeSAXHandler();
                parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);

                List<ExpenseReportAttendee> attendees = handler.getReportAttendees();
                if (attendees != null) {
                    for (ExpenseReportAttendee attendee : attendees) {
                        if (attendee.atnTypeCode != null
                                && attendee.atnTypeCode.equalsIgnoreCase(Const.SYSTEM_EMPLOYEE_ATTENDEE_TYPE_CODE)) {
                            reply.attendee = attendee;
                        }
                    }
                    if (reply.attendee == null) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".parseReply: no default attendee found!");
                    }

                } else {
                    reply.mwsStatus = Const.REPLY_STATUS_FAILURE;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return reply;
    }

}
