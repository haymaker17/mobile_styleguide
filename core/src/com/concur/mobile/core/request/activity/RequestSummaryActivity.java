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
import com.concur.mobile.core.request.task.RequestTask;
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
import com.concur.mobile.platform.request.groupConfiguration.Policy;
import com.concur.mobile.platform.request.groupConfiguration.RequestGroupConfiguration;
import com.concur.mobile.platform.request.groupConfiguration.SegmentType;
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
    private RelativeLayout actionButton;
    // for Details
    private BaseAsyncResultReceiver asyncReceiver;
    // for Submit & Recall
    private BaseAsyncResultReceiver asyncReceiverRequestAction;
    private BaseAsyncResultReceiver asyncReceiverFormFields;

    private ConnectFormFieldsCache formFieldsCache = null;
    private String formWaitingForRefresh = null;
    private String entryWaitingForRefresh;
    private SegmentType.RequestSegmentType entryWaitingForRefreshType;

    protected Boolean showCodes;
    protected int category;
    protected int minSearchLength = 1;

    protected Boolean activateOptionButton;

    private RequestDTO tr = null;
    private Locale locale = null;

    private boolean isSegmentsON = false;
    private boolean isSegmentAIR = false;
    private boolean isSegmentTRAIN = false;
    private boolean isSegmentHOTEL = false;
    private boolean isSegmentCAR = false;

    // --- true if user can submit this request
    private boolean isSubmitable = true;

    @Override protected void onCreate(Bundle savedInstanceState) {
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
            Toast.makeText(this, getResources().getString(R.string.general_error), Toast.LENGTH_LONG);
            // getting back to list screen
            final Intent resIntent = new Intent();
            resIntent.putExtra(RequestHeaderActivity.DO_WS_REFRESH, true);
            setResult(Activity.RESULT_OK, resIntent);
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

        actionButton = (RelativeLayout) findViewById(R.id.actionButton);
        actionButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // CALL SUMMIT/RECALL METHOD
                callRequestAction();
            }
        });
    }

    private void processEntryCreationByType(SegmentType.RequestSegmentType segmentType) {
        final String formId = getConcurCore().getRequestGroupConfigurationCache().getValue(getUserId())
                .extractSegmentFormId(tr.getPolicyId(), segmentType);
        waitFormFieldsCacheRefresh(formId, null, segmentType);
        formFieldsCache.setFormRefreshStatus(formId, true);

        new RequestTask(RequestSummaryActivity.this, 2, asyncReceiverFormFields,
                ConnectHelper.ConnectVersion.VERSION_3_0, ConnectHelper.Module.FORM_FIELDS, ConnectHelper.Action.LIST,
                formId).addUrlParameter(RequestTask.P_FORM_ID, formId).addResultData(RequestTask.P_FORM_ID, formId)
                .execute();
    }

    public void setSegmentButtonClose() {
        TextView tv = (TextView) findViewById(R.id.addItemRequest);
        tv.setText(R.string.add_another_item_to_request);
        isSegmentsON = false;
    }

    public void onClickAir(View view) {
        //TextView tv = (TextView) view.findViewById(R.id.AirSummarySegmentText);
        closeSegmentsChoice();
        setSegmentButtonClose();
        processEntryCreationByType(SegmentType.RequestSegmentType.AIR);
    }

    public void onClickTrain(View view) {
        //TextView tv = (TextView) view.findViewById(R.id.TrainSummarySegmentText);
        closeSegmentsChoice();
        setSegmentButtonClose();
        processEntryCreationByType(SegmentType.RequestSegmentType.RAIL);
    }

    public void onClickHotel(View view) {
        //TextView tv = (TextView) view.findViewById(R.id.HotelSummarySegmentText);
        closeSegmentsChoice();
        setSegmentButtonClose();
        processEntryCreationByType(SegmentType.RequestSegmentType.HOTEL);
    }

    public void onClickCar(View view) {
        //TextView tv = (TextView) view.findViewById(R.id.CarSummarySegmentText);
        closeSegmentsChoice();
        setSegmentButtonClose();
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

    public void closeSegmentsChoice() {

        RelativeLayout layoutSegmentAIR = (RelativeLayout) findViewById(R.id.AirSegmentChoice);
        RelativeLayout layoutSegmentTRAIN = (RelativeLayout) findViewById(R.id.TrainSegmentChoice);
        RelativeLayout layoutSegmentHOTEL = (RelativeLayout) findViewById(R.id.HotelSegmentChoice);
        RelativeLayout layoutSegmentCAR = (RelativeLayout) findViewById(R.id.CarSegmentChoice);
        View separator = findViewById(R.id.SegmentsSeparator);

        layoutSegmentAIR.setVisibility(View.GONE);
        layoutSegmentTRAIN.setVisibility(View.GONE);
        layoutSegmentHOTEL.setVisibility(View.GONE);
        layoutSegmentCAR.setVisibility(View.GONE);
        separator.setVisibility(View.GONE);
    }

    private void updateTRDetailsUI(RequestDTO request) {

        RelativeLayout addItemButton = (RelativeLayout) findViewById(R.id.addItemButton);
        addItemButton.setVisibility(View.GONE);
        closeSegmentsChoice();

        if (tr.isActionPermitted(RequestParser.PermittedAction.SUBMIT)) {
            addItemButton.setVisibility(View.VISIBLE);

            final RequestGroupConfiguration rgc = getConcurCore().getRequestGroupConfigurationCache()
                    .getValue(getUserId());
            if (rgc != null) {
                for (Policy p : rgc.getPolicies()) {
                    if (tr.getPolicyId().equals(p.getId())) {
                        for (SegmentType st : p.getSegmentTypes()) {
                            String code = st.getCode();
                            if (SegmentType.RequestSegmentType.AIR.getCode().equals(code)) {
                                isSegmentAIR = true;
                            } else if (SegmentType.RequestSegmentType.RAIL.getCode().equals(code)) {
                                isSegmentTRAIN = true;
                            } else if (SegmentType.RequestSegmentType.HOTEL.getCode().equals(code)) {
                                isSegmentHOTEL = true;
                            } else if (SegmentType.RequestSegmentType.CAR.getCode().equals(code)) {
                                isSegmentCAR = true;
                            }
                        }
                    }
                }
            }

            //Show ADD SEGMENTS

            updateNewButtonsViews();

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
            ((EntryListAdapter) entryListView.getAdapter())
                    .updateList(new ArrayList<RequestEntryDTO>(tr.getEntriesMap().values()));
            if (tr.getEntriesMap().size() <= 1) {
                final int height = (int) TypedValue
                        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, getResources().getDisplayMetrics());
                segmentListViewLayout.getLayoutParams().height = height;
            } else if (tr.getEntriesMap().size() > 1) {
                final int height = (int) TypedValue
                        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 170, getResources().getDisplayMetrics());
                segmentListViewLayout.getLayoutParams().height = height;
            } else if (tr.getEntriesMap().size() > 1) {
                final int height = (int) TypedValue
                        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 170, getResources().getDisplayMetrics());
                segmentListViewLayout.getLayoutParams().height = height;
            }
        }
    }

    private void updateNewButtonsViews() {
        RelativeLayout layoutSegmentAIR = (RelativeLayout) findViewById(R.id.AirSegmentChoice);
        RelativeLayout layoutSegmentTRAIN = (RelativeLayout) findViewById(R.id.TrainSegmentChoice);
        RelativeLayout layoutSegmentHOTEL = (RelativeLayout) findViewById(R.id.HotelSegmentChoice);
        RelativeLayout layoutSegmentCAR = (RelativeLayout) findViewById(R.id.CarSegmentChoice);
        View separator = findViewById(R.id.SegmentsSeparator);

        layoutSegmentAIR.setVisibility(View.GONE);
        layoutSegmentTRAIN.setVisibility(View.GONE);
        layoutSegmentHOTEL.setVisibility(View.GONE);
        layoutSegmentCAR.setVisibility(View.GONE);
        separator.setVisibility(View.GONE);

        if (isSegmentsON) {
            if (isSegmentAIR) {
                layoutSegmentAIR.setVisibility(View.VISIBLE);
            }
            if (isSegmentTRAIN) {
                layoutSegmentTRAIN.setVisibility(View.VISIBLE);
            }
            if (isSegmentHOTEL) {
                layoutSegmentHOTEL.setVisibility(View.VISIBLE);
            }
            if (isSegmentCAR) {
                layoutSegmentCAR.setVisibility(View.VISIBLE);
            }
            separator.setVisibility(View.VISIBLE);
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
                new RequestTask(RequestSummaryActivity.this, 1, asyncReceiver, ConnectHelper.Action.DETAIL, tr.getId())
                        .execute();
            } else {
                // --- we lost the request object. Should not happen, yet if it does just go back to the list & log it
                Log.e(Const.LOG_TAG, CLS_TAG + " refreshData() : request object lost, going back to list activity.");
                finish();
            }
        } else if (refreshRequired || !showCacheData()) {
            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), CLS_TAG);
        } else {
            setView(ID_DETAIL_VIEW);
        }
    }

    private void callRequestAction() {
        if (ConcurCore.isConnected()) {
            requestSummaryVF.setDisplayedChild(ID_LOADING_VIEW);
            // --- creates the listener
            asyncReceiverRequestAction.setListener(new TRActionListener());
            // --- onRequestResult calls cleanup() on execution, so listener will be destroyed by processing
            // --- if (isSubmitable) => user taped on a submit button, else he obviously could only tap
            //     on a recall one.
            new RequestTask(RequestSummaryActivity.this, 1, asyncReceiverRequestAction,
                    isSubmitable ? ConnectHelper.Action.SUBMIT : ConnectHelper.Action.RECALL, tr.getId()).execute();
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

        @Override public void onRequestSuccess(Bundle resultData) {
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

                    new RequestTask(RequestSummaryActivity.this, 2, asyncReceiverFormFields,
                            ConnectHelper.ConnectVersion.VERSION_3_0, ConnectHelper.Module.FORM_FIELDS,
                            ConnectHelper.Action.LIST, re.getSegmentFormId())
                            .addUrlParameter(RequestTask.P_FORM_ID, re.getSegmentFormId())
                            .addResultData(RequestTask.P_FORM_ID, re.getSegmentFormId()).execute();
                }
            }

            // Display submit/recall button?
            actionButton = (RelativeLayout) findViewById(R.id.actionButton);
            if (tr.isActionPermitted(RequestParser.PermittedAction.SUBMIT)) {
                actionButton.setVisibility(View.VISIBLE);
                ((TextView) actionButton.findViewById(R.id.actionButtonText))
                        .setText(getResources().getString(R.string.tr_submit));
                isSubmitable = true;
            } else if (tr.isActionPermitted(RequestParser.PermittedAction.RECALL)) {
                actionButton.setVisibility(View.VISIBLE);
                ((TextView) actionButton.findViewById(R.id.actionButtonText))
                        .setText(getResources().getString(R.string.tr_recall));
                isSubmitable = false;
            } else {
                actionButton.setVisibility(View.GONE);
            }

        }

        @Override public void onRequestFail(Bundle resultData) {
            ConnectHelper.displayResponseMessage(getApplicationContext(), resultData,
                    getResources().getString(R.string.tr_error_retrieving_detail));
            handleRefreshFail();
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onRequestCancel");
            Log.d(Const.LOG_TAG, " onRequestFail in TRDetailsListener...");
        }

        @Override public void onRequestCancel(Bundle resultData) {
            ConnectHelper
                    .displayMessage(getApplicationContext(), getResources().getString(R.string.tr_operation_canceled));
            handleRefreshFail();
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onRequestCancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in TRDetailsListener...");
        }

        @Override public void cleanup() {
            asyncReceiver.setListener(null);
        }
    }

    /**
     * Handles tap upon an item on the request list
     */
    class EntryListAdapterRowClickListener implements AdapterView.OnItemClickListener {

        @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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

        if (viewID == ID_LOADING_VIEW) {
            activateOptionButton = false;
        } else {
            activateOptionButton = true;
        }

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

            startActivityForResult(i, ENTRY_UPDATE_RESULT);
        }
    }

    public class TRActionListener implements AsyncReplyListener {

        @Override public void onRequestSuccess(Bundle resultData) {
            ((RequestListCache) getConcurCore().getRequestListCache()).setDirty(true);

            // metrics
            final Map<String, String> params = new HashMap<String, String>();
            params.put(isSubmitable ? Flurry.EVENT_NAME_SUBMIT : "Request Recall", tr.getId());
            EventTracker.INSTANCE
                    .track(Flurry.CATEGORY_TRAVEL_REQUEST, Flurry.PARAM_VALUE_TRAVEL_REQUEST_SUMMARY, params);

            // getting back to list screen
            final Intent resIntent = new Intent();
            resIntent.putExtra(RequestHeaderActivity.DO_WS_REFRESH, true);
            setResult(Activity.RESULT_OK, resIntent);

            finish();
        }

        @Override public void onRequestFail(Bundle resultData) {
            ConnectHelper.displayResponseMessage(getApplicationContext(), resultData,
                    getResources().getString(isSubmitable ? R.string.tr_error_submit : R.string.tr_error_recall));

            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestfails");
            Log.d(Const.LOG_TAG, " onRequestFail in TRActionListener...");
            requestSummaryVF.setDisplayedChild(ID_DETAIL_VIEW);
        }

        @Override public void onRequestCancel(Bundle resultData) {
            ConnectHelper
                    .displayMessage(getApplicationContext(), getResources().getString(R.string.tr_operation_canceled));
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onRequestCancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in TRActionListener...");
            requestSummaryVF.setDisplayedChild(ID_DETAIL_VIEW);
        }

        @Override public void cleanup() {
            asyncReceiverRequestAction.setListener(null);
        }
    }

    /**
     * Call asynchronous task to retrieve data through connect
     */
    private class SegmentFormFieldsListener implements AsyncReplyListener {

        @Override public void onRequestSuccess(Bundle resultData) {
            final String formId = resultData.getString(RequestTask.P_FORM_ID);
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
        }

        @Override public void onRequestFail(Bundle resultData) {
            final String formId = resultData.getString(RequestTask.P_FORM_ID);
            formFieldsCache.setFormRefreshStatus(formId, false);
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestfails");
            Log.d(Const.LOG_TAG, " onRequestFail in SegmentFormFieldsListener...");
            handleAwaitingRefresh(formId, false);
        }

        @Override public void onRequestCancel(Bundle resultData) {
            final String formId = resultData.getString(RequestTask.P_FORM_ID);
            formFieldsCache.setFormRefreshStatus(formId, false);
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestcancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in SegmentFormFieldsListener...");
            handleAwaitingRefresh(formId, false);
        }

        @Override public void cleanup() {
            //asyncReceiverFormFields.setListener(null);
        }
    }

    private void handleAwaitingRefresh(String formId, boolean isSuccess) {
        if (formWaitingForRefresh != null && formId.equals(formWaitingForRefresh)) {
            if (isSuccess) {
                displayEntryDetail(entryWaitingForRefresh,
                        (entryWaitingForRefreshType != null ? entryWaitingForRefreshType.getCode() : null));
            } else {
                setView(ID_DETAIL_VIEW);
                Log.e(CLS_TAG, "Form refresh failed");
                Toast.makeText(this, getResources().getString(R.string.general_error), Toast.LENGTH_LONG);
            }
            formWaitingForRefresh = null;
            entryWaitingForRefresh = null;
        }
    }

    private void handleRefreshFail() {
        if (!showCacheData()) {
            Log.d(Const.LOG_TAG, CLS_TAG + " onRequestCancel() : no cache data to display, going back to list.");
            finish();
        }
    }

    // //////////
    // STATES //
    // //////////
    @Override protected void onResume() {
        super.onResume();

        activateOptionButton = true;

        // DETAILS
        if (asyncReceiver == null) {
            // activity creation
            asyncReceiver = new BaseAsyncResultReceiver(new Handler());
            refreshData(false);
        }

        // SUBMIT / RECALL
        // activity creation
        if (asyncReceiverRequestAction == null) {
            asyncReceiverRequestAction = new BaseAsyncResultReceiver(new Handler());
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

    @Override protected void onPause() {
        super.onPause();
        cleanupReceivers();
    }

    // OVERFLOW MENU
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.request_digest, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem menuItem) {

        if (activateOptionButton) {
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
        return super.onOptionsItemSelected(menuItem);
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // --- we force the view to go back to detail as it can be loading when we leave to create an entry
        setView(ID_DETAIL_VIEW);
        switch (requestCode) {
        case (ENTRY_UPDATE_RESULT):
        case (HEADER_UPDATE_RESULT): {
            if (data != null) {
                final Boolean newText = data.getBooleanExtra(RequestHeaderActivity.DO_WS_REFRESH, false);
                if (newText) {
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
    }
}
