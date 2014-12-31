/**
 * Extension of ConcurServie to define client server interface methods.
 * 
 * @author sunill
 * 
 * */
package com.concur.mobile.gov.service;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ConcurServiceHandler;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.travel.air.service.AirFilterRequest;
import com.concur.mobile.core.travel.air.service.AirSellRequest;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.expense.charge.data.MobileExpense;
import com.concur.mobile.gov.expense.charge.service.AddToVchRequest;
import com.concur.mobile.gov.expense.charge.service.DeleteTMUnappliedExpenseRequest;
import com.concur.mobile.gov.expense.charge.service.MobileExpenseListRequest;
import com.concur.mobile.gov.expense.doc.data.GovExpenseForm;
import com.concur.mobile.gov.expense.doc.data.ReasonCodes;
import com.concur.mobile.gov.expense.doc.service.AttachTMReceiptRequest;
import com.concur.mobile.gov.expense.doc.service.DeleteTMExpenseRequest;
import com.concur.mobile.gov.expense.doc.service.DocumentDetailRequest;
import com.concur.mobile.gov.expense.doc.service.DocumentListRequest;
import com.concur.mobile.gov.expense.doc.service.GetAuthForVchDocListRequest;
import com.concur.mobile.gov.expense.doc.service.GetTMExpenseFormRequest;
import com.concur.mobile.gov.expense.doc.service.GetTMExpenseTypesRequest;
import com.concur.mobile.gov.expense.doc.service.SaveTMExpenseFormRequest;
import com.concur.mobile.gov.expense.doc.stamp.data.MttDocument;
import com.concur.mobile.gov.expense.doc.stamp.data.MttReturnTo;
import com.concur.mobile.gov.expense.doc.stamp.data.MttStamps;
import com.concur.mobile.gov.expense.doc.stamp.service.AvailableStampsRequest;
import com.concur.mobile.gov.expense.doc.stamp.service.StampRequirementInfoRequest;
import com.concur.mobile.gov.expense.doc.stamp.service.StampTMDocumentRequest;
import com.concur.mobile.gov.expense.doc.voucher.service.CreateVoucherFromAuthRequest;
import com.concur.mobile.gov.expense.service.GovSearchListRequest;
import com.concur.mobile.gov.travel.activity.TDYPerDiemLocationItem;
import com.concur.mobile.gov.travel.data.PerDiemListRow;
import com.concur.mobile.gov.travel.service.AuthNumsRequest;
import com.concur.mobile.gov.travel.service.DocInfoFromTripLocatorRequest;
import com.concur.mobile.gov.travel.service.GovAirFilterRequest;
import com.concur.mobile.gov.travel.service.GovAirSearchRequest;
import com.concur.mobile.gov.travel.service.GovAirSellRequest;
import com.concur.mobile.gov.travel.service.GovCarSellRequest;
import com.concur.mobile.gov.travel.service.GovHotelConfirmRequest;
import com.concur.mobile.gov.travel.service.GovHotelSearchRequest;
import com.concur.mobile.gov.travel.service.GovLocationSearchRequest;
import com.concur.mobile.gov.travel.service.GovRailSellRequest;
import com.concur.mobile.gov.travel.service.PerDiemLocationListRequest;
import com.concur.mobile.gov.travel.service.PerDiemRateRequest;

public class GovService extends ConcurService {

    protected HandlerThread getHandlerThread() {
        return new HandlerThread("GovCoreService");
    }

    protected ConcurServiceHandler getHandler(Looper lpr) {
        return new GovServiceHandler(this, lpr);
    }

    /**
     * send document list request
     * 
     * @return refence of documentListRequest class
     */
    public DocumentListRequest sendDocumentListRequest() {
        DocumentListRequest request = null;
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new DocumentListRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            // Set the message up
            Message msg = handler
                .obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_DOCUMENT, request);
            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Get document detail.
     * 
     * @param docName
     *            : selected document name
     * @param docType
     *            : selected documetn type
     * @param travelerId
     *            : traveler id
     * @return : reference of DocumentDetailRequest class/
     */
    public DocumentDetailRequest sendDocumentDetailRequest(String docName, String docType,
        String travelerId)
    {
        DocumentDetailRequest docDetailRequest = null;
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            docDetailRequest = new DocumentDetailRequest();
            docDetailRequest.sessionId = sessionId;
            docDetailRequest.userId = userId;
            docDetailRequest.messageId = Long.toString(System.currentTimeMillis());
            docDetailRequest.docName = docName;
            docDetailRequest.docType = docType;
            docDetailRequest.travid = travelerId;
            // Set the message up
            Message msg = handler
                .obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_DOCUMENT_DETAIL, docDetailRequest);
            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return docDetailRequest;
    }

    /**
     * Get list of stamp from server of selected document.
     * 
     * @param docName
     *            : selected document name
     * @param docType
     *            : selected documetn type
     * @param travelerId
     *            : traveler id
     * @return : reference of AvailableStampsRequest class
     */
    public AvailableStampsRequest sendStampListRequest(String docName, String docType,
        String travelerId)
    {
        AvailableStampsRequest availableStampsRequest = null;
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            availableStampsRequest = new AvailableStampsRequest();
            availableStampsRequest.sessionId = sessionId;
            availableStampsRequest.userId = userId;
            availableStampsRequest.messageId = Long.toString(System.currentTimeMillis());
            availableStampsRequest.docName = docName;
            availableStampsRequest.docType = docType;
            availableStampsRequest.travid = travelerId;
            // Set the message up
            Message msg = handler
                .obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_LIST_OF_STAMP, availableStampsRequest);
            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return availableStampsRequest;
    }

    /**
     * Reason required service. Get information of reason item on ui.
     * 
     * @param docName
     *            : selected document name
     * @param docType
     *            : selected documetn type
     * @param travelerId
     *            : traveler id
     * @param stampName
     *            : selected stamp
     * @param stampReqUserId
     *            : user id from server.
     * @return reference of StampRequirementInfoRequest class
     */
    public StampRequirementInfoRequest sendStampReasonRequiredInfo(String docName, String docType,
        String travelerId, String stampName, String stampReqUserId)
    {
        StampRequirementInfoRequest request = null;
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new StampRequirementInfoRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.docName = docName;
            request.docType = docType;
            request.travId = travelerId;
            request.stampName = stampName;
            request.stampReqUserId = stampReqUserId;
            // Set the message up
            Message msg = handler
                .obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_STAMP_REQ_RESPONSE, request);
            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Stamp the document
     * 
     * @param document
     *            : selected gov document.
     * @param signingPinText
     *            : signing pin/key
     * @param commentsText
     *            : comments
     * @param selectedMttStamp
     *            :selected stamp
     * @param selectedReason
     *            : selected reason code
     * @param selectedReturnTo
     *            : selected returnTo element
     * @return : reference of StampTMDocumentRequest.
     */
    public StampTMDocumentRequest sendStampGovDocumentReq(MttDocument document,
        String signingPinText, String commentsText, MttStamps selectedMttStamp,
        ReasonCodes selectedReason, MttReturnTo selectedReturnTo)
    {
        StampTMDocumentRequest request = null;
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new StampTMDocumentRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.docName = document.docName;
            request.docType = document.docType;
            request.travid = document.travId;
            if (signingPinText != null) {
                request.signkey = signingPinText;
            }
            String value = "";
            if (selectedMttStamp != null) {
                value = selectedMttStamp.stamp;
            }
            request.stampName = value;
            value = "";
            if (selectedReason != null) {
                value = selectedReason.code;
            }
            request.reasonCode = value;
            value = "";
            if (selectedReturnTo != null) {
                value = selectedReturnTo.returntoId;
            }
            request.returnTo = value;
            // Set the message up
            Message msg = handler
                .obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_STAMP_DOC_RESPONSE, request);
            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Get list of unapplied expenses list from server.
     * 
     * @return : reference of MobileExpenseListRequest
     */
    public MobileExpenseListRequest sendMobileExpenseListReq() {
        MobileExpenseListRequest request = null;
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new MobileExpenseListRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            // Set the message up
            Message msg = handler
                .obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_UNAPP_EXP_LIST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    public CreateVoucherFromAuthRequest sendCreateVoucherFromAuthReq(String travelerId, String authName, String authType)
    {
        CreateVoucherFromAuthRequest request = null;

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new CreateVoucherFromAuthRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.travelerId = travelerId;
            request.authName = authName;
            request.authType = authType;

            // Set the message up
            Message msg = handler.obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_CREATE_VOUCHER_FROM_AUTH, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * send request to add list of unapplied expense to selected document voucher.
     * 
     * @param docType
     *            : document type
     * @param vchNum
     *            : document name
     * @param checkedExpItem
     *            : list of selected mobile expenses
     * @return : reference of AddToVchRequest.
     */
    public AddToVchRequest addListOfExpToVch(String docType, String vchNum, HashSet<MobileExpense> checkedExpItem) {
        AddToVchRequest request = null;

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new AddToVchRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.docType = docType;
            request.vchNum = vchNum;
            request.mobileExpList = checkedExpItem;
            // Set the message up
            Message msg = handler.obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_ADD_EXP_TO_VCH, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    public AttachTMReceiptRequest sendAttachTMReceiptRequest(String receiptId, String ccExpId, String docName,
        String docType, String expId)
    {
        AttachTMReceiptRequest request = null;

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new AttachTMReceiptRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.receiptId = receiptId;
            request.ccExpId = ccExpId;
            request.docName = docName;
            request.docType = docType;
            request.expId = expId;

            // Set the message up
            Message msg = handler.obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_ATTACH_TM_RECEIPT, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * send request to get privacy act notice.
     * 
     * @return : reference of GovMessagesRequest.
     */
    public GovMessagesRequest sendPrivacyActNoticeRequest() {
        GovMessagesRequest request = null;
        request = new GovMessagesRequest();
        Message msg = handler.obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_GOV_MSGS, request);
        // Hand off to the handler thread
        handler.sendMessage(msg);
        return request;
    }

    /**
     * Get auth numbers to book Air,Car,Hotel or Rail
     * 
     * @return : reference of AuthNumsRequest
     */
    public AuthNumsRequest getAuthNums() {
        AuthNumsRequest request = null;
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new AuthNumsRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            // Set the message up
            Message msg = handler
                .obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_AUTH_NUMS, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Get per diem list location to book Air,Car or Rail
     * 
     * @param selectedLocation
     * 
     * @param latitude
     *            : latitude of selected location
     * @param longitude
     *            : longitude of selected location
     * @param countryCode
     *            : country code of selected location
     * @param range
     *            : range, by default send 50.
     * @return : reference of PerDiemLocationListRequest
     */
    public PerDiemLocationListRequest getPerdiemLocationList(LocationChoice selectedLocation) {
        PerDiemLocationListRequest request = null;
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new PerDiemLocationListRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.latitude = selectedLocation.latitude;
            request.longitude = selectedLocation.longitude;
            request.countryCode = selectedLocation.countryAbbrev;
            request.state = selectedLocation.state;
            request.city = selectedLocation.city;

            // Set the message up
            Message msg = handler
                .obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_PERDIEM_LOCATION, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Get per diem list location to book hotel
     * 
     * @param stateOrCountryCode
     *            : country code
     * @param currency
     *            : currency code
     * @param startDate
     *            : trip begin date
     * @param endDate
     *            : trip end date
     * @param location
     *            : location name
     * @return : reference of PerDiemRateRequest
     */
    public PerDiemRateRequest getPerdiemRateLocationList(String stateOrCountryCode, String currency,
        Calendar startDate,
        Calendar endDate, String location) {
        PerDiemRateRequest request = null;
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new PerDiemRateRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.stateOrCountryCode = stateOrCountryCode;
            request.currency = currency;
            request.checkInDate = startDate;
            request.checkOutDate = endDate;
            request.location = location;
            // Set the message up
            Message msg = handler
                .obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_PERDIEM_RATE_LOCATION, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will perform a search for hotels based on check-in/out dates, hotel chain
     * and location.
     * 
     * Upon search completion, a broadcast will be sent with action
     * 'Const.ACTION_HOTEL_SEARCH_RESULTS' indicating the search has completed.
     * The results of the search, a 'HotelSearchReply' object will be set on the
     * application instance, i.e., 'ConcurCore'.
     * 
     * @param dateEnd
     *            check-in date.
     * @param dateStart
     *            check-out date.
     * @param hotelChain
     *            hotel chain.
     * @param lat
     *            search center latitude.
     * @param lon
     *            search center longitude.
     * @param radius
     *            search radius.
     * @param startIndex
     *            the starting index into a set of cached results on the server.
     * @param count
     *            the number of results to return from the cached results on the
     *            server.
     */
    public GovHotelSearchRequest searchPerdiemHotel(Calendar dateEnd, Calendar dateStart, String hotelChain,
        String lat, String lon, String radius, String scale, Integer startIndex, Integer count, Double perDiemAmt) {

        GovHotelSearchRequest request = null;
        String msgId = Long.toString(System.currentTimeMillis());
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            // Build the message and hand off to the handler
            request = new GovHotelSearchRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            request.dateEnd = dateEnd;
            request.dateStart = dateStart;
            request.hotelChain = hotelChain;
            request.lat = lat;
            request.lon = lon;
            request.perDiemRate = perDiemAmt;
            request.radius = radius;
            request.scale = scale;
            request.startIndex = startIndex;
            request.count = count;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_HOTEL_SEARCH_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Generate auth number to book Air,Car,Hotel or Rail
     * 
     * @return : reference of GovRulesAgreementRequest
     */
    public GovRulesAgreementRequest sendSafeHarborAgreement(Boolean isAgree) {
        GovRulesAgreementRequest request = null;
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new GovRulesAgreementRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.isAgree = isAgree;
            // Set the message up
            Message msg = handler
                .obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_SEND_AGREEMENT, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Performs an Air search with the given criteria.
     */
    public ServiceRequest searchForFlights(String departIATA, String arriveIATA, Calendar departDT,
        Calendar returnDT,
        String cabinClass, boolean refundableOnly) {

        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (sessionId != null) {
            // Build the message and hand off to the handler
            GovAirSearchRequest request = new GovAirSearchRequest();
            request.departIATA = departIATA;
            request.arriveIATA = arriveIATA;
            request.departDateTime = departDT;
            request.returnDateTime = returnDT;
            request.cabinClass = cabinClass;
            request.refundableOnly = refundableOnly;
            request.showGovRateTypes = true;

            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_AIR_SEARCH_REQUEST, request);
            handler.sendMessage(msg);

            return request;
        }

        return null;
    }

    /**
     * Performs a filtered Air search with the given criteria.
     */
    public AirFilterRequest getFilteredFlights(String airlineCode, String rateType) {

        GovAirFilterRequest request = null;

        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (sessionId != null) {
            // Build the message and hand off to the handler
            request = new GovAirFilterRequest();
            request.airlineCode = airlineCode;
            request.rateType = rateType;

            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_AIR_FILTER_REQUEST, request);
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Will send a request to book an airfare to the server.
     * 
     * @param userId
     *            the user id performing the action.
     * @param ccId
     *            the credit card id.
     * @param fareId
     *            the fare id.
     * @param refundableOnly
     *            refundable only.
     * @param tripName
     *            the trip name.
     * @param fields
     *            a list of custom booking information fields.
     * @param existingTANumber
     *            the travel authorization number
     * @param perdiemLocationID
     *            the per-diem location ID
     * @param selectedPerDiemItem
     * @return an instance of <code>AirSellRequest</code> that can be cancelled.
     */
    public AirSellRequest sendAirSellRequest(String userId, int ccId, String fareId, String programId,
        boolean refundableOnly, String tripName, String violationCode, String violationJustification,
        List<TravelCustomField> fields, String existingTANumber, String perdiemLocationID,
        TDYPerDiemLocationItem selectedPerDiemItem) {

        GovAirSellRequest request = null;

        String sessionId = Preferences.getSessionId();

        if (sessionId != null) {
            request = new GovAirSellRequest();
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.ccId = ccId;
            request.existingTANumber = existingTANumber;
            request.fareId = fareId;
            request.programId = programId;
            request.refundableOnly = refundableOnly;
            request.tripName = tripName;
            request.violationCode = violationCode;
            request.violationJustification = violationJustification;
            request.fields = fields;
            if (selectedPerDiemItem == null || (selectedPerDiemItem.getPerDiemItem() == null)) {
                request.country = "US";
                request.zipCode = null;
                request.state = null;
                request.name = null;
            } else {
                PerDiemListRow item = selectedPerDiemItem.getPerDiemItem();
                String stateOrCountry = item.conus;
                if (stateOrCountry != null && stateOrCountry.equalsIgnoreCase("c")) {
                    request.country = "US";
                    request.state = item.locst;
                } else if (stateOrCountry != null && stateOrCountry.equalsIgnoreCase("o")) {
                    request.country = item.locst;
                    request.state = null;
                } else {
                    request.country = "US";
                    request.state = null;
                }
                request.zipCode = Integer.toString(item.zipcode);
                request.name = item.locate;
            }

            // Set the per-diem loc ID:
            request.perdiemLocationID = perdiemLocationID;
            request.existingTANumber = existingTANumber;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_AIR_SELL_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Reserve car
     * 
     * @param carId
     * @param creditCardId
     * @param recordLocator
     * @param tripId
     * @param tripLocator
     * @param violationCode
     * @param violationJustification
     * @param fields
     * @param existingTANumber
     * @param perdiemLocationID
     * @param selectedPerDiemItem
     * @return
     */
    public GovCarSellRequest reserveCar(String carId, String creditCardId, String recordLocator, String tripId,
        String tripLocator, String violationCode, String violationJustification, List<TravelCustomField> fields,
        String existingTANumber, String perdiemLocationID, TDYPerDiemLocationItem selectedPerDiemItem) {

        GovCarSellRequest request = null;

        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (sessionId != null) {
            // Build the message and hand off to the handler
            request = new GovCarSellRequest();
            request.carId = carId;
            request.creditCardId = creditCardId;
            request.recordLocator = recordLocator;
            request.tripId = tripId;
            request.tripLocator = tripLocator;
            request.violationCode = violationCode;
            request.violationJustification = violationJustification;
            request.fields = fields;
            request.existingTANumber = existingTANumber;
            request.perdiemLocationID = perdiemLocationID;
            if (selectedPerDiemItem == null || (selectedPerDiemItem.getPerDiemItem() == null)) {
                request.country = null;
                request.zipCode = null;
                request.state = null;
                request.name = null;
            } else {
                PerDiemListRow item = selectedPerDiemItem.getPerDiemItem();
                String stateOrCountry = item.conus;
                if (stateOrCountry != null && stateOrCountry.equalsIgnoreCase("c")) {
                    request.country = "US";
                    request.state = item.locst;
                } else if (stateOrCountry != null && stateOrCountry.equalsIgnoreCase("o")) {
                    request.country = item.locst;
                    request.state = null;
                } else {
                    request.country = "US";
                    request.state = null;
                }
                request.zipCode = Integer.toString(item.zipcode);
                request.name = item.locate;
            }
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_CAR_SELL_REQUEST, request);
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Will send a request to confirm a hotel room.
     * 
     * Upon completion, a broadcast will be sent with action
     * 'Const.ACTION_HOTEL_CONFIRM_RESULTS' indicating the confirm has
     * completed.
     * 
     * @param bicCode
     *            the bic code for the room.
     * @param ccId
     *            the credit card id used to confirm the room.
     * @param chainCode
     *            the hotel chain code.
     * @param propertyId
     *            the hotel property id.
     * @param propertyName
     *            the hotel property name.
     * @param sellSource
     *            the hotel room sell source.
     * @param tripId
     *            the trip id.
     * @param hotelReason
     *            the hotel reason justification.
     * @param hotelReasonCode
     *            the hotel reason id code.
     * @param fields
     *            the list of booking information fields.
     * @param perdiemLocationID
     * @param existingTANumber
     * @param selectedPerDiemItem
     */
    public GovHotelConfirmRequest sendConfirmGovHotelRoomRequest(String bicCode, String ccId, String chainCode,
        String propertyId, String propertyName, String sellSource, String tripId, String hotelReason,
        String hotelReasonCode, List<TravelCustomField> fields, String existingTANumber, String perdiemLocationID,
        TDYPerDiemLocationItem selectedPerDiemItem) {

        GovHotelConfirmRequest request = null;
        String msgId = Long.toString(System.currentTimeMillis());
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new GovHotelConfirmRequest();
            request.bicCode = bicCode;
            request.ccId = ccId;
            request.chainCode = chainCode;
            request.propertyId = propertyId;
            request.propertyName = propertyName;
            request.sellSource = sellSource;
            request.tripId = tripId;
            request.hotelReason = hotelReason;
            request.hotelReasonCode = hotelReasonCode;
            request.fields = fields;
            request.perdiemLocationID = perdiemLocationID;
            request.existingTANumber = existingTANumber;
            if (selectedPerDiemItem == null || (selectedPerDiemItem.getPerDiemItem() == null)) {
                request.country = null;
                request.zipCode = null;
                request.state = null;
                request.name = null;
            } else {
                PerDiemListRow item = selectedPerDiemItem.getPerDiemItem();
                String stateOrCountry = item.conus;
                if (stateOrCountry != null && stateOrCountry.equalsIgnoreCase("c")) {
                    request.country = "US";
                    request.state = item.locst;
                } else if (stateOrCountry != null && stateOrCountry.equalsIgnoreCase("o")) {
                    request.country = item.locst;
                    request.state = null;
                } else {
                    request.country = "US";
                    request.state = null;
                }
                request.zipCode = Integer.toString(item.zipcode);
                request.name = item.locate;
            }
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_HOTEL_CONFIRM_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * 
     * Send The request to reserve Rail/Amtrak
     * 
     * @param groupId
     * @param bucket
     * @param creditCardId
     * @param deliveryOption
     * @param violationCode
     * @param violationJustification
     * @param fields
     * @param existingTANumber
     * @param perdiemLocationID
     * @param selectedPerDiemItem
     * @return
     */
    public GovRailSellRequest reserveTrainReq(String groupId, String bucket, String creditCardId,
        String deliveryOption,
        String violationCode, String violationJustification, List<TravelCustomField> fields,
        String existingTANumber, String perdiemLocationID, TDYPerDiemLocationItem selectedPerDiemItem) {

        GovRailSellRequest request = null;

        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (sessionId != null) {
            // Build the message and hand off to the handler
            request = new GovRailSellRequest();
            request.groupId = groupId;
            request.bucket = bucket;
            request.creditCardId = creditCardId;
            request.deliveryOption = deliveryOption;
            request.violationCode = violationCode;
            request.violationJustification = violationJustification;
            request.fields = fields;
            request.perdiemLocationID = perdiemLocationID;
            request.existingTANumber = existingTANumber;
            if (selectedPerDiemItem == null || (selectedPerDiemItem.getPerDiemItem() == null)) {
                request.country = "US";
                request.zipCode = null;
                request.state = null;
                request.name = null;
            } else {
                PerDiemListRow item = selectedPerDiemItem.getPerDiemItem();
                String stateOrCountry = item.conus;
                if (stateOrCountry != null && stateOrCountry.equalsIgnoreCase("c")) {
                    request.country = "US";
                    request.state = item.locst;
                } else if (stateOrCountry != null && stateOrCountry.equalsIgnoreCase("o")) {
                    request.country = item.locst;
                    request.state = null;
                } else {
                    request.country = "US";
                    request.state = null;
                }
                request.zipCode = Integer.toString(item.zipcode);
                request.name = item.locate;
            }
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_RAIL_SELL_REQUEST, request);
            handler.sendMessage(msg);
        }

        return request;
    }

    public GetTMExpenseTypesRequest sendGetTMExpenseTypesRequest()
    {
        GetTMExpenseTypesRequest request = null;

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new GetTMExpenseTypesRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());

            // Set the message up
            Message msg = handler.obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_TM_EXPENSE_TYPES, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    public GetTMExpenseFormRequest sendGetTMExpenseFormRequest(String docType, String expDesc)
    {
        GetTMExpenseFormRequest request = null;

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new GetTMExpenseFormRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.docType = docType;
            request.expDescription = expDesc;

            // Set the message up
            Message msg = handler.obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_TM_EXPENSE_FORM, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * get document info from trip locator.
     * 
     * @param taNumber
     * @param travid
     * @param tripLocator
     * @return
     */
    public DocInfoFromTripLocatorRequest getDocInfoFromTripLocator(String taNumber, String travid, String tripLocator) {

        DocInfoFromTripLocatorRequest request = null;

        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (sessionId != null) {
            // Build the message and hand off to the handler
            request = new DocInfoFromTripLocatorRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;

            request.tanum = taNumber;
            request.travid = travid;
            request.tripLocator = tripLocator;

            Message msg = handler.obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_DOC_INFO_FROM_TRIPLOCATOR_FAIL, request);
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * 
     * Send request to delete TM expense of selected document voucher.
     * 
     * @param docName
     * @param docType
     * @param expId
     * @return
     */
    public DeleteTMExpenseRequest deleteTMExpense(String docName, String docType, String expId) {

        DeleteTMExpenseRequest request = null;

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new DeleteTMExpenseRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.docName = docName;
            request.docType = docType;
            request.expId = expId;
            // Set the message up
            Message msg = handler.obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_DELETE_TM_EXPENSE, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * 
     * Send request to delete TM unapplied expense of selected document voucher.
     * 
     * @param ccExpId
     * 
     * @return
     */
    public DeleteTMUnappliedExpenseRequest deleteTMUnappliedExpense(String ccExpId) {

        DeleteTMUnappliedExpenseRequest request = null;

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new DeleteTMUnappliedExpenseRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.ccExpId = ccExpId;
            // Set the message up
            Message msg = handler.obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_DELETE_TM_UNAPPLIED_EXPENSE, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * 
     * Send request to save new TM expense
     * 
     */
    public SaveTMExpenseFormRequest sendSaveTMExpenseFormRequest(GovExpenseForm form) {

        SaveTMExpenseFormRequest request = null;

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new SaveTMExpenseFormRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.form = form;

            // Set the message up
            Message msg = handler.obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_SAVE_TM_EXPENSE_FORM, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a search list request to the server.
     * 
     * @param userId
     *            the user id.
     * @param query
     *            the search query.
     * @param fieldId
     *            the field id.
     * @return an instance of <code>GovSearchListRequest</code> if the request was made; <code>null</code> otherwise.
     */
    public GovSearchListRequest sendGovSearchListRequest(String userId, String query, String fieldId, String expDesc) {
        GovSearchListRequest request = null;

        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {

            request = new GovSearchListRequest();
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.query = query;
            request.fieldId = fieldId;
            request.docType = "VCH";
            request.expenseDescription = expDesc;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_SEARCH_LIST_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    public void govSearchLocations(String searchText) {
        govSearchLocations(searchText, false);
    }

    public void govSearchLocations(String searchText, boolean airportsOnly) {

        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (sessionId != null) {
            // Build the message and hand off to the handler
            GovLocationSearchRequest request = new GovLocationSearchRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            request.searchText = searchText;
            request.airportsOnly = airportsOnly;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_LOCATION_SEARCH_REQUEST, request);
            handler.sendMessage(msg);
        }
    }

    /**
     * send auth document list request which are ready fpr voucher
     * 
     * @return refence of documentListRequest class
     */
    public GetAuthForVchDocListRequest sendAuthForVchDocRequest() {
        GetAuthForVchDocListRequest request = null;
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new GetAuthForVchDocListRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            // Set the message up
            Message msg = handler
                .obtainMessage(com.concur.mobile.gov.util.Const.HANDLER_MSG_GET_AUTH_FOR_VCH_DOCUMENT, request);
            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }
}
