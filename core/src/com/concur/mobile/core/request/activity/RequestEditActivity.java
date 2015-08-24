package com.concur.mobile.core.request.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.request.util.RequestExceptionDialog;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.common.formfield.ConnectForm;
import com.concur.mobile.platform.common.formfield.ConnectFormField;
import com.concur.mobile.platform.common.formfield.ConnectFormFieldsCache;
import com.concur.mobile.platform.request.RequestGroupConfigurationCache;
import com.concur.mobile.platform.request.RequestListCache;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.dto.RequestEntryDTO;
import com.concur.mobile.platform.request.dto.RequestExceptionDTO;
import com.concur.mobile.platform.request.dto.RequestSegmentDTO;
import com.concur.mobile.platform.request.groupConfiguration.RequestGroupConfiguration;
import com.concur.mobile.platform.request.location.Location;
import com.concur.mobile.platform.request.task.RequestTask;
import com.concur.mobile.platform.request.util.ConnectHelper;
import com.concur.mobile.platform.request.util.RequestParser;

import java.text.DateFormatSymbols;
import java.util.*;

public class RequestEditActivity extends BaseActivity implements OnClickListener, DatePickerDialog.OnDateSetListener {

    public static final String REQUEST_IS_EDITABLE = "REQUEST_EDITABLE";
    public static final String DO_WS_REFRESH = "WS_REFRESH";

    private Intent locationIntent;
    private TextView locationTappedView = null;
    private TextView dateTappedView = null;
    private TextView customTappedView = null;

    /**
     * Touch Event
     */
    private View previousTappedView;

    private static final String CLS_TAG = RequestListActivity.class.getSimpleName();

    private static final int ID_LOADING_VIEW = 0;
    private static final int ID_EDIT_VIEW = 1;

    private ViewFlipper requestHeaderVF;

    private boolean canSave;

    protected Locale locale;
    protected ConnectForm form;
    private RequestDTO tr;
    private RequestListCache requestListCache = null;
    private ConnectFormFieldsCache formFieldsCache = null;
    private RequestGroupConfigurationCache groupConfigurationCache = null;
    private BaseAsyncResultReceiver asyncRequestActionReceiver;
    private Date startDate = null;
    private Date endDate = null;

    private ConnectFormField commentCFF;

    private int requiredColor;

    /**
     * Bundle need to contain:
     * - RequestSummaryActivity.REQUEST_IS_EDITABLE
     * And for consultation / update:
     * - RequestListActivity.REQUEST_ID
     *
     * @param savedInstanceState
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requiredColor = getResources().getColor(R.color.CellTextRed);

        setContentView(R.layout.request_edit);

        final ConcurCore concurCore = (ConcurCore) getApplication();
        requestListCache = (RequestListCache) concurCore.getRequestListCache();
        formFieldsCache = (ConnectFormFieldsCache) concurCore.getRequestFormFieldsCache();
        groupConfigurationCache = (RequestGroupConfigurationCache) concurCore.getRequestGroupConfigurationCache();

        final Bundle bundle = getIntent().getExtras();
        final String requestId = bundle.getString(RequestListActivity.REQUEST_ID);

        locale = this.getResources().getConfiguration().locale != null ?
                this.getResources().getConfiguration().locale :
                Locale.US;

        // --- update mode
        if (requestId != null) {
            tr = requestListCache.getValue(requestId);
            form = formFieldsCache.getFormFields(tr.getHeaderFormId());
            setCanSave(tr.getApprovalStatus().equals(RequestDTO.ApprovalStatus.CREATION.getName()) || tr
                    .getApprovalStatus().equals(RequestDTO.ApprovalStatus.RECALLED.getName()));
            startDate = tr.getStartDate();
            endDate = tr.getEndDate();
        }
        // --- create mode
        else {
            tr = new RequestDTO();
            final RequestGroupConfiguration rgc = groupConfigurationCache.getValue(getUserId());
            form = formFieldsCache.getValue(rgc.getFormId());
            // --- TR initialization
            tr.setHeaderFormId(rgc.getFormId());
            tr.setPolicyId(rgc.getDefaultPolicyId());
            tr.setCurrencyCode(Currency.getInstance(locale).getCurrencyCode());
            tr.setRequestDate(new Date());
            setCanSave(true);
        }

        asyncRequestActionReceiver = new BaseAsyncResultReceiver(new Handler());

        /** Set Comment Title */
        commentCFF = form.getConnectFormFieldByName(ConnectFormField.NameType.COMMENT);
        TextView commentTitleView = (TextView) findViewById(R.id.requestEditCommentTitle);
        if (commentCFF != null && commentCFF.isRequired())
            commentTitleView.setText(commentCFF.getLabel());
        else
            commentTitleView.setText(getResources().getString(R.string.tr_comment) + " (" + getResources()
                    .getString(R.string.tr_optional) + ")");

        configureUI();
    }

    private void configureUI() {

        /**************************/
        /** finish editing event */
        //(EMPTY FIELD REQUIRED)
        EditText businessPurposeEditText = (EditText) findViewById(R.id.requestEditBusinessPurpose);
        businessPurposeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    previousTappedView = v;
                } else {
                    /** check if empty or not */
                    TextView tv = (TextView) v;
                    if (tv.getText().length() == 0) {
                        tv.setTextColor(requiredColor);
                        tv.setText("" + getResources().getString(R.string.tr_required));
                    }
                }
            }

        });

        //(EMPTY FIELD REQUIRED)
        EditText commentEditText = (EditText) findViewById(R.id.requestEditComment);
        commentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    previousTappedView = v;
                } else {
                    if (commentCFF != null && commentCFF.isRequired()) {
                        /** check if empty or not */
                        TextView tv = (TextView) v;
                        if (tv.getText().length() == 0) {
                            tv.setTextColor(requiredColor);
                            tv.setText("" + getResources().getString(R.string.tr_required));
                        }
                    }
                }
            }
        });

        //Set view after Loading
        requestHeaderVF = ((ViewFlipper) findViewById(R.id.requestDetailVF));
        requestHeaderVF.setDisplayedChild(ID_EDIT_VIEW);

        // Set the expense header navigation bar information.
        try {
            final String headerNavBarTitle = getResources().getString(R.string.travel_request_header_title);
            getSupportActionBar().setTitle(headerNavBarTitle);
        } catch (Resources.NotFoundException resNotFndExc) {
            android.util.Log.e(Const.LOG_TAG,
                    CLS_TAG + ".populateExpenseHeaderNavBarInfo: missing navigation bar title text resource!",
                    resNotFndExc);
        }

        // Location
        final RelativeLayout startLocationLayout = (RelativeLayout) findViewById(R.id.requestEditStartLocationLayout);
        startLocationLayout.setOnClickListener(this);
        final RelativeLayout destinationLayout = (RelativeLayout) findViewById(R.id.requestEditDestinationLayout);
        destinationLayout.setOnClickListener(this);

        //Date
        RelativeLayout startDateLayout = (RelativeLayout) findViewById(R.id.requestEditStartDateLayout);
        startDateLayout.setOnClickListener(this);
        RelativeLayout endDateLayout = (RelativeLayout) findViewById(R.id.requestEditEndDateLayout);
        endDateLayout.setOnClickListener(this);

        //Business Purpose & Comment
        RelativeLayout requestEditBusinessPurposeLayout = (RelativeLayout) findViewById(
                R.id.requestEditBusinessPurposeLayout);
        requestEditBusinessPurposeLayout.setOnClickListener(this);
        RelativeLayout requestEditCommentLayout = (RelativeLayout) findViewById(R.id.requestEditCommentLayout);
        requestEditCommentLayout.setOnClickListener(this);

    }

    /*
     * (non-Javadoc)
     *
     * @see com.concur.mobile.activity.expense.ConcurView#onActivityResult(int, int, android.content.Intent)
     */
    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            final String selectedListItemKey = data.getStringExtra(LocationSearchActivity.EXTRA_PARAM_LOCATION_ID);
            final String selectedListItemText = data.getStringExtra(LocationSearchActivity.EXTRA_PARAM_LOCATION_NAME);
            if (locationTappedView != null) {
                if (selectedListItemKey != null) {
                    locationTappedView.setText(selectedListItemText);
                    locationTappedView.setHint(selectedListItemKey);
                } else {
                    locationTappedView.setText("");
                    locationTappedView.setHint("");
                }
            }
        }

        /** check required Field when com back to activity */
        if (locationTappedView != null)
            validateTextField(locationTappedView);
    }

    @Override protected void onResume() {
        super.onResume();
        if (asyncRequestActionReceiver == null) {
            asyncRequestActionReceiver = new BaseAsyncResultReceiver(new Handler());
        }
    }

    @Override public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        String month = new DateFormatSymbols().getMonths()[monthOfYear];
        this.dateTappedView.setText("" + month.substring(0, 3) + " " + dayOfMonth + ", " + year);
        if (this.dateTappedView.getId() == R.id.requestEditStartDateLayout) {
            startDate = new Date(year, monthOfYear, dayOfMonth);
        } else if (this.dateTappedView.getId() == R.id.requestEditEndDateLayout) {
            endDate = new Date(year, monthOfYear, dayOfMonth);
        }
    }

    public void setTextEditFocus(View view) {
        EditText textView = (EditText) view;
        textView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(textView, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override public void onClick(View view) {

        dateTappedView = null;
        locationTappedView = null;
        customTappedView = null;

        //Dates Onclick Event
        if (view.getId() == R.id.requestEditStartDateLayout || view.getId() == R.id.requestEditEndDateLayout) {

            if (view.getId() == R.id.requestEditStartDateLayout) {
                this.dateTappedView = (TextView) findViewById(R.id.requestEditStartDate);
            } else if (view.getId() == R.id.requestEditEndDateLayout) {
                this.dateTappedView = (TextView) findViewById(R.id.requestEditEndDate);
            }

            checkEmptyText(dateTappedView);
            DatePickerDialog dialog = new DatePickerDialog(this, this, Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            //For exit event (EMPTY FIELD REQUIRED)
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_NEGATIVE) {
                                /** check if empty or not */
                                validateTextField(dateTappedView);
                            }
                        }
                    });
            dialog.show();

        }

        //Locations Onclick Event
        else if (view.getId() == R.id.requestEditStartLocationLayout
                || view.getId() == R.id.requestEditDestinationLayout) {
            if (locationIntent == null) {
                locationIntent = new Intent(RequestEditActivity.this, LocationSearchActivity.class);
                locationIntent
                        .putExtra(LocationSearchActivity.EXTRA_PARAM_LOCATION_TYPE, Location.LocationType.CITY.name());
                        /*(viewedType == SegmentType.RequestSegmentType.AIR ?
                                Location.LocationType.AIRPORT.name() :
                                Location.LocationType.CITY.name()));*/
            }
            if (view.getId() == R.id.requestEditStartLocationLayout) {
                locationTappedView = (EditText) findViewById(R.id.requestEditStartLocation);
            } else if (view.getId() == R.id.requestEditDestinationLayout) {
                locationTappedView = (EditText) findViewById(R.id.requestEditDestination);
            }

            checkEmptyText(locationTappedView);
            startActivityForResult(locationIntent, Const.REQUEST_CODE_LOCATION);
        }

        //Comment and business Purpose  Event
        else if (view.getId() == R.id.requestEditBusinessPurposeLayout
                || view.getId() == R.id.requestEditCommentLayout) {
            if (view.getId() == R.id.requestEditBusinessPurposeLayout) {
                customTappedView = (EditText) findViewById(R.id.requestEditBusinessPurpose);
            } else if (view.getId() == R.id.requestEditCommentLayout) {
                customTappedView = (EditText) findViewById(R.id.requestEditComment);
            }

            checkEmptyText(customTappedView);
            this.setTextEditFocus(customTappedView);
        }

        // --- Submit exceptions dialog box confirmation button
        else if (view.getId() == R.id.btn_submit) {
            requestHeaderVF.setDisplayedChild(ID_LOADING_VIEW);
            // --- creates the listener
            asyncRequestActionReceiver.setListener(new SaveAndSubmitListener());
            // --- onRequestResult calls cleanup() on execution, so listener will be destroyed by processing
            new RequestTask(RequestEditActivity.this, 1, asyncRequestActionReceiver, tr.getId() != null ?
                    ConnectHelper.Action.UPDATE_AND_SUBMIT :
                    ConnectHelper.Action.CREATE_AND_SUBMIT, tr.getId()).setPostBody(RequestParser.toJson(tr))
                    .addUrlParameter(RequestTask.P_REQUEST_DO_SUBMIT, Boolean.TRUE.toString())
                    .addUrlParameter(RequestTask.P_REQUEST_FORCE_SUBMIT, Boolean.TRUE.toString())
                    .addResultData(RequestTask.P_REQUEST_FORCE_SUBMIT, Boolean.TRUE.toString()).execute();
        }
    }

    /**
     * Call asynchronous task to retrieve data through connect
     */
    private class SaveAndSubmitListener implements BaseAsyncRequestTask.AsyncReplyListener {

        @Override public void onRequestSuccess(Bundle resultData) {
            final Boolean isForceSubmit = resultData.getString(RequestTask.P_REQUEST_FORCE_SUBMIT)
                    .equals(Boolean.TRUE.toString());
            // --- parse the configurations received
            final RequestDTO request = new RequestParser()
                    .parseSaveAndSubmitResponse(resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
            if (request != null) {
                // --- refresh cache
                requestListCache.removeValue(request.getId());
                requestListCache.addValue(request.getId(), request);
                // --- refresh current request object
                tr = request;

                // --- check/display exceptions
                if (request.getHighestExceptionLevel().equals(RequestExceptionDTO.ExceptionLevel.BLOCKING) || (
                        request.getHighestExceptionLevel().equals(RequestExceptionDTO.ExceptionLevel.NON_BLOCKING)
                                && !isForceSubmit)) {
                    requestHeaderVF.setDisplayedChild(ID_EDIT_VIEW);
                    final List<RequestExceptionDTO> exceptionList = new ArrayList<>();
                    // --- Adding Request exceptions
                    for (RequestExceptionDTO e : request.getExceptions()) {
                        e.setTitle("Request");
                        exceptionList.add(e);
                    }
                    if (request.getEntriesMap() != null) {
                        // --- Filtering entry/segment exceptions by segment type (flight / rail / etc)
                        final Map<String, List<RequestExceptionDTO>> exceptionByType = new HashMap<>();
                        for (RequestEntryDTO entry : request.getEntriesMap().values()) {
                            if (!exceptionByType.containsKey(entry.getSegmentTypeCode())) {
                                exceptionByType.put(entry.getSegmentTypeCode(), new ArrayList<RequestExceptionDTO>());
                            }
                            for (RequestExceptionDTO e : entry.getExceptions()) {
                                e.setTitle(entry.getSegmentType());
                                exceptionByType.get(entry.getSegmentTypeCode()).add(e);
                            }
                            for (RequestSegmentDTO segment : entry.getListSegment()) {
                                for (RequestExceptionDTO e : segment.getExceptions()) {
                                    e.setTitle(entry.getSegmentType());
                                    exceptionByType.get(entry.getSegmentTypeCode()).add(e);
                                }
                            }
                        }
                        // --- Adding segment entry/segment exceptions to list
                        for (List<RequestExceptionDTO> lExceptions : exceptionByType.values()) {
                            exceptionList.addAll(lExceptions);
                        }
                    }

                    /** Exception AlertDialog */
                    final RequestExceptionDialog cdd = new RequestExceptionDialog(RequestEditActivity.this,
                            exceptionList);
                    // --- Display Exceptions
                    cdd.show();
                } else {
                    // --- redirect to list screen
                    final Intent resIntent = new Intent();
                    resIntent.putExtra(DO_WS_REFRESH, true);
                    setResult(Activity.RESULT_OK, resIntent);
                    finish();
                }
            }
        }

        @Override public void onRequestFail(Bundle resultData) {
            requestHeaderVF.setDisplayedChild(ID_EDIT_VIEW);
            displayServerErrorMsg();
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onRequestFail");
            Log.d(Const.LOG_TAG, " onRequestFail in SaveAndSubmitListener...");
        }

        @Override public void onRequestCancel(Bundle resultData) {
            requestHeaderVF.setDisplayedChild(ID_EDIT_VIEW);
            displayServerErrorMsg();
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onRequestCancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in SaveAndSubmitListener...");
        }

        @Override public void cleanup() {
            asyncRequestActionReceiver.setListener(null);
        }
    }

    private void displayServerErrorMsg() {
        Toast.makeText(this, R.string.tr_server_error, Toast.LENGTH_LONG).show();
    }

    public void saveAction(View view) {

        if (validateFields(null)) {
            // --- Apply name
            final TextView dest = (TextView) findViewById(R.id.requestEditDestination);
            tr.setName(com.concur.mobile.base.util.Format
                    .localizeText(this, R.string.tr_name_parameterized_value, dest.getText()));
            requestHeaderVF.setDisplayedChild(ID_LOADING_VIEW);
            // --- creates the listener
            asyncRequestActionReceiver.setListener(new SaveAndSubmitListener());
            // --- onRequestResult calls cleanup() on execution, so listener will be destroyed by processing
            new RequestTask(RequestEditActivity.this, 1, asyncRequestActionReceiver, tr.getId() != null ?
                    ConnectHelper.Action.UPDATE_AND_SUBMIT :
                    ConnectHelper.Action.CREATE_AND_SUBMIT, tr.getId()).setPostBody(RequestParser.toJson(tr))
                    .addUrlParameter(RequestTask.P_REQUEST_DO_SUBMIT, Boolean.TRUE.toString())
                    .addUrlParameter(RequestTask.P_REQUEST_FORCE_SUBMIT, Boolean.FALSE.toString())
                    .addResultData(RequestTask.P_REQUEST_FORCE_SUBMIT, Boolean.FALSE.toString()).execute();
        }

    }

    public static String toCamelCase(final String init) {
        if (init == null)
            return null;

        final StringBuilder ret = new StringBuilder(init.length());

        for (final String word : init.split(" ")) {
            if (!word.isEmpty()) {
                ret.append(Character.toUpperCase(word.charAt(0)));
                ret.append(word.substring(1).toLowerCase());
            }
            if (!(ret.length() == init.length()))
                ret.append(" ");
        }

        return ret.toString();
    }

    /** check required for specifique field */

    /**
     * check state of all fields
     * view!=null : specifique field
     * view==null : all fields
     */
    private boolean validateFields(View view) {
        boolean isValid = true;
        TextView textView;
        if (view == null || view.getId() == R.id.requestEditStartDateLayout) {
            textView = (TextView) findViewById(R.id.requestEditStartDate);
            isValid &= this.validateTextField(textView);
            if (isValid) {
                tr.setStartDate(startDate);
            }
        }
        if (view == null || view.getId() == R.id.requestEditEndDateLayout) {
            textView = (TextView) findViewById(R.id.requestEditEndDate);
            isValid &= this.validateTextField(textView);
            if (isValid) {
                tr.setStartDate(endDate);
            }
        }
        if (view == null || view.getId() == R.id.requestEditStartLocationLayout) {
            textView = (TextView) findViewById(R.id.requestEditStartLocation);
            isValid &= this.validateTextField(textView);
            if (isValid) {
                // TODO : set start location on segment ?
            }
        }
        if (view == null || view.getId() == R.id.requestEditDestinationLayout) {
            textView = (TextView) findViewById(R.id.requestEditDestination);
            isValid &= this.validateTextField(textView);
            if (isValid) {
                // TODO : set destination on segment ?
            }
        }
        if (view == null || view.getId() == R.id.requestEditBusinessPurposeLayout) {
            textView = (TextView) findViewById(R.id.requestEditBusinessPurpose);
            isValid &= this.validateTextField(textView);
            if (isValid) {
                tr.setPurpose(textView.getText().toString());
            }
        }
        if (view == null || view.getId() == R.id.requestEditCommentLayout) {
            textView = (TextView) findViewById(R.id.requestEditComment);
            if (commentCFF != null && commentCFF.isRequired()) {
                isValid &= this.validateTextField(textView);
                if (isValid) {
                    tr.setLastComment(textView.getText().toString());
                }
            }
        }
        return isValid;
    }

    private boolean validateTextField(TextView textView) {
        if (textView.getText().length() == 0 || textView.getCurrentTextColor() == requiredColor) {
            textView.setTextColor(requiredColor);
            textView.setText("" + getResources().getString(R.string.tr_required));
            return false;
        }
        return true;
    }

    private void checkEmptyText(TextView textView) {
        if (textView.getCurrentTextColor() == requiredColor) {
            textView.setTextColor(Color.parseColor("#1d78bd"));
            textView.setText("");
        }
    }

    public boolean isCanSave() {
        return canSave;
    }

    public void setCanSave(boolean canSave) {
        this.canSave = canSave;
    }

}
