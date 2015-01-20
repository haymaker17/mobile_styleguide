package com.concur.mobile.core.request.activity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

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

public class RequestDigestActivity extends BaseActivity {

    private static final int ID_LOADING_VIEW = 0;
    private static final int ID_DETAIL_VIEW = 1;

    private static final String CLS_TAG = RequestDigestActivity.class.getSimpleName();
    private final RequestParser requestParser = new RequestParser();

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
        setContentView(R.layout.request_details);
        category = getResources().getColor(R.color.SectionHeaderBackground);

        formFieldsCache = (ConnectFormFieldsCache) getConcurCore().getRequestFormFieldsCache();
        asyncReceiverFormFields = new BaseAsyncResultReceiver(new Handler());
        asyncReceiverFormFields.setListener(new SegmentFormFieldsListener());

        final Bundle bundle = getIntent().getExtras();
        final String requestId = bundle.getString(RequestListActivity.REQUEST_ID);
        if (requestId != null) {
            tr = getConcurCore().getRequestListCache().getValue(requestId);
        }
        else {
            Log.e(Const.LOG_TAG, CLS_TAG + " onCreate() : problem on tr retrieved, going back to list activity.");
            // TODO : throw exception & display toast message ? @See with PM
            finish();
        }
        configure();
    }

    private void configure() {
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
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".populateExpenseHeaderNavBarInfo: missing navigation bar title text resource!", resNotFndExc);
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

        final String formattedAmount = FormatUtil.formatAmount(request.getTotal(), RequestDigestActivity.this
                .getResources().getConfiguration().locale, request.getCurrency(), true, true);

        name.setText(request.getName());
        amount.setText(formattedAmount);
        amountInButton.setText(formattedAmount);

        final Locale loc = this.getResources().getConfiguration().locale;
        startDate.setText(DateUtil.getFormattedDateForLocale(DateUtil.DatePattern.SHORT, (loc != null) ? loc
                : Locale.US, request.getStartDate()));

        name.setTypeface(Typeface.DEFAULT_BOLD);
        amount.setTypeface(Typeface.DEFAULT_BOLD);

        if (tr.getEntriesList() != null) {
            ((EntryListAdapter) entryListView.getAdapter()).updateList(tr.getEntriesList());
            if (tr.getEntriesList().size() <= 1) {
                final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, getResources()
                        .getDisplayMetrics());
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

                new RequestDetailsTask(RequestDigestActivity.this, 1, asyncReceiver, tr.getId()).execute();
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
            new RequestSubmitTask(RequestDigestActivity.this, 1, asyncReceiverSubmit, tr.getId()).execute();
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
                        new RequestFormFieldsTask(RequestDigestActivity.this, 1, asyncReceiverFormFields, segment.getSegmentFormId(), false).execute();
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
            Log.d(Const.LOG_TAG, " onRequestFail in TRListListener...");
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            handleRefreshFail();
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestcancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in TRListListener...");
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
            CharSequence text = "REQUEST SUBMITTED";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            // metrics
            final Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.EVENT_NAME_SUBMIT, tr.getId());
            EventTracker.INSTANCE.track(Flurry.CATEGORY_HOME, Flurry.PARAM_VALUE_TRAVEL_REQUEST, params);

            // on retrourne sur la list
            finish();
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            Context context = getApplicationContext();
            CharSequence text = resultData.getString(BaseAsyncRequestTask.HTTP_STATUS_MESSAGE);
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestfails");
            Log.d(Const.LOG_TAG, " onRequestFail in TRListListener...");
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onrequestcancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in TRListListener...");
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
            asyncReceiverFormFields.setListener(null);
        }
    }

    private void handleAwaitingRefresh(String formId, boolean isSuccess) {
        if (formWaitingForRefresh != null && formId.equals(formWaitingForRefresh)) {
            if (isSuccess)
                displaySegmentDetail(formWaitingForRefresh);
            formWaitingForRefresh = null;
        }

    }

    /**
     * Generates *** intent
     *
     * @param formId
     *            form ID
     */
    private void displaySegmentDetail(String formId) {
        // --- TODO -- displays the corresponding segment ?
    }

    private void handleRefreshFail() {
        if (!showCacheData()) {
            Log.d(Const.LOG_TAG, CLS_TAG + " onRequestCancel() : no cache data to display, going back to list.");
            startActivity(new Intent(RequestDigestActivity.this, RequestListActivity.class));
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
            asyncReceiver.setListener(new TRDetailsListener());
            refreshData(false);
        } else {
            // activity restoration
            asyncReceiver.setListener(new TRDetailsListener());
        }

        // SUBMIT
        // activity creation
        if (asyncReceiverSubmit == null) {
            asyncReceiverSubmit = new BaseAsyncResultReceiver(new Handler());
        }
        // activity restoration
        asyncReceiverSubmit.setListener(new TRSubmitListener());

        // F & F
        // activity creation
        if (asyncReceiverFormFields == null) {
            asyncReceiverFormFields = new BaseAsyncResultReceiver(new Handler());
        }
        // activity restoration
        asyncReceiverFormFields.setListener(new SegmentFormFieldsListener());

    }

    @Override
    protected void onPause() {
        super.onPause();
        asyncReceiver.setListener(null);
        asyncReceiverSubmit.setListener(null);
        asyncReceiverFormFields.setListener(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        asyncReceiver.setListener(null);
        asyncReceiverSubmit.setListener(null);
        asyncReceiverFormFields.setListener(null);
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
                    Intent intent = new Intent(RequestDigestActivity.this, RequestHeaderActivity.class);
                	intent.putExtra(RequestListActivity.REQUEST_ID, tr.getId());
                    startActivity(intent);
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
            }
            return true;
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
    }

}
