package com.concur.mobile.core.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.location.Address;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.data.IExpenseReportEntryInfo;
import com.concur.mobile.core.data.IExpenseReportInfo;
import com.concur.mobile.core.data.IExpenseReportInfo.ReportType;
import com.concur.mobile.core.data.IItineraryDBInfo;
import com.concur.mobile.core.data.IItineraryInfo;
import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.data.ReportDBUtil;
import com.concur.mobile.core.data.SystemConfig;
import com.concur.mobile.core.data.UserConfig;
import com.concur.mobile.core.expense.charge.data.AttendeesEntryMap;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.MobileEntry;
import com.concur.mobile.core.expense.charge.data.PersonalCard;
import com.concur.mobile.core.expense.charge.service.CardListRequest;
import com.concur.mobile.core.expense.charge.service.DeleteMobileEntriesRequest;
import com.concur.mobile.core.expense.charge.service.SaveMobileEntryRequest;
import com.concur.mobile.core.expense.data.CountSummary;
import com.concur.mobile.core.expense.data.ExpenseListInfo;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.data.ReceiptPictureSaveAction;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptInfo;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptShareItem;
import com.concur.mobile.core.expense.receiptstore.service.DeleteReceiptImageRequest;
import com.concur.mobile.core.expense.receiptstore.service.GetReceiptImageUrlRequest;
import com.concur.mobile.core.expense.receiptstore.service.GetReceiptImageUrlsRequest;
import com.concur.mobile.core.expense.receiptstore.service.RetrieveURLRequest;
import com.concur.mobile.core.expense.report.approval.service.ApproveReportRequest;
import com.concur.mobile.core.expense.report.approval.service.RejectReportRequest;
import com.concur.mobile.core.expense.report.approval.service.ReportsToApproveRequest;
import com.concur.mobile.core.expense.report.data.CarConfig;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportApprover;
import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.data.ExpenseReportListInfo;
import com.concur.mobile.core.expense.report.data.IExpenseReportListInfo;
import com.concur.mobile.core.expense.report.service.ActiveReportsRequest;
import com.concur.mobile.core.expense.report.service.AddReportReceiptRequest;
import com.concur.mobile.core.expense.report.service.AddReportReceiptV2Request;
import com.concur.mobile.core.expense.report.service.AddToReportRequest;
import com.concur.mobile.core.expense.report.service.AppendReceiptImageRequest;
import com.concur.mobile.core.expense.report.service.ApproverSearchRequest;
import com.concur.mobile.core.expense.report.service.AttendeeFormRequest;
import com.concur.mobile.core.expense.report.service.AttendeeSaveRequest;
import com.concur.mobile.core.expense.report.service.AttendeeSearchFieldsRequest;
import com.concur.mobile.core.expense.report.service.AttendeeSearchRequest;
import com.concur.mobile.core.expense.report.service.CarConfigsRequest;
import com.concur.mobile.core.expense.report.service.ClearReportEntryReceiptRequest;
import com.concur.mobile.core.expense.report.service.DefaultAttendeeRequest;
import com.concur.mobile.core.expense.report.service.DistanceToDateRequest;
import com.concur.mobile.core.expense.report.service.ExchangeRateRequest;
import com.concur.mobile.core.expense.report.service.ExtendedAttendeeSearchRequest;
import com.concur.mobile.core.expense.report.service.GetConditionalFieldActionRequest;
import com.concur.mobile.core.expense.report.service.GetTaxFormRequest;
import com.concur.mobile.core.expense.report.service.ItemizeHotelRequest;
import com.concur.mobile.core.expense.report.service.MarkEntryReceiptViewedRequest;
import com.concur.mobile.core.expense.report.service.MarkReceiptsViewedRequest;
import com.concur.mobile.core.expense.report.service.RemoveReportExpenseRequest;
import com.concur.mobile.core.expense.report.service.ReportDeleteRequest;
import com.concur.mobile.core.expense.report.service.ReportDetailRequest;
import com.concur.mobile.core.expense.report.service.ReportEntryDetailRequest;
import com.concur.mobile.core.expense.report.service.ReportEntryFormRequest;
import com.concur.mobile.core.expense.report.service.ReportFormRequest;
import com.concur.mobile.core.expense.report.service.ReportHeaderDetailRequest;
import com.concur.mobile.core.expense.report.service.ReportItemizationEntryFormRequest;
import com.concur.mobile.core.expense.report.service.SaveReportEntryReceiptRequest;
import com.concur.mobile.core.expense.report.service.SaveReportEntryRequest;
import com.concur.mobile.core.expense.report.service.SaveReportRequest;
import com.concur.mobile.core.expense.report.service.SubmitReportRequest;
import com.concur.mobile.core.expense.service.CountSummaryRequest;
import com.concur.mobile.core.expense.service.DownloadMobileEntryReceiptRequest;
import com.concur.mobile.core.expense.service.GetExpenseTypesRequest;
import com.concur.mobile.core.expense.service.SaveReceiptRequest;
import com.concur.mobile.core.expense.service.SearchListRequest;
import com.concur.mobile.core.travel.air.service.AirCancelRequest;
import com.concur.mobile.core.travel.air.service.AirFilterRequest;
import com.concur.mobile.core.travel.air.service.AirSearchRequest;
import com.concur.mobile.core.travel.air.service.AirSellRequest;
import com.concur.mobile.core.travel.air.service.AlternativeAirScheduleRequest;
import com.concur.mobile.core.travel.car.service.CancelCarRequest;
import com.concur.mobile.core.travel.car.service.CarSearchRequest;
import com.concur.mobile.core.travel.car.service.CarSellRequest;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.SellOptionField;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.travel.hotel.service.HotelConfirmRequest;
import com.concur.mobile.core.travel.hotel.service.HotelDetailRequest;
import com.concur.mobile.core.travel.hotel.service.HotelImagesRequest;
import com.concur.mobile.core.travel.hotel.service.HotelSearchRequest;
import com.concur.mobile.core.travel.rail.data.RailStation;
import com.concur.mobile.core.travel.rail.service.CancelRailRequest;
import com.concur.mobile.core.travel.rail.service.RailSearchRequest;
import com.concur.mobile.core.travel.rail.service.RailSellRequest;
import com.concur.mobile.core.travel.rail.service.RailStationListReply;
import com.concur.mobile.core.travel.rail.service.RailStationListRequest;
import com.concur.mobile.core.travel.rail.service.RailTicketDeliveryOptionsRequest;
import com.concur.mobile.core.travel.service.ItineraryRequest;
import com.concur.mobile.core.travel.service.ItinerarySummaryListRequest;
import com.concur.mobile.core.travel.service.LocationSearchRequest;
import com.concur.mobile.core.travel.service.ReasonCodeRequest;
import com.concur.mobile.core.travel.service.TravelCustomFieldsRequest;
import com.concur.mobile.core.travel.service.TravelCustomFieldsUpdateRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ExpenseDAOConverter;
import com.concur.mobile.core.util.ReceiptDAOConverter;
import com.concur.mobile.platform.expense.provider.ExpenseUtil;
import com.concur.mobile.platform.expense.receipt.list.ReceiptListUtil;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.expense.smartexpense.SmartExpense;

public class ConcurService extends Service {

    private static final String CLS_TAG = ConcurService.class.getSimpleName();

    protected final IBinder binder = new LocalBinder();

    protected Looper looper;
    public ConcurServiceHandler handler;

    public SharedPreferences prefs;

    /**
     * Contains whether or not reposting is enabled. NOTE: This is only currently being used to determine whether to punt post
     * requests that have been stored in the database. There is no current support in the client for re-posting, perhaps in the
     * future. Until then, trying to destroy the existing functionality we have put into the client to support this.
     */
    // protected boolean repostEnabled;

    public MobileDatabase db;

    /**
     * Class for clients to access. Because we know this service always runs in the same process as its clients, we don't need to
     * deal with IPC.
     */
    public class LocalBinder extends Binder {

        public ConcurService getService() {
            return ConcurService.this;
        }
    }

    @Override
    public void onCreate() {

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Hook up our database
        db = new MobileDatabase(this);

        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        HandlerThread thread = getHandlerThread();
        thread.start();

        // Grab the looper for the thread and hook our handler up to it
        looper = thread.getLooper();
        handler = getHandler(looper);

        super.onCreate();
    }

    /**
     * Gets the instance of <code>MobileDatabase</code> that has been instantiated by this service. <br>
     * <b>NOTE:</b> ConcurService <b>owns</b> the returned instance. Clients should not attempt to close the returned instance.
     * 
     * @return returns the instance of <code>MobileDatabase</code> that has been instantiated by this service.
     */
    public MobileDatabase getMobileDatabase() {
        return db;
    }

    protected HandlerThread getHandlerThread() {
        return new HandlerThread("ConcurCoreService");
    }

    /**
     * Constructs a new instance of <code>ConcurServiceHandler</code> and associates it with a looper.
     * 
     * @param lpr
     *            contains the looper.
     * @return returns an instance of <code>ConcurServiceHandler</code>.
     */
    protected ConcurServiceHandler getHandler(Looper lpr) {
        return new ConcurServiceHandler(this, lpr);
    }

    @Override
    public void onDestroy() {
        // Make sure to shutdown the looper
        looper.quit();

        // Instruct the mobile database to close.
        db.close();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".onBind: ");
        // Return our local binder
        return binder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Service#onRebind(android.content.Intent)
     */
    @Override
    public void onRebind(Intent intent) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".onRebind: ");
        super.onRebind(intent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Service#onUnbind(android.content.Intent)
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".onUnbind: ");
        return super.onUnbind(intent);
    }

    /**
     * Will enqueue a message to the service handler that it needs to clear the local database after any outstanding requests.
     */
    public void resetData() {

        // Construct a message to punt the local data store.
        Message msg = handler.obtainMessage(Const.MSG_CLEAR_LOCAL_DATA);

        // Hand off the message for processing.
        handler.sendMessage(msg);
    }

    /**
     * @deprecated - use {@link com.concur.mobile.platform.authentication.LogoutRequestTask LogoutRequestTask} instead.
     * 
     *             Will send a request to log out of the current session.
     */
    @Deprecated
    public void sendLogoutRequest() {
        LogoutRequest request = null;
        // Obtain the current session ID.
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            // Set the message up.
            request = new LogoutRequest();
            request.sessionId = sessionId;
            request.messageId = Long.toString(System.currentTimeMillis());
            Message msg = handler.obtainMessage(Const.MSG_LOGOUT_REQUEST, request);

            // Hand off the message for processing.
            handler.sendMessage(msg);
        }
    }

    // ///////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////
    //
    // Travel-related endpoints
    //
    // ///////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Retrieves a list of <code>Trip</code> objects from the local cache representing trip summary objects.
     * 
     * NOTE: If there is data present, this method will set the current trip summary list and "last retrieved timestamp" within
     * the itinerary cache.
     * 
     * @return an list of <code>Trip</code> objects if cached locally; otherwise <code>null</code> is returned.
     */
    public List<Trip> getItinerarySummaryList() {

        ArrayList<Trip> trips = null;

        String userId = prefs.getString(Const.PREF_USER_ID, null);

        String cachedResponse = db.loadResponse(Const.MSG_ITINERARY_SUMMARY_LIST_REQUEST, userId);
        if (cachedResponse != null) {
            Calendar lastRetrievedTS = db.getReponseLastRetrieveTS(Const.MSG_ITINERARY_SUMMARY_LIST_REQUEST, userId);
            ConcurCore app = (ConcurCore) getApplication();
            // MOB-21026 - Catch parsing error.
            try {
                trips = Trip.parseItineraryXml(cachedResponse);
            } catch (Exception e) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getItinerarySummaryList: Error parsing the Itinerary XML.");
            }
            if (trips != null) {
                IItineraryCache itinCache = app.getItinCache();
                if (itinCache != null) {
                    itinCache.setItinerarySummaryList(trips);
                    itinCache.setItinerarySummaryListUpdateTime(lastRetrievedTS);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getItinerarySummaryList: itin cache is null!");
                }
            }
        }
        return trips;
    }

    /**
     * Will retrieve from persistence an itinerary info object given an itin locator.
     * 
     * @param itinLocator
     *            the itinerary locator.
     * @return an instance of <code>IItineraryInfo</code>; otherwise, if the itinerary is not locally persisted, <code>null</code>
     *         will be returned.
     */
    public IItineraryInfo getItinerary(String itinLocator) {

        IItineraryDBInfo itinInfo = null;

        String userId = prefs.getString(Const.PREF_USER_ID, null);
        itinInfo = db.loadItinerary(itinLocator, userId);
        if (itinInfo != null) {
            List<Trip> trips = Trip.parseItineraryXml(itinInfo.getXML());
            if (trips != null && trips.size() > 0) {
                itinInfo.setItinerary(trips.get(0));
            }
        }
        return itinInfo;
    }

    /**
     * Will retrieve the last client update time for an itinerary detail object.
     * 
     * @param itinLocator
     *            the itinerary locator.
     * @return returns an instance of <code>Calendar</code> containing the last time the client received an itinerary detail
     *         object; otherwise, <code>null</code> is returned if a detailed itinerary is not stored.
     */
    public Calendar getItineraryUpdateTime(String itinLocator) {
        Calendar updateTime = null;
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        updateTime = db.loadItineraryUpdateTime(itinLocator, userId);
        return updateTime;
    }

    /**
     * Will send a request to retrieve a full detailed itinerary.
     * 
     * @param itinLocator
     *            the itinerary locator id.
     * @return returns an instance of <code>ItineraryRequest</code> which may be used to cancel the request.
     */
    public ItineraryRequest sendItineraryRequest(String itinLocator) {
        return sendItineraryRequest(itinLocator, null, prefs.getString(Const.PREF_USER_ID, null), false);
    }

    /**
     * Will send a request to retrieve a full detailed itinerary.
     * 
     * @param itinLocator
     *            - the itinerary locator id
     * @param companyId
     *            - company id of the user to whom the itinerary belongs
     * @param userId
     *            - user id to whom the itinerary belongs
     * @param isForApprover
     *            - inform server that the requesting itinerary cannot be modified by the client
     * @return
     */
    public ItineraryRequest sendItineraryRequest(String itinLocator, String companyId, String userId,
            boolean isForApprover) {
        ItineraryRequest request = null;
        // The current session ID is stored in the shared preferences.
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            request = new ItineraryRequest();
            request.itinLocator = itinLocator;
            request.sessionId = sessionId;
            request.companyId = companyId;
            request.userId = userId;
            request.isForApprover = isForApprover;
            request.messageId = Long.toString(System.currentTimeMillis());

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_ITINERARY_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to retrieve itinerary summary information.
     * 
     * Once the request is complete, a <code>Const.ACTION_SUMMARY_TRIPS_UPDATED</code> action will be broadcast containing result
     * information.
     * 
     * @param background
     *            whether or not the request should be handled concurently with other requests.
     * 
     * @return an instance of <code>ItinerarySummaryListRequest</code> that may be used to cancel the request.
     */
    public ItinerarySummaryListRequest sendItinerarySummaryListRequest(boolean background) {

        ItinerarySummaryListRequest request = null;

        // The current session ID is stored in the shared preferences.
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            request = new ItinerarySummaryListRequest();
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.background = background;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_ITINERARY_SUMMARY_LIST_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to cancel a car reservation.
     * 
     * @param bookingSource
     *            the car booking source.
     * @param reason
     *            the reason.
     * @param recordLocator
     *            the car booking record locator.
     * @param segmentKey
     *            the car booking segment key.
     * @param tripId
     *            the cliqbook trip id.
     * @return an instance of <code>CancelCarRequest</code>.
     */
    public CancelCarRequest sendCancelCarRequest(String bookingSource, String reason, String recordLocator,
            String segmentKey, String tripId) {
        CancelCarRequest request = null;
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        String requestBody = CancelCarRequest
                .buildRequestBody(bookingSource, reason, recordLocator, segmentKey, tripId);

        // if (repostEnabled) {
        // if (!db.insertHTTPRequest(userId, "POST", CancelCarRequest.SERVICE_END_POINT, requestBody,
        // Const.MSG_CANCEL_CAR_REQUEST, msgId, null)) {
        // Log.e(Const.LOG_TAG, CLS_TAG + ".sendCancelCarRequest: failed to save request body!");
        // }
        // }

        // The current session ID is stored in the shared preferences.
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            // session id will be assigned at the beginning of this request
            // being processed.
            request = new CancelCarRequest();
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.requestBody = requestBody;
            // Set the message up.
            Message msg = handler.obtainMessage(Const.MSG_CANCEL_CAR_REQUEST, request);
            // Hand off to the handler thread.
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Sends a request to cancel a rail reservation.
     * 
     * @param bookingSource
     *            the rail booking source.
     * @param reason
     *            the reason
     * @param recordLocator
     *            the rail booking record locator.
     * @param segmentKey
     *            the rail booking segment key.
     * @param tripId
     *            the cliqbook trip id.
     * @return returns an instance of <code>CancelRailRequest</code> if the request can be succesfully created; <code>null</code>
     *         otherwise.
     */
    public CancelRailRequest sendCancelRailRequest(String bookingSource, String reason, String recordLocator,
            String segmentKey, String tripId) {
        CancelRailRequest request = null;
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        String requestBody = CancelRailRequest.buildRequestBody(bookingSource, reason, recordLocator, segmentKey,
                tripId);
        // if (repostEnabled) {
        // if (!db.insertHTTPRequest(userId, "POST", CancelRailRequest.SERVICE_END_POINT, requestBody,
        // Const.MSG_CANCEL_RAIL_REQUEST, msgId, null)) {
        // Log.e(Const.LOG_TAG, CLS_TAG + ".sendCancelRailRequest: failed to save request body!");
        // }
        // }
        // The current session ID is stored in the shared preferences.
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            // session id will be assigned at the beginning of this request
            // being processed.
            request = new CancelRailRequest();
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.requestBody = requestBody;
            // Set the message up.
            Message msg = handler.obtainMessage(Const.MSG_CANCEL_RAIL_REQUEST, request);
            // Hand off to the handler thread.
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Gets the rail station list
     */
    public ArrayList<RailStation> getRailStationList() {

        String userId = prefs.getString(Const.PREF_USER_ID, null);

        ArrayList<RailStation> stations = null;
        String cachedResponse = db.loadResponse(Const.MSG_TRAVEL_RAIL_STATION_LIST_REQUEST, userId, false);
        if (cachedResponse != null) {
            try {
                RailStationListReply stationReply = RailStationListReply.parseXMLReply(cachedResponse);
                stations = stationReply.railStations;
                Calendar lastResponseTimeTS = db.getReponseLastRetrieveTS(Const.MSG_TRAVEL_RAIL_STATION_LIST_REQUEST,
                        userId);
                ConcurCore app = (ConcurCore) getApplication();
                app.setRailStationList(stations);
                app.setRailStationListLastRetrieved(lastResponseTimeTS);
                app.setCodeRailStationMap(stationReply.codeStationMap);
            } catch (RuntimeException rte) {
                Log.e(Const.LOG_TAG,
                        CLS_TAG + ".getRailStationList: Exception parsing cached response - " + rte.getMessage() + ".",
                        rte);
            }
        }
        return stations;
    }

    /**
     * Sends a request to obtain the latest rail station list for the given vendor
     */
    public RailStationListRequest sendRailStationListRequest(String vendorCode) {

        RailStationListRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {

            request = new RailStationListRequest();

            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.vendorCode = vendorCode;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_RAIL_STATION_LIST_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }

        return request;
    }

    public ServiceRequest searchForFlights(String departIATA, String arriveIATA, Calendar departDT, Calendar returnDT,
            String cabinClass, boolean refundableOnly) {

        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (sessionId != null) {
            // Build the message and hand off to the handler
            AirSearchRequest request = new AirSearchRequest();
            request.departIATA = departIATA;
            request.arriveIATA = arriveIATA;
            request.departDateTime = departDT;
            request.returnDateTime = returnDT;
            request.cabinClass = cabinClass;
            request.refundableOnly = refundableOnly;

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
     * Sends a request to retrieve a filtered list of the last air search results based on airline code and number of stops.
     * 
     * @param airlineCode
     *            the airline code.
     * @param numStops
     *            the number of stops.
     * @return an instance of <code>AirFilterRequest</code> that can be used to cancel the request.
     */
    public AirFilterRequest getFilteredFlights(String airlineCode, String numStops) {

        AirFilterRequest request = null;

        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (sessionId != null) {
            // Build the message and hand off to the handler
            request = new AirFilterRequest();
            request.airlineCode = airlineCode;
            request.numStops = numStops;

            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_AIR_FILTER_REQUEST, request);
            handler.sendMessage(msg);
        }

        return request;
    }

    // for backward compatibility as being used from VoiceCarSearchActivity
    public CarSearchRequest searchForCars(String pickupLat, String pickupLong, Calendar pickupDateTime,
            String dropoffLat, String dropoffLong, Calendar dropoffDateTime, String carType) {
        return searchForCars(pickupLat, pickupLong, pickupDateTime, dropoffLat, dropoffLong, dropoffDateTime, carType,
                null, null);
    }

    // MOB-14727 - new method using the location iata
    public CarSearchRequest searchForCars(String pickupLat, String pickupLong, Calendar pickupDateTime,
            String dropoffLat, String dropoffLong, Calendar dropoffDateTime, String carType, String pickupIata,
            String dropoffIata) {

        CarSearchRequest request = null;

        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (sessionId != null) {
            // Build the message and hand off to the handler
            request = new CarSearchRequest();
            request.pickupLat = pickupLat;
            request.pickupLong = pickupLong;
            request.pickupDateTime = pickupDateTime;
            request.dropoffLat = dropoffLat;
            request.dropoffLong = dropoffLong;
            request.dropoffDateTime = dropoffDateTime;
            request.carType = carType;
            request.offAirport = false;
            request.pickupIata = pickupIata;
            request.dropoffIata = dropoffIata;

            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_CAR_SEARCH_REQUEST, request);
            handler.sendMessage(msg);
        }

        return request;
    }

    public CarSellRequest reserveCar(String carId, String creditCardId, String recordLocator, String tripId,
            String tripLocator, String violationCode, String violationJustification, List<TravelCustomField> fields) {

        CarSellRequest request = null;

        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (sessionId != null) {
            // Build the message and hand off to the handler
            request = new CarSellRequest();
            request.carId = carId;
            request.creditCardId = creditCardId;
            request.recordLocator = recordLocator;
            request.tripId = tripId;
            request.tripLocator = tripLocator;
            request.violationCode = violationCode;
            request.violationJustification = violationJustification;
            request.fields = fields;

            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_CAR_SELL_REQUEST, request);
            handler.sendMessage(msg);
        }

        return request;
    }

    public RailSearchRequest searchForTrains(RailStation depStation, RailStation arrStation, Calendar depDateTime,
            Calendar retDateTime, int numPassengers) {

        RailSearchRequest request = null;

        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (sessionId != null) {
            // Build the message and hand off to the handler
            request = new RailSearchRequest();
            request.depStation = depStation;
            request.arrStation = arrStation;
            request.depDateTime = depDateTime;
            request.retDateTime = retDateTime;
            request.numPassengers = numPassengers;

            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_RAIL_SEARCH_REQUEST, request);
            handler.sendMessage(msg);
        }

        return request;
    }

    public void getRailTicketDeliveryOptions(String groupId, String bucket) {

        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (sessionId != null) {
            // Build the message and hand off to the handler
            RailTicketDeliveryOptionsRequest request = new RailTicketDeliveryOptionsRequest();
            request.groupId = groupId;
            request.bucket = bucket;

            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_RAIL_TICKET_DELIVERY_OPTION_REQUEST, request);
            handler.sendMessage(msg);
        }
    }

    public RailSellRequest reserveTrain(String groupId, String bucket, String creditCardId, String deliveryOption,
            String violationCode, String violationJustification, List<TravelCustomField> fields) {

        RailSellRequest request = null;

        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (sessionId != null) {
            // Build the message and hand off to the handler
            request = new RailSellRequest();
            request.groupId = groupId;
            request.bucket = bucket;
            request.creditCardId = creditCardId;
            request.deliveryOption = deliveryOption;
            request.violationCode = violationCode;
            request.violationJustification = violationJustification;
            request.fields = fields;

            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_RAIL_SELL_REQUEST, request);
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Will perform a search for hotels based on check-in/out dates, hotel chain and location.
     * 
     * Upon search completion, a broadcast will be sent with action 'Const.ACTION_HOTEL_SEARCH_RESULTS' indicating the search has
     * completed. The results of the search, a 'HotelSearchReply' object will be set on the application instance, i.e.,
     * 'ConcurCore'.
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
     *            the number of results to return from the cached results on the server.
     */
    public HotelSearchRequest searchForHotels(Calendar dateEnd, Calendar dateStart, String hotelChain, String lat,
            String lon, String radius, String scale, Integer startIndex, Integer count) {

        HotelSearchRequest request = null;
        String msgId = Long.toString(System.currentTimeMillis());
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            // Build the message and hand off to the handler
            request = new HotelSearchRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            request.dateEnd = dateEnd;
            request.dateStart = dateStart;
            request.hotelChain = hotelChain;
            request.lat = lat;
            request.lon = lon;
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
     * Will send a request to confirm a hotel room.
     * 
     * Upon completion, a broadcast will be sent with action 'Const.ACTION_HOTEL_CONFIRM_RESULTS' indicating the confirm has
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
     */
    public HotelConfirmRequest sendConfirmHotelRoomRequest(String bicCode, String ccId, String chainCode,
            String propertyId, String propertyName, String sellSource, String tripId, String hotelReason,
            String hotelReasonCode, List<TravelCustomField> fields, boolean redeemTravelPoints) {

        HotelConfirmRequest request = null;
        String msgId = Long.toString(System.currentTimeMillis());
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new HotelConfirmRequest();
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
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            request.redeemTravelPoints = redeemTravelPoints;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_HOTEL_CONFIRM_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Gets hotel property details based on a property id.
     * 
     * Upon completion, a broadcast will be sent with action 'Const.ACTION_HOTEL_DETAIL_RESULTS' indicating the request has
     * completed. The results of the request, a 'HotelChoiceDetail' object will be set on the application instance, i.e.,
     * 'ConcurCore'.
     * 
     * @param propertyId
     *            the hotel property id.
     */
    public HotelDetailRequest getHotelDetails(String propertyId) {

        HotelDetailRequest request = null;
        String msgId = Long.toString(System.currentTimeMillis());
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new HotelDetailRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.propertyId = propertyId;
            request.messageId = msgId;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_HOTEL_DETAIL_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Gets hotel images based on a property id and gds type.
     * 
     * @param propertyId
     *            the hotel property id.
     */
    public HotelImagesRequest getHotelImages(int gdsType, String propertyId) {

        HotelImagesRequest request = null;
        String msgId = Long.toString(System.currentTimeMillis());
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            request = new HotelImagesRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.gdsType = gdsType;
            request.propertyId = propertyId;
            request.messageId = msgId;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_HOTEL_IMAGES_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    public void searchLocations(String searchText) {
        searchLocations(searchText, false);
    }

    public void searchLocations(String searchText, boolean airportsOnly) {

        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (sessionId != null) {
            // Build the message and hand off to the handler
            LocationSearchRequest request = new LocationSearchRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            request.searchText = searchText;
            request.airportsOnly = airportsOnly;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_LOCATION_SEARCH_REQUEST, request);
            handler.sendMessage(msg);
        }
    }

    // ///////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////
    //
    //
    // ///////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Gets the user configuration object for the user with <code>userId</code>.
     * 
     * // Expense-related endpoints
     * 
     * @param userId
     *            the user id.
     * @return the <code>UserConfig</code> object for the user with <code>userId</code>.
     */
    public UserConfig getUserConfig(String userId) {

        UserConfig uc = null;
        String cachedResponse = db.loadResponse(Const.MSG_USER_CONFIG_REQUEST, userId);
        if (cachedResponse != null) {
            try {
                UserConfigReply ucReply = UserConfigReply.parseXMLReply(cachedResponse);
                uc = ucReply.config;
                Calendar lastResponseTimeTS = db.getReponseLastRetrieveTS(Const.MSG_USER_CONFIG_REQUEST, userId);
                ((ConcurCore) getApplication()).setUserConfigLastRetrieved(lastResponseTimeTS);
            } catch (RuntimeException rte) {
                Log.e(Const.LOG_TAG,
                        CLS_TAG + ".getUserConfig: Exception parsing cached response - " + rte.getMessage() + ".", rte);
            }
        }
        return uc;
    }

    /**
     * Sends a request to obtain the latest user configuration information for the user
     * 
     * @param userId
     *            the user id.
     * @param hash
     *            the hash code of the current user configuration information. Can be <code>null</code> reload all user
     *            configuration information.
     */
    public void sendUserConfigRequest(String userId, String hash) {

        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {

            UserConfigRequest request = new UserConfigRequest();

            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.hash = hash;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_USER_CONFIG_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
    }

    /**
     * Gets the system configuration object for the company associated with user <code>userId</code>.
     * 
     * @param userId
     *            the user id.
     * @return the <code>SystemConfig</code> object for the company associated with the user <code>userId</code>.
     */
    public SystemConfig getSystemConfig(String userId) {

        SystemConfig sysConfig = null;
        String cachedResponse = db.loadResponse(Const.MSG_SYSTEM_CONFIG_REQUEST, userId);
        if (cachedResponse != null) {
            try {
                sysConfig = SystemConfig.parseSystemConfigXml(cachedResponse);
                Calendar lastResponseTimeTS = db.getReponseLastRetrieveTS(Const.MSG_SYSTEM_CONFIG_REQUEST, userId);
                ((ConcurCore) getApplication()).setSystemConfigLastRetrieved(lastResponseTimeTS);
            } catch (IOException ioExc) {
                Log.e(Const.LOG_TAG,
                        CLS_TAG + ".getSystemConfig: I/O exception parsing cached response - " + ioExc.getMessage()
                                + ".", ioExc);
            }
        }
        return sysConfig;
    }

    /**
     * Sends a request to obtain the latest system configuration information for the company associated with <code>userId</code>.
     * 
     * @param userId
     *            the user id.
     * @param hash
     *            the hash code of the current system configuration information. Can be <code>null</code> to retrieve full system
     *            configuration information.
     */
    public void sendSystemConfigRequest(String userId, String hash) {

        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {

            SystemConfigRequest request = new SystemConfigRequest();

            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.hash = hash;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_SYSTEM_CONFIG_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
    }

    /**
     * Will send a search request to query for a list of attendees.
     * 
     * @param userId
     *            the user id of the logged in user.
     * @param query
     *            the query.
     * @param excAtnKeys
     *            the list of attendee keys that should be excluded from the list results.
     * @return an instance of <code>AttendeesSearchRequest</code> that can be used to cancel the request.
     */
    public AttendeeSearchRequest sendAttendeesSearchRequest(String userId, String query, List<String> excAtnKeys) {
        AttendeeSearchRequest request = null;

        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            // Set up the request.
            request = new AttendeeSearchRequest();
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.query = query;
            request.excAtnKeys = excAtnKeys;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_ATTENDEE_SEARCH_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }

        return request;
    }

    public ExtendedAttendeeSearchRequest sendExtendedAttendeesSearchRequest(String userId, String atnTypeKey,
            List<ExpenseReportFormField> formFields, List<String> excAtnKeys, String expKey, String rptPolKey,
            String rptEntKey) {
        ExtendedAttendeeSearchRequest request = null;

        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            // Set up the request.
            request = new ExtendedAttendeeSearchRequest();
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.atnTypeKey = atnTypeKey;
            request.formFields = formFields;
            request.excAtnKeys = excAtnKeys;
            request.expKey = expKey;
            request.rptPolKey = rptPolKey;
            request.rptEntKey = rptEntKey;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_EXTENDED_ATTENDEE_SEARCH_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Will send a search request to query for a list of approvers.
     * 
     * @param userId
     *            the user id of the logged in user.
     * @param rptKey
     *            the key of the report being sent for approval.
     * @param fieldName
     *            any one of <code>ApproverSearchRequest.FIELD_*</code>.
     * @param query
     *            the search query
     * 
     * @return an instance of <code>ApproverSearchRequest</code> that can be used to cancel the request.
     */
    public ApproverSearchRequest sendApproverSearchRequest(String userId, String rptKey, String fieldName, String query) {
        ApproverSearchRequest request = null;

        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            // Set up the request.
            request = new ApproverSearchRequest();
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.rptKey = rptKey;
            request.fieldName = fieldName;
            request.query = query;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_APPROVER_SEARCH_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Will send a request to save attendee information.
     * 
     * @param userId
     *            the user id.
     * @param attendee
     *            the attendee being saved.
     * @return an instance of <code>AttendeeSaveRequest</code> that can be used to cancel the request; otherwise,
     *         <code>null</code> is returned.
     */
    public AttendeeSaveRequest sendAttendeeSaveRequest(String userId, ExpenseReportAttendee attendee) {
        return sendAttendeeSaveRequest(userId, attendee, false);
    }

    /**
     * Will send a request to save attendee information, optionally allowing for a forced save of a duplicate
     * 
     * @param userId
     *            the user id.
     * @param attendee
     *            the attendee being saved.
     * @return an instance of <code>AttendeeSaveRequest</code> that can be used to cancel the request; otherwise,
     *         <code>null</code> is returned.
     */
    public AttendeeSaveRequest sendAttendeeSaveRequest(String userId, ExpenseReportAttendee attendee, boolean force) {
        AttendeeSaveRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            // Set up the request.
            request = new AttendeeSaveRequest();
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.attendee = attendee;
            request.force = force;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_ATTENDEE_SAVE_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to obtain a list of attendee types.
     * 
     * @param userId
     *            the user id associated with the request.
     * @param ftCode
     *            contains the ftcode for the search. For attendee search, the value should be <code>ATNSEARCH</code> For attendee
     *            add, the value can be left <code>null</code>.
     * @return an instance of <code>SearchListRequest</code> that can be used to cancel the request.
     */
    public SearchListRequest sendAttendeeTypeSearchRequest(String userId, String ftCode) {
        SearchListRequest request = null;

        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            // Set up the request.
            request = new SearchListRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.fieldId = "AtnTypeKey";
            request.ftCode = ftCode;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_GET_ATTENDEE_TYPES_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to obtain a list of attendee search fields for various attendee types.
     * 
     * @param userId
     *            the user id associated with the request.
     * @return an instance of <code>AttendeeSearchFieldsRequest</code> that can be used to cancel the request.
     */
    public AttendeeSearchFieldsRequest sendAttendeeSearchFieldsRequest(String userId) {
        AttendeeSearchFieldsRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            // Set up the request.
            request = new AttendeeSearchFieldsRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_ATTENDEE_SEARCH_FIELDS_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to obtain a form to support editing for a specific attendee.
     * 
     * @param userId
     *            the current mobile user id.
     * @param atnTypeKey
     *            the attendee type key.
     * @param atnKey
     *            the attendee key.
     * @return an instance of <code>AttendeeFormRequest</code> that can be used to cancel the request.
     */
    public AttendeeFormRequest sendAttendeeFormRequest(String userId, String atnTypeKey, String atnKey) {
        AttendeeFormRequest request = null;

        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {

            request = new AttendeeFormRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.atnTypeKey = atnTypeKey;
            request.atnKey = atnKey;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_ATTENDEE_FORM_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to obtain default attendee information representing the current mobile user.
     * 
     * @param userId
     *            the mobile user id.
     * @return an instance of <code>DefaultAttendeeRequest</code> that can be used to cancel the request. will return
     *         <code>null</code> if the request can not be made.
     */
    public DefaultAttendeeRequest sendDefaultAttendeeRequest(String userId) {
        DefaultAttendeeRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            request = new DefaultAttendeeRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_DEFAULT_ATTENDEE_REQUEST, request);
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
     * @param isMRU
     *            whether MRU's should be included at the top of the results.
     * @param fieldId
     *            the field id.
     * @param ftCode
     *            the ft code.
     * @param listKey
     *            the list key.
     * @param parentLiKey
     *            the parent Li key.
     * @param reportKey
     *            the report key.
     * @return an instance of <code>SearchListRequest</code> if the request was made; <code>null</code> otherwise.
     */
    public SearchListRequest sendSearchListRequest(String userId, String query, boolean isMRU, String fieldId,
            String ftCode, String listKey, String parentLiKey, String reportKey, String searchBy) {
        SearchListRequest request = null;

        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {

            request = new SearchListRequest();
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.query = query;
            request.isMRU = isMRU;
            request.fieldId = fieldId;
            request.ftCode = ftCode;
            request.listKey = listKey;
            request.parentLiKey = parentLiKey;
            request.reportKey = reportKey;
            request.searchBy = searchBy;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_SEARCH_LIST_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a search for the given currency <code>query</code>
     * 
     * @param userId
     *            the user id.
     * @param query
     *            the search query.
     * @param fieldId
     *            the field id.
     * @param reportKey
     *            the report key.
     * 
     * @return an instance of <code>SearchListRequest</code> if the request was made; <code>null</code> otherwise.
     */
    public SearchListRequest sendCurrencySearchRequest(String userId, String query, String fieldId, String reportKey) {
        SearchListRequest request = null;

        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {

            request = new SearchListRequest();
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.query = query;
            request.isMRU = false;
            request.fieldId = fieldId;
            request.ftCode = null;
            request.listKey = null;
            request.parentLiKey = null;
            request.reportKey = reportKey;
            request.searchBy = null;
            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_CURRENCY_SEARCH_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    } // sendCurrencySearchRequest()

    /**
     * Will retrieve from persistence the last reply from a corp SSO query request.
     * 
     * @return an instance of <code>CorpSsoQueryReply</code> containing response information.
     */
    public CorpSsoQueryReply getCorpSsoQueryReply() {
        CorpSsoQueryReply retVal = null;
        String replyXml = db.loadResponse(Const.MSG_SSO_QUERY_REQUEST, Const.SSO_USER_ID);
        if (replyXml != null && replyXml.length() > 0) {
            retVal = CorpSsoQueryReply.parseXMLReply(replyXml);
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            ConcurCore.setCorpSsoQueryReply(retVal);
        }
        return retVal;
    }

    /**
     * Will purge from persistance the last reply from a corp SSO query request.
     */
    public void clearCorpSSoQueryReply() {
        db.deleteResponse(Const.MSG_SSO_QUERY_REQUEST, Const.SSO_USER_ID);
    }

    /**
     * Will send a request to query SSO information based on a company code.
     * 
     * @param companyCode
     *            the company code.
     * @return an instance of <code>CorpSsoQueryRequest</code> that may be used to cancel the request.
     */
    public CorpSsoQueryRequest sendCorpSsoQueryRequest(String companyCode) {

        // Set the message up
        CorpSsoQueryRequest request = new CorpSsoQueryRequest();
        request.messageId = Long.toString(System.currentTimeMillis());
        request.companyCode = companyCode;
        // This is a symbolic user id used in persisting the results of the
        // CorpSsoQuery response.
        request.userId = Const.SSO_USER_ID;
        Message msg = handler.obtainMessage(Const.MSG_SSO_QUERY_REQUEST, request);

        // Hand off to the handler thread
        handler.sendMessage(msg);
        return request;
    }

    /**
     * Sends a request to mark the receipts viewed for the given report
     */
    public MarkReceiptsViewedRequest sendReceiptsViewedRequest(String rptKey) {

        MarkReceiptsViewedRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {

            request = new MarkReceiptsViewedRequest();

            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.rptKey = rptKey;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_MARK_RECEIPTS_VIEWED_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Sends a request to the server indicating a report entry receipt has been viewed.
     * 
     * <br>
     * <b>NOTE:</b><br>
     * This method is intended to be used when an approver views a line item receipt.
     * 
     * @param rpeKey
     *            the report entry receipt.
     * @return an instance of <code>MarkEntryReceiptViewedRequest</code>.
     */
    public MarkEntryReceiptViewedRequest sendEntryReceiptViewedRequest(String rpeKey) {

        MarkEntryReceiptViewedRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {

            request = new MarkEntryReceiptViewedRequest();

            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.rpeKey = rpeKey;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_MARK_ENTRY_RECEIPT_VIEWED_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Retrieve the locally cached set of active reports.
     * 
     * <b>NOTE:</b> This method will, prior to returning, set the list of active reports on the instance of
     * <code>ConcurCore</code>. After this method returns, clients should call <code>ConcurCore.getActiveReports</code> to
     * retrieve the list.
     */
    public IExpenseReportListInfo getActiveReports(String userId) {
        IExpenseReportListInfo reportListInfo = null;
        List<IExpenseReportInfo> reportInfos = ReportDBUtil.loadReports(db, IExpenseReportInfo.ReportType.ACTIVE,
                userId, false);
        if (reportInfos != null && reportInfos.size() > 0) {
            // The above active report list is the summary list, i.e.,
            // non-detail, and so are not edited.
            // Use the first report's update time as the time for the list, they
            // all have the same update time.
            reportListInfo = new ExpenseReportListInfo(reportInfos, reportInfos.get(0).getUpdateTime());
        }
        return reportListInfo;
    }

    /**
     * Will send a request to the server to retrieve the latest set of active reports.
     * 
     * When complete an {@link Intent} will be broadcast with the action of Const.ACTION_EXPENSE_ACTIVE_REPORTS_UPDATED
     */
    public ActiveReportsRequest sendActiveReportsRequest() {

        ActiveReportsRequest request = null;

        String sessionId = Preferences.getSessionId();

        if (sessionId != null) {
            request = new ActiveReportsRequest();

            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_ACTIVE_REPORTS_REQUEST, request);

            // Hand off to the handler thread
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
     * @return an instance of <code>AirSellRequest</code> that can be cancelled.
     */
    public AirSellRequest sendAirSellRequest(String userId, int ccId, String fareId, String programId,
            boolean refundableOnly, String tripName, String violationCode, String violationJustification,
            List<TravelCustomField> fields, List<SellOptionField> preSellOptionFields, String cvvNumber,
            boolean hasSellOptions, boolean redeemTravelPoints) {
        AirSellRequest request = null;

        String sessionId = Preferences.getSessionId();

        if (sessionId != null) {
            request = new AirSellRequest();
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.ccId = ccId;
            request.fareId = fareId;
            request.programId = programId;
            request.refundableOnly = refundableOnly;
            request.tripName = tripName;
            request.violationCode = violationCode;
            request.violationJustification = violationJustification;
            request.fields = fields;
            request.preSellOptionFields = preSellOptionFields;
            request.cvvNumber = cvvNumber;
            request.hasSellOptions = hasSellOptions;
            request.redeemTravelPoints = redeemTravelPoints;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_AIR_SELL_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Will send a request to the server to cancel an air booking.
     * 
     * @param userId
     *            the id of the user performing the action.
     * @param recordLocator
     *            the record locator of the booking to cancel.
     * @param comment
     *            an optional comment.
     * @return an instance of <code>AirCancelRequest</code> that may be used to cancel the request.
     */
    public AirCancelRequest sendAirCancelRequest(String userId, String recordLocator, String comment) {
        AirCancelRequest request = null;

        String sessionId = Preferences.getSessionId();

        if (sessionId != null) {
            request = new AirCancelRequest();
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.recordLocator = recordLocator;
            request.comment = comment;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_AIR_CANCEL_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Will send a request to the server to remove an expense from an expense report.
     * 
     * @param userId
     *            the current user id.
     * @param reportKey
     *            the report key from the report is being generated.
     * @param expenseEntryKeys
     *            the list of report entry keys.
     */
    public RemoveReportExpenseRequest sendRemoveReportExpenseRequest(String userId, String reportKey,
            ArrayList<String> expenseEntryKeys) {

        RemoveReportExpenseRequest request = null;
        // Init the message body and ID
        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            request = new RemoveReportExpenseRequest();
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            request.reportKey = reportKey;
            request.expenseEntryKeys = expenseEntryKeys;

            request.buildRequestBody();

            // if (repostEnabled) {
            // // Persist the request
            // if (!db.insertHTTPRequest(userId, "POST", RemoveReportExpenseRequest.SERVICE_END_POINT,
            // request.requestBody, Const.MSG_EXPENSE_REMOVE_REPORT_EXPENSE_REQUEST, msgId, null)) {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".sendRemoveReportExpenseRequest: failed to save request body!");
            // }
            // }

            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_REMOVE_REPORT_EXPENSE_REQUEST, request);
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Will initiate a request to obtain a list of reports to approve.
     * 
     * When complete, an {@link Intent} will be broadcast with the action of Const.ACTION_EXPENSE_APPROVAL_REPORTS_UPDATED
     */
    public IExpenseReportListInfo getReportsToApproveList(String userId) {
        IExpenseReportListInfo reportListInfo = null;
        List<IExpenseReportInfo> reportInfos = ReportDBUtil.loadReports(db, IExpenseReportInfo.ReportType.APPROVAL,
                userId, false);
        if (reportInfos != null && reportInfos.size() > 0) {
            // The above approval report list is the summary list, i.e.,
            // non-detail, and so are not edited.
            // Use the first report's update time as the time for the list, they
            // all have the same update time.
            reportListInfo = new ExpenseReportListInfo(reportInfos, reportInfos.get(0).getUpdateTime());
        }
        return reportListInfo;
    }

    /**
     * Will load into a detailed report entry into cache.
     * 
     * @param rptKey
     *            the report key.
     * @param rptEntKey
     *            the report entry key.
     * @param type
     *            the report type.
     * @param userId
     *            the user id.
     * @return <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean loadReportEntryIntoCache(String rptKey, String rptEntKey, ReportType type, String userId,
            boolean detail) {
        boolean retVal = false;

        ConcurCore ConcurCore = (ConcurCore) getApplication();
        IExpenseReportCache expRepCache = null;
        switch (type) {
        case ACTIVE: {
            expRepCache = ConcurCore.getExpenseActiveCache();
            break;
        }
        case APPROVAL: {
            expRepCache = ConcurCore.getExpenseApprovalCache();
            break;
        }
        }
        ExpenseReport expRep = expRepCache.getReportDetail(rptKey);
        if (expRep != null) {
            // Grab a reference to the expense entry 'rptEntKey'in 'expRep'.
            ExpenseReportEntry expRepEnt = expRep.findEntryByReportKey(rptEntKey);
            if (expRepEnt != null) {
                // First punt, 'expRepEnt' from the reports expense entry map.
                expRep.reportExpenseEntryMap.remove(expRepEnt.reportEntryKey);
                // Second, if 'expRepEnt' is detailed entry and is itemized,
                // ensure any itemization expense entries
                // are also removed from the entry map.
                if (expRepEnt instanceof ExpenseReportEntryDetail) {
                    ExpenseReportEntryDetail expRepEntDet = (ExpenseReportEntryDetail) expRepEnt;
                    if (expRepEntDet.getItemizations() != null && expRepEntDet.getItemizations().size() > 0) {
                        for (ExpenseReportEntry itemExpRepEnt : expRepEntDet.getItemizations()) {
                            expRep.reportExpenseEntryMap.remove(itemExpRepEnt.reportEntryKey);
                        }
                    }
                }
            }
            // Load the expense report entry info object from persistence.
            IExpenseReportEntryInfo expRepEntInfo = ReportDBUtil.loadReportEntry(db, type, rptKey, rptEntKey, userId,
                    detail, expRep.reportExpenseEntryMap);
            if (expRepEntInfo != null && expRepEntInfo.getEntry() != null) {
                // Add the top-level expense report entry to the 'expRep'. If
                // the entry is a detailed entry with itemization, then
                // in the above call to 'loadReportEntry', the re-parsing of the
                // detailed report entry object will add any
                // itemizations
                // to the 'expRep.reportExpenseEntryMap'.
                expRep.replaceOrAddReportEntry(expRepEntInfo.getEntry());
                retVal = true;
            }
        }
        if (retVal) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".loadReportEntryIntoCache: successfully loaded "
                    + ((detail) ? "detailed" : "non-detailed") + " report entry into cache.");
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".loadReportEntryIntoCache: un-successfully loaded "
                    + ((detail) ? "detailed" : "non-detailed") + " report entry into cache.");
        }
        return retVal;
    }

    /**
     * Will obtain a list of report infos containing the built reports. <br>
     * <b>NOTE:</b><br>
     * Passing in a value of <code>true</code> for <code>detail</code> will load a complete set of all detailed reports into
     * memory! This can be expensive. While this API supports this, clients are discouraged from doing this! <br>
     * 
     * @param userId
     *            the user id associated with the reports.
     * @param type
     *            the report type.
     * @param detail
     *            whether to retrieve detailed reports.
     * @return an instance of <code>IExpenseReportListInfo</code> containing the report info objects.
     */
    public IExpenseReportListInfo getReports(String userId, ReportType type, boolean detail) {
        IExpenseReportListInfo reportListInfo = null;
        List<IExpenseReportInfo> reportInfos = ReportDBUtil.loadReports(db, type, userId, detail);
        if (reportInfos != null && reportInfos.size() > 0) {
            Calendar updateTimeTS = null;
            if (detail) {
                // Use the earliest timestamp among the report infos.
                for (IExpenseReportInfo reportInfo : reportInfos) {
                    if (reportInfo.getUpdateTime() != null) {
                        if (updateTimeTS == null) {
                            updateTimeTS = reportInfo.getUpdateTime();
                        } else {
                            updateTimeTS = (reportInfo.getUpdateTime().before(updateTimeTS)) ? reportInfo
                                    .getUpdateTime() : updateTimeTS;
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".getReports: report info has a null update time!");
                    }
                }
            } else {
                // Use the update time of the first report info. Non-detailed
                // items all currently have the same
                // update time.
                if (reportInfos.get(0).getUpdateTime() != null) {
                    updateTimeTS = reportInfos.get(0).getUpdateTime();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getReports: report info has a null update time!");
                }
            }
            reportListInfo = new ExpenseReportListInfo(reportInfos, updateTimeTS);
        }
        return reportListInfo;
    }

    /**
     * Will send a background request off to the server to refresh the list of reports to approve.
     * 
     * When complete, an {@link Intent} will be broadcast with the action of Const.ACTION_EXPENSE_APPROVAL_REPORTS_UPDATED.
     */
    public ReportsToApproveRequest sendReportsToApproveRequest(String userId) {

        ReportsToApproveRequest request = null;

        // Obtain the current session ID.
        String sessionId = Preferences.getSessionId();

        if (sessionId != null) {
            // Set the message up.
            request = new ReportsToApproveRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = Long.toString(System.currentTimeMillis());
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_REPORT_APPROVAL_LIST_REQUEST, request);

            // Hand off to the handler thread.
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Gets the list of <code>ReceiptShareItem</code> objects representing receipts to be shared with the ReceiptStore.
     * 
     * @return returns a list of <code>ReceiptShareItem</code> objects representing receipts to be shared with the ReceiptStore.
     */
    public List<ReceiptShareItem> getReceiptShareList() {
        List<ReceiptShareItem> receiptShares = null;
        receiptShares = db.loadReceiptShareItems();
        return receiptShares;
    }

    /**
     * Adds a list of <code>ReceiptShareItem</code> objects representing receipt to be shared with the ReceiptStore.
     * 
     * @param receiptShares
     *            contains a list of <code>ReceiptShareItem</code> objects representing receipts to be shared with the
     *            ReceiptStore.
     */
    public void addReceiptShares(List<ReceiptShareItem> receiptShares) {
        db.insertReceiptShareItems(receiptShares);
    }

    /**
     * Removes a list of <code>ReceiptShareItem</code> objects representing receipts to be shared with the ReceiptStore.
     * 
     * @param receiptShares
     *            contains a list of <code>ReceiptShareItem</code> objects representing receipts to be shared with the
     *            ReceiptStore.
     */
    public void removeReceiptShares(List<ReceiptShareItem> receiptShares) {
        db.deleteReceiptShareItems(receiptShares);
    }

    /**
     * Gets the list of receipt info objects associated with a user.
     * 
     * @param userId
     *            the user id.
     * @return the list of receipt info objects.
     */
    public List<ReceiptInfo> getReceiptInfos(String userId) {
        List<ReceiptInfo> receiptInfos = null;
        // Get the saved data from the new ReceiptList ContentProvider.
        // Filter out OCR/ExpenseIt receipts.
        List<ReceiptDAO> receiptList = ReceiptListUtil.getReceiptList(ConcurCore.getContext(), userId, true);
        receiptInfos = ReceiptDAOConverter.convertReceiptDAOToReceiptInfo(receiptList);

        return receiptInfos;
    }

    /**
     * Will get an instance of <code>IExpenseReportDetailInfo</code> for the report key <code>reportKey</code> in the local cache.
     * 
     * @param reportKey
     *            the expense report detail report key.
     * @param type
     *            the report type.
     * 
     * @return an instance of <code>IExpenseReportDetailInfo</code> for the report key <code>reportKey</code>.
     */
    public IExpenseReportInfo getReportDetail(String reportKey, ReportType type) {

        IExpenseReportInfo reportInfo = null;
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        reportInfo = ReportDBUtil.loadReport(db, type, reportKey, userId, true);
        return reportInfo;
    }

    /**
     * Will retrieve the list of <code>IExpenseReportDetailInfo</code> objects for the user <code>userId</code>. <br>
     * <b>NOTE:</b><br>
     * This method will only retrieve the report key and the update time, it will not return a built report detail object within
     * the <code>IExpenseReportDetailInfo</code> objects.
     * 
     * @param userId
     *            the user id.
     * @param type
     *            the type of report.
     * @return a list of <code>IExpenseReportDetailInfo</code> objects containing only report key
     */
    public List<IExpenseReportInfo> getReportDetailInfos(String userId, ReportType type) {
        List<IExpenseReportInfo> reportInfos = null;
        reportInfos = ReportDBUtil.loadReportInfos(db, type, userId, true);
        return reportInfos;
    }

    /**
     * Will initiate a request to obtain a report that contains a detailed header, but summary objects. Subsequent requests to
     * retrieve detailed entry objects can be made via the <code>sendReportEntryDetailRequest</code> method.
     * 
     * When complete, an {@link Intent} will be broadcast with the action of Const.ACTION_EXPENSE_REPORT_DETAIL_UPDATED.
     * 
     * @param reportKey
     *            the report key.
     * @param reportSourceKey
     *            the report source key, one of <code>Const.EXPENSE_REPORT_SOURCE_ACTIVE</code> or
     *            <code>Const.EXPENSE_REPORT_SOURCE_APPROVAL</code>.
     */

    public ReportDetailRequest sendReportDetailSummaryRequest(String reportKey, int reportSourceKey) {

        ReportDetailRequest request = null;

        // Obtain the current session ID.
        String sessionId = Preferences.getSessionId();

        if (sessionId != null) {
            // Set that a request is pending.
            if (reportSourceKey == Const.EXPENSE_REPORT_SOURCE_ACTIVE
                    || reportSourceKey == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {

                // Set the message up.
                request = new ReportDetailRequest();
                request.version = ReportDetailRequest.Version.V3;
                request.userId = prefs.getString(Const.PREF_USER_ID, null);
                request.sessionId = sessionId;
                request.reportKey = reportKey;
                request.messageId = Long.toString(System.currentTimeMillis());
                request.reportSourceKey = reportSourceKey;
                Message msg = handler.obtainMessage(Const.MSG_EXPENSE_REPORT_DETAIL_REQUEST, request);
                // Hand off the message for processing.
                handler.sendMessage(msg);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendReportDetailSummaryRequest: invalid report source key '"
                        + reportSourceKey + "'.");
            }
        }
        return request;
    }

    /**
     * Will initiate a request to obtain details of a report header.
     * 
     * When complete, an {@link Intent} will be broadcast with the action of Const.ACTION_EXPENSE_REPORT_DETAIL_UPDATED.
     * 
     * @param reportKey
     *            the report key.
     * @param reportSourceKey
     *            the report source key, one of <code>Const.EXPENSE_REPORT_SOURCE_ACTIVE</code> or
     *            <code>Const.EXPENSE_REPORT_SOURCE_APPROVAL</code>.
     */
    public ReportHeaderDetailRequest sendReportHeaderDetailRequest(String reportKey, int reportSourceKey) {

        ReportHeaderDetailRequest request = null;

        // Obtain the current session ID.
        String sessionId = Preferences.getSessionId();

        if (sessionId != null) {
            // Set that a request is pending.
            if (reportSourceKey == Const.EXPENSE_REPORT_SOURCE_ACTIVE
                    || reportSourceKey == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {
                // Set the message up.
                request = new ReportHeaderDetailRequest();
                request.userId = prefs.getString(Const.PREF_USER_ID, null);
                request.sessionId = sessionId;
                request.reportKey = reportKey;
                request.messageId = Long.toString(System.currentTimeMillis());
                request.reportSourceKey = reportSourceKey;
                Message msg = handler.obtainMessage(Const.MSG_EXPENSE_REPORT_HEADER_DETAIL_REQUEST, request);
                // Hand off the message for processing.
                handler.sendMessage(msg);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendReportHeaderDetailRequest: invalid report source key '"
                        + reportSourceKey + "'.");
            }
        }
        return request;
    }

    /**
     * Will initiate a request to obtain details of a report entry.
     * 
     * When complete, an {@link Intent} will be broadcast with the action of Const.ACTION_EXPENSE_REPORT_ENTRY_DETAIL_UPDATED.
     * 
     * @param reportEntryKey
     *            the report entry key.
     * @param reportSourceKey
     *            the report source key, one of <code>Const.EXPENSE_REPORT_SOURCE_ACTIVE</code> or
     *            <code>Const.EXPENSE_REPORT_SOURCE_APPROVAL</code>.
     */
    public ReportEntryDetailRequest sendReportEntryDetailRequest(String reportKey, String reportEntryKey,
            int reportSourceKey) {
        ReportEntryDetailRequest request = null;

        // Obtain the current session ID.
        String sessionId = Preferences.getSessionId();

        if (sessionId != null) {
            // Set that a request is pending.
            if (reportSourceKey == Const.EXPENSE_REPORT_SOURCE_ACTIVE
                    || reportSourceKey == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {

                // Set the message up.
                request = new ReportEntryDetailRequest();
                request.userId = prefs.getString(Const.PREF_USER_ID, null);
                request.sessionId = sessionId;
                request.reportKey = reportKey;
                request.reportEntryKey = reportEntryKey;
                request.messageId = Long.toString(System.currentTimeMillis());
                request.reportSourceKey = reportSourceKey;
                Message msg = handler.obtainMessage(Const.MSG_EXPENSE_REPORT_ENTRY_DETAIL_REQUEST, request);
                // Hand off the message for processing.
                handler.sendMessage(msg);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendReportEntryDetailRequest: invalid report source key '"
                        + reportSourceKey + "'.");
            }
        }
        return request;
    }

    /**
     * Will initiate a request to obtain details of a report.
     * 
     * When complete, an {@link Intent} will be broadcast with the action of Const.ACTION_EXPENSE_REPORT_DETAIL_UPDATED.
     * 
     * @param reportKey
     *            the report key.
     * @param reportSourceKey
     *            the report source key, one of <code>Const.EXPENSE_REPORT_SOURCE_ACTIVE</code> or
     *            <code>Const.EXPENSE_REPORT_SOURCE_APPROVAL</code>.
     */
    public ReportDetailRequest sendReportDetailRequest(String reportKey, int reportSourceKey) {

        ReportDetailRequest request = null;

        // Obtain the current session ID.
        String sessionId = Preferences.getSessionId();

        if (sessionId != null) {
            // Set that a request is pending.
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            IExpenseReportCache expRepCache = null;
            if (reportSourceKey == Const.EXPENSE_REPORT_SOURCE_ACTIVE
                    || reportSourceKey == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {
                switch (reportSourceKey) {
                case Const.EXPENSE_REPORT_SOURCE_ACTIVE:
                    expRepCache = ConcurCore.getExpenseActiveCache();
                    break;
                case Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL:
                    expRepCache = ConcurCore.getExpenseApprovalCache();
                    break;
                }
                expRepCache.setDetailedReportRequestPending(true);

                // Set the message up.
                request = new ReportDetailRequest();
                request.sessionId = sessionId;
                request.userId = prefs.getString(Const.PREF_USER_ID, null);
                request.reportKey = reportKey;
                request.messageId = Long.toString(System.currentTimeMillis());
                request.reportSourceKey = reportSourceKey;
                Message msg = handler.obtainMessage(Const.MSG_EXPENSE_REPORT_DETAIL_REQUEST, request);
                // Hand off the message for processing.
                handler.sendMessage(msg);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendReportDetailRequest: invalid report source key '"
                        + reportSourceKey + "'.");
            }
        }
        return request;
    }

    /**
     * Send a request to get the entry form (ReportEntryDetail)
     * 
     * @param expKey
     *            the expense type key (EXP_KEY)
     * @param reportKey
     *            the report key
     * @param entryKey
     *            the report entry key
     */
    public ReportEntryFormRequest sendReportEntryFormRequest(String expKey, String reportKey, String entryKey) {
        ReportEntryFormRequest request = null;

        // Obtain the current session ID.
        String sessionId = Preferences.getSessionId();

        if (sessionId != null) {

            if (expKey != null) {
                // Clear out any previous form cached for entry creation
                ConcurCore app = (ConcurCore) getApplication();
                app.setCurrentEntryDetailForm(null);

                // Set the message up.
                request = new ReportEntryFormRequest();
                request.userId = prefs.getString(Const.PREF_USER_ID, null);
                request.sessionId = sessionId;
                request.expKey = expKey;
                request.reportKey = reportKey;
                request.entryKey = entryKey;
                request.messageId = Long.toString(System.currentTimeMillis());
                Message msg = handler.obtainMessage(Const.MSG_EXPENSE_REPORT_ENTRY_FORM_REQUEST, request);

                // Hand off the message for processing.
                handler.sendMessage(msg);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendReportEntryFormRequest: invalid expense key");
            }
        }
        return request;
    }

    /**
     * Send a request to get the entry form (ReportEntryDetail)
     * 
     * @param expKey
     *            the expense type key (EXP_KEY)
     * @param reportKey
     *            the report key
     * @param entryKey
     *            the report entry key
     */
    public ReportItemizationEntryFormRequest sendReportItemizationEntryFormRequest(boolean withFormDef, String expKey,
            String reportKey, String parentEntryKey, String entryKey) {
        ReportItemizationEntryFormRequest request = null;

        // Obtain the current session ID.
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            if (expKey != null) {
                if (reportKey != null) {
                    if (parentEntryKey != null) {
                        // Clear out any previous form cached for entry creation
                        ConcurCore app = (ConcurCore) getApplication();
                        app.setCurrentEntryDetailForm(null);

                        // Set the message up.
                        request = new ReportItemizationEntryFormRequest();
                        request.userId = prefs.getString(Const.PREF_USER_ID, null);
                        request.sessionId = sessionId;
                        request.withFormDef = withFormDef;
                        request.expKey = expKey;
                        request.reportKey = reportKey;
                        request.parentEntryKey = parentEntryKey;
                        request.entryKey = entryKey;
                        request.messageId = Long.toString(System.currentTimeMillis());
                        Message msg = handler.obtainMessage(Const.MSG_EXPENSE_REPORT_ITEMIZATION_ENTRY_FORM_REQUEST,
                                request);

                        // Hand off the message for processing.
                        handler.sendMessage(msg);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".sendReportItemizationEntryFormRequest: invalid parent entry key");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".sendReportItemizationEntryFormRequest: invalid report key");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendReportItemizationEntryFormRequest: invalid expense key");
            }
        }
        return request;
    }

    /**
     * Will delete the XML representations of all the report data whose report keys are not contained within
     * <code>reportKeys</code>.
     * 
     * @param reportKeys
     *            the list of report keys whose data, if any, should be retained.
     * @param type
     *            the report type, i.e., active, approval, etc.
     * @param detail
     *            whether report detail data should be punted.
     * 
     * @return the number of report detail XML representations that were deleted.
     */
    public void deleteReportNotInReportKeyList(ArrayList<String> reportKeys, ReportType type, boolean detail) {
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        ReportDBUtil.deleteReports(db, type, userId, detail, reportKeys);
    }

    /**
     * Will delete the detailed report information from the persistent cache for the key <code>reportKey</code>.
     * 
     * @param reportKey
     *            the report key of the report to be removed.
     * @param type
     *            the report type.
     */
    public void deleteDetailReport(String reportKey, ReportType type) {
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        ReportDBUtil.deleteReport(db, type, reportKey, userId, true);
    }

    /**
     * Will delete the non-detailed report information from the persistence cache for the key <code>reportKey</code>.
     * 
     * @param reportKey
     *            the report key.
     * @param type
     *            the report type.
     */
    public void deleteReport(String reportKey, ReportType type) {
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        ReportDBUtil.deleteReport(db, type, reportKey, userId, false);
    }

    /**
     * Gets the persistent list of report keys for reports that have been submitted for approval (for reimbursement, not as an
     * approver) for which no reply has been retrieved from the server.
     * 
     * @return the list of submitted reports for approval upon which a reply from MWS is pending.
     */
    public ArrayList<String> getReportsSubmitted(String userId) {
        return db.getReportsSubmitted(userId);
    }

    /**
     * Gets the persistent list of report keys for reports that have been submitted for approval for which no reply has been
     * retrieved from the server.
     * 
     * @return the list of submitted reports for approval upon which a reply from MWS is pending.
     */
    public ArrayList<String> getReportsSubmittedApprove(String userId) {
        return db.getReportsSubmittedApprove(userId);
    }

    /**
     * Gets the persistent list of report keys for reports that have been submitted to the for rejection for which no reply has
     * been retrieved from the server.
     * 
     * @return the list of submitted reports for rejection upon which a reply from MWS is pending.
     */
    public ArrayList<String> getReportsSubmittedReject(String userId) {
        return db.getReportsSubmittedReject(userId);
    }

    /**
     * Will send off a report for submission to the approval process.
     * 
     * @param expRep
     *            the expense report to sent into the approval process.
     * @param userId
     *            the user id of the submitter.
     * @param approver
     *            an optional approver.
     */
    public SubmitReportRequest sendReportSubmit(ExpenseReport expRep, String userId, ExpenseReportApprover approver) {

        SubmitReportRequest request = null;

        String sessionId = Preferences.getSessionId();

        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            // if (repostEnabled) {
            // // Add the request to the persistent store.
            // if (!db.insertHTTPRequest(userId, "POST", SubmitReportRequest.SERVICE_END_POINT + "/"
            // + expRep.reportKey, null, Const.MSG_EXPENSE_REPORT_SUBMIT_REQUEST, msgId, expRep.reportKey)) {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".sendReportSubmit: failed to store HTTP request.");
            // }
            // }
            // Update the ExpenseReportCache.
            IExpenseReportCache expAppCache = ((ConcurCore) getApplication()).getExpenseActiveCache();
            expAppCache.addSubmitted(expRep.reportKey);
            // Construct the request and pass it off the handler.
            request = new SubmitReportRequest();
            request.sessionId = sessionId;
            request.reportKey = expRep.reportKey;
            request.userId = userId;
            request.messageId = msgId;

            // Handle selected approver.
            if (approver != null && approver.empKey != null) {
                request.approverEmpKey = approver.empKey;
            }

            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_REPORT_SUBMIT_REQUEST, request);
            // Hand off the message for processing.
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Will send a report off for approval.
     * 
     * @param expRep
     *            the expense report to be sent for approval.
     * @param comment
     *            the optional comment.
     * @param userId
     *            the current user id.
     * @param statKey
     *            the workflow status key (multiple approval action).
     */
    public ApproveReportRequest sendReportApprove(ExpenseReport expRep, String comment, String userId, String statKey) {

        ApproveReportRequest request = null;

        String sessionId = Preferences.getSessionId();

        if (sessionId != null) {
            long msgId = System.currentTimeMillis();

            if (expRep.isDetail()) {
                IExpenseReportCache expApprovalCache = ((ConcurCore) getApplication()).getExpenseApprovalCache();
                expRep = expApprovalCache.getReport(expRep.reportKey);
            }

            // Construct the request and pass it off the handler.
            request = new ApproveReportRequest();
            request.sessionId = sessionId;
            request.reportKey = expRep.reportKey;
            request.userId = userId;
            request.expRep = expRep;
            request.comment = comment;
            request.statKey = statKey;

            request.buildRequestBody();

            // Add the request to the persistent store.
            if (db.addReportSubmittedApprove(userId, expRep.reportKey, request.requestBody, msgId)) {
                // Update the ExpenseApprovalCache.
                IExpenseReportCache expAppCache = ((ConcurCore) getApplication()).getExpenseApprovalCache();
                expAppCache.addSubmittedForApprove(expRep.reportKey);

                request.messageId = Long.toString(msgId);
                Message msg = handler.obtainMessage(Const.MSG_EXPENSE_REPORT_APPROVE_REQUEST, request);
                // Hand off the message for processing.
                handler.sendMessage(msg);

            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendReportApprove: unable to persist request for report '"
                        + expRep.reportKey + "'.");
            }

        }

        return request;
    }

    /**
     * Will send a report off for rejection.
     * 
     * @param expRep
     *            the expense report to be sent for rejection.
     * @param comment
     *            the comment associated with the rejection.
     */
    public RejectReportRequest sendReportReject(ExpenseReport expRep, String comment, String userId) {

        RejectReportRequest request = null;

        String sessionId = Preferences.getSessionId();

        if (sessionId != null) {
            long msgId = System.currentTimeMillis();

            // Construct the request and pass it off the handler.
            request = new RejectReportRequest();
            request.userId = userId;
            request.sessionId = sessionId;
            request.reportKey = expRep.reportKey;
            request.expRep = expRep;
            request.comment = comment;

            request.buildRequestBody();

            // Add the request to the persistent store.
            if (db.addReportSubmittedReject(userId, expRep.reportKey, request.requestBody, msgId)) {

                // Update the ExpenseApprovalCache.
                IExpenseReportCache expAppCache = ((ConcurCore) getApplication()).getExpenseApprovalCache();
                expAppCache.addSubmittedForReject(expRep.reportKey);

                request.messageId = Long.toString(msgId);
                Message msg = handler.obtainMessage(Const.MSG_EXPENSE_REPORT_REJECT_REQUEST, request);
                // Hand off the message for processing.
                handler.sendMessage(msg);

            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendReportReject: unable to persist request for report '"
                        + expRep.reportKey + "'.");
            }

        }

        return request;
    }

    /**
     * Will retrieve the current list of <code>Expense</code> objects for the user <code>userId</code>.
     * 
     * @param userId
     *            the user for which to retrieve the expenses.
     * 
     * @return a list of expenses for the user.
     */
    public ExpenseListInfo getAllExpenses(String userId) {

        // First load from the last set retrieved from the server.
        ConcurCore concurCore = (ConcurCore) ConcurCore.getContext();
        List<SmartExpense> smartExpenses = ExpenseUtil.getSmartExpenses(concurCore, userId);
        ExpenseListInfo expListInfo = ExpenseDAOConverter
                .convertSmartExpenseDAOToExpenseListInfo(smartExpenses, userId);

        ArrayList<Expense> expenses = null;
        ArrayList<PersonalCard> personalCards = null;

        // Second, load a set from the SmartExpnenseDAO list and apply changes.
        if (expListInfo != null) {
            expenses = expListInfo.getExpenseList();
            personalCards = expListInfo.getPersonalCards();
        }

        ArrayList<MobileEntry> mobileEntries = db.loadMobileEntries(userId);
        if (mobileEntries != null) {
            Iterator<MobileEntry> entriesIter = mobileEntries.iterator();
            while (entriesIter.hasNext()) {
                MobileEntry mobileEntry = entriesIter.next();
                if (expenses != null) {
                    ListIterator<Expense> expIter = expenses.listIterator();
                    while (expIter.hasNext()) {
                        Expense expense = expIter.next();
                        if (expense.getExpenseEntryType().equals(mobileEntry.getEntryType())) {
                            switch (expense.getExpenseEntryType()) {
                            case CASH: {
                                if (mobileEntry.getMeKey() != null) {
                                    if (mobileEntry.getMeKey()
                                            .equalsIgnoreCase(expense.getCashTransaction().getMeKey())) {
                                        // Same mobile entry, apply updates.
                                        MobileEntry expMobEnt = expense.getCashTransaction();
                                        expMobEnt.update(mobileEntry);
                                    }
                                } else {
                                    Expense newExp = new Expense(mobileEntry);
                                    expIter.add(newExp);
                                }
                                break;
                            }
                            case PERSONAL_CARD: {
                                if (mobileEntry.getPcaKey() != null && mobileEntry.getPctKey() != null) {
                                    if (mobileEntry.getPcaKey().equalsIgnoreCase(expense.getPersonalCard().pcaKey)
                                            && mobileEntry.getPctKey().equalsIgnoreCase(
                                                    expense.getPersonalCardTransaction().pctKey)) {
                                        if (expense.getPersonalCardTransaction().mobileEntry != null) {
                                            MobileEntry expMobEnt = expense.getPersonalCardTransaction().mobileEntry;
                                            // Set the expense key (expense
                                            // type).
                                            expMobEnt.setExpKey(mobileEntry.getExpKey());
                                            // Set the comment.
                                            expMobEnt.setComment(mobileEntry.getComment());
                                            // Set the receipt information.
                                            expMobEnt.setHasReceiptImage(mobileEntry.hasReceiptImage());
                                            expMobEnt.setReceiptImageDataLocal(mobileEntry.hasReceiptImageDataLocal());
                                            expMobEnt.setReceiptImageDataLocalFilePath(mobileEntry
                                                    .getReceiptImageDataLocalFilePath());
                                            expMobEnt.setCreateDate(mobileEntry.getCreateDate());
                                            expMobEnt.setUpdateDate(mobileEntry.getUpdateDate());
                                        } else {
                                            expense.getPersonalCardTransaction().mobileEntry = mobileEntry;
                                        }
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG
                                                    + ".getAllExpenses: personal card mobile entry has null card account and/or transaction keys!");
                                }
                                break;
                            }
                            case CORPORATE_CARD: {
                                if (mobileEntry.getCctKey() != null) {
                                    if (mobileEntry.getCctKey().equalsIgnoreCase(
                                            expense.getCorporateCardTransaction().getCctKey())) {
                                        if (expense.getCorporateCardTransaction().getMobileEntry() != null) {
                                            MobileEntry expMobEnt = expense.getCorporateCardTransaction()
                                                    .getMobileEntry();
                                            // Set the expense key (expense
                                            // type).
                                            expMobEnt.setExpKey(mobileEntry.getExpKey());
                                            // Set the comment.
                                            expMobEnt.setComment(mobileEntry.getComment());
                                            // Set the receipt information.
                                            expMobEnt.setHasReceiptImage(mobileEntry.hasReceiptImage());
                                            expMobEnt.setReceiptImageDataLocal(mobileEntry.hasReceiptImageDataLocal());
                                            expMobEnt.setReceiptImageDataLocalFilePath(mobileEntry
                                                    .getReceiptImageDataLocalFilePath());
                                            expMobEnt.setCreateDate(mobileEntry.getCreateDate());
                                            expMobEnt.setUpdateDate(mobileEntry.getUpdateDate());
                                        } else {
                                            expense.getCorporateCardTransaction().setMobileEntry(mobileEntry);
                                        }
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG
                                            + ".getAllExpenses: corporate card mobile entry has null transaction key!");
                                }
                                break;
                            }
                            }
                        }
                    }
                } else {
                    Expense expense = null;
                    switch (mobileEntry.getEntryType()) {
                    case CASH:
                        expense = new Expense(mobileEntry);
                        break;
                    case PERSONAL_CARD:
                        Log.e(Const.LOG_TAG,
                                CLS_TAG
                                        + ".getAllExpenses: personal card mobile entry found with no expense entries from server!");
                        break;
                    case CORPORATE_CARD:
                        Log.e(Const.LOG_TAG,
                                CLS_TAG
                                        + ".getAllExpenses: corporate card mobile entry found with no expense entries from server!");
                        break;
                    }
                    if (expense != null) {
                        if (expenses == null) {
                            expenses = new ArrayList<Expense>();
                            personalCards = new ArrayList<PersonalCard>();
                            expListInfo = new ExpenseListInfo(expenses, personalCards, Calendar.getInstance(TimeZone
                                    .getTimeZone("UTC")));
                        }
                        expenses.add(expense);
                    }
                }
            }
        }
        return expListInfo;
    }

    /**
     * Gets the current list of mobile entries associated with a user.
     * 
     * @param userId
     *            the id of the user.
     * 
     * @return an instance of <code>MobileEntryInfo</code>; otherwise if no cached data exists, returns <code>null</code>
     */
    public ArrayList<MobileEntry> getMobileEntries(String userId) {
        return db.loadMobileEntries(userId);
    }

    /**
     * Will send a request to add a receipt image to an existing report.
     * 
     * The result of the request is sent out via broadcast with the intent action of
     * <code>Const.ACTION_EXPENSE_ADD_REPORT_RECEIPT</code>.
     * 
     * @param userId
     *            the id of the user performing this action.
     * @param rptKey
     *            the report key.
     * @param filePath
     *            the receipt file absolute path name.
     * @return returns an instance of <code>AddReportReceiptV2Request</code> if the request has been created; otherwise,
     *         <code>null</code> is returned.
     */
    public AddReportReceiptV2Request sendAddReportReceiptV2Request(String userId, String rptKey, String filePath,
            SaveReceiptRequest.SaveReceiptUploadListener listener, boolean deleteReceiptFile) {
        AddReportReceiptV2Request request = null;

        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new AddReportReceiptV2Request();
            request.rptKey = rptKey;
            request.filePath = filePath;
            request.deleteReceiptFile = deleteReceiptFile;
            request.listener = listener;
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            // Add to our post table, if re-posting is enabled.
            // if (repostEnabled) {
            // if (!db.insertHTTPRequest(userId, "POST", request.getServiceEndpointURI(), null,
            // Const.MSG_EXPENSE_ADD_REPORT_RECEIPT_V2_REQUEST, msgId, filePath)) {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".sendAddReportReceiptV2Request: failed to save request body!");
            // }
            // }
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_ADD_REPORT_RECEIPT_V2_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to add a receipt image id to an existing report.
     * 
     * The result of the request is sent out via broadcast with the intent action of
     * <code>Const.ACTION_EXPENSE_ADD_REPORT_RECEIPT</code>.
     * 
     * @param userId
     *            the id of the user performing this action.
     * @param rptKey
     *            the report key.
     * @param receiptImageId
     *            the receipt image id.
     * @return returns an instance of <code>AddReportReceiptRequest</code> if the request has been created; otherwise,
     *         <code>null</code> is returned.
     */
    public AddReportReceiptRequest sendAddReportReceiptRequest(String userId, String rptKey, String receiptImageId) {
        AddReportReceiptRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new AddReportReceiptRequest();
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("<AddReportReceiptAction ");
            strBldr.append("xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">");
            strBldr.append("<ReceiptImageId>");
            strBldr.append(receiptImageId);
            strBldr.append("</ReceiptImageId>");
            strBldr.append("<RptKey>");
            strBldr.append(rptKey);
            strBldr.append("</RptKey>");
            strBldr.append("</AddReportReceiptAction>");
            request.requestBody = strBldr.toString();

            // Add to our post table, if re-posting is enabled.
            // if (repostEnabled) {
            // if (!db.insertHTTPRequest(userId, "POST", request.getServiceEndpointURI(), request.requestBody,
            // Const.MSG_EXPENSE_ADD_REPORT_RECEIPT_REQUEST, msgId, null)) {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".sendAddReportReceiptRequest: failed to save request body!");
            // }
            // }
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_ADD_REPORT_RECEIPT_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will save a report entry.
     * 
     * @param userId
     *            the user id.
     * 
     * @param expRepEnt
     *            the expense report entry to save.
     * 
     * @return an instance of <code>SaveReportEntryRequest</code>.
     */
    public SaveReportEntryRequest sendSaveReportEntryRequest(String userId, ExpenseReportEntryDetail expRepEntDet,
            boolean copyDownToChildForms, String polKey, String expKey) {
        SaveReportEntryRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new SaveReportEntryRequest();
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            request.expRepEntDet = expRepEntDet;
            request.copyDownToChildForms = copyDownToChildForms;

            // MOB-8452
            request.setExpKey(expKey);
            request.setPolKey(polKey);

            request.buildRequestBody();
            // Add to our post table, if re-posting is enabled.
            // walt ask question about reposting.
            // if (repostEnabled) {
            // if (!db.insertHTTPRequest(userId, "POST", request.getServiceEndpointURI(), request.requestBody,
            // Const.MSG_EXPENSE_SAVE_REPORT_ENTRY_REQUEST, msgId, null)) {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".saveReportEntry: failed to save request body!");
            // }
            // }
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_SAVE_REPORT_ENTRY_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will itemize a hotel entry.
     * 
     * @param userId
     *            the user id.
     * 
     * @param expRepEnt
     *            the expense report entry to save.
     * 
     * @return an instance of <code>SaveReportEntryRequest</code>.
     */
    public ItemizeHotelRequest sendItemizeHotelRequest(ExpenseReportEntryDetail expRepEntDet, boolean combineAmounts,
            String checkIn, String checkOut, String nights, Double roomRate, Double roomTax, Double otherTax1,
            Double otherTax2, String additionalExpKey1, Double additionalAmount1, String additionalExpKey2,
            Double additionalAmount2) {

        ItemizeHotelRequest request = null;
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new ItemizeHotelRequest();
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            request.expRepEntDet = expRepEntDet;
            request.combineAmounts = combineAmounts;
            request.checkIn = checkIn;
            request.checkOut = checkOut;
            request.nights = nights;
            request.roomRate = roomRate;
            request.roomTax = roomTax;
            request.otherTax1 = otherTax1;
            request.otherTax2 = otherTax2;
            request.additionalExpKey1 = additionalExpKey1;
            request.additionalAmount1 = additionalAmount1;
            request.additionalExpKey2 = additionalExpKey2;
            request.additionalAmount2 = additionalAmount2;

            request.buildRequestBody();

            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_ITEMIZE_HOTEL_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to associate a receipt image id with a report entry.
     * 
     * @param userId
     *            the user id.
     * @param expRep
     *            the expense report.
     * @param expRepEnt
     *            the expense report entry.
     * @param receiptImageId
     *            the receipt image id.
     * @return an instance of <code>SaveReportEntryReceiptRequest</code> that can be used for cancelling.
     */
    public SaveReportEntryReceiptRequest sendSaveReportEntryReceiptRequest(String userId, ExpenseReport expRep,
            ExpenseReportEntry expRepEnt, String receiptImageId) {

        SaveReportEntryReceiptRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null && expRepEnt != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new SaveReportEntryReceiptRequest();
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            request.reportKey = expRep.reportKey;
            request.reportEntryKey = expRepEnt.reportEntryKey;
            request.receiptImageId = receiptImageId;
            // Add to our post table, if re-posting is enabled.
            // if (repostEnabled) {
            // if (!db.insertHTTPRequest(userId, "POST", request.getServiceEndpointURI(), request.requestBody,
            // Const.MSG_EXPENSE_SAVE_REPORT_ENTRY_RECEIPT_REQUEST, msgId, null)) {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".saveReport: failed to save request body!");
            // }
            // }
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_SAVE_REPORT_ENTRY_RECEIPT_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to clear a report entry receipt image id.
     * 
     * @param userId
     *            the user id.
     * @param expRep
     *            the expense report.
     * @param expRepEnt
     *            the expense report entry.
     * @return an instance of <code>SaveReportEntryReceiptRequest</code> that can be used for cancelling.
     */
    public ClearReportEntryReceiptRequest sendClearReportEntryReceiptRequest(String userId, ExpenseReport expRep,
            ExpenseReportEntry expRepEnt) {
        ClearReportEntryReceiptRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new ClearReportEntryReceiptRequest();
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            request.reportKey = expRep.reportKey;
            request.reportEntryKey = expRepEnt.reportEntryKey;
            // Add to our post table, if re-posting is enabled.
            // if (repostEnabled) {
            // if (!db.insertHTTPRequest(userId, "POST", request.getServiceEndpointURI(), request.requestBody,
            // Const.MSG_EXPENSE_CLEAR_REPORT_ENTRY_RECEIPT_REQUEST, msgId, null)) {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".saveReport: failed to save request body!");
            // }
            // }
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_CLEAR_REPORT_ENTRY_RECEIPT_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to the server to save the contents of a report header.
     * 
     * @param userId
     *            the user id.
     * @param expRepDet
     *            the expense report detail object.
     * @param frmFldViews
     *            the list of form field view objects containing edited values.
     * @return An instance of <code>SaveReportRequest</code> if the request could be made; <code>null</code> otherwise.
     */
    public SaveReportRequest sendSaveReportRequest(String userId, ExpenseReportDetail expRepDet,
            boolean copyDownToChildForms) {

        SaveReportRequest request = null;

        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new SaveReportRequest();
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            request.expRepDet = expRepDet;
            request.copyDownToChildForms = copyDownToChildForms;
            request.requestBody = request.buildRequestBody();
            // Add to our post table, if re-posting is enabled.
            // if (repostEnabled) {
            // if (!db.insertHTTPRequest(userId, "POST", request.getServiceEndpointURI(), request.requestBody,
            // Const.MSG_EXPENSE_SAVE_REPORT_REQUEST, msgId, null)) {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".saveReport: failed to save request body!");
            // }
            // }
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_SAVE_REPORT_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to obtain a set of reason codes.
     * 
     * @param userId
     *            the user id.
     * @param filterCode
     *            the filter code to send with the request or <code>null</code> to retrieve all reason codes, i.e., "standard" +
     *            "custom".
     * @return returns the list of reason codes.
     */
    public ReasonCodeRequest sendReasonCodeRequest(String userId, String filterCode) {

        ReasonCodeRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new ReasonCodeRequest();
            request.filterCode = filterCode;
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_REASON_CODE_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Sends a request to the server to obtain a list of expense types.
     * 
     * @param userId
     *            the user id.
     * @param policyKey
     *            an optional policy key.
     * @return an instance of <code>GetExpenseTypesRequest</code>.
     */
    public GetExpenseTypesRequest sendGetExpenseTypesRequest(String userId, String policyKey) {
        GetExpenseTypesRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new GetExpenseTypesRequest();
            request.policyKey = policyKey;
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_GET_EXPENSE_TYPES, request);
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Will send a request to the server to obtain the URL for a receipt image.
     * 
     * @param userId
     *            the user id.
     * @param receiptImageId
     *            the receipt image id.
     * @return an instance of <code>GetReceiptImageUrlRequest</code>.
     */
    public GetReceiptImageUrlRequest sendGetReceiptImageUrlRequest(String userId, String receiptImageId) {

        GetReceiptImageUrlRequest request = null;

        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new GetReceiptImageUrlRequest();
            request.receiptImageId = receiptImageId;
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_GET_RECEIPT_IMAGE_URL, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to append one receipt image to another.
     * 
     * @param userId
     *            the end-user id.
     * @param fromReceiptImageId
     *            the id of the receipt image being appended.
     * @param toReceiptImageId
     *            the id of the receipt image to be appended to.
     * @return an instance of <code>AppendReceiptImageRequqest</code> that can be used to cancel the request.
     */
    public AppendReceiptImageRequest sendAppendReceiptImageRequest(String userId, String fromReceiptImageId,
            String toReceiptImageId) {
        AppendReceiptImageRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new AppendReceiptImageRequest();
            request.fromReceiptImageId = fromReceiptImageId;
            request.toReceiptImageId = toReceiptImageId;
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_APPEND_RECEIPT_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    public GetConditionalFieldActionRequest sendGetDynamicActionRequest(String ffKey, String fieldValue) {
        GetConditionalFieldActionRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new GetConditionalFieldActionRequest();
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.formFieldKey = ffKey;
            request.formFieldValue = fieldValue;
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_GET_CONDITIONAL_FIELDS, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to delete a receipt image.
     * 
     * @param userId
     *            the end-user id.
     * @param receiptImageId
     *            the id of the receipt image to be deleted.
     * @return an instance of <code>DeleteReceiptImageRequqest</code> that can be used to cancel the request.
     */
    public DeleteReceiptImageRequest sendDeleteReceiptImageRequest(String userId, String receiptImageId) {
        DeleteReceiptImageRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new DeleteReceiptImageRequest();
            request.receiptImageId = receiptImageId;
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_DELETE_RECEIPT_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to retrieve the current list of receipts available in the receipt store for a particular end-user.
     * 
     * @param userId
     *            the end-user user id.
     * @param filterMobileExpenses
     *            whether the server should filter out receipts associated with mobile expenses.
     * @return an instance of <code>GetReceiptImageUrlsRequest</code> if a request was made; otherwise, <code>null</code> is
     *         returned.
     */
    public GetReceiptImageUrlsRequest sendGetReceiptImageUrlsRequest(String userId, boolean filterMobileExpenses) {

        GetReceiptImageUrlsRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new GetReceiptImageUrlsRequest();
            request.filterMobileExpenses = filterMobileExpenses;
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_GET_RECEIPT_IMAGE_URLS, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    public RetrieveURLRequest sendRetrieveURLRequest(String urlStr, File filePath) {
        RetrieveURLRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            request = new RetrieveURLRequest();
            request.filePath = filePath;
            request.urlStr = urlStr;
            request.messageId = Long.toString(System.currentTimeMillis());
            request.sessionId = sessionId;
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_RETRIEVE_URL_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to the server to delete a report.
     * 
     * @param rptKey
     *            the key of the report to be deleted.
     * @return an instance of <code>DeleteReportRequest</code> that can be used to cancel the request.
     */
    public ReportDeleteRequest sendDeleteReportRequest(String rptKey) {
        ReportDeleteRequest request = null;

        String userId = prefs.getString(Const.PREF_USER_ID, null);
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new ReportDeleteRequest();
            request.rptKey = rptKey;
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_DELETE_REPORT_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will save a receipt to the receipt store. Default standalone receipt to false.
     * 
     * @param userId
     *            the user id making the upload request.
     * @param filePath
     *            contains the absolute path of the receipt image file.
     * @param deleteReceiptFile
     *            contains whether the receipt image file should be deleted after the save attempt.
     * @param listener
     *            contains a reference to an upload listener to receive progress updates.
     * 
     * @return an instance of <code>SaveReceiptRequest</code>.
     */
    public SaveReceiptRequest sendSaveReceiptRequest(String userId, String filePath, boolean deleteReceiptFile,
            SaveReceiptRequest.SaveReceiptUploadListener listener, boolean msgReq) {
        return sendSaveReceiptRequest(userId, filePath, deleteReceiptFile, listener, false, msgReq);
    }

    /**
     * Will save a receipt to the receipt store.
     * 
     * @param userId
     *            the user id making the upload request.
     * @param filePath
     *            contains the absolute path of the receipt image file.
     * @param deleteReceiptFile
     *            contains whether the receipt image file should be deleted after the save attempt.
     * @param listener
     *            contains a reference to an upload listener to receive progress updates.
     * @param standaloneReceipt
     *            whether this receipt is standalone or attached to something. Needed for offline.
     * 
     * @return an instance of <code>SaveReceiptRequest</code>.
     */
    public SaveReceiptRequest sendSaveReceiptRequest(String userId, String filePath, boolean deleteReceiptFile,
            SaveReceiptRequest.SaveReceiptUploadListener listener, boolean standaloneReceipt, boolean msgReq) {

        return sendConnectSaveReceiptRequest(userId, filePath, deleteReceiptFile, listener, standaloneReceipt,
                SaveReceiptRequest.SaveReceiptCall.MWS, null, null, msgReq);
    }

    /**
     * Will save a receipt to the receipt store via the Concur Connect "post image" request.
     * 
     * @param userId
     *            the user id making the upload request.
     * @param filePath
     *            contains the absolute path of the receipt image file.
     * @param deleteReceiptFile
     *            contains whether the receipt image file should be deleted after the save attempt.
     * @param listener
     *            contains a reference to an upload listener to receive progress updates.
     * 
     * @return an instance of <code>SaveReceiptRequest</code>.
     */
    public SaveReceiptRequest sendConnectPostImageRequest(String userId, String filePath, boolean deleteReceiptFile,
            SaveReceiptRequest.SaveReceiptUploadListener listener, boolean msgReq) {

        return sendConnectPostImageRequest(userId, filePath, deleteReceiptFile, listener, false, msgReq);
    }

    /**
     * Will save a receipt to the receipt store via the Concur Connect "post image" request.
     * 
     * @param userId
     *            the user id making the upload request.
     * @param filePath
     *            contains the absolute path of the receipt image file.
     * @param deleteReceiptFile
     *            contains whether the receipt image file should be deleted after the save attempt.
     * @param listener
     *            contains a reference to an upload listener to receive progress updates.
     * @param standaloneReceipt
     *            whether this receipt is standalone or attached to something. Needed for offline.
     * 
     * @return an instance of <code>SaveReceiptRequest</code>.
     */
    public SaveReceiptRequest sendConnectPostImageRequest(String userId, String filePath, boolean deleteReceiptFile,
            SaveReceiptRequest.SaveReceiptUploadListener listener, boolean standaloneReceipt, boolean msgReq) {

        return sendConnectSaveReceiptRequest(userId, filePath, deleteReceiptFile, listener, standaloneReceipt,
                SaveReceiptRequest.SaveReceiptCall.CONNECT_POST_IMAGE, null, null, msgReq);
    }

    /**
     * Will save a receipt that is associated with a report via the Concur Connect "post image to report" request.
     * 
     * @param userId
     *            the user id making the upload request.
     * @param filePath
     *            contains the absolute path of the receipt image file.
     * @param deleteReceiptFile
     *            contains whether the receipt image file should be deleted after the save attempt.
     * @param listener
     *            contains a reference to an upload listener to receive progress updates.
     * @param rptKey
     *            contains the key of the report to which this receipt should be associated.
     * 
     * @return an instance of <code>SaveReceiptRequest</code>.
     */
    public SaveReceiptRequest sendConnectPostImageReportRequest(String userId, String filePath,
            boolean deleteReceiptFile, SaveReceiptRequest.SaveReceiptUploadListener listener, String rptKey,
            boolean msgReq) {

        return sendConnectPostImageReportRequest(userId, filePath, deleteReceiptFile, listener, false, rptKey, msgReq);
    }

    /**
     * Will save a receipt that is associated with a report via the Concur Connect "post image to report" request.
     * 
     * @param userId
     *            the user id making the upload request.
     * @param filePath
     *            contains the absolute path of the receipt image file.
     * @param deleteReceiptFile
     *            contains whether the receipt image file should be deleted after the save attempt.
     * @param listener
     *            contains a reference to an upload listener to receive progress updates.
     * @param standaloneReceipt
     *            whether this receipt is standalone or attached to something. Needed for offline.
     * @param rptKey
     *            contains the key of the report to which this receipt should be associated.
     * 
     * @return an instance of <code>SaveReceiptRequest</code>.
     */
    public SaveReceiptRequest sendConnectPostImageReportRequest(String userId, String filePath,
            boolean deleteReceiptFile, SaveReceiptRequest.SaveReceiptUploadListener listener,
            boolean standaloneReceipt, String rptKey, boolean msgReq) {

        return sendConnectSaveReceiptRequest(userId, filePath, deleteReceiptFile, listener, standaloneReceipt,
                SaveReceiptRequest.SaveReceiptCall.CONNECT_POST_IMAGE_REPORT, rptKey, null, msgReq);
    }

    /**
     * Will save a receipt that is associated with a report entry via the Concur Connect "post image to entry" request.
     * 
     * @param userId
     *            the user id making the upload request.
     * @param filePath
     *            contains the absolute path of the receipt image file.
     * @param deleteReceiptFile
     *            contains whether the receipt image file should be deleted after the save attempt.
     * @param listener
     *            contains a reference to an upload listener to receive progress updates.
     * @param rpeKey
     *            contains the key of the report entry to which this receipt should be associated.
     * 
     * @return an instance of <code>SaveReceiptRequest</code>.
     */
    public SaveReceiptRequest sendConnectPostImageEntryRequest(String userId, String filePath,
            boolean deleteReceiptFile, SaveReceiptRequest.SaveReceiptUploadListener listener, String rpeKey,
            boolean msgReq) {

        return sendConnectPostImageEntryRequest(userId, filePath, deleteReceiptFile, listener, false, rpeKey, msgReq);
    }

    /**
     * Will save a receipt that is associated with a report entry via the Concur Connect "post image to entry" request.
     * 
     * @param userId
     *            the user id making the upload request.
     * @param filePath
     *            contains the absolute path of the receipt image file.
     * @param deleteReceiptFile
     *            contains whether the receipt image file should be deleted after the save attempt.
     * @param listener
     *            contains a reference to an upload listener to receive progress updates.
     * @param standaloneReceipt
     *            whether this receipt is standalone or attached to something. Needed for offline.
     * @param rpeKey
     *            contains the key of the report entry to which this receipt should be associated.
     * 
     * @return an instance of <code>SaveReceiptRequest</code>.
     */
    public SaveReceiptRequest sendConnectPostImageEntryRequest(String userId, String filePath,
            boolean deleteReceiptFile, SaveReceiptRequest.SaveReceiptUploadListener listener,
            boolean standaloneReceipt, String rpeKey, boolean msgReq) {

        return sendConnectSaveReceiptRequest(userId, filePath, deleteReceiptFile, listener, standaloneReceipt,
                SaveReceiptRequest.SaveReceiptCall.CONNECT_POST_IMAGE_REPORT_ENTRY, null, rpeKey, msgReq);
    }

    /**
     * Will save a receipt that is associated with a report entry via the Concur Connect "post image to entry" request.
     * 
     * @param userId
     *            the user id making the upload request.
     * @param filePath
     *            contains the absolute path of the receipt image file.
     * @param deleteReceiptFile
     *            contains whether the receipt image file should be deleted after the save attempt.
     * @param listener
     *            contains a reference to an upload listener to receive progress updates.
     * @param standaloneReceipt
     *            whether this receipt is standalone or attached to something. Needed for offline.
     * @param rptKey
     *            contains the key of the report if this receipt is being associated with a report via Connect.
     * @param rpeKey
     *            contains the key of the report entry if this receipt is being associated with a report entry via Connect.
     * 
     * @return an instance of <code>SaveReceiptRequest</code>.
     */
    private SaveReceiptRequest sendConnectSaveReceiptRequest(String userId, String filePath, boolean deleteReceiptFile,
            SaveReceiptRequest.SaveReceiptUploadListener listener, boolean standaloneReceipt,
            SaveReceiptRequest.SaveReceiptCall call, String rptKey, String rpeKey, boolean msgReq) {

        SaveReceiptRequest request = null;

        String sessionId = Preferences.getSessionId();
        /*
         * MOB-11786 : This is very rare case, and required null check on file path.
         * https://jira.concur.com/jira/secure/attachment/188451/Bug580751_log.txt
         */
        if ((!ConcurCore.isConnected() || sessionId != null) && (filePath != null)) {
            String msgId = Long.toString(System.currentTimeMillis());

            request = new SaveReceiptRequest();
            request.filePath = filePath;
            request.deleteReceiptFile = deleteReceiptFile;
            request.listener = listener;
            request.imageOrigin = "mobile";
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            request.standaloneReceipt = standaloneReceipt;
            // MOB-11857 : Partially support for this JIRA.
            request.setMsgIdReq(msgReq);
            request.receiptEndpoint = call;
            switch (request.receiptEndpoint) {
            case CONNECT_POST_IMAGE: {
                request.accessToken = Preferences.getAccessToken();
                break;
            }
            case CONNECT_POST_IMAGE_REPORT: {
                request.accessToken = Preferences.getAccessToken();
                request.rptKey = rptKey;
                break;
            }
            case CONNECT_POST_IMAGE_REPORT_ENTRY: {
                request.accessToken = Preferences.getAccessToken();
                request.rpeKey = rpeKey;
                break;
            }
            }

            // // Add to our post table, if re-posting is enabled.
            // if (repostEnabled) {
            // if (!db.insertHTTPRequest(userId, "POST", request.getServiceEndpointURI(), null,
            // Const.MSG_EXPENSE_SAVE_RECEIPT_REQUEST, msgId, filePath)) {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".saveReceipt: failed to save request body!");
            // }
            // }
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_SAVE_RECEIPT_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to retrieve the distance to date for a given vehicle and date.
     * 
     * @param carKey
     *            the car key of the vehicle
     * @param date
     *            the date of the entry.
     * @param excludeRpeKey
     *            the RPE_KEY of the entry to not include in the calc.
     * @return an instance of <code>DistanceToDateRequest</code>.
     */
    public DistanceToDateRequest sendDistanceToDateRequest(Integer carKey, Calendar tranDate, String excludeRpeKey) {
        DistanceToDateRequest request = null;
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new DistanceToDateRequest();
            request.carKey = carKey;
            request.date = tranDate;
            request.excludeRpeKey = excludeRpeKey;
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_DISTANCE_TO_DATE_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to retrieve an exchange rate to convert from <code>fromCrnCode</code> currency to
     * <code>toCrnCode</code> currency for the date.
     * 
     * @param userId
     *            the user id making the request.
     * @param fromCrnCode
     *            the from currency code.
     * @param toCrnCode
     *            the to currency code.
     * @param date
     *            the date.
     * @return an instance of <code>ExchangeRateRequest</code>.
     */
    public ExchangeRateRequest sendExchangeRateRequest(String userId, String fromCrnCode, String toCrnCode,
            Calendar date) {
        ExchangeRateRequest request = null;
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new ExchangeRateRequest();
            request.fromCrnCode = fromCrnCode;
            request.toCrnCode = toCrnCode;
            request.date = date;
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_EXCHANGE_RATE_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will save the last filled out travel custom field information for a user.
     * 
     * @param userId
     *            the user id.
     * @param tcfInfo
     *            the serialized travel custom field information.
     */
    public void saveTravelCustomFieldInfo(String userId, String tcfInfo) {
        try {
            Calendar lastSavedTS = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            db.saveResponse(Const.MSG_FILLED_OUT_TRAVEL_CUSTOM_FIELD_INFO, lastSavedTS, tcfInfo, userId, true);
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".saveTravelCustomFieldInfo: ", sqlExc);
        } catch (Exception exc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".saveTravelCustomFieldInfo: ", exc);
        }
    }

    /**
     * Will retrieve the last filled out travel custom field information for a user.
     * 
     * @param userId
     *            the user id.
     * @return returns a string containing serialized travel custom field information.
     */
    public String getTravelCustomFieldInfo(String userId) {
        String retVal = null;
        retVal = db.loadResponse(Const.MSG_FILLED_OUT_TRAVEL_CUSTOM_FIELD_INFO, userId, true);
        return retVal;
    }

    public SaveMobileEntryRequest saveMobileEntry(String userId, MobileEntry mobileEntry,
            ReceiptPictureSaveAction saveAction, String receiptFilePath, boolean deleteReceiptFile, boolean msgReq) {
        return saveMobileEntry(userId, mobileEntry, saveAction, receiptFilePath, deleteReceiptFile, false, msgReq);
    }

    /**
     * Will save a mobile entry to the local cache and to the server.
     * 
     * @param userId
     *            the user id associated with the entry.
     * @param mobileEntry
     *            the mobile entry to be saved.
     * @param receiptFilePath
     *            the file path of the receipt to be uploaded.
     * @param deleteReceiptFile
     *            contains whether or not the receipt image file should be deleted after a save attempt.
     * @param forceUpload
     *            This is being uploaded to the server
     */
    public SaveMobileEntryRequest saveMobileEntry(String userId, MobileEntry mobileEntry,
            ReceiptPictureSaveAction saveAction, String receiptFilePath, boolean deleteReceiptFile,
            boolean forceUpload, boolean msgReq) {

        SaveMobileEntryRequest request = null;

        String sessionId = Preferences.getSessionId();

        if (!ConcurCore.isConnected() || sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());

            // construct a message instance and pass to service handler.
            request = new SaveMobileEntryRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.localKey = mobileEntry.getLocalKey();
            request.mobileEntryKey = mobileEntry.getMeKey();
            request.mobileEntry = mobileEntry;
            request.messageId = msgId;
            request.clearImage = (saveAction == ReceiptPictureSaveAction.CLEAR_PICTURE);
            request.filePath = receiptFilePath;
            request.deleteReceiptFile = deleteReceiptFile;
            request.forceUpload = forceUpload;
            // MOB-11857 : Partially support for this JIRA.
            request.setMsgIdReq(msgReq);
            // MOB-8452
            request.setExpKey(mobileEntry.getExpKey());
            request.buildRequestBody();

            // if (repostEnabled) {
            // // persist this request to the database.
            // if (!db.insertHTTPRequest(userId, "POST", request.getServiceEndpointURI(), request.requestBody,
            // Const.MSG_EXPENSE_MOBILE_ENTRY_SAVE_REQUEST, msgId, null)) {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".saveMobileEntry: failed to save request body!");
            // }
            // }
            //
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_MOBILE_ENTRY_SAVE_REQUEST, request);
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Will delete a list of mobile entries.
     * 
     * @param userId
     *            the user id associated with the request.
     * @param mobileEntries
     *            the list of mobile entries to delete.
     */
    public DeleteMobileEntriesRequest sendMobileEntryDeleteRequest(String userId, ArrayList<MobileEntry> mobileEntries) {

        DeleteMobileEntriesRequest request = null;

        String msgId = Long.toString(System.currentTimeMillis());

        // create and submit the request.
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            request = new DeleteMobileEntriesRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.mobileEntries = mobileEntries;
            request.messageId = msgId;

            request.buildRequestBody();

            // if (repostEnabled) {
            // // persist this request is re-posting is enabled.
            // if (!db.insertHTTPRequest(userId, "POST", "/mobile/Expense/DeleteMobileEntries", request.requestBody,
            // Const.MSG_EXPENSE_MOBILE_ENTRY_DELETE_REQUEST, msgId, null)) {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".deleteMobileEntry: failed to save request body!");
            // }
            // }

            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_MOBILE_ENTRY_DELETE_REQUEST, request);
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Will send a request to the server to add a series of transactions to a report.
     * 
     * @param userId
     *            the request user id.
     * @param reportKey
     *            the report key.
     * @param reportName
     *            the report name.
     * @param meKeys
     *            the list of mobile expense keys.
     * @param pctKeys
     *            the list of personal card transaction keys.
     * @param cctKeys
     *            the list of corporate card transaction keys.
     * @param smartCorpExpenses
     *            the list of smart corporate expenses.
     * @param smartPersExpenses
     *            the list of smart personal expenses.
     * @param smartExpIds
     *            the list of expense it expenses.
     */
    public AddToReportRequest addToReport(String reportKey, String reportName, ArrayList<String> meKeys,
            ArrayList<String> pctKeys, ArrayList<String> cctKeys, ArrayList<Expense> smartCorpExpenses,
            ArrayList<Expense> smartPersExpenses, List<AttendeesEntryMap> attendeeEntryMaps,
            ArrayList<String> smartExpIds) {
        AddToReportRequest request = null;
        // Init the message ID
        String msgId = Long.toString(System.currentTimeMillis());
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (pctKeys != null && pctKeys.size() > 0) {
            // Write the transactions to the database and reset the card list
            db.addHiddenPersonalCardTransactions(pctKeys);
            handler.reprocessCardList(userId);
        }

        // if( cctKeys != null && cctKeys.size() > 0) {
        // // Write the transactions to the database and reset the card list.
        // db.addHiddenCardTransactions(cctKeys);
        // handler.reprocessCardList(userId);
        // }

        String sessionId = Preferences.getSessionId();

        if (sessionId != null) {
            // Build the message and hand off to the handler
            request = new AddToReportRequest();
            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            request.reportKey = reportKey;
            request.reportName = reportName;
            request.meKeys = meKeys;
            request.pctKeys = pctKeys;
            request.cctKeys = cctKeys;
            request.smartCorpExpenses = smartCorpExpenses;
            request.smartPersExpenses = smartPersExpenses;
            request.attendeesEntryMaps = attendeeEntryMaps;
            request.smartExpIds = smartExpIds;
            request.buildRequestBody();

            // if (repostEnabled) {
            // // Persist the request
            // if (!db.insertHTTPRequest(userId, "POST", AddToReportRequest.SERVICE_END_POINT, request.requestBody,
            // Const.MSG_EXPENSE_ADD_TO_REPORT_REQUEST, msgId, null)) {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".addToReport: failed to save request body!");
            // }
            // }

            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_ADD_TO_REPORT_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will delete a receipt image file from the receipt directory if not referenced by any mobile expense entry other than the
     * one whose local key is <code>localKey</code>. <br>
     * <b>NOTE: this method will now delete the file if the <code>localKey</code> is <code>null</code> meaning is not saved.</b>
     * 
     * @param localKey
     *            a mobile entry local key that may still reference the file.
     * @param receiptImageFilePath
     *            the pathe of the receipt image file that might be deleted.
     */
    public void deleteReceiptImageFileIfUnreferenced(String localKey, String receiptImageFilePath) {
        int receiptImageReferenceCount = 0;

        if (localKey != null) {
            receiptImageReferenceCount = db.getReceiptImageFilePathReferenceCount(localKey, receiptImageFilePath);
        }
        if (receiptImageReferenceCount == 0) {
            if (receiptImageFilePath != null) {
                File receiptImageFile = new File(receiptImageFilePath);
                if (receiptImageFile.exists()) {
                    receiptImageFile.delete();
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".deleteReceiptImageFileIfUnreferenced: receiptImageFilePath is null!");
            }
        }
    }

    public ArrayList<PersonalCard> getPersonalCardList() {
        ArrayList<PersonalCard> cards = null;
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        String cachedResponse = db.loadResponse(Const.MSG_CARD_LIST_REQUEST, userId);
        if (cachedResponse != null) {
            Calendar lastRetrievedTS = db.getReponseLastRetrieveTS(Const.MSG_CARD_LIST_REQUEST, userId);
            ConcurCore app = (ConcurCore) getApplication();
            cards = PersonalCard.parseCardXml(cachedResponse);
            if (cards != null) {
                app.setCards(cards);
                app.setCardsLastRetrieved(lastRetrievedTS);
            }
        }
        return cards;
    }

    /**
     * Retrieve the current card+transaction list.
     * 
     * When complete an {@link Intent} will be broadcast with the action of Const.ACTION_CARDS_UPDATED
     */
    public void sendPersonalCardListRequest() {

        String sessionId = Preferences.getSessionId();

        // The current session ID is stored in the shared preferences.
        CardListRequest request = new CardListRequest();
        request.sessionId = sessionId;
        request.userId = prefs.getString(Const.PREF_USER_ID, null);
        request.messageId = Long.toString(System.currentTimeMillis());

        // TODO : Add performance profile logging

        // Set the message up
        Message msg = handler.obtainMessage(Const.MSG_CARD_LIST_REQUEST, request);

        // Hand off to the handler thread
        handler.sendMessage(msg);
    }

    /**
     * Gets the base64 encoded receipt image data associated with this expense.
     * 
     * @param mobileEntry
     *            the mobile entry for which to retrieve the receipt image data.
     * 
     * @return the mobile entry base64 encoded receipt image data.
     */
    public String getMobileEntryReceiptImageData(MobileEntry mobileEntry) {
        return db.loadMobileEntryReceiptImageData(mobileEntry.getLocalKey());
    }

    /**
     * Will start the asynchronous download of a mobile entry receipt.
     * 
     * When complete an {@link Intent} will be broadcast with the action of Const.ACTION_EXPENSE_RECEIPT_DOWNLOADED.
     * 
     * @param mobileEntry
     *            the mobile entry upon which to download the receipt.
     */
    public void downloadMobileEntryReceipt(MobileEntry mobileEntry) {

        String sessionId = Preferences.getSessionId();

        if (sessionId != null) {
            // Construct the request.
            DownloadMobileEntryReceiptRequest request = new DownloadMobileEntryReceiptRequest();
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.localKey = mobileEntry.getLocalKey();
            request.meKey = mobileEntry.getMeKey();
            request.mobileEntry = mobileEntry;
            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_DOWNLOAD_RECEIPT_REQUEST, request);
            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
    }

    /**
     * Retrieves an instance of <code>CountSummary</code> from the local cache.
     * 
     * NOTE: If there is data present, this method will set the count summary and "last retrieved timestamp" on the application
     * object.
     * 
     * @return an instance of <code>CountSummary</code> if cached locally; otherwise <code>null</code> is returned.
     */
    public CountSummary getCountSummary() {
        // NOTE: Code in the home screen activity depends upon the count summary
        // object and time stamp
        // being set on the application object.
        CountSummary countSummary = null;
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        String cachedResponse = db.loadResponse(Const.MSG_SUMMARY_COUNT_REQUEST, userId);
        if (cachedResponse != null) {
            Calendar lastRetrievedTS = db.getReponseLastRetrieveTS(Const.MSG_SUMMARY_COUNT_REQUEST, userId);
            ConcurCore app = (ConcurCore) getApplication();
            countSummary = CountSummary.parseSummaryXml(cachedResponse);
            if (countSummary != null) {
                app.setSummary(countSummary);
                app.setSummaryLastRetrieved(lastRetrievedTS);
            }
        }
        return countSummary;
    }

    /**
     * Will send a request to retrieve count summary information.
     * 
     * Once the request is complete, a <code>Const.ACTION_SUMMARY_UPDATED</code> action will be broadcast containing result
     * information.
     * 
     * @param background
     *            contains whether the request should be handled as a background request.
     * 
     * @return an instance of <code>CountSummaryRequest</code> that may be used to cancel the request.
     */
    public CountSummaryRequest sendCountSummaryRequest(boolean background) {

        CountSummaryRequest request = null;

        // The current session ID is stored in the shared preferences.
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            request = new CountSummaryRequest();
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());
            request.background = background;

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_SUMMARY_COUNT_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to retrieve travel custom fields configuration.
     * 
     * @return returns an instance of <code>TravelCustomFieldsRequest</code>.
     */
    public TravelCustomFieldsRequest sendTravelCustomFieldsRequest() {
        TravelCustomFieldsRequest request = null;

        // The current session ID is stored in the shared preferences.
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            request = new TravelCustomFieldsRequest();
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_CUSTOM_FIELDS_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to update a set of travel custom fields.
     * 
     * @return returns an instance of <code>TravelCustomFieldsUpdateRequest</code>.
     */
    public TravelCustomFieldsUpdateRequest sendTravelCustomFieldsUpdateRequest(List<TravelCustomField> fields) {
        TravelCustomFieldsUpdateRequest request = null;

        // The current session ID is stored in the shared preferences.
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            request = new TravelCustomFieldsUpdateRequest();
            request.fields = fields;
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_TRAVEL_CUSTOM_FIELDS_UPDATE_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Retrieves a list of <code>CarConfig</code> objects from the local cache. The general pattern is that the in-memory (app
     * object) cache has been checked and this is called if the app cache is null.
     * 
     * NOTE: If there is data present, this method will set the config list and "last retrieved timestamp" on the application
     * object.
     * 
     * @return a list of <code>CarConfig</code> objects if cached locally; otherwise <code>null</code> is returned.
     */
    public ArrayList<CarConfig> getCarConfigs() {

        ArrayList<CarConfig> configs = null;

        String userId = prefs.getString(Const.PREF_USER_ID, null);

        String cachedResponse = db.loadResponse(Const.MSG_EXPENSE_CAR_CONFIGS_REQUEST, userId);
        if (cachedResponse != null) {
            Calendar lastRetrievedTS = db.getReponseLastRetrieveTS(Const.MSG_EXPENSE_CAR_CONFIGS_REQUEST, userId);
            ConcurCore app = (ConcurCore) getApplication();

            try {
                configs = CarConfig.parseCarConfigXml(cachedResponse);
                if (configs != null) {
                    app.setCarConfigs(configs);
                    app.setCarConfigsLastRetrieved(lastRetrievedTS);
                }
            } catch (IOException ioExc) {
                Log.e(Const.LOG_TAG,
                        CLS_TAG + ".getCarConfigs: I/O exception parsing cached response: " + ioExc.getMessage(), ioExc);
            }

        }
        return configs;
    }

    /**
     * Will send a request to retrieve car configs.
     * 
     * Once the request is complete, a <code>Const.ACTION_EXPENSE_CAR_CONFIGS_UPDATED</code> action will be broadcast containing
     * result information.
     * 
     * @return an instance of <code>CarConfigsRequest</code> that may be used to cancel the request.
     */

    public CarConfigsRequest sendCarConfigsRequest() {

        CarConfigsRequest request = null;

        // The current session ID is stored in the shared preferences.
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            request = new CarConfigsRequest();
            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_CAR_CONFIGS_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    /**
     * Will send a request to retrieve check in current user's location.
     * 
     * Once the request is complete, a <code>Const.ACTION_LOCATION_CHECK_IN</code> action will be broadcast containing result
     * information.
     * 
     * @param address
     *            the geo <code>Address</code> of the current user's location
     * @param assistanceRequired
     *            <code>Y</code> if assitance is required, otherwise <code>N</code>.
     * @param comment
     *            optional user comment
     */
    public LocationCheckInRequest checkInCurrentLocation(Address address, String assistanceRequired,
            String daysRemaining, String comment) {

        String msgId = Long.toString(System.currentTimeMillis());

        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        LocationCheckInRequest request = null;

        if (sessionId != null) {

            // Build the message and hand off to the handler
            request = new LocationCheckInRequest();
            request.longitude = Double.toString(address.getLongitude());
            request.latitude = Double.toString(address.getLatitude());
            request.city = address.getLocality();
            request.state = address.getAdminArea();
            request.countryCode = address.getCountryCode();
            request.assistanceRequired = assistanceRequired;
            request.daysRemaining = daysRemaining;
            request.comment = comment;

            request.sessionId = sessionId;
            request.userId = userId;
            request.messageId = msgId;
            Message msg = handler.obtainMessage(Const.MSG_LOCATION_CHECK_IN_REQUEST, request);
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Sends a request to the server to get the list of Report form fields.
     * 
     * @param policyKey
     *            an optional policy key.
     * @return an instance of <code>ReportFormRequest</code>.
     */
    public ReportFormRequest sendReportFormRequest(String policyKey) {

        ReportFormRequest request = null;
        String sessionId = Preferences.getSessionId();
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new ReportFormRequest();
            request.policyKey = policyKey;
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            Message msg = handler.obtainMessage(Const.MSG_EXPENSE_REPORT_FORM_REQUEST, request);
            handler.sendMessage(msg);
        }

        return request;
    }

    /**
     * Create request for alternative flight schedule
     * 
     * @return
     */
    public AlternativeAirScheduleRequest searchAlternativeFlightScheduleRequest(String arriveIATA, String departIATA,
            String code, Calendar date) {

        AlternativeAirScheduleRequest request = null;

        // The current session ID is stored in the shared preferences.
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            request = new AlternativeAirScheduleRequest();
            request.arriveIATA = arriveIATA;
            request.carrierCode = code;
            request.departIATA = departIATA;
            request.flightDate = date;

            request.sessionId = sessionId;
            request.userId = prefs.getString(Const.PREF_USER_ID, null);
            request.messageId = Long.toString(System.currentTimeMillis());

            // Set the message up
            Message msg = handler.obtainMessage(Const.MSG_ALTERNATIVE_FLIGHT_SEARCH_REQUEST, request);

            // Hand off to the handler thread
            handler.sendMessage(msg);
        }
        return request;
    }

    public PostCrashLogRequest sendPostCrashLogRequest(String version, String stack) {
        PostCrashLogRequest request = null;
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        // session id will be assigned at the beginning of this request
        // being processed.
        request = new PostCrashLogRequest();
        request.userId = userId;
        request.messageId = Long.toString(System.currentTimeMillis());
        request.version = version;
        request.stack = stack;

        // Set the message up.
        Message msg = handler.obtainMessage(Const.MSG_POST_CRASH_LOG_REQUEST, request);

        // Hand off to the handler thread.
        handler.sendMessage(msg);

        return request;
    }

    public NotificationRegisterRequest sendNotificationRegisterRequest(String token, boolean isTesting) {
        NotificationRegisterRequest request = null;
        String userId = prefs.getString(Const.PREF_USER_ID, null);

        Log.v(Const.LOG_TAG, "AWSPush // registering with MWS // token from GCM  // " + token);

        // session id will be assigned at the beginning of this request
        // being processed.
        request = new NotificationRegisterRequest();
        request.userId = userId;
        request.messageId = Long.toString(System.currentTimeMillis());
        request.token = token;

        // Set the message up.
        Message msg = handler.obtainMessage(Const.MSG_NOTIFICATION_REGISTER, request);

        // Hand off to the handler thread.
        handler.sendMessage(msg);

        return request;
    }

    /**
     * Clear all callbacks and messages within the ConcurServiceHandler queue. For example, if we intentionally clear the Session
     * Id (e.g. while logging out), we don't want another request firing off validate session and trying to log the user out, so
     * we clear all of those requests.
     */
    public void clearHandlerMessages() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * Will send a request to the server to get tax form.
     * 
     * @param expKey
     *            : expense key
     * @param date
     *            : expense date
     * @param lnKey
     *            : location key
     * @param reportEntryKey
     *            : report entry key
     * @param ctryCode
     *            : Country Code
     * @param ctrySubCode
     *            : Country Sub Code
     * @return an instance of <code>GetTaxFormRequest</code> that can be used to finish the request.
     */
    public GetTaxFormRequest getTaxForm(String expKey, String date, String lnKey, String reportEntryKey,
            String ctryCode, String ctrySubCode) {
        GetTaxFormRequest request = null;

        String userId = prefs.getString(Const.PREF_USER_ID, null);
        String sessionId = Preferences.getSessionId();
        if (sessionId != null) {
            String msgId = Long.toString(System.currentTimeMillis());
            request = new GetTaxFormRequest();
            request.expKey = expKey;
            request.lnKey = lnKey;
            request.date = date;
            request.reportEntryKey = reportEntryKey;
            request.ctryCode = ctryCode;
            request.ctrySubCode = ctrySubCode;
            request.messageId = msgId;
            request.sessionId = sessionId;
            request.userId = userId;
            Message msg = handler.obtainMessage(Const.MSG_GET_TAX_FORM_REQUEST, request);
            handler.sendMessage(msg);
        }
        return request;
    }

}
