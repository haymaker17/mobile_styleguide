package com.concur.mobile.core.request.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.request.adapter.EntryListAdapter;
import com.concur.mobile.core.request.task.RequestDetailsTask;
import com.concur.mobile.core.request.task.RequestFormFieldsTask;
import com.concur.mobile.core.request.task.RequestSubmitTask;
import com.concur.mobile.core.request.util.ConnectHelper;
import com.concur.mobile.core.request.util.DateUtil;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.common.formfield.ConnectForm;
import com.concur.mobile.platform.common.formfield.ConnectFormFieldsCache;
import com.concur.mobile.platform.request.RequestListCache;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.dto.RequestEntryDTO;
import com.concur.mobile.platform.request.util.RequestParser;
import com.concur.mobile.platform.ui.common.dialog.NoConnectivityDialogFragment;

import java.util.*;

public class RequestSummaryActivity extends BaseActivity {

    private static final String CLS_TAG = RequestSummaryActivity.class.getSimpleName();
    private final RequestParser requestParser = new RequestParser();

    private static final int ID_LOADING_VIEW = 0;
    private static final int ID_DETAIL_VIEW = 1;

    public static final int HEADER_UPDATE_RESULT = 1;
    private static final int ENTRY_UPDATE_RESULT = 2;

    public static final String REQUEST_IS_EDITABLE = "false";

    /**
     * A reference to the application instance representing the client.
     */
    private ViewFlipper requestSummaryVF;
    private ListView entryListView;
    private RelativeLayout segmentListViewLayout;
    private RelativeLayout submitButton;
    // for Details
    private BaseAsyncResultReceiver asyncReceiver;
    // for Submit
    private BaseAsyncResultReceiver asyncReceiverSubmit;
    private BaseAsyncResultReceiver asyncReceiverFormFields;

    private ConnectFormFieldsCache formFieldsCache = null;
    private String formWaitingForRefresh = null;
    private String entryWaitingForRefresh;

    protected Boolean showCodes;
    protected int category;
    protected int minSearchLength = 1;

    private RequestDTO tr = null;
    private Locale locale = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_summary);
        category = getResources().getColor(R.color.SectionHeaderBackground);

        formFieldsCache = (ConnectFormFieldsCache) getConcurCore().getRequestFormFieldsCache();
        asyncReceiverFormFields = new BaseAsyncResultReceiver(new Handler());
        asyncReceiverFormFields.setListener(new SegmentFormFieldsListener());

        final Bundle bundle = getIntent().getExtras();
        final String requestId = bundle.getString(RequestListActivity.REQUEST_ID);
        if (requestId != null) {
            tr = getConcurCore().getRequestListCache().getValue(requestId);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + " onCreate() : problem on tr retrieved, going back to list activity.");
            // TODO : throw exception & display toast message ? @See with PM
            finish();
        }

        this.locale = this.getResources().getConfiguration().locale != null ?
                this.getResources().getConfiguration().locale :
                Locale.US;

        configureUI();
    }

    private void configureUI() {
        requestSummaryVF = ((ViewFlipper) findViewById(R.id.requestDetailVF));
        requestSummaryVF.setDisplayedChild(ID_LOADING_VIEW);
        entryListView = ((ListView) findViewById(R.id.segmentListView));
        entryListView.setAdapter(new EntryListAdapter(this.getBaseContext(), null, locale));
        entryListView.setOnItemClickListener(new EntryListAdapterRowClickListener());
        segmentListViewLayout = (RelativeLayout) findViewById(R.id.segmentListViewLayout);

        // Set the expense header navigation bar information.
        try {
            final String headerNavBarTitle = getResources().getString(R.string.travel_request_title);
            getSupportActionBar().setTitle(headerNavBarTitle);
        } catch (Resources.NotFoundException resNotFndExc) {
            Log.e(Const.LOG_TAG,
                    CLS_TAG + ".populateExpenseHeaderNavBarInfo: missing navigation bar title text resource!",
                    resNotFndExc);
        }

        submitButton = (RelativeLayout) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // CALL SUMMIT METHOD
                submitRequest();
            }
        });
    }

    private void updateTRDetailsUI(RequestDTO request) {
        final TextView name = (TextView) findViewById(R.id.requestDetailsName);
        final TextView amount = (TextView) findViewById(R.id.requestDetailsAmount);
        final TextView amountInButton = (TextView) findViewById(R.id.requestDetailsAmountInButton);
        final TextView startDate = (TextView) findViewById(R.id.requestDetailsStartDate);
        final TextView business_purpose = (TextView) findViewById(R.id.requestBusinessPurpose);
        final TextView requestComment = (TextView) findViewById(R.id.requestComment);

        final String formattedAmount = FormatUtil
                .formatAmount(request.getTotal(), locale, request.getCurrencyCode(), true, true);

        name.setText(request.getName());
        amount.setText(formattedAmount);
        amountInButton.setText(formattedAmount);
        business_purpose.setText(request.getPurpose());
        requestComment.setText(request.getLastComment());

        startDate.setText(
                DateUtil.getFormattedDateForLocale(DateUtil.DatePattern.SHORT, locale, request.getStartDate()));

        name.setTypeface(Typeface.DEFAULT_BOLD);
        amount.setTypeface(Typeface.DEFAULT_BOLD);

        if (tr.getEntriesMap() != null) {
            //TODO
            ((EntryListAdapter) entryListView.getAdapter())
                    .updateList(new ArrayList<RequestEntryDTO>(tr.getEntriesMap().values()));
            if (tr.getEntriesMap().size() <= 1) {
                final int height = (int) TypedValue
                        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, getResources().getDisplayMetrics());
                segmentListViewLayout.getLayoutParams().height = height;
            }
        }
    }

    /**
     * Call asynchronous task to retrieve data through connect
     */
    private void refreshData(boolean refreshRequired) {
        if (ConcurCore.isConnected()) {
            if (tr != null) {
                requestSummaryVF.setDisplayedChild(ID_LOADING_VIEW);
                Log.d(Const.LOG_TAG, CLS_TAG + " calling increment from refreshList");

                // --- creates the listener
                asyncReceiver.setListener(new TRDetailsListener());
                // --- onRequestResult calls cleanup() on execution, so listener will be destroyed by processing
                new RequestDetailsTask(RequestSummaryActivity.this, 1, asyncReceiver, tr.getId()).execute();
            } else {
                // --- we lost the request object. Should not happen, yet if it does just go back to the list & log it
                Log.e(Const.LOG_TAG, CLS_TAG + " refreshData() : request object lost, going back to list activity.");
                finish();
            }
        } else if (refreshRequired || !showCacheData()) {
            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), CLS_TAG);
        }
    }

    private void submitRequest() {
        if (ConcurCore.isConnected()) {
            // --- creates the listener
            asyncReceiverSubmit.setListener(new TRSubmitListener());
            requestSummaryVF.setDisplayedChild(ID_LOADING_VIEW);
            // --- onRequestResult calls cleanup() on execution, so listener will be destroyed by processing
            new RequestSubmitTask(RequestSummaryActivity.this, 1, asyncReceiverSubmit, tr.getId()).execute();
        } else {
            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), CLS_TAG);
        }
    }

    /**
     * Display cached data
     *
     * @return true if there were cached data available & display were successful (should always be successful)
     */
    private boolean showCacheData() {
        if (tr.getEntriesMap() != null) {
            requestSummaryVF.setDisplayedChild(ID_DETAIL_VIEW);
            tr.setEntriesMap(tr.getEntriesMap());
            updateTRDetailsUI(tr);
            return true;
        } else {
            finish();
        }
        return false;
    }

    // /////////////
    // LISTENERS //
    // /////////////

    /**
     * Handles asynchronous task response (connect ws response)
     */
    public class TRDetailsListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            requestSummaryVF.setDisplayedChild(ID_DETAIL_VIEW);
            requestParser.parseTRDetailResponse(tr, resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
            updateTRDetailsUI(tr);

            // --- using an hashset to ensure uniqueness
            final Set<String> segmentFormIds = new HashSet<String>();
            if (tr.getEntriesMap() != null) {
                /* All segments of an entry have the same form type so we just have to get the first one
                 * (cf Jad 27/01/2015 - 17h16 GMT+1)
                 */
                for (RequestEntryDTO re : tr.getEntriesMap().values()) {
                    if (re.getListSegment() != null && re.getListSegment().size() > 0) {
                        segmentFormIds.add(re.getSegmentFormId());
                    }
                    // --- iterating over unique ids to retrieve formfields
                    formFieldsCache.setFormRefreshStatus(re.getSegmentFormId(), true);
                    new RequestFormFieldsTask(RequestSummaryActivity.this, 1, asyncReceiverFormFields,
                            re.getSegmentFormId(), false).execute();
                }
            }

            // Display submit button?
            submitButton = (RelativeLayout) findViewById(R.id.submitButton);
            if (tr.isActionPermitted(RequestParser.PermittedAction.SUBMIT)) {
                submitButton.setVisibility(View.VISIBLE);
            } else {
                submitButton.setVisibility(View.GONE);
            }

        }

        @Override
        public void onRequestFail(Bundle resultData) {
            ConnectHelper.displayResponseMessage(getApplicationContext(), resultData,
                    getResources().getString(R.string.tr_error_retrieving_detail));
            handleRefreshFail();
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onRequestCancel");
            Log.d(Const.LOG_TAG, " onRequestFail in TRDetailsListener...");
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            ConnectHelper
                    .displayMessage(getApplicationContext(), getResources().getString(R.string.tr_operation_canceled));
            handleRefreshFail();
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onRequestCancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in TRDetailsListener...");
        }

        @Override
        public void cleanup() {
            asyncReceiver.setListener(null);
        }
    }

    /**
     * Handles tap upon an item on the request list
     */
    class EntryListAdapterRowClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final EntryListAdapter adapter = (EntryListAdapter) entryListView.getAdapter();
            final RequestEntryDTO entry = adapter.getItem(position);
            if (entry != null) {
                final String reqId = entry.getId();
                if (reqId != null) {
                    if (entry.getListSegment() != null && entry.getListSegment().size() > 0) {
                        final String segmentFormId = entry.getSegmentFormId();
                        if (!formFieldsCache.isFormBeingRefreshed(segmentFormId)) {
                            displayEntryDetail(reqId);
                        } else {
                            waitFormFieldsCacheRefresh(segmentFormId, entry.getId());
                        }
                    } else {
                        //TODO What to do ? is this case possible ?
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onItemClick: request id is null!");
                }
            }
        }
    }

    private void waitFormFieldsCacheRefresh(String formId, String entryId) {
        // set screen as loading and implement some handling on task success / fail / cancel actions
        formWaitingForRefresh = formId;
        entryWaitingForRefresh = entryId;

        setView(ID_LOADING_VIEW);
    }

    /**
     * Handles the view switch between empty view and list view
     *
     * @param viewID id of the view to display
     */
    private void setView(int viewID) {
        requestSummaryVF.setDisplayedChild(viewID);
    }

    /**
     * Generates RequestDetailsActivity intent
     *
     * @param entryId request object id
     */
    private void displayEntryDetail(String entryId) {
        //final RequestDTO tr = requestListCache.getValue(reqId);
        // --- we go to detail screen even if there is no connection if we have cached detail data
        if (!ConcurCore.isConnected() || tr == null || tr.getEntriesMap() == null) {
            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), CLS_TAG);
        } else {
            final Intent i = new Intent(RequestSummaryActivity.this, RequestEntryActivity.class);
            i.putExtra(RequestListActivity.REQUEST_ID, tr.getId());
            i.putExtra(RequestEntryActivity.ENTRY_ID, entryId);

            // --- Flurry tracking
            i.putExtra(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_TRAVEL_REQUEST_SUMMARY);
            final Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_FROM, Flurry.PARAM_VALUE_TRAVEL_REQUEST_SUMMARY);
            params.put(Flurry.PARAM_NAME_TO, Flurry.PARAM_VALUE_TRAVEL_REQUEST_ENTRY);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_TRAVEL_REQUEST, Flurry.EVENT_NAME_LAUNCH, params);

            cleanupReceivers();
            startActivityForResult(i, ENTRY_UPDATE_RESULT);
        }
    }

    public class TRSubmitListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            ConnectHelper.displayMessage(getApplicationContext(), "REQUEST SUBMITTED");
            ((RequestListCache) getConcurCore().getRequestListCache()).setDirty(true);

            // metrics
            final Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.EVENT_NAME_SUBMIT, tr.getId());
            EventTracker.INSTANCE
                    .track(Flurry.CATEGORY_TRAVEL_REQUEST, Flurry.PARAM_VALUE_TRAVEL_REQUEST_SUMMARY, params);

            // getting back to list screen
            final Intent resIntent = new Intent();
            resIntent.putExtra(RequestHeaderActivity.DO_WS_REFRESH, true);
            setResult(Activity.RESULT_OK, resIntent);

            cleanupReceivers();
            finish();
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            ConnectHelper.displayResponseMessage(getApplicationContext(), resultData,
                    getResources().getString(R.string.tr_error_submit));

            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestfails");
            Log.d(Const.LOG_TAG, " onRequestFail in TRSubmitListener...");
            requestSummaryVF.setDisplayedChild(ID_DETAIL_VIEW);
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            ConnectHelper
                    .displayMessage(getApplicationContext(), getResources().getString(R.string.tr_operation_canceled));
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestcancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in TRSubmitListener...");
            requestSummaryVF.setDisplayedChild(ID_DETAIL_VIEW);
        }

        @Override
        public void cleanup() {
            asyncReceiverSubmit.setListener(null);
        }
    }

    /**
     * Call asynchronous task to retrieve data through connect
     */
    private class SegmentFormFieldsListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            final String formId = resultData.getString(RequestFormFieldsTask.PARAM_FORM_ID);
            formFieldsCache.setFormRefreshStatus(formId, false);
            // --- parse the form received
            final ConnectForm rForm = requestParser
                    .parseFormFieldsResponse(resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
            if (rForm != null)
            // --- add form to cache if there is any
            {
                formFieldsCache.addForm(formId, rForm);
                handleAwaitingRefresh(formId, true);
            } else {
                handleAwaitingRefresh(formId, false);
            }
            // TODO see if there can be a nextPage, and if it's the case handle it
            // => we should never have pagination, as we're supposed to maximise LIMIT to avoid that case
            // (for now at least)
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            final String formId = resultData.getString(RequestFormFieldsTask.PARAM_FORM_ID);
            formFieldsCache.setFormRefreshStatus(formId, false);
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestfails");
            Log.d(Const.LOG_TAG, " onRequestFail in SegmentFormFieldsListener...");
            handleAwaitingRefresh(formId, false);
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            final String formId = resultData.getString(RequestFormFieldsTask.PARAM_FORM_ID);
            formFieldsCache.setFormRefreshStatus(formId, false);
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestcancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in SegmentFormFieldsListener...");
            handleAwaitingRefresh(formId, false);
        }

        @Override
        public void cleanup() {
            //asyncReceiverFormFields.setListener(null);
        }
    }

    private void handleAwaitingRefresh(String formId, boolean isSuccess) {
        if (formWaitingForRefresh != null && formId.equals(formWaitingForRefresh)) {
            if (isSuccess) {
                displayEntryDetail(entryWaitingForRefresh);
            } else {
                setView(ID_DETAIL_VIEW);
                // TODO : display an error message ?
            }
            formWaitingForRefresh = null;
            entryWaitingForRefresh = null;
        }
    }

    private void handleRefreshFail() {
        if (!showCacheData()) {
            Log.d(Const.LOG_TAG, CLS_TAG + " onRequestCancel() : no cache data to display, going back to list.");
            cleanupReceivers();
            startActivity(new Intent(RequestSummaryActivity.this, RequestListActivity.class));
        }
    }

    // //////////
    // STATES //
    // //////////
    @Override
    protected void onResume() {
        super.onResume();

        // DETAILS
        if (asyncReceiver == null) {
            // activity creation
            asyncReceiver = new BaseAsyncResultReceiver(new Handler());
            refreshData(false);
        }

        // SUBMIT
        // activity creation
        if (asyncReceiverSubmit == null) {
            asyncReceiverSubmit = new BaseAsyncResultReceiver(new Handler());
        }

        // F & F
        // activity creation
        if (asyncReceiverFormFields == null) {
            asyncReceiverFormFields = new BaseAsyncResultReceiver(new Handler());
        }
        // activity restoration
        asyncReceiverFormFields.setListener(new SegmentFormFieldsListener());
    }

    private void cleanupReceivers() {
        asyncReceiverFormFields.setListener(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cleanupReceivers();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cleanupReceivers();
    }

    // OVERFLOW MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.request_digest, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        final int itemId = menuItem.getItemId();
        if (itemId == R.id.request_header) {
            if (isServiceAvailable()) {
                if (ConcurCore.isConnected()) {
                    final Intent intent = new Intent(RequestSummaryActivity.this, RequestHeaderActivity.class);
                    intent.putExtra(RequestListActivity.REQUEST_ID, tr.getId());
                    if (tr.isActionPermitted(RequestParser.PermittedAction.SAVE)) {
                        intent.putExtra(RequestSummaryActivity.REQUEST_IS_EDITABLE, Boolean.TRUE.toString());
                    } else {
                        intent.putExtra(RequestSummaryActivity.REQUEST_IS_EDITABLE, Boolean.FALSE.toString());
                    }

                    // --- Flurry tracking
                    intent.putExtra(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_TRAVEL_REQUEST_SUMMARY);
                    final Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_FROM, Flurry.PARAM_VALUE_TRAVEL_REQUEST_SUMMARY);
                    params.put(Flurry.PARAM_NAME_TO, Flurry.PARAM_VALUE_TRAVEL_REQUEST_HEADER);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_TRAVEL_REQUEST, Flurry.EVENT_NAME_LAUNCH, params);

                    cleanupReceivers();
                    startActivityForResult(intent, HEADER_UPDATE_RESULT);
                } else {
                    new NoConnectivityDialogFragment().show(getSupportFragmentManager(), CLS_TAG);
                }
            }
            return true;
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case (ENTRY_UPDATE_RESULT):
        case (HEADER_UPDATE_RESULT): {
            if (resultCode == Activity.RESULT_OK) {
                final Boolean newText = data.getBooleanExtra(RequestHeaderActivity.DO_WS_REFRESH, false);
                if (newText) {
                    refreshData(true);
                } else {
                    refreshData(false);
                }
            }
            break;
        }
        }
    }
}
