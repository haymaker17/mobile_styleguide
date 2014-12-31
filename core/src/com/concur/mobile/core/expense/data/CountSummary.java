package com.concur.mobile.core.expense.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

public class CountSummary {

    public int corpCardTransCount;
    public int persCardTransCount;
    public int mobileEntryCount;
    public int reportsToApprove;
    public int travelRequestsToApprove;
    public int unsubmittedReportsCount;
    public String unsubmittedReportsCrnCode;
    public double unsubmittedReportsTotal;
    public int invoicesToApprove;
    public int invoicesToSubmit;
    public int tripsToApproveCount;
    public int receiptCaptureCount;
    public int purchaseRequestsToApprove;

    public CountSummary() {

    }

    public static CountSummary parseSummaryXml(String responseXml) {

        CountSummary summary = null;

        if (responseXml != null && responseXml.length() > 0) {

            summary = new CountSummary();

            DocumentBuilderFactory factory = null;
            DocumentBuilder builder = null;
            Document doc = null;

            try {
                factory = DocumentBuilderFactory.newInstance();
                builder = factory.newDocumentBuilder();
                doc = builder.parse(new ByteArrayInputStream(responseXml.getBytes()));
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // Something went south on the request. Log it.
                Log.e(Const.LOG_TAG, "Request generated an error", e);
            } catch (IllegalStateException e) {
                // Unable to parse the response XML. You know what to do.
                Log.e(Const.LOG_TAG, "XML response was not parseable", e);
            } catch (SAXException e) {
                // Unable to parse the response XML. You know what to do.
                Log.e(Const.LOG_TAG, "XML response was not parseable", e);
            }

            if (doc == null) {
                summary.unsubmittedReportsCrnCode = "";
            } else {
                // Corporate card transaction count.
                NodeList corpCardTransNode = doc.getElementsByTagName("CorporateCardTransactionCount");
                if (corpCardTransNode.item(0) != null) {
                    Integer entries = Parse.safeParseInteger(corpCardTransNode.item(0).getFirstChild().getNodeValue());
                    summary.corpCardTransCount = (entries == null) ? 0 : entries;
                }

                NodeList invToApproveNode = doc.getElementsByTagName("InvoicesToApproveCount");
                if (invToApproveNode.item(0) != null) {
                    Integer invoices = Parse.safeParseInteger(invToApproveNode.item(0).getFirstChild().getNodeValue());
                    summary.invoicesToApprove = (invoices == null) ? 0 : invoices;
                }

                NodeList invToSubmitNode = doc.getElementsByTagName("InvoicesToSubmitCount");
                if (invToSubmitNode.item(0) != null) {
                    Integer invoices = Parse.safeParseInteger(invToSubmitNode.item(0).getFirstChild().getNodeValue());
                    summary.invoicesToSubmit = (invoices == null) ? 0 : invoices;
                }

                NodeList prToApproveNode = doc.getElementsByTagName("PurchaseRequestsToApproveCount");
                if (prToApproveNode.item(0) != null) {
                    Integer pr = Parse.safeParseInteger(prToApproveNode.item(0).getFirstChild().getNodeValue());
                    summary.purchaseRequestsToApprove = (pr == null) ? 0 : pr;
                }

                // Mobile entry count.
                NodeList entryNode = doc.getElementsByTagName("MobileEntryCount");
                if (entryNode.item(0) != null) {
                    Integer entries = Parse.safeParseInteger(entryNode.item(0).getFirstChild().getNodeValue());
                    summary.mobileEntryCount = (entries == null) ? 0 : entries;
                }

                // Corporate card transaction count.
                NodeList persCardTransNode = doc.getElementsByTagName("PersonalCardTransactionCount");
                if (persCardTransNode.item(0) != null) {
                    Integer entries = Parse.safeParseInteger(persCardTransNode.item(0).getFirstChild().getNodeValue());
                    summary.persCardTransCount = (entries == null) ? 0 : entries;
                }

                // ExpenseIt receipt capture count.
                NodeList receiptCaptureCount = doc.getElementsByTagName("ReceiptCaptureCount");
                if (receiptCaptureCount.item(0) != null) {
                    Integer entries = Parse
                            .safeParseInteger(receiptCaptureCount.item(0).getFirstChild().getNodeValue());
                    summary.receiptCaptureCount = (entries == null) ? 0 : entries;
                }

                NodeList approvalsNode = doc.getElementsByTagName("ReportsToApproveCount");
                if (approvalsNode.item(0) != null) {
                    Integer approvals = Parse.safeParseInteger(approvalsNode.item(0).getFirstChild().getNodeValue());
                    summary.reportsToApprove = (approvals == null) ? 0 : approvals;
                }

                // Travel Request Approvals
                NodeList trApprovalsNode = doc.getElementsByTagName("TravelRequestApprovalCount");
                if (trApprovalsNode.item(0) != null) {
                    Integer approvals = Parse.safeParseInteger(trApprovalsNode.item(0).getFirstChild().getNodeValue());
                    summary.travelRequestsToApprove = (approvals == null) ? 0 : approvals;
                }

                NodeList reportCountNode = doc.getElementsByTagName("UnsubmittedReportsCount");
                if (reportCountNode.item(0) != null) {
                    Integer count = Parse.safeParseInteger(reportCountNode.item(0).getFirstChild().getNodeValue());
                    summary.unsubmittedReportsCount = count;
                }

                NodeList reportCrnNode = doc.getElementsByTagName("UnsubmittedReportsCrnCode");
                if (reportCrnNode.item(0) != null) {
                    String crnCode = reportCrnNode.item(0).getFirstChild().getNodeValue();
                    summary.unsubmittedReportsCrnCode = crnCode;
                }

                NodeList reportTotalNode = doc.getElementsByTagName("UnsubmittedReportsTotal");
                if (reportTotalNode.item(0) != null) {
                    Double total = Parse.safeParseDouble(reportTotalNode.item(0).getFirstChild().getNodeValue());
                    summary.unsubmittedReportsTotal = total;
                }

                // for trip approvals
                NodeList tripsToApproveTotalNode = doc.getElementsByTagName("TripsToApproveCount");
                Node tripsAppCountNode = tripsToApproveTotalNode.item(0);
                if (tripsAppCountNode != null) {
                    summary.tripsToApproveCount = Parse.safeParseInteger(tripsAppCountNode.getFirstChild()
                            .getNodeValue());
                }
            }
        }

        return summary;
    }

}
