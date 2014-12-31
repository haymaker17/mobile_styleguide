package com.concur.mobile.core.expense.report.service;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.concur.mobile.core.expense.report.data.AttendeeSearchField;
import com.concur.mobile.core.expense.report.data.AttendeeSearchField.AttendeeSearchFieldSAXHandler;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

public class AttendeeSearchFieldsReply extends ServiceReply {

    public List<AttendeeSearchField> atnSrchFlds;

    static AttendeeSearchFieldsReply parseAttendeeSearchFields(String responseXml) {
        AttendeeSearchFieldsReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            AttendeeSearchFieldSAXHandler handler = new AttendeeSearchFieldSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = new AttendeeSearchFieldsReply();
            reply.atnSrchFlds = handler.getAttendeeSearchFields();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

}
