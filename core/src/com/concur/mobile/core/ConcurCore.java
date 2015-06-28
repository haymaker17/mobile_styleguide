package com.concur.mobile.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.location.Address;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.data.SystemConfig;
import com.concur.mobile.core.data.UserConfig;
import com.concur.mobile.core.expense.charge.data.PersonalCard;
import com.concur.mobile.core.expense.data.CountSummary;
import com.concur.mobile.core.expense.data.ExpenseCache;
import com.concur.mobile.core.expense.data.IExpenseCache;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.data.SearchListResponse;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptStoreCache;
import com.concur.mobile.core.expense.report.data.CarConfig;
import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.service.ApproverSearchReply;
import com.concur.mobile.core.expense.report.service.AttendeeSaveReply;
import com.concur.mobile.core.expense.report.service.AttendeeSearchReply;
import com.concur.mobile.core.expense.report.service.ConditionalFieldAction;
import com.concur.mobile.core.expense.report.service.ExtendedAttendeeSearchReply;
import com.concur.mobile.core.expense.report.service.GetTaxFormReply;
import com.concur.mobile.core.expense.travelallowance.FixedAllowances;
import com.concur.mobile.core.expense.travelallowance.Itinerary;
import com.concur.mobile.core.expense.travelallowance.ItineraryRow;
import com.concur.mobile.core.expense.travelallowance.TaConfig;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.ipm.service.IpmReply;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.CorpSsoQueryReply;
import com.concur.mobile.core.service.CustomAsyncRequestTask;
import com.concur.mobile.core.travel.air.service.AirFilterReply;
import com.concur.mobile.core.travel.air.service.AirSearchReply;
import com.concur.mobile.core.travel.air.service.AlternativeAirScheduleReply;
import com.concur.mobile.core.travel.car.service.CarSearchReply;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.ItineraryCache;
import com.concur.mobile.core.travel.data.RuleViolation;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.data.TravelCustomFieldsConfig;
import com.concur.mobile.core.travel.data.TripToApprove;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.travel.hotel.data.HotelChoiceDetail;
import com.concur.mobile.core.travel.hotel.service.HotelSearchReply;
import com.concur.mobile.core.travel.rail.data.RailStation;
import com.concur.mobile.core.travel.rail.service.RailSearchReply;
import com.concur.mobile.core.travel.service.LocationSearchReply;
import com.concur.mobile.core.travel.service.ReasonCodeReply;
import com.concur.mobile.core.travel.service.TripApprovalReqObject;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.FeedbackManager;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.util.net.SessionManager;
import com.concur.mobile.core.widget.MultiViewDialog;
import com.concur.mobile.platform.authentication.LoginResult;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.base.VisibleActivityStateTracker;
import com.concur.mobile.platform.common.Cache;
import com.concur.mobile.platform.common.formfield.ConnectForm;
import com.concur.mobile.platform.common.formfield.ConnectFormFieldsCache;
import com.concur.mobile.platform.config.provider.ClientData;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.provider.ExpenseUtil;
import com.concur.mobile.platform.location.LastLocationTracker;
import com.concur.mobile.platform.request.RequestGroupConfigurationCache;
import com.concur.mobile.platform.request.RequestListCache;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.groupConfiguration.RequestGroupConfiguration;
import com.concur.mobile.platform.service.MWSPlatformManager;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;
import com.concur.mobile.platform.ui.common.util.PreferenceUtil;
import com.concur.mobile.platform.util.Parse;
import com.concur.platform.PlatformProperties;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

public abstract class ConcurCore extends MultiDexApplication {

    // Maps between an activity class name and the list of currently running
    // AsyncTasks that were spawned while it was active.
    private final Map<String, List<CustomAsyncRequestTask>> mActivityTaskMap = new HashMap<String, List<CustomAsyncRequestTask>>();

    public enum Product {

        MILEAGETRACKER(R.string.mileagetracker_name, "MileageTRacker"), CORPORATE(R.string.corporate_name, "Corporate"), SMARTEXPENSE(
                R.string.se_name, "BRONX"), GOV(R.string.gov_name, "Concur Gov");

        int nameId;
        String name;
        String offering;

        // Offering matches the ProductOffering element in login results.
        private Product(int nameId, String offering) {
            this.nameId = nameId;
            this.offering = offering;
        }

        public String getName() {
            if (name == null) {
                name = ConcurCore.getContext().getText(nameId).toString();
            }
            return name;
        }
    }

    protected Product product;

    protected static final String CLS_TAG = ConcurCore.class.getSimpleName();
    public static final String FROM_NOTIFICATION = "from notification";

    // Request codes can't have a value greater than '0xffff', i.e., can't go
    // beyond
    // lower 16-bits of an integer value.
    // TODO: Re-visit why we need such a high-value for request code.
    public static final int START_UP_REQ_CODE = 65535;
    // public static final String DO_FINISH= "doFinish";

    // We need context in some deep places. Keep a static copy here.
    protected static Context appContext;

    // Store autologin starttime, endtime and user click time.
    public static long startAutologinTime, stopAutoLoginTime, userClickTime,userEntryAppTimer, userSuccessfulLoginTimer;

    // Location - retrieve current location/address form LastLocationTracker
    protected LastLocationTracker lastLocationTracker;

    // Our list of cards and transactions.
    protected ArrayList<PersonalCard> cards;
    protected Calendar cardsLastRetrieved;

    // The summary count
    protected CountSummary summary;
    protected Calendar summaryLastRetrieved;

    protected ConcurService concurService;

    // Contains a reference to system configuration information related to
    // the current end-users company information.
    protected SystemConfig systemConfig;
    // Contains the last time the system config was retrieved.
    protected Calendar systemConfigLastRetrieved;
    // Contains whether or not a call to 'getSystemConfig' has been delayed due
    // to the app not yet bound to the service.
    protected boolean getSystemConfigDelayed;
    // Contains whether a call to 'sendSystemConfig' has been delayed due to
    // the app not not having connectivity.
    protected boolean sendSystemConfigDelayed;

    // Contains a reference to user configuration information related to
    // the current end-user
    protected UserConfig userConfig;
    // Contains the last time the user config was retrieved.
    protected Calendar userConfigLastRetrieved;
    // Contains whether or not a call to 'getUserConfig' has been delayed due
    // to the app not yet bound to the service.
    protected boolean getUserConfigDelayed;
    // Contians whether a call to 'sendUserConfig' has been delayed due to
    // the app not having connectivity.
    protected boolean sendUserConfigDelayed;
    /**
     * Contains a reference to the tax form
     */
    protected GetTaxFormReply taxFormReply;
    /**
     * Contains a reference to the expense cache.
     */
    protected IExpenseCache expenseCache;

    /**
     * Contains a reference to the itinerary cache.
     */
    protected IItineraryCache itinCache;

    // The current report entry form being edited (used for new entries only,
    // editing uses the
    // ReportEntryDetail in the report cache)
    protected ExpenseReportEntryDetail currentEntryForm;

    // The latest air search responses
    protected AirSearchReply airSearchResults;
    protected AirFilterReply airFilterResults;
    protected AlternativeAirScheduleReply alternativeAirScheduleReply;
    // Our list of car configs and rates
    protected ArrayList<CarConfig> carConfigs;
    protected Calendar carConfigsLastRetrieved;

    // The latest car search response
    protected CarSearchReply carSearchResults;

    // The latest attendees search response.
    protected AttendeeSearchReply attendeeSearchResults;

    // The last extended attendee search response.
    protected ExtendedAttendeeSearchReply extendedAttendeeSearchResults;

    // The latest approver search response.
    protected ApproverSearchReply approverSearchResults;

    // The results from the last attendee save attempt
    protected AttendeeSaveReply attendeeSaveResults;

    // The results from the last getDynamicFieldAction attempt
    protected List<ConditionalFieldAction> conditionalFieldActionsResults;

    // The last selected attendee from a search.
    protected List<ExpenseReportAttendee> selectedAttendees;

    // The last form that was retrieved for a particular attendee.
    protected ExpenseReportAttendee attendeeForm;

    // The latest hotel search response.
    protected HotelSearchReply hotelSearchResults;

    // The latest rail search response
    protected RailSearchReply railSearchResults;

    // Contains a cache of hotel detail objects keyed on property id. This cache
    // is cleared out
    // when an end-user is no longer logged in.
    protected HashMap<String, HotelChoiceDetail> hotelDetailCache = new HashMap<String, HotelChoiceDetail>();

    // The latest location search response
    protected LocationSearchReply locationSearchResults;

    // The latest expense search response.
    protected SearchListResponse expenseSearchListResults;

    // Contains the list of attendees currently being edited;
    protected List<ExpenseReportAttendee> editedAttendees;

    // The latest rail station list
    // TODO: In the future, when more than Amtrak is supported, this will
    // probably need to be a map
    // for multiple vendors. Although at that point we probably won't use this
    // mechanism anyway
    // since the station list will be too large to hold completely.
    protected ArrayList<RailStation> railStations;
    // Contains a map from a rail station code to the rail station itself. This
    // is used
    // as a means of looking up city, state, and country information.
    protected HashMap<String, RailStation> codeRailStationMap;
    protected Calendar railStationsLastRetrieved;

    protected FixedAllowances fixedAllowances;
    protected Itinerary taItinerary;
    protected ItineraryRow taItineraryRow;
    protected TaConfig taConfig;

    // Controllers for Allowance and Itinerary handling
    private TravelAllowanceItineraryController taItineraryController;
    private FixedTravelAllowanceController fixedTravelAllowanceController;

    // Trips for Approval
    protected List<TripToApprove> tripsToApprove;
    protected Calendar tripsToApproveLastRetrieved;

    // Trip Rule Violations
    protected List<RuleViolation> tripRuleViolations;
    protected Calendar tripRuleViolationsLastRetrieved;

    // Travel Custom Field
    protected TravelCustomField travelCustomField;
    protected Calendar travelCustomFieldLastRetrieved;

    // Trip to Approve/Reject response status
    protected MWSResponseStatus requestTaskStatus;

    // Contains the last response from a corporate SSO query request.
    protected CorpSsoQueryReply corpSsoQueryReply;

    // Contains the last retrieved instance of travel custom fields
    // configuration.
    protected TravelCustomFieldsConfig travelCustomFieldsConfig;

    // Contains a reference to the last retrieved set of reason codes.
    protected ReasonCodeReply reasonCodes;

    // for sending the data to the AsyncTask
    protected TripApprovalReqObject tripApprovalReqObj;

    // Hotel Streaming
    protected ArrayList<String> propertyIdsAlreadyPriced = new ArrayList<String>();

    // Travel Policy Violations
    protected List<Violation> travelPolicyViolations;
    protected Calendar travelPolicyViolationsLastRetrieved;

    // PTB Header viewed
    protected boolean viewedPriceToBeatList;
    protected Calendar viewedPriceToBeatListLastRetrieved;

    protected IpmReply ipmMsgResults;
    /**
     * Contains an intent sent as a sticky broadcast indicating the app has successfully bound to the service.
     */
    protected Intent serviceBoundIntent = new Intent(Const.ACTION_CONCUR_SERVICE_BOUND);

    protected Intent userConfigAvailIntent = new Intent(Const.ACTION_CONCUR_USER_CONFIG_AVAIL);
    protected Intent userConfigUnAvailIntent = new Intent(Const.ACTION_CONCUR_USER_CONFIG_UNAVAIL);

    protected Intent sysConfigAvailIntent = new Intent(Const.ACTION_CONCUR_SYS_CONFIG_AVAIL);
    protected Intent sysConfigUnAvailIntent = new Intent(Const.ACTION_CONCUR_SYS_CONFIG_UNAVAIL);

    /**
     * Contains an intent sent as sticky broadcast indicating the app has lost its binding to the service.
     */
    protected Intent serviceUnboundIntent = new Intent(Const.ACTION_CONCUR_SERVICE_UNBOUND);

    /**
     * Contains an intent sent as a sticky broadcast indicating the app has data connectivity.
     */
    protected Intent dataConnectivityAvailable = new Intent(
            com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_AVAILABLE);

    /**
     * Contains an intent sent as a sticky broadcast indicating the app has lost data connectivity.
     */
    protected Intent dataConnectivityUnavailable = new Intent(
            com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_UNAVAILABLE);

    /**
     * Contains a reference to the travel request list cache.
     */
    protected Cache<String, RequestDTO> requestListCache = new RequestListCache();

    /**
     * Contains a reference to the travel request formfields cache.
     */
    protected Cache<String, ConnectForm> requestFormFieldsCache = new ConnectFormFieldsCache();

    /**
     * Contains a reference to the travel request group configuration cache.
     */
    protected Cache<String, RequestGroupConfiguration> requestGroupConfigurationCache = new RequestGroupConfigurationCache();

    /**
     * Local instance to handle service connection events and keep our service reference up-to-date
     */
    protected ServiceConnection serviceConn = new ServiceConnection() {

        /**
         * When connecting grab a reference to the full service class for our use
         */
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".ServiceConnection.onServiceConnected: ");
            concurService = ((ConcurService.LocalBinder) service).getService();
            removeStickyBroadcast(serviceUnboundIntent);

            // Below, make any outstanding request for data that were delayed
            // due to the
            // service not being bound.

            // If a call to get system configuration has been delayed, then
            // perform it now.
            if (getSystemConfigDelayed) {
                Log.i(Const.LOG_TAG, CLS_TAG + ".ServiceConnection.onServiceConnected: getting system configuration.");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.this);
                String userId = prefs.getString(Const.PREF_USER_ID, null);
                if (userId != null) {
                    setSystemConfig(concurService.getSystemConfig(userId));
                    getSystemConfigDelayed = false;
                } else {
                    Log.d(Const.LOG_TAG,
                            CLS_TAG
                                    + ".ServiceConnection.onServiceConnected: userId is not set, yet 'getSystemConfigDelayed' is 'true'!");
                }
            }
            // If a call to send system configuration has been delayed, then
            // perform it now.
            if (isConnected() && sendSystemConfigDelayed) {
                Log.i(Const.LOG_TAG, CLS_TAG
                        + ".ServiceConnection.onServiceConnected: sending system configuration request.");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.this);
                String userId = prefs.getString(Const.PREF_USER_ID, null);
                if (userId != null) {
                    String hash = ((systemConfig != null) ? systemConfig.getHash() : null);
                    concurService.sendSystemConfigRequest(userId, hash);
                    sendSystemConfigDelayed = false;
                } else {
                    Log.d(Const.LOG_TAG,
                            CLS_TAG
                                    + ".ServiceConnection.onServiceConnected: userId is not set, yet 'sendSystemConfigDelayed' is 'true'!");
                }
            }
            // If a call to get user configuration has been delayed, then
            // perform it now.
            if (getUserConfigDelayed) {
                Log.i(Const.LOG_TAG, CLS_TAG + ".ServiceConnection.onServiceConnected: getting user configuration.");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.this);
                String userId = prefs.getString(Const.PREF_USER_ID, null);
                if (userId != null) {
                    setUserConfig(concurService.getUserConfig(userId));
                    getUserConfigDelayed = false;
                } else {
                    Log.d(Const.LOG_TAG,
                            CLS_TAG
                                    + ".ServiceConnection.onServiceConnected: userId is not set, yet 'getUserConfigDelayed' is 'true'!");
                }
            }
            // If a call to send user configuration has been delayed, then
            // perform it now.
            if (isConnected() && sendUserConfigDelayed) {
                Log.i(Const.LOG_TAG, CLS_TAG
                        + ".ServiceConnection.onServiceConnected: sending user configuration request.");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.this);
                String userId = prefs.getString(Const.PREF_USER_ID, null);
                if (userId != null) {
                    String hash = ((userConfig != null) ? userConfig.hash : null);
                    concurService.sendUserConfigRequest(userId, hash);
                    if (prefs.getBoolean(Const.PREF_CAN_EXPENSE, false)) {
                        Log.i(Const.LOG_TAG, CLS_TAG
                                + ".ServiceConnection.onServiceConnected: sending default attendee request.");
                        concurService.sendDefaultAttendeeRequest(userId);
                    }
                    sendUserConfigDelayed = false;
                } else {
                    Log.d(Const.LOG_TAG,
                            CLS_TAG
                                    + ".ServiceConnection.onServiceConnected: userId is not set, yet 'sendUserConfigDelayed' is 'true'!");
                }
            }

            // Immediately prior to sending out the service bound sticky
            // broadcast, set the
            // MobileDatabase reference on the Preferences class.
            MobileDatabase mdb = concurService.getMobileDatabase();
            if (mdb != null) {
                Log.d(Const.LOG_TAG, CLS_TAG
                        + "..ServiceConnection.onServiceConnected: setting database on Preferences.");
                Preferences.setMobileDatabase(concurService.getMobileDatabase());
            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + "..ServiceConnection.onServiceConnected: database is null.");
            }

            // Send the sticky broadcast that the concur service is now bound.
            sendStickyBroadcast(serviceBoundIntent);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".ServiceConnection.onServiceDisconnected: ");
            concurService = null;
            removeStickyBroadcast(serviceBoundIntent);
            sendStickyBroadcast(serviceUnboundIntent);
        }
    };

    // ////////////////////////////////////////////////////////////////////////////
    //
    // Filter and receiver for connectivity state
    //

    protected static boolean connected;

    // Contains the start time at which connectivity was detected to be
    // unavailable.
    protected long unconnectedStartTimeMillis = 0L;

    // Contains whether or not the end-user's device has airplane mode enabled.
    protected boolean inAirplaneMode;

    /**
     * Guaranteed unique device id.
     */
    protected static String uniqueDeviceId;

    protected final IntentFilter connectivityFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

    protected final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {

        protected final String CLS_TAG = ConcurCore.CLS_TAG + "." + "ConnectivityReceiver";

        protected void writeDebugInfo(Intent intent) {

            final String MTAG = "writeDebugInfo: ";
            final String LTAG = CLS_TAG + "." + MTAG;

            boolean hasConnExtra = intent.hasExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY);
            if (hasConnExtra) {
                Log.d(Const.LOG_TAG, LTAG + "no connectivity!");
            } else {
                if (intent.hasExtra(ConnectivityManager.EXTRA_NETWORK_INFO)) {
                    NetworkInfo ni = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    if (ni != null) {
                        Log.d(Const.LOG_TAG, LTAG + "intent has EXTRA_NETWORK_INFO");
                        Log.d(Const.LOG_TAG, LTAG + "extra network info -> " + ni.toString());
                    } else {
                        Log.d(Const.LOG_TAG, LTAG + "EXTRA_NETWORK_INFO is null!");
                    }
                } else {
                    Log.d(Const.LOG_TAG, LTAG + "intent missing EXTRA_NETWORK_INFO");
                }
                if (intent.hasExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO)) {
                    NetworkInfo ni = intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
                    if (ni != null) {
                        Log.d(Const.LOG_TAG, LTAG + "intent has EXTRA_OTHER_NETWORK_INFO");
                        Log.d(Const.LOG_TAG, LTAG + "extra other network info -> " + ni.toString());
                    } else {
                        Log.d(Const.LOG_TAG, LTAG + "EXTRA_OTHER_NETWORK_INFO is null!");
                    }
                } else {
                    Log.d(Const.LOG_TAG, LTAG + "intent missing EXTRA_OTHER_NETWORK_INFO");
                }
            }

        }

        @Override
        public void onReceive(Context context, Intent intent) {

            final String LTAG = CLS_TAG + ".onReceive: ";

            writeDebugInfo(intent);

            boolean hasConnExtra = intent.hasExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY);
            String connectType = "unknown";

            if (hasConnExtra) {
                Log.v(Const.LOG_TAG, LTAG + "using connection extra");
                // Check it for connectivity
                connected = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            } else if (intent.hasExtra(ConnectivityManager.EXTRA_NETWORK_INFO)) {
                Log.v(Const.LOG_TAG, LTAG + "has network info");
                // NI available, use it.
                NetworkInfo ni = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                connectType = ni.getTypeName();
                connected = ni.isConnected();
                if (!connected) {
                    if (intent.hasExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO)) {
                        ni = intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
                        if (ni != null) {
                            connectType = ni.getTypeName();
                            connected = ni.isConnected();
                        }
                    }
                    if (!connected) {
                        Log.d(Const.LOG_TAG, LTAG + "checking with Connectivity Service.");
                        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (cm != null) {
                            ni = cm.getActiveNetworkInfo();
                            if (ni != null) {
                                connectType = ni.getTypeName();
                                connected = ni.isConnected();
                            } else {
                                connected = false;
                            }
                        } else {
                            Log.d(Const.LOG_TAG, LTAG + "unable to access Connectivity Service.");
                        }
                    }
                }
            }
            Log.v(Const.LOG_TAG, LTAG + "connection(" + connectType + "): connected set to " + connected);

            // Send the correct sticky broadcast.
            if (connected) {
                removeStickyBroadcast(dataConnectivityUnavailable);
                sendStickyBroadcast(dataConnectivityAvailable);
            } else {
                removeStickyBroadcast(dataConnectivityAvailable);
                sendStickyBroadcast(dataConnectivityUnavailable);
            }

            // Check if a call to retrieve system configuration from the server
            // was delayed due to
            // no connectivity, if so, then perform that call now.
            if (connected && sendSystemConfigDelayed && concurService != null) {
                Log.i(Const.LOG_TAG, LTAG + "connection: send system configuration request was delayed, sending now.");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.this);
                String userId = prefs.getString(Const.PREF_USER_ID, null);
                if (userId != null) {
                    String hash = ((systemConfig != null) ? systemConfig.getHash() : null);
                    concurService.sendSystemConfigRequest(userId, hash);
                    sendSystemConfigDelayed = false;
                } else {
                    Log.d(Const.LOG_TAG, LTAG
                            + "connection: userId is not set, yet 'sendSystemConfigDelayed' is 'true'!");
                }
            }
            // Check if a call to retrieve user configuration from the server
            // was delayed due to
            // no connectivity, if so, then perform that call now.
            if (connected && sendUserConfigDelayed && concurService != null) {
                Log.i(Const.LOG_TAG, LTAG + "connection: send user configuration request was delayed, sending now.");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.this);
                String userId = prefs.getString(Const.PREF_USER_ID, null);
                if (userId != null) {
                    String hash = ((userConfig != null) ? userConfig.hash : null);
                    concurService.sendUserConfigRequest(userId, hash);
                    if (prefs.getBoolean(Const.PREF_CAN_EXPENSE, false)) {
                        // Send the request to retrieve the default user
                        // attendee.
                        Log.i(Const.LOG_TAG, LTAG
                                + "connection: send default attendee request was delayed, sending now.");
                        concurService.sendDefaultAttendeeRequest(userId);
                    }
                    sendUserConfigDelayed = false;
                } else {
                    Log.d(Const.LOG_TAG, LTAG + "connection: userId is not set, yet 'sendUserConfigDelayed' is 'true'!");
                }
            }

            // Detect a period of being "offline" due to "airplane mode",
            // "no network", etc.
            if (connected) {
                if (unconnectedStartTimeMillis != 0L) {

                    // Reset 'inAirplaneMode'.
                    inAirplaneMode = false;

                    // Reset 'unconnectedStartTimeMillis.
                    unconnectedStartTimeMillis = 0L;
                }
            } else {
                // Reset 'unconnectedStartTimeMillis.
                if (unconnectedStartTimeMillis == 0L) {
                    unconnectedStartTimeMillis = System.currentTimeMillis();
                }
                // Determine whether we're in airplane mode or not.
                inAirplaneMode = (Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0);
            }
        }
    };

    //
    // End connectivity receiver
    //
    // ////////////////////////////////////////////////////////////////////////////

    // /////////////////////////////////////////////////////////////////////////////
    // Static fetchers.
    // Technically these shouldn't be static but the platform guarantees that
    // the application
    // object is created before anything else can exist.
    // Made static just to avoid having to write
    // ((ConcurMobile)getApplication()) everywhere.
    //
    public static Context getContext() {
        return appContext;
    }

    public static boolean isConnected() {
        return connected;
    }

    //
    // End of static fetchers
    //
    // /////////////////////////////////////////////////////////////////////////////

    /**
     * Good ol' default constructor
     */
    public ConcurCore() {
        appContext = this;
        Const.DEFAULT_MWS_ADDRESS = getServerAddress();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setProduct(this.getPackageName());
        initEventTracker();
        initUserAgentValue();
        initABTests();
        initVisibleActivityStateTracker();
        initLastLocationTracker();

        // Clear any previous sticky broadcasts related to the app's data
        // connectivity.
        removeStickyBroadcast(dataConnectivityAvailable);
        removeStickyBroadcast(dataConnectivityUnavailable);

        // Set connection state and start listening for changes
        Log.v(Const.LOG_TAG, "==== Setting initial connect state");
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        String connectType = "unknown";
        if (ni != null && ni.isConnected()) {
            connectType = ni.getTypeName();
            connected = true;
            sendStickyBroadcast(dataConnectivityAvailable);
        } else {
            connected = false;
            sendStickyBroadcast(dataConnectivityUnavailable);
        }
        Log.v(Const.LOG_TAG, CLS_TAG + ".onCreate: initial connection(" + connectType + ") state: " + connected);

        // If we're not connected at app start-up, then set the current time
        // stop.
        if (!connected) {
            unconnectedStartTimeMillis = System.currentTimeMillis();
            inAirplaneMode = (Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0);
        }

        registerReceiver(connectivityReceiver, connectivityFilter);

        // Remove any previous sticky broadcast regarding the app being bound to
        // the service.
        removeStickyBroadcast(serviceBoundIntent);

        // Set the sticky broadcast that the app has not yet bound to the
        // service.
        sendStickyBroadcast(serviceUnboundIntent);

        // Connect up to the service
        Log.d(Const.LOG_TAG, CLS_TAG + ".onCreate: binding to the service.");
        boolean res = bindProductService();
        if (!res) {
            Log.wtf(Const.LOG_TAG, CLS_TAG + ".onCreate: failed to bind to the service!");
        }

        // Plug in our uncaught exception handler
        // UncaughtExceptionHandler originalHandler =
        // Thread.getDefaultUncaughtExceptionHandler();
        // Thread.setDefaultUncaughtExceptionHandler(ConcurException.getUncaughtExceptionHandler(this,
        // originalHandler));

        // Set the expense approval cache.
        expenseCache = new ExpenseCache(this);
        // Set the itinerary cache.
        itinCache = new ItineraryCache(this);
        initPlatformProperties();

    }

    /**
     * Reset Auto Login Timers.
     * */
    public static void resetAutloLoginTimes(){
        startAutologinTime =0L;
        stopAutoLoginTime =0L;
        userClickTime =0L;
    }

    /**
     * Reset User Entry and Successful Login timer.
     * */
    public static void resetUserTimers(){
        userEntryAppTimer=0;
        userSuccessfulLoginTimer=0;
    }
    // Initialize the platform properties.
    private void initPlatformProperties() {

        String serverAddress = Preferences.getServerAddress();
        if (TextUtils.isEmpty(serverAddress)) {
            serverAddress = "www.concursolutions.com";
        }
        PlatformProperties.setServerAddress(serverAddress);
        // set user agent
        PlatformProperties.setUserAgent(Const.HTTP_HEADER_USER_AGENT_VALUE);

        MWSPlatformManager mwsPlatMngr = new MWSPlatformManager();

        // Set the platform session manager.
        PlatformProperties.setPlatformSessionManager(mwsPlatMngr);

        // Set the other properties.
        SessionInfo sessionInfo = ConfigUtil.getSessionInfo(getApplicationContext());
        if (sessionInfo != null) {
            // MOB-18535 - AccessToken must always be set even if session is
            // expired
            // because AutoLoginV3 requires Oauth token to re-auth.
            PlatformProperties.setAccessToken(sessionInfo.getAccessToken());

            if (!mwsPlatMngr.isSessionExpired(sessionInfo)) {
                PlatformProperties.setSessionId(sessionInfo.getSessionId());
            }
        }
    }

    abstract protected boolean bindProductService();

    @Override
    public void onTerminate() {

        // Stop listening for connectivity
        unregisterReceiver(connectivityReceiver);

        // Disconnect the service
        Log.d(Const.LOG_TAG, CLS_TAG + ".onTerminate: unbinding from the service.");
        unbindService(serviceConn);

        super.onTerminate();
    }

    public abstract void setProduct(String componentName);

    public abstract String getStringResourcePackageName();

    /**
     * <p>
     * Returns the hashed User ID used for GA tracking.
     * </p>
     * <p>
     * Note that if the end-user has not yet logged in, then the value of <code>{userId}</code> may be blank/empty.
     * </p>
     * 
     * @return Returns the hashed User ID used for GA tracking.
     */
    public static String getTrackingUserId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        String userid = prefs.getString(Const.PREF_USER_ID, "");
        ClientData clientData = new ClientData(appContext);
        if (!userid.isEmpty()) {
            clientData.userId = userid;
            clientData.key = LoginResult.TAG_ANALYTICS_ID;
            if (clientData.load()) {
                userid = clientData.text;
            }else{
                userid="";
            }
        }
        return userid;
    }

    /**
     * Initializes the EventTracker for Flurry & Google Analytics.
     */
    protected void initEventTracker() {
        Log.d(Const.LOG_TAG, CLS_TAG + " - initializing Event Tracker.");
        EventTracker.INSTANCE.init(this, getTrackingUserId(), getGATrackingId());
    }

    /**
     * Initialize the VisibleActivityStateTracker to receive activity lifecycle callbacks
     */
    protected void initVisibleActivityStateTracker() {
        Log.d(Const.LOG_TAG, CLS_TAG + " - initializing ApplicationStateTracker.");
        this.registerActivityLifecycleCallbacks(VisibleActivityStateTracker.INSTANCE);
    }

    /**
     * Initialize the location tracker, and allow it to set current location via setCurrentLocation() callback when obtained.
     */
    protected void initLastLocationTracker() {
        Log.d(Const.LOG_TAG, CLS_TAG + " - initializing LastLocationTracker.");
        lastLocationTracker = new LastLocationTracker(getApplicationContext(), null);
    }

    /**
     * Allow access to location tracker. Move to somewhere else in platform?
     */
    public LastLocationTracker getLocationTracker() {
        return lastLocationTracker;
    }

    /**
     * Performs initialization of any A/B Tests built for a particular product release.
     */
    protected void initABTests() {
    }

    protected abstract String getServerAddress();

    /**
     * Will initialize the value of <code>Const.HTTP_HEADER_USER_AGENT_VALUE</code>. <b>NOTE:</b> This method relies upon the
     * product being set.
     */
    protected void initUserAgentValue() {
        // Initialize the user-agent http header information.
        StringBuilder ua = new StringBuilder();
        switch (product) {
        case CORPORATE:
            ua.append("Corporate/");
            break;
        default:
            ua.append("Unknown Mobile/");
            break;
        }
        String versionName;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            versionName = "0.0.0";
        }
        ua.append(versionName);
        ua.append(" (Android, ").append(Build.MODEL).append(", ").append(Build.VERSION.RELEASE).append(")");
        Const.HTTP_HEADER_USER_AGENT_VALUE = ua.toString();
    }

    public Product getProduct() {
        return product;
    }

    public synchronized Location getCurrentLocation() {
        if (this.lastLocationTracker != null)
            return lastLocationTracker.getCurrentLocaton();

        return null;
    }

    /**
     * Gets the appropriate Google Analytics Tracking ID for the application.
     * 
     * @return the appropriate Google Analytics Tracking ID for the application.
     */
    public abstract String getGATrackingId();

    /**
     * Returns the current server address.
     * 
     * @return the current server address;
     */
    public synchronized Address getCurrentAddress() {
        if (this.lastLocationTracker != null)
            return lastLocationTracker.getCurrentAddress();

        return null;
    }

    public void setCards(ArrayList<PersonalCard> cards) {
        this.cards = cards;
    }

    public ArrayList<PersonalCard> getCards() {
        return cards;
    }

    public void setCardsLastRetrieved(Calendar ts) {
        this.cardsLastRetrieved = ts;
    }

    public Calendar getCardsLastRetrieved() {
        return cardsLastRetrieved;
    }

    public void setSummary(CountSummary summary) {
        this.summary = summary;
    }

    public CountSummary getSummary() {
        return summary;
    }

    public void setSummaryLastRetrieved(Calendar ts) {
        this.summaryLastRetrieved = ts;
    }

    public Calendar getSummaryLastRetrieved() {
        return summaryLastRetrieved;
    }

    public void setAirSearchResults(AirSearchReply results) {
        this.airSearchResults = results;
    }

    public AirSearchReply getAirSearchResults() {
        return airSearchResults;
    }

    public void setAirFilterResults(AirFilterReply results) {
        this.airFilterResults = results;
    }

    public AirFilterReply getAirFilterResults() {
        return airFilterResults;
    }

    public void setCarConfigs(ArrayList<CarConfig> configs) {
        this.carConfigs = configs;
    }

    public ArrayList<CarConfig> getCarConfigs() {
        return carConfigs;
    }

    public void setCarConfigsLastRetrieved(Calendar ts) {
        this.carConfigsLastRetrieved = ts;
    }

    public Calendar getCarConfigsLastRetrieved() {
        return carConfigsLastRetrieved;
    }

    public void setCarSearchResults(CarSearchReply results) {
        this.carSearchResults = results;
    }

    public CarSearchReply getCarSearchResults() {
        return carSearchResults;
    }

    /**
     * set alternative schedules
     * */
    public void setAlternativeFlightSchedules(AlternativeAirScheduleReply results) {
        this.alternativeAirScheduleReply = results;
    }

    /**
     * get alternative schedules
     * */
    public AlternativeAirScheduleReply getAlternativeFlightSchedules() {
        return alternativeAirScheduleReply;
    }

    /**
     * Sets the latest response to a search attendees request.
     * 
     * @param results
     *            a search attendees response.
     */
    public void setAttendeeSearchResults(AttendeeSearchReply results) {
        attendeeSearchResults = results;
    }

    /**
     * Gets the latest response to a search attendees request.
     * 
     * @return the search attendees response.
     */
    public AttendeeSearchReply getAttendeeSearchResults() {
        return attendeeSearchResults;
    }

    /**
     * Sets the latest response to an extended attendees search request.
     * 
     * @param results
     *            an extended attendees search response.
     */
    public void setExtendedAttendeeSearchResults(ExtendedAttendeeSearchReply results) {
        extendedAttendeeSearchResults = results;
    }

    /**
     * Gets the latest response to an extended attendee search request.
     * 
     * @return the extended attendees search response.
     */
    public ExtendedAttendeeSearchReply getExtendedAttendeeSearchResults() {
        return extendedAttendeeSearchResults;
    }

    /**
     * Sets the latest response to a search approver request.
     * 
     * @param results
     *            a search approver response.
     */
    public void setApproverSearchResults(ApproverSearchReply results) {
        approverSearchResults = results;
    }

    /**
     * Gets the latest response to a search approver request.
     * 
     * @return the search approver response.
     */
    public ApproverSearchReply getApproverSearchResults() {
        return approverSearchResults;
    }

    /**
     * Gets the last reply from a corp SSO query request.
     * 
     * @return the corp sso query reply.
     */
    public CorpSsoQueryReply getCorpSsoQueryReply() {
        return corpSsoQueryReply;
    }

    /**
     * Sets the last reply from a corp SSO query request.
     * 
     * @param reply
     *            the corp sso query reply.
     */
    public void setCorpSsoQueryReply(CorpSsoQueryReply reply) {
        corpSsoQueryReply = reply;
    }

    /**
     * Sets the list of attendees that was selected based on an attendee search.
     * 
     * @param attendees
     *            the selected attendee list.
     */
    public void setSelectedAttendees(List<ExpenseReportAttendee> attendees) {
        selectedAttendees = attendees;
    }

    /**
     * Gets the attendee that was selected based on an attendee search.
     * 
     * @return an attendee that was selected based on an attendee search.
     */
    public List<ExpenseReportAttendee> getSelectedAttendees() {
        return selectedAttendees;
    }

    /**
     * Sets the last downloaded attendee form.
     * 
     * @param attendeeForm
     *            the last downloaded attendee form.
     */
    public void setAttendeeForm(ExpenseReportAttendee attendeeForm) {
        this.attendeeForm = attendeeForm;
    }

    /**
     * Gets the last downloaded attendee form.
     * 
     * @return the last downloaded attendee form.
     */
    public ExpenseReportAttendee getAttendeeForm() {
        return attendeeForm;
    }

    /**
     * Gets the list of attendees that are being edited.
     * 
     * @return the list of attendees that are being edited.
     */
    public List<ExpenseReportAttendee> getEditedAttendees() {
        return editedAttendees;
    }

    /**
     * Sets the list of attendees that are being edited.
     * 
     * @param attendees
     *            the list of attendees that are being edited.
     */
    public void setEditedAttendees(List<ExpenseReportAttendee> attendees) {
        this.editedAttendees = attendees;
    }

    /**
     * Sets the results for the last AttendeeSaveRequest
     * 
     */
    public void setAttendeeSaveResults(AttendeeSaveReply results) {
        attendeeSaveResults = results;
    }

    /**
     * Gets the results for the last AttendeeSaveRequest
     */
    public AttendeeSaveReply getAttendeeSaveResults() {
        return attendeeSaveResults;
    }

    /**
     * set the results from the last call to GetDynamicFieldActions
     * 
     * @param conditionalFieldActions
     */
    public void setConditionalFieldActionsResults(List<ConditionalFieldAction> conditionalFieldActions) {
        conditionalFieldActionsResults = conditionalFieldActions;
    }

    /**
     * Return the results of last call to GetDynamicFieldActions
     * 
     * @return
     */
    public List<ConditionalFieldAction> getConditionalFieldActionsResults() {
        return conditionalFieldActionsResults;
    }

    /**
     * Sets the most recent hotel search results.
     * 
     * @param results
     *            the search results.
     */
    public void setHotelSearchResults(HotelSearchReply results) {
        this.hotelSearchResults = results;
    }

    /**
     * Gets the most recent hotel search results.
     * 
     * @return the hotel search results.
     */
    public HotelSearchReply getHotelSearchResults() {
        return hotelSearchResults;
    }

    /**
     * Gets the most recent retrieved hotel detail information based on property id.
     * 
     * @param propertyId
     *            the hotel property id.
     * @return the hotel details object upon success; <code>null</code> otherwise.
     */
    public HotelChoiceDetail getHotelDetail(String propertyId) {
        HotelChoiceDetail detail = null;
        if (hotelDetailCache != null && hotelDetailCache.containsKey(propertyId)) {
            detail = hotelDetailCache.get(propertyId);
        }
        return detail;
    }

    /**
     * Adds to the in-memory cache the hotel detail information for a particular property id.
     * 
     * @param propertyId
     *            the hotel property id.
     * @param detail
     *            the hotel detail.
     */
    public void setHotelDetail(String propertyId, HotelChoiceDetail detail) {
        if (hotelDetailCache == null) {
            hotelDetailCache = new HashMap<String, HotelChoiceDetail>();
        }
        hotelDetailCache.put(propertyId, detail);
    }

    public void setRailSearchResults(RailSearchReply results) {
        this.railSearchResults = results;
    }

    public RailSearchReply getRailSearchResults() {
        return railSearchResults;
    }

    public void setLocationSearchResults(LocationSearchReply results) {
        this.locationSearchResults = results;
    }

    public LocationSearchReply getLocationSearchResults() {
        return locationSearchResults;
    }

    public void setExpenseSearchListResults(SearchListResponse results) {
        this.expenseSearchListResults = results;
    }

    public SearchListResponse getExpenseSearchListResults() {
        return expenseSearchListResults;
    }

    public ArrayList<RailStation> getRailStationList() {
        return railStations;
    }

    public void setRailStationList(ArrayList<RailStation> stations) {
        this.railStations = stations;
    }

    public void setCodeRailStationMap(HashMap<String, RailStation> map) {
        codeRailStationMap = map;
    }

    public HashMap<String, RailStation> getCodeRailStationMap() {
        return codeRailStationMap;
    }

    public Calendar getRailStationListLastRetrieved() {
        return railStationsLastRetrieved;
    }

    public void setRailStationListLastRetrieved(Calendar stationsLastRetrieved) {
        this.railStationsLastRetrieved = stationsLastRetrieved;
    }

    public TravelAllowanceItineraryController getTaItineraryController() {
        if (taItineraryController == null) {
            taItineraryController = new TravelAllowanceItineraryController(this);
        }
        return taItineraryController;
    }

    /**
     * Creates an instance of a {@link FixedTravelAllowanceController}
     * @return The controller
     */
    public FixedTravelAllowanceController getFixedTravelAllowanceController() {
        if (this.fixedTravelAllowanceController == null) {
            this.fixedTravelAllowanceController = new FixedTravelAllowanceController(this);
        }
        return this.fixedTravelAllowanceController;
    }

    public TaConfig getTAConfig() {
        return taConfig;
    }

    public void setTAConfig(TaConfig taConfig) {
        this.taConfig = taConfig;
    }

    public Itinerary getTAItinerary() {
        return taItinerary;
    }

    public void setTAItinerary(Itinerary taItinerary) {
        this.taItinerary = taItinerary;
    }

    public FixedAllowances getFixedAllowances() {
        return fixedAllowances;
    }

    public void setFixedAllowances(FixedAllowances fixedAllowances) {
        this.fixedAllowances = fixedAllowances;
    }

    public ItineraryRow getSelectedTAItineraryRow() {
        return taItineraryRow;
    }

    public void setSelectedTAItineraryRow(ItineraryRow row) {
        this.taItineraryRow = row;
    }

    public List<TripToApprove> getTripsToApprove() {
        return tripsToApprove;
    }

    public void setTripsToApprove(List<TripToApprove> tripsToApprove) {
        this.tripsToApprove = tripsToApprove;
    }

    public Calendar getTripsToApproveLastRetrieved() {
        return tripsToApproveLastRetrieved;
    }

    public void setTripsToApproveLastRetrieved(Calendar tripsToApproveLastRetrieved) {
        this.tripsToApproveLastRetrieved = tripsToApproveLastRetrieved;
    }

    public List<RuleViolation> getTripRuleViolations() {
        return tripRuleViolations;
    }

    public void setTripRuleViolations(List<RuleViolation> tripRuleViolations) {
        this.tripRuleViolations = tripRuleViolations;
    }

    public Calendar getTripRuleViolationsLastRetrieved() {
        return tripRuleViolationsLastRetrieved;
    }

    public void setTripRuleViolationsLastRetrieved(Calendar tripRuleViolationsLastRetrieved) {
        this.tripRuleViolationsLastRetrieved = tripRuleViolationsLastRetrieved;
    }

    public TravelCustomField getTravelCustomField() {
        return travelCustomField;
    }

    public void setTravelCustomField(TravelCustomField travelCustomField) {
        this.travelCustomField = travelCustomField;
    }

    public Calendar getTravelCustomFieldLastRetrieved() {
        return travelCustomFieldLastRetrieved;
    }

    public void setTravelCustomFieldLastRetrieved(Calendar travelCustomFieldLastRetrieved) {
        this.travelCustomFieldLastRetrieved = travelCustomFieldLastRetrieved;
    }

    public MWSResponseStatus getRequestTaskStatus() {
        return requestTaskStatus;
    }

    public void setRequestTaskStatus(MWSResponseStatus requestTaskStatus) {
        this.requestTaskStatus = requestTaskStatus;
    }

    /**
     * Gets the current system configuration information related to the current end-users company.
     * 
     * @return the current system configuration information related to the current end-users company.
     */
    public SystemConfig getSystemConfig() {
        if (systemConfig == null && !getSystemConfigDelayed) {
            initSystemConfig();
        }
        return systemConfig;
    }

    /**
     * Sets the current system configuration information related to the current end-users company.
     * 
     * @param systemConfig
     *            the current system configuration information.
     */
    public void setSystemConfig(SystemConfig systemConfig) {
        if (systemConfig == null) {
            removeStickyBroadcast(sysConfigAvailIntent);
            sendStickyBroadcast(sysConfigUnAvailIntent);
        } else {
            removeStickyBroadcast(sysConfigUnAvailIntent);
            sendStickyBroadcast(sysConfigAvailIntent);
        }
        this.systemConfig = systemConfig;
    }

    /**
     * Gets the last time the system configuration was retrieved.
     * 
     * @return the last time the system configuration was retrieved.
     */
    public Calendar getSystemConfigLastRetrieved() {
        return systemConfigLastRetrieved;
    }

    /**
     * Gets the last retrieved list of reason codes.
     * 
     * @return the list of reason codes.
     */
    public ReasonCodeReply getReasonCodes() {
        return reasonCodes;
    }

    /**
     * Sets the current retrieved list of reason codes.
     * 
     * @param reasonCodes
     *            the list of reason codes.
     */
    public void setReasonCodes(ReasonCodeReply reasonCodes) {
        this.reasonCodes = reasonCodes;
    }

    /**
     * Gets the travel custom fields configuration.
     * 
     * @return returns the last retrieved travel custom fields configuration.
     */
    public TravelCustomFieldsConfig getTravelCustomFieldsConfig() {
        return travelCustomFieldsConfig;
    }

    /**
     * Sets the travel custom fields configuration.
     * 
     * @param config
     *            an instance of <code>TravelCustomFieldsConfig</code>.
     */
    public void setTravelCustomFieldsConfig(TravelCustomFieldsConfig config) {
        travelCustomFieldsConfig = config;
    }

    /**
     * Sets the last time the system configuration was retrieved.
     * 
     * @param systemConfigLastRetrieved
     *            the last time the system configuration was retrieved.
     */
    public void setSystemConfigLastRetrieved(Calendar systemConfigLastRetrieved) {
        this.systemConfigLastRetrieved = systemConfigLastRetrieved;
    }

    /**
     * Gets the current user configuration information associated with the currently logged in end-user. This method will call
     * <code>ConcurMobile.initUserConfig</code> if the user configuration information is not currently loaded.
     * 
     * @return an instance of <code>UserConfig</code> or <code>null</code> if no local information.
     */
    public UserConfig getUserConfig() {
        if (userConfig == null && !getUserConfigDelayed) {
            initUserConfig();
        }
        return userConfig;
    }

    /**
     * Sets the current user configuration information.
     * 
     * @param userConfig
     *            the user configuration information.
     */
    public void setUserConfig(UserConfig userConfig) {
        if (userConfig == null) {
            removeStickyBroadcast(userConfigAvailIntent);
            sendStickyBroadcast(userConfigUnAvailIntent);
        } else {
            removeStickyBroadcast(userConfigUnAvailIntent);
            sendStickyBroadcast(userConfigAvailIntent);
        }
        this.userConfig = userConfig;
    }

    /**
     * Gets the time at which the last update to user configuration information was obtained.
     * 
     * @return the time at which the last user configuration information was obtained.
     */
    public Calendar getUserConfigLastRetrieved() {
        return userConfigLastRetrieved;
    }

    /**
     * Sets the time at which the last update to user configuration information was obtained.
     * 
     * @param userConfigLastRetrieved
     */
    public void setUserConfigLastRetrieved(Calendar userConfigLastRetrieved) {
        this.userConfigLastRetrieved = userConfigLastRetrieved;
    }

    /**
     * Will update a text view component whose resource id is <code>R.id.dataBarLastUpdate</code> in the view associated with the
     * <code>screen</code> activity with the
     * 
     * @param screen
     * @param date
     */
    public void updateLastUpdateText(Activity screen, Calendar date) {

        if (date != null) {
            TextView tv = (TextView) screen.findViewById(R.id.dataBarLastUpdate);
            if (tv != null) {
                Time local = new Time(Time.getCurrentTimezone());
                local.set(date.getTimeInMillis());
                FormatUtil.DATA_UPDATE_DISPLAY.setTimeZone(TimeZone.getTimeZone(Time.getCurrentTimezone()));
                tv.setText(Format.localizeText(this, R.string.databar_lastupdate,
                        FormatUtil.DATA_UPDATE_DISPLAY.format(date.getTime())));
            }
        } else {
            Log.i(Const.LOG_TAG, CLS_TAG + ".updateLastUpdateTime: null date!");
        }
    }

    /**
     * Retrieve a reference to the single instance of our service
     * 
     * @return A reference to the {@link ConcurService} or null if not connected
     */
    public ConcurService getService() {
        return concurService;
    }

    /**
     * Return the Activity used for handling export of a report to a local format (such as PDF)
     * 
     * @return An Activity that can be launched to handle the export or null if no such Activity exists in the product
     */
    public Class<? extends Activity> getExportActivity() {
        return null;
    }

    /**
     * Gets a reference to the in-memory itinerary cache.
     * 
     * @return a reference to an <code>IItineraryCache</code> itinerary cache.
     */
    public IItineraryCache getItinCache() {
        return itinCache;
    }

    /**
     * Gets the instance of <code>IExpenseReportCache</code> for reports that have been submitted for approval.
     * 
     * @return an instance of <code>IExpenseReportCache</code> for reports that have been submitted for approval.
     */
    public IExpenseReportCache getExpenseApprovalCache() {
        return expenseCache.getApprovalCache();
    }

    /**
     * Gets the instance of <code>IExpenseReportCache</code> for active reports to be submitted for approval.
     * 
     * @return an instance of <code>IExpenseReportCache</code> for active reports to be submitted for approval.
     */
    public IExpenseReportCache getExpenseActiveCache() {
        return expenseCache.getActiveCache();
    }

    /**
     * Gets the instance of <code>IExpenseEntryCache</code> associated with this application.
     * 
     * @return an instance of <code>IExpenseEntryCache</code>.
     */
    public IExpenseEntryCache getExpenseEntryCache() {
        return expenseCache.getEntryCache();
    }

    /**
     * Gets the instance of <code>ReceiptStoreCache</code> associated with the application.
     * 
     * @return an instance of <code>ReceiptStoreCache</code>.
     */
    public ReceiptStoreCache getReceiptStoreCache() {
        return expenseCache.getReceiptStoreCache();
    }

    /**
     * Sets the currently-being-used entry form.
     * 
     * @param ed
     */
    public void setCurrentEntryDetailForm(ExpenseReportEntryDetail ed) {
        currentEntryForm = ed;
    }

    /**
     * Returns the currently-being-used entry form. If you did not just make a service request to get this form then do not assume
     * the value here is what you want.
     * 
     * @return
     */
    public ExpenseReportEntryDetail getCurrentEntryDetailForm() {
        return currentEntryForm;
    }

    public TripApprovalReqObject getTripApprovalReqObj() {
        return tripApprovalReqObj;
    }

    public void setTripApprovalReqObj(TripApprovalReqObject tripApprovalReqObj) {
        this.tripApprovalReqObj = tripApprovalReqObj;
    }

    // start of Hotel streaming
    public ArrayList<String> getPropertyIdsAlreadyPriced() {
        return propertyIdsAlreadyPriced;
    }

    // end of hotel streaming

    public List<Violation> getTravelPolicyViolations() {
        return travelPolicyViolations;
    }

    public void setTravelPolicyViolations(List<Violation> travelPolicyViolations) {
        this.travelPolicyViolations = travelPolicyViolations;
    }

    public Calendar getTravelPolicyViolationsLastRetrieved() {
        return travelPolicyViolationsLastRetrieved;
    }

    public void setTravelPolicyViolationsLastRetrieved(Calendar travelPolicyViolationsLastRetrieved) {
        this.travelPolicyViolationsLastRetrieved = travelPolicyViolationsLastRetrieved;
    }

    /**
     * @return the viewedPriceToBeatListLastRetrieved
     */
    public Calendar getViewedPriceToBeatListLastRetrieved() {
        return viewedPriceToBeatListLastRetrieved;
    }

    /**
     * @param viewedPriceToBeatListLastRetrieved
     *            the viewedPriceToBeatListLastRetrieved to set
     */
    public void setViewedPriceToBeatListLastRetrieved(Calendar viewedPriceToBeatListLastRetrieved) {
        this.viewedPriceToBeatListLastRetrieved = viewedPriceToBeatListLastRetrieved;
    }

    /**
     * @return the viewedPriceToBeatList
     */
    public boolean hasViewedPriceToBeatList() {
        return viewedPriceToBeatList;
    }

    /**
     * @param viewedPriceToBeatList
     *            the viewedPriceToBeatList to set
     */
    public void setViewedPriceToBeatList(boolean viewedPriceToBeatList) {
        this.viewedPriceToBeatList = viewedPriceToBeatList;
    }

    /**
     * Will initialize system configuration information for a particular user.
     */
    public void initSystemConfig() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        systemConfig = null;
        systemConfigLastRetrieved = null;
        sendSystemConfigDelayed = false;
        getSystemConfigDelayed = false;
        removeStickyBroadcast(sysConfigAvailIntent);
        removeStickyBroadcast(sysConfigUnAvailIntent);
        if (userId != null) {
            if (concurService != null) {
                systemConfig = concurService.getSystemConfig(userId);
                if (systemConfig == null) {
                    sendStickyBroadcast(sysConfigUnAvailIntent);
                } else {
                    sendStickyBroadcast(sysConfigAvailIntent);
                }
                if (isConnected()) {
                    // Send a request to retrieve it.
                    String hash = ((systemConfig != null) ? systemConfig.getHash() : null);
                    concurService.sendSystemConfigRequest(userId, hash);
                } else {
                    sendSystemConfigDelayed = true;
                }
            } else {
                getSystemConfigDelayed = true;
                sendSystemConfigDelayed = true;
                sendStickyBroadcast(sysConfigUnAvailIntent);
            }
        }
    }

    /**
     * Will initialize user configuration information.
     */
    public void initUserConfig() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        userConfig = null;
        userConfigLastRetrieved = null;
        sendUserConfigDelayed = false;
        getUserConfigDelayed = false;
        removeStickyBroadcast(userConfigAvailIntent);
        removeStickyBroadcast(userConfigUnAvailIntent);
        if (userId != null) {
            if (concurService != null) {
                userConfig = concurService.getUserConfig(userId);
                if (userConfig == null) {
                    sendStickyBroadcast(userConfigUnAvailIntent);
                } else {
                    sendStickyBroadcast(userConfigAvailIntent);
                }

                if (isConnected()) {
                    // Send a request to retrieve it.
                    String hash = ((userConfig != null) ? userConfig.hash : null);
                    concurService.sendUserConfigRequest(userId, hash);
                    if (prefs.getBoolean(Const.PREF_CAN_EXPENSE, false)) {
                        // Send the request to retrieve the default user
                        // attendee.
                        concurService.sendDefaultAttendeeRequest(userId);
                    }
                } else {
                    sendUserConfigDelayed = true;
                }
            } else {
                sendStickyBroadcast(userConfigUnAvailIntent);
                getUserConfigDelayed = true;
                sendUserConfigDelayed = true;
            }
        }

    }

    /**
     * Gets the instance of <code>RequestCache</code> for requests that have been retrieved.
     * 
     * @return an instance of <code>RequestCache</code>
     */
    public Cache<String, RequestDTO> getRequestListCache() {
        return requestListCache;
    }

    /**
     * Gets the instance of <code>RequestCache</code> for the retrieved requests forms / request segments forms.
     * 
     * @return an instance of <code>RequestCache</code>
     */
    public Cache<String, ConnectForm> getRequestFormFieldsCache() {
        return requestFormFieldsCache;
    }

    /**
     * Gets the instance of <code>RequestGroupConfigurationCache</code> for requests configurations that have been retrieved.
     *
     * @return an instance of <code>RequestGroupConfigurationCache</code>
     */
    public Cache<String, RequestGroupConfiguration> getRequestGroupConfigurationCache() {
        return requestGroupConfigurationCache;
    }

    /**
     * Clears any in-memory caches of data specific to an end-user.
     */
    public void clearCaches() {
        expenseCache = new ExpenseCache(this);
        itinCache = new ItineraryCache(this);
        hotelDetailCache.clear();
        requestListCache.clear();
        requestFormFieldsCache.clear();
    }

    /**
     * clears in-memory cache of hotel details
     */
    public void clearHotelDetailCache() {
        if (hotelDetailCache != null) {
            hotelDetailCache.clear();
        }
    }

    /**
     * Clears all offline data
     */
    public void clearLocalData() {
        // Clear any in-memory data
        clearCaches();
        // clear content provider data
        clearContentProviderData();
        cards = null;
        cardsLastRetrieved = null;
        summary = null;
        summaryLastRetrieved = null;
        attendeeSearchResults = null;
        extendedAttendeeSearchResults = null;
        attendeeSaveResults = null;
        attendeeForm = null;
        selectedAttendees = null;
        editedAttendees = null;
        carConfigs = null;
        carConfigsLastRetrieved = null;
        airSearchResults = null;
        airFilterResults = null;
        corpSsoQueryReply = null;
        travelCustomFieldsConfig = null;
        reasonCodes = null;
        systemConfig = null;
        userConfig = null;
        // TODO cdiaz: Clear Platform SysConfig and UserConfig once it's
        // implemented.
        // Reset the DB
        concurService.resetData();
    }

    private void clearContentProviderData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.this);
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        ExpenseUtil.clearSmartExpenses(this, userId);
    }

    /**
     * Gets the instance of <code>View</code> that provides a promotional view as part of the "What's New" dialog.
     * 
     * @return an instance of <code>View</code> that provides a promotional view.
     */
    public View getPromoView(Context ctx) {
        return null;
    }

    public void launchStartUpActivity(Activity act) {

    }

    public void launchHome(Activity act) {

    }

    public View getWhatsNewView(Context ctx) {
        View whatsNewView = null;
        LayoutInflater inflater = LayoutInflater.from(ctx);
        whatsNewView = inflater.inflate(R.layout.whats_new, null);
        String versionName;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            versionName = "0.0.0";
        }
        String title = Format.localizeText(this, R.string.whats_new_in_version, versionName);
        TextView txtView = (TextView) whatsNewView.findViewById(R.id.whats_new_title);
        if (txtView != null) {
            txtView.setText(title);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getWhatsNewView: unable to locate 'whats_new_title' view!");
        }
        TableLayout tblLayout = (TableLayout) whatsNewView.findViewById(R.id.whats_new_table);
        if (tblLayout != null) {
            // Loop through the string resources to load all the whats new items
            final String whatsNewPrefix = "whats_new_item_";
            int item = 1;
            StringBuilder whatsNewKey = new StringBuilder(whatsNewPrefix);
            final Resources resources = this.getResources();
            while (item > 0) {
                whatsNewKey.append(item);
                int resId = resources.getIdentifier(whatsNewKey.toString(), "string", getStringResourcePackageName());
                if (resId > 0) {
                    CharSequence itemText = getText(resId);
                    TableRow tblRow = (TableRow) inflater.inflate(R.layout.whats_new_row, null);
                    txtView = (TextView) tblRow.findViewById(R.id.whats_new_item_text);
                    if (txtView != null) {
                        txtView.setText(itemText);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".getWhatsNewView: unable to locate 'whats_new_item_text' view!");
                    }
                    tblLayout.addView(tblRow);
                    item++;
                    whatsNewKey.setLength(0);
                    whatsNewKey.append(whatsNewPrefix);
                } else {
                    item = 0;
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getWhatsNewView: unable to locate 'whats_new_table' view!");
        }
        return whatsNewView;
    }

    /**
     * An aggregator method to keep all dialogs consist across the application. To use this method an activity must override their
     * onCreateDialog() and call this one.
     * 
     * @param activity
     *            A {@link Activity} object.
     * @param id
     *            The id of the desired dialog. See {@link Const} for the constant values.
     * @return
     */
    public Dialog createDialog(final Activity activity, int id) {

        ProgressDialog dialog = null;

        switch (id) {
        case Const.DIALOG_LOGIN_WAIT:
            dialog = new ProgressDialog(activity);
            dialog.setMessage(this.getText(R.string.dlg_logging_in));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        case Const.DIALOG_REGISTER_PIN:
            dialog = new ProgressDialog(activity);
            dialog.setMessage(this.getText(R.string.dlg_registering));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        case Const.DIALOG_RETRIEVE_ITINS:
            dialog = new ProgressDialog(activity);
            dialog.setMessage(this.getText(R.string.dlg_retrieving_itin));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        case Const.DIALOG_TRAVEL_RETRIEVE_FLIGHT_STATS:
            dialog = new ProgressDialog(activity);
            dialog.setMessage(this.getText(R.string.dlg_travel_flight_stat_retrieve));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        case Const.DIALOG_EXPENSE_RETRIEVE_EXPENSE_APPROVALS:
            dialog = new ProgressDialog(activity);
            dialog.setMessage(getText(R.string.dlg_retrieving_exp_rep));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        case Const.DIALOG_EXPENSE_APPROVE_REPORT_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_report_approve_failed);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_NO_PDF_VIEWER: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_no_viewer);
            dlgBldr.setMessage(R.string.dlg_expense_no_pdf_viewer);
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // NOTE: Explicitly call 'cancel' rather than
                    // 'dismiss' as there are cancel listeners
                    // in both 'ExpenseReceipt' and 'ViewImage' to
                    // handle finishing the respective
                    // activities. A dismiss listener will be invoked
                    // upon orientation change which
                    // which can result in 'ExpenseReceipt' and
                    // 'ViewImage' prematurely being "finished".
                    dialog.cancel();
                }
            });
            return dlgBldr.create();
        }
        // Use NoConnectivityDialogFragment for any future uses of this dialog.
        case Const.DIALOG_NO_CONNECTIVITY: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_no_connectivity_title);
            dlgBldr.setMessage(R.string.dlg_no_connectivity_message);
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_REMOVE_REPORT_EXPENSE_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_remove_report_expense_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_CAMERA_IMAGE_IMPORT_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_camera_image_import_failed_title);
            dlgBldr.setMessage(R.string.dlg_expense_camera_image_import_failed_message);
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_REPORT_DETAIL:
            dialog = new ProgressDialog(activity);
            dialog.setMessage(getText(R.string.dlg_retrieving_exp_detail));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        case Const.DIALOG_EXPENSE_SAVE_RECEIPT:
            dialog = new ProgressDialog(activity);
            dialog.setMessage(getText(R.string.dlg_saving_receipt));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        case Const.DIALOG_EXPENSE_SAVE_RECEIPT_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_save_receipt_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_NO_IMAGING_CONFIGURATION: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_no_imaging_configuration_title);
            dlgBldr.setMessage(R.string.dlg_expense_no_imaging_configuration_message);
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_ADD_REPORT_RECEIPT_SUCCEEDED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_save_report_receipt_title);
            dlgBldr.setMessage(R.string.dlg_expense_save_report_receipt_succeeded_message);
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_ADD_REPORT_RECEIPT_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_save_report_receipt_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_no_external_storage_available_title);
            dlgBldr.setMessage(R.string.dlg_expense_no_external_storage_available_message);
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY_RECEIPT_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_save_report_entry_receipt_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_CLEAR_RECEIPT_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_clear_report_entry_receipt_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_DELETE_REPORT_PROGRESS: {
            dialog = new ProgressDialog(activity);
            dialog.setMessage(getText(R.string.deleting_report));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        }
        case Const.DIALOG_EXPENSE_DELETE_REPORT_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_report_delete_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT_IMAGE_URL:
            dialog = new ProgressDialog(activity);
            dialog.setMessage(getText(R.string.dlg_expense_retrieve_receipt_image_url));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        case Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT_IMAGE_URL_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_e_receipt_unavailable_title);
            dlgBldr.setMessage(R.string.dlg_e_receipt_unavailable);
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_POLICY_ERROR_PROMPT: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.report_submit_error);
            dlgBldr.setMessage(getText(R.string.report_submit_policy_error));
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_NO_EXPENSE_TYPE_CURRENCY: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_no_expense_type_currency_title);
            dlgBldr.setMessage(getText(R.string.dlg_expense_no_expense_type_currency_message));
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_UNDEFINED_EXPENSE_TYPE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.report_submit_error);
            dlgBldr.setMessage(getText(R.string.report_submit_undefined_expense_type));
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_MISSING_RECEIPT: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setMessage(getText(R.string.report_submit_missing_receipt));
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_REJECT_COMMENT_PROMPT: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setMessage(getText(R.string.send_back_comment_prompt));
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        // Use SystemUnavailableDialogFragment for this case if making a new
        // system unavailable alert dialog
        case Const.DIALOG_SYSTEM_UNAVAILABLE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_system_unavailable_title);
            dlgBldr.setMessage(getText(R.string.dlg_system_unavailable_message));
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_EXPENSES_RETRIEVE_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expenses_retrieve_failed_title);
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT:
            dialog = new ProgressDialog(activity);
            dialog.setMessage(getText(R.string.dlg_retrieve_exp_receipt));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        case Const.DIALOG_EXPENSE_RETRIEVE_REPORT_RECEIPT:
            dialog = new ProgressDialog(activity);
            dialog.setMessage(getText(R.string.dlg_retrieve_report_receipt));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        case Const.DIALOG_TRAVEL_RETRIEVE_HOTEL_DETAIL:
            dialog = new ProgressDialog(activity);
            dialog.setMessage(getText(R.string.dlg_retrieve_travel_hotel_detail));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        case Const.DIALOG_TRAVEL_RETRIEVE_HOTEL_DETAIL_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_retrieve_travel_hotel_detail_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_TRAVEL_NO_AIR_PERMISSION: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.general_air_booking);
            dlgBldr.setMessage(R.string.dlg_no_air_permission_message);
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_TRAVEL_FLEX_FARE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.general_air_booking);
            dlgBldr.setMessage(R.string.dlg_no_air_flex_message);
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_TRAVEL_BOOKING_CUSTOM_REQUIRED_FIELDS: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.general_travel_booking);
            dlgBldr.setMessage(R.string.dlg_booking_custom_required_fields_message);
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_TRAVEL_PROFILE_INCOMPLETE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.general_travel_profile);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            int travelProfile = prefs.getInt(Const.PREF_TRAVEL_PROFILE_STATUS,
                    Const.TRAVEL_PROFILE_ALL_REQUIRED_DATA_PLUS_TSA);
            dlgBldr.setMessage(ViewUtil.getTextResourceIdForProfileCheck(travelProfile));
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_SAVE:
            dialog = new ProgressDialog(activity);
            dialog.setMessage(getText(R.string.dlg_expense_save));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            break;
        case Const.DIALOG_EXPENSE_DELETE:
            dialog = new ProgressDialog(activity);
            dialog.setMessage(getText(R.string.dlg_expense_delete));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            break;
        case Const.DIALOG_EXPENSE_ADD_TO_REPORT_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_add_to_report_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_APPROVAL_RETRIEVE_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_approval_retrieve_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_ACTIVE_REPORT_RETRIEVE_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_active_report_retrieve_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_travel_itinerary_retrieve_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY: {
            dialog = new ProgressDialog(activity);
            dialog.setMessage(this.getText(R.string.dlg_travel_itinerary_retrieve));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        }
        case Const.DIALOG_EXPENSE_REPORT_DETAIL_RETRIEVE_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_report_detail_retrieve_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_SAVE_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_save_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_DELETE_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_delete_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_DOWNLOAD_RECEIPT_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_upload_receipt_failed_title);
            dlgBldr.setMessage(R.string.dlg_expense_upload_receipt_failed_message);
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT_UNAVAILABLE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setMessage(getText(R.string.dlg_receipt_unavailable));
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_E_RECEIPT_UNAVAILABLE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(getText(R.string.dlg_e_receipt_unavailable_title));
            dlgBldr.setMessage(getText(R.string.dlg_e_receipt_unavailable));
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_MOBILE_ENTRY: {
            dialog = new ProgressDialog(activity);
            dialog.setMessage(getText(R.string.dlg_retrieve_outofpocket));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        }
        case Const.DIALOG_RETRIEVE_CARDS:
            dialog = new ProgressDialog(activity);
            dialog.setMessage(this.getText(R.string.dlg_retrieving_cards));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        case Const.DIALOG_OUT_OF_POCKET_EXPENSE_TRANSACTION_DATE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setMessage(getText(R.string.out_of_pocket_expense_transaction_date_prompt));
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_OUT_OF_POCKET_EXPENSE_VENDOR_NAME: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setMessage(getText(R.string.out_of_pocket_expense_vendor_name_prompt));
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_OUT_OF_POCKET_EXPENSE_TYPE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setMessage(getText(R.string.out_of_pocket_expense_type_prompt));
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_OUT_OF_POCKET_EXPENSE_CURRENCY: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setMessage(getText(R.string.out_of_pocket_expense_currency_prompt));
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_OUT_OF_POCKET_EXPENSE_AMOUNT: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_quick_expense_amount_title);
            dlgBldr.setMessage(R.string.dlg_quick_expense_amount_message);
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_OUT_OF_POCKET_EXPENSE_LOCATION_NAME: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setMessage(getText(R.string.out_of_pocket_expense_location_name_prompt));
            dlgBldr.setPositiveButton(activity.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_RETRIEVE_ACTIVE_REPORTS: {
            dialog = new ProgressDialog(activity);
            dialog.setMessage(this.getText(R.string.dlg_retrieving_active_reports));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        }
        case Const.DIALOG_EXPENSE_ENTRY_FORM: {
            dialog = new ProgressDialog(activity);
            dialog.setMessage(this.getText(R.string.dlg_retrieving_expense_form));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        }
        case Const.DIALOG_EXPENSE_CREATE_REPORT: {
            dialog = new ProgressDialog(activity);
            dialog.setMessage(this.getText(R.string.dlg_creating_report));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            break;
        }
        case Const.DIALOG_WHATS_NEW: {
            List<View> views = new ArrayList<View>();
            View promo = getPromoView(activity);
            if (promo != null) {
                views.add(promo);
            }
            View whatsNew = getWhatsNewView(activity);
            if (whatsNew != null) {
                views.add(whatsNew);
            }
            MultiViewDialog mvDialog = new MultiViewDialog(activity, R.style.ConcurTheme_Dialog_WhatsNew, views);
            return mvDialog;
        }
        // Use PromptToRateDialogFragment for this case from now on. Make sure
        // to check if ConcurCore.isConnected()
        case Const.DIALOG_PROMPT_TO_RATE: {
            FeedbackManager.with(activity).showRatingsPrompt();

            /*
             * if (ConcurCore.isConnected()) { AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
             * dlgBldr.setTitle(R.string.dlg_prompt_to_rate_title); dlgBldr.setMessage(R.string.dlg_prompt_to_rate_message);
             * dlgBldr.setPositiveButton (activity.getText(R.string.dlg_prompt_to_rate_yes), new DialogInterface.OnClickListener()
             * {
             * 
             * @Override public void onClick(DialogInterface dialog, int which) { SharedPreferences prefs =
             * PreferenceManager.getDefaultSharedPreferences(ConcurCore .getContext()); Preferences.setPromptedToRate(prefs);
             * dialog.dismiss();
             * 
             * Intent market = new Intent(Intent.ACTION_VIEW, Uri .parse("market://details?id=com.concur.breeze"));
             * market.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             * 
             * try { startActivity(market); } catch (ActivityNotFoundException e) { Log.e(Const.LOG_TAG,
             * "No activity found to handle market:// URI"); } } }); dlgBldr.setNegativeButton
             * (activity.getText(R.string.dlg_prompt_to_rate_no), new DialogInterface.OnClickListener() {
             * 
             * @Override public void onClick(DialogInterface dialog, int which) { SharedPreferences prefs =
             * PreferenceManager.getDefaultSharedPreferences(ConcurCore .getContext()); Preferences.setPromptedToRate(prefs);
             * dialog.dismiss(); } }); return dlgBldr.create();
             * 
             * } else { Log.d(Const.LOG_TAG, "showdialog.rate : offline"); }
             */
            break;
        }
        case Const.DIALOG_EXPENSE_NO_EXPENSE_TYPES: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(activity);
            dlgBldr.setTitle(R.string.dlg_expense_no_expense_types_title);
            dlgBldr.setMessage(R.string.dlg_expense_no_expense_types_message);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();

        }
        }
        return dialog;
    }

    // TODO: clean up these helpers to use the Application context

    /**
     * Save a preference value into the default {@link SharedPreferences} used by the application.
     * 
     * @deprecated - use
     *             {@link com.concur.mobile.platform.ui.common.util.PreferenceUtil#savePreference(Context ctx, String name, String value)}
     *             instead.
     * 
     * @param ctx
     *            The {@link Context}. Typically a calling {@link Activity}
     * @param name
     *            The name of the shared preference. See {@link Const} for a list of names.
     * @param value
     *            The {@link String} value to save
     */
    @Deprecated
    public void savePreference(Context ctx, String name, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        savePreference(prefs, name, value);
    }

    /**
     * Save a preference value into the default {@link SharedPreferences} used by the application.
     * 
     * @deprecated - use
     *             {@link com.concur.mobile.platform.ui.common.util.PreferenceUtil#savePreference(Context ctx, String name, boolean value)}
     *             instead.
     * 
     * @param ctx
     *            The {@link Context}. Typically a calling {@link Activity}
     * @param name
     *            The name of the shared preference. See {@link Const} for a list of names.
     * @param value
     *            The {@link Boolean} value to save
     */
    @Deprecated
    public void savePreference(Context ctx, String name, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        savePreference(prefs, name, value);
    }

    /**
     * Save a preference value into an existing {@link SharedPreferences}
     * 
     * @deprecated - use
     *             {@link com.concur.mobile.platform.ui.common.util.PreferenceUtil#savePreference(SharedPreferences prefs, String name, String value)}
     *             instead.
     * 
     * @param prefs
     *            A {@link SharedPreferences} to hold the preference.
     * @param name
     *            The name of the shared preference. See {@link Const} for a list of names.
     * @param value
     *            The {@link String} value to save
     */
    @Deprecated
    public void savePreference(SharedPreferences prefs, String name, String value) {
        Editor e = prefs.edit();
        e.putString(name, value);
        e.commit();
    }

    /**
     * Save a preference value into an existing {@link SharedPreferences}
     * 
     * @deprecated - use
     *             {@link com.concur.mobile.platform.ui.common.util.PreferenceUtil#savePreference(SharedPreferences prefs, String name, Long value)}
     *             instead.
     * 
     * @param prefs
     *            A {@link SharedPreferences} to hold the preference.
     * @param name
     *            The name of the shared preference. See {@link Const} for a list of names.
     * @param value
     *            The {@link Long} value to save
     */
    @Deprecated
    public void savePreference(SharedPreferences prefs, String name, Long value) {
        Editor e = prefs.edit();
        e.putLong(name, value);
        e.commit();
    }

    /**
     * Save a preference value into an existing {@link SharedPreferences}
     * 
     * @deprecated - use
     *             {@link com.concur.mobile.platform.ui.common.util.PreferenceUtil#savePreference(SharedPreferences prefs, String name, Integer value)}
     *             instead.
     * @param prefs
     *            A {@link SharedPreferences} to hold the preference.
     * @param name
     *            The name of the shared preference. See {@link Const} for a list of names.
     * @param value
     *            The {@link Integer} value to save
     */
    @Deprecated
    public void savePreference(SharedPreferences prefs, String name, Integer value) {
        Editor e = prefs.edit();
        e.putInt(name, value);
        e.commit();
    }

    /**
     * Save a preference value into an existing {@link SharedPreferences}
     * 
     * @deprecated - use
     *             {@link com.concur.mobile.platform.ui.common.util.PreferenceUtil#savePreference(SharedPreferences prefs, String name, boolean value)}
     *             instead.
     * @param prefs
     *            A {@link SharedPreferences} to hold the preference.
     * @param name
     *            The name of the shared preference. See {@link Const} for a list of names.
     * @param value
     *            The {@link Boolean} value to save
     */
    @Deprecated
    public void savePreference(SharedPreferences prefs, String name, boolean value) {
        Editor e = prefs.edit();
        e.putBoolean(name, value);
        e.commit();
    }

    // /////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////
    // public static final DateFormat LOC_LOG_TS = new
    // SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S '--' ");
    //
    // public static void appendLocLog(String text) {
    //
    // File path = Environment.getExternalStoragePublicDirectory("logs");
    // if (!path.exists()) {
    // path.mkdirs();
    // }
    // File logFile = new File(path, "loc.log");
    // if (!logFile.exists()) {
    // try {
    // logFile.createNewFile();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
    // try {
    // // BufferedWriter for performance, true to set append to file flag
    // BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
    // Calendar now = Calendar.getInstance();
    // buf.append(FormatUtil.safeFormatCalendar(LOC_LOG_TS, now));
    // buf.append(text);
    // buf.newLine();
    // buf.close();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
    // /////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////

    // Our login expiration/activity redirect is convoluted. We need a hook here
    // so that each app can get back to its own way of expiring.
    public abstract void expireLogin();

    /**
     * Executed when the "remote wipe" flag is sent down from a Login/Auto-Login request.
     */
    public void remoteWipe() {
        // no-op
    }

    /**
     * Will examine a responses map from a login response and update any preferences based on the results.
     * 
     * @param sessionId
     *            a session id.
     * @param cm
     *            the application instance.
     * @param responses
     *            a map of response values.
     */
    public static void saveLoginResponsePreferences(String sessionId, ConcurCore cm, Map<String, Object> responses) {

        // Save a number of attributes related to the values in 'responses'.

        String serverUrl = (String) responses.get(Const.LR_SERVER_URL);
        String abTestID = (String) responses.get(Const.LR_ABTEST_ID);
        String abTestExp = (String) responses.get(Const.LR_ABTEST_EXP);
        Integer duration = (Integer) responses.get(Const.LR_SESSION_DURATION);
        Long expire = (Long) responses.get(Const.LR_SESSION_EXPIRATION);
        String userId = (String) responses.get(Const.LR_USER_ID);
        String accessToken = (String) responses.get(Const.LR_ACCESS_TOKEN);
        String userCrnCode = (String) responses.get(Const.LR_USER_CRN_CODE);
        String roles = (String) responses.get(Const.LR_ROLES);
        String entityType = (String) responses.get(Const.LR_ENTITY_TYPE);
        Boolean locationCheckIn = (Boolean) responses.get(Const.LR_SITE_SETTINGS_LOCACTION_CHECK_IN);
        if (locationCheckIn == null)
            locationCheckIn = Boolean.FALSE;

        Boolean cardEditTransDate = (Boolean) responses.get(Const.LR_SITE_SETTINGS_CARD_TRANS_DATE_EDITABLE);
        if (cardEditTransDate == null)
            cardEditTransDate = Boolean.FALSE;

        Boolean cardAllowTransDelete = (Boolean) responses.get(Const.LR_SITE_SETTINGS_CARD_ALLOW_TRANS_DELETE);
        if (cardAllowTransDelete == null)
            cardAllowTransDelete = Boolean.FALSE;

        Boolean mobilePersonalMileageOnHome = (Boolean) responses
                .get(Const.LR_SITE_SETTINGS_MOBILE_PERSONAL_CAR_MILEAGE_ON_HOME);
        if (mobilePersonalMileageOnHome == null)
            mobilePersonalMileageOnHome = Boolean.FALSE;

        Boolean requiredCustomFields = (Boolean) responses.get(Const.LR_REQUIRED_CUSTOM_FIELDS);
        if (requiredCustomFields == null)
            requiredCustomFields = Boolean.FALSE;

        Integer travelProfileStatus = (Integer) responses.get(Const.LR_TRAVEL_PROFILE_STATUS);
        Boolean hideReceiptStore = (Boolean) responses.get(Const.LR_SITE_SETTINGS_HIDE_RECEIPT_STORE);
        if (hideReceiptStore == null)
            hideReceiptStore = Boolean.FALSE;

        Boolean showNonRefundableMessage = (Boolean) responses.get(Const.LR_SITE_SETTINGS_SHOW_NONREFUNDABLE_MESSAGE);
        if (showNonRefundableMessage == null)
            showNonRefundableMessage = Boolean.FALSE;

        Boolean showListCodes = (Boolean) responses.get(Const.LR_SITE_SETTINGS_SHOW_LIST_CODES);
        if (showListCodes == null)
            showListCodes = Boolean.FALSE;

        Boolean allowApprovals = (Boolean) responses.get(Const.LR_SITE_SETTINGS_ALLOW_APPROVALS);
        if (allowApprovals == null)
            allowApprovals = Boolean.FALSE;

        Boolean allowReports = (Boolean) responses.get(Const.LR_SITE_SETTINGS_ALLOW_REPORTS);
        if (allowReports == null)
            allowReports = Boolean.FALSE;

        Boolean allowTravelBooking = (Boolean) responses.get(Const.LR_SITE_SETTINGS_ALLOW_TRAVEL_BOOKING);
        if (allowTravelBooking == null)
            allowTravelBooking = Boolean.FALSE;

        Boolean allowVoiceBooking = (Boolean) responses.get(Const.LR_SITE_SETTINGS_ALLOW_VOICE_BOOKING);
        if (allowVoiceBooking == null)
            allowVoiceBooking = Boolean.FALSE;

        Boolean enableSpdy = (Boolean) responses.get(Const.LR_SITE_SETTINGS_ENABLE_SPDY);
        if (enableSpdy == null)
            enableSpdy = Boolean.FALSE;

        Boolean hasFixedTA = (Boolean) responses.get(Const.LR_SITE_SETTINGS_MOBILE_HAS_FIXED_TA);
        if (hasFixedTA == null)
            hasFixedTA = Boolean.FALSE;

        Boolean enableConditionalFieldEvaluation = (Boolean) responses
                .get(Const.LR_SITE_SETTINGS_ENABLE_CONDITIONAL_FIELD_EVALUATION);
        if (enableConditionalFieldEvaluation == null)
            enableConditionalFieldEvaluation = Boolean.FALSE;

        String nshAgreeString = (String) responses.get(Const.NEED_SAFE_HARBOR_AGREEMENT);
        Boolean needSafeHarborAgreement = Parse.safeParseBoolean(nshAgreeString);
        if (needSafeHarborAgreement == null)
            needSafeHarborAgreement = Boolean.FALSE;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(cm);
        Preferences.setSessionInfo(prefs, accessToken, sessionId, duration, expire);
        if (serverUrl != null) {
            PreferenceUtil.savePreference(prefs, Const.PREF_MWS_ADDRESS, serverUrl);
        }
        PreferenceUtil.savePreference(prefs, Const.PREF_ABTEST_ID, abTestID);
        PreferenceUtil.savePreference(prefs, Const.PREF_ABTEST_EXP, abTestExp);
        PreferenceUtil.savePreference(prefs, Const.PREF_USER_ID, userId);
        PreferenceUtil.savePreference(prefs, Const.PREF_USER_CRN_CODE, userCrnCode);
        PreferenceUtil.savePreference(prefs, Const.PREF_ENTITY_TYPE, entityType);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_CHECK_IN_LOCATION, locationCheckIn);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_EDIT_CARD_TRANS_DATE, cardEditTransDate);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_DELETE_CARD_TRANS, cardAllowTransDelete);
        PreferenceUtil
                .savePreference(prefs, Const.PREF_CAN_SHOW_PERSONAL_CAR_MILEAGE_HOME, mobilePersonalMileageOnHome);
        PreferenceUtil.savePreference(prefs, Const.PREF_RECEIPT_STORE_HIDDEN, hideReceiptStore);
        PreferenceUtil.savePreference(prefs, Const.PREF_SHOW_NONREFUNDABLE_MESSAGE, showNonRefundableMessage);
        PreferenceUtil.savePreference(prefs, Const.PREF_SHOW_LIST_CODES, showListCodes);
        PreferenceUtil.savePreference(prefs, Const.PREF_ALLOW_APPROVALS, allowApprovals);
        PreferenceUtil.savePreference(prefs, Const.PREF_ALLOW_REPORTS, allowReports);
        PreferenceUtil.savePreference(prefs, Const.PREF_ALLOW_TRAVEL_BOOKING, allowTravelBooking);
        PreferenceUtil.savePreference(prefs, Const.PREF_ALLOW_VOICE_BOOKING, allowVoiceBooking);

        PreferenceUtil.savePreference(prefs, Const.PREF_ENABLE_SPDY, enableSpdy);

        PreferenceUtil.savePreference(prefs, Const.PREF_REQUIRED_CUSTOM_FIELDS, requiredCustomFields);
        PreferenceUtil.savePreference(prefs, Const.PREF_TRAVEL_PROFILE_STATUS, travelProfileStatus);

        PreferenceUtil.savePreference(prefs, Const.PREF_HAS_FIXED_TA, hasFixedTA);
        PreferenceUtil.savePreference(prefs, Const.PREF_ALLOW_CONDITIONAL_FIELD_EVALUATION,
                enableConditionalFieldEvaluation);

        // NeedSafeHarborAgreement
        PreferenceUtil.savePreference(prefs, Const.PREF_NSH_AGREE, (needSafeHarborAgreement == null) ? false
                : needSafeHarborAgreement);
        // Clear any in-memory caches.
        cm.clearCaches();

        // Check to see if they can approve
        boolean canApprove = roles.contains(Const.MOBILE_EXPENSE_APPROVER);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_EXPENSE_APPROVE, canApprove);

        // Check to see if they have access to TR
        if (responses.get(Const.LR_PERMISSIONS_TR) != null) {
            boolean hasTR = (Boolean) responses.get(Const.LR_PERMISSIONS_TR);
            PreferenceUtil.savePreference(prefs, Const.PREF_HAS_TR, hasTR);
        }

        // Check to see if they can use Travel Requests
        if (responses.get(Const.LR_PERMISSIONS_TR_USER) != null) {
            boolean canUseTR = (Boolean) responses.get(Const.LR_PERMISSIONS_TR_USER);
            PreferenceUtil.savePreference(prefs, Const.PREF_CAN_TR, canUseTR);
        }

        // Check to see if they can approve Travel Requests
        boolean canTRApprove = roles.contains(Const.MOBILE_TRAVEL_REQUEST_APPROVER);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_TR_APPROVE, canTRApprove);

        // Check to see if they can approve Invoices
        boolean canInvoiceApprove = roles.contains(Const.MOBILE_INVOICE_APPROVER);
        PreferenceUtil.savePreference(prefs, Const.PREF_IS_INVOICE_APPROVER, canInvoiceApprove);

        // Check to see if they can approve Purchase Requests
        boolean canPurchaseRequestApprove = roles.contains(Const.MOBILE_INVOICE_PURCHASE_REQUEST_APPROVER);
        PreferenceUtil.savePreference(prefs, Const.PREF_IS_PURCHASE_REQUEST_APPROVER, canPurchaseRequestApprove);

        // Check to see if they can approve travel
        boolean canTravelApprove = roles.contains(Const.MOBILE_TRAVEL_APPROVER);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_TRAVEL_APPROVE, canTravelApprove);

        // Check to see if they can view and submit Invoices
        boolean canInvoiceSubmit = roles.contains(Const.MOBILE_INVOICE_USER);
        PreferenceUtil.savePreference(prefs, Const.PREF_IS_INVOICE_USER, canInvoiceSubmit);

        // Check to see if they are a traveler.
        boolean canTravel = cm.isTraveler(roles);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_TRAVEL, canTravel);

        // Check to see whether they can book air.
        boolean canBookAir = roles.contains(Const.MOBILE_AIR_USER);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_AIR, canBookAir);

        // Check to see whether the end-user is a flex fare user.
        boolean canFlexFare = roles.contains(Const.MOBILE_FLEX_FARE_USER);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_FLEX_FARE, canFlexFare);

        // Check for itinerary viewer (non-cliqbook company, TMC)
        boolean isItinViewer = roles.contains(Const.MOBILE_ITIN_VIEWER);
        PreferenceUtil.savePreference(prefs, Const.PREF_IS_ITIN_VIEWER, isItinViewer);

        // Check for the expenese user role.
        boolean canExpense = roles.contains(Const.MOBILE_EXPENSE_USER);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_EXPENSE, canExpense);

        // Check for the rail user role.
        boolean canRail = roles.contains(Const.MOBILE_RAIL_USER);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_RAIL, canRail);

        // Check for a dining user role.
        boolean canDine = roles.contains(Const.MOBILE_DINING_USER);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_DINE, canDine);

        // Check for a taxi user role.
        boolean canTaxi = roles.contains(Const.MOBILE_TAXI_USER);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_TAXI, canTaxi);

        // Check for a "tax without card" user.
        boolean canTaxWithOutCard = roles.contains(Const.MOBILE_TAX_WITHOUT_CARD);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_TAX_WITHOUT_CARD, canTaxWithOutCard);

        // Check if we should show the TripIt ads
        boolean showTripItAd = roles.contains(Const.MOBILE_SHOW_TRIPIT_AD);
        PreferenceUtil.savePreference(prefs, Const.PREF_SHOW_TRIPIT_AD, showTripItAd);

        // Check for a LNA user.
        boolean canLNAUser = roles.contains(Const.MOBILE_SHOW_LNA_USER);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_LNA_USER, canLNAUser);

        // Check for a GOV user.
        boolean isGsaUser = roles.contains(Const.MOBILE_GOV_USER);
        PreferenceUtil.savePreference(prefs, Const.PREF_CAN_GOV_USER, isGsaUser);

        // Check for a open booking user.
        boolean isOpenBookingUser = roles.contains(Const.MOBILE_OPEN_BOOKING_USER);
        PreferenceUtil.savePreference(prefs, Const.PREF_SHOW_OPEN_BOOKING, isOpenBookingUser);

        // Show call travel agent?
        boolean showCallTravelAgent = roles.contains(Const.MOBILE_ENABLE_CALL_TRAVEL_AGENT);
        PreferenceUtil.savePreference(prefs, Const.PREF_SHOW_CALL_TRAVEL_AGENT, showCallTravelAgent);

        // Check if we should show the expense it ads
        boolean showExpenseIt = roles.contains(Const.MOBILE_EXPENSEIT_USER);
        PreferenceUtil.savePreference(prefs, Const.PREF_SHOW_EXPENSEIT_AD, showExpenseIt);

        // Check if we should have product offering
        String productOffering = (String) responses.get(Const.LR_PRODUCT_OFFERING);
        PreferenceUtil.savePreference(prefs, Const.PREF_PRODUCT_OFFERING, productOffering);

        // Set Test Drive user expiration date into prefs
        String accountExpirationDate = (String) responses.get(Const.LR_ACCOUNT_EXPIRATION_DATE);
        PreferenceUtil.savePreference(prefs, Const.PREF_ACCOUNT_EXPIRATION_DATE, accountExpirationDate);

        // MOB-16792 - Be sure to update the User ID for all the Flurry
        // Sessions.
        EventTracker.INSTANCE.setUserId(getTrackingUserId());

        // Check for Travel Points User role
        boolean isTravelPointsUser = roles.contains(Const.MOBILE_TRAVEL_POINTS_USER);
        PreferenceUtil.savePreference(prefs, Const.PREF_TRAVEL_POINTS_USER, isTravelPointsUser);

        // Check if we should show Price to Beat in the Menu
        boolean showP2BGenerator = roles.contains(Const.MOBILE_SHOW_P2B_GENERATOR);
        PreferenceUtil.savePreference(prefs, Const.PREF_SHOW_PRICE_TO_BEAT_MENU, showP2BGenerator);

        // MOB-24198 - show Jarvis Hotel UI
        Boolean showHotelJarvisUI = (Boolean) responses.get(Const.LR_SITE_SETTINGS_SHOW_JARVIS_HOTEL_UI);
        if (showHotelJarvisUI == null)
            showHotelJarvisUI = Boolean.FALSE;
        PreferenceUtil.savePreference(prefs, Const.SHOW_JARVIS_HOTEL_UI, showHotelJarvisUI);

    }

    /**
     * Check traveller role for the user.
     * */
    public boolean isTraveler(String roles) {
        boolean canTravel = roles.contains(Const.MOBILE_TRAVELER);
        return canTravel;
    }

    /**
     * Gets whether or not there is a session available.
     * 
     * @return returns whether there is a session available.
     */
    public boolean isSessionAvailable() {
        String sessionId = SessionManager.validateSessionId(this);
        return (sessionId != null && sessionId.length() > 0);
    }

    public void setTaxFormReply(GetTaxFormReply taxFormReply) {
        this.taxFormReply = taxFormReply;

    }

    public GetTaxFormReply getTaxFormReply() {
        return this.taxFormReply;
    }

    // private static void outputTravelProfileStatus(Integer
    // travelProfileStatus) {
    // String profStatStr = null;
    // switch (travelProfileStatus) {
    // case 0: {
    // profStatStr = "Everything is OK.";
    // break;
    // }
    // case 1: {
    // profStatStr =
    // "Missing TSA-required information. TSA-required fields are:\n" + ""
    // + "Middle Name OR No Middle Name (checked), Gender or Date of Birth";
    // break;
    // }
    // case 2: {
    // profStatStr = "Profile Incomplete.";
    // break;
    // }
    // case 20: {
    // profStatStr =
    // "Missing Required Custom fields. The user?????????s profile has one or more required\n"
    // + "custom fields that are not filled out.";
    // break;
    // }
    // case 21: {
    // profStatStr =
    // "Profile has never been saved. The user has never saved their profile\n"
    // +
    // "from the profile page. The profile must be saved once in order to book.";
    // break;
    // }
    // case 22: {
    // profStatStr =
    // "The user?????????s travel configuration billing code and billy type\n"
    // +
    // "requires the existence of projects and the user has no such projects.";
    // break;
    // }
    // case 23: {
    // profStatStr = "Vinnet-related. Not implemented at this time.";
    // break;
    // }
    // case 24: {
    // profStatStr = "The module property Cell Phone required is set to true,\n"
    // + "but the user's cell phone field is blank.";
    // break;
    // }
    // case 25: {
    // profStatStr =
    // "The module property Passport Nationality Required is set to true,\n"
    // + "but the user's passport nationality field is blank.";
    // break;
    // }
    // case 26: {
    // profStatStr = "The module property Job Title Required is set to true,\n"
    // + "but the user's job title field is blank.";
    // break;
    // }
    // case 27: {
    // profStatStr = "The module property Org Unit required is set to true,\n"
    // + "but the user's org unit field is null.";
    // break;
    // }
    // case 28: {
    // profStatStr =
    // "The module property Name-Remark Required is set to true,\n"
    // + "but the Name Remark field is blank.\n"
    // +
    // "NOTE: This field has a customizable label, so it might not be called \'Name Remark\'\n"
    // + "on the profile page.";
    // break;
    // }
    // case 29: {
    // profStatStr = "The module property Manager Required is set to true,\n"
    // + "but the user's Manager ID field is blank/null.";
    // break;
    // }
    // case 30: {
    // profStatStr =
    // "The module property Employee ID Required is set to true,\n"
    // + "but the user's Employee ID field is blank/null.";
    // break;
    // }
    // case 31: {
    // profStatStr =
    // "The module property eMail1 required to save Profile is set to true,\n"
    // + "but the user's Email 1 field is blank/null.";
    // break;
    // }
    // case 32: {
    // profStatStr =
    // "The module property Profile Requires Work Address is set to true,\n"
    // +
    // "but the user is missing his work street address, work city, or work country";
    // break;
    // }
    // case 33: {
    // profStatStr =
    // "The user has neither a work phone number nor a home phone number.\n"
    // + "He needs to have one or the other.";
    // break;
    // }
    // }
    // Log.i(Const.LOG_TAG, CLS_TAG + ".outputTravelProfileStatus: (" +
    // travelProfileStatus + ") "
    // + ((profStatStr != null) ? profStatStr : "unknown."));
    // }

    /**
     * Remove the AsyncTask reference with the activity. Called from Async Task onPostExecute.
     * 
     * @param task
     *            - Async Task to be removed
     */
    public void removeTask(CustomAsyncRequestTask task) {
        for (Entry<String, List<CustomAsyncRequestTask>> entry : mActivityTaskMap.entrySet()) {
            List<CustomAsyncRequestTask> tasks = entry.getValue();
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i) == task) {
                    tasks.remove(i);
                    break;
                }
            }

            if (tasks.size() == 0) {
                mActivityTaskMap.remove(entry.getKey());
                return;
            }
        }
    }

    /**
     * Save the AsyncTask reference with the activity. Called from Async Task onPreExecute
     * 
     * @param activity
     *            - activity instance
     * @param task
     *            - Async Task
     */
    public void addTask(Activity activity, CustomAsyncRequestTask task) {
        String key = activity.getClass().getCanonicalName();
        List<CustomAsyncRequestTask> tasks = mActivityTaskMap.get(key);
        if (tasks == null) {
            tasks = new ArrayList<CustomAsyncRequestTask>();
            mActivityTaskMap.put(key, tasks);
        }

        tasks.add(task);
    }

    /**
     * Lookup all AsyncTasks that have been started on behalf of this Activity and null out their Activity references.
     * 
     * @param activity
     *            - activity instance that is going to be destroyed
     */
    public void detach(Activity activity) {
        List<CustomAsyncRequestTask> tasks = mActivityTaskMap.get(activity.getClass().getCanonicalName());
        if (tasks != null) {
            for (CustomAsyncRequestTask task : tasks) {
                task.setActivity(null);
            }
        }
    }

    /**
     * Notified by the onRestoreInstanceState of the new Activity instance. Set the Activity reference to all of the AsyncTasks
     * that are still running.
     * 
     * @param activity
     *            - new activity instance that is re-created
     */
    public void attach(Activity activity) {
        List<CustomAsyncRequestTask> tasks = mActivityTaskMap.get(activity.getClass().getCanonicalName());
        if (tasks != null) {
            for (CustomAsyncRequestTask task : tasks) {
                task.setActivity(activity);
            }
        }
    }

    /**
     * @return the ipmMsgResults
     */
    public IpmReply getIpmMsgResults() {
        return ipmMsgResults;
    }

    /**
     * @param ipmMsgResults
     *            the ipmMsgResults to set
     */
    public void setIpmMsgResults(IpmReply ipmMsgResults) {
        this.ipmMsgResults = ipmMsgResults;
    }

}
