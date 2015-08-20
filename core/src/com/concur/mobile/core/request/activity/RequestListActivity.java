package com.concur.mobile.core.request.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.request.adapter.SortedRequestListAdapter;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.platform.common.formfield.ConnectForm;
import com.concur.mobile.platform.common.formfield.ConnectFormFieldsCache;
import com.concur.mobile.platform.request.RequestGroupConfigurationCache;
import com.concur.mobile.platform.request.RequestListCache;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.groupConfiguration.RequestGroupConfiguration;
import com.concur.mobile.platform.request.task.RequestTask;
import com.concur.mobile.platform.request.util.ConnectHelper;
import com.concur.mobile.platform.request.util.RequestParser;
import com.concur.mobile.platform.request.util.RequestStatus;
import com.concur.mobile.platform.ui.common.dialog.NoConnectivityDialogFragment;
import com.concur.mobile.platform.ui.common.util.RowSwipeGestureListener;
import com.getbase.floatingactionbutton.AddFloatingActionButton;

import java.util.*;

/**
 * @author olivierb
 */
public class RequestListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String KEY_SEARCHED_STATUS = "searchedStatus";
    public static final String REQUEST_ID = "requestId";

    public static final boolean REQUEST_LIST_SWIPE_TO_LEFT = true;

    private static final String CLS_TAG = RequestListActivity.class.getSimpleName();
    private static final int ID_LOADING_VIEW = 0;
    private static final int ID_EMPTY_VIEW = 1;
    private static final int ID_LIST_VIEW = 2;
    private static final int SUMMARY_RESULT = 1;
    private static final int HEADER_RESULT = 2;
    private static boolean HAS_CONFIGURATION = false;

    protected int category;

    private final RequestParser requestParser = new RequestParser();
    private RequestStatus searchedStatus = RequestStatus.PENDING_EBOOKING;

    private ListView requestListView;
    private AddFloatingActionButton newRequestButton;
    private ViewFlipper requestListVF;
    private TextView newRequestText;
    private SwipeRefreshLayout swipeLayout;

    private BaseAsyncResultReceiver asyncTRListReceiver;
    private BaseAsyncResultReceiver asyncFormFieldsReceiver;
    private BaseAsyncResultReceiver asyncGroupConfigurationReceiver;
    private BaseAsyncResultReceiver asyncRequestActionReceiver;

    private RequestGroupConfigurationCache groupConfigurationCache = null;
    private RequestListCache requestListCache = null;
    private ConnectFormFieldsCache formFieldsCache = null;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ConcurCore concurCore = (ConcurCore) getApplication();
        setContentView(R.layout.request_list);

        asyncGroupConfigurationReceiver = new BaseAsyncResultReceiver(new Handler());
        asyncFormFieldsReceiver = new BaseAsyncResultReceiver((new Handler()));

        // --- Group Configuration
        if (!concurCore.getRequestGroupConfigurationCache().hasCachedValues()) {
            HAS_CONFIGURATION = false;
            asyncGroupConfigurationReceiver.setListener(new GroupConfigurationListener());
            new RequestTask(RequestListActivity.this, 1, asyncGroupConfigurationReceiver,
                    ConnectHelper.ConnectVersion.VERSION_3_1, ConnectHelper.Module.GROUP_CONFIGURATIONS,
                    ConnectHelper.Action.LIST, null).execute();
        } else {
            HAS_CONFIGURATION = true;
            asyncFormFieldsReceiver.setListener(new FormFieldsListener());
            new RequestTask(RequestListActivity.this, 2, asyncFormFieldsReceiver,
                    ConnectHelper.ConnectVersion.VERSION_3_1, ConnectHelper.Module.FORM_FIELDS,
                    ConnectHelper.Action.LIST, null).execute();
        }

        // -------------

        groupConfigurationCache = (RequestGroupConfigurationCache) getConcurCore().getRequestGroupConfigurationCache();
        requestListCache = (RequestListCache) concurCore.getRequestListCache();
        formFieldsCache = (ConnectFormFieldsCache) concurCore.getRequestFormFieldsCache();

        category = getResources().getColor(R.color.SectionHeaderBackground);
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(KEY_SEARCHED_STATUS)) {
            searchedStatus = RequestStatus.valueOf(bundle.getString(KEY_SEARCHED_STATUS));
        } else {
            // default value
            searchedStatus = RequestStatus.ACTIVE;
        }

        configureUI();

        // Flurry Notification
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_TRAVEL_REQUEST_LIST);
        EventTracker.INSTANCE.track(Flurry.CATEGORY_TRAVEL_REQUEST, Flurry.EVENT_NAME_LAUNCH, params);

    }

    private void configureUI() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Get components references
        requestListView = ((ListView) findViewById(R.id.requestListView));
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        requestListVF = ((ViewFlipper) findViewById(R.id.requestListVF));
        newRequestButton = ((AddFloatingActionButton) findViewById(R.id.newRequestButton));
        newRequestText = ((TextView) findViewById(R.id.textNewRequest));
        setView(ID_EMPTY_VIEW);

        /** Horizontal Swipe implementation */
        // --- Create a gesture detector
        final GestureDetector gestureDetector = new GestureDetector(this,
                new RowSwipeGestureListener<RequestDTO>(requestListView, REQUEST_LIST_SWIPE_TO_LEFT) {

                    /**
                     * @see RowSwipeGestureListener#onRowTap(Object)
                     */
                    @Override public boolean onRowTap(RequestDTO request) {

                        // Flurry Notification
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_TRAVEL_REQUEST_LIST_ROW_TAP_ACTION);
                        EventTracker.INSTANCE.track(Flurry.CATEGORY_TRAVEL_REQUEST, Flurry.EVENT_NAME_ACTION, params);

                        if (request != null) {
                            final String reqId = request.getId();
                            if (reqId != null) {
                                displayTravelRequestDetail(reqId);
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onItemClick: request id is null!");
                            }
                        }
                        return false;
                    }

                    /**
                     * @see RowSwipeGestureListener#onButtonTap(Object, View)
                     */
                    @Override public boolean onButtonTap(RequestDTO request, View view) {
                        if (view != null && view.getId() == R.id.recallButton) {
                            // --- execute recall action
                            Log.d(CLS_TAG, "--- Request RECALL action (list)");

                            // Flurry Notification
                            Map<String, String> params = new HashMap<String, String>();
                            params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_TRAVEL_REQUEST_LIST_RECALL_ACTION);
                            EventTracker.INSTANCE
                                    .track(Flurry.CATEGORY_TRAVEL_REQUEST, Flurry.EVENT_NAME_ACTION, params);

                            recallRequestAction(request);
                        }
                        return true;
                    }

                    /**
                     * @see RowSwipeGestureListener#isRowSwipeable(Object)
                     */
                    @Override public boolean isRowSwipeable(RequestDTO tr) {
                        return tr != null && tr.isActionPermitted(RequestParser.PermittedAction.RECALL);
                    }
                });
        // --- Apply it on the ListView
        requestListView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        /************************************************/

        // --- Apply an adapter using SwipeableRowView(s)
        requestListView.setAdapter(new SortedRequestListAdapter(this.getBaseContext(), null));

        // Set up the swipe to refresh.
        swipeLayout.setOnRefreshListener(this);
        //swipeLayout.setColorSchemeResources(R.color.MaterialConcurBlue);

        // Set the expense header navigation bar information.
        try {
            final String headerNavBarTitle = getResources().getString(R.string.travel_request_title);
            getSupportActionBar().setTitle(headerNavBarTitle);
        } catch (Resources.NotFoundException resNotFndExc) {
            Log.e(Const.LOG_TAG,
                    CLS_TAG + ".populateExpenseHeaderNavBarInfo: missing navigation bar title text resource!",
                    resNotFndExc);
        }

        newRequestButton.setOnClickListener(new OnClickListener() {

            @Override public void onClick(View v) {
                // --- reminder: button is displayed only if a configuration with a default policy exists
                final String formId = groupConfigurationCache.getValue(getUserId()).getFormId();

                // --- Flurry tracking
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_TRAVEL_REQUEST_LIST_PLUS_BUTTON);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_TRAVEL_REQUEST, Flurry.EVENT_NAME_ACTION, params);

                displayTravelRequestEdit(null);
            }
        });
    }

    /**
     * Handles the view switch between empty view and list view
     *
     * @param viewID id of the view to display
     */
    private void setView(int viewID) {

        boolean isCreateTRAvailable = isCreateRequestAvailable();
        requestListVF.setDisplayedChild(viewID);

        if (viewID == ID_EMPTY_VIEW) {
            if (isCreateTRAvailable) {
                newRequestText.setText(getResources().getString(R.string.tr_new_request_desc));
                newRequestButton.setVisibility(View.VISIBLE);
            } else {
                newRequestText.setText(getResources().getString(R.string.tr_new_request_desc_no_create));
                newRequestButton.setVisibility(View.GONE);
            }
        }

        if (viewID == ID_LOADING_VIEW || !isCreateTRAvailable) {
            newRequestButton.setVisibility(View.GONE);
        } else {
            newRequestButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Generates RequestDetailsActivity intent
     *
     * @param reqId request object id
     */
    private void displayTravelRequestDetail(String reqId) {
        final RequestDTO tr = requestListCache.getValue(reqId);
        // --- we go to detail screen even if there is no connection if we have cached detail data
        if (!ConcurCore.isConnected() && (tr == null || tr.getEntriesMap() == null)) {
            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), CLS_TAG);
        } else {
            /* FIXME
            final Intent i = new Intent(RequestListActivity.this, RequestSummaryActivity.class);
            i.putExtra(RequestListActivity.REQUEST_ID, reqId);

            // --- Flurry tracking
            i.putExtra(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_TRAVEL_REQUEST_LIST);
            final Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_FROM, Flurry.PARAM_VALUE_TRAVEL_REQUEST_LIST);
            params.put(Flurry.PARAM_NAME_TO, Flurry.PARAM_VALUE_TRAVEL_REQUEST_SUMMARY);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_TRAVEL_REQUEST, Flurry.EVENT_NAME_LAUNCH, params);

            startActivityForResult(i, SUMMARY_RESULT);*/
        }
    }

    private void displayTravelRequestEdit(String reqId) {
        if (HAS_CONFIGURATION) {
            final Intent i = new Intent(RequestListActivity.this, RequestEditActivity.class);
            i.putExtra(RequestListActivity.REQUEST_ID, reqId);
            i.putExtra(RequestEditActivity.REQUEST_IS_EDITABLE, Boolean.TRUE.toString());
            i.putExtra(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_TRAVEL_REQUEST_LIST);

            startActivityForResult(i, HEADER_RESULT);
        }
    }

    private void updateListUI(List<RequestDTO> listRequests) {
        if (listRequests == null || listRequests.size() <= 0) {
            setView(ID_EMPTY_VIEW);
        } else {
            setView(ID_LIST_VIEW);
            ((SortedRequestListAdapter) requestListView.getAdapter()).updateList(listRequests);
        }
        swipeLayout.setRefreshing(false);
    }

    /**
     * Call asynchronous task to retrieve data through connect
     */
    private void refreshData(boolean refreshRequired) {
        if (ConcurCore.isConnected()) {
            setView(ID_LOADING_VIEW);
            Log.d(Const.LOG_TAG, CLS_TAG + " calling increment from refreshList");
            // --- creates the listener
            if (asyncTRListReceiver == null) {
                // activity creation
                asyncTRListReceiver = new BaseAsyncResultReceiver(new Handler());
            }
            asyncTRListReceiver.setListener(new TRListListener());
            // --- onRequestResult calls cleanup() on execution, so listener will be destroyed by processing
            new RequestTask(RequestListActivity.this, 1, asyncTRListReceiver, ConnectHelper.Action.LIST, null)
                    .addUrlParameter(RequestTask.P_REQUESTS_STATUS, searchedStatus.toString())
                    .addUrlParameter(RequestTask.P_REQUESTS_WITH_SEG_TYPES, Boolean.TRUE.toString())
                    .addUrlParameter(RequestTask.P_REQUESTS_WITH_USER_PERMISSIONS, Boolean.TRUE.toString())
                    .addUrlParameter(ConnectHelper.PARAM_LIMIT, "100").execute();
        } else if (refreshRequired) {
            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), CLS_TAG);
        } else {
            setView(ID_LIST_VIEW);
        }
    }

    private void recallRequestAction(RequestDTO tr) {
        if (ConcurCore.isConnected()) {
            setView(ID_LOADING_VIEW);
            // --- creates the listener
            asyncRequestActionReceiver.setListener(new TRActionListener());
            // --- onRequestResult calls cleanup() on execution, so listener will be destroyed by processing
            new RequestTask(RequestListActivity.this, 1, asyncRequestActionReceiver, ConnectHelper.Action.RECALL,
                    tr.getId()).addResultData(RequestTask.P_REQUEST_ID, tr.getId()).execute();
        } else {
            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), CLS_TAG);
        }
    }

    /**
     * Creation is available only if the user has a configuration in which a DefaultRequestPolicy specified.
     *
     * @return true / false
     */
    private boolean isCreateRequestAvailable() {
        if (HAS_CONFIGURATION) {
            final RequestGroupConfiguration rgc = getConcurCore().getRequestGroupConfigurationCache()
                    .getValue(getUserId());
            if (rgc != null && rgc.getDefaultPolicyId() != null) {
                return true;
            }
        }
        return false;
    }

    private void handleRefreshFail() {
        if (requestListCache.hasCachedValues()) {
            updateListUI(new ArrayList<RequestDTO>(requestListCache.getValues()));
        } else {
            updateListUI(null);
        }
    }

    @Override protected void onResume() {
        super.onResume();
        // --- Request Group Configuration
        if (asyncGroupConfigurationReceiver == null) {
            asyncGroupConfigurationReceiver = new BaseAsyncResultReceiver(new Handler());
        }
        asyncGroupConfigurationReceiver.setListener(new GroupConfigurationListener());

        // --- Forms & fields (called by list refresh)
        if (asyncFormFieldsReceiver == null) {
            asyncFormFieldsReceiver = new BaseAsyncResultReceiver(new Handler());
        }
        asyncFormFieldsReceiver.setListener(new FormFieldsListener());

        // --- List refresh
        if (asyncTRListReceiver == null) {
            // activity creation
            refreshData(false);
        } else if (requestListCache.isDirty()) {
            // --- refresh view offline
            updateListUI(new ArrayList<RequestDTO>(requestListCache.getValues()));
            requestListCache.setDirty(false);
        }

        // RECALL
        // activity creation
        if (asyncRequestActionReceiver == null) {
            asyncRequestActionReceiver = new BaseAsyncResultReceiver(new Handler());
        }
    }

    @Override protected void onPause() {
        super.onPause();
        cleanupReceivers();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void cleanupReceivers() {
        asyncFormFieldsReceiver.setListener(null);
    }

    @Override public void onRefresh() {
        // If we are offline, show a toast and dismiss the refresh spinner.
        if (!ConcurCore.isConnected()) {
            swipeLayout.setRefreshing(false);
            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), CLS_TAG);
        } else {
            refreshData(true);
        }
    }

    /**
     * Call asynchronous task to retrieve data through connect
     */
    private class TRListListener implements AsyncReplyListener {

        @Override public void onRequestSuccess(Bundle resultData) {
            // converts the result string to a list of TravelRequestDTO
            final List<RequestDTO> listRequests = requestParser
                    .parseTRListResponse(resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));

            // --- using an hashset to ensure uniqueness
            final Set<String> headerFormIds = new HashSet<String>();
            if (requestListCache.hasCachedValues()) {
                requestListCache.clear();
            }
            for (RequestDTO trDTO : listRequests) {
                // --- cache refresh
                requestListCache.addValue(trDTO);
                // --- hashset will handle duplicates
                headerFormIds.add(trDTO.getHeaderFormId());
            }

            // update the approvals list with the trips needing approval
            updateListUI(listRequests);
        }

        @Override public void onRequestFail(Bundle resultData) {
            if (requestListView.getAdapter().isEmpty()) {
                handleRefreshFail();
            }
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestfails");
            Log.d(Const.LOG_TAG, " onRequestFail in TRListListener...");
        }

        @Override public void onRequestCancel(Bundle resultData) {
            if (requestListView.getAdapter().isEmpty()) {
                handleRefreshFail();
            }
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestcancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in TRListListener...");
        }

        @Override public void cleanup() {
            asyncTRListReceiver.setListener(null);
        }
    }

    /**
     * Call asynchronous task to retrieve data through connect
     */
    private class FormFieldsListener implements AsyncReplyListener {

        @Override public void onRequestSuccess(Bundle resultData) {
            final String formId = resultData.getString(RequestTask.P_FORM_ID);
            // --- parse the form received
            final List<ConnectForm> lForms = requestParser
                    .parseFormFieldsResponse(resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
            formFieldsCache.addForms(lForms);
        }

        @Override public void onRequestFail(Bundle resultData) {
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestfails");
            Log.d(Const.LOG_TAG, " onRequestFail in FormFieldsListener...");
        }

        @Override public void onRequestCancel(Bundle resultData) {
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestcancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in FormFieldsListener...");
        }

        @Override public void cleanup() {
            asyncFormFieldsReceiver.setListener(null);
        }
    }

    /**
     * Call asynchronous task to retrieve data through connect
     */
    private class GroupConfigurationListener implements AsyncReplyListener {

        @Override public void onRequestSuccess(Bundle resultData) {
            // --- parse the configurations received
            final List<RequestGroupConfiguration> rgcc = requestParser
                    .parseRequestGroupConfigurationsResponse(resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
            if (rgcc != null && rgcc.size() > 0) {
                // --- add configurations to cache if there is any
                final String userId = getUserId();
                for (RequestGroupConfiguration rgc : rgcc) {
                    groupConfigurationCache.addValue(userId, rgc);
                }
                // --- New Request button is set to visible only if we obtained a configuration (contains segment types) and it has a default policy
                HAS_CONFIGURATION = true;
                // --- Forms & Fields
                asyncFormFieldsReceiver.setListener(new FormFieldsListener());
                new RequestTask(RequestListActivity.this, 2, asyncFormFieldsReceiver,
                        ConnectHelper.ConnectVersion.VERSION_3_1, ConnectHelper.Module.FORM_FIELDS,
                        ConnectHelper.Action.LIST, null).execute();
            }
        }

        @Override public void onRequestFail(Bundle resultData) {
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestfails");
            Log.d(Const.LOG_TAG, " onRequestFail in GroupConfigurationListener...");
        }

        @Override public void onRequestCancel(Bundle resultData) {
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestcancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in GroupConfigurationListener...");
        }

        @Override public void cleanup() {
            asyncGroupConfigurationReceiver.setListener(null);
        }
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case (HEADER_RESULT):
        case (SUMMARY_RESULT):
            if (data != null) {
                // --- no result means we came from a request creation, so we need a refresh
                final Boolean doWSCall = data.getBooleanExtra(RequestEditActivity.DO_WS_REFRESH, true);
                if (doWSCall) {
                    refreshData(true);
                } else {
                    refreshData(false);
                }
            } else {
                refreshData(false);
            }
            break;
        }
    }

    public class TRActionListener implements AsyncReplyListener {

        @Override public void onRequestSuccess(Bundle resultData) {
            ((RequestListCache) getConcurCore().getRequestListCache()).setDirty(true);

            final String trId = resultData.getString(RequestTask.P_REQUEST_ID);

            // metrics
            final Map<String, String> params = new HashMap<String, String>();
            params.put("Request Recall", trId);
            EventTracker.INSTANCE
                    .track(Flurry.CATEGORY_TRAVEL_REQUEST, Flurry.PARAM_VALUE_TRAVEL_REQUEST_SUMMARY, params);

            // refreshing view
            refreshData(true);
        }

        @Override public void onRequestFail(Bundle resultData) {
            ConnectHelper.displayResponseMessage(getApplicationContext(), resultData,
                    getResources().getString(R.string.tr_error_recall));

            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestfails");
            Log.d(Const.LOG_TAG, " onRequestFail in TRActionListener...");
            setView(ID_LIST_VIEW);
        }

        @Override public void onRequestCancel(Bundle resultData) {
            ConnectHelper
                    .displayMessage(getApplicationContext(), getResources().getString(R.string.tr_operation_canceled));
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onRequestCancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in TRActionListener...");
            setView(ID_LIST_VIEW);
        }

        @Override public void cleanup() {
            asyncRequestActionReceiver.setListener(null);
        }
    }
}
