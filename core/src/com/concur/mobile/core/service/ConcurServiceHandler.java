package com.concur.mobile.core.service;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.data.IExpenseReportDBInfo;
import com.concur.mobile.core.data.IExpenseReportInfo;
import com.concur.mobile.core.data.IExpenseReportInfo.ReportType;
import com.concur.mobile.core.data.IItineraryDBInfo;
import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.data.ReportDBUtil;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.MobileEntry;
import com.concur.mobile.core.expense.charge.data.MobileEntryStatus;
import com.concur.mobile.core.expense.charge.data.PersonalCard;
import com.concur.mobile.core.expense.charge.service.AllExpenseReply;
import com.concur.mobile.core.expense.charge.service.AllExpenseRequest;
import com.concur.mobile.core.expense.charge.service.CardListRequest;
import com.concur.mobile.core.expense.charge.service.DeleteMobileEntriesReply;
import com.concur.mobile.core.expense.charge.service.DeleteMobileEntriesRequest;
import com.concur.mobile.core.expense.charge.service.SaveMobileEntryReply;
import com.concur.mobile.core.expense.charge.service.SaveMobileEntryRequest;
import com.concur.mobile.core.expense.data.ExpenseListInfo;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptShareItem;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptShareItem.Status;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptStoreCache;
import com.concur.mobile.core.expense.receiptstore.service.DeleteReceiptImageRequest;
import com.concur.mobile.core.expense.receiptstore.service.GetReceiptImageUrlReply;
import com.concur.mobile.core.expense.receiptstore.service.GetReceiptImageUrlRequest;
import com.concur.mobile.core.expense.receiptstore.service.GetReceiptImageUrlsReply;
import com.concur.mobile.core.expense.receiptstore.service.GetReceiptImageUrlsRequest;
import com.concur.mobile.core.expense.receiptstore.service.RetrieveURLRequest;
import com.concur.mobile.core.expense.report.approval.service.ApproveReportRequest;
import com.concur.mobile.core.expense.report.approval.service.RejectReportRequest;
import com.concur.mobile.core.expense.report.approval.service.ReportApproveRejectServiceReply;
import com.concur.mobile.core.expense.report.approval.service.ReportsToApproveReply;
import com.concur.mobile.core.expense.report.approval.service.ReportsToApproveRequest;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportListInfo;
import com.concur.mobile.core.expense.report.data.IExpenseReportListInfo;
import com.concur.mobile.core.expense.report.service.ActiveReportsReply;
import com.concur.mobile.core.expense.report.service.ActiveReportsRequest;
import com.concur.mobile.core.expense.report.service.AddReportReceiptRequest;
import com.concur.mobile.core.expense.report.service.AddReportReceiptV2Request;
import com.concur.mobile.core.expense.report.service.AddToReportReply;
import com.concur.mobile.core.expense.report.service.AddToReportRequest;
import com.concur.mobile.core.expense.report.service.AppendReceiptImageRequest;
import com.concur.mobile.core.expense.report.service.ApproverSearchReply;
import com.concur.mobile.core.expense.report.service.ApproverSearchRequest;
import com.concur.mobile.core.expense.report.service.AttendeeFormReply;
import com.concur.mobile.core.expense.report.service.AttendeeFormRequest;
import com.concur.mobile.core.expense.report.service.AttendeeSaveReply;
import com.concur.mobile.core.expense.report.service.AttendeeSaveRequest;
import com.concur.mobile.core.expense.report.service.AttendeeSearchFieldsReply;
import com.concur.mobile.core.expense.report.service.AttendeeSearchFieldsRequest;
import com.concur.mobile.core.expense.report.service.AttendeeSearchReply;
import com.concur.mobile.core.expense.report.service.AttendeeSearchRequest;
import com.concur.mobile.core.expense.report.service.CarConfigsReply;
import com.concur.mobile.core.expense.report.service.CarConfigsRequest;
import com.concur.mobile.core.expense.report.service.ClearReportEntryReceiptReply;
import com.concur.mobile.core.expense.report.service.ClearReportEntryReceiptRequest;
import com.concur.mobile.core.expense.report.service.DefaultAttendeeReply;
import com.concur.mobile.core.expense.report.service.DefaultAttendeeRequest;
import com.concur.mobile.core.expense.report.service.DistanceToDateReply;
import com.concur.mobile.core.expense.report.service.DistanceToDateRequest;
import com.concur.mobile.core.expense.report.service.ExchangeRateReply;
import com.concur.mobile.core.expense.report.service.ExchangeRateRequest;
import com.concur.mobile.core.expense.report.service.ExtendedAttendeeSearchReply;
import com.concur.mobile.core.expense.report.service.ExtendedAttendeeSearchRequest;
import com.concur.mobile.core.expense.report.service.GetConditionalFieldActionReply;
import com.concur.mobile.core.expense.report.service.GetConditionalFieldActionRequest;
import com.concur.mobile.core.expense.report.service.GetTaxFormReply;
import com.concur.mobile.core.expense.report.service.GetTaxFormRequest;
import com.concur.mobile.core.expense.report.service.ItemizeHotelReply;
import com.concur.mobile.core.expense.report.service.ItemizeHotelRequest;
import com.concur.mobile.core.expense.report.service.MarkEntryReceiptViewedRequest;
import com.concur.mobile.core.expense.report.service.MarkReceiptsViewedRequest;
import com.concur.mobile.core.expense.report.service.RemoveReportExpenseReply;
import com.concur.mobile.core.expense.report.service.RemoveReportExpenseRequest;
import com.concur.mobile.core.expense.report.service.ReportDeleteRequest;
import com.concur.mobile.core.expense.report.service.ReportDetailReply;
import com.concur.mobile.core.expense.report.service.ReportDetailRequest;
import com.concur.mobile.core.expense.report.service.ReportEntryDetailReply;
import com.concur.mobile.core.expense.report.service.ReportEntryDetailRequest;
import com.concur.mobile.core.expense.report.service.ReportEntryFormReply;
import com.concur.mobile.core.expense.report.service.ReportEntryFormRequest;
import com.concur.mobile.core.expense.report.service.ReportFormReply;
import com.concur.mobile.core.expense.report.service.ReportFormRequest;
import com.concur.mobile.core.expense.report.service.ReportHeaderDetailReply;
import com.concur.mobile.core.expense.report.service.ReportHeaderDetailRequest;
import com.concur.mobile.core.expense.report.service.ReportItemizationEntryFormReply;
import com.concur.mobile.core.expense.report.service.ReportItemizationEntryFormRequest;
import com.concur.mobile.core.expense.report.service.SaveReportEntryReceiptReply;
import com.concur.mobile.core.expense.report.service.SaveReportEntryReceiptRequest;
import com.concur.mobile.core.expense.report.service.SaveReportEntryReply;
import com.concur.mobile.core.expense.report.service.SaveReportEntryRequest;
import com.concur.mobile.core.expense.report.service.SaveReportReply;
import com.concur.mobile.core.expense.report.service.SaveReportRequest;
import com.concur.mobile.core.expense.report.service.SubmitReportReply;
import com.concur.mobile.core.expense.report.service.SubmitReportRequest;
import com.concur.mobile.core.expense.service.DownloadMobileEntryReceiptReply;
import com.concur.mobile.core.expense.service.DownloadMobileEntryReceiptRequest;
import com.concur.mobile.core.expense.service.GetExpenseTypesReply;
import com.concur.mobile.core.expense.service.GetExpenseTypesRequest;
import com.concur.mobile.core.expense.service.KeyedServiceReply;
import com.concur.mobile.core.expense.service.SaveReceiptReply;
import com.concur.mobile.core.expense.service.SaveReceiptRequest;
import com.concur.mobile.core.expense.service.SearchListReply;
import com.concur.mobile.core.expense.service.SearchListRequest;
import com.concur.mobile.core.travel.air.service.AirCancelReply;
import com.concur.mobile.core.travel.air.service.AirCancelRequest;
import com.concur.mobile.core.travel.air.service.AirFilterReply;
import com.concur.mobile.core.travel.air.service.AirFilterRequest;
import com.concur.mobile.core.travel.air.service.AirSearchReply;
import com.concur.mobile.core.travel.air.service.AirSearchRequest;
import com.concur.mobile.core.travel.air.service.AirSellReply;
import com.concur.mobile.core.travel.air.service.AirSellRequest;
import com.concur.mobile.core.travel.air.service.AlternativeAirScheduleReply;
import com.concur.mobile.core.travel.air.service.AlternativeAirScheduleRequest;
import com.concur.mobile.core.travel.car.service.CancelCarRequest;
import com.concur.mobile.core.travel.car.service.CarSearchReply;
import com.concur.mobile.core.travel.car.service.CarSearchRequest;
import com.concur.mobile.core.travel.car.service.CarSellReply;
import com.concur.mobile.core.travel.car.service.CarSellRequest;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.hotel.service.HotelConfirmReply;
import com.concur.mobile.core.travel.hotel.service.HotelConfirmRequest;
import com.concur.mobile.core.travel.hotel.service.HotelDetailReply;
import com.concur.mobile.core.travel.hotel.service.HotelDetailRequest;
import com.concur.mobile.core.travel.hotel.service.HotelImagesReply;
import com.concur.mobile.core.travel.hotel.service.HotelImagesRequest;
import com.concur.mobile.core.travel.hotel.service.HotelSearchReply;
import com.concur.mobile.core.travel.hotel.service.HotelSearchRequest;
import com.concur.mobile.core.travel.rail.service.CancelRailRequest;
import com.concur.mobile.core.travel.rail.service.RailSearchReply;
import com.concur.mobile.core.travel.rail.service.RailSearchRequest;
import com.concur.mobile.core.travel.rail.service.RailSellReply;
import com.concur.mobile.core.travel.rail.service.RailSellRequest;
import com.concur.mobile.core.travel.rail.service.RailStationListReply;
import com.concur.mobile.core.travel.rail.service.RailStationListRequest;
import com.concur.mobile.core.travel.rail.service.RailTicketDeliveryOptionsReply;
import com.concur.mobile.core.travel.rail.service.RailTicketDeliveryOptionsRequest;
import com.concur.mobile.core.travel.service.ItineraryReply;
import com.concur.mobile.core.travel.service.ItineraryRequest;
import com.concur.mobile.core.travel.service.LocationSearchReply;
import com.concur.mobile.core.travel.service.LocationSearchRequest;
import com.concur.mobile.core.travel.service.ReasonCodeReply;
import com.concur.mobile.core.travel.service.ReasonCodeRequest;
import com.concur.mobile.core.travel.service.TravelCustomFieldsReply;
import com.concur.mobile.core.travel.service.TravelCustomFieldsRequest;
import com.concur.mobile.core.travel.service.TravelCustomFieldsUpdateReply;
import com.concur.mobile.core.travel.service.TravelCustomFieldsUpdateRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.net.SessionManager;
import com.concur.mobile.platform.util.Format;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import org.apache.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.FactoryConfigurationError;

public class ConcurServiceHandler extends Handler {

    private static final String CLS_TAG = ConcurServiceHandler.class.getSimpleName();

    protected ConcurService concurService;

    protected ConcurServiceHandler() {
    }

    /**
     * 
     * @param service
     *            A reference to the running {@link ConcurService}
     * @param looper
     *            A reference to the {@link Looper} attached to this thread
     */
    public ConcurServiceHandler(ConcurService service, Looper looper) {
        super(looper);
        concurService = service;
        // Ensure we clear out any sticky broadcast events.
        clearStickyBroadcasts();
    }

    /**
     * Ensure any previously sticky broadcasts are removed.
     */
    protected void clearStickyBroadcasts() {
        concurService.removeStickyBroadcast(new Intent(Const.ACTION_NETWORK_ACTIVITY_START));
    }

    protected synchronized boolean verifySession(Message msg) {

        if (msg.obj instanceof ServiceRequest) {
            ServiceRequest request = (ServiceRequest) msg.obj;
            Log.e("verifySession" +
                    "" +
                    "" +
                    "" +
                    "" +
                    " : ","REQUEST ID : " + msg.what+ " REQUEST : " + request.toString());
            // Only check for session validity when online.
            if (request.isSessionRequired() && ConcurCore.isConnected()) {
                // Validate the current session id.
                String sessionId = SessionManager.validateSessionId((ConcurCore) concurService.getApplication(), msg,
                        new SessionManager.AutoLoginListener() {

                            @Override
                            public void onSuccess(String sessionId) {
                                if (sessionId == null) {
                                    ConcurCore app = (ConcurCore) ConcurCore.getContext();
                                    // If we have no session at this point then auto-login was
                                    // unsuccessful or not allowed.
                                    // Bail out and throw them back to login
                                    // Punt following line.
                                    app.expireLogin();
                                }
                            }

                            @Override
                            public void onRemoteWipe() {
                                // MOB-18782 - If remote wipe was passed down, then notify the app.
                                ConcurCore app = (ConcurCore) ConcurCore.getContext();
                                app.remoteWipe();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                // TODO: MOB-18707 Invoke ConcurCore that a network error occurred.
                                broadcastSystemUnavailable(errorMessage);
                            }

                        }); // end AutoLoginListener()

                if (sessionId == null) {
                    return false;
                } else {
                    // Set the session id on the request.
                    request.sessionId = sessionId;
                }
            }
        }

        return true;

    }

    @Override
    /**
     * Handle messages as they are sent here via the looper
     */
    public void handleMessage(Message msg) {
        if (!verifySession(msg)) {
            return;
        }

        switch (msg.what) {
        case Const.MSG_CLEAR_LOCAL_DATA: {
            Intent intent = new Intent(Const.ACTION_DATABASE_RESET);
            try {
                // Reset the database.
                concurService.db.reset();
            } finally {
                // Send broadcast
                concurService.sendBroadcast(intent);
            }
            break;
        }
        case Const.MSG_ITINERARY_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieving_itinerary).toString());

                // This is a request to retrieve a single itinerary.
                ItineraryRequest request = (ItineraryRequest) msg.obj;

                Calendar lastRetrievedTS = null;
                Intent intent = new Intent(Const.ACTION_TRIP_UPDATED);
                try {

                    ItineraryReply reply = (ItineraryReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {

                            // Add the itin locator.
                            intent.putExtra(Const.EXTRA_ITIN_LOCATOR, request.itinLocator);

                            // Set a new update timestamp and insert/update the
                            // new itinerary.
                            lastRetrievedTS = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            IItineraryDBInfo itinInfo = concurService.db.updateItinerary(request.itinLocator,
                                    request.userId, reply.xmlReply, lastRetrievedTS);
                            if (itinInfo != null) {
                                // Set the trip object reference into the
                                // 'itinInfo' object.
                                itinInfo.setItinerary(reply.trip);
                                // Update the in-memory itin cache.
                                ConcurCore app = (ConcurCore) concurService.getApplication();
                                IItineraryCache itinCache = app.getItinCache();
                                if (itinCache != null) {
                                    itinCache.addItinerary(request.itinLocator, itinInfo);
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG
                                            + ".handleMessage(ItineraryRequest): itin cache is null!");
                                }
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".handleMessage(ItineraryRequest): unable to store itinerary!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ItineraryRequest): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ItineraryRequest): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }

                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_ITINERARY_SUMMARY_LIST_REQUEST: {
            try {
                // This is a request to retrieve the current itinerary list
                ServiceRequest request = (ServiceRequest) msg.obj;
                request.run(concurService, msg.what);
            } catch (ClassCastException ccExc) {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".handleMessage(ItinerarySummaryListRequest): msg.obj is not of type ServiceRequest!");
            }
            break;
        }
        case Const.MSG_SSO_QUERY_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_sso_info).toString());

                // This is a request to retrieve SSO log in information.
                CorpSsoQueryRequest request = (CorpSsoQueryRequest) msg.obj;

                Intent intent = new Intent(Const.ACTION_CORP_SSO_QUERY);
                try {
                    ServiceReply reply = request.process(concurService);
                    if (reply != null) {
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                        if (reply.httpStatusCode == HttpStatus.SC_OK) {
                            intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                            if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)
                                    || reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_OK)) {
                                // Save the response.
                                Calendar lastRetrievedTS = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                concurService.db.saveResponse(msg.what, lastRetrievedTS,
                                        ((CorpSsoQueryReply) reply).xmlReply, request.userId);
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(CorpSsoQueryRequest): MWS status("
                                        + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                                intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(CorpSsoQueryRequest): HTTP status("
                                    + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                        }
                        // Regardless of outcome, set the reply on the
                        // application object.
                        ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                        ConcurCore.setCorpSsoQueryReply((CorpSsoQueryReply) reply);
                    } else {
                        // Ensure an intent is defined regardless of outcome.
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, HttpStatus.SC_OK);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, "OK");
                        intent.putExtra(Const.REPLY_STATUS, Const.REPLY_STATUS_SUCCESS);
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_DELETE_REPORT_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.deleting_report).toString());

                ReportDeleteRequest request = (ReportDeleteRequest) msg.obj;

                Intent intent = new Intent(Const.ACTION_DELETE_REPORT);
                try {
                    ServiceReply reply = request.process(concurService);
                    if (reply != null) {
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                        if (reply.httpStatusCode == HttpStatus.SC_OK) {
                            intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                            if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                // Punt the report from the in-memory cache.
                                ConcurCore app = (ConcurCore) concurService.getApplication();
                                IExpenseReportCache expRepCache = app.getExpenseActiveCache();
                                // Check if Report has expenses to determine if we need to refresh Expenses list
                                Boolean reportHasExpenses = false;
                                List<ExpenseReportEntry> expenseEntries = expRepCache.getReportEntries(request.rptKey);
                                if (expenseEntries != null && expenseEntries.size() > 0) {
                                    reportHasExpenses = true;
                                }
                                // Punt the report from in-memory/persistent
                                // cache.
                                expRepCache.deleteReport(request.rptKey);
                                expRepCache.deleteDetailReport(request.rptKey);

                                // If we deleted a report with expenses in it, we need to set the Expense List to refresh when
                                // it's opened the next time to display those Expenses
                                if (reportHasExpenses) {
                                    app.getExpenseEntryCache().setShouldFetchExpenseList();
                                }
                                // Set the flag the report list should be
                                // refreshed.
                                expRepCache.setShouldRefreshReportList();
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(DeleteReportRequest): MWS status("
                                        + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                                intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(DeleteReportRequest): HTTP status("
                                    + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                        }
                    } else {
                        // Ensure an intent is defined regardless of outcome.
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, HttpStatus.SC_OK);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, "OK");
                        intent.putExtra(Const.REPLY_STATUS, Const.REPLY_STATUS_SUCCESS);
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_TRAVEL_REASON_CODE_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_reason_codes)
                        .toString());

                ReasonCodeRequest request = (ReasonCodeRequest) msg.obj;

                Intent intent = new Intent(Const.ACTION_TRAVEL_REASON_CODES_UPDATED);
                try {
                    ReasonCodeReply reply = (ReasonCodeReply) request.process(concurService);
                    if (reply != null) {
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                        if (reply.httpStatusCode == HttpStatus.SC_OK) {
                            intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                            if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                ConcurCore concurCore = (ConcurCore) concurService.getApplication();
                                concurCore.setReasonCodes(reply);
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ReasonCodeRequest): MWS status("
                                        + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                                intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ReasonCodeRequest): HTTP status("
                                    + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                        }
                    } else {
                        // Ensure an intent is defined regardless of outcome.
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, HttpStatus.SC_OK);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, "OK");
                        intent.putExtra(Const.REPLY_STATUS, Const.REPLY_STATUS_SUCCESS);
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_LOGOUT_REQUEST: {
            // TODO: In the future, all this needs to be ripped out and replaced with the Platform LogoutRequestTask.
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.logging_out).toString());

                // This is a request to logout.
                LogoutRequest request = (LogoutRequest) msg.obj;

                Intent intent = new Intent(Const.ACTION_LOGOUT);
                try {
                    ServiceReply reply = request.process(concurService);
                    if (reply != null) {
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                        if (reply.httpStatusCode == HttpStatus.SC_OK) {
                            intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                            if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                Log.d(Const.LOG_TAG, CLS_TAG + ".handleMessage(LogoutRequest): user logged out.");
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(LogoutRequest): MWS status("
                                        + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                                intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(LogoutRequest): HTTP status("
                                    + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                        }
                    } else {
                        // Ensure an intent is defined regardless of outcome.
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, HttpStatus.SC_OK);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, "OK");
                        intent.putExtra(Const.REPLY_STATUS, Const.REPLY_STATUS_SUCCESS);
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                } finally {
                    // Regardless of how control leaves the upper block, ensure
                    // we clear the session information
                    // on the client but only if the session id hasn't been
                    // re-set by a subsequent login request.
                    Preferences.clearSessionIfCurrent(PreferenceManager.getDefaultSharedPreferences(concurService),
                            request.sessionId);

                    // Be sure to clear the OAuth access token as well.
                    Preferences.clearAccessToken();

                    // Clear any A/B Test Info.
                    Preferences.clearABTestInfo(PreferenceManager.getDefaultSharedPreferences(concurService));
                }
                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_CANCEL_CAR_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.canceling_car).toString());
                CancelCarRequest request = (CancelCarRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_CAR_CANCEL_RESULT);
                try {
                    ServiceReply reply = request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        // if (concurService.repostEnabled) {
                        // // Clear the request from the database.
                        // if (!concurService.db.deleteHTTPRequest(request.messageId)) {
                        // Log.e(Const.LOG_TAG, CLS_TAG
                        // + ".handleMessage(CancelCar): unable to delete 'CancelCar' persistent request.");
                        // }
                        // }
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (!reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(CancelCar): MWS status(" + reply.mwsStatus
                                    + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(CancelCar): HTTP status(" + reply.httpStatusCode
                                + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_CANCEL_RAIL_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.canceling_rail).toString());
                CancelRailRequest request = (CancelRailRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_RAIL_CANCEL_RESULT);
                try {
                    ServiceReply reply = request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (!reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(CancelRail): MWS status(" + reply.mwsStatus
                                    + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(CancelRail): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_TRAVEL_CUSTOM_FIELDS_UPDATE_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what,
                        concurService.getText(R.string.retrieve_travel_custom_fields_configuration).toString());

                TravelCustomFieldsUpdateRequest request = (TravelCustomFieldsUpdateRequest) msg.obj;

                Intent intent = new Intent(Const.ACTION_TRAVEL_CUSTOM_FIELDS_UPDATED);
                try {
                    TravelCustomFieldsUpdateReply reply = (TravelCustomFieldsUpdateReply) request
                            .process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            if (reply.config != null) {
                                // Store data in the app
                                ConcurCore app = (ConcurCore) concurService.getApplication();
                                app.setTravelCustomFieldsConfig(reply.config);
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(TravelCustomFieldsUpdate): null config!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(TravelCustomFieldsUpdate): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(TravelCustomFieldsUpdate): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_TRAVEL_CUSTOM_FIELDS_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what,
                        concurService.getText(R.string.retrieve_travel_custom_fields_configuration).toString());

                TravelCustomFieldsRequest request = (TravelCustomFieldsRequest) msg.obj;

                Intent intent = new Intent(Const.ACTION_TRAVEL_CUSTOM_FIELDS);
                try {
                    TravelCustomFieldsReply reply = (TravelCustomFieldsReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            if (reply.config != null) {
                                // Store data in the app
                                ConcurCore app = (ConcurCore) concurService.getApplication();
                                app.setTravelCustomFieldsConfig(reply.config);
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(TravelCustomFields): null config!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(TravelCustomFields): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(TravelCustomFields): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_SYSTEM_CONFIG_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_system_configuration)
                        .toString());

                SystemConfigRequest request = (SystemConfigRequest) msg.obj;

                Calendar lastRetrievedTS = null;
                Intent intent = new Intent(Const.ACTION_SYSTEM_CONFIG_UPDATE);
                try {
                    SystemConfigReply reply = (SystemConfigReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            if (reply.sysConfig != null) {
                                ConcurCore app = (ConcurCore) concurService.getApplication();
                                IExpenseEntryCache expenseCache = app.getExpenseEntryCache();
                                expenseCache.putExpenseTypeInDatabaseMap(concurService, "-1");

                                lastRetrievedTS = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                // Store data in the app
                                if (request.hash != null && request.hash.length() > 0) {
                                    if (reply.sysConfig.getResponseId() != null) {
                                        // Set the response ID in the intent.
                                        // This will permit any receivers to
                                        // determine whether
                                        intent.putExtra(Const.REPLY_STATUS_GC_RESPONSE_ID_KEY,
                                                reply.sysConfig.getResponseId());
                                        if (reply.sysConfig.getResponseId().equalsIgnoreCase(
                                                Const.SC_RESPONSE_NO_CHANGE_ID)) {
                                            // Update the response time in the
                                            // database, but leave the last
                                            // response in tact.
                                            concurService.db.updateResponseTime(msg.what, lastRetrievedTS,
                                                    request.userId);
                                            app.setSystemConfigLastRetrieved(lastRetrievedTS);
                                        } else if (reply.sysConfig.getResponseId().equalsIgnoreCase(
                                                Const.SC_RESPONSE_UPDATED_ID)) {
                                            // Update the entire response.
                                            concurService.db.saveResponse(msg.what, lastRetrievedTS, reply.xmlReply,
                                                    request.userId);
                                            app.setSystemConfig(reply.sysConfig);
                                            app.setSystemConfigLastRetrieved(lastRetrievedTS);
                                        } else {
                                            Log.e(Const.LOG_TAG, CLS_TAG
                                                    + ".handleMessage(SystemConfig): invalid response id: '"
                                                    + reply.sysConfig.getResponseId() + "'.");
                                        }
                                    } else {
                                        Log.e(Const.LOG_TAG, CLS_TAG
                                                + ".handleMessage(SystemConfig): response id is null!");
                                    }
                                } else {
                                    // Save the response.
                                    concurService.db.saveResponse(msg.what, lastRetrievedTS, reply.xmlReply,
                                            request.userId);
                                    // Store data in the app.
                                    app.setSystemConfig(reply.sysConfig);
                                    app.setSystemConfigLastRetrieved(lastRetrievedTS);
                                }
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SystemConfig): null system config!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SystemConfig): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SystemConfig): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_EXCHANGE_RATE_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_exchange_rate)
                        .toString());

                ExchangeRateRequest request = (ExchangeRateRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_EXCHANGE_RATE_UPDATED);
                ExchangeRateReply reply = null;
                try {
                    reply = (ExchangeRateReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.EXTRA_EXPENSE_EXCHANGE_RATE_KEY, reply.exchangeRate);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ExchangeRate): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ExchangeRate): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_DISTANCE_TO_DATE_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_distance_to_date)
                        .toString());

                DistanceToDateRequest request = (DistanceToDateRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_DISTANCE_TO_DATE_RETRIEVED);
                DistanceToDateReply reply = null;
                try {
                    reply = (DistanceToDateReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.EXTRA_EXPENSE_DISTANCE_TO_DATE, reply.distance);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(DistanceToDate): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ExchangeRate): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_SAVE_RECEIPT_REQUEST: {
            try {
                SaveReceiptRequest request = (SaveReceiptRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_RECEIPT_SAVE);

                if (!ConcurCore.isConnected()) {

                    if (request.standaloneReceipt) {
                        // Save it to the database
                        MobileDatabase mdb = concurService.getMobileDatabase();
                        // Kludge and store the time in the display name
                        // Use UTC date formatter instead of local one, since the downstream date/str transformation assumes UTC
                        String now = FormatUtil.XML_DF.format(Calendar.getInstance().getTime());
                        ReceiptShareItem rsi = new ReceiptShareItem(null, null, request.filePath, now, Status.HOLD);
                        ArrayList<ReceiptShareItem> rsList = new ArrayList<ReceiptShareItem>(1);
                        rsList.add(rsi);
                        mdb.insertReceiptShareItems(rsList);
                        // Add the "offline create" flag which indicates this is a standalone
                        // receipt and saved offline.
                        intent.putExtra(Flurry.PARAM_NAME_OFFLINE_CREATE, true);
                    }

                    // Fake out all our status values
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, HttpStatus.SC_OK);
                    intent.putExtra(Const.REPLY_STATUS, Const.REPLY_STATUS_SUCCESS);

                    // And provide a fake receipt ID. This is used by the higher levels to recognize that they need to
                    // save the image path and link it to the expense
                    intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY, SaveReceiptReply.OFFLINE_RECEIPT_ID);

                } else {
                    // Broadcast the start network activity message.
                    broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.saving_receipt).toString());

                    SaveReceiptReply reply = null;
                    try {
                        reply = (SaveReceiptReply) request.process(concurService);
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                        // Saving receipts to Connect can return an 'HttpStatus.SC_CREATE' when the
                        // resource has been created.
                        if (reply.httpStatusCode == HttpStatus.SC_CREATED) {
                            reply.httpStatusCode = HttpStatus.SC_OK;
                        }
                        intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                        if (reply.httpStatusCode == HttpStatus.SC_OK) {
                            intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                            if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY, reply.receiptImageId);
                                // if (concurService.repostEnabled) {
                                // // Clear the request from the database.
                                // if (!concurService.db.deleteHTTPRequest(request.messageId)) {
                                // Log.e(Const.LOG_TAG, CLS_TAG
                                // + ".handleMessage(SaveReceipt): unable to delete request for file '"
                                // + request.filePath + "'.");
                                // }
                                // }
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SaveReceipt): MWS status("
                                        + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                                intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SaveReceipt): HTTP status("
                                    + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } catch (ServiceRequestException srvReqExc) {
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                    } catch (IOException ioExc) {
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                    } finally {
                        // Ensure the image file is not left around.
                        boolean puntFile = false;
                        if (reply != null && Const.REPLY_STATUS_SUCCESS.equalsIgnoreCase(reply.mwsStatus)) {
                            puntFile = true;
                        }

                        if (puntFile && request.deleteReceiptFile) {
                            if (request.filePath != null) {
                                // Ensure we punt the image file.
                                File receiptImageFile = new File(request.filePath);
                                if (receiptImageFile.exists()) {
                                    if (receiptImageFile.delete()) {
                                        Log.d(Const.LOG_TAG, CLS_TAG
                                                + ".handleMessage(SaveReceiptRequest): deleted receipt file '"
                                                + request.filePath + "'.");
                                    } else {
                                        Log.e(Const.LOG_TAG, CLS_TAG
                                                + ".handleMessage(SaveReceiptRequest): failed to delete receipt file '"
                                                + request.filePath + "'.");
                                    }
                                }
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".handleMessage(SaveReceiptRequest): 'request.filePath' is null!");
                            }
                        } else {
                            Log.d(Const.LOG_TAG, CLS_TAG
                                    + ".handleMessage(SaveReceiptRequest): skipped deleting receipt file '"
                                    + request.filePath + "'.");
                        }
                    }
                }
                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_EXPENSE_GET_RECEIPT_IMAGE_URL: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_receipt_url).toString());

                GetReceiptImageUrlRequest request = (GetReceiptImageUrlRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_RECEIPT_IMAGE_URL_DOWNLOADED);
                try {
                    GetReceiptImageUrlReply reply = (GetReceiptImageUrlReply) request.process(concurService);
                    intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY, request.receiptImageId);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_URL_KEY, reply.receiptImageUrl);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetReceiptImageUrl): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetReceiptImageUrl): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_GET_CONDITIONAL_FIELDS: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_expense_types)
                        .toString());
                GetConditionalFieldActionRequest request = (GetConditionalFieldActionRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_CONDITIONAL_FIELDS_DOWNLOADED);
                try {

                    GetConditionalFieldActionReply reply = (GetConditionalFieldActionReply) request
                            .process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            if (request.formFieldKey != null && request.formFieldValue != null
                                    && reply.conditionalFieldActionList != null) {
                                // Place the list of conditional fields into the active cache.
                                ConcurCore app = (ConcurCore) concurService.getApplication();
                                app.setConditionalFieldActionsResults(reply.conditionalFieldActionList);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetConditionalFieldAction): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetConditionalFieldAction): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }

                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_GET_EXPENSE_TYPES: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_expense_types)
                        .toString());

                GetExpenseTypesRequest request = (GetExpenseTypesRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_EXPENSE_TYPES_DOWNLOADED);
                try {
                    GetExpenseTypesReply reply = (GetExpenseTypesReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            if (request.policyKey != null && reply.expenseTypes != null) {
                                // Place the list of report specific expense
                                // types into the active cache.
                                ConcurCore app = (ConcurCore) concurService.getApplication();
                                IExpenseEntryCache expEntCache = app.getExpenseEntryCache();
                                expEntCache.putExpenseTypes(request.policyKey, reply.expenseTypes, concurService);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetExpenseTypes): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetExpenseTypes): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_ATTENDEE_SAVE_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.saving_attendee).toString());

                AttendeeSaveRequest request = (AttendeeSaveRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_ATTENDEE_SAVE);
                try {
                    AttendeeSaveReply reply = (AttendeeSaveReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // Set the last search results on the application.
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            ConcurCore.setAttendeeSaveResults(reply);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AttendeeSaveRequest): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AttendeeSaveRequest): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_ATTENDEE_SEARCH_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_attendee_search_list)
                        .toString());

                AttendeeSearchRequest request = (AttendeeSearchRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_ATTENDEE_SEARCH_UPDATED);
                try {
                    AttendeeSearchReply reply = (AttendeeSearchReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // Set the last search results on the application.
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            ConcurCore.setAttendeeSearchResults(reply);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AttendeeSearchRequest): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AttendeeSearchRequest): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_EXTENDED_ATTENDEE_SEARCH_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_attendee_search_list)
                        .toString());

                ExtendedAttendeeSearchRequest request = (ExtendedAttendeeSearchRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_EXTENDED_ATTENDEE_SEARCH_UPDATED);
                try {
                    ExtendedAttendeeSearchReply reply = (ExtendedAttendeeSearchReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // Set the last search results on the application.
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            ConcurCore.setExtendedAttendeeSearchResults(reply);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ExtendedAttendeeSearchRequest): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ExtendedAttendeeSearchRequest): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_DEFAULT_ATTENDEE_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_default_attendee_info)
                        .toString());

                DefaultAttendeeRequest request = (DefaultAttendeeRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_DEFAULT_ATTENDEE_DOWNLOADED);
                try {
                    DefaultAttendeeReply reply = (DefaultAttendeeReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS) && reply.attendee != null) {
                            // Set list of attendee types on the active report
                            // cache.
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            IExpenseReportCache expRepCache = ConcurCore.getExpenseActiveCache();
                            expRepCache.setDefaultAttendee(reply.attendee);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(DefaultAttendeeRequest): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(DefaultAttendeeRequest): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_ATTENDEE_FORM_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_attendee_form)
                        .toString());

                AttendeeFormRequest request = (AttendeeFormRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_ATTENDEE_FORM_DOWNLOADED);
                try {
                    AttendeeFormReply reply = (AttendeeFormReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // Set list of attendee types on the active report
                            // cache.
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            ConcurCore.setAttendeeForm(reply.attendee);
                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_KEY, request.atnTypeKey);
                            intent.putExtra(Const.EXTRA_EXPENSE_ATTENDEE_KEY, request.atnKey);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AttendeeFormRequest): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AttendeeFormRequest): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_GET_ATTENDEE_TYPES_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_attendee_types)
                        .toString());

                SearchListRequest request = (SearchListRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_ATTENDEE_TYPES_DOWNLOADED);
                try {
                    SearchListReply reply = (SearchListReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // Set list of attendee types on the active report
                            // cache.
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            IExpenseReportCache expRepCache = ConcurCore.getExpenseActiveCache();
                            if (request.ftCode != null) {
                                if (request.ftCode.equalsIgnoreCase(Const.ATTENDEE_SEARCH_LIST_FT_CODE)) {
                                    expRepCache.setSearchAttendeeTypes(reply.response.listItems);
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG
                                                    + ".handleMessage(AttendeeTypesRequest): ftCode is non-null and not for attendee search!");
                                }
                            } else {
                                expRepCache.setAddAttendeeTypes(reply.response.listItems);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AttendeeTypesRequest): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AttendeeTypesRequest): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_ATTENDEE_SEARCH_FIELDS_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_attendee_search_fields)
                        .toString());
                AttendeeSearchFieldsRequest request = (AttendeeSearchFieldsRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_ATTENDEE_SEARCH_FIELDS_DOWNLOADED);
                try {
                    AttendeeSearchFieldsReply reply = (AttendeeSearchFieldsReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // Set list of attendee search fields on the active
                            // report cache.
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            IExpenseReportCache expRepCache = ConcurCore.getExpenseActiveCache();
                            expRepCache.setAttendeeSearchFields(reply.atnSrchFlds);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AttendeeSearchFieldsRequest): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AttendeeSearchFieldsRequest): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_APPROVER_SEARCH_REQUEST: {

            // Handle approver search request
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_approver_search_list)
                        .toString());

                ApproverSearchRequest request = (ApproverSearchRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_APPROVER_SEARCH_UPDATED);
                try {
                    ApproverSearchReply reply = (ApproverSearchReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // Set the last search results on the application.
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            ConcurCore.setApproverSearchResults(reply);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ApproverSearchRequest): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ApproverSearchRequest): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_EXPENSE_SEARCH_LIST_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_search_list).toString());

                SearchListRequest request = (SearchListRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_SEARCH_LIST_UPDATED);
                try {
                    SearchListReply reply = (SearchListReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // Set the last search results on the application.
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            ConcurCore.setExpenseSearchListResults(reply.response);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SearchListRequest): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SearchListRequest): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_CURRENCY_SEARCH_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_currency).toString());

                SearchListRequest request = (SearchListRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_CURRENCY_SEARCH_UPDATED);
                try {
                    SearchListReply reply = (SearchListReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.EXTRA_EXPENSE_CURRENCY_SEARCH_RESULTS, reply.response);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SearchListRequest): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SearchListRequest): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_DELETE_RECEIPT_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.deleting_receipt).toString());
                DeleteReceiptImageRequest request = (DeleteReceiptImageRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_RECEIPT_DELETED);
                try {
                    ActionStatusServiceReply reply = (ActionStatusServiceReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY, request.receiptImageId);
                            // Punt the receipt from the receipt store cache.
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            ReceiptStoreCache rcptStoreCache = ConcurCore.getReceiptStoreCache();
                            rcptStoreCache.deleteReceiptInfo(request.receiptImageId);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(DeleteReceipt): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(DeleteReceipt): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                        intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_APPEND_RECEIPT_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.appending_receipt).toString());
                AppendReceiptImageRequest request = (AppendReceiptImageRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_RECEIPT_APPENDED);
                try {
                    ActionStatusServiceReply reply = (ActionStatusServiceReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.EXTRA_EXPENSE_FROM_RECEIPT_IMAGE_ID_KEY, request.fromReceiptImageId);
                            intent.putExtra(Const.EXTRA_EXPENSE_TO_RECEIPT_IMAGE_ID_KEY, request.toReceiptImageId);
                            // Punt the receipt that was appended from the receipt store cache as the
                            // server will remove it from the Receipt Store.
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            ReceiptStoreCache rcptStoreCache = ConcurCore.getReceiptStoreCache();
                            rcptStoreCache.deleteReceiptInfo(request.fromReceiptImageId);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AppendReceipt): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AppendReceipt): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                        intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_RETRIEVE_URL_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_url).toString());
                RetrieveURLRequest request = (RetrieveURLRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_RETRIEVE_URL);
                try {
                    ServiceReply reply = request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.EXTRA_EXPENSE_FILE_PATH, request.filePath.getAbsolutePath());
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(RetrieveURLRequest): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(RetrieveURLRequest): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_GET_RECEIPT_IMAGE_URLS: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_receipt_urls)
                        .toString());

                GetReceiptImageUrlsRequest request = (GetReceiptImageUrlsRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_RECEIPT_IMAGE_URLS_DOWNLOADED);
                try {
                    GetReceiptImageUrlsReply reply = (GetReceiptImageUrlsReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if ((reply.mwsStatus != null) && (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS))) {
                            Calendar lastRetrievedTS = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            // Save the response.
                            concurService.db.saveResponse(msg.what, lastRetrievedTS, reply.xmlReply, request.userId);
                            // Update the in-memory cache.
                            ConcurCore app = (ConcurCore) concurService.getApplication();
                            ReceiptStoreCache receiptStoreCache = app.getReceiptStoreCache();
                            receiptStoreCache.setReceiptInfoList(reply.receiptInfos, lastRetrievedTS);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetReceiptImageUrls): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetReceiptImageUrls): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_SAVE_REPORT_ENTRY_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.saving_report_entry).toString());

                SaveReportEntryRequest request = (SaveReportEntryRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_REPORT_ENTRY_SAVE);
                SaveReportEntryReply reply = null;
                try {
                    reply = (SaveReportEntryReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            if (reply.expRepEntDet != null) {

                                // Put the key into the response in case the
                                // caller needs it (e.g. new entry creation)
                                intent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, reply.expRepEntDet.reportEntryKey);

                                // Grab a reference to the ExpenseReportDetail
                                // object.
                                ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                                IExpenseReportCache expRepCache = ConcurCore.getExpenseActiveCache();
                                if (expRepCache.hasReportDetail(reply.expRepEntDet.rptKey)) {
                                    ExpenseReportDetail expRepDet = expRepCache
                                            .getReportDetail(reply.expRepEntDet.rptKey);
                                    if (expRepDet != null) {
                                        // Replace or add the report detail
                                        // entry from the reply.
                                        expRepDet.replaceOrAddReportEntry(reply.expRepEntDet);
                                        // Update the 'reportTotalApproved'
                                        // attribute.
                                        if (reply.reportTotalApproved != null) {
                                            expRepDet.totalApprovedAmount = reply.reportTotalApproved;
                                        }
                                        // Update the 'reportTotalClaimed'
                                        // attribute.
                                        if (reply.reportTotalClaimed != null) {
                                            expRepDet.totalClaimedAmount = reply.reportTotalClaimed;
                                        }
                                        // Update the 'reportTotalPosted'
                                        // attribute.
                                        if (reply.reportTotalPosted != null) {
                                            expRepDet.totalPostedAmount = reply.reportTotalPosted;
                                        }
                                        // Update the persistence tier for both
                                        // the report header and the report
                                        // entry.
                                        Calendar updateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                        IExpenseReportInfo reportInfo = ReportDBUtil.updateReportHeader(
                                                concurService.db, IExpenseReportInfo.ReportType.ACTIVE, expRepDet,
                                                request.userId, updateTime);
                                        ReportDBUtil.updateReportEntry(concurService.db, expRepDet,
                                                IExpenseReportInfo.ReportType.ACTIVE, reply.expRepEntDet,
                                                request.userId, updateTime, true);
                                        // Replace in the in-memory cache.
                                        expRepCache.putReportDetail(reportInfo);
                                    } else {
                                        Log.e(Const.LOG_TAG,
                                                CLS_TAG
                                                        + ".handleMessage(SaveReportEntry): 'hasReportDetail' returned 'true', "
                                                        + "but 'getReportDetail' returned null!");
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG
                                                    + ".handleMessage(SaveReportEntry): unable to locate expense report detail object "
                                                    + "in active cache!");
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG
                                                + ".handleMessage(SaveReportEntry): reply was successful but expense report entry detail object is null!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SaveReportEntry): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SaveReportEntry): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }

        case Const.MSG_EXPENSE_ITEMIZE_HOTEL_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.itemizing_hotel).toString());

                ItemizeHotelRequest request = (ItemizeHotelRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_HOTEL_ITEMIZED);
                ItemizeHotelReply reply = null;
                try {
                    reply = (ItemizeHotelReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // if (concurService.repostEnabled) {
                            // // Clear the request from the database.
                            // if (!concurService.db.deleteHTTPRequest(request.messageId)) {
                            // Log.e(Const.LOG_TAG,
                            // CLS_TAG
                            // + ".handleMessage(ItemizeHotel): unable to delete request to itemize hotel.");
                            // }
                            // }
                            if (reply.expRepEntDet != null) {
                                // Grab a reference to the ExpenseReportDetail
                                // object.
                                ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                                IExpenseReportCache expRepCache = ConcurCore.getExpenseActiveCache();
                                if (expRepCache.hasReportDetail(reply.expRepEntDet.rptKey)) {
                                    ExpenseReportDetail expRepDet = expRepCache
                                            .getReportDetail(reply.expRepEntDet.rptKey);
                                    if (expRepDet != null) {
                                        // Replace or add the report detail
                                        // entry from the reply.
                                        expRepDet.replaceOrAddReportEntry(reply.expRepEntDet);

                                        // Update the 'reportTotalApproved'
                                        // attribute.
                                        if (reply.reportTotalApproved != null) {
                                            expRepDet.totalApprovedAmount = reply.reportTotalApproved;
                                        }
                                        // Update the 'reportTotalClaimed'
                                        // attribute.
                                        if (reply.reportTotalClaimed != null) {
                                            expRepDet.totalClaimedAmount = reply.reportTotalClaimed;
                                        }
                                        // Update the 'reportTotalPosted'
                                        // attribute.
                                        if (reply.reportTotalPosted != null) {
                                            expRepDet.totalPostedAmount = reply.reportTotalPosted;
                                        }
                                        // Update persistence for both the
                                        // report header and entry.
                                        Calendar updateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                        IExpenseReportInfo reportInfo = ReportDBUtil.updateReportHeader(
                                                concurService.db, IExpenseReportInfo.ReportType.ACTIVE, expRepDet,
                                                request.userId, updateTime);
                                        ReportDBUtil.updateReportEntry(concurService.db, expRepDet,
                                                IExpenseReportInfo.ReportType.ACTIVE, reply.expRepEntDet,
                                                request.userId, updateTime, true);
                                        // Replace in the in-memory cache.
                                        expRepCache.putReportDetail(reportInfo);
                                    } else {
                                        Log.e(Const.LOG_TAG, CLS_TAG
                                                + ".handleMessage(ItemizeHotel): 'hasReportDetail' returned 'true', "
                                                + "but 'getReportDetail' returned null!");
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG
                                                    + ".handleMessage(ItemizeHotel): unable to locate expense report detail object "
                                                    + "in active cache!");
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG
                                                + ".handleMessage(ItemizeHotel): reply was successful but expense report entry detail object is null!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ItemizeHotel): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ItemizeHotel): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_CLEAR_REPORT_ENTRY_RECEIPT_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.clearing_report_entry_receipt)
                        .toString());

                ClearReportEntryReceiptRequest request = (ClearReportEntryReceiptRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_REPORT_ENTRY_RECEIPT_CLEAR);
                ClearReportEntryReceiptReply reply = null;
                try {
                    reply = (ClearReportEntryReceiptReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // if (concurService.repostEnabled) {
                            // // Clear the request from the database.
                            // if (!concurService.db.deleteHTTPRequest(request.messageId)) {
                            // Log.e(Const.LOG_TAG,
                            // CLS_TAG
                            // + ".handleMessage(ClearReportEntryReceipt): unable to delete request to save the report entry.");
                            // }
                            // }
                            if (reply.expRepEntDet != null) {
                                // Update the in-memory detailed report object.
                                // Grab a reference to the ExpenseReportDetail
                                // object.
                                ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                                IExpenseReportCache expRepCache = ConcurCore.getExpenseActiveCache();
                                if (expRepCache.hasReportDetail(reply.expRepEntDet.rptKey)) {
                                    ExpenseReportDetail expRepDet = expRepCache
                                            .getReportDetail(reply.expRepEntDet.rptKey);
                                    if (expRepDet != null) {
                                        // Replace or add the report detail
                                        // entry from the reply.
                                        expRepDet.replaceOrAddReportEntry(reply.expRepEntDet);
                                        // The current SaveReportEntryReceipt
                                        // MWS call will return the same report
                                        // entry that
                                        // was passed into it, so
                                        // 'reply.expRepEntDet' may in fact be
                                        // an entry itemization. Since
                                        // the current persistence tier supports
                                        // persisting an entry at the parent
                                        // entry level, we
                                        // need to check that
                                        // 'reply.expRepEntDet.parentReportEntryKey'
                                        // is not set, if it is, then
                                        // we
                                        // need to look up the entry detail
                                        // representing the parent entry and
                                        // save at that level.
                                        ExpenseReportEntry saveEntry = reply.expRepEntDet;
                                        if (reply.expRepEntDet.parentReportEntryKey != null
                                                && reply.expRepEntDet.parentReportEntryKey.length() > 0) {
                                            saveEntry = expRepCache.getReportEntry(expRepDet,
                                                    reply.expRepEntDet.reportEntryKey);
                                        }
                                        if (saveEntry != null) {
                                            // Update the persistence tier.
                                            Calendar updateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                            IExpenseReportInfo reportInfo = ReportDBUtil.updateReportEntry(
                                                    concurService.db, expRepDet, IExpenseReportInfo.ReportType.ACTIVE,
                                                    saveEntry, request.userId, updateTime, true);
                                            // Replace in the in-memory cache.
                                            expRepCache.putReportDetail(reportInfo);
                                        } else {
                                            Log.e(Const.LOG_TAG,
                                                    CLS_TAG
                                                            + ".handleMessage(ClearReportEntryReceipt): unable to locate entry to persist!");
                                        }
                                    } else {
                                        Log.e(Const.LOG_TAG,
                                                CLS_TAG
                                                        + ".handleMessage(ClearReportEntryReceipt): 'hasReportDetail' returned 'true', "
                                                        + "but 'getReportDetail' returned null!");
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG
                                                    + ".handleMessage(ClearReportEntryReceipt): unable to locate expense report detail object "
                                                    + "in active cache!");
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG
                                                + ".handleMessage(ClearReportEntryReceipt): reply was successful but report detail object is null!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ClearReportEntryReceipt): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ClearReportEntryReceipt): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_SAVE_REPORT_ENTRY_RECEIPT_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.saving_report_entry_receipt)
                        .toString());

                SaveReportEntryReceiptRequest request = (SaveReportEntryReceiptRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_REPORT_ENTRY_RECEIPT_SAVE);
                SaveReportEntryReceiptReply reply = null;
                try {
                    reply = (SaveReportEntryReceiptReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // if (concurService.repostEnabled) {
                            // // Clear the request from the database.
                            // if (!concurService.db.deleteHTTPRequest(request.messageId)) {
                            // Log.e(Const.LOG_TAG,
                            // CLS_TAG
                            // + ".handleMessage(SaveReportEntryReceipt): unable to delete request to save the report entry.");
                            // }
                            // }
                            if (reply.expRepEntDet != null) {
                                // Update the in-memory detailed report object.
                                // Grab a reference to the ExpenseReportDetail
                                // object.
                                ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                                IExpenseReportCache expRepCache = ConcurCore.getExpenseActiveCache();
                                if (expRepCache.hasReportDetail(reply.expRepEntDet.rptKey)) {
                                    ExpenseReportDetail expRepDet = expRepCache
                                            .getReportDetail(reply.expRepEntDet.rptKey);
                                    if (expRepDet != null) {
                                        // Replace or add the report detail
                                        // entry from the reply.
                                        expRepDet.replaceOrAddReportEntry(reply.expRepEntDet);
                                        // The current SaveReportEntryReceipt
                                        // MWS call will return the same report
                                        // entry that
                                        // was passed into it, so
                                        // 'reply.expRepEntDet' may in fact be
                                        // an entry itemization. Since
                                        // the current persistence tier supports
                                        // persisting an entry at the parent
                                        // entry level, we
                                        // need to check that
                                        // 'reply.expRepEntDet.parentReportEntryKey'
                                        // is not set, if it is, then
                                        // we
                                        // need to look up the entry detail
                                        // representing the parent entry and
                                        // save at that level.
                                        ExpenseReportEntry saveEntry = reply.expRepEntDet;
                                        if (reply.expRepEntDet.parentReportEntryKey != null
                                                && reply.expRepEntDet.parentReportEntryKey.length() > 0) {
                                            saveEntry = expRepCache.getReportEntry(expRepDet,
                                                    reply.expRepEntDet.reportEntryKey);
                                        }
                                        if (saveEntry != null) {
                                            // Update the persistence tier.
                                            Calendar updateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                            IExpenseReportInfo reportInfo = ReportDBUtil.updateReportEntry(
                                                    concurService.db, expRepDet, IExpenseReportInfo.ReportType.ACTIVE,
                                                    saveEntry, request.userId, updateTime, true);
                                            // Replace in the in-memory cache.
                                            expRepCache.putReportDetail(reportInfo);
                                        } else {
                                            Log.e(Const.LOG_TAG,
                                                    CLS_TAG
                                                            + ".handleMessage(SaveReportEntryReceipt): unable to locate entry to persist!");
                                        }
                                    } else {
                                        Log.e(Const.LOG_TAG,
                                                CLS_TAG
                                                        + ".handleMessage(SaveReportEntryReceipt): 'hasReportDetail' returned 'true', "
                                                        + "but 'getReportDetail' returned null!");
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG
                                                    + ".handleMessage(SaveReportEntryReceipt): unable to locate expense report detail object "
                                                    + "in active cache!");
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG
                                                + ".handleMessage(SaveReportEntryReceipt): reply was successful but report detail object is null!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SaveReportEntryReceipt): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SaveReportEntryReceipt): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_SAVE_REPORT_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.saving_report).toString());

                SaveReportRequest request = (SaveReportRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_REPORT_SAVE);
                SaveReportReply reply = null;
                try {
                    reply = (SaveReportReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // if (concurService.repostEnabled) {
                            // // Clear the request from the database.
                            // if (!concurService.db.deleteHTTPRequest(request.messageId)) {
                            // Log.e(Const.LOG_TAG,
                            // CLS_TAG
                            // + ".handleMessage(SaveReportEntry): unable to delete request to save the report.");
                            // }
                            // }
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            IExpenseReportCache expRepCache = ConcurCore.getExpenseActiveCache();
                            // Update the persistence tier.
                            if (reply.reportDetail != null) {
                                Calendar updateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                IExpenseReportInfo reportInfo = ReportDBUtil.updateReport(concurService.db,
                                        reply.reportDetail, IExpenseReportInfo.ReportType.ACTIVE, true, request.userId,
                                        updateTime);
                                expRepCache.putReportDetail(reportInfo);
                                // Add the report key so the caller can
                                // reference the report detail.
                                intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, reply.reportDetail.reportKey);
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SaveReportRequest): "
                                        + "reply was successful but report detail is null!");
                            }
                            // Clear the pending report flag.
                            expRepCache.setDetailedReportRequestPending(false);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SaveReport): MWS status(" + reply.mwsStatus
                                    + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SaveReport): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_ADD_REPORT_RECEIPT_V2_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.add_report_receipt).toString());

                AddReportReceiptV2Request request = (AddReportReceiptV2Request) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_ADD_REPORT_RECEIPT);
                ActionStatusServiceReply reply = null;
                try {
                    reply = (ActionStatusServiceReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // if (concurService.repostEnabled) {
                            // // Clear the request from the database.
                            // if (!concurService.db.deleteHTTPRequest(request.messageId)) {
                            // Log.e(Const.LOG_TAG,
                            // CLS_TAG
                            // + ".handleMessage(AddReportReceiptV2): unable to delete request to add a report receipt.");
                            // }
                            // }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AddReportReceiptV2): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AddReportReceiptV2): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                } finally {
                    // Ensure the image file is not left around.
                    boolean puntFile = false;
                    if (reply != null && reply.mwsStatus != null
                            && reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                        puntFile = true;
                        // } else {
                        // puntFile = true; //!concurService.repostEnabled;
                    }
                    if (puntFile && request.deleteReceiptFile) {
                        if (request.filePath != null) {
                            // Ensure we punt the image file.
                            File receiptImageFile = new File(request.filePath);
                            if (receiptImageFile.exists()) {
                                if (receiptImageFile.delete()) {
                                    Log.d(Const.LOG_TAG, CLS_TAG
                                            + ".handleMessage(AddReportReceiptV2): deleted receipt file '"
                                            + request.filePath + "'.");
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG
                                            + ".handleMessage(AddReportReceiptV2): failed to delete receipt file '"
                                            + request.filePath + "'.");
                                }
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".handleMessage(AddReportReceiptV2): 'request.filePath' is null!");
                        }
                    } else {
                        Log.d(Const.LOG_TAG, CLS_TAG
                                + ".handleMessage(AddReportReceiptV2): skipped deleting receipt file '"
                                + request.filePath + "'.");
                    }
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_ADD_REPORT_RECEIPT_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.add_report_receipt).toString());

                AddReportReceiptRequest request = (AddReportReceiptRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_ADD_REPORT_RECEIPT);
                ActionStatusServiceReply reply = null;
                try {
                    reply = (ActionStatusServiceReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // if (concurService.repostEnabled) {
                            // // Clear the request from the database.
                            // if (!concurService.db.deleteHTTPRequest(request.messageId)) {
                            // Log.e(Const.LOG_TAG,
                            // CLS_TAG
                            // + ".handleMessage(AddReportReceipt): unable to delete request to add a report receipt.");
                            // }
                            // }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AddReportReceipt): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AddReportReceipt): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        default:
            handleMessage2(msg);
        }

    }

    /**
     * Handle messages as they are sent here via the looper. This is a fall through method used to reduce the size of the original
     * method. This is needed in order to fit it in on Android 2.2.
     */
    public void handleMessage2(Message msg) {
        switch (msg.what) {
        case Const.MSG_USER_CONFIG_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_user_configuration)
                        .toString());

                UserConfigRequest request = (UserConfigRequest) msg.obj;

                Calendar lastRetrievedTS = null;
                Intent intent = new Intent(Const.ACTION_USER_CONFIG_UPDATE);
                try {
                    UserConfigReply reply = (UserConfigReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            if (reply.config != null) {
                                lastRetrievedTS = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                // Store data in the app
                                ConcurCore app = (ConcurCore) concurService.getApplication();
                                if (request.hash != null && request.hash.length() > 0) {
                                    String respId = reply.config.responseId;
                                    if (respId != null) {
                                        // Set the response ID in the intent.
                                        // This will permit any receivers to
                                        // determine whether
                                        intent.putExtra(Const.REPLY_STATUS_GC_RESPONSE_ID_KEY, respId);
                                        if (respId.equalsIgnoreCase(Const.SC_RESPONSE_NO_CHANGE_ID)) {
                                            // Update the response time in the
                                            // database, but leave the last
                                            // response intact.
                                            concurService.db.updateResponseTime(msg.what, lastRetrievedTS,
                                                    request.userId);
                                            app.setUserConfigLastRetrieved(lastRetrievedTS);
                                        } else if (respId.equalsIgnoreCase(Const.SC_RESPONSE_UPDATED_ID)) {
                                            // Update the entire response.
                                            concurService.db.saveResponse(msg.what, lastRetrievedTS, reply.xmlReply,
                                                    request.userId);
                                            app.setUserConfig(reply.config);
                                            app.setUserConfigLastRetrieved(lastRetrievedTS);
                                        } else {
                                            Log.e(Const.LOG_TAG, CLS_TAG
                                                    + ".handleMessage(UserConfig): invalid response id: '" + respId
                                                    + "'.");
                                        }
                                    } else {
                                        Log.e(Const.LOG_TAG, CLS_TAG
                                                + ".handleMessage(UserConfig): response id is null!");
                                    }
                                } else {
                                    // Save the response.
                                    concurService.db.saveResponse(msg.what, lastRetrievedTS, reply.xmlReply,
                                            request.userId);
                                    // Store data in the app.
                                    app.setUserConfig(reply.config);
                                    app.setUserConfigLastRetrieved(lastRetrievedTS);
                                }
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(UserConfig): null user config!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(UserConfig): MWS status(" + reply.mwsStatus
                                    + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(UserConfig): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_TRAVEL_RAIL_STATION_LIST_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_rail_station_list)
                        .toString());

                RailStationListRequest request = (RailStationListRequest) msg.obj;

                Calendar lastRetrievedTS = null;
                Intent intent = new Intent(Const.ACTION_RAIL_STATION_LIST_RESULTS);
                try {
                    RailStationListReply reply = (RailStationListReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            if (reply.railStations != null) {
                                // Save the response (unencrypted).
                                lastRetrievedTS = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                concurService.db.saveResponse(msg.what, lastRetrievedTS, reply.xmlReply,
                                        request.userId, false);
                                // Store data in the app.
                                ConcurCore app = (ConcurCore) concurService.getApplication();
                                app.setRailStationList(reply.railStations);
                                app.setCodeRailStationMap(reply.codeStationMap);
                                app.setRailStationListLastRetrieved(lastRetrievedTS);
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(RailStationList): null station list!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(RailStationList): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(RailStationList): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_ACTIVE_REPORTS_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_active_reports)
                        .toString());

                // This is a request to retrieve the active report list
                ActiveReportsRequest request = (ActiveReportsRequest) msg.obj;

                Calendar lastRetrievedTS = null;
                Intent intent = new Intent(Const.ACTION_EXPENSE_ACTIVE_REPORTS_UPDATED);
                try {
                    ActiveReportsReply reply = (ActiveReportsReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {

                            // Iterate over each active report and update the
                            // persistence tier.
                            List<IExpenseReportInfo> reportInfos = null;
                            lastRetrievedTS = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            if (reply.reports != null) {
                                reportInfos = new ArrayList<IExpenseReportInfo>(reply.reports.size());
                                for (ExpenseReport report : reply.reports) {
                                    IExpenseReportDBInfo reportInfo = ReportDBUtil.updateReport(concurService.db,
                                            report, IExpenseReportInfo.ReportType.ACTIVE, false, request.userId,
                                            lastRetrievedTS);
                                    if (reportInfo != null) {
                                        reportInfos.add(reportInfo);
                                    }
                                }
                            }

                            // Store data in the app
                            ConcurCore app = (ConcurCore) concurService.getApplication();
                            if (reply.reports != null) {
                                IExpenseReportCache expRepCache = app.getExpenseActiveCache();
                                IExpenseReportListInfo expRepListInfo = new ExpenseReportListInfo(reportInfos,
                                        lastRetrievedTS);
                                expRepCache.setReportList(expRepListInfo);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetActiveReports): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetActiveReports): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send broadcast
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_REPORT_APPROVAL_LIST_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_reports_to_approve)
                        .toString());

                // This is a request to retrieve the current list of reports to
                // approve.
                ReportsToApproveRequest request = (ReportsToApproveRequest) msg.obj;

                // Process the request.
                Intent intent = new Intent(Const.ACTION_EXPENSE_APPROVAL_REPORTS_UPDATED);
                try {
                    ReportsToApproveReply reply = (ReportsToApproveReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {

                            // Update persistence.
                            Calendar updateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            List<IExpenseReportInfo> reportInfos = null;
                            if (reply.reports != null) {
                                reportInfos = new ArrayList<IExpenseReportInfo>(reply.reports.size());
                                for (ExpenseReport report : reply.reports) {
                                    IExpenseReportInfo reportInfo = ReportDBUtil.updateReport(concurService.db, report,
                                            IExpenseReportInfo.ReportType.APPROVAL, false, request.userId, updateTime);
                                    reportInfos.add(reportInfo);
                                }
                            }
                            IExpenseReportListInfo reportListInfo = new ExpenseReportListInfo(reportInfos, updateTime);

                            // Set the new list of reports to be approved.
                            ConcurCore app = (ConcurCore) concurService.getApplication();
                            app.getExpenseApprovalCache().setReportList(reportListInfo);
                            app.getExpenseApprovalCache().clearDetailReportsNotInApproveList();
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetReportsToApprove): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Broadcast the completion message
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_REPORT_HEADER_DETAIL_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.update_report_detail).toString());

                // This is a request to retrieve report details.
                ReportHeaderDetailRequest request = (ReportHeaderDetailRequest) msg.obj;

                // Process the request.
                Intent intent = new Intent(Const.ACTION_EXPENSE_REPORT_HEADER_DETAIL_UPDATED);
                try {
                    ReportHeaderDetailReply reply = (ReportHeaderDetailReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, request.reportKey);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // Locate the report detail object currently in the
                            // cache.
                            IExpenseReportCache expRepCache = null;
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            ReportType type = null;
                            switch (request.reportSourceKey) {
                            case Const.EXPENSE_REPORT_SOURCE_ACTIVE:
                                expRepCache = ConcurCore.getExpenseActiveCache();
                                type = IExpenseReportInfo.ReportType.ACTIVE;
                                break;
                            case Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL:
                                expRepCache = ConcurCore.getExpenseApprovalCache();
                                type = IExpenseReportInfo.ReportType.APPROVAL;
                                break;
                            default:
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".handleMessage(GetReportDetail): invalid report source key of '"
                                        + request.reportSourceKey + "'.");
                                break;
                            }
                            if (expRepCache != null) {
                                ExpenseReportDetail updateReportDetail = expRepCache.getReportDetail(request.reportKey);
                                if (updateReportDetail != null) {
                                    if (reply.reportDetail != null) {

                                        // Update the report header information
                                        // from the retrieved report with the
                                        // one
                                        // currently in memory. NOTE: The newly
                                        // retrieved report header detail does
                                        // not
                                        // contain detailed entries, just
                                        // summary entry objects.
                                        updateReportDetail.updateHeader(reply.reportDetail);
                                        // Update the persistence tier.
                                        Calendar updateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                        IExpenseReportInfo reportInfo = ReportDBUtil.updateReportHeader(
                                                concurService.db, type, updateReportDetail, request.userId, updateTime);
                                        // Update the in-memory cache.
                                        expRepCache.putReportDetail(reportInfo);
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetReportHeaderDetail): "
                                            + "unable to locate report header to update with report key '"
                                            + request.reportKey + "'");
                                }
                            }
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetReportHeaderDetail): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Broadcast the response.
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_REPORT_ENTRY_DETAIL_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_report_entry_detail)
                        .toString());

                // This is a request to retrieve report details.
                ReportEntryDetailRequest request = (ReportEntryDetailRequest) msg.obj;

                // Process the request.
                Intent intent = new Intent(Const.ACTION_EXPENSE_REPORT_ENTRY_DETAIL_UPDATED);
                try {
                    ReportEntryDetailReply reply = (ReportEntryDetailReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, request.reportEntryKey);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // Update the cache.
                            if (request.reportSourceKey == Const.EXPENSE_REPORT_SOURCE_ACTIVE
                                    || request.reportSourceKey == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {
                                ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                                IExpenseReportCache expRepCache = null;
                                ReportType reportType = null;
                                switch (request.reportSourceKey) {
                                case Const.EXPENSE_REPORT_SOURCE_ACTIVE:
                                    expRepCache = ConcurCore.getExpenseActiveCache();
                                    reportType = IExpenseReportInfo.ReportType.ACTIVE;
                                    break;
                                case Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL:
                                    expRepCache = ConcurCore.getExpenseApprovalCache();
                                    reportType = IExpenseReportInfo.ReportType.APPROVAL;
                                    break;
                                }
                                // Grab a reference to the detail report to be
                                // updated.
                                ExpenseReportDetail reportDetail = expRepCache.getReportDetail(request.reportKey);
                                if (reportDetail != null) {
                                    // Update the in-memory representation.
                                    reportDetail.replaceOrAddReportEntry(reply.expRepEntDet);
                                    // Update the persistence tier.
                                    Calendar updateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                    ReportDBUtil.updateReportEntry(concurService.db, reportDetail, reportType,
                                            reply.expRepEntDet, request.userId, updateTime, true);
                                    // Add to the intent the report key and
                                    // report entry key.
                                    intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, reportDetail.reportKey);
                                    intent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY,
                                            reply.expRepEntDet.reportEntryKey);
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetReportEntryDetail): "
                                            + "unable to locate detailed report in cache!");
                                }
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".handleMessage(GetReportEntryDetail): invalid report source key of '"
                                        + request.reportSourceKey + "'.");
                            }
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetReportDetail): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Broadcast the response.
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_REPORT_DETAIL_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_report_detail)
                        .toString());

                // This is a request to retrieve report details.
                ReportDetailRequest request = (ReportDetailRequest) msg.obj;

                // Process the request.
                Intent intent = new Intent(Const.ACTION_EXPENSE_REPORT_DETAIL_UPDATED);
                try {
                    ReportDetailReply reply = (ReportDetailReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, request.reportKey);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS) && reply.report != null) {
                            // Update the cache.
                            if (request.reportSourceKey == Const.EXPENSE_REPORT_SOURCE_ACTIVE
                                    || request.reportSourceKey == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {
                                ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                                IExpenseReportCache expRepCache = null;
                                ReportType reportType = null;
                                switch (request.reportSourceKey) {
                                case Const.EXPENSE_REPORT_SOURCE_ACTIVE:
                                    expRepCache = ConcurCore.getExpenseActiveCache();
                                    reportType = IExpenseReportInfo.ReportType.ACTIVE;
                                    break;
                                case Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL:
                                    expRepCache = ConcurCore.getExpenseApprovalCache();
                                    reportType = IExpenseReportInfo.ReportType.APPROVAL;
                                    break;
                                }
                                // Update the database with the XML
                                // representation of the report detail.
                                Calendar updateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                IExpenseReportInfo reportInfo = ReportDBUtil.updateReport(concurService.db,
                                        reply.report, reportType, true, request.userId, updateTime);
                                // Update the in-memory cache.
                                expRepCache.putReportDetail(reportInfo);
                                // Clear the pending report flag.
                                expRepCache.setDetailedReportRequestPending(false);
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".handleMessage(GetReportDetail): invalid report source key of '"
                                        + request.reportSourceKey + "'.");
                            }
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetReportDetail): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Broadcast the response.
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_REPORT_ITEMIZATION_ENTRY_FORM_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what,
                        concurService.getText(R.string.retrieve_report_itemization_entry_form).toString());

                ReportItemizationEntryFormRequest request = (ReportItemizationEntryFormRequest) msg.obj;

                // Process the request.
                Intent intent = new Intent(Const.ACTION_EXPENSE_REPORT_ITEMIZATION_ENTRY_FORM_UPDATED);
                try {
                    ReportItemizationEntryFormReply reply = (ReportItemizationEntryFormReply) request
                            .process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, request.reportKey);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {

                            // We do not currently cache these in the DB. At the
                            // moment they
                            // will only be used to create new entries (which is
                            // not supported offline)
                            // so we will get it fresh each time to avoid any
                            // problems.

                            // Pass the report key back on through
                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, request.reportKey);

                            // Update the in-memory cache.
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            ConcurCore.setCurrentEntryDetailForm(reply.entryDetail);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetReportItemizationEntryForm): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Broadcast the response.
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_REPORT_ENTRY_FORM_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_report_entry_form)
                        .toString());

                ReportEntryFormRequest request = (ReportEntryFormRequest) msg.obj;

                // Process the request.
                Intent intent = new Intent(Const.ACTION_EXPENSE_REPORT_ENTRY_FORM_UPDATED);
                try {
                    ReportEntryFormReply reply = (ReportEntryFormReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, request.reportKey);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {

                            // We do not currently cache these in the DB. At the
                            // moment they
                            // will only be used to create new entries (which is
                            // not supported offline)
                            // so we will get it fresh each time to avoid any
                            // problems.

                            // Pass the report key back on through
                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, request.reportKey);

                            // Update the in-memory cache.
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            ConcurCore.setCurrentEntryDetailForm(reply.entryDetail);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetReportEntryForm): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Broadcast the response.
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_REPORT_SUBMIT_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.submit_report).toString());

                // This is a request to approve a report.
                SubmitReportRequest request = (SubmitReportRequest) msg.obj;

                // Process the request.
                Intent intent = new Intent(Const.ACTION_EXPENSE_REPORT_SUBMIT_UPDATE);
                try {
                    SubmitReportReply reply = (SubmitReportReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {

                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);

                        // // If re-post is enabled, then clear out the message.
                        // if (concurService.repostEnabled) {
                        // // Clear the request from the database.
                        // if (!concurService.db.deleteHTTPRequest(request.messageId)) {
                        // Log.e(Const.LOG_TAG,
                        // CLS_TAG
                        // +
                        // ".handleMessage(SubmitReport): unable to delete submitted report approval persistent request for report '"
                        // + request.reportKey + "'.");
                        // }
                        // }

                        ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                        boolean needsApprover = true;
                        // MOB-7602 Note that the submit request can return HTTP
                        // 200 OK but is still
                        // hasn't been submitted if this user requires to
                        // manually select an approver.
                        // In this case, don't remove the reportKey from the
                        // report cache.
                        if (!Const.REPLY_STATUS_NO_APPROVER.equalsIgnoreCase(reply.mwsStatus)
                                && !Const.REPLY_STATUS_REVIEW_APPROVAL_FLOW_APPROVER.equalsIgnoreCase(reply.mwsStatus)) {

                            // Remove the report key from the list of pending
                            // submissions in the expense report cache.
                            IExpenseReportCache expAppCache = ConcurCore.getExpenseActiveCache();
                            expAppCache.removeSubmitted(request.reportKey);

                            // Don't need to manually select an approver.
                            needsApprover = false;
                        }

                        // Populate intent with any error message.
                        if (reply.mwsStatus != null && !reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_FAILURE)) {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }

                        // Was a new report detail object parsed?
                        if (reply.reportDetail != null) {

                            // MOB-7602 Don't bother updating the cache if the
                            // report detail
                            // hasn't changed because the user needs to manually
                            // select an approver.
                            // Update the persistence tier.
                            if (!needsApprover) {
                                Calendar updateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                IExpenseReportInfo reportInfo = ReportDBUtil.updateReport(concurService.db,
                                        reply.reportDetail, IExpenseReportInfo.ReportType.ACTIVE, true, request.userId,
                                        updateTime);
                                // Update the in-memory active cache.
                                IExpenseReportCache expRepCache = ConcurCore.getExpenseActiveCache();
                                expRepCache.putReportDetail(reportInfo);

                            }

                            // Update the intent with information indicating a
                            // new report detail was provided.
                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE, Boolean.TRUE);
                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, reply.reportDetail.reportKey);

                        } else {
                            if (reply.mwsStatus != null && reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SubmitReportRequest): "
                                        + "reply was successful but no report detail object was parsed!");
                            }
                        }

                        // Was a default approver parsed?
                        if (reply.defaultApprover != null) {
                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_DEFAULT_APPROVER, reply.defaultApprover);
                        }

                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SubmitReport): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Broadcast the completion message
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_REPORT_APPROVE_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.approve_report).toString());

                // This is a request to approve a report.
                ApproveReportRequest request = (ApproveReportRequest) msg.obj;

                // Process the request.
                Intent intent = new Intent(Const.ACTION_EXPENSE_REPORT_APPROVE);
                try {
                    ReportApproveRejectServiceReply reply = (ReportApproveRejectServiceReply) request
                            .process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        // if (concurService.repostEnabled) {
                        // // Clear the request from the database.
                        // // NOTE: When the client really has an off-line mode
                        // // and re-posts are supported, the request removal
                        // // should be moved up above.
                        // if (!concurService.db.deleteReportSubmittedApprove(request.reportKey)) {
                        // Log.e(Const.LOG_TAG,
                        // CLS_TAG
                        // + ".handleMessage: unable to delete submitted report approval persistent request for report '"
                        // + request.reportKey + "'.");
                        // }
                        // }
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        // Remove the report key from the list of pending
                        // approvals in the expense approval cache.
                        ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                        IExpenseReportCache expAppCache = ConcurCore.getExpenseApprovalCache();
                        expAppCache.removeSubmittedForApprove(request.reportKey);
                        if (!reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                        // Regardless of result from the server, if we have a
                        // non-null list of reports
                        // to approve, then update in-memory and persistent
                        // lists.
                        if (reply.reportsToApprove != null) {
                            List<IExpenseReportInfo> reportInfos = new ArrayList<IExpenseReportInfo>(
                                    reply.reportsToApprove.size());
                            Calendar updateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            for (ExpenseReport report : reply.reportsToApprove) {
                                IExpenseReportInfo reportInfo = ReportDBUtil.updateReport(concurService.db, report,
                                        IExpenseReportInfo.ReportType.APPROVAL, false, request.userId, updateTime);
                                reportInfos.add(reportInfo);
                            }
                            // Set the new list of reports to be approved.
                            IExpenseReportListInfo expRepListInfo = new ExpenseReportListInfo(reportInfos, updateTime);
                            // Update in-memory cache.
                            ConcurCore app = (ConcurCore) concurService.getApplication();
                            app.getExpenseApprovalCache().setReportList(expRepListInfo);
                            app.getExpenseApprovalCache().clearDetailReportsNotInApproveList();

                            // Add a flag to the intent indicating the reports
                            // have been updated.
                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_TO_APPROVE_LIST_PENDING, true);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ApproveReport): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Broadcast the completion message
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_REPORT_REJECT_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.reject_report).toString());

                // This is a request to reject a report.
                RejectReportRequest request = (RejectReportRequest) msg.obj;

                // Process the request.
                Intent intent = new Intent(Const.ACTION_EXPENSE_REPORT_SEND_BACK);
                try {
                    ReportApproveRejectServiceReply reply = (ReportApproveRejectServiceReply) request
                            .process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        // if (concurService.repostEnabled) {
                        // // Clear the request from the database.
                        // // NOTE: When the client really has an off-line mode
                        // // and re-posts are supported, the request removal
                        // // should be moved up above.
                        // if (!concurService.db.deleteReportSubmittedReject(request.reportKey)) {
                        // Log.e(Const.LOG_TAG,
                        // CLS_TAG
                        // +
                        // ".handleMessage(RejectReport): unable to delete submitted report rejection persistent request for report '"
                        // + request.reportKey + "'.");
                        // }
                        // }
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        // Remove the report key from the list of pending
                        // rejections in the expense approval cache.
                        ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                        IExpenseReportCache expAppCache = ConcurCore.getExpenseApprovalCache();
                        expAppCache.removeSubmittedForReject(request.reportKey);

                        if (!reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                        // Regardless of result from the server, if we have a
                        // non-null list of reports
                        // to approve, then update in-memory and persistent
                        // lists.
                        if (reply.reportsToApprove != null) {
                            List<IExpenseReportInfo> reportInfos = new ArrayList<IExpenseReportInfo>(
                                    reply.reportsToApprove.size());
                            Calendar updateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            for (ExpenseReport report : reply.reportsToApprove) {
                                IExpenseReportInfo reportInfo = ReportDBUtil.updateReport(concurService.db, report,
                                        IExpenseReportInfo.ReportType.APPROVAL, false, request.userId, updateTime);
                                reportInfos.add(reportInfo);
                            }
                            // Set the new list of reports to be approved.
                            IExpenseReportListInfo expRepListInfo = new ExpenseReportListInfo(reportInfos, updateTime);
                            // Update in-memory cache.
                            ConcurCore app = (ConcurCore) concurService.getApplication();
                            app.getExpenseApprovalCache().setReportList(expRepListInfo);
                            app.getExpenseApprovalCache().clearDetailReportsNotInApproveList();

                            // Add a flag to the intent indicating the reports
                            // have been updated.
                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_TO_APPROVE_LIST_PENDING, true);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(RejectReport): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Broadcast the completion message
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_DOWNLOAD_RECEIPT_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_mobile_entry_receipt)
                        .toString());

                // This is a request to download a mobile entry receipt.
                DownloadMobileEntryReceiptRequest request = (DownloadMobileEntryReceiptRequest) msg.obj;

                // Process the request.
                Intent intent = new Intent(Const.ACTION_EXPENSE_RECEIPT_DOWNLOADED);
                try {
                    DownloadMobileEntryReceiptReply reply = (DownloadMobileEntryReceiptReply) request
                            .process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // Update the reference to the mobile entry that was
                            // passed into this request.
                            request.mobileEntry.setReceiptImageDataLocal(true);
                            request.mobileEntry.setReceiptImageDataLocalFilePath(reply.filePath);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(DownloadReceipt): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Broadcast the completion message.
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_ALL_EXPENSE_REQUEST: {
            // TODO E-DAO - this should be removed/deprecated in the future in favor of GSEL
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_all_entries).toString());

                AllExpenseRequest request = (AllExpenseRequest) msg.obj;
                // Process the request.
                Intent intent = new Intent(Const.ACTION_EXPENSE_ALL_EXPENSE_UPDATED);
                try {
                    AllExpenseReply reply = (AllExpenseReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            // Store the response.
                            Calendar lastRetrievedTS = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            concurService.db.saveResponse(msg.what, lastRetrievedTS, reply.xmlReply, request.userId);

                            // Add any offline expenses to the list before putting it in the cache
                            ArrayList<MobileEntry> mes = concurService.db.loadMobileEntries(request.userId,
                                    MobileEntryStatus.NEW);
                            if (mes != null) {
                                for (MobileEntry me : mes) {
                                    // Add it to the reply list.
                                    reply.expenses.add(new Expense(me));
                                }
                            }

                            IExpenseEntryCache expenseEntryCache = ConcurCore.getExpenseEntryCache();
                            ExpenseListInfo expListInfo = new ExpenseListInfo(reply.expenses, reply.personalCards,
                                    lastRetrievedTS);
                            expenseEntryCache.setExpenseEntries(expListInfo);

                            // Punt any non-offline expenses from the table
                            // TODO This puts us in a middle ground of non-local expenses being persisted in the response table
                            // TODO and local expenses persisted in the expense table.
                            // TODO Rectify that and get everything into the expense table.
                            concurService.db.deleteNonLocalExpenseEntries(request.userId);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetMobileEntries): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Broadcast the completion message.
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_EXPENSE_MOBILE_ENTRY_SAVE_REQUEST: {
            try {
                SaveMobileEntryRequest request = (SaveMobileEntryRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_MOBILE_ENTRY_SAVED);

                // If we are not connected then just save this entry as an offline entry. Upload will happen later.
                // Also, if we are editing an offline expense while online, go here.
                if (!ConcurCore.isConnected()
                        || ((request.localKey != null && request.localKey.length() > 0)
                                && (request.mobileEntryKey == null || request.mobileEntryKey.trim().length() == 0) && !request.forceUpload)) {

                    // Determine whether an offline create event occurred.
                    // This has to take place prior to the call to 'processSaveMobileEntryRequest' since the 'localKey' value
                    // can be set within that method.
                    boolean offlineCreate = ((request.localKey == null || request.localKey.length() == 0)
                            && (request.mobileEntryKey == null || request.mobileEntryKey.length() == 0) && !(ConcurCore
                            .isConnected()));

                    processSaveMobileEntryRequest(request, null);

                    // Fake out all our status values
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, HttpStatus.SC_OK);
                    intent.putExtra(Const.REPLY_STATUS, Const.REPLY_STATUS_SUCCESS);
                    intent.putExtra(Flurry.PARAM_NAME_OFFLINE_CREATE, offlineCreate);
                } else {

                    // Broadcast the start network activity message.
                    broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.save_mobile_entry)
                            .toString());

                    try {
                        SaveMobileEntryReply reply = (SaveMobileEntryReply) request.process(concurService);
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                        intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                        if (reply.httpStatusCode == HttpStatus.SC_OK) {
                            intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                            if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {

                                // Process the success result.
                                processSaveMobileEntryRequest(request, reply);
                                // Ensure that the receipt image gets punted.
                                if (request.filePath != null) {
                                    if (request.deleteReceiptFile) {
                                        // Ensure we punt the image file.
                                        File receiptImageFile = new File(request.filePath);
                                        if (receiptImageFile.exists()) {
                                            if (receiptImageFile.delete()) {
                                                Log.d(Const.LOG_TAG, CLS_TAG
                                                        + ".handleMessage(SaveMobileEntry): deleted receipt file '"
                                                        + request.filePath + "'.");
                                            } else {
                                                Log.e(Const.LOG_TAG,
                                                        CLS_TAG
                                                                + ".handleMessage(SaveMobileEntry): failed to delete receipt file '"
                                                                + request.filePath + "'.");
                                            }
                                        }
                                    } else {
                                        Log.d(Const.LOG_TAG, CLS_TAG
                                                + ".handleMessage(SaveMobileEntry): skipped deleting receipt file '"
                                                + request.filePath + "'.");
                                    }
                                }
                            } else {
                                intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SaveMobileEntry): HTTP status("
                                    + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                        }
                    } catch (ServiceRequestException srvReqExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".handleMessage(SaveMobileEntry): service request exception caught.", srvReqExc);
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                    } catch (IOException ioExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(SaveMobileEntry): IO exception caught.", ioExc);
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                        intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                    }
                }
                // Broadcast a save completion.
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_EXPENSE_ADD_TO_REPORT_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.adding_to_report).toString());

                AddToReportRequest request = (AddToReportRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_ADDED_TO_REPORT);

                try {
                    AddToReportReply reply = (AddToReportReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {

                        // Store the overall request status
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if ((reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS))
                                || (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS_SMARTEXP))) {

                            if (reply.mwsErrorMessage != null && reply.mwsErrorMessage.length() > 0) {
                                intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            }
                            if (request.hasPersonalCardTransactions()) {
                                // The request must have updated some card
                                // charges

                                // Clear out the local transaction changes
                                concurService.db.clearHiddenPersonalCardTransactions();

                                // Requery the cards
                                Calendar lastRetrievedTS = null;
                                String responseXml = getCardList(request.sessionId);

                                if (responseXml != null) {
                                    lastRetrievedTS = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                    concurService.db.saveResponse(msg.what, lastRetrievedTS, responseXml,
                                            request.userId);
                                    processCardList(lastRetrievedTS, responseXml);
                                }
                            }
                            if (reply.reportDetail != null) {
                                // Update the persistence tier.
                                Calendar updateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                IExpenseReportInfo reportInfo = ReportDBUtil.updateReport(concurService.db,
                                        reply.reportDetail, ReportType.ACTIVE, true, request.userId, updateTime);
                                // Add the report detail to the in-memory cache.
                                IExpenseReportCache expRepCache = ((ConcurCore) concurService.getApplication())
                                        .getExpenseActiveCache();
                                expRepCache.putReportDetail(reportInfo);
                                // Add the report key to the intent.
                                intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, reportInfo.getReportKey());
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(AddToReport): "
                                        + "reply.reportDetail is null but MWS response was success!");
                            }

                        } else {
                            // TODO: handle errors!
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_EXPENSE_REMOVE_REPORT_EXPENSE_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.remove_report_expense)
                        .toString());

                RemoveReportExpenseRequest request = (RemoveReportExpenseRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_REMOVE_REPORT_EXPENSE);

                try {
                    RemoveReportExpenseReply reply = (RemoveReportExpenseReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        // Add the MWS status.
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        // If re-post is enabled, then clear out the message.
                        // if (concurService.repostEnabled) {
                        // // Clear the request from the database.
                        // if (!concurService.db.deleteHTTPRequest(request.messageId)) {
                        // Log.e(Const.LOG_TAG,
                        // CLS_TAG
                        // +
                        // ".handleMessage(RemoveReportExpense): unable to delete submitted report approval persistent request for report '"
                        // + request.reportKey + "'.");
                        // }
                        // }
                        // Populate intent with any error message.
                        if (reply.mwsStatus != null && !reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_FAILURE)) {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                        // Was a new report detail object parsed?
                        if (reply.reportDetail != null) {
                            // Update the persistence tier.
                            Calendar updateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            IExpenseReportInfo reportInfo = ReportDBUtil.updateReport(concurService.db,
                                    reply.reportDetail, IExpenseReportInfo.ReportType.ACTIVE, true, request.userId,
                                    updateTime);
                            // Update the in-memory active cache.
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            IExpenseReportCache expRepCache = ConcurCore.getExpenseActiveCache();
                            expRepCache.putReportDetail(reportInfo);
                            // Update the intent with information indicating a
                            // new report detail was provided.
                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE, Boolean.TRUE);
                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, reply.reportDetail.reportKey);
                        } else {
                            if (reply.mwsStatus != null && reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(RemoveReportExpense): "
                                        + "reply was successful but no report detail object was parsed!");
                            }
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_EXPENSE_MOBILE_ENTRY_DELETE_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.delete_mobile_entries)
                        .toString());

                DeleteMobileEntriesRequest request = (DeleteMobileEntriesRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_MOBILE_ENTRIES_DELETED);
                try {
                    DeleteMobileEntriesReply reply = (DeleteMobileEntriesReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        // if (concurService.repostEnabled) {
                        // // Clear the request from the database.
                        // // NOTE: When the client really has an off-line mode
                        // // and re-posts are supported, the request removal
                        // // should be moved up above.
                        // if (!concurService.db.deleteHTTPRequest(request.messageId)) {
                        // Log.e(Const.LOG_TAG,
                        // CLS_TAG
                        // + ".handleMessage(DeleteMobileEntries: unable to delete HTTP request from POST table!");
                        // }
                        // }
                        reply.encodeMobileEntryKeyResponses(intent);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            // Iterate over all the successful deletion
                            // responses and update in-memory/persistent cache.
                            IExpenseEntryCache expEntCache = ((ConcurCore) concurService.getApplication())
                                    .getExpenseEntryCache();
                            for (int repInd = 0; repInd < reply.meKeys.size(); ++repInd) {
                                KeyedServiceReply keyReply = reply.meKeys.get(repInd);
                                if (keyReply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                    // First, punt from the database (if it is there, probably not).
                                    concurService.db.deleteMobileEntry(keyReply.key);

                                    // Second, punt from in-memory cache.
                                    Expense expense = expEntCache.findCashExpenseEntry(keyReply.key);
                                    if (expense != null) {
                                        // Ensure no referenced receipt image
                                        // files are not left around.
                                        if (expense.getCashTransaction().hasReceiptImageDataLocal()) {
                                            concurService.deleteReceiptImageFileIfUnreferenced(expense
                                                    .getCashTransaction().getLocalKey(), expense.getCashTransaction()
                                                    .getReceiptImageDataLocalFilePath());
                                        }
                                        expEntCache.removeExpenseEntry(expense);
                                    } else {
                                        Log.e(Const.LOG_TAG,
                                                CLS_TAG
                                                        + ".handleMessage(DeleteMobileEntries): unable to locate mobile entry with key '"
                                                        + keyReply.key + "' in in-memory cache.");
                                    }
                                }
                            }
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(DeleteMobileEntries): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_CARD_LIST_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_card_list).toString());

                // This is a request to retrieve the current card+transactions
                // list
                CardListRequest request = (CardListRequest) msg.obj;

                Calendar lastRetrievedTS = null;

                // Go get a list of cards+transactions
                String cachedResponse = concurService.db.loadResponse(msg.what, request.userId);
                if (cachedResponse != null) {
                    lastRetrievedTS = concurService.db.getReponseLastRetrieveTS(msg.what, request.userId);
                    processCardList(lastRetrievedTS, cachedResponse);
                }

                if (request.sessionId != null) {
                    String responseXml = getCardList(request.sessionId);

                    if (responseXml != null) {
                        lastRetrievedTS = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        concurService.db.saveResponse(msg.what, lastRetrievedTS, responseXml, request.userId);
                        processCardList(lastRetrievedTS, responseXml);
                    }
                }
            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_SUMMARY_COUNT_REQUEST: {
            try {
                // This is a request to retrieve the current itinerary list
                ServiceRequest request = (ServiceRequest) msg.obj;
                request.run(concurService, msg.what);
            } catch (ClassCastException ccExc) {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".handleMessage(SummaryCountRequest): msg.obj is not of type ServiceRequest!");
            }
            break;
        }
        case Const.MSG_TRAVEL_AIR_CANCEL_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.canceling_air).toString());
                AirCancelRequest request = (AirCancelRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_AIR_CANCEL_RESULTS);
                try {
                    AirCancelReply reply = (AirCancelReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR, request.recordLocator);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_TRAVEL_AIR_SELL_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.booking_air).toString());
                AirSellRequest request = (AirSellRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_AIR_BOOK_RESULTS);

                try {
                    AirSellReply reply = (AirSellReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, reply.itinLocator);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_TRAVEL_AIR_SEARCH_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.searching_for_flights)
                        .toString());

                AirSearchRequest request = (AirSearchRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_AIR_SEARCH_RESULTS);

                try {
                    AirSearchReply reply = (AirSearchReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        ConcurCore app = ((ConcurCore) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            app.setAirSearchResults(reply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            app.setAirSearchResults(null);
                        }
                    }

                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_TRAVEL_AIR_FILTER_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.filtering_flights).toString());

                AirFilterRequest request = (AirFilterRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_AIR_FILTER_RESULTS);

                try {
                    AirFilterReply reply = (AirFilterReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        ConcurCore app = ((ConcurCore) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            app.setAirFilterResults(reply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            app.setAirSearchResults(null);
                        }
                    }

                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_TRAVEL_CAR_SEARCH_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.searching_for_cars).toString());

                CarSearchRequest request = (CarSearchRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_CAR_SEARCH_RESULTS);

                try {
                    CarSearchReply reply = (CarSearchReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        ConcurCore app = ((ConcurCore) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            app.setCarSearchResults(reply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            app.setCarSearchResults(null);
                        }
                    }

                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_TRAVEL_CAR_SELL_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.booking_car).toString());

                CarSellRequest request = (CarSellRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_CAR_SELL_RESULTS);
                try {
                    CarSellReply reply = (CarSellReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, reply.itinLocator);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_TRAVEL_RAIL_SEARCH_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.searching_for_trains).toString());

                RailSearchRequest request = (RailSearchRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_RAIL_SEARCH_RESULTS);

                try {
                    RailSearchReply reply = (RailSearchReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        ConcurCore app = ((ConcurCore) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            app.setRailSearchResults(reply);
                        } else {
                            // TODO: handle errors!
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            app.setRailSearchResults(null);
                        }
                    }

                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_TRAVEL_RAIL_TICKET_DELIVERY_OPTION_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_rail_delivery_options)
                        .toString());

                RailTicketDeliveryOptionsRequest request = (RailTicketDeliveryOptionsRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_RAIL_TDO_RESULTS);

                try {
                    RailTicketDeliveryOptionsReply reply = (RailTicketDeliveryOptionsReply) request
                            .process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            int c = reply.deliveryOptions.size();
                            intent.putExtra(RailTicketDeliveryOptionsReply.KEY_COUNT, c);
                            for (int i = 0; i < c; i++) {
                                Bundle b = reply.deliveryOptions.get(i).getBundle();
                                String keyName = RailTicketDeliveryOptionsReply.KEY_TDO + Integer.toString(i);
                                intent.putExtra(keyName, b);
                            }
                        } else {
                            // TODO: handle errors!
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }

                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_TRAVEL_RAIL_SELL_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.booking_train).toString());

                RailSellRequest request = (RailSellRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_RAIL_SELL_RESULTS);
                try {
                    RailSellReply reply = (RailSellReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, reply.itinLocator);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }

                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_TRAVEL_HOTEL_SEARCH_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.searching_for_hotels).toString());

                HotelSearchRequest request = (HotelSearchRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_HOTEL_SEARCH_RESULTS);
                try {
                    HotelSearchReply reply = (HotelSearchReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        ConcurCore app = ((ConcurCore) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            HotelSearchReply curReply = app.getHotelSearchResults();
                            if (curReply != null) {
                                curReply.hotelChoices.addAll(reply.hotelChoices);
                                curReply.length += reply.length;
                            } else {
                                app.setHotelSearchResults(reply);
                            }
                            // MOB-12309 clear the hotel details cache as we do not want to show the in-memory cache if the user
                            // changes the dates and search for the same property
                            app.clearHotelDetailCache();
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            app.setHotelSearchResults(null);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_TRAVEL_HOTEL_CONFIRM_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.confirming_hotel_room)
                        .toString());

                HotelConfirmRequest request = (HotelConfirmRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_HOTEL_CONFIRM_RESULTS);
                try {
                    HotelConfirmReply reply = (HotelConfirmReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, reply.itinLocator);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_TRAVEL_HOTEL_IMAGES_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_hotel_images)
                        .toString());

                HotelImagesRequest request = (HotelImagesRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_HOTEL_IMAGES_RESULTS);
                try {
                    HotelImagesReply reply = (HotelImagesReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);

                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            if (reply.imagePairs != null) {
                                intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_IMAGES, reply.getImagePairBundle());
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".handleMessage(HotelImagesRequest): hotel images list is null!");
                            }
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }

                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_TRAVEL_HOTEL_DETAIL_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_hotel_details)
                        .toString());

                HotelDetailRequest request = (HotelDetailRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_HOTEL_DETAIL_RESULTS);
                try {
                    HotelDetailReply reply = (HotelDetailReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        ConcurCore app = ((ConcurCore) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            if (reply.hotelChoiceDetail != null) {
                                app.setHotelDetail(request.propertyId, reply.hotelChoiceDetail);
                                intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_PROPERTY_ID, request.propertyId);
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".handleMessage(HotelDetailRequest): hotel choice detail object is null!");
                            }
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            app.setHotelSearchResults(null);
                        }
                    }
                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_TRAVEL_LOCATION_SEARCH_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.searching_for_locations)
                        .toString());

                LocationSearchRequest request = (LocationSearchRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_LOCATION_SEARCH_RESULTS);

                try {
                    LocationSearchReply reply = (LocationSearchReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        ConcurCore app = ((ConcurCore) concurService.getApplication());
                        if (!Const.REPLY_STATUS_FAILURE.equalsIgnoreCase(reply.mwsStatus)) {
                            app.setLocationSearchResults(reply);
                        } else {
                            // TODO: handle errors!
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            app.setLocationSearchResults(reply);
                        }
                    }

                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_MARK_RECEIPTS_VIEWED_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.marking_receipts_viewed)
                        .toString());

                MarkReceiptsViewedRequest request = (MarkReceiptsViewedRequest) msg.obj;

                try {
                    ActionStatusServiceReply reply = (ActionStatusServiceReply) request.process(concurService);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        if (!reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(MarkReceiptsViewed): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(MarkReceiptsViewed): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(MarkReceiptsViewed): IOException.", ioExc);
                }

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_MARK_ENTRY_RECEIPT_VIEWED_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.marking_entry_receipt_viewed)
                        .toString());

                MarkEntryReceiptViewedRequest request = (MarkEntryReceiptViewedRequest) msg.obj;

                try {
                    ActionStatusServiceReply reply = (ActionStatusServiceReply) request.process(concurService);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        if (!reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(MarkEntryReceiptViewed): MWS status("
                                    + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(MarkEntryReceiptViewed): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(MarkEntryReceiptViewed): IOException.", ioExc);
                }

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }
            break;
        }
        case Const.MSG_EXPENSE_CAR_CONFIGS_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_car_configs).toString());

                CarConfigsRequest request = (CarConfigsRequest) msg.obj;
                // Process the request.
                Intent intent = new Intent(Const.ACTION_EXPENSE_CAR_CONFIGS_UPDATED);
                try {
                    CarConfigsReply reply = (CarConfigsReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
                            // Store the response.
                            Calendar lastRetrievedTS = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            concurService.db.saveResponse(msg.what, lastRetrievedTS, reply.xmlReply, request.userId);

                            ConcurCore.setCarConfigs(reply.carConfigs);
                            ConcurCore.setCarConfigsLastRetrieved(lastRetrievedTS);

                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(GetCarConfigs): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Broadcast the completion message.
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_LOCATION_CHECK_IN_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.location_check_in_progress)
                        .toString());

                LocationCheckInRequest request = (LocationCheckInRequest) msg.obj;
                // Process the request.
                Intent intent = new Intent(Const.ACTION_LOCATION_CHECK_IN);
                try {
                    LocationCheckInReply reply = (LocationCheckInReply) request.process(concurService);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus != null && !reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(LocationCheckIn): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Broadcast the completion message.
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_EXPENSE_REPORT_FORM_REQUEST: {
            try {
                // Broadcast the start network activity message.
                broadcastStartNetworkActivity(msg.what,
                        concurService.getText(R.string.dlg_expense_report_header_loading_form).toString());

                ReportFormRequest request = (ReportFormRequest) msg.obj;
                // Process the request.
                Intent intent = new Intent(Const.ACTION_EXPENSE_REPORT_FORM_DOWNLOADED);
                try {
                    ReportFormReply reply = (ReportFormReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {

                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        if (reply.mwsStatus != null && !reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                        } else {
                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_FORM_FIELDS, reply.xmlReply);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(ReportForm): HTTP status("
                                + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
                }
                // Broadcast the completion message.
                concurService.sendBroadcast(intent);

            } finally {
                // Broadcast the stop network activity message.
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }

        // MOB-9112
        case Const.MSG_ALTERNATIVE_FLIGHT_SEARCH_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_alternative_flights)
                        .toString());

                AlternativeAirScheduleRequest request = (AlternativeAirScheduleRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_ALTERNATIVE_AIR_SEARCH_RESULTS);

                try {
                    AlternativeAirScheduleReply reply = (AlternativeAirScheduleReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        ConcurCore app = ((ConcurCore) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            app.setAlternativeFlightSchedules(reply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            app.setAirSearchResults(null);
                        }
                    }

                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        case Const.MSG_POST_CRASH_LOG_REQUEST: {
            // We don't care about success or anything else. We try to send,
            // that's it.
            PostCrashLogRequest request = (PostCrashLogRequest) msg.obj;

            try {
                ActionStatusServiceReply reply = (ActionStatusServiceReply) request.process(concurService);
                if (reply.httpStatusCode == HttpStatus.SC_OK) {
                    if (!reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(PostCrashLog): MWS status(" + reply.mwsStatus
                                + ") - " + reply.mwsErrorMessage + ".");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(PostCrashLog): HTTP status(" + reply.httpStatusCode
                            + ") - " + reply.httpStatusText + ".");
                }
            } catch (IOException ioExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(PostCrashLog): IOException.", ioExc);
            }

            break;
        }
        case Const.MSG_NOTIFICATION_REGISTER: {
            // We don't care about success or anything else. We try to send,
            // that's it.
            NotificationRegisterRequest request = (NotificationRegisterRequest) msg.obj;

            try {
                ActionStatusServiceReply reply = (ActionStatusServiceReply) request.process(concurService);
                if (reply.httpStatusCode == HttpStatus.SC_OK) {
                    if (!reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(NotificationRegister): MWS status("
                                + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(NotificationRegister): HTTP status("
                            + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                }
            } catch (IOException ioExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleMessage(NotificationRegister): IOException.", ioExc);
            }

            break;
        }
        case Const.MSG_GET_TAX_FORM_REQUEST: {
            try {
                broadcastStartNetworkActivity(msg.what, concurService.getText(R.string.retrieve_taxForm).toString());

                GetTaxFormRequest request = (GetTaxFormRequest) msg.obj;
                Intent intent = new Intent(Const.ACTION_EXPENSE_TAX_FORM_FILTER);

                try {
                    GetTaxFormReply reply = (GetTaxFormReply) request.process(concurService);

                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                    intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);

                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                        ConcurCore app = ((ConcurCore) concurService.getApplication());
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            app.setTaxFormReply(reply);
                        } else {
                            intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                            app.setTaxFormReply(null);
                        }
                    }

                } catch (ServiceRequestException sre) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, sre.getMessage());
                } catch (IOException ioe) {
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                    intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioe.getMessage());
                }
                // Send out the broadcast.
                concurService.sendBroadcast(intent);

            } finally {
                broadcastStopNetworkActivity(msg.what);
            }

            break;
        }
        default:
            super.handleMessage(msg);
        }
    }

    /**
     * Will process the result of a save mobile entry request with reply.
     * 
     * @param request
     *            the save request.
     * @param reply
     *            the save reply.
     */
    protected void processSaveMobileEntryRequest(SaveMobileEntryRequest request, SaveMobileEntryReply reply) {

        final String MTAG = ".processSaveMobileEntryRequest: ";
        // Update the 'hasReceiptImage' property based on whether the end-user
        // chose to clear
        // the expense entry receipt.
        if (request.clearImage) {
            request.mobileEntry.setHasReceiptImage(false);
            request.mobileEntry.setReceiptImageDataLocal(false);
        }
        // Set the mobile entry key returned from the server.
        boolean newExpense = false;
        if ((request.mobileEntry.getMeKey() == null || request.mobileEntry.getMeKey().length() == 0)
                && (request.mobileEntry.getLocalKey() == null || request.mobileEntry.getLocalKey().length() == 0)) {
            newExpense = true;
            // A null reply indicates an offline save
            if (reply != null) {
                // Set our ME_KEY and, since we saved to server, set the status to NORMAL
                request.mobileEntry.setMeKey(reply.mobileEntryKey);
                request.mobileEntry.setStatus(MobileEntryStatus.NORMAL);
            }
        }

        // If the local key in the request is null, then it means this is the
        // first change applied
        // to the expense item and it should just be inserted, else we update
        // the existing entry.
        Calendar updateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (request.localKey == null) {
            if (!concurService.db.insertMobileEntry(request.userId, request.mobileEntry, updateTime)) {
                Log.e(Const.LOG_TAG, CLS_TAG + MTAG + "unable to insert new change!");
            }
        } else {
            if (!concurService.db.updateMobileEntry(request.userId, request.mobileEntry, updateTime)) {
                Log.e(Const.LOG_TAG, CLS_TAG + MTAG + "unable to update new change!");
            }
        }

        // Second, update our in-memory model.
        IExpenseEntryCache expEntCache = ((ConcurCore) concurService.getApplication()).getExpenseEntryCache();
        switch (request.mobileEntry.getEntryType()) {
        case CASH: {
            // Is this a new expense?
            // For cash expenses (the only ones we can create locally), we want
            // to force load the cache
            // now. This will be needed for a comparison in the newExpense
            // branch below.
            Expense exp = null;
            // Get the expense, be it offline or not.
            if (request.mobileEntry.getMeKey() == null || request.mobileEntry.getMeKey().length() == 0) {
                MobileEntry me = expEntCache.findMobileEntryByLocalKey(request.mobileEntry.getLocalKey());
                if (me != null) {
                    exp = new Expense(me);
                }
            } else {
                exp = expEntCache.findCashExpenseEntry(request.mobileEntry.getMeKey());
            }

            if (!newExpense) {
                if (exp != null) {
                    // Update the mobile entry object representing the cash
                    // transaction.
                    exp.getCashTransaction().update(request.mobileEntry);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + MTAG + "in-memory cash expense not found!");
                }
            } else {
                // If the expense cache had never been loaded (app startup) then the DB insert above
                // for a new cash expense would put it in the DB. We need to cache to get loaded
                // before we add here or else we'll not know that it already exists (it was just loaded
                // into memory from the DB) and we'll end up adding it into the cache again right here.
                if (exp == null) {
                    if (reply == null) {
                        // No server, offline
                        expEntCache.addMobileEntry(request.mobileEntry);
                    } else {
                        Expense expense = new Expense(request.mobileEntry);
                        expEntCache.addExpenseEntry(expense);
                    }
                }
            }
            break;
        }
        case PERSONAL_CARD: {
            Expense exp = expEntCache.findPersonalCardExpenseEntry(request.mobileEntry.getPcaKey(),
                    request.mobileEntry.getPctKey());
            if (exp != null) {
                MobileEntry cardMobileEntry = exp.getPersonalCardTransaction().mobileEntry;
                if (cardMobileEntry != null) {
                    // Update the mobile entry object representing the personal
                    // card transaction.
                    cardMobileEntry.update(request.mobileEntry);
                } else {
                    // Set the mobile entry object representing the personal
                    // card transaction.
                    exp.getPersonalCardTransaction().mobileEntry = request.mobileEntry;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + MTAG + "in-memory personal card expense not found!");
            }
            break;
        }
        case CORPORATE_CARD: {
            Expense exp = expEntCache.findCorporateCardExpenseEntry(request.mobileEntry.getCctKey());
            if (exp != null) {
                MobileEntry cardMobileEntry = exp.getCorporateCardTransaction().getMobileEntry();
                if (cardMobileEntry != null) {
                    // Update the mobile entry object representing the personal
                    // card transaction.
                    cardMobileEntry.update(request.mobileEntry);
                } else {
                    // Set the mobile entry object representing the personal
                    // card transaction.
                    exp.getCorporateCardTransaction().setMobileEntry(request.mobileEntry);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + MTAG + "in-memory corporate card expense not found!");
            }
            break;
        }
        case SMART_CORPORATE: {
            Expense exp = expEntCache.findSmartCorpExpenseEntry(request.mobileEntry.getCctKey());
            if (exp != null) {
                MobileEntry cashEntry = exp.getCashTransaction();
                if (cashEntry != null) {
                    cashEntry.update(request.mobileEntry);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + MTAG
                            + "cash transaction associated with smart corp expense is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + MTAG + "in-memory smart expense not found!");
            }
            break;
        }
        case SMART_PERSONAL: {
            Expense exp = expEntCache.findSmartPersExpenseEntry(request.mobileEntry.getPctKey());
            if (exp != null) {
                MobileEntry cashEntry = exp.getCashTransaction();
                if (cashEntry != null) {
                    cashEntry.update(request.mobileEntry);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + MTAG
                            + "cash transaction associated with smart pers expense is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + MTAG + "in-memory smart expense not found!");
            }
            break;
        }
        }
    }

    /**
     * Will broadcast a message that the application is accessing the network.
     */
    public void broadcastStartNetworkActivity(int actType, String actText) {
        Intent i = new Intent(Const.ACTION_NETWORK_ACTIVITY_START);
        i.putExtra(Const.ACTION_NETWORK_ACTIVITY_TYPE, actType);
        i.putExtra(Const.ACTION_NETWORK_ACTIVITY_TEXT, actText);
        concurService.sendBroadcast(i);
    }

    /**
     * Will broadcast a message that the application is no longer accessing the network.
     */
    public void broadcastStopNetworkActivity(int actType) {
        Intent i = new Intent(Const.ACTION_NETWORK_ACTIVITY_STOP);
        i.putExtra(Const.ACTION_NETWORK_ACTIVITY_TYPE, actType);
        concurService.sendBroadcast(i);
    }

    /**
     * Will broadcast a message that the application is accessing the network.
     */
    public void broadcastSystemUnavailable(String errorMessage) {
        Intent i = new Intent(Const.ACTION_NETWORK_SYSTEM_UNAVAILABLE);
        i.putExtra(Const.ACTION_NETWORK_ACTIVITY_TEXT, errorMessage);
        concurService.sendBroadcast(i);
    }

    /**
     * A simple helper to convert an InputStream into a String.
     * 
     * @param is
     *            The input stream
     * @return A String containing the full contents of the InputStream
     * @throws IOException
     */
    protected String readStream(InputStream is, String encoding) throws IOException {
        final char[] buffer = new char[8192];
        StringBuilder out = new StringBuilder();

        Reader in;
        try {
            in = new InputStreamReader(is, encoding);
        } catch (UnsupportedEncodingException e) {
            in = new InputStreamReader(is);
        }

        int readCount;
        do {
            readCount = in.read(buffer, 0, buffer.length);
            if (readCount > 0) {
                out.append(buffer, 0, readCount);
            }
        } while (readCount >= 0);

        return out.toString();
    }

    /**
     * Convert a card response XML (either from the server or the response cache) into a list of objects that are stored in the
     * application.
     * 
     * @param lastRetrievedTS
     * @param cardXml
     */
    protected void processCardList(Calendar lastRetrievedTS, String cardXml) {

        ConcurCore app = (ConcurCore) concurService.getApplication();

        ArrayList<PersonalCard> cards = PersonalCard.parseCardXml(cardXml);

        // Filter out any transactions that are still pending a response from
        // the server
        ArrayList<String> pctKeys = concurService.db.selectHiddenPersonalCardTransactions();
        if (pctKeys != null) {
            // Loop through all the transactions and remove some if needed
            int cardCount = cards.size();
            cardLoop: for (int cardPos = 0; cardPos < cardCount; cardPos++) {
                PersonalCard card = cards.get(cardPos);
                if (card.transactions != null) {
                    int txnCount = card.transactions.size();
                    for (int txnPos = txnCount - 1; txnPos >= 0; txnPos--) {
                        String pctKey = card.transactions.get(txnPos).pctKey;
                        if (pctKeys.contains(pctKey)) {
                            card.transactions.remove(txnPos);
                            pctKeys.remove(pctKey);
                            if (pctKeys.isEmpty())
                                break cardLoop;
                        }
                    }
                }
            }
        }

        if (cards != null) {
            app.setCards(cards);
            app.setCardsLastRetrieved(lastRetrievedTS);
        }

        // Broadcast the completion message
        Intent i = new Intent(Const.ACTION_CARDS_UPDATED);
        concurService.sendBroadcast(i);
    }

    /**
     * Force reprocessing of the card list. This is needed when the transaction change data is updated.
     */
    public void reprocessCardList(String userId) {

        String cachedResponse = concurService.db.loadResponse(Const.MSG_CARD_LIST_REQUEST, userId);
        if (cachedResponse != null) {
            processCardList(concurService.db.getReponseLastRetrieveTS(Const.MSG_CARD_LIST_REQUEST, userId),
                    cachedResponse);
        }
    }

    /**
     * Retrieve a card list from the server and parse it into a list of {@link PersonalCard}s
     * 
     * @param sessionId
     *            The session ID for the active server session
     * @return A String containing the response XML
     */
    protected String getCardList(String sessionId) {

        String responseXml = null;
        HttpURLConnection connection = null;

        try {

            // Grab the server address
            String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());

            // Build our URI
            StringBuilder serverURI = new StringBuilder(serverAdd)
                    .append("/Mobile/Expense/GetPersonalCardsWithTransactions");
            URL url = null;
            url = new URL(serverURI.toString());
            boolean enableSpdy = Preferences.shouldEnableSpdy();

            // Create and open the connection

            if (enableSpdy && Build.VERSION.SDK_INT < 19) {
                OkHttpClient client = new OkHttpClient();
                OkUrlFactory factory = new OkUrlFactory(client);
                connection = factory.open(url);
                Log.d(Const.LOG_TAG, getClass().getSimpleName() + " .getCardList // SPDY is enabled // ");

            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            // Set timeout values
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);

            connection.addRequestProperty("X-SessionID", sessionId);
            connection.addRequestProperty(Const.HTTP_HEADER_USER_AGENT, Const.HTTP_HEADER_USER_AGENT_VALUE);

            if (connection.getResponseCode() == HttpStatus.SC_OK) {

                // Suck the stream into a string
                InputStream is = new BufferedInputStream(connection.getInputStream());
                String encodingHeader = connection.getContentEncoding();
                String encoding = "UTF-8";
                if (encodingHeader != null) {
                    encoding = encodingHeader;
                }

                responseXml = readStream(is, encoding);
            } else {
                ServiceRequest.logError(connection, CLS_TAG + ".getCardList");
            }

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return responseXml;
    }
}
