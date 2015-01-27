package com.concur.mobile.core.request.activity;

import android.app.Activity;
import android.content.Context;
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
import com.concur.mobile.core.request.service.RequestParser;
import com.concur.mobile.core.request.task.RequestDetailsTask;
import com.concur.mobile.core.request.task.RequestFormFieldsTask;
import com.concur.mobile.core.request.task.RequestSubmitTask;
import com.concur.mobile.core.request.util.DateUtil;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.common.formfield.ConnectForm;
import com.concur.mobile.platform.common.formfield.ConnectFormFieldsCache;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.dto.RequestEntryDTO;
import com.concur.mobile.platform.request.dto.RequestSegmentDTO;
import com.concur.mobile.platform.ui.common.dialog.NoConnectivityDialogFragment;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RequestSummaryActivity extends BaseActivity {

    private static final String CLS_TAG = RequestSummaryActivity.class.getSimpleName();
    private final RequestParser requestParser = new RequestParser();

    private static final int ID_LOADING_VIEW = 0;
    private static final int ID_DETAIL_VIEW = 1;

    public static final int HEADER_UPDATE_RESULT = 1;

    public static final String REQUEST_IS_EDITABLE = "false";

    /**
     * A reference to the application instance representing the client.
     */
    private ViewFlipper requestDetailVF;
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

    protected Boolean showCodes;
    protected int category;
    protected int minSearchLength = 1;

    private RequestDTO tr = null;

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
        configureUI();
    }

    private void configureUI() {
        requestDetailVF = ((ViewFlipper) findViewById(R.id.requestDetailVF));
        requestDetailVF.setDisplayedChild(ID_LOADING_VIEW);
        entryListView = ((ListView) findViewById(R.id.segmentListView));
        entryListView.setAdapter(new EntryListAdapter(this.getBaseContext(), null));
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

        final Locale loc = this.getResources().getConfiguration().locale != null ?
                this.getResources().getConfiguration().locale :
                Locale.US;

        final String formattedAmount = FormatUtil
                .formatAmount(request.getTotal(), loc, request.getCurrencyCode(), true, true);

        name.setText(request.getName());
        amount.setText(formattedAmount);
        amountInButton.setText(formattedAmount);
        business_purpose.setText(request.getPurpose());
        requestComment.setText(request.getLastComment());

        startDate.setText(DateUtil.getFormattedDateForLocale(DateUtil.DatePattern.SHORT, loc, request.getStartDate()));

        name.setTypeface(Typeface.DEFAULT_BOLD);
        amount.setTypeface(Typeface.DEFAULT_BOLD);

        if (tr.getEntriesList() != null) {
            ((EntryListAdapter) entryListView.getAdapter()).updateList(tr.getEntriesList());
            if (tr.getEntriesList().size() <= 1) {
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
                requestDetailVF.setDisplayedChild(ID_LOADING_VIEW);
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
        if (tr.getEntriesList() != null) {
            requestDetailVF.setDisplayedChild(ID_DETAIL_VIEW);
            tr.setEntriesList(tr.getEntriesList());
            updateTRDetailsUI(tr);
            return true;
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
            requestDetailVF.setDisplayedChild(ID_DETAIL_VIEW);
            requestParser.parseTRDetailResponse(tr, resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
            updateTRDetailsUI(tr);

            if (tr.getEntriesList() != null) {
                for (RequestEntryDTO re : tr.getEntriesList()) {
                    for (RequestSegmentDTO segment : re.getListSegment()) {
                        formFieldsCache.setFormRefreshStatus(segment.getSegmentFormId(), true);
                        new RequestFormFieldsTask(RequestSummaryActivity.this, 1, asyncReceiverFormFields,
                                segment.getSegmentFormId(), false).execute();
                    }
                }
            }

            // Display submit button?
            submitButton = (RelativeLayout) findViewById(R.id.submitButton);
            if (tr.isActionPermitted(RequestDTO.SUBMIT)) {
                submitButton.setVisibility(View.VISIBLE);
            } else {
                submitButton.setVisibility(View.GONE);
            }

        }

        @Override
        public void onRequestFail(Bundle resultData) {
            handleRefreshFail();
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestfails");
            Log.d(Const.LOG_TAG, " onRequestFail in TRDetailsListener...");
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            handleRefreshFail();
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestcancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in TRDetailsListener...");
        }

        @Override
        public void cleanup() {
            asyncReceiver.setListener(null);
        }
    }

    public class TRSubmitListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            Context context = getApplicationContext();
            final CharSequence text = "REQUEST SUBMITTED";
            int duration = Toast.LENGTH_LONG;
            final Toast toast = Toast.makeText(context, text, duration);
            toast.show();

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
            final CharSequence text = resultData.getString(BaseAsyncRequestTask.HTTP_STATUS_MESSAGE);
            int duration = Toast.LENGTH_LONG;
            final Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();

            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestfails");
            Log.d(Const.LOG_TAG, " onRequestFail in TRSubmitListener...");
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestcancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in TRSubmitListener...");
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
            }
            // TODO see if there can be a nextPage, and if it's the case handle it
            handleAwaitingRefresh(formId, true);
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
                displaySegmentDetail(formWaitingForRefresh);
            }
            formWaitingForRefresh = null;
        }
    }

    /**
     * Generates *** intent
     *
     * @param formId form ID
     */
    private void displaySegmentDetail(String formId) {
        //cleanupReceivers();
        // --- TODO -- displays the corresponding segment ?
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
                    if (tr.isActionPermitted(RequestDTO.SAVE)) {
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
