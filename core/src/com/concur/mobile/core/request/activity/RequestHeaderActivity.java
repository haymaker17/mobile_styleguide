package com.concur.mobile.core.request.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.apptentive.android.sdk.Log;
import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.AbstractConnectFormFieldActivity;
import com.concur.mobile.core.request.service.RequestParser;
import com.concur.mobile.core.request.task.RequestSaveTask;
import com.concur.mobile.core.request.util.ConnectHelper;
import com.concur.mobile.core.request.util.DateUtil;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.platform.common.formfield.ConnectForm;
import com.concur.mobile.platform.common.formfield.ConnectFormField;
import com.concur.mobile.platform.common.formfield.ConnectFormFieldsCache;
import com.concur.mobile.platform.request.RequestGroupConfigurationCache;
import com.concur.mobile.platform.request.RequestListCache;
import com.concur.mobile.platform.request.dto.FormDTO;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.groupConfiguration.RequestGroupConfiguration;
import com.concur.mobile.platform.ui.common.dialog.NoConnectivityDialogFragment;
import com.concur.mobile.platform.util.Parse;

import java.util.*;

public class RequestHeaderActivity extends AbstractConnectFormFieldActivity implements OnClickListener {

    private static final String CLS_TAG = RequestListActivity.class.getSimpleName();

    private static final int ID_LOADING_VIEW = 0;
    private static final int ID_HEADER_VIEW = 1;

    private static final String FIELD_NAME = "Name";
    private static final String FIELD_START_DATE = "StartDate";
    private static final String FIELD_END_DATE = "EndDate";
    private static final String FIELD_PURPOSE = "Purpose";
    private static final String FIELD_COMMENT = "Comment";
    private static final String FIELD_EMP_NAME = "EmpName";
    private static final String FIELD_CURRENCY_NAME = "CurrencyName";
    private static final String FIELD_TOTAL_POSTED_AMOUNT = "TotalPostedAmount";

    public static final String DO_WS_REFRESH = "doWSRefresh";

    protected ConnectForm form;
    protected Locale locale;

    private RequestListCache requestListCache = null;
    private ConnectFormFieldsCache formFieldsCache = null;
    private RequestGroupConfigurationCache groupConfigurationCache = null;
    private RequestDTO tr;
    private BaseAsyncResultReceiver asyncReceiverSave;

    private ViewFlipper requestHeaderVF;
    private RelativeLayout saveButton;

    /**
     * Bundle need to contain:
     * - RequestSummaryActivity.REQUEST_IS_EDITABLE
     * And for consultation / update:
     * - RequestListActivity.REQUEST_ID
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.request_header);

        final ConcurCore concurCore = (ConcurCore) getApplication();

        requestListCache = (RequestListCache) concurCore.getRequestListCache();
        formFieldsCache = (ConnectFormFieldsCache) concurCore.getRequestFormFieldsCache();
        groupConfigurationCache = (RequestGroupConfigurationCache) concurCore.getRequestGroupConfigurationCache();

        final Bundle bundle = getIntent().getExtras();
        final String requestId = bundle.getString(RequestListActivity.REQUEST_ID);

        this.isEditable = bundle.getString(RequestSummaryActivity.REQUEST_IS_EDITABLE).equals(Boolean.TRUE.toString());

        locale = this.getResources().getConfiguration().locale != null ?
                this.getResources().getConfiguration().locale :
                Locale.US;

        // --- update mode
        if (requestId != null) {
            tr = requestListCache.getValue(requestId);
            form = formFieldsCache.getFormFields(tr.getHeaderFormId());
        }
        // --- create mode
        else {
            tr = new RequestDTO();
            final RequestGroupConfiguration rgc = groupConfigurationCache.getValue(getUserId());
            form = formFieldsCache.getValue(rgc.getFormId());
            // --- TR initialization
            tr.setHeaderFormId(rgc.getFormId());
            tr.setPolicyId(rgc.getDefaultPolicyId());
            // --- TODO : TBC
            tr.setCurrencyCode(Currency.getInstance(locale).getCurrencyCode());
            tr.setRequestDate(new Date());
        }

        configureUI();
    }

    @Override
    protected Locale getLocale() {
        return locale;
    }

    @Override
    protected DateUtil.DatePattern getPattern() {
        return DateUtil.DatePattern.DB_INPUT;
    }

    private void configureUI() {
        requestHeaderVF = ((ViewFlipper) findViewById(R.id.requestDetailVF));
        requestHeaderVF.setDisplayedChild(ID_HEADER_VIEW);
        final LinearLayout requestTitleLayout = (LinearLayout) findViewById(R.id.requestTitleLayout);
        final LinearLayout requestHeaderFields = (LinearLayout) findViewById(R.id.requestHeaderFields);
        final String fieldToTitle = FIELD_NAME;
        this.setDisplayFields(tr, form, requestTitleLayout, requestHeaderFields, fieldToTitle);

        saveButton = (RelativeLayout) findViewById(R.id.saveButton);
        applySaveButtonPolicy(saveButton);
    }

    @Override
    protected void save(ConnectForm form, FormDTO tr) {
        super.save(form, tr);
        if (ConcurCore.isConnected()) {
            // --- creates the listener
            asyncReceiverSave.setListener(new SaveListener());
            requestHeaderVF.setDisplayedChild(ID_LOADING_VIEW);
            // --- onRequestResult calls cleanup() on execution, so listener will be destroyed by processing
            new RequestSaveTask(RequestHeaderActivity.this, 1, asyncReceiverSave, (RequestDTO) tr).execute();
        } else {
            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), CLS_TAG);
        }
    }

    @Override
    public void onClick(View view) {
        for (int i = 0; i < getDateViews().size(); i++) {
            if ((Integer) getDateViews().get(i) == view.getId()) {
                ((DatePickerDialog) getDatePickerDialogs().get(i)).show();
            }
        }
    }

    @Override
    protected String getLabelFromFieldName(String fieldName) {
        if (fieldName.equals(FIELD_NAME)) {
            return this.getResources().getString(R.string.tr_name);
        } else if (fieldName.equals(FIELD_START_DATE)) {
            return this.getResources().getString(R.string.tr_start_date);
        } else if (fieldName.equals(FIELD_END_DATE)) {
            return this.getResources().getString(R.string.tr_end_date);
        } else if (fieldName.equals(FIELD_PURPOSE)) {
            return this.getResources().getString(R.string.business_purpose);
        } else if (fieldName.equals(FIELD_COMMENT)) {
            return this.getResources().getString(R.string.comment);
        } else if (fieldName.equals(FIELD_EMP_NAME)) {
            return this.getResources().getString(R.string.tr_employee_name);
        } else if (fieldName.equals(FIELD_CURRENCY_NAME)) {
            return this.getResources().getString(R.string.currency);
        } else if (fieldName.equals(FIELD_TOTAL_POSTED_AMOUNT)) {
            return this.getResources().getString(R.string.amount);
        } else {
            return "";
        }
    }

    @Override public void applySaveButtonPolicy(View saveButtonView) {
        saveButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // CALL SUMMIT METHOD
                save(form, tr);
            }
        });
    }

    @Override
    public String getModelValueByFieldName(FormDTO request, String fieldName) {
        final RequestDTO tr = (RequestDTO) request;

        if (fieldName.equals(FIELD_NAME)) {
            if (tr.getName() == null) {
                return "";
            }
            return tr.getName();
        } else if (fieldName.equals(FIELD_START_DATE)) {
            if (tr.getStartDate() == null) {
                return "";
            }
            return formatDate(tr.getStartDate());
        } else if (fieldName.equals(FIELD_END_DATE)) {
            if (tr.getStartDate() == null) {
                return "";
            }
            return formatDate(tr.getEndDate());
        } else if (fieldName.equals(FIELD_PURPOSE)) {
            if (tr.getPurpose() == null) {
                return "";
            }
            return tr.getPurpose();
        } else if (fieldName.equals(FIELD_COMMENT)) {
            if (tr.getLastComment() == null) {
                return "";
            }
            return tr.getLastComment();
        } else if (fieldName.equals(FIELD_EMP_NAME)) {
            if (tr.getEmployeeName() == null) {
                return "";
            }
            return tr.getEmployeeName();
        } else if (fieldName.equals(FIELD_CURRENCY_NAME)) {
            if (tr.getCurrencyCode() == null) {
                return "";
            }
            return tr.getCurrencyCode();
        } else if (fieldName.equals(FIELD_TOTAL_POSTED_AMOUNT)) {
            return String.valueOf(tr.getTotal());
        } else {
            return "";
        }
    }

    @Override
    protected void setModelValueByFieldName(FormDTO request, String fieldName, String value) {
        final RequestDTO tr = (RequestDTO) request;
        if (fieldName.equals(FIELD_NAME)) {
            tr.setName(value);
        } else if (fieldName.equals(FIELD_START_DATE)) {
            tr.setStartDate(parseDate(value));
        } else if (fieldName.equals(FIELD_END_DATE)) {
            tr.setEndDate(parseDate(value));
        } else if (fieldName.equals(FIELD_PURPOSE)) {
            tr.setPurpose(value);
        } else if (fieldName.equals(FIELD_COMMENT)) {
            tr.setLastComment(value);
        } else if (fieldName.equals(FIELD_EMP_NAME)) {
            tr.setEmployeeName(value);
        } else if (fieldName.equals(FIELD_CURRENCY_NAME)) {
            tr.setCurrencyCode(value);
        } else if (fieldName.equals(FIELD_TOTAL_POSTED_AMOUNT)) {
            tr.setTotal(Parse.safeParseDouble(value));
        }
    }

    @Override
    protected void applySpecificRender(final TextView component, final LinearLayout.LayoutParams llp,
            final ConnectFormField ff) {
        if (ff.getName().equals(FIELD_NAME)) {
            component.setTextAppearance(this, R.style.ListCellHeaderText);
            component.setTextColor(getResources().getColor(R.color.White));
            component.setTypeface(Typeface.DEFAULT_BOLD);
            component.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            if (component instanceof EditText) {
                component.setHint(getResources().getString(R.string.tr_name));
                component.setHintTextColor(getResources().getColor(R.color.light_gray));
            }
        }
    }

    public class SaveListener implements BaseAsyncRequestTask.AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            final boolean isCreation = tr.getId() == null;
            ConnectHelper.displayMessage(getApplicationContext(), "REQUEST SAVED");

            // metrics
            final Map<String, String> params = new HashMap<String, String>();

            params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_TRAVEL_REQUEST_HEADER);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_TRAVEL_REQUEST,
                    (isCreation ? Flurry.EVENT_NAME_CREATE : Flurry.EVENT_NAME_SAVED), params);

            // --- TODO Refresh values

            if (resultData != null) {

                final String requestId = RequestParser
                        .parseActionResponse(resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));

                // we go to the digest screen
                if (isCreation) {

                    final Intent i = new Intent(RequestHeaderActivity.this, RequestSummaryActivity.class);
                    i.putExtra(RequestListActivity.REQUEST_ID, requestId);

                    // --- Flurry tracking
                    i.putExtra(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_TRAVEL_REQUEST_HEADER);
                    params.clear();
                    params.put(Flurry.PARAM_NAME_FROM, Flurry.PARAM_VALUE_TRAVEL_REQUEST_HEADER);
                    params.put(Flurry.PARAM_NAME_TO, Flurry.PARAM_VALUE_TRAVEL_REQUEST_SUMMARY);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_TRAVEL_REQUEST, Flurry.EVENT_NAME_LAUNCH, params);

                    startActivity(i);
                    finish();
                } else {
                    final Intent resIntent = new Intent();
                    resIntent.putExtra(DO_WS_REFRESH, true);
                    setResult(Activity.RESULT_OK, resIntent);

                    finish();
                }

            }
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            ConnectHelper.displayResponseMessage(getApplicationContext(), resultData,
                    getResources().getString(R.string.tr_error_save));

            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onRequestFail");
            Log.d(Const.LOG_TAG, " onRequestFail in SaveListener...");
            requestHeaderVF.setDisplayedChild(ID_HEADER_VIEW);
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            ConnectHelper
                    .displayMessage(getApplicationContext(), getResources().getString(R.string.tr_operation_canceled));
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onRequestCancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in SaveListener...");
            requestHeaderVF.setDisplayedChild(ID_HEADER_VIEW);
        }

        @Override
        public void cleanup() {
            asyncReceiverSave.setListener(null);
        }
    }

    private void cleanupReceivers() {
        // NTD
    }

    @Override
    protected void onResume() {
        super.onResume();

        // SAVE
        // activity creation
        if (asyncReceiverSave == null) {
            asyncReceiverSave = new BaseAsyncResultReceiver(new Handler());
        }
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
}
