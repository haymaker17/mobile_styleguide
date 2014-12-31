package com.concur.mobile.core.expense.report.activity;

import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpStatus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.report.data.CarConfig;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.AccessType;
import com.concur.mobile.core.expense.report.service.CarConfigsRequest;
import com.concur.mobile.core.expense.report.service.DistanceToDateRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.InlineTextFormFieldView;

public abstract class AbstractExpenseEntryMileage extends ExpenseEntry {

    private static final String CLS_TAG = AbstractExpenseEntryMileage.class.getSimpleName();

    protected static final int DIALOG_CAR_CONFIG_PROGRESS = 3;
    protected static final int DIALOG_CAR_CONFIG_CANCELLED = 4;
    protected static final int DIALOG_CAR_CONFIG_FAILURE = 5;
    protected static final int DIALOG_CAR_CONFIG_NO_RATES = 6;
    protected static final int DIALOG_CAR_CONFIG_NO_CARS = 7;
    protected static final int DIALOG_DISTANCE_TO_DATE_PROGRESS = 8;
    protected static final int DIALOG_DISTANCE_TO_DATE_CANCELLED = 9;
    protected static final int DIALOG_DISTANCE_TO_DATE_FAILURE = 10;

    protected static final String CAR_CONFIGS_RECEIVER_KEY = "car.configs.receiver";
    protected static final String DISTANCE_TO_DATE_RECEIVER_KEY = "distance.to.date.receiver";

    // Members to allow quick access to various fields and view for rate/amount
    // updating
    protected ExpenseReportFormField amountField;
    protected TextView dtdView;

    protected CarConfig carConfig;

    // Reference to an outstanding request to retrieve car configs.
    protected CarConfigsRequest carConfigsRequest;

    protected CarConfigsReceiver carConfigsReceiver;
    protected final IntentFilter carConfigsFilter = new IntentFilter(Const.ACTION_EXPENSE_CAR_CONFIGS_UPDATED);

    // Reference to an outstanding request to retrieve distance to date.
    protected DistanceToDateRequest dtdRequest;

    protected DtdReceiver dtdReceiver;
    protected final IntentFilter dtdFilter = new IntentFilter(Const.ACTION_EXPENSE_DISTANCE_TO_DATE_RETRIEVED);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Make sure we have/can get a car config
        ConcurCore app = (ConcurCore) getApplication();

        // Call the non-build version so we can check editability now. The
        // rebuild version
        // will be called in super.onCreate()
        setExpenseReportWithoutBuildView(getIntent());

        // Restore any receivers.
        restoreReceivers();

        if (isReportEditable() && app.getCarConfigs() == null) {
            // Nope, go get them.
            if (ConcurCore.isConnected()) {
                // If 'carConfigsReceiver' was not restored above, then create a
                // new receiver
                // instance below, register it and send the request.
                if (carConfigsReceiver == null) {
                    // Register the receiver.
                    registerCarConfigsReceiver();
                    carConfigsRequest = app.getService().sendCarConfigsRequest();
                    if (carConfigsRequest != null) {
                        // Set the request on the receiver.
                        carConfigsReceiver.setRequest(carConfigsRequest);
                        showDialog(DIALOG_CAR_CONFIG_PROGRESS);
                    } else {
                        // Unregister the receiver.
                        unregisterCarConfigsReceiver();
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to create car configs request!");
                    }
                }
            }
        }

        super.onCreate(savedInstanceState);

        // We want our activity to finish after saving
        finishOnSave = true;

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save 'CarConfigsReceiver'.
        if (carConfigsReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            carConfigsReceiver.setActivity(null);
            // Add to the retainer
            retainer.put(CAR_CONFIGS_RECEIVER_KEY, carConfigsReceiver);
        }

        // Save 'DtdReceiver'
        if (dtdReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            dtdReceiver.setActivity(null);
            // Add to the retainer.
            retainer.put(DISTANCE_TO_DATE_RECEIVER_KEY, dtdReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    @Override
    protected void restoreReceivers() {
        super.restoreReceivers();
        // If an orientation change occurred, then the 'CarConfigsReceiver'
        // instance will be restored
        // in the few lines below.
        if (retainer != null) {
            if (retainer.contains(CAR_CONFIGS_RECEIVER_KEY)) {
                carConfigsReceiver = (CarConfigsReceiver) retainer.get(CAR_CONFIGS_RECEIVER_KEY);
                if (carConfigsReceiver != null) {
                    // Set the activity on the receiver.
                    carConfigsReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: retainer contains null reference for car configs receiver!");
                }
            }
            if (retainer.contains(DISTANCE_TO_DATE_RECEIVER_KEY)) {
                dtdReceiver = (DtdReceiver) retainer.get(DISTANCE_TO_DATE_RECEIVER_KEY);
                if (dtdReceiver != null) {
                    // Set the activity on the receiver.
                    dtdReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: retainer contains null reference for distance to date receiver!");
                }
            }
        }
    }

    @Override
    protected void buildView() {

        // Construct the filter and receiver.
        saveReportEntryFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_ENTRY_SAVE);

        // Instruct the window manager to only show the soft keyboard when the
        // end-user clicks on it.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Set the content view.
        setContentView(R.layout.expense_entry_mileage);

        // Configure the screen header.
        configureScreenHeader(expRep);

        // Configure the screen footer.
        configureScreenFooter();

        // Get the entry detail/form
        Intent origIntent = getIntent();
        String rpeKey = origIntent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);

        if (rpeKey != null) {
            // We're being passed in an existing entry to edit
            ExpenseReportEntry entry = expRepCache.getReportEntry(expRep, rpeKey);
            if (entry != null && entry instanceof ExpenseReportEntryDetail) {
                expRepEntDet = (ExpenseReportEntryDetail) entry;
            }
        } else {
            // This is a new entry. Get the form details cached by the calls
            // prior to this activity.
            ConcurCore app = (ConcurCore) getApplication();
            expRepEntDet = app.getCurrentEntryDetailForm();

            // And hide the submit button...
            Button button = (Button) findViewById(R.id.approve_button);
            button.setVisibility(View.INVISIBLE);
        }

        if (expRepEntDet != null && frmFldViewListener != null) {

            frmFldViewListener.setExpenseReportEntry(expRepEntDet);

            if (rpeKey != null) {
                // Set the expense entry title information.
                populateExpenseEntryTitleHeader();
            } else {
                // Hide the report entry header.
                View view = findViewById(R.id.expense_entry_title_header);
                if (view != null) {
                    view.setVisibility(View.GONE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate expense entry title header view!");
                }
                // Hide the header separator.
                view = findViewById(R.id.header_separator);
                if (view != null) {
                    view.setVisibility(View.GONE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate entry title header separator!");
                }
            }

            // Set itemizations/attendees.
            populateItemizationAttendeeReceipt();

            // Set the expense entry exceptions.
            populateExpenseEntryExceptions();

            // If a config request is outstanding then just don't build the
            // form. Too many complications
            if (carConfigsRequest == null) {

                // Grab a reference to any previously build form field views.
                List<FormFieldView> srcFrmFlds = null;
                if (frmFldViewListener != null && frmFldViewListener.getFormFieldViews() != null
                        && frmFldViewListener.getFormFieldViews().size() > 0) {
                    srcFrmFlds = frmFldViewListener.getFormFieldViews();
                }

                // Populate the form
                populateFormFields();

                // Grab a reference to any newly build form fields.
                List<FormFieldView> dstFrmFlds = null;
                if (frmFldViewListener != null && frmFldViewListener.getFormFieldViews() != null
                        && frmFldViewListener.getFormFieldViews().size() > 0) {
                    dstFrmFlds = frmFldViewListener.getFormFieldViews();
                }
                // Transfer any edited values from 'srcFrmFlds' to 'dstFrmFlds'
                // where they match on
                // field id and field type.
                if (srcFrmFlds != null && srcFrmFlds.size() > 0 && dstFrmFlds != null && dstFrmFlds.size() > 0) {
                    transferEditedValues(srcFrmFlds, dstFrmFlds);
                }

                // Restore any form field values.
                restoreFormFieldState();
            }

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: expense entry details not found!");
        }

    }

    /**
     * Sends a "distance to date" request for a specific car key and calendar date.
     * 
     * @param carKey
     *            contains the car key.
     * @param date
     *            contains the calendar date.
     */
    protected void sendDistanceToDateRequest(Integer carKey, Calendar date) {
        ConcurCore app = (ConcurCore) getApplication();
        // Register the receiver.
        registerDtdReceiver();
        Intent origIntent = getIntent();
        String excludeRpeKey = origIntent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
        dtdRequest = app.getService().sendDistanceToDateRequest(carKey, date, excludeRpeKey);
        if (dtdRequest != null) {
            // Set the request on the receiver.
            dtdReceiver.setRequest(dtdRequest);
            showDialog(DIALOG_DISTANCE_TO_DATE_PROGRESS);
        } else {
            // Unregister the receiver.
            unregisterDtdReceiver();
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendDistanceToDateRequest: unable to create DTD request!");
        }
    }

    @Override
    protected abstract void populateFormFields();

    /**
     * Given a wrapper view (typically the expense details layout), find the specific field label view for the given field ID
     * 
     * @param container
     * @param id
     * @return
     */
    protected View findLabelViewByTag(View container, String id) {
        View layout = container.findViewWithTag(id);
        if (layout != null) {
            return layout.findViewById(R.id.field_name);
        }
        return null;
    }

    /**
     * Given a wrapper view (typically the expense details layout), find the specific field value view for the given field ID
     * 
     * @param container
     * @param id
     * @return
     */
    protected View findValueViewByTag(View container, String id) {
        View retVal = null;
        if (frmFldViewListener != null && frmFldViewListener.getFormFieldViews() != null) {
            for (FormFieldView ffv : frmFldViewListener.getFormFieldViews()) {
                if (ffv.getFormField().getId() != null && ffv.getFormField().getId().equalsIgnoreCase(id)) {
                    int valueFieldId = R.id.field_value;
                    if (ffv instanceof InlineTextFormFieldView) {
                        valueFieldId = ffv.getViewFieldValueId();
                    }
                    if (ffv.view != null) {
                        retVal = ffv.view.findViewById(valueFieldId);
                    }
                }
            }
        }
        return retVal;
    }

    protected String buildDistanceUnitsLabel(String currentLabel) {

        String label = currentLabel;

        if (carConfig != null) {
            String units = getText(R.string.general_distance_miles).toString();
            if (CarConfig.DISTANCE_KM.equals(carConfig.distanceUnit)) {
                units = getText(R.string.general_distance_kilometers).toString();
            }

            if (!currentLabel.contains(units)) {
                StringBuilder sb = new StringBuilder(currentLabel);
                sb.append(" (").append(units).append(')');
                label = sb.toString();
            }

        }

        return label;
    }

    protected void updateDistanceUnitsLabel() {

        final String distanceId = "BusinessDistance";

        if (expRepEntDet != null && frmFldViewListener != null) {
            // These should never be null but some paranoia code arising from a
            // busted server
            // in RQA3. No reason to crash the clients.
            ExpenseReportFormField distanceField = expRepEntDet.findFormFieldByFieldId(distanceId);
            FormFieldView distanceFFV = frmFldViewListener.findFormFieldViewById(distanceId);

            if (distanceField != null && distanceFFV != null) {

                String newLabel = buildDistanceUnitsLabel(distanceField.getLabel());

                ViewGroup viewGroup = (ViewGroup) findViewById(R.id.expense_entry_details);
                if (viewGroup != null) {
                    TextView labelView = (TextView) findLabelViewByTag(viewGroup, distanceId);
                    if (labelView != null) {
                        labelView.setText(distanceFFV.buildLabel(newLabel));
                    }
                }
            }
        }
    }

    protected abstract void setCarConfig();

    protected abstract void setCarRate();

    protected abstract Integer getCarKey();

    @Override
    protected String[] getHardStopFieldIds() {
        return null;
    }

    @Override
    protected void sendSaveRequest() {

        // The amount field is read-only for mileage on the client. However, if
        // it is read-only
        // when it gets to the server then the server will ignore it.
        // We set it to read-write before the save so that the server picks it
        // up.
        // However, we have to do it here (after the form field commits but
        // before the request is built)
        // because changing a field from RO to RW generates a type cast problem
        // when the form field
        // code tries to (unnecessarily in this case) read the value from the
        // field.

        if (amountField != null) { // paranoia
            amountField.setAccessType(AccessType.RW);
        }

        super.sendSaveRequest();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;

        if (frmFldViewListener != null && frmFldViewListener.isCurrentFormFieldViewSet()
                && id >= FormFieldView.DIALOG_ID_BASE) {
            dlg = frmFldViewListener.getCurrentFormFieldView().onCreateDialog(id);
        } else {

            switch (id) {
            case DIALOG_CAR_CONFIG_PROGRESS: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(this.getText(R.string.retrieve_car_configs));
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                dialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // Cancel the request.
                        if (carConfigsRequest != null) {
                            carConfigsRequest.cancel();
                        }

                        // Unregister the receiver and clear the reference to
                        // the receiver.
                        if (carConfigsReceiver != null) {
                            getApplicationContext().unregisterReceiver(carConfigsReceiver);
                            carConfigsReceiver = null;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".onCreateDialog.CarConfigProgressDialog.onCancel: null car config receiver!");
                        }

                        // Gotta bail. Can't be here without rates.
                        showDialog(DIALOG_CAR_CONFIG_CANCELLED);
                    }
                });
                dlg = dialog;
                break;
            }
            case DIALOG_CAR_CONFIG_CANCELLED: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setMessage(getText(R.string.dlg_car_rates_cancelled));
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        dialog.dismiss();
                        finish();
                    }
                });
                return dlgBldr.create();
            }
            case DIALOG_CAR_CONFIG_FAILURE: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setMessage(getText(R.string.dlg_car_rates_failed));
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        dialog.dismiss();
                        finish();
                    }
                });
                return dlgBldr.create();
            }
            case DIALOG_CAR_CONFIG_NO_RATES: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setMessage(getText(R.string.dlg_car_rates_no_personal));
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        dialog.dismiss();
                        finish();
                    }
                });
                return dlgBldr.create();
            }
            case DIALOG_CAR_CONFIG_NO_CARS: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setMessage(getText(R.string.dlg_car_rates_no_cars));
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        dialog.dismiss();
                        finish();
                    }
                });
                return dlgBldr.create();
            }
            case DIALOG_DISTANCE_TO_DATE_PROGRESS: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(this.getText(R.string.retrieve_distance_to_date));
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                dialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // Cancel the request.
                        if (dtdRequest != null) {
                            dtdRequest.cancel();
                        }

                        // Unregister the receiver and clear the reference to
                        // the receiver.
                        if (dtdReceiver != null) {
                            getApplicationContext().unregisterReceiver(dtdReceiver);
                            dtdReceiver = null;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".onCreateDialog.DistanceToDateProgressDialog.onCancel: null DTD receiver!");
                        }

                        // Gotta bail. Can't be here without rates.
                        showDialog(DIALOG_DISTANCE_TO_DATE_CANCELLED);
                    }
                });
                dlg = dialog;
                break;
            }
            case DIALOG_DISTANCE_TO_DATE_CANCELLED: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setMessage(getText(R.string.dlg_distance_to_date_cancelled));
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        dialog.dismiss();
                        finish();
                    }
                });
                return dlgBldr.create();
            }
            case DIALOG_DISTANCE_TO_DATE_FAILURE: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setMessage(getText(R.string.dlg_distance_to_date_failed));
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        dialog.dismiss();
                        finish();
                    }
                });
                return dlgBldr.create();
            }
            default: {
                dlg = super.onCreateDialog(id);
            }
            }
        }

        return dlg;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (frmFldViewListener != null && frmFldViewListener.isCurrentFormFieldViewSet()) {
            frmFldViewListener.getCurrentFormFieldView().onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (frmFldViewListener != null && frmFldViewListener.isCurrentFormFieldViewSet()
                && id >= FormFieldView.DIALOG_ID_BASE) {
            frmFldViewListener.getCurrentFormFieldView().onPrepareDialog(id, dialog);
        } else {
            super.onPrepareDialog(id, dialog);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuSave) {
            save();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected IntentFilter getBroadcastReceiverIntentFilter() {
        return null;
    }

    @Override
    protected abstract int getHeaderNavBarTitleResourceId();

    @Override
    protected boolean isDetailReportRequired() {
        return false;
    }

    @Override
    protected boolean shouldReceiveDataEvents() {
        return false;
    }

    /**
     * Will register an instance of <code>CarConfigsReceiver</code> with the application context and set the 'carConfigsReceiver'
     * member.
     */
    protected void registerCarConfigsReceiver() {
        if (carConfigsReceiver == null) {
            carConfigsReceiver = new CarConfigsReceiver(this);
            getApplicationContext().registerReceiver(carConfigsReceiver, carConfigsFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerCarConfigsReceiver: carConfigsReceiver is *not* null!");
        }
    }

    /**
     * Will unregister the instance of <code>CarConfigsReceiver</code> with the application context and set the
     * 'carConfigsReceiver' member to <code>null</code>.
     */
    protected void unregisterCarConfigsReceiver() {
        if (carConfigsReceiver != null) {
            getApplicationContext().unregisterReceiver(carConfigsReceiver);
            carConfigsReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterCarConfigsReceiver: carConfigsReceiver is null!");
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling the result of retrieving the car config list.
     */
    static class CarConfigsReceiver extends BroadcastReceiver {

        // A reference to the mileage activity.
        private AbstractExpenseEntryMileage activity;

        // Contains the request for which this receiver is waiting on a
        // response.
        CarConfigsRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        CarConfigsReceiver(AbstractExpenseEntryMileage activity) {
            this.activity = activity;
        }

        void setActivity(AbstractExpenseEntryMileage activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.carConfigsRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        void setRequest(CarConfigsRequest request) {
            this.request = request;
        }

        @Override
        public void onReceive(Context context, Intent i) {

            // Does this receiver have a current activity?
            if (activity != null) {
                // Unregister this receiver.
                activity.unregisterCarConfigsReceiver();

                boolean requestFailed = true;
                int serviceRequestStatus = i.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = i.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (i.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                    requestFailed = false;
                                } else {
                                    activity.actionStatusErrorMessage = i.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage + ".");
                                }
                            } else {
                                activity.lastHttpErrorMessage = i.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage + ".");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + i.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                if (requestFailed) {
                    activity.showDialog(DIALOG_CAR_CONFIG_FAILURE);
                } else {
                    // Reset the search request object.
                    activity.carConfigsRequest = null;

                    // Reset this request object.
                    request = null;

                    // Rebuild the form
                    activity.buildView();
                }

                try {
                    activity.removeDialog(DIALOG_CAR_CONFIG_PROGRESS);
                } catch (IllegalArgumentException ilaExc) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: removeDialog: ", ilaExc);
                }

            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = i;
            }
        }
    }

    protected void registerDtdReceiver() {
        if (dtdReceiver == null) {
            dtdReceiver = new DtdReceiver(this);
            getApplicationContext().registerReceiver(dtdReceiver, dtdFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerDtdReceiver: dtdReceiver is *not* null!");
        }
    }

    protected void unregisterDtdReceiver() {
        if (dtdReceiver != null) {
            getApplicationContext().unregisterReceiver(dtdReceiver);
            dtdReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterCarConfigsReceiver: dtdReceiver is null!");
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling the result of retrieving the distance to date.
     */
    static class DtdReceiver extends BroadcastReceiver {

        // A reference to the mileage activity.
        private AbstractExpenseEntryMileage activity;

        // Contains the request for which this receiver is waiting on a
        // response.
        DistanceToDateRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        DtdReceiver(AbstractExpenseEntryMileage activity) {
            this.activity = activity;
        }

        void setActivity(AbstractExpenseEntryMileage activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.dtdRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        void setRequest(DistanceToDateRequest request) {
            this.request = request;
        }

        @Override
        public void onReceive(Context context, Intent i) {

            // Does this receiver have a current activity?
            if (activity != null) {
                // Unregister this receiver.
                activity.unregisterDtdReceiver();

                boolean requestFailed = true;
                int serviceRequestStatus = i.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = i.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (i.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                    requestFailed = false;
                                } else {
                                    activity.actionStatusErrorMessage = i.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage + ".");
                                }
                            } else {
                                activity.lastHttpErrorMessage = i.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage + ".");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + i.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                if (requestFailed) {
                    activity.showDialog(DIALOG_DISTANCE_TO_DATE_FAILURE);
                } else {
                    // Reset the search request object.
                    activity.dtdRequest = null;

                    // Reset this request object.
                    request = null;

                    // Update the distance
                    int newDtd = i.getIntExtra(Const.EXTRA_EXPENSE_DISTANCE_TO_DATE, 0);
                    if (activity.carConfig != null) {
                        Integer carKey = activity.getCarKey();
                        if (carKey != null) {
                            activity.carConfig.updateDistanceToDate(carKey, newDtd);
                        } else {
                            activity.carConfig.updateDistanceToDate(newDtd);
                        }
                        if (activity.dtdView != null) {
                            activity.dtdView.setText(Integer.toString(newDtd));
                        }
                        activity.setCarRate();
                    }
                }

                try {
                    // Remove the dialog.
                    activity.removeDialog(DIALOG_DISTANCE_TO_DATE_PROGRESS);
                } catch (IllegalArgumentException ilaExc) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: removeDialog: ", ilaExc);
                }

            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = i;
            }
        }
    }

}
