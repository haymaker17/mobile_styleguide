/**
 * Copyright (c) 2011 Concur Technologies, Inc.
 */
package com.concur.mobile.core.expense.report.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

import com.concur.mobile.core.expense.report.data.ExpenseReportApprover;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ServiceReply</code> to handle the results of an approver search request.
 * 
 * @author Chris N. Diaz
 */
public class ApproverSearchReply extends ServiceReply {

    private static final String CLS_TAG = ApproverSearchReply.class.getSimpleName();

    private static final String NAMESPACE = "";

    /**
     * Contains the approver search results.
     */
    public List<ExpenseReportApprover> results;

    /**
     * The source query.
     */
    public String query;

    /**
     * Will parse the XML response body to an approver search request.
     * 
     * @param inputStream
     *            the response XML body.
     * @param encoding
     *            the encoding of the <code>inputStream</code> (e.g. <code>Encoding.UTF-8</code>)
     * @return an instance of <code>SearchApproverReply</code> containing the list of approvers.
     */
    public static ApproverSearchReply parseXMLReply(InputStream inputStream, Encoding encoding) {

        ApproverSearchReply reply = new ApproverSearchReply();

        if (inputStream != null && encoding != null) {

            final List<ExpenseReportApprover> listOfApprovers = new ArrayList<ExpenseReportApprover>();
            final ExpenseReportApprover currApprover = new ExpenseReportApprover();

            RootElement root = new RootElement(NAMESPACE, "ArrayOfApproverInfo");
            Element approverInfo = root.getChild(NAMESPACE, "ApproverInfo");
            approverInfo.setStartElementListener(new StartElementListener() {

                public void start(Attributes attributes) {
                    // Clear the current values of the ApproverInfo
                    currApprover.reset();
                }
            });
            approverInfo.setEndElementListener(new EndElementListener() {

                public void end() {
                    // Make a clone of the current ApproverInfo and
                    // add it to the list of ExpenseReportApprovers.
                    listOfApprovers.add(currApprover.clone());
                }
            });

            Element email = approverInfo.getChild(NAMESPACE, "Email");
            email.setEndTextElementListener(new EndTextElementListener() {

                public void end(String body) {
                    currApprover.email = body;
                }
            });

            Element empKey = approverInfo.getChild(NAMESPACE, "EmpKey");
            empKey.setEndTextElementListener(new EndTextElementListener() {

                public void end(String body) {
                    currApprover.empKey = body;
                }
            });

            Element firstName = approverInfo.getChild(NAMESPACE, "FirstName");
            firstName.setEndTextElementListener(new EndTextElementListener() {

                public void end(String body) {
                    currApprover.firstName = body;
                }
            });

            Element lastName = approverInfo.getChild(NAMESPACE, "LastName");
            lastName.setEndTextElementListener(new EndTextElementListener() {

                public void end(String body) {
                    currApprover.lastName = body;
                }
            });

            Element loginId = approverInfo.getChild(NAMESPACE, "LoginId");
            loginId.setEndTextElementListener(new EndTextElementListener() {

                public void end(String body) {
                    currApprover.loginId = body;
                }
            });

            try {
                Xml.parse(inputStream, encoding, root.getContentHandler());
                reply.results = listOfApprovers;
                reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
            } catch (Exception e) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".respnseXml - error parsing XML.", e);
                throw new RuntimeException(e);
            }
        }

        return reply;
    }
}
