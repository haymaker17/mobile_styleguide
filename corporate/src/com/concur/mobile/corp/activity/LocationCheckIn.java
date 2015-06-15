/**
 * Copyright (c) 2011 Concur Technologies, Inc.
 */
package com.concur.mobile.corp.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;
import com.concur.breeze.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.LocationCheckInRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.SpinnerItem;
import com.concur.mobile.corp.ConcurMobile;

/**
 * An extension of <code>BaseActivity</code> for allowing a user to "check in" their current location.
 * 
 * @author Chris N. Diaz
 * 
 */
public class LocationCheckIn extends BaseActivity implements OnClickListener {

    private static final String CLS_TAG = LocationCheckIn.class.getSimpleName();

    private final static String LOCATION_CHECK_IN_KEY = "location.check.in.receiver";

    // Keys used to save/retrieve state during screen orientation change.
    private final static String LOCATION = "LOCATION";
    private final static String DAYS_REMAINING = "DAYS_REMAINING";
    private final static String ASSISTANCE_REQ = "ASSISTANCE_REQ";
    private final static String COMMENT = "COMMENT";

    private final static int GETTING_LOCATION_DIALOG = 0;
    private final static int DIALOG_COMMENT = 1;
    private final static int DIALOG_DAYS_REMAINING = 2;
    private final static int DIALOG_ENTER_REQUIRED_FIELDS = 4;
    private final static int DIALOG_CHECK_IN_PROGRESS = 5;
    private final static int DIALOG_CHECK_IN_FAILED = 6;
    private final static int DIALOG_CHECK_IN_SUCCESS = 7;
    private final static int DIALOG_COULD_NOT_FIND_LOCATION = 8;

    // Right now, these values are hard-coded according to WIKI.
    private static final SpinnerItem[] daysRemainingItems;
    static {
        Context ctx = ConcurMobile.getContext();
        daysRemainingItems = new SpinnerItem[] { new SpinnerItem("-1", R.string.location_check_in_days_remaining_hour),
                new SpinnerItem("1", R.string.location_check_in_days_remaining_day),
                new SpinnerItem("2", ctx.getString(R.string.location_check_in_days_remaining_x_days, "2")),
                new SpinnerItem("3", ctx.getString(R.string.location_check_in_days_remaining_x_days, "3")),
                new SpinnerItem("4", ctx.getString(R.string.location_check_in_days_remaining_x_days, "4")),
                new SpinnerItem("5", ctx.getString(R.string.location_check_in_days_remaining_x_days, "5")),
                new SpinnerItem("6", ctx.getString(R.string.location_check_in_days_remaining_x_days, "6")),
                new SpinnerItem("7", R.string.location_check_in_days_remaining_week),
                new SpinnerItem("30", R.string.location_check_in_days_remaining_month) };
    }

    /**
     * Contains the intent filter used to register the location check in receiver.
     */
    protected IntentFilter locationCheckInFilter;
    /**
     * Contains a reference to the receiver to handle the result of checking in user's location.
     */
    protected LocationCheckInReceiver locationCheckInReceiver;
    /**
     * Contains a reference to an outstanding request to check in location.
     */
    protected LocationCheckInRequest checkInRequest;

    protected TextView currentLocationField;
    protected TextView daysRemainingField;
    protected CheckedTextView assistanceRequiredField;
    protected TextView commentField;
    protected Button checkInButton;

    protected SpinnerItem currentDaysRemaining;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.corp.activity.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_check_in);

        initUI();
        initFieldValues(savedInstanceState);

        // Restore any receivers.
        restoreReceivers();
    }

    /*
     * 
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;

        switch (id) {
        case GETTING_LOCATION_DIALOG: {

            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage(this.getText(R.string.dlg_getting_current_location));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    // If they cancel this dialog then we need to bail out of the entire activity.
                    finish();
                }
            });
            dlg = dialog;
            break;
        }
        case DIALOG_COULD_NOT_FIND_LOCATION: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.location_check_in_location_not_found_title);
            dlgBldr.setMessage(R.string.location_check_in_location_not_found_message);
            dlgBldr.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_CHECK_IN_PROGRESS: {

            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage(this.getText(R.string.location_check_in_progress));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dlg = dialog;
            break;
        }
        case DIALOG_CHECK_IN_FAILED: {

            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.location_check_in_failed_title);
            dlgBldr.setMessage(R.string.location_check_in_failed_message);
            dlgBldr.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_CHECK_IN_SUCCESS: {

            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.location_check_in_success_title);
            dlgBldr.setMessage(R.string.location_check_in_success_message);
            dlgBldr.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_DAYS_REMAINING: {

            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setCancelable(true);
            ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(this,
                    android.R.layout.simple_spinner_item, daysRemainingItems) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    return super.getDropDownView(position, convertView, parent);
                }
            };

            listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Get the currently selected item.
            int selectedItem = -1;
            if (currentDaysRemaining != null) {
                for (int i = 0; i < daysRemainingItems.length; i++) {
                    if (currentDaysRemaining.id.equals(daysRemainingItems[i].id)) {
                        selectedItem = i;
                        break;
                    }
                }
            }

            dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    currentDaysRemaining = daysRemainingItems[which];
                    daysRemainingField.setText(currentDaysRemaining.name);
                    removeDialog(DIALOG_DAYS_REMAINING);
                }
            });

            dlgBldr.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    removeDialog(DIALOG_DAYS_REMAINING);
                }
            });
            dlg = dlgBldr.create();

            break;
        }
        case DIALOG_COMMENT: {

            final EditText textEdit = new EditText(this);
            textEdit.setMinLines(3);
            textEdit.setMaxLines(3);
            textEdit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            textEdit.setText(commentField.getText());

            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(getText(R.string.comment));
            dlgBldr.setCancelable(true);
            dlgBldr.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    removeDialog(DIALOG_COMMENT);
                }
            });
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    removeDialog(DIALOG_COMMENT);
                    String comment = textEdit.getText().toString().trim();
                    commentField.setText(comment);
                }
            });
            dlgBldr.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    removeDialog(DIALOG_COMMENT);
                }
            });
            // TODO: Force to show soft-keyboard so user doesn't have to double-click.
            dlgBldr.setView(textEdit);
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_ENTER_REQUIRED_FIELDS: {

            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.location_check_in_failed_title);
            dlgBldr.setMessage(R.string.location_check_in_missing_fields);
            dlgBldr.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();

            break;
        }
        default: {
            ConcurMobile concurMobile = (ConcurMobile) getApplication();
            dlg = concurMobile.createDialog(this, id);
            break;
        }
        } // end-switch
        return dlg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    public void onClick(View v) {

        switch (v.getId()) {
        case R.id.location_check_in_current_loc: {
            if (ViewUtil.isMappingAvailable(this)) {
                ConcurMobile app = (ConcurMobile) getApplication();
                Location loc = app.getCurrentLocation();
                if (loc != null) {
                    // Launch the Map using the geo coordinates.
                    String geoUriString = "geo:" + loc.getLatitude() + "," + loc.getLongitude() + "?z=18";
                    Log.d(Const.LOG_TAG, CLS_TAG + ".onClick(): Launching Map activity with geoUri: " + geoUriString);

                    Uri geoUri = Uri.parse(geoUriString);
                    Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
                    startActivity(mapCall);
                }
            } else {
                Log.i(Const.LOG_TAG, CLS_TAG + ".onClick: no mapping application installed!");
            }
            break;
        }
        case R.id.location_check_in_days_remaining:
            showDialog(DIALOG_DAYS_REMAINING);
            break;
        case R.id.location_check_in_assistance_required:
            assistanceRequiredField.toggle();
            break;
        case R.id.location_check_in_comment:
            showDialog(DIALOG_COMMENT);
            break;
        case R.id.footer_button_one:
            if (!ConcurMobile.isConnected()) {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            } else {
                doCheckIn();
            }

            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.corp.activity.BaseActivity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        // Save the location.
        outState.putCharSequence(LOCATION, currentLocationField.getText());

        // Save the days remaining.
        outState.putSerializable(DAYS_REMAINING, currentDaysRemaining);

        // Save if assistance required.
        outState.putBoolean(ASSISTANCE_REQ, assistanceRequiredField.isChecked());

        // Save the comment.
        outState.putCharSequence(COMMENT, commentField.getText());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.corp.activity.BaseActivity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (retainer != null) {
            // Save the receiver
            if (locationCheckInReceiver != null) {
                // Clear the activity reference, it will be set in the 'onCreate' method.
                locationCheckInReceiver.setActivity(null);
                // Add to the retainer.
                retainer.put(LOCATION_CHECK_IN_KEY, locationCheckInReceiver);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.corp.activity.BaseActivity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    // ################## HELPER METHODS ############## //

    protected void restoreReceivers() {
        if (retainer != null) {
            // Restore 'LocationCheckInReceiver'
            if (retainer.contains(LOCATION_CHECK_IN_KEY)) {
                locationCheckInReceiver = (LocationCheckInReceiver) retainer.get(LOCATION_CHECK_IN_KEY);
                if (locationCheckInReceiver != null) {
                    // Set the activity on the receiver.
                    locationCheckInReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: retainer contains null reference for location check in receiver!");
                }
            }
        }
    }

    private void initFieldValues(Bundle sis) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".initFieldValues");

        if (sis == null) {
            // Initial value of Days Remaining should be "Not Sure".
            currentDaysRemaining = daysRemainingItems[0];
            daysRemainingField.setText(currentDaysRemaining.name);

            ConcurMobile app = (ConcurMobile) getApplication();
            Address addr = app.getCurrentAddress();
            int index = (addr != null) ? addr.getMaxAddressLineIndex() : -1;
            if (index == -1) {
                showDialog(DIALOG_COULD_NOT_FIND_LOCATION);
                return;
            } else {
                // Set the human-readable address.
                StringBuffer add = new StringBuffer();
                for (int i = 0; i < index; i++) {
                    add.append(addr.getAddressLine(i));
                    add.append(", ");
                }
                // Remove trailing comma.
                String s = add.toString().trim();
                s = (s.endsWith(",")) ? s.substring(0, s.length() - 1) : s;
                currentLocationField.setText(s);
            }

        } else {
            // Screen orientation was changed, need to set existing values.

            // Set the current location.
            currentLocationField.setText(sis.getCharSequence(LOCATION));

            // Set the days remaining.
            currentDaysRemaining = (SpinnerItem) sis.getSerializable(DAYS_REMAINING);
            daysRemainingField.setText(currentDaysRemaining.name);

            // Set if assistance is required.
            assistanceRequiredField.setChecked(sis.getBoolean(ASSISTANCE_REQ));

            // Set the comment.
            commentField.setText(sis.getCharSequence(COMMENT));
        }
    }

    private void initUI() {
        Log.d(Const.LOG_TAG, CLS_TAG + ".initUI");

        // Init header title.
        getSupportActionBar().setTitle(R.string.location_check_in_title);

        // Init fields:
        // Current location
        TextView currLocTitle = (TextView) findViewById(R.id.location_check_in_current_loc).findViewById(
                R.id.field_name);
        currLocTitle.setText(R.string.location_check_in_current_location);
        currentLocationField = (TextView) findViewById(R.id.location_check_in_current_loc).findViewById(
                R.id.field_value);
        findViewById(R.id.location_check_in_current_loc).setOnClickListener(this);

        // Days remaining
        TextView daysRemainingTitle = (TextView) findViewById(R.id.location_check_in_days_remaining).findViewById(
                R.id.field_name);
        daysRemainingTitle.setText(R.string.location_check_in_days_remaining);
        daysRemainingField = (TextView) findViewById(R.id.location_check_in_days_remaining).findViewById(
                R.id.field_value);
        findViewById(R.id.location_check_in_days_remaining).setOnClickListener(this);

        // Immediate assistance required
        assistanceRequiredField = (CheckedTextView) findViewById(R.id.location_check_in_assistance_required)
                .findViewById(R.id.field_name);
        assistanceRequiredField.setText(R.string.location_check_in_assistance_required);
        assistanceRequiredField.setChecked(false); // MOB-7207 default to disabled
        findViewById(R.id.location_check_in_assistance_required).setOnClickListener(this);

        // Comment
        TextView commentTitle = (TextView) findViewById(R.id.location_check_in_comment).findViewById(R.id.field_name);
        commentTitle.setText(R.string.location_check_in_comment);
        commentField = (TextView) findViewById(R.id.location_check_in_comment).findViewById(R.id.field_value);
        findViewById(R.id.location_check_in_comment).setOnClickListener(this);

        // Init footer.
        checkInButton = (Button) findViewById(R.id.footer_button_one);
        checkInButton.setText(R.string.location_check_in_button);
    }

    private void doCheckIn() {

        // First check if we have an address location.
        ConcurMobile app = (ConcurMobile) getApplication();
        Address addr = app.getCurrentAddress();

        if (addr == null) {
            Log.w(Const.LOG_TAG, CLS_TAG + ".doCheckIn() - currentLocationAddress is null!");
            showDialog(DIALOG_COULD_NOT_FIND_LOCATION);
            return;
        }

        // Validate the values (current location and days remaining are required).
        CharSequence currentLocation = currentLocationField.getText();
        if (currentLocation == null || currentLocation.length() == 0 || currentDaysRemaining == null) {

            Log.d(Const.LOG_TAG, CLS_TAG + ".doCheckIn: Missing required field(s).");
            showDialog(DIALOG_ENTER_REQUIRED_FIELDS);
            return;
        }

        // Make the call
        ConcurService svc = getConcurService();
        if (svc != null) {
            registerLocationCheckInReceiver();

            // Invoke actual MWS request.
            String assist = (assistanceRequiredField.isChecked() ? "Y" : "N");
            String comment = (commentField.getText() != null) ? commentField.getText().toString() : "";
            checkInRequest = svc.checkInCurrentLocation(addr, assist, currentDaysRemaining.id, comment);

            if (checkInRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".doCheckIn: unable to create 'LocationCheckInRequest' request!");
                unregisterLocationCheckInReceiver();
            } else {
                // Set the request object on the receiver.
                locationCheckInReceiver.setServiceRequest(checkInRequest);
                // Show the "Checking In..." progress dialog.
                showDialog(DIALOG_CHECK_IN_PROGRESS);
            }
        } else {
            Log.i(Const.LOG_TAG, CLS_TAG + ".doCheckIn: service is unavailable.");
        }
    }

    /**
     * Will register an instance of <code>LocationCheckInReceiver</code> with the application context and set the
     * <code>locationCheckInReceiver</code> attribute.
     */
    private void registerLocationCheckInReceiver() {
        if (locationCheckInReceiver == null) {
            locationCheckInReceiver = new LocationCheckInReceiver(this);
            if (locationCheckInFilter == null) {
                locationCheckInFilter = new IntentFilter(Const.ACTION_LOCATION_CHECK_IN);
            }
            getApplicationContext().registerReceiver(locationCheckInReceiver, locationCheckInFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerLocationCheckInReceiver: locationCheckInFilter is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>LocationCheckInReceiver</code> with the application context and set the
     * <code>locationCheckInReceiver</code> to <code>null</code>.
     */
    private void unregisterLocationCheckInReceiver() {
        if (locationCheckInReceiver != null) {
            getApplicationContext().unregisterReceiver(locationCheckInReceiver);
            locationCheckInReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterLocationCheckInReceiver: checkInReceiver is null!");
        }
    }

    // ############################################################# //
    // ####################### INNER CLASS ######################### //
    // ############################################################# //

    /**
     * Extension of <code>BaseBroadcastReceiver</code> for handling notification of the result checking in location.
     * 
     * @author Chris N. Diaz
     */
    static class LocationCheckInReceiver extends BaseBroadcastReceiver<LocationCheckIn, LocationCheckInRequest> {

        /**
         * Default constructor.
         * 
         * @param activity
         */
        protected LocationCheckInReceiver(LocationCheckIn activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver#setServiceRequest(com.concur.mobile.corp.service
         * .ServiceRequest )
         */
        @Override
        protected void setActivityServiceRequest(LocationCheckInRequest request) {
            activity.checkInRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver#clearActivityServiceRequest(com.concur.mobile.corp
         * .activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(LocationCheckIn activity) {
            activity.checkInRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver#unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterLocationCheckInReceiver();
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(DIALOG_CHECK_IN_PROGRESS);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver#handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            activity.setResult(Activity.RESULT_OK, new Intent());
            activity.showDialog(DIALOG_CHECK_IN_SUCCESS);

            // Flurry Notification
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_LNA_VIEW);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_LNA, Flurry.EVENT_NAME_CHECK_IN, params);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver#handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(DIALOG_CHECK_IN_FAILED);
        }

    } // end LocationCheckInReceiver

} // end class
