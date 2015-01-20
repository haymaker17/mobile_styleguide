package com.concur.mobile.core.request.activity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ViewFlipper;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.request.adapter.SplitRequestListAdapter;
import com.concur.mobile.core.request.service.RequestParser;
import com.concur.mobile.core.request.task.RequestFormFieldsTask;
import com.concur.mobile.core.request.task.RequestGroupConfigurationsTask;
import com.concur.mobile.core.request.task.RequestListTask;
import com.concur.mobile.core.request.util.RequestStatus;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.platform.common.formfield.ConnectForm;
import com.concur.mobile.platform.common.formfield.ConnectFormFieldsCache;
import com.concur.mobile.platform.request.RequestListCache;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.groupConfiguration.RequestGroupConfiguration;
import com.concur.mobile.platform.request.groupConfiguration.RequestGroupConfigurationsContainer;
import com.concur.mobile.platform.ui.common.dialog.NoConnectivityDialogFragment;

/**
 * @author olivierb
 */
public class RequestListActivity extends BaseActivity {

    // TODO : move somewhere else / use something existing
    public enum ListenerOutputType {
        SUCCESS, ERROR, CANCEL
    }

    public static final String KEY_SEARCHED_STATUS = "searchedStatus";
    public static final String REQUEST_ID = "requestId";
    private static final String CLS_TAG = RequestListActivity.class.getSimpleName();
    private static final int ID_LOADING_VIEW = 0;
    private static final int ID_EMPTY_VIEW = 1;
    private static final int ID_LIST_VIEW = 2;

    private static boolean HAS_CONFIGURATION = false;

    private final RequestParser requestParser = new RequestParser();

    private RequestStatus searchedStatus = RequestStatus.PENDING_EBOOKING;

    private ListView requestListView;
    private Button newRequestButton;
    private ViewFlipper requestListVF;
    private BaseAsyncResultReceiver asyncTRListReceiver;
    private BaseAsyncResultReceiver asyncFormFieldsReceiver;
    private BaseAsyncResultReceiver asyncGroupConfigurationReceiver;

    protected Boolean showCodes;
    protected int category;
    protected int minSearchLength = 1;

    private RequestListCache requestListCache = null;
    private ConnectFormFieldsCache formFieldsCache = null;
    private String formWaitingForRefresh = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ConcurCore concurCore = (ConcurCore) getApplication();
        setContentView(R.layout.request_list);

        asyncGroupConfigurationReceiver = new BaseAsyncResultReceiver(new Handler());
        asyncGroupConfigurationReceiver.setListener(new GroupConfigurationListener());

        newRequestButton = ((Button) findViewById(R.id.newRequestButton));
        if (!concurCore.getRequestGroupConfigurationCache().hasCachedValues()) {
            HAS_CONFIGURATION = false;
            new RequestGroupConfigurationsTask(RequestListActivity.this, 1, asyncGroupConfigurationReceiver).execute();
        }
        else{
            HAS_CONFIGURATION = true;
        }
        new RequestListTask(RequestListActivity.this, 1, asyncTRListReceiver, searchedStatus).execute();

        // -------------

        requestListCache = (RequestListCache) concurCore.getRequestListCache();
        formFieldsCache = (ConnectFormFieldsCache) concurCore.getRequestFormFieldsCache();

        category = getResources().getColor(R.color.SectionHeaderBackground);
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(KEY_SEARCHED_STATUS))
            searchedStatus = RequestStatus.valueOf(bundle.getString(KEY_SEARCHED_STATUS));
        else
            // default value
            searchedStatus = RequestStatus.ACTIVE;

        configure();
    }

    private void configure() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // Get components references
        requestListView = ((ListView) findViewById(R.id.requestListView));
        requestListVF = ((ViewFlipper) findViewById(R.id.requestListVF));
        setView(ID_EMPTY_VIEW);

        requestListView.setAdapter(new SplitRequestListAdapter(this.getBaseContext(), null));
        requestListView.setOnItemClickListener(new ListAdapterRowClickListener());

        // Set the expense header navigation bar information.
        try {
            final String headerNavBarTitle = getResources().getString(R.string.travel_request_title);
            getSupportActionBar().setTitle(headerNavBarTitle);
        } catch (Resources.NotFoundException resNotFndExc) {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".populateExpenseHeaderNavBarInfo: missing navigation bar title text resource!", resNotFndExc);
        }

        newRequestButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO - request creation
            }
        });
    }

    /**
     * Handles the view switch between empty view and list view
     * 
     * @param viewID
     *            id of the view to display
     */
    private void setView(int viewID) {
        requestListVF.setDisplayedChild(viewID);
        if (viewID == ID_LOADING_VIEW || !isCreateRequestAvailable())
            newRequestButton.setVisibility(View.GONE);
        else
            newRequestButton.setVisibility(View.VISIBLE);
    }

    /**
     * Handles tap upon an item on the request list
     */
    class ListAdapterRowClickListener implements AdapterView.OnItemClickListener {

        private final String CLS_TAG = RequestListActivity.CLS_TAG + ListAdapterRowClickListener.class.getSimpleName();

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final SplitRequestListAdapter adapter = (SplitRequestListAdapter) requestListView.getAdapter();
            final RequestDTO request = adapter.getItem(position);
            if (request != null) {
                final String reqId = request.getId();
                if (reqId != null) {
                    if (!formFieldsCache.isFormBeingRefreshed(request.getHeaderFormId()))
                        displayTravelRequestDetail(reqId);
                    else
                        waitFormFieldsCacheRefresh(request.getId());
                } else
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onItemClick: request id is null!");
            }
        }
    }

    private void waitFormFieldsCacheRefresh(String formId) {
        setView(ID_LOADING_VIEW);
        // set screen as loading and implement some handling on task success / fail / cancel actions
        formWaitingForRefresh = formId;
    }

    private void handleAwaitingRefresh(String formId, boolean isSuccess) {
        if (formWaitingForRefresh != null && formId.equals(formWaitingForRefresh)) {
            if (isSuccess)
                displayTravelRequestDetail(formWaitingForRefresh);
            formWaitingForRefresh = null;
        }

    }

    /**
     * Generates RequestDetailsActivity intent
     * 
     * @param reqId
     *            request object id
     */
    private void displayTravelRequestDetail(String reqId) {
        final RequestDTO tr = requestListCache.getValue(reqId);
        // --- we go to detail screen even if there is no connection if we have cached detail data
        if (!ConcurCore.isConnected() && (tr == null || tr.getEntriesList() == null)) {
            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), CLS_TAG);
        } else {
            final Intent i = new Intent(RequestListActivity.this, RequestDigestActivity.class);
            i.putExtra(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_HOME);
            i.putExtra(REQUEST_ID, reqId);

            final Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_TRAVEL_REQUEST);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.EVENT_NAME_ACTION, params);
            startActivity(i);
        }
    }

    private void updateListUI(List<RequestDTO> listRequests) {
        if (listRequests == null || listRequests.size() <= 0) {
            setView(ID_EMPTY_VIEW);
        } else {
            setView(ID_LIST_VIEW);
            ((SplitRequestListAdapter) requestListView.getAdapter()).updateList(listRequests);
        }
    }

    /**
     * Call asynchronous task to retrieve data through connect
     */
    private void refreshData() {
        if (ConcurCore.isConnected()) {
            setView(ID_LOADING_VIEW);
            Log.d(Const.LOG_TAG, CLS_TAG + " calling increment from refreshList");
            new RequestListTask(RequestListActivity.this, 1, asyncTRListReceiver, searchedStatus).execute();
        } else {
            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), CLS_TAG);
        }
    }

    /**
     * Call asynchronous task to retrieve data through connect
     */
    private class TRListListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            // converts the result string to a list of TravelRequestDTO
            final List<RequestDTO> listRequests = requestParser.parseTRListResponse(resultData
                    .getString(BaseAsyncRequestTask.HTTP_RESPONSE));
            final Set<String> headerFormIds = new HashSet<String>();
            if (requestListCache.hasCachedValues())
                requestListCache.clear();
            for (RequestDTO trDTO : listRequests) {
                // --- cache refresh
                requestListCache.addValue(trDTO);
                // --- hashset will handle duplicates
                headerFormIds.add(trDTO.getHeaderFormId());
            }
            /*
             * WS call to get forms & fields data with the set of ids TBC : using a queue isn't necessary as user mostly use a
             * tiny number of different forms, and anyway they usually have at most 5 requests at the same time.
             */
            for (String formId : headerFormIds) {
                formFieldsCache.setFormRefreshStatus(formId, true);
                new RequestFormFieldsTask(RequestListActivity.this, 2, asyncFormFieldsReceiver, formId, true).execute();
            }

            // update the approvals list with the trips needing approval
            updateListUI(listRequests);
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            if (requestListView.getAdapter().isEmpty())
                handleRefreshFail();
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestfails");
            Log.d(Const.LOG_TAG, " onRequestFail in TRListListener...");
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            if (requestListView.getAdapter().isEmpty())
                handleRefreshFail();
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestcancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in TRListListener...");
        }

        @Override
        public void cleanup() {
            asyncTRListReceiver.setListener(null);
        }
    }

    /**
     * Call asynchronous task to retrieve data through connect
     */
    private class FormFieldsListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            final String formId = resultData.getString(RequestFormFieldsTask.PARAM_FORM_ID);
            formFieldsCache.setFormRefreshStatus(formId, false);
            // --- parse the form received
            final ConnectForm rForm = requestParser.parseFormFieldsResponse(resultData
                    .getString(BaseAsyncRequestTask.HTTP_RESPONSE));
            if (rForm != null)
                // --- add form to cache if there is any
                formFieldsCache.addForm(formId, rForm);
            // TODO see if there can be a nextPage, and if it's the case handle it
            handleAwaitingRefresh(formId, true);
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            final String formId = resultData.getString(RequestFormFieldsTask.PARAM_FORM_ID);
            formFieldsCache.setFormRefreshStatus(formId, false);
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestfails");
            Log.d(Const.LOG_TAG, " onRequestFail in FormFieldsListener...");
            handleAwaitingRefresh(formId, false);
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            final String formId = resultData.getString(RequestFormFieldsTask.PARAM_FORM_ID);
            formFieldsCache.setFormRefreshStatus(formId, false);
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestcancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in FormFieldsListener...");
            handleAwaitingRefresh(formId, false);
        }

        @Override
        public void cleanup() {
            asyncFormFieldsReceiver.setListener(null);
        }
    }

    /**
     * Call asynchronous task to retrieve data through connect
     */
    private class GroupConfigurationListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            // --- parse the configurations received
            final RequestGroupConfigurationsContainer rgcc = requestParser.parseRequestGroupConfigurationsResponse(resultData
                    .getString(BaseAsyncRequestTask.HTTP_RESPONSE));
            if (rgcc != null && rgcc.getGroupConfigurations().size() > 0) {
                // --- add configurations to cache if there is any
                final String userId = getUserId();
                for (RequestGroupConfiguration rgc : rgcc.getGroupConfigurations()){
                    getConcurCore().getRequestGroupConfigurationCache().addValue(userId, rgc);
                }
                // --- New Request button is set to visible only if we obtained a configuration (contains segment types) and it has a default policy
                HAS_CONFIGURATION = true;
                if (isCreateRequestAvailable()) {
                    newRequestButton.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestfails");
            Log.d(Const.LOG_TAG, " onRequestFail in GroupConfigurationListener...");
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestcancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in GroupConfigurationListener...");
        }

        @Override
        public void cleanup() {
            asyncGroupConfigurationReceiver.setListener(null);
        }
    }

    /**
     * Creation is available only if the user has a configuration in which a DefaultRequestPolicy specified.
     * @return true / false
     */
    private boolean isCreateRequestAvailable(){
        if (HAS_CONFIGURATION){
            final RequestGroupConfiguration rgc = getConcurCore().getRequestGroupConfigurationCache().getValue(getUserId());
            if (rgc.getDefaultPolicyId() != null){
                return true;
            }
        }
        return false;
    }

    // TODO : see with PM if we should display cache or an empty list
    private void handleRefreshFail() {
        if (requestListCache.hasCachedValues()) {
            updateListUI((List<RequestDTO>) requestListCache.getValues());
        } else {
            updateListUI(null);
        }
    }

    @Override
    protected void onResume() {
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
            asyncTRListReceiver = new BaseAsyncResultReceiver(new Handler());
            asyncTRListReceiver.setListener(new TRListListener());
            refreshData();
        } else {
            // activity restoration
            asyncTRListReceiver.setListener(new TRListListener());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        asyncTRListReceiver.setListener(null);
        asyncFormFieldsReceiver.setListener(null);
        asyncGroupConfigurationReceiver.setListener(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            asyncTRListReceiver.setListener(null);
            asyncFormFieldsReceiver.setListener(null);
            NavUtils.navigateUpFromSameTask(this);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        asyncTRListReceiver.setListener(null);
        asyncFormFieldsReceiver.setListener(null);
        asyncGroupConfigurationReceiver.setListener(null);
    }

}
