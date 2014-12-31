package com.concur.mobile.core.expense.report.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
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

public class ExpenseEntryMileage extends AbstractExpenseEntryMileage {

    private static final String CLS_TAG = ExpenseEntryMileage.class.getSimpleName();

    protected static String[] HARD_STOP_FIELD_IDS = { "BusinessDistance" };

    protected ExpenseReportFormField dtdField;
    protected FormFieldView amountFFV;
    protected TextView amountView;
    protected EditText distanceView;
    protected DatePickerFormFieldView dateFFV;
    protected FormFieldView vehicleFFV;
    protected TextView passengerView;

    protected CarRate fixedRate;
    protected CarRate passengerRate;

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
            params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_PERSONAL);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_CAR_MILEAGE, Flurry.EVENT_NAME_CREATE, params);
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
            ignoreFields.add("TransactionCurrencyName");
            ignoreFields.add("PostedAmount");
            ignoreFields.add("ExchangeRate");
            ignoreFields.add("PatKey");

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

            if (isReportEditable()) {
                setCarConfig();

                Integer carKey = null;
                boolean hasVariableRate = false;

                if (carConfig != null && carConfig.configType.equals(CarConfig.TYPE_PER_VAR)) {
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

                    // Check if this user's CarConfig has rates at all.
                    hasVariableRate = CarConfig.hasVariableRate(carConfig, now, carKey, CarRateType.TYPE_PER_VAR_CAR);
                    passengerRate = CarConfig.findVariableRate(carConfig, now, carKey, CarRateType.TYPE_PER_VAR_PAS, 0);

                    // And fire off a distance to date update request
                    // The distance can change from other client (mobile and non-mobile) actions
                    // so we update frequently to stay correct.
                    if (dtdReceiver == null && carKey != null) {
                        sendDistanceToDateRequest(carKey, now);
                    }

                } else {
                    carKey = 1; // Just to get us past the check below
                    fixedRate = CarConfig.findFixedRate(carConfig, now);
                }

                // If no rate has been set or there is no car (and there isn't an outstanding config request)
                // then we cannot continue. Get out.
                if (carConfigsRequest == null) {
                    if (carKey == null) {
                        showDialog(DIALOG_CAR_CONFIG_NO_CARS);
                        // The dialog will finish us.
                    } else if (fixedRate == null && !hasVariableRate) {
                        showDialog(DIALOG_CAR_CONFIG_NO_RATES);
                        // The dialog will finish us.
                    }
                }
            }

            // Set the amount to be read-only because it is not directly editable on the client
            // When we go to save we will need to set the field back to read-write because
            // the server will ignore the field if it is read-only
            // Tweak the label as well
            amountField = expRepEntDet.findFormFieldByFieldId("TransactionAmount");
            amountField.setAccessType(AccessType.RO);
            amountField.setLabel(getText(R.string.amount).toString());

            // Do some personal variable specific setup
            if (carConfig != null && CarConfig.TYPE_PER_VAR.equals(carConfig.configType)) {
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

                List<FormFieldView> frmFldViews = populateViewWithFormFields(viewGroup, expRepEntDet.getFormFields(),
                        ignoreFields);

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

                // Get the distance view and hook it for calculations
                distanceView = (EditText) findValueViewByTag(viewGroup, "BusinessDistance");
                if (distanceView != null) {
                    distanceView.addTextChangedListener(new TextWatcher() {

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (carConfig != null && carConfig.configType.equals(CarConfig.TYPE_PER_VAR)) {
                                setCarRate();
                            }
                            setAmount(s.toString(), null);
                        }
                    });
                }

                // Get the date view and hook it for changes.
                dateFFV = (DatePickerFormFieldView) frmFldViewListener.findFormFieldViewById("TransactionDate");
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
                            setCarRate();
                            setAmount(null, null);
                        }
                    });
                }

                // Hook vehicle ID view for changes and recalc
                vehicleFFV = frmFldViewListener.findFormFieldViewById("CarKey");
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
                            setCarRate();
                            setAmount(null, null);
                        }
                    });
                }

                // Hook passenger count view for changes and recalc
                passengerView = (TextView) findValueViewByTag(viewGroup, "PassengerCount");
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
                            setAmount(null, s.toString());
                        }
                    });
                }

            }

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
            carConfig = CarConfig.findPersonalConfig(configs);
            updateDistanceUnitsLabel();
        }
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.activity.AbstractExpenseEntryMileage#setCarRate()
     */
    @Override
    protected void setCarRate() {
        // Find the new rate
        Calendar cal;
        if (dateFFV != null) {
            cal = dateFFV.getCalendar();
        } else {
            // We're specifically getting 'now' in the device local timezone because then
            // we compare it with rate dates that should represent local time.
            cal = Calendar.getInstance();
        }

        if (carConfig != null && carConfig.configType.equals(CarConfig.TYPE_PER_ONE)) {
            fixedRate = CarConfig.findFixedRate(carConfig, cal);
        } else {
            Integer carKey = null;
            if (vehicleFFV != null) {
                carKey = Integer.decode(vehicleFFV.getCurrentValue());
            }

            // NOTE: Not getting the variable car rate; variable amount will just be calculated on-the-fly.
            passengerRate = CarConfig.findVariableRate(carConfig, cal, carKey, CarRateType.TYPE_PER_VAR_PAS, 0);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.activity.AbstractExpenseEntryMileage#getHeaderNavBarTitleResourceId()
     */
    @Override
    protected int getHeaderNavBarTitleResourceId() {
        return R.string.mileage_expense;
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

    protected void setAmount(String distanceString, String passengerString) {

        if (distanceString == null) {
            // Grab it from the view
            if (distanceView != null) {
                distanceString = distanceView.getText().toString();
            }
        }

        if (passengerString == null) {
            // Grab it from the view
            if (passengerView != null) {
                passengerString = passengerView.getText().toString();
            }
        }

        double amount = 0.0;

        if (distanceString != null && distanceString.trim().length() > 0) {
            int distance = Integer.parseInt(distanceString);

            if (carConfig.configType.equals(CarConfig.TYPE_PER_VAR)) {
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

                // Calculate variable car rate with distance limits.
                amount = CarConfig.calculateVariableAmount(carConfig, cal, carKey, CarRateType.TYPE_PER_VAR_CAR,
                        distance);

            } else if (fixedRate != null) {
                // Just use the fixed rate.
                amount = distance * fixedRate.rate;
            }

            if (passengerRate != null) {
                int passengerCount = 0;
                if (passengerString != null && passengerString.trim().length() > 0) {
                    passengerCount = Integer.decode(passengerString);
                }
                amount = amount + (distance * passengerRate.rate * passengerCount);
            }
        }

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

}
