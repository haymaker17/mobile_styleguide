/**
 * Copyright (c) 2012 Concur Technologies, Inc.
 */
package com.concur.mobile.core.expense.report.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.report.data.CarConfig;
import com.concur.mobile.core.expense.report.data.CarDetail;
import com.concur.mobile.core.expense.report.data.CarRate;
import com.concur.mobile.core.expense.report.data.CarRateType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.AccessType;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.DatePickerFormFieldView;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.FormFieldViewListener;
import com.concur.mobile.core.view.SearchListFormFieldView;
import com.concur.mobile.core.view.StaticPickListFormFieldView;

/**
 * 
 * @author Chris N. Diaz
 * 
 */
public class ExpenseEntryCompanyMileage extends AbstractExpenseEntryMileage {

    private static final String CLS_TAG = ExpenseEntryCompanyMileage.class.getSimpleName();

    private static final int DIALOG_CAR_CONFIG_NO_RATES_GENERAL = 11;
    private static final int DIALOG_CAR_CONFIG_NO_RATES_FOR_DATE = 12;
    private static final int DIALOG_CAR_CONFIG_NO_RATES_FOR_CAR = 13;
    private static final int DIALOG_INVALID_ODOMETER_VALUE = 14;
    private static final int DIALOG_INVALID_DISTANCE = 15;

    // Only allow user to input a billion miles in the odometer.
    private static final InputFilter[] MAX_NUMBER_LENGTH = new InputFilter[] { new InputFilter.LengthFilter(10) };

    private static String[] HARD_STOP_FIELD_IDS = { "BusinessDistance" };

    private ExpenseReportFormField dtdField;
    private FormFieldView amountFFV;
    private TextView amountView;
    private EditText busDistanceView;
    private EditText perDistanceView;
    private EditText totalDistanceView;
    private DatePickerFormFieldView dateFFV;
    private FormFieldView vehicleFFV;
    private EditText odometerStartView;
    private EditText odometerEndView;
    private TextWatcher totalDistanceTextWatcher;
    protected CarRate passengerRate;
    private boolean isNoReimbursement;
    protected EditText passengerView;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.activity.AbstractExpenseEntryMileage#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Flurry Notification.
        Intent intent = getIntent();
        String expRepEntryKey = intent.getExtras().getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
        if (expRepEntryKey == null || expRepEntryKey.trim().length() == 0) {
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_COMPANY);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_CAR_MILEAGE, Flurry.EVENT_NAME_CREATE, params);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.activity.AbstractExpenseEntryMileage#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog dlg = null;

        // Check whether there a form field view should handle the dialog creation.
        if (frmFldViewListener != null && frmFldViewListener.isCurrentFormFieldViewSet()
                && id >= FormFieldView.DIALOG_ID_BASE) {

            dlg = frmFldViewListener.getCurrentFormFieldView().onCreateDialog(id);

        } else {

            // MOB-10648 - need to keep track of the dialogs being opened.
            dlg = dialogs.get(id);

            if (dlg == null) {

                switch (id) {
                case DIALOG_CAR_CONFIG_NO_RATES_GENERAL: {
                    AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                    dlgBldr.setMessage(getText(R.string.dlg_no_car_rates_general));
                    dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_CANCELED);
                            dialog.dismiss();
                        }
                    });
                    dlg = dlgBldr.create();
                    break;
                }
                case DIALOG_CAR_CONFIG_NO_RATES_FOR_DATE: {
                    AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                    dlgBldr.setMessage(getText(R.string.dlg_no_car_rates_for_date));
                    dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_CANCELED);
                            dialog.dismiss();
                        }
                    });
                    dlg = dlgBldr.create();
                    break;
                }
                case DIALOG_CAR_CONFIG_NO_RATES_FOR_CAR: {
                    AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                    dlgBldr.setMessage(getText(R.string.dlg_no_car_rates_for_car));
                    dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_CANCELED);
                            dialog.dismiss();
                        }
                    });
                    dlg = dlgBldr.create();
                    break;
                }
                case DIALOG_INVALID_ODOMETER_VALUE: {
                    AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                    String odomStart = expRepEntDet.findFormFieldByFieldId("OdometerStart").getLabel();
                    String odomEnd = expRepEntDet.findFormFieldByFieldId("OdometerEnd").getLabel();
                    String msg = Format.localizeText(ExpenseEntryCompanyMileage.this,
                            R.string.dlg_com_car_mileage_invalid_odom, odomStart, odomEnd);
                    dlgBldr.setMessage(msg);
                    dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_CANCELED);
                            dialog.dismiss();
                        }
                    });
                    dlg = dlgBldr.create();
                    break;
                }
                case DIALOG_INVALID_DISTANCE: {
                    AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                    String totalDist = expRepEntDet.findFormFieldByFieldId("TotalDistance").getLabel();
                    String msg = Format.localizeText(ExpenseEntryCompanyMileage.this,
                            R.string.dlg_com_car_mileage_invalid_distance, totalDist);
                    dlgBldr.setMessage(msg);
                    dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_CANCELED);
                            dialog.dismiss();
                        }
                    });
                    dlg = dlgBldr.create();
                    break;
                }

                default: {
                    dlg = super.onCreateDialog(id);
                    break;
                }

                } // end-switch

                dialogs.put(id, dlg); // MOB-10648

            } // end-if
        }

        return dlg;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.activity.ExpenseEntry#createFormFieldViewListener()
     */
    @Override
    protected FormFieldViewListener createFormFieldViewListener() {
        return new CompanyMileageFormFieldViewListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Notice we're adding the form field listeners after restoring the
        // FormField state and values so we don't pre-maturely trigger any listeners.
        addFormFieldListeners();
    }

    /**
     * Adds the form field listeners so the distances and odometer readings are updated accordingly.
     */
    private void addFormFieldListeners() {

        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.expense_entry_details);
        if (viewGroup != null) {

            // Date listener
            TextView dateView = (TextView) findValueViewByTag(viewGroup, "TransactionDate");
            if (dateView != null) {
                dateView.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        boolean hasRate = hasVariableCarRate(null);
                        if (hasRate == false && !isNoReimbursement) {
                            showDialog(DIALOG_CAR_CONFIG_NO_RATES_FOR_DATE);
                        } else {
                            setAmount(null, null);
                        }
                    }
                });
            }

            // Vehicle ID listener
            TextView vehicleView = (TextView) findValueViewByTag(viewGroup, "CarKey");
            if (vehicleView != null) {
                vehicleView.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        boolean hasRate = hasVariableCarRate(null);
                        if (hasRate == false && !isNoReimbursement) {
                            showDialog(DIALOG_CAR_CONFIG_NO_RATES_FOR_CAR);
                        } else {
                            setAmount(null, null);
                        }
                    }
                });
            }

            // Hook Odometer (Start) for changes
            if (odometerStartView != null) {
                odometerStartView.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        updateDistances(odometerStartView);
                        setAmount(null, null);
                    }
                });
            }

            // Odometer (End) listener
            if (odometerEndView != null) {
                odometerEndView.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        updateDistances(odometerEndView);
                        setAmount(null, null);
                    }
                });
            }

            // Total Distance listener
            if (totalDistanceView != null) {
                totalDistanceTextWatcher = new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        updateOdometer(s.toString());
                        updateDistances(totalDistanceView);
                        setAmount(null, null);
                    }
                };

                totalDistanceView.addTextChangedListener(totalDistanceTextWatcher);
            }

            // Business Distance listener
            if (busDistanceView != null) {
                busDistanceView.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        updateDistances(busDistanceView);
                        setAmount(s.toString(), null);
                    }
                });
            }

            // Personal Distance listener
            if (perDistanceView != null) {
                perDistanceView.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        updateDistances(perDistanceView);
                        setAmount(null, s.toString());
                    }
                });
            }

            // Hook passenger count view for changes and recalc

            if (passengerView != null) {
                passengerView.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        setAmount(null, null, s.toString());
                    }
                });
            }

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.activity.AbstractExpenseEntryMileage#populateFormFields()
     */
    @Override
    protected void populateFormFields() {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.expense_entry_details);
        if (viewGroup != null) {

            // Hide some fields that may not be defined as hidden
            ArrayList<String> ignoreFields = new ArrayList<String>();
            ignoreFields.add("ExpKey");
            ignoreFields.add("PostedAmount");
            ignoreFields.add("ExchangeRate");

            // Set some default values
            ExpenseReportFormField transDate = expRepEntDet.findFormFieldByFieldId("TransactionDate");

            // We're specifically getting 'now' in the device local timezone because
            // we want to init the value to a local date.
            Calendar now = Calendar.getInstance();
            if (transDate.getValue() == null) {
                transDate.setValue(FormatUtil.XML_DF_LOCAL.format(now.getTime()));
            }

            ExpenseReportFormField distanceField = expRepEntDet.findFormFieldByFieldId("BusinessDistance");
            ExpenseReportFormField carField = expRepEntDet.findFormFieldByFieldId("CarKey");

            boolean sendDistanceToDateRequest = false;
            Integer dtdCarKey = null;

            // Flag to get us out if something isn't right
            boolean canContinue = true;

            if (isReportEditable()) {
                setCarConfig();

                boolean hasRate = false;
                Integer carKey = null;

                if (carConfig != null && carConfig.configType.equals(CarConfig.TYPE_COM_FIX) || carConfig != null
                        && carConfig.configType.equals(CarConfig.TYPE_COM_FULL)) {

                    if (carField != null) {
                        String carLiKey = carField.getLiKey();
                        if (carLiKey != null) {
                            carKey = Integer.decode(carLiKey);
                        }

                        if (carKey == null && carConfig != null) {
                            CarDetail car = carConfig.getPreferredCar();
                            if (car == null) {
                                // Just use the first car. We have to have some car.
                                car = carConfig.getFirstCar();
                            }

                            if (car != null) {
                                carKey = car.key;
                            }
                        }
                    }

                    // Check if this user's CarConfig has any rates.
                    hasRate = hasVariableCarRate(carKey);
                    passengerRate = CarConfig.findCompanyVariableRate(carConfig, now, carKey,
                            CarRateType.TYPE_COM_FIX_PAS, 0);

                    // And fire off a distance to date update request
                    // The distance can change from other client (mobile and non-mobile) actions
                    // so we update frequently to stay correct.
                    if (dtdReceiver == null && carKey != null) {
                        sendDistanceToDateRequest = true;
                        dtdCarKey = carKey;
                    }

                } else {
                    carKey = -1; // Just to get us past the check below
                }

                // If no rate has been set or there is no car (and there isn't an outstanding config request)
                // then we cannot continue. Get out.
                if (carConfigsRequest == null) {
                    if (carKey == null || (carKey > -1 && hasRate == false)) {

                        // This might be a No Reimbursement config.
                        // Check if the CarConfig has CarDetails w/o rates.
                        if (carConfig.getPreferredCar() != null || carConfig.getFirstCar() != null) {

                            // This is a No Reimbursement CarConfig, hide some fields
                            isNoReimbursement = true;
                            ignoreFields.add("TransactionCurrencyName");
                            ignoreFields.add("TransactionAmount");

                        } else {
                            showDialog(DIALOG_CAR_CONFIG_NO_CARS);
                            // The dialog will finish us.
                            canContinue = false;
                        }
                    } else if (hasRate == false) {
                        showDialog(DIALOG_CAR_CONFIG_NO_RATES);
                        // The dialog will finish us.
                        canContinue = false;
                    }
                }
            }

            if (canContinue) {

                // Set the amount to be read-only because it is not directly editable on the client
                // When we go to save we will need to set the field back to read-write because
                // the server will ignore the field if it is read-only
                // Tweak the label as well
                amountField = expRepEntDet.findFormFieldByFieldId("TransactionAmount");
                amountField.setAccessType(AccessType.RO);
                amountField.setLabel(getText(R.string.amount).toString());
                amountField.setVerifyValue(false); // MOB-10638 - Company Car Mileage can have 0 amount value.

                // Do some variable specific setup
                if (carConfig != null && CarConfig.TYPE_COM_FIX.equals(carConfig.configType) || carConfig != null
                        && CarConfig.TYPE_COM_FULL.equals(carConfig.configType)) {

                    if (isReportEditable()) {
                        // Set the static list for vehicles
                        if (carField != null) {
                            carField.setStaticList(carConfig.getCarItems());
                        }

                        // Default the vehicle to the preferred one if it is not already set
                        if (carField != null && carField.getLiKey() == null) {
                            CarDetail car = carConfig.getPreferredCar();
                            if (car == null) {
                                // Just use the first car. We have to have a car for a rate.
                                car = carConfig.getFirstCar();
                            }

                            if (car != null) {
                                carField.setLiKey(Integer.toString(car.key));
                                carField.setValue(car.vehicleId);
                            }
                        }
                    }

                    // Set the DistanceToDate field value
                    dtdField = expRepEntDet.findFormFieldByFieldId("DistanceToDate");
                    if (dtdField != null) {
                        String carLiKey = carField.getLiKey();
                        Integer carKey;
                        if (carLiKey == null || carLiKey.trim().length() == 0) {
                            carKey = null;
                        } else {
                            carKey = Integer.parseInt(carLiKey);
                        }
                        long dtd = carConfig.getDistanceToDate(carKey);
                        dtdField.setValue(Long.toString(dtd));
                    }
                }

                if (!isReportEditable()) {
                    populateViewWithFields(viewGroup, expRepEntDet.getFormFields(), ignoreFields);
                } else {

                    List<FormFieldView> frmFldViews = populateViewWithFormFields(viewGroup,
                            expRepEntDet.getFormFields(), ignoreFields);

                    if (frmFldViews != null && frmFldViews.size() > 0) {
                        if (frmFldViewListener != null) {
                            frmFldViewListener.setFormFieldViews(frmFldViews);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: frmFldViewListener is null!");
                        }
                    }

                    // Get the DTD view and save it for use when DTD updates
                    dtdView = (TextView) findValueViewByTag(viewGroup, "DistanceToDate");

                    // Get the amount view and save it for use when distance changes
                    amountView = (TextView) findValueViewByTag(viewGroup, "TransactionAmount");
                    amountFFV = frmFldViewListener.findFormFieldViewById("TransactionAmount");

                    // Get the date view and hook it for changes.
                    dateFFV = (DatePickerFormFieldView) frmFldViewListener.findFormFieldViewById("TransactionDate");

                    // Hook vehicle ID view for changes and recalc
                    vehicleFFV = frmFldViewListener.findFormFieldViewById("CarKey");

                    // Get a reference to the odometer start so we can
                    // update it automatically (when other fields change).
                    odometerStartView = (EditText) findValueViewByTag(viewGroup, "OdometerStart");
                    if (odometerStartView != null) {
                        odometerStartView.setFilters(MAX_NUMBER_LENGTH);
                    }

                    passengerView = (EditText) findValueViewByTag(viewGroup, "PassengerCount");

                    boolean hasRate = hasVariableCarRate(null);
                    Integer carKey = getCarKey();

                    // MOB-11295 Get the first/currently selected Vehicle and set the odometer start.
                    // MOB-11895 But only if there isn't already an odometer start value present (i.e. it's a new entry)
                    Intent intent = getIntent();
                    String expRepEntryKey = intent.getExtras().getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
                    if (hasRate && !isNoReimbursement && (expRepEntryKey == null)) {
                        if (carKey != null && carConfig != null) {
                            CarDetail carDetail = carConfig.getCarDetail(carKey);
                            if (carDetail != null && odometerStartView != null) {
                                if (carDetail.odometerStart != null) {
                                    odometerStartView.setText(Long.toString(carDetail.odometerStart));
                                } else {
                                    odometerStartView.setText("0");
                                }
                            }
                        }
                    }

                    // Hook Odometer (End) for changes
                    odometerEndView = (EditText) findValueViewByTag(viewGroup, "OdometerEnd");
                    if (odometerEndView != null) {
                        odometerEndView.setFilters(MAX_NUMBER_LENGTH);
                    }
                    // Get the Total Distance view and hook it for calculations
                    totalDistanceView = (EditText) findValueViewByTag(viewGroup, "TotalDistance");
                    if (totalDistanceView != null) {
                        totalDistanceView.setFilters(MAX_NUMBER_LENGTH);
                    }

                    // Get the Business Distance view and hook it for calculations
                    busDistanceView = (EditText) findValueViewByTag(viewGroup, "BusinessDistance");
                    if (busDistanceView != null) {
                        busDistanceView.setFilters(MAX_NUMBER_LENGTH);
                    }

                    // Get the Personal Distance view and hook it for calculations
                    perDistanceView = (EditText) findValueViewByTag(viewGroup, "PersonalDistance");
                    if (perDistanceView != null) {
                        perDistanceView.setFilters(MAX_NUMBER_LENGTH);
                    }
                }

                if (sendDistanceToDateRequest && dtdCarKey != null) {
                    Calendar transDateCal = now;
                    if (dateFFV != null) {
                        transDateCal = dateFFV.getCalendar();
                    }
                    sendDistanceToDateRequest(dtdCarKey, transDateCal);
                }

            } // canContinue

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: expense entry details view group not found!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.activity.AbstractExpenseEntryMileage#setCarConfig()
     */
    @Override
    protected void setCarConfig() {
        ConcurCore app = (ConcurCore) getApplication();
        ArrayList<CarConfig> configs = app.getCarConfigs();
        if (configs != null) {
            carConfig = CarConfig.findCompanyConfig(configs);
            updateDistanceUnitsLabel();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.activity.AbstractExpenseEntryMileage#setCarRate()
     */
    @Override
    protected void setCarRate() {
        // No need to do anything here since CCM will have variable car rates,
        // thus, the rate and amount will be determined on-the-fly during calcualtion.

        // NOTE: 'setCarRate' is called by 'DtDReceiver.onReceive' once an update to DtD occurs.
        setAmount(null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.activity.AbstractExpenseEntryMileage#getCarKey()
     */
    @Override
    protected Integer getCarKey() {
        Integer retVal = null;
        try {
            if (vehicleFFV != null) {
                String carKeyStr = vehicleFFV.getCurrentValue();
                retVal = Integer.decode(carKeyStr);
            }
        } catch (NumberFormatException numFormExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getCarKey: number format exception parsing key.", numFormExc);
        }
        return retVal;
    }

    private boolean hasVariableCarRate(Integer carKey) {

        Calendar cal;
        if (dateFFV != null) {
            cal = dateFFV.getCalendar();
        } else {
            // We're specifically getting 'now' in the device local timezone because then
            // we compare it with rate dates that should represent local time.
            cal = Calendar.getInstance();
        }

        if (carKey == null) {
            if (vehicleFFV != null) {
                carKey = Integer.decode(vehicleFFV.getCurrentValue());
            }
        }

        return CarConfig.hasVariableRate(carConfig, cal, carKey, CarRateType.TYPE_COM_FIX_BUS);
    }

    /**
     * Gets the Personal CarRate.
     * 
     * @return the Personal Car Rate, or <code>null</code> if none is found.
     */
    private CarRate getPersonalCarRate() {
        // Find the new rate
        Calendar cal;
        if (dateFFV != null) {
            cal = dateFFV.getCalendar();
        } else {
            // We're specifically getting 'now' in the device local timezone because then
            // we compare it with rate dates that should represent local time.
            cal = Calendar.getInstance();
        }

        Integer carKey = null;
        if (vehicleFFV != null) {
            carKey = Integer.decode(vehicleFFV.getCurrentValue());
        }

        long newDistance = 0L;

        // Get the Personal distance.
        if (perDistanceView != null) {
            String dst = perDistanceView.getText().toString();
            if (dst.trim().length() > 0) {
                try {
                    newDistance = Long.parseLong(dst);
                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".setCarRate: number format exception parsing '" + dst + "'.", nfe);
                }
            }
        }

        return CarConfig.findCompanyVariableRate(carConfig, cal, carKey, CarRateType.TYPE_COM_FIX_PER, newDistance);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.activity.AbstractExpenseEntryMileage#getHeaderNavBarTitleResourceId()
     */
    @Override
    protected int getHeaderNavBarTitleResourceId() {
        return R.string.company_mileage_expense;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.activity.AbstractExpenseEntryMileage#getHardStopFieldIds()
     */
    @Override
    protected String[] getHardStopFieldIds() {
        return HARD_STOP_FIELD_IDS;
    }

    private void updateOdometer(String totalDistance) {

        if (totalDistance != null && totalDistance.trim().length() > 0) {

            if (odometerEndView != null) {

                // Get the start odometer values.
                long start = 0;
                if (odometerStartView != null && odometerStartView.getText().toString().trim().length() > 0) {
                    start = Long.parseLong(odometerStartView.getText().toString());
                }

                // The new Odometer End should be the sum of the Odometer Start
                // and the Total Distance value.
                long odomEnd = start + Long.parseLong(totalDistance);
                String odomEndStr = Long.toString(odomEnd);
                if (odomEnd > 0 && !odometerEndView.getText().toString().equals(odomEndStr)
                        && !odometerEndView.hasFocus()) {

                    odometerEndView.setText(odomEndStr);
                } else if (odomEnd <= 0 && !odometerEndView.getText().toString().equals("0")) {
                    odometerEndView.setText("0");
                }

            }
        }

    }

    private void updateDistances(TextView modifiedView) {

        // Get the start and end odometer values.
        long start = 0;
        long end = 0;
        if (odometerStartView != null && odometerStartView.getText().toString().trim().length() > 0) {
            start = Long.parseLong(odometerStartView.getText().toString());
        }
        if (odometerEndView != null && odometerEndView.getText().toString().trim().length() > 0) {
            end = Long.parseLong(odometerEndView.getText().toString());
        }

        // If the value is negative, set all the distances to 0.
        long total = end - start;
        if (total <= 0) {
            if (totalDistanceView != null && modifiedView != totalDistanceView
                    && !totalDistanceView.getText().toString().equals("0")) {
                totalDistanceView.setText("0");
            }
            if (busDistanceView != null && modifiedView != busDistanceView
                    && !busDistanceView.getText().toString().equals("0")) {
                busDistanceView.setText("0");
            }
            if (perDistanceView != null && modifiedView != perDistanceView
                    && !perDistanceView.getText().toString().equals("0")) {
                perDistanceView.setText("0");
            }
        } else {

            // Total Distance is the difference between OdometerStart and OdometerEnd
            String totalStr = Long.toString(total);
            if (totalDistanceView != null && modifiedView != totalDistanceView
                    && !totalDistanceView.getText().toString().equals(totalStr)) {

                totalDistanceView.setText(totalStr);
            }

            if (busDistanceView != null && modifiedView != busDistanceView) {

                // Distance (Business) is the difference between TotalDistance and Distance (Personal)
                long perDist = 0;
                if (perDistanceView != null && perDistanceView.getText().toString().trim().length() > 0) {

                    perDist = Long.parseLong(perDistanceView.getText().toString());
                }

                long busDist = total - perDist;
                String busDistStr = Long.toString(busDist);
                if (busDist > 0 && !busDistanceView.getText().toString().equals(busDistStr)) {

                    busDistanceView.setText(busDistStr);

                } else if (busDist <= 0 && !busDistanceView.getText().toString().equals("0")) {

                    busDistanceView.setText("0");
                }

            } else if (busDistanceView != null && modifiedView == busDistanceView) {

                // Need to update the Distance(Personal) if the Distance(Business) was modified.
                long busDist = 0;
                if (busDistanceView.getText().toString().trim().length() > 0) {
                    busDist = Long.parseLong(busDistanceView.getText().toString());
                }

                long perDist = total - busDist;
                String perDistStr = Long.toString(perDist);
                if (perDist > 0 && !perDistanceView.getText().toString().equals(perDistStr)) {

                    perDistanceView.setText(perDistStr);

                } else if (perDist <= 0 && !perDistanceView.getText().toString().equals("0")) {

                    perDistanceView.setText("0");
                }

            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.activity.AbstractExpenseActivity#save()
     */
    @Override
    protected void save() {

        // Check values before saving.
        if (odometerStartView != null && odometerEndView != null) {

            long odomStart = 0;
            long odomEnd = 0;

            if (odometerStartView.getText().toString().trim().length() > 0) {
                odomStart = Long.parseLong(odometerStartView.getText().toString());
            }
            if (odometerEndView.getText().toString().trim().length() > 0) {
                odomEnd = Long.parseLong(odometerEndView.getText().toString());
            }

            if (odomEnd < odomStart) {
                showDialog(DIALOG_INVALID_ODOMETER_VALUE);
                return;
            }
        }

        // Check if Business Distance is valid.
        if (busDistanceView != null && totalDistanceView != null) {

            long totalDist = 0;
            long busDist = 0;

            if (totalDistanceView.getText().toString().trim().length() > 0) {
                totalDist = Long.parseLong(totalDistanceView.getText().toString());
            }
            if (busDistanceView.getText().toString().trim().length() > 0) {
                busDist = Long.parseLong(busDistanceView.getText().toString());
            }

            if (totalDist < busDist) {
                showDialog(DIALOG_INVALID_DISTANCE);
                return;
            }
        }

        // Check if Personal Distance is valid.
        if (perDistanceView != null && totalDistanceView != null) {

            long totalDist = 0;
            long perDist = 0;

            if (totalDistanceView.getText().toString().trim().length() > 0) {
                totalDist = Long.parseLong(totalDistanceView.getText().toString());
            }
            if (perDistanceView.getText().toString().trim().length() > 0) {
                perDist = Long.parseLong(perDistanceView.getText().toString());
            }

            if (totalDist < perDist) {
                showDialog(DIALOG_INVALID_DISTANCE);
                return;
            }
        }

        super.save();
    }

    /*
     * Overloading method as we need to include Additional Passenger milage in amount
     */

    private void setAmount(String busDistanceString, String perDistanceString, String passengerString) {
        // If this is a No Remibursement config, then just return.
        if (isNoReimbursement) {
            return;
        }

        if (passengerString == null) {
            // Grab it from the view
            if (passengerView != null) {
                passengerString = passengerView.getText().toString();
            }
        }

        Calendar cal;
        if (dateFFV != null) {
            cal = dateFFV.getCalendar();
        } else {
            // We're specifically getting 'now' in the device local timezone because then
            // we compare it with rate dates that should represent local time.
            cal = Calendar.getInstance();
        }

        Integer carKey = null;
        if (vehicleFFV != null) {
            carKey = Integer.decode(vehicleFFV.getCurrentValue());
        }
        // Calculate the amount based on the Business Distance.
        double amount = 0.0;
        if (busDistanceString == null) {
            // Grab it from the view
            if (busDistanceView != null) {
                busDistanceString = busDistanceView.getText().toString();
            }
        }
        if (busDistanceString != null && busDistanceString.trim().length() > 0) {

            // MOB-10762 - Need to calculate the amount based on different distance intervals.
            long busDistance = Long.parseLong(busDistanceString.trim());

            amount = CarConfig.calculateVariableAmount(carConfig, cal, carKey, CarRateType.TYPE_COM_FIX_BUS,
                    busDistance);

        }

        // Now calculate the amount based on the Personal Distance.
        if (perDistanceString == null) {
            // Grab it from the view
            if (perDistanceView != null) {
                perDistanceString = perDistanceView.getText().toString();
            }
        }
        if (perDistanceString != null && perDistanceString.trim().length() > 0) {
            // Get the personal car rate.
            CarRate perCarRate = getPersonalCarRate();
            if (perCarRate != null) {
                double perDistance = Double.parseDouble(perDistanceString);
                amount += (perDistance * perCarRate.rate);
            }
        }

        // Get the passenger rate
        passengerRate = CarConfig.findCompanyVariableRate(carConfig, cal, carKey, CarRateType.TYPE_COM_FIX_PAS, 0);

        if (passengerRate != null && busDistanceString.trim().length() > 0) {

            long busDistance = Long.parseLong(busDistanceString.trim());
            int passengerCount = 0;
            if (passengerString != null && passengerString.trim().length() > 0) {
                passengerCount = Integer.decode(passengerString);
            }
            amount = amount + (busDistance * passengerRate.rate * passengerCount);
        }
        // Format the value for display.
        String displayValue = FormatUtil.formatAmount(amount,
                ConcurCore.getContext().getResources().getConfiguration().locale, expRepEntDet.transactionCrnCode,
                false);
        amountView.setText(displayValue);

        // Since the amount field is read-only, the value will not be updated by the
        // default save code. Keep the underlying amount value in sync here.
        // Format the value for the wire.
        String wireValue = amountFFV.formatValueForWire(displayValue);
        amountField.setValue(wireValue);

    }

    private void setAmount(String busDistanceString, String perDistanceString) {

        setAmount(busDistanceString, perDistanceString, null);
    }

    /**
     * An extension of <code>ExpenseFormFieldViewListener</code> to track changes to certain fields.
     */
    class CompanyMileageFormFieldViewListener extends ExpenseFormFieldViewListener {

        private final String CLS_TAG = ExpenseEntryCompanyMileage.class.getSimpleName() + "."
                + CompanyMileageFormFieldViewListener.class.getSimpleName();

        protected static final String CAR_FIELD_ID = "CarKey";
        protected static final String TRANSACTION_DATE_FIELD_ID = "TransactionDate";

        public CompanyMileageFormFieldViewListener(BaseActivity activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.core.expense.activity.ExpenseEntry.ExpenseFormFieldViewListener#valueChanged(com.concur.mobile.core
         * .util.FormFieldView )
         */
        @Override
        public void valueChanged(FormFieldView frmFldView) {
            super.valueChanged(frmFldView);

            // Detect a change to the car selection and update 'StartingOdometer' value.
            if (CAR_FIELD_ID.equalsIgnoreCase(frmFldView.getFormField().getId())) {
                Integer carKey = getSelectedCarKey();
                if (carKey != null && carConfig != null) {
                    CarDetail carDetail = carConfig.getCarDetail(carKey);
                    if (carDetail != null) {
                        // Update the odometer start value.
                        if (odometerStartView != null) {
                            if (carDetail.odometerStart != null) {
                                odometerStartView.setText(Long.toString(carDetail.odometerStart));
                            } else {
                                odometerStartView.setText("0");
                            }
                        }
                        // Update the Distance To Date field.
                        if (dtdField != null) {
                            String dtdStr = (carDetail.distanceToDate != null) ? Long
                                    .toString(carDetail.distanceToDate) : "0";
                            dtdField.setValue(dtdStr);
                        }
                        // Send a request to update distance to date.
                        sendDistanceToDateRequest(carKey, Calendar.getInstance());
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".valueChanged: unable to locate car detail for key!");
                    }
                }

            }

            // Detect a change to the transaction date field and fetch a distance to date value.
            if (TRANSACTION_DATE_FIELD_ID.equalsIgnoreCase(frmFldView.getFormField().getId())) {
                if (dateFFV != null) {
                    Calendar transDate = dateFFV.getCalendar();
                    Integer carKey = getSelectedCarKey();
                    if (carKey != null) {
                        // Send a request to update distance to date.
                        sendDistanceToDateRequest(carKey, transDate);
                    }
                }
            }
        }

        /**
         * Gets the currently selected car key.
         * 
         * @return returns the currently selected car key.
         */
        private Integer getSelectedCarKey() {
            Integer carKey = null;
            if (vehicleFFV != null) {
                String carKeyStr = null;
                if (vehicleFFV instanceof StaticPickListFormFieldView) {
                    StaticPickListFormFieldView pckLstFFV = (StaticPickListFormFieldView) vehicleFFV;
                    carKeyStr = pckLstFFV.getCurrentValue();
                } else if (vehicleFFV instanceof SearchListFormFieldView) {
                    SearchListFormFieldView srchLstFFV = (SearchListFormFieldView) vehicleFFV;
                    carKeyStr = srchLstFFV.getLiKey();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".getSelectedCarKey: expected static pick list or search list for car type field!");
                }
                if (carKeyStr != null && carKeyStr.length() > 0) {
                    try {
                        carKey = Integer.decode(carKeyStr);
                    } catch (NumberFormatException numFormExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".getSelectedCarKey: expected a numeric car key value!",
                                numFormExc);
                    }
                }
            }
            return carKey;
        }

    }

}
