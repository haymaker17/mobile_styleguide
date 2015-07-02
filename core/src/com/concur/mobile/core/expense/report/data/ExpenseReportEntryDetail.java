/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.AccessType;
import com.concur.mobile.core.expense.report.service.GetTaxFormReply;
import com.concur.mobile.core.expense.report.service.TaxForm;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;

/**
 * Models detailed expense report entry information.
 * 
 * @author AndrewK
 */
public class ExpenseReportEntryDetail extends ExpenseReportEntry {

    private static final long serialVersionUID = 1L;

    private static final String CLS_TAG = ExpenseReportEntryDetail.class.getSimpleName();

    /**
     * Contains the XML representation for this report entry.
     */
    public String xmlRep;

    /**
     * Contains a list of <code>ExpenseReportComment</code> objects representing entry-level comments.
     */
    private ArrayList<ExpenseReportComment> comments;

    /**
     * Contains a list of <code>ExpenseReportExceptions</code> objects representing entry-level exceptions.
     */
    private ArrayList<ExpenseReportException> exceptions;

    /**
     * Contains a list of <code>ExpenseReportFormField</code> objects representing entry-level custom fields.
     */
    private List<ExpenseReportFormField> formFields;

    /**
     * Contains a list of <code>ExpenseReportFormField</code> objects representing entry-level custom fields.
     */
    private List<TaxForm> taxForms;

    /**
     * Contains a list of <code>ExpenseReportEntryItemization</code> objects representing entry-level itemizations.
     */
    private ArrayList<ExpenseReportEntry> itemizations;

    /**
     * Contains a list of <code>ExpenseReportAttendee</code> objects representing entry level attendees.
     */
    private List<ExpenseReportAttendee> attendees;

    /**
     *
     * @return A list for {@code ExpenseReportFormField}s needed for travel allowance.
     * In case there are no travel allowance fields this method returns an empty list.
     */
    public List<ExpenseReportFormField> getTravelAllowanceFields(Context context) {
        ConcurCore app = (ConcurCore) context.getApplicationContext();
        FixedTravelAllowance ta = app.getFixedTravelAllowanceController().getFixedTA(taDayKey);
        List<ExpenseReportFormField> travelAllowanceFields = new ArrayList<ExpenseReportFormField>();
        
        if (ta == null) {
            travelAllowanceFields.add(new ExpenseReportFormField("1", null, "No adjustment available", AccessType.RO,
                    ExpenseReportFormField.ControlType.EDIT, ExpenseReportFormField.DataType.VARCHAR, true));
            return travelAllowanceFields;
        }

        String breakfastLabel = app.getFixedTravelAllowanceController().getBreakfastLabel();
        String lunchLabel = app.getFixedTravelAllowanceController().getLunchLabel();
        String dinnerLabel = app.getFixedTravelAllowanceController().getDinnerLabel();

        ExpenseReportFormField field1 = new ExpenseReportFormField("1", breakfastLabel, ta.getBreakfastProvision().getCodeDescription(), AccessType.RO,
                ExpenseReportFormField.ControlType.EDIT, ExpenseReportFormField.DataType.VARCHAR, true);

        ExpenseReportFormField field2 = new ExpenseReportFormField("2", lunchLabel, ta.getLunchProvision().getCodeDescription(), AccessType.RO,
                ExpenseReportFormField.ControlType.EDIT, ExpenseReportFormField.DataType.VARCHAR, true);

        ExpenseReportFormField field3 = new ExpenseReportFormField("3", dinnerLabel, ta.getDinnerProvision().getCodeDescription(), AccessType.RO,
                ExpenseReportFormField.ControlType.EDIT, ExpenseReportFormField.DataType.VARCHAR, true);



        travelAllowanceFields.add(field1);
        travelAllowanceFields.add(field2);
        travelAllowanceFields.add(field3);

        return travelAllowanceFields;
    }


    /**
     * Gets whether this is a detail report entry.
     * 
     * @return whether this is a detailed report entry.
     */
    public boolean isDetail() {
        return true;
    }

    /**
     * Gets the list of entry-level comments.
     * 
     * @return the list of entry-level comments.
     */
    public ArrayList<ExpenseReportComment> getComments() {
        return comments;
    }

    /**
     * Gets the list of entry-level exceptions.
     * 
     * @return the list of entry-level exceptions.
     */
    public ArrayList<ExpenseReportException> getExceptions() {
        return exceptions;
    }

    /**
     * Gets the list of entry-level form fields.
     * 
     * @return the list of entry-level form fields.
     */
    public List<ExpenseReportFormField> getFormFields() {
        return formFields;
    }

    /**
     * Gets the list of entry-level form fields.
     * 
     * @return the list of entry-level form fields.
     */
    public List<TaxForm> getTaxForm() {
        return taxForms;
    }

    /**
     * Sets the list of entry-level form fields.
     * 
     * param the list of entry-level form fields.
     */
    public void setTaxForm(List<TaxForm> taxForms) {
        this.taxForms = taxForms;
    }

    /**
     * Get the list of entry itemizations.
     * 
     * @return the list of entry itemizations.
     */
    public ArrayList<ExpenseReportEntry> getItemizations() {
        return itemizations;
    }

    /**
     * Sum up the amounts of child entries.
     * 
     * @return the summed up amount of all child entries or 0.0 if there are none.
     */
    public double getItemizationTotal() {
        double total = 0.0;
        if (itemizations != null) {
            int size = itemizations.size();
            for (int i = 0; i < size; i++) {
                total += itemizations.get(i).transactionAmount;
            }
        }

        return total;
    }

    /**
     * Will apply the amount <code>totalAmount</code> with currency code <code>crnCode</code> among the current set of attendees.
     * 
     * @param totalAmount
     *            the total amount to apportion.
     * @param crnCode
     *            the currency code.
     */
    public void divideAmountAmongAttendees(double totalAmount, String crnCode, List<ExpenseReportAttendee> attendees) {

        double amountToDivide = totalAmount;

        // Walk through all the attendees and select those with 'isAmountEdited' field set to 'false'.
        List<ExpenseReportAttendee> currentAttendees = new ArrayList<ExpenseReportAttendee>();
        if (attendees != null) {
            for (ExpenseReportAttendee attendee : attendees) {
                if (attendee.isAmountEdited != null && attendee.isAmountEdited && attendee.amount != null) {
                    amountToDivide -= attendee.amount;
                } else {
                    currentAttendees.add(attendee);
                }
            }
        }
        if (currentAttendees.size() > 0) {
            // Obtain the number of decimal places for the currency code.
            int numDecimalPlacesToRoundOff = 0;
            try {
                Currency currency = Currency.getInstance(crnCode);
                numDecimalPlacesToRoundOff = currency.getDefaultFractionDigits();
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".divideAmountAmongAttendees: invalid currency code '" + crnCode + "'",
                        ilaExc);
            }
            // The variable name says pennies, but they're not really pennies.
            // It works for all currencies. To understand the algorithm, think pennies.
            double numPenniesPerDollar = Math.pow(10, numDecimalPlacesToRoundOff);

            // Compute the total number of "pennies" to be divided up.
            BigDecimal amountToDivideAsDecimal = BigDecimal.valueOf(amountToDivide).setScale(
                    numDecimalPlacesToRoundOff, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal numPenniesPerDollarAsDecimal = BigDecimal.valueOf(numPenniesPerDollar).setScale(
                    numDecimalPlacesToRoundOff, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal totalNumPenniesAsDecimal = amountToDivideAsDecimal.multiply(numPenniesPerDollarAsDecimal);
            int totalNumPennies = totalNumPenniesAsDecimal.intValue();

            // When we start off, the total number of pennies is the number of pennies
            // that we have left to distribute.
            int penniesLeftToDistribute = totalNumPennies;

            // Now distribute the pennies to each attendee based on the number of instances
            // owned by that attendee.
            int totalAttendeeInstances = countAttendeeInstances(currentAttendees) + noShowCount;
            int numAttendeeInstancesWhoHaveReceivedPennies = 0;
            for (int i = 0; i < currentAttendees.size(); ++i) {
                ExpenseReportAttendee atn = currentAttendees.get(i);

                int numAttendeeInstancesWhoHaveNotYetReceivedPennies = totalAttendeeInstances
                        - numAttendeeInstancesWhoHaveReceivedPennies;
                int penniesForEachAttendeeInstance = penniesLeftToDistribute
                        / numAttendeeInstancesWhoHaveNotYetReceivedPennies;
                int penniesForThisAttendee = penniesForEachAttendeeInstance
                        * ((atn.instanceCount != null) ? atn.instanceCount : 0);

                penniesLeftToDistribute -= penniesForThisAttendee;

                double amountForThisAttendee = ((double) penniesForThisAttendee) / numPenniesPerDollar;

                // Need ported two lines here.
                BigDecimal amountForThisAttendeeAsDecimal = BigDecimal.valueOf(amountForThisAttendee).setScale(
                        numDecimalPlacesToRoundOff);
                atn.amount = amountForThisAttendeeAsDecimal.doubleValue();

                numAttendeeInstancesWhoHaveReceivedPennies += ((atn.instanceCount != null) ? atn.instanceCount : 0);

            }

        }

    }

    /**
     * Counts the total number of attendee instances among all attendees.
     * 
     * @return the total number of attendee instances among all attendees.
     */
    public int countAttendeeInstances(List<ExpenseReportAttendee> attendees) {
        int numInstances = 0;
        if (attendees != null) {
            for (ExpenseReportAttendee attendee : attendees) {
                if (attendee.instanceCount != null) {
                    numInstances += attendee.instanceCount;
                }
            }
        }
        return numInstances;
    }

    /**
     * Determine the amount to be attributed to the 'no show' attendees. This is the total amount, not amount per 'no show'.
     * 
     * @param attendees
     * @return
     */
    public double getNoShowAmount(double transAmt, List<ExpenseReportAttendee> attendees) {
        double amt = 0.0;

        if (noShowCount > 0) {
            // Only add stuff up if we have no shows
            double allocatedAmt = 0.0;
            for (ExpenseReportAttendee attendee : attendees) {
                allocatedAmt += (attendee.amount != null ? attendee.amount : 0.0);
            }
            amt = transAmt - allocatedAmt;
        }

        return amt;
    }

    /**
     * Whether the report entry object can have attendees.
     * 
     * @param expTypes
     *            references a list of expense types to check.
     * @return whether this report entry supports adding attendees.
     */
    public boolean canHaveAttendees(List<ExpenseType> expTypes) {
        boolean retVal = false;
        boolean fieldHidden = false;

        ExpenseReportFormField attendeeField = findFormFieldByFieldId("Attendees");

        if (attendeeField != null && attendeeField.getAccessType() == AccessType.HD) {
            fieldHidden = true;
        }

        if (!fieldHidden && expTypes != null) {
            for (ExpenseType expType : expTypes) {
                if (expType.key != null && expType.key.equalsIgnoreCase(expKey)) {
                    retVal = ((expType.supportsAttendees != null) ? expType.supportsAttendees : false);
                    break;
                }
            }
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".canHaveAttendees: expTypes is null!");
        }
        return retVal;
    }

    /**
     * Gets the list of entry attendees.
     * 
     * @return the list of entry attendees.
     */
    public List<ExpenseReportAttendee> getAttendees() {
        return attendees;
    }

    /**
     * Sets the list of entry attendees.
     * 
     * @param attendees
     *            the list of entry attendees.
     */
    public void setAttendees(List<ExpenseReportAttendee> attendees) {
        this.attendees = attendees;
    }

    /**
     * Finds an instance of <code>ExpenseReportEntry</code> based on a key look-up.
     * 
     * @param expEntRepKey
     *            the expense report entry key.
     * 
     * @return an instance of <code>ExpenseReportEntry</code> if found; <code>null</code> otherwise.
     */
    public ExpenseReportFormField findFormFieldByFieldId(String fieldId) {

        ExpenseReportFormField expRepFrmFld = null;

        if (formFields != null) {
            Iterator<ExpenseReportFormField> frmFldIter = formFields.iterator();
            while (frmFldIter.hasNext()) {
                ExpenseReportFormField frmFld = frmFldIter.next();
                if (frmFld.getId().equalsIgnoreCase(fieldId)) {
                    expRepFrmFld = frmFld;
                    break;
                }
            }
        }
        return expRepFrmFld;
    }

    /**
     * Will parse an XML description of report entry detail object.
     * 
     * @param responseXml
     *            string containing the XML description.
     * 
     * @return returns an instance of <code>ExpenseReportEntryDetail</code>
     */
    public static ExpenseReportEntryDetail parseReportEntryDetailXml(String responseXml) {
        return parseReportEntryDetailXml(new ByteArrayInputStream(responseXml.getBytes()));
    }

    /**
     * Will parse an XML description of report entry detail object.
     * 
     * @param stream
     *            contains a reference to a stream providing report entry detail XML content.
     * 
     * @return returns an instance of <code>ExpenseReportEntryDetail</code>
     */
    public static ExpenseReportEntryDetail parseReportEntryDetailXml(InputStream stream) {

        ExpenseReportEntryDetail entryDetail = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ExpenseReportEntryDetailSAXHandler handler = new ExpenseReportEntryDetailSAXHandler();
            parser.parse(stream, handler);
            ArrayList<ExpenseReportEntry> entries = handler.getReportEntries();
            // This entry point should only ever produce one entry, and it will be a detail entry
            if (entries.size() > 0) {
                entryDetail = (ExpenseReportEntryDetail) entries.get(0);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return entryDetail;
    }

    /**
     * Will parse from <code>reader</code> a report entry detail object of length <code>contentLength</code>.
     * 
     * @param reader
     *            the reader providing the content.
     * @param contentLength
     *            the content length.
     * @return an instance of <code>ExpenseReportEntryDetail</code>.
     */
    public static ExpenseReportEntryDetail parseReportEntryDetailXml(Reader reader, long contentLength) {
        ExpenseReportEntryDetail entryDetail = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ExpenseReportEntryDetailSAXHandler handler = new ExpenseReportEntryDetailSAXHandler();
            handler.buildXML = true;
            int strBufSize = ((contentLength != -1L) ? ((int) contentLength) : (8 * 1024));
            handler.xmlStrBuf = new StringBuilder(strBufSize);
            parser.parse(new InputSource(reader), handler);
            ArrayList<ExpenseReportEntry> entries = handler.getReportEntries();
            // This entry point should only ever produce one entry, and it will be a detail entry
            if (entries.size() > 0) {
                entryDetail = (ExpenseReportEntryDetail) entries.get(0);
                entryDetail.xmlRep = handler.xmlStrBuf.toString();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return entryDetail;
    }

    /**
     * Will parse an XML description of report entry detail object.
     * 
     * @param reader
     *            contains a reference to a reader providing report entry detail XML content.
     * 
     * @return returns an instance of <code>ExpenseReportEntryDetail</code>
     */
    public static ExpenseReportEntryDetail parseReportEntryDetailXml(Reader reader) {

        ExpenseReportEntryDetail entryDetail = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ExpenseReportEntryDetailSAXHandler handler = new ExpenseReportEntryDetailSAXHandler();
            parser.parse(new InputSource(reader), handler);
            ArrayList<ExpenseReportEntry> entries = handler.getReportEntries();
            // This entry point should only ever produce one entry, and it will be a detail entry
            if (entries.size() > 0) {
                entryDetail = (ExpenseReportEntryDetail) entries.get(0);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return entryDetail;
    }

    /**
     * Provides an extension of <code>DefaultHandler</code> to support parsing expense report entry information.
     * 
     * @author AndrewK
     */
    public static class ExpenseReportEntryDetailSAXHandler extends ExpenseReportEntrySAXHandler {

        private static final String CLS_TAG = ExpenseReportEntryDetail.CLS_TAG + "."
                + ExpenseReportEntryDetailSAXHandler.class.getSimpleName();

        private static final String COMMENTS = "Comments";
        private static final String EXCEPTIONS = "Exceptions";
        private static final String FIELDS = "Fields";
        private static final String ITEMIZATIONS = "Itemizations";
        private static final String ATTENDEES = "Attendees";
        private static final String TAX_FORMS = "TaxForms";

        /**
         * Contains whether the handler will build up the XML based on the SAX events.
         */
        private boolean buildXML;

        /**
         * Contains the string buffer used to contain the built up XML.
         */
        private StringBuilder xmlStrBuf;

        /**
         * Contains whether or not report entry comments are currently being parsed.
         */
        private boolean parsingReportEntryComments;

        /**
         * Contains whether or not report entry exceptions are currently being parsed.
         */
        private boolean parsingReportEntryExceptions;

        /**
         * Contains whether or not report entry form fields are currently being parsed.
         */
        private boolean parsingReportEntryFields;

        /**
         * Contains whether or not tax forms are currently being parsed.
         */
        private boolean parsingTaxForms;

        /**
         * Contains whether or not report entry itemizations are currently being parsed.
         */
        private boolean parsingReportEntryItemizations;

        /**
         * Contains whether or not report entry attendees are currently being parsed.
         */
        private boolean parsingReportEntryAttendees;

        /**
         * Contains a reference to a SAX handler for parsing report entry comments.
         */
        private ExpenseReportComment.ExpenseReportCommentSAXHandler entryCommentHandler;

        /**
         * Contains a reference to a SAX handler for parsing report entry exceptions.
         */
        private ExpenseReportException.ExpenseReportExceptionSAXHandler entryExceptionHandler;

        /**
         * Contains a reference to a SAX handler for parsing report entry fields.
         */
        private ExpenseReportFormField.ExpenseReportFormFieldSAXHandler entryFieldHandler;

        /**
         * Contains a reference to a SAX handler for parsing report entry itemizations.
         */
        private ExpenseReportEntryItemization.ExpenseReportEntryItemizationSAXHandler entryItemizationHandler;

        /**
         * Contains a reference to a SAX handler for parsing report entry attendees.
         */
        private ExpenseReportAttendee.ExpenseReportAttendeeSAXHandler entryAttendeeHandler;

        /**
         * Contains a reference to a SAX handler for parsing tax forms
         */
        private GetTaxFormReply.TaxFormsSAXHandler taxFormHandler;

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.data.expense.ExpenseReportEntry.ExpenseReportEntrySAXHandler#createReportEntry()
         */
        @Override
        protected ExpenseReportEntry createReportEntry() {
            return new ExpenseReportEntryDetail();
        }

        @Override
        public ArrayList<ExpenseReportEntry> getReportEntries() {
            ArrayList<ExpenseReportEntry> entries = super.getReportEntries();

            // We need to shift the distance field on mileage entries to be just after the
            // amount field.
            final int size = entries.size();
            for (int i = 0; i < size; i++) {
                // TODO This needs to look at expense code, not key. However, there is no guarantee we
                // have the expense type data at this point. Resolve later.
                ExpenseReportEntryDetail entry = (ExpenseReportEntryDetail) entries.get(i);
                if (Const.EXPENSE_TYPE_MILEAGE.equals(entry.expKey)) {

                    // The BusinessDistance field is just tacked on to the end of the form but that
                    // is usually a poor place for it.
                    // Find it and move it to just after TransactionAmount
                    List<ExpenseReportFormField> fields = entry.formFields;
                    ExpenseReportFormField dstField = null;
                    for (int j = fields.size() - 1; j > 0; j--) {
                        ExpenseReportFormField field = fields.get(j);

                        if (field.getId().equals("BusinessDistance")) {
                            // Hold onto it and remove it from the list
                            dstField = field;
                            fields.remove(j);
                        }

                        if (field.getId().equals("TransactionAmount") && dstField != null) {
                            fields.add(j + 1, dstField);
                        }
                    }

                }
            }

            return entries;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {

            if (buildXML) {
                for (int chInd = start; chInd < (start + length); ++chInd) {
                    FormatUtil.escapeForXML(xmlStrBuf, ch[chInd]);
                }
            }

            if (parsingReportEntryComments) {
                if (entryCommentHandler != null) {
                    entryCommentHandler.characters(ch, start, length);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".characters: null entry comment handler!");
                }
            } else if (parsingReportEntryExceptions) {
                if (entryExceptionHandler != null) {
                    entryExceptionHandler.characters(ch, start, length);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".characters: null entry exception handler!");
                }
            } else if (parsingReportEntryFields) {
                if (entryFieldHandler != null) {
                    entryFieldHandler.characters(ch, start, length);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".characters: null entry field handler!");
                }
            } else if (parsingReportEntryItemizations) {
                if (entryItemizationHandler != null) {
                    entryItemizationHandler.characters(ch, start, length);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".characters: null entry itemization handler!");
                }
            } else if (parsingReportEntryAttendees) {
                if (entryAttendeeHandler != null) {
                    entryAttendeeHandler.characters(ch, start, length);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".characters: null entry attendee handler!");
                }
            } else if (parsingTaxForms) {
                if (taxFormHandler != null) {
                    taxFormHandler.characters(ch, start, length);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".characters: null tax form handler!");
                }
            } else {
                super.characters(ch, start, length);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if (buildXML) {
                xmlStrBuf.append('<');
                xmlStrBuf.append(localName);
                xmlStrBuf.append('>');
            }

            if (parsingReportEntryComments) {
                if (entryCommentHandler != null) {
                    entryCommentHandler.startElement(uri, localName, qName, attributes);
                    elementHandled = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: null entry comment handler!");
                }
            } else if (parsingReportEntryExceptions) {
                if (entryExceptionHandler != null) {
                    entryExceptionHandler.startElement(uri, localName, qName, attributes);
                    elementHandled = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: null entry exception handler!");
                }
            } else if (parsingReportEntryFields) {
                if (entryFieldHandler != null) {
                    entryFieldHandler.startElement(uri, localName, qName, attributes);
                    elementHandled = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: null entry field handler!");
                }
            } else if (parsingReportEntryItemizations) {
                if (entryItemizationHandler != null) {
                    entryItemizationHandler.startElement(uri, localName, qName, attributes);
                    elementHandled = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: null entry itemization handler!");
                }
            } else if (parsingReportEntryAttendees) {
                if (entryAttendeeHandler != null) {
                    entryAttendeeHandler.startElement(uri, localName, qName, attributes);
                    elementHandled = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: null entry itemization handler!");
                }
            } else if (parsingTaxForms) {
                if (taxFormHandler != null) {
                    taxFormHandler.startElement(uri, localName, qName, attributes);
                    elementHandled = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: null entry tax form handler!");
                }
            } else {
                elementHandled = false;
                super.startElement(uri, localName, qName, attributes);
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(REPORT_ENTRY_DETAIL)) {
                        reportEntry = createReportEntry();
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(COMMENTS)) {
                        parsingReportEntryComments = true;
                        entryCommentHandler = new ExpenseReportComment.ExpenseReportCommentSAXHandler();
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(EXCEPTIONS)) {
                        parsingReportEntryExceptions = true;
                        entryExceptionHandler = new ExpenseReportException.ExpenseReportExceptionSAXHandler();
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(FIELDS) && !parsingTaxForms) {
                        parsingReportEntryFields = true;
                        entryFieldHandler = new ExpenseReportFormField.ExpenseReportFormFieldSAXHandler();
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(ITEMIZATIONS)) {
                        parsingReportEntryItemizations = true;
                        entryItemizationHandler = new ExpenseReportEntryItemization.ExpenseReportEntryItemizationSAXHandler();
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(ATTENDEES)) {
                        parsingReportEntryAttendees = true;
                        entryAttendeeHandler = new ExpenseReportAttendee.ExpenseReportAttendeeSAXHandler();
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(TAX_FORMS)) {
                        parsingTaxForms = true;
                        taxFormHandler = new GetTaxFormReply.TaxFormsSAXHandler();
                        elementHandled = true;
                    }
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            if (buildXML) {
                xmlStrBuf.append("</");
                xmlStrBuf.append(localName);
                xmlStrBuf.append('>');
            }

            if (reportEntry != null) {
                if (parsingReportEntryComments) {
                    if (entryCommentHandler != null) {
                        if (localName.equalsIgnoreCase(COMMENTS)) {
                            ((ExpenseReportEntryDetail) reportEntry).comments = entryCommentHandler.getReportComments();
                            parsingReportEntryComments = false;
                            entryCommentHandler = null;
                        } else {
                            entryCommentHandler.endElement(uri, localName, qName);
                        }
                        elementHandled = true;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report entry comment handler!");
                    }
                } else if (parsingReportEntryExceptions) {
                    if (entryExceptionHandler != null) {
                        if (localName.equalsIgnoreCase(EXCEPTIONS)) {
                            ((ExpenseReportEntryDetail) reportEntry).exceptions = entryExceptionHandler
                                    .getReportExceptions();
                            parsingReportEntryExceptions = false;
                            entryExceptionHandler = null;
                        } else {
                            entryExceptionHandler.endElement(uri, localName, qName);
                        }
                        elementHandled = true;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report entry exception handler!");
                    }
                } else if (parsingReportEntryFields) {
                    if (entryFieldHandler != null) {
                        if (localName.equalsIgnoreCase(FIELDS)) {
                            ((ExpenseReportEntryDetail) reportEntry).formFields = entryFieldHandler
                                    .getReportFormFields();
                            parsingReportEntryFields = false;
                            entryFieldHandler = null;
                        } else {
                            entryFieldHandler.endElement(uri, localName, qName);
                        }
                        elementHandled = true;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report entry field handler!");
                    }
                } else if (parsingReportEntryItemizations) {
                    if (entryItemizationHandler != null) {
                        if (localName.equalsIgnoreCase(ITEMIZATIONS)) {
                            ((ExpenseReportEntryDetail) reportEntry).itemizations = entryItemizationHandler
                                    .getReportEntries();
                            parsingReportEntryItemizations = false;
                            entryItemizationHandler = null;
                        } else {
                            entryItemizationHandler.endElement(uri, localName, qName);
                        }
                        elementHandled = true;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report entry itemization handler!");
                    }
                } else if (parsingReportEntryAttendees) {
                    if (entryAttendeeHandler != null) {
                        if (localName.equalsIgnoreCase(ATTENDEES)) {
                            ((ExpenseReportEntryDetail) reportEntry).attendees = entryAttendeeHandler
                                    .getReportAttendees();
                            parsingReportEntryAttendees = false;
                            entryAttendeeHandler = null;
                        } else {
                            entryAttendeeHandler.endElement(uri, localName, qName);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report entry attendee handler!");
                    }
                } else if (parsingTaxForms) {
                    if (taxFormHandler != null) {
                        if (localName.equalsIgnoreCase(TAX_FORMS)) {
                            if (taxFormHandler.getReply() != null) {
                                ((ExpenseReportEntryDetail) reportEntry).taxForms = taxFormHandler.getReply().listOfTaxForm;
                            } else {
                                ((ExpenseReportEntryDetail) reportEntry).taxForms = null;
                            }
                            parsingTaxForms = false;
                            taxFormHandler = null;
                        } else {
                            taxFormHandler.endElement(uri, localName, qName);
                        }
                        elementHandled = true;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report entry field handler!");
                    }
                } else {
                    elementHandled = false;
                    super.endElement(uri, localName, qName);
                    if (!elementHandled) {
                        if (localName.equalsIgnoreCase(REPORT_ENTRY_DETAIL)) {
                            reportEntries.add(reportEntry);
                            reportEntry = null;
                            elementHandled = true;
                        } else if (this.getClass().equals(ExpenseReportEntryDetailSAXHandler.class)) {
                            Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element '" + localName + "'.");
                            // Clear out any collected characters if this class is the last stop.
                            chars.setLength(0);
                        }
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null report detail!");
                chars.setLength(0);
            }
            // Only clear if it was handled.
            if (elementHandled) {
                chars.setLength(0);
            }
        }

        /**
         * Will serialize to XML the attributes of a report entry detail object.
         * 
         * @param strBldr
         *            the string builder to append XML serialized data.
         * @param entry
         *            the expense report detail object.
         */
        public static void serializeToXML(StringBuilder strBldr, ExpenseReportEntry entry) {
            if (strBldr != null) {
                if (entry != null) {
                    // ApprovedAmount
                    ViewUtil.addXmlElement(strBldr, APPROVED_AMOUNT, entry.approvedAmount);
                    // EreceiptId
                    ViewUtil.addXmlElement(strBldr, E_RECEIPT_ID, entry.eReceiptId);
                    // ExpKey
                    ViewUtil.addXmlElement(strBldr, EXPENSE_KEY, entry.expKey);
                    // ExpName
                    ViewUtil.addXmlElement(strBldr, EXPENSE_NAME, entry.expenseName);
                    // FormKey
                    ViewUtil.addXmlElement(strBldr, FORM_KEY, entry.formKey);
                    // HasAllocation
                    ViewUtil.addXmlElementYN(strBldr, HAS_ALLOCATION, entry.hasAllocation);
                    // HasAttendees
                    ViewUtil.addXmlElementYN(strBldr, HAS_ATTENDEES, entry.hasAttendees);
                    // HasComments
                    ViewUtil.addXmlElementYN(strBldr, HAS_COMMENTS, entry.hasComments);
                    // HasExceptions
                    ViewUtil.addXmlElementYN(strBldr, HAS_EXCEPTIONS, entry.hasExceptions);
                    // HasMobileReceipt
                    ViewUtil.addXmlElementYN(strBldr, HAS_MOBILE_RECEIPT, entry.hasMobileReceipt);
                    // ImageRequired
                    ViewUtil.addXmlElementYN(strBldr, IMAGE_REQUIRED, entry.imageRequired);
                    // IsCreditCardCharge
                    ViewUtil.addXmlElementYN(strBldr, IS_CREDIT_CARD_CHARGE, entry.isCreditCardCharge);
                    // IsItemized
                    ViewUtil.addXmlElementYN(strBldr, IS_ITEMIZED, entry.isItemized);
                    // IsPersonal
                    ViewUtil.addXmlElementYN(strBldr, IS_PERSONAL, entry.isPersonal);
                    // IsPersonalCardCharge
                    ViewUtil.addXmlElementYN(strBldr, IS_PERSONAL_CARD_CHARGE, entry.personalCardCharge);
                    // LocationName
                    ViewUtil.addXmlElement(strBldr, LOCATION_NAME, entry.locationName);
                    // MeKey
                    ViewUtil.addXmlElement(strBldr, ME_KEY, entry.meKey);
                    // ParentRpeKey
                    ViewUtil.addXmlElement(strBldr, PARENT_REPORT_ENTRY_KEY, entry.parentReportEntryKey);
                    // ReceiptImageId
                    ViewUtil.addXmlElement(strBldr, RECEIPT_IMAGE_ID, entry.receiptImageId);
                    // ReceiptRequired
                    ViewUtil.addXmlElementYN(strBldr, RECEIPT_REQUIRED, entry.receiptRequired);
                    // RpeKey
                    ViewUtil.addXmlElement(strBldr, REPORT_ENTRY_KEY, entry.reportEntryKey);
                    // RptKey
                    ViewUtil.addXmlElement(strBldr, REPORT_KEY, entry.rptKey);
                    // SeverityLevel
                    ViewUtil.addXmlElement(strBldr, SEVERITY_LEVEL, entry.severityLevel);
                    // TransactionAmount
                    ViewUtil.addXmlElement(strBldr, TRANSACTION_AMOUNT, entry.transactionAmount);
                    // TransactionCrnCode
                    ViewUtil.addXmlElement(strBldr, TRANSACTION_CRN_CODE, entry.transactionCrnCode);
                    // TransactionDate
                    ViewUtil.addXmlElement(strBldr, TRANSACTION_DATE, entry.transactionDate);
                    // VendorDescription
                    ViewUtil.addXmlElement(strBldr, VENDOR_DESCRIPTION, entry.vendorDescription);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeToXML: entry is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeToXML: strBldr is null!");
            }
        }

        /**
         * Will serialize to XML the report entry level form fields
         * 
         * @param strBldr
         *            the <code>StringBuilder</code> instance.
         * @param frmFlds
         *            the list of form fields.
         */
        public static void serializeFormFieldsToXML(StringBuilder strBldr, List<ExpenseReportFormField> frmFlds) {
            if (strBldr != null) {
                if (frmFlds != null) {
                    strBldr.append('<');
                    strBldr.append(FIELDS);
                    strBldr.append(" xmlns:f='http://schemas.datacontract.org/2004/07/Snowbird'");
                    strBldr.append('>');
                    for (ExpenseReportFormField frmFld : frmFlds) {
                        ExpenseReportFormField.ExpenseReportFormFieldSAXHandler.serializeToXML(strBldr, frmFld);
                    }
                    strBldr.append("</");
                    strBldr.append(FIELDS);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeFormFieldsToXML: frmFlds is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeFormFieldsToXML: strBldr is null!");
            }
        }

        /**
         * Will serialize to XML the report entry level tax form fields
         * 
         * @param strBldr
         *            the <code>StringBuilder</code> instance.
         * @param taxFormList
         *            the list of tax form .
         */
        public static void serializeTaxFormFieldsToXML(StringBuilder strBldr, List<TaxForm> taxFormList) {
            if (strBldr != null) {
                if (taxFormList != null) {
                    strBldr.append('<');
                    strBldr.append(TAX_FORMS);
                    strBldr.append('>');
                    for (TaxForm taxForm : taxFormList) {
                        GetTaxFormReply.TaxFormsSAXHandler.serializeTaxFormItemToXML(strBldr, taxForm);
                    }

                    strBldr.append("</");
                    strBldr.append(TAX_FORMS);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeTaxFormFieldsToXML: taxFormList is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeTaxFormFieldsToXML: strBldr is null!");
            }
        }

        /**
         * Will serialize to XML the report entry level attendee information.
         * 
         * @param strBldr
         *            the <code>StringBuilder</code> instance.
         * @param attendees
         *            the list of attendees.
         */
        public static void serializeAttendeesToXML(StringBuilder strBldr, List<ExpenseReportAttendee> attendees) {
            if (strBldr != null) {
                if (attendees != null) {
                    strBldr.append('<');
                    strBldr.append(ATTENDEES);
                    strBldr.append('>');
                    for (ExpenseReportAttendee attendee : attendees) {
                        ExpenseReportAttendee.ExpenseReportAttendeeSAXHandler.serializeToXML(strBldr, attendee);
                    }
                    strBldr.append("</");
                    strBldr.append(ATTENDEES);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeAttendeesToXML: attendees is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeAttendeesToXML: strBldr is null!");
            }
        }

        /**
         * Will serialize a complete <code>ExpenseReportEntryDetail</code> object to XML.
         * 
         * @param strBldr
         *            the string builder to hold the serialized object.
         * @param entry
         *            the expense report entry detail object to serialize.
         */
        public static void serializeAllToXML(StringBuilder strBldr, ExpenseReportEntry entry) {
            if (strBldr != null) {
                if (entry != null) {
                    ExpenseReportEntryDetail entryDetail = null;
                    if (entry instanceof ExpenseReportEntryDetail) {
                        entryDetail = (ExpenseReportEntryDetail) entry;
                    }
                    strBldr.append('<');
                    if (entryDetail != null) {
                        strBldr.append(REPORT_ENTRY_DETAIL);
                    } else {
                        strBldr.append(REPORT_ENTRY);
                    }
                    strBldr.append('>');
                    // Serialize the top-level attributes.
                    serializeToXML(strBldr, entry);
                    if (entryDetail != null) {
                        // Fields
                        if (entryDetail.getFormFields() != null) {
                            serializeFormFieldsToXML(strBldr, entryDetail.getFormFields());
                        }
                        // Attendees
                        if (entryDetail.getAttendees() != null) {
                            serializeAttendeesToXML(strBldr, entryDetail.getAttendees());
                        }
                        // Comments
                        if (entryDetail.getComments() != null) {
                            serializeCommentsToXML(strBldr, entryDetail.getComments());
                        }
                        // Exceptions
                        if (entryDetail.getExceptions() != null) {
                            serializeExceptionsToXML(strBldr, entryDetail.getExceptions());
                        }
                        // Itemizations
                        if (entryDetail.getItemizations() != null) {
                            serializeItemizationsToXML(strBldr, entryDetail.getItemizations());
                        }
                        // tax forms
                        if (entryDetail.getFormFields() != null) {
                            serializeTaxFormFieldsToXML(strBldr, entryDetail.getTaxForm());
                        }
                    }
                    strBldr.append("</");
                    if (entryDetail != null) {
                        strBldr.append(REPORT_ENTRY_DETAIL);
                    } else {
                        strBldr.append(REPORT_ENTRY);
                    }
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeAllToXML: expRepEntDet is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeAllToXML: strBldr is null!");
            }
        }

        public static void serializeItemizationsToXML(StringBuilder strBldr, List<ExpenseReportEntry> itemizations) {
            if (strBldr != null) {
                if (itemizations != null) {
                    strBldr.append('<');
                    strBldr.append(ITEMIZATIONS);
                    strBldr.append('>');
                    for (ExpenseReportEntry expRepEnt : itemizations) {
                        ExpenseReportEntryItemization.ExpenseReportEntryItemizationSAXHandler
                                .serializeItemizationAllToXML(strBldr, (ExpenseReportEntryItemization) expRepEnt);
                    }
                    strBldr.append("</");
                    strBldr.append(ITEMIZATIONS);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeItemizationsToXML: itemizations is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeItemizationsToXML: strBldr is null!");
            }
        }

        /**
         * Will serialize a list of expenese report comments to XML.
         * 
         * @param strBldr
         *            the string builder holding the serialized XML.
         * @param comments
         *            the list of comments.
         */
        public static void serializeCommentsToXML(StringBuilder strBldr, List<ExpenseReportComment> comments) {
            if (strBldr != null) {
                if (comments != null) {
                    strBldr.append('<');
                    strBldr.append(COMMENTS);
                    strBldr.append('>');
                    for (ExpenseReportComment comment : comments) {
                        ExpenseReportComment.ExpenseReportCommentSAXHandler.serializeToXML(strBldr, comment);
                    }
                    strBldr.append("</");
                    strBldr.append(COMMENTS);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeCommentsToXML: comments is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeCommentsToXML: strBldr is null!");
            }
        }

        /**
         * Will serialize a list of expense report exceptions to XML.
         * 
         * @param strBldr
         *            the string builder holding the serialized XML.
         * @param comments
         *            the list of exceptions.
         */
        public static void serializeExceptionsToXML(StringBuilder strBldr, List<ExpenseReportException> exceptions) {
            if (strBldr != null) {
                if (exceptions != null) {
                    strBldr.append('<');
                    strBldr.append(EXCEPTIONS);
                    strBldr.append('>');
                    for (ExpenseReportException exception : exceptions) {
                        ExpenseReportException.ExpenseReportExceptionSAXHandler.serializeToXML(strBldr, exception);
                    }
                    strBldr.append("</");
                    strBldr.append(EXCEPTIONS);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeExceptionsToXML: comments is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeExceptionsToXML: strBldr is null!");
            }
        }

    }

}
