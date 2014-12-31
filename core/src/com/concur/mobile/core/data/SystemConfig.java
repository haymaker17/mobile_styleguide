/**
 * 
 */
package com.concur.mobile.core.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.data.ExpenseType.ExpenseTypeSAXHandler;
import com.concur.mobile.core.travel.data.CompanyLocation;
import com.concur.mobile.core.travel.data.ReasonCode;
import com.concur.mobile.core.travel.data.RefundableInfo;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides system configuration information related to an end-users company.
 * 
 * @author AndrewK
 */
public class SystemConfig {

    private static final String CLS_TAG = SystemConfig.class.getSimpleName();

    /**
     * Contains a list of company office locations.
     */
    private ArrayList<CompanyLocation> companyLocations;

    /**
     * Contains a list of company car reasons.
     */
    private ArrayList<ReasonCode> carReasons;

    /**
     * Contains a list of company hotel reasons.
     */
    private ArrayList<ReasonCode> hotelReasons;

    /**
     * Contains a list of company air reasons.
     */
    private ArrayList<ReasonCode> airReasons;

    /**
     * Contains a list of expense types.
     */
    private ArrayList<ExpenseType> expenseTypes;

    /**
     * Contains a reference to refundable information.
     */
    private RefundableInfo refundableInfo;

    /**
     * Contains whether a violation justification is required when booking a fare outside of company policy.
     */
    private Boolean ruleViolationExplanationRequired;

    /**
     * Contains the server computed hash code for the information contained in this system configuration object.
     */
    private String hash;

    /**
     * Contains the response string, either one of 'UPDATED' or 'NO_CHANGE' depending upon changes occurring on the server.
     */
    private String responseId;

    /**
     * Gets the list of <code>CompanyLocation</code> objects specific to the end user's company.
     * 
     * @return the list of <code>CompanyLocation</code> objects specific to the end user's company.
     */
    public ArrayList<CompanyLocation> getCompanyLocations() {
        return companyLocations;
    }

    /**
     * Gets the list of <code>ReasonCode</code> objects specific to the end user's car rental company information..
     * 
     * @return the list of <code>CarReason</code> objects specific to the end user's company.
     */
    public ArrayList<ReasonCode> getCarReasons() {
        return carReasons;
    }

    /**
     * Gets the list of <code>ReasonCode</code> objects specific to the end user's hotel rental company information.
     * 
     * @return the list of <code>ReasonCode</code> objects specific to the end user's company.
     */
    public ArrayList<ReasonCode> getHotelReasons() {
        return hotelReasons;
    }

    /**
     * Gets the list of <code>ReasonCode</code> objects specific to the end user's air booking company information.
     * 
     * @return the list of <code>ReasonCode</code> objects specific to the end user's company.
     */
    public ArrayList<ReasonCode> getAirReasons() {
        return airReasons;
    }

    /**
     * Gets the list of <code>ExpenseType</code> objects specific to the end user's company information.
     * 
     * @return the list of <code>ExpenseType</code> objects specific to the end user's company.
     */
    public ArrayList<ExpenseType> getExpenseTypes() {
        return expenseTypes;
    }

    /**
     * Gets the response ID from the last call to query the system configuration information for the current end-user.
     * 
     * @return the response ID from the last call to query the system configuration information for the current end-user. Contains
     *         either 'NO_CHANGE' or 'UPDATED' to reflect system configuration information that has not changed or has been
     *         updated; respectively.
     */
    public String getResponseId() {
        return responseId;
    }

    /**
     * Gets the server computed hash code representing the information contained in this system configuration object.
     * 
     * @return the server computed hash code representing the information contained in this system configuration object.
     */
    public String getHash() {
        return hash;
    }

    /**
     * Gets the refundable info.
     * 
     * @return returns the refundable info.
     */
    public RefundableInfo getRefundableInfo() {
        return refundableInfo;
    }

    /**
     * Gets whether or not a violation justification is required when booking a fare outside of company policy.
     * 
     * @return returns whether a violation justification is required when booking a fare outside of company policy.
     */
    public Boolean getRuleViolationExplanationRequired() {
        return ruleViolationExplanationRequired;
    }

    /**
     * Will parse the XML representation of system configuration information specific to a company.
     * 
     * @param systemConfigXml
     *            the XML representation.
     * 
     * @return An instance of <code>SystemConfig</code> containing the information parsed from <code>systemConfigXML</code>
     */
    public static SystemConfig parseSystemConfigXml(String systemConfigXml) throws IOException {
        SystemConfig systemConfig = null;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            SystemConfigSAXHandler handler = new SystemConfigSAXHandler();
            parser.parse(new ByteArrayInputStream(systemConfigXml.getBytes()), handler);
            systemConfig = handler.getSystemConfig();
        } catch (ParserConfigurationException parsConfExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseSystemConfigXML: parser exception.", parsConfExc);
            throw new IOException(parsConfExc.getMessage());
        } catch (SAXException saxExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseSystemConfigXML: sax parsing exception.", saxExc);
            throw new IOException(saxExc.getMessage());
        }
        return systemConfig;
    }

    /**
     * An extension of <code>DefaultHandler</code> used to parse system configuration information.
     * 
     * @author AndrewK
     */
    private static class SystemConfigSAXHandler extends DefaultHandler {

        private static String CLS_TAG = SystemConfig.CLS_TAG + "." + SystemConfigSAXHandler.class.getSimpleName();

        private static final String SYSTEM_CONFIG = "SystemConfig";
        private static final String OFFICES = "Offices";
        private static final String OFFICE_CHOICE = "OfficeChoice";
        private static final String CAR_REASONS = "CarReasons";
        private static final String HOTEL_REASONS = "HotelReasons";
        private static final String AIR_REASONS = "AirReasons";
        private static final String REASON_CODE = "ReasonCode";
        private static final String INFO = "Info";
        private static final String HASH = "Hash";
        private static final String RESPONSE_ID = "ResponseId";
        private static final String EXPENSE_TYPES = "ExpenseTypes";
        private static final String REFUNDABLE_INFO = "RefundableInfo";
        private static final String RULE_VIOLATION_EXPLANATION_REQUIRED = "RuleViolationExplanationRequired";

        // A reference to the system config object being built.
        private SystemConfig systemConfig = null;

        // The reason code currently being parsed.
        private ReasonCode reasonCode = null;

        // The company location currently being parsed.
        private CompanyLocation companyLocation = null;

        private StringBuilder chars = null;

        // Parsing company locations?
        private boolean parsingCompanyLocations;
        // Parsing car reason codes?
        private boolean parsingCarReasons;
        // Parsing hotel reason codes?
        private boolean parsingHotelReasons;
        // Parsing air reasons?
        private boolean parsingAirReasons;
        // Parsing info.
        private boolean parsingInfo;
        // Parsing refundable info.
        private boolean parsingRefundableInfo;

        // A reference to the expense type handler.
        private ExpenseTypeSAXHandler expenseTypeHandler;

        /**
         * Gets the parsed system config object.
         * 
         * @return the system config object.
         */
        SystemConfig getSystemConfig() {
            return systemConfig;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startDocument()
         */
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            chars = new StringBuilder();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            if (expenseTypeHandler != null) {
                expenseTypeHandler.characters(ch, start, length);
            } else {
                chars.append(ch, start, length);
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
            super.startElement(uri, localName, qName, attributes);
            if (localName.equalsIgnoreCase(SYSTEM_CONFIG)) {
                systemConfig = new SystemConfig();
            } else if (localName.equalsIgnoreCase(OFFICES)) {
                if (systemConfig != null) {
                    systemConfig.companyLocations = new ArrayList<CompanyLocation>();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: system config is null!");
                }
            } else if (localName.equalsIgnoreCase(OFFICE_CHOICE)) {
                if (systemConfig != null) {
                    companyLocation = new CompanyLocation();
                    parsingCompanyLocations = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: system config is null!");
                }
            } else if (localName.equalsIgnoreCase(CAR_REASONS)) {
                if (systemConfig != null) {
                    systemConfig.carReasons = new ArrayList<ReasonCode>();
                    parsingCarReasons = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: system config is null!");
                }
            } else if (localName.equalsIgnoreCase(AIR_REASONS)) {
                if (systemConfig != null) {
                    systemConfig.airReasons = new ArrayList<ReasonCode>();
                    parsingAirReasons = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: system config is null!");
                }
            } else if (localName.equalsIgnoreCase(REASON_CODE)) {
                if (systemConfig != null) {
                    if (parsingCarReasons) {
                        if (systemConfig.carReasons != null) {
                            reasonCode = new ReasonCode();
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: null car reasons list!");
                        }
                    } else if (parsingHotelReasons) {
                        if (systemConfig.hotelReasons != null) {
                            reasonCode = new ReasonCode();
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: null hotel reasons list!");
                        }
                    } else if (parsingAirReasons) {
                        if (systemConfig.airReasons != null) {
                            reasonCode = new ReasonCode();
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: null air reasons list!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: reason code tag outside of '" + CAR_REASONS
                                + "', '" + HOTEL_REASONS + "', '" + AIR_REASONS + "' tags!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: system config is null!");
                }
            } else if (localName.equalsIgnoreCase(HOTEL_REASONS)) {
                if (systemConfig != null) {
                    systemConfig.hotelReasons = new ArrayList<ReasonCode>();
                    parsingHotelReasons = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: system config is null!");
                }
            } else if (localName.equalsIgnoreCase(INFO)) {
                if (systemConfig != null) {
                    parsingInfo = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: system config is null!");
                }
            } else if (localName.equalsIgnoreCase(REFUNDABLE_INFO)) {
                if (systemConfig != null) {
                    systemConfig.refundableInfo = new RefundableInfo();
                    parsingRefundableInfo = true;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: system config is null!");
                }
            } else if (localName.equalsIgnoreCase(EXPENSE_TYPES)) {
                if (systemConfig != null) {
                    expenseTypeHandler = new ExpenseType.ExpenseTypeSAXHandler();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: system config is null!");
                }
            } else if (expenseTypeHandler != null) {
                if (systemConfig != null) {
                    expenseTypeHandler.startElement(uri, localName, qName, attributes);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: system config is null!");
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
            super.endElement(uri, localName, qName);

            if (systemConfig != null) {
                if (localName.equalsIgnoreCase(REASON_CODE)) {
                    if (parsingCarReasons) {
                        if (systemConfig.carReasons != null) {
                            systemConfig.carReasons.add(reasonCode);
                            reasonCode = null;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: system config car reasons list is null!");
                        }
                    } else if (parsingHotelReasons) {
                        if (systemConfig.hotelReasons != null) {
                            systemConfig.hotelReasons.add(reasonCode);
                            reasonCode = null;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: system config hotel reasons list is null!");
                        }
                    } else if (parsingAirReasons) {
                        if (systemConfig.airReasons != null) {
                            systemConfig.airReasons.add(reasonCode);
                            reasonCode = null;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: system config air reasons list is null!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: found '" + REASON_CODE
                                + "' tag outside of parsing car/hotel reasons!");
                    }
                } else if (localName.equalsIgnoreCase(OFFICE_CHOICE)) {
                    if (systemConfig.companyLocations != null) {
                        systemConfig.companyLocations.add(companyLocation);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: system config company location list is null!");
                    }
                } else if (localName.equalsIgnoreCase(OFFICES)) {
                    parsingCompanyLocations = false;
                } else if (localName.equalsIgnoreCase(CAR_REASONS)) {
                    parsingCarReasons = false;
                } else if (localName.equalsIgnoreCase(HOTEL_REASONS)) {
                    parsingHotelReasons = false;
                } else if (localName.equalsIgnoreCase(AIR_REASONS)) {
                    parsingAirReasons = false;
                } else if (localName.equalsIgnoreCase(INFO)) {
                    parsingInfo = false;
                } else if (localName.equalsIgnoreCase(RULE_VIOLATION_EXPLANATION_REQUIRED)) {
                    systemConfig.ruleViolationExplanationRequired = Parse.safeParseBoolean(chars.toString().trim());
                } else if (localName.equalsIgnoreCase(REFUNDABLE_INFO)) {
                    parsingRefundableInfo = false;
                } else if (parsingCompanyLocations) {
                    if (companyLocation != null) {
                        companyLocation.handleElement(localName, chars.toString().trim());
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: company location is null!");
                    }
                } else if (parsingCarReasons || parsingHotelReasons || parsingAirReasons) {
                    if (reasonCode != null) {
                        reasonCode.handleElement(localName, chars.toString().trim());
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reason code is null!");
                    }
                } else if (parsingInfo) {
                    if (localName.equalsIgnoreCase(RESPONSE_ID)) {
                        systemConfig.responseId = chars.toString().trim();
                    } else if (localName.equalsIgnoreCase(HASH)) {
                        systemConfig.hash = chars.toString().trim();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unrecognized INFO XML tag '" + localName + "'.");
                    }
                } else if (parsingRefundableInfo) {
                    if (systemConfig.refundableInfo != null) {
                        systemConfig.refundableInfo.handleElement(localName, chars.toString().trim());
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: refundableInfo is null!");
                    }
                } else if (expenseTypeHandler != null) {
                    if (localName.equalsIgnoreCase(EXPENSE_TYPES)) {
                        expenseTypeHandler.postProcessList();
                        systemConfig.expenseTypes = expenseTypeHandler.getExpenseTypes();
                        expenseTypeHandler.cleanUp();
                        expenseTypeHandler = null;
                    } else {
                        expenseTypeHandler.endElement(uri, localName, qName);
                    }
                } else if (localName.equalsIgnoreCase(SYSTEM_CONFIG)) {
                    // No-op.
                } else {
                    // Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML tag '" + localName + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null system config!");
            }
            chars.setLength(0);
        }

    }

}
