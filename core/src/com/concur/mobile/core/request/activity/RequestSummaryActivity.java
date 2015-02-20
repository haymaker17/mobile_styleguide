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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

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
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.dto.RequestEntryDTO;
import com.concur.mobile.platform.request.groupConfiguration.Policy;
import com.concur.mobile.platform.request.groupConfiguration.RequestGroupConfiguration;
import com.concur.mobile.platform.request.groupConfiguration.SegmentType;
import com.concur.mobile.platform.request.util.RequestParser;
import com.concur.mobile.platform.ui.common.dialog.NoConnectivityDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
    private SegmentType.RequestSegmentType entryWaitingForRefreshType;

    protected Boolean showCodes;
    protected int category;
    protected int minSearchLength = 1;

    private RequestDTO tr = null;
    private Locale locale = null;

    boolean isSegmentsON = false;

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

    private void processEntryCreationByType(SegmentType.RequestSegmentType segmentType) {
        final String formId = getConcurCore().getRequestGroupConfigurationCache().getValue(getUserId()).extractSegmentDefaultFormId(segmentType);
        waitFormFieldsCacheRefresh(formId, null, segmentType);
        formFieldsCache.setFormRefreshStatus(formId, true);
        new RequestFormFieldsTask(RequestSummaryActivity.this, 1, asyncReceiverFormFields,
                formId, false).execute();
    }

    public void onClickAir(View view) {
        TextView tv = (TextView) view.findViewById(R.id.AirSummarySegmentText);
        System.out.println(tv.getText());
        processEntryCreationByType(SegmentType.RequestSegmentType.AIR);
    }

    public void onClickTrain(View view) {
        TextView tv = (TextView) view.findViewById(R.id.TrainSummarySegmentText);
        System.out.println(tv.getText());
        processEntryCreationByType(SegmentType.RequestSegmentType.RAIL);
    }

    public void onClickHotel(View view) {
        TextView tv = (TextView) view.findViewById(R.id.HotelSummarySegmentText);
        System.out.println(tv.getText());
        processEntryCreationByType(SegmentType.RequestSegmentType.HOTEL);
    }

    public void onClickCar(View view) {
        TextView tv = (TextView) view.findViewById(R.id.CarSummarySegmentText);
        System.out.println(tv.getText());
        processEntryCreationByType(SegmentType.RequestSegmentType.CAR);
    }

    public void onClickAddItem(View view) {

        TextView tv = (TextView) view.findViewById(R.id.addItemRequest);
        if (isSegmentsON) {
            isSegmentsON = false;
            tv.setText(R.string.add_another_item_to_request);
        } else {
            isSegmentsON = true;
            tv.setText(R.string.add_another_item_to_request_OFF);
        }

        showCacheData();
    }

    private void updateTRDetailsUI(RequestDTO request) {

        boolean isSegmentAIR = false;
        boolean isSegmentTRAIN = false;
        boolean isSegmentHOTEL = false;
        boolean isSegmentCAR = false;

        RequestGroupConfiguration rgc = getConcurCore().getRequestGroupConfigurationCache().getValue(getUserId());
        for (Policy p : rgc.getPolicies()) {
            if (p.getIsDefault()) {
                for (SegmentType st : p.getSegmentTypes()) {
                    String iconCode = st.getIconCode();
                    if (SegmentType.RequestSegmentType.AIR.getCode().equals(iconCode))
                        isSegmentAIR = true;
                    else if (SegmentType.RequestSegmentType.RAIL.getCode().equals(iconCode))
                        isSegmentTRAIN = true;
                    else if (SegmentType.RequestSegmentType.HOTEL.getCode().equals(iconCode))
                        isSegmentHOTEL = true;
                    else if (SegmentType.RequestSegmentType.CAR.getCode().equals(iconCode))
                        isSegmentCAR = true;
                }
            }
        }

        //Show ADD SEGMENTS
        RelativeLayout layoutSegmentAIR = (RelativeLayout) findViewById(R.id.AirSegmentChoice);
        RelativeLayout layoutSegmentTRAIN = (RelativeLayout) findViewById(R.id.TrainSegmentChoice);
        RelativeLayout layoutSegmentHOTEL = (RelativeLayout) findViewById(R.id.HotelSegmentChoice);
        RelativeLayout layoutSegmentCAR = (RelativeLayout) findViewById(R.id.CarSegmentChoice);
        View separator = (View) findViewById(R.id.SegmentsSeparator);

        layoutSegmentAIR.setVisibility(View.GONE);
        layoutSegmentTRAIN.setVisibility(View.GONE);
        layoutSegmentHOTEL.setVisibility(View.GONE);
        layoutSegmentCAR.setVisibility(View.GONE);
        separator.setVisibility(View.GONE);
        if (isSegmentsON) {
            if (isSegmentAIR) layoutSegmentAIR.setVisibility(View.VISIBLE);
            if (isSegmentTRAIN) layoutSegmentTRAIN.setVisibility(View.VISIBLE);
            if (isSegmentHOTEL) layoutSegmentHOTEL.setVisibility(View.VISIBLE);
            if (isSegmentCAR) layoutSegmentCAR.setVisibility(View.VISIBLE);
            separator.setVisibility(View.VISIBLE);
        }


        //SUMMARY
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
            // --- process all forms contained in this request
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
                            displayEntryDetail(reqId, null);
                        } else {
                            waitFormFieldsCacheRefresh(segmentFormId, entry.getId(), null);
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

    private void waitFormFieldsCacheRefresh(String formId, String entryId, SegmentType.RequestSegmentType segmentType) {
        // set screen as loading and implement some handling on task success / fail / cancel actions
        formWaitingForRefresh = formId;
        entryWaitingForRefresh = entryId;
        entryWaitingForRefreshType = segmentType;

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

    private void displayEntryDetail(String entryId, String requestSegmentTypeCode) {
        //final RequestDTO tr = requestListCache.getValue(reqId);
        // --- we go to detail screen even if there is no connection if we have cached detail data
        if (!ConcurCore.isConnected() || tr == null || tr.getEntriesMap() == null) {
            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), CLS_TAG);
        } else {
            final Intent i = new Intent(RequestSummaryActivity.this, RequestEntryActivity.class);
            i.putExtra(RequestListActivity.REQUEST_ID, tr.getId());
            i.putExtra(RequestEntryActivity.ENTRY_ID, entryId);
            i.putExtra(RequestEntryActivity.REQUEST_SEGMENT_TYPE_CODE, requestSegmentTypeCode);


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
                displayEntryDetail(entryWaitingForRefresh, (entryWaitingForRefreshType != null ? entryWaitingForRefreshType.getCode() : null));
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
        //asyncReceiverFormFields.setListener(null);
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
        // --- we force the view to go back to detail as it can be loading when we leave to create an entry
        setView(ID_DETAIL_VIEW);
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
