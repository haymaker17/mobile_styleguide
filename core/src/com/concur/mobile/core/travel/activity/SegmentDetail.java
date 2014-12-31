package com.concur.mobile.core.travel.activity;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.Segment;
import com.concur.mobile.core.travel.data.Segment.SegmentType;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

public class SegmentDetail extends BaseActivity {

    protected final static String CLS_TAG = SegmentDetail.class.getSimpleName();

    private static final int DIALOG_CANCEL_SEGMENT_CONFIRM = 0;
    private static final int DIALOG_CANCEL_SEGMENT_PROGRESS = 1;
    private static final int DIALOG_CANCEL_SEGMENT_FAILED = 2;
    private static final int DIALOG_CANCEL_SEGMENT_SUCCEEDED = 3;

    private static final String EXTRA_SEGMENT_CANCEL_RECEIVER_KEY = "segment.cancel.receiver";

    public static final char defaultDelim = ',';

    /**
     * Contains the receiver used to handle the response from a segment cancel request.
     */
    protected SegmentCancelReceiver segmentCancelReceiver;
    /**
     * Contains the intent filter used to register the above segment cancel receiver.
     */
    protected IntentFilter segmentCancelFilter;
    /**
     * Contains a reference to an oustanding request to cancel a segment.
     */
    protected ServiceRequest segmentCancelRequest;

    protected Segment seg;

    protected Trip trip;

    protected boolean segmentInitDelayed;

    protected Segment getSegment() {

        Segment segment = null;

        Intent i = getIntent();
        String itinLoc = i.getStringExtra(Const.EXTRA_ITIN_LOCATOR);
        String segKey = i.getStringExtra(Const.EXTRA_SEGMENT_KEY);

        IItineraryCache itinCache = getConcurCore().getItinCache();
        if (itinCache != null) {
            trip = itinCache.getItinerary(itinLoc);
            if (trip != null) {
                segment = trip.getSegment(segKey);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getSegment: unable to locate itinerary!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getSegment: itin cache is null!");
        }
        return segment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(getHeaderTitle());

        if (isServiceAvailable()) {
            initSegment();
        } else {
            segmentInitDelayed = true;
        }

        // Restore any receivers.
        restoreReceivers();

    }

    // NOTE: Following seven methods should be overridden by a sub-class of
    // SegmentDetail to support cancelling a specific segment.

    /**
     * Gets the segment cancel confirm dialog title.
     * 
     * @return the segment cancel confirm dialog title.
     */
    protected String getSegmentCancelConfirmDialogTitle() {
        return "";
    }

    /**
     * Gets the segment cancel confirm dialog message.
     * 
     * @return the segment cancel confirm dialog message.
     */
    protected String getSegmentCancelConfirmDialogMessage() {
        return "";
    }

    /**
     * Gets the segment cancel progress dialog message.
     * 
     * @return the segment cancel progress dialog message.
     */
    protected String getSegmentCancelProgressDialogMessage() {
        return "";
    }

    /**
     * Gets the segment cancel failed dialog title.
     * 
     * @return the segment cancel failed dialog title.
     */
    protected String getSegmentCancelFailedDialogTitle() {
        return "";
    }

    /**
     * Gets the segment cancel failed dialog message.
     * 
     * @return the segment failed dialog message.
     */
    protected String getSegmentCancelFailedDialogMessage() {
        return "";
    }

    /**
     * Gets the segment cancel success dialog title.
     * 
     * @return the segment cancel success dialog title.
     */
    protected String getSegmentCancelSuccessDialogTitle() {
        return "";
    }

    /**
     * Gets the segment cancel success dialog message.
     * 
     * @return the segment cancel success dialog message.
     */
    protected String getSegmentCancelSuccessDialogMessage() {
        return "";
    }

    /**
     * Gets the intent filter used to register a receiver to handle th result of a segment cancel request.
     * 
     * @return an intent filter used to register a receiver to handle the result of a segment cancel request.
     */
    protected IntentFilter getSegmentCancelFilter() {
        return null;
    }

    /**
     * Sends a request to cancel this segment.
     * 
     * @return an instance of <code>ServiceRequest</code>.
     */
    protected ServiceRequest sendCancelSegmentRequest() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case DIALOG_CANCEL_SEGMENT_CONFIRM: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(getSegmentCancelConfirmDialogTitle());
            dlgBldr.setMessage(getSegmentCancelConfirmDialogMessage());
            dlgBldr.setPositiveButton(R.string.okay, new Dialog.OnClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
                 */
                public void onClick(DialogInterface dialog, int which) {

                    // Dismiss the dialog.
                    dialog.dismiss();
                    // Start the segment cancel. TODO, remove this check after all segments implement async task
                    if (seg.getType() == SegmentType.HOTEL) {
                        doSegmentCancelV2();
                    } else {
                        doSegmentCancel();
                    }
                }
            });
            dlgBldr.setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
                 */
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = dlgBldr.create();
            break;
        }
        case DIALOG_CANCEL_SEGMENT_PROGRESS: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getSegmentCancelProgressDialogMessage());
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.content.DialogInterface.OnCancelListener#onCancel(android.content.DialogInterface)
                 */
                public void onCancel(DialogInterface dialog) {
                    // Cancel the request.
                    if (segmentCancelRequest != null) {
                        segmentCancelRequest.cancel();
                    }
                    unregisterSegmentCancelReceiver();
                }
            });
            dialog = progDlg;
            break;
        }
        case DIALOG_CANCEL_SEGMENT_SUCCEEDED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(getSegmentCancelSuccessDialogTitle());
            dlgBldr.setMessage(getSegmentCancelSuccessDialogMessage());
            dlgBldr.setPositiveButton(R.string.okay, new Dialog.OnClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
                 */
                public void onClick(DialogInterface dialog, int which) {
                    // Dismiss the dialog.
                    dialog.dismiss();
                    // Set the result to be 'OK'.
                    setResult(Activity.RESULT_OK);
                    // Just finish the activity.
                    finish();
                }
            });
            dialog = dlgBldr.create();
            // Ensure that no matter how this dialog is dismissed, that the result is set.
            dialog.setOnDismissListener(new OnDismissListener() {

                public void onDismiss(DialogInterface dialog) {
                    // Set the result to be 'OK'.
                    setResult(Activity.RESULT_OK);
                }
            });
            break;
        }
        case DIALOG_CANCEL_SEGMENT_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(getSegmentCancelFailedDialogTitle());
            dlgBldr.setMessage(getSegmentCancelFailedDialogMessage());
            dlgBldr.setPositiveButton(R.string.okay, new Dialog.OnClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
                 */
                public void onClick(DialogInterface dialog, int which) {
                    // Dismiss the dialog.
                    dialog.dismiss();
                }
            });
            dialog = dlgBldr.create();
            break;
        }
        case Const.ALTERNATIVE_AIR_SEARCH_PROGRESS_DIALOG: {
            ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(this.getText(R.string.dlg_travel_alternative_flight_retrieve));
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            dialog = pDialog;
            break;
        }

        case Const.ALTERNATIVE_AIR_SEARCH_FAIL_DIALOG: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_travel_alternative_flight_retrieve_fail);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = dlgBldr.create();
            break;
        }

        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (retainer != null) {
            if (segmentCancelReceiver != null) {
                // Clear the activity reference, it will be set in the new SegmentDetail instance.
                segmentCancelReceiver.setActivity(null);
                retainer.put(EXTRA_SEGMENT_CANCEL_RECEIVER_KEY, segmentCancelReceiver);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case DIALOG_CANCEL_SEGMENT_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            if (actionStatusErrorMessage != null) {
                alertDlg.setMessage(actionStatusErrorMessage);
            }
            break;
        }
        case Const.ALTERNATIVE_AIR_SEARCH_FAIL_DIALOG: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            if (actionStatusErrorMessage != null) {
                alertDlg.setMessage(actionStatusErrorMessage);
            }
            break;
        }
        default: {
            super.onPrepareDialog(id, dialog);
            break;
        }
        }
    }

    public void onClick(View view) {
        final int id = view.getId();
        if (id == R.id.cancel) {
            if (ConcurCore.isConnected()) {
                showDialog(DIALOG_CANCEL_SEGMENT_CONFIRM);
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        restoreReceivers();
    }

    protected void restoreReceivers() {
        if (retainer != null) {
            // Restore any segment cancel receiver.
            if (retainer.contains(EXTRA_SEGMENT_CANCEL_RECEIVER_KEY)) {
                segmentCancelReceiver = (SegmentCancelReceiver) retainer.get(EXTRA_SEGMENT_CANCEL_RECEIVER_KEY);
                // Reset the activity reference.
                segmentCancelReceiver.setActivity(this);
            }
        }
    }

    protected String getHeaderTitle() {
        return "";
    }

    protected void initSegment() {
        seg = getSegment();
        if (seg == null) {
            // We screwed up somewhere
            Log.e(Const.LOG_TAG, "SegmentDetail received bad locator extras");
            finish();
        }

    }

    @Override
    protected void onServiceAvailable() {
        // Not bound, probably a restart.
        // Show the network progress indicator. Technically not network but oh well.
        ViewUtil.setNetworkActivityIndicatorVisibility(this, View.VISIBLE, null);
        initSegment();
        segmentInitDelayed = false;

        ViewUtil.setNetworkActivityIndicatorVisibility(this, View.INVISIBLE, null);
    }

    /**
     * Ye old blank string helper
     */
    protected boolean isBlank(String s) {
        return (s == null || s.length() == 0);
    }

    protected View populateField(int fieldId, int labelId, CharSequence valueText, int linkMask) {
        View view = populateField(fieldId, labelId, valueText);
        TextView valueView = (TextView) view.findViewById(R.id.field_value);
        Linkify.addLinks(valueView, linkMask);
        return view;
    }

    protected View populateField(int fieldId, int labelId, CharSequence valueText) {
        return populateField(fieldId, labelId, valueText, false);
    }

    protected View populateField(int fieldId, int labelId, CharSequence valueText, boolean toastable) {
        View layout = findViewById(fieldId);
        TextView label = (TextView) layout.findViewById(R.id.field_name);
        label.setText(labelId);
        TextView value = (TextView) layout.findViewById(R.id.field_value);

        if (valueText == null || valueText.toString().trim().length() <= 0) {
            valueText = Const.NA;
        }

        value.setText(valueText);

        if (toastable) {
            makeFieldToastable(layout);
        }

        return layout;
    }

    protected void makeFieldToastable(int fieldId) {
        View layout = findViewById(fieldId);
        makeFieldToastable(layout);
    }

    protected void makeFieldToastable(final View layout) {
        TextView value = (TextView) layout.findViewById(R.id.field_value);
        final CharSequence text = value.getText();
        layout.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Toast.makeText(layout.getContext(), text, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void hideField(int fieldId) {
        findViewById(fieldId).setVisibility(View.GONE);
    }

    protected void showField(int fieldId) {
        findViewById(fieldId).setVisibility(View.VISIBLE);
    }

    protected void populateTimeFlipper(int fieldId, Calendar cal) {
        View layout = findViewById(fieldId);
        TextView time = (TextView) layout.findViewById(R.id.time);
        TextView ampm = (TextView) layout.findViewById(R.id.ampm);

        String timeText;

        if (DateFormat.is24HourFormat(this)) {
            timeText = Format.safeFormatCalendar(FormatUtil.SHORT_24HR_TIME_ONLY_DISPLAY, cal);
            // No AM/PM
            ampm.setVisibility(View.INVISIBLE);
        } else {
            timeText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_ONLY_DISPLAY, cal);
            final String ampmText = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_AMPM_DISPLAY, cal);
            ampm.setText(ampmText);
        }

        time.setText(timeText);

    }

    /**
     * Helper to quickly set the text on a text view
     * 
     * @param viewId
     *            The ID of a TextView
     * @param text
     *            The text to set into the view
     */
    protected TextView setText(int viewId, Object text) {
        TextView tv = (TextView) findViewById(viewId);

        if (tv != null && text != null) {
            tv.setText(text.toString());
        }

        return tv;
    }

    /**
     * Helper to quickly set the text on a text view
     * 
     * @param viewId
     *            The ID of a TextView
     * @param id
     *            The id of the string to set into the view
     */
    protected TextView setText(int viewId, int id) {
        TextView tv = (TextView) findViewById(viewId);

        if (tv != null) {
            tv.setText(com.concur.mobile.base.util.Format.localizeText(this, id));
        }

        return tv;
    }

    /**
     * Helper to quickly set the text on a text view and turn it into a clickable link
     * 
     * @param viewId
     *            The ID of a TextView
     * @param text
     *            The text to set into the view
     * @param linkifyMask
     *            An integer mask indicating the type of text to link. See {@link Linkify}.
     */
    protected TextView setText(int viewId, String text, int linkifyMask) {

        TextView tv = (TextView) findViewById(viewId);

        if (tv != null && text != null) {
            Spannable linkText = new SpannableString(text);
            Linkify.addLinks(linkText, linkifyMask);

            tv.setText(linkText);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }

        return tv;
    }

    public void linkMap(int viewId, final String address) {
        View view = findViewById(viewId);
        linkMap(view, address);
    }

    public void linkMap(View view, final String address) {
        view.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                try {
                    String uri;
                    // MOB-13020 Temporary fix to get around a bug with Google maps app which crashes for 'New York Penn Station'
                    // or 'New York -
                    // Penn Station, NY' strings. Need to be revisited later and remove this fix.
                    if (address.contains("New York - Penn Station")) {
                        uri = "geo:40.750915,-73.994358?z=18";
                    } else {
                        uri = new StringBuilder("geo:0,0?q=").append(address).toString();
                    }
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(i);
                } catch (ActivityNotFoundException anfExc) {
                    // No-op. No mapping installed!
                    Log.i(Const.LOG_TAG, CLS_TAG + ".linkMap.onClick: no mapping activity found!");
                }
            }
        });
    }

    /**
     * Linkify all the text in the given TextView and create a click listener to take it to the specified URL
     * 
     * @param viewId
     *            The ID of a TextView
     * @param context
     *            The {@link Context} of the Activity
     */
    public void linkUrl(int viewId, final Context context, final String url) {

        final TextView tv = (TextView) findViewById(viewId);
        Linkify.addLinks(tv, Pattern.compile(".*"), null);

        // TODO: This doesn't give us the right UI interaction. The link doesn't highlight or anything
        // when clicked. May need to provide our own movement method to make it all work right.
        tv.setMovementMethod(null);
        tv.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
            }

        });

    }

    public CharSequence textOrNA(String value) {
        CharSequence text;
        if (isBlank(value)) {
            text = Const.NA;
        } else {
            text = value;
        }
        return text;
    }

    private void doSegmentCancel() {
        registerSegmentCancelReceiver();
        segmentCancelRequest = sendCancelSegmentRequest();
        if (segmentCancelRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".doSegmentCancel: unable to create segment cancel request.");
            unregisterSegmentCancelReceiver();
        } else {
            // Set the request object on the receiver.
            segmentCancelReceiver.setServiceRequest(segmentCancelRequest);
            // Show the dialog.
            showDialog(DIALOG_CANCEL_SEGMENT_PROGRESS);
        }
    }

    /**
     * Cancel V2 for Hotel
     */
    protected void doSegmentCancelV2() {
        // TODO Auto-generated method stub

    }

    /**
     * Will register a segment cancel receiver.
     */
    protected void registerSegmentCancelReceiver() {
        if (segmentCancelReceiver == null) {
            segmentCancelReceiver = new SegmentCancelReceiver(this);
            if (segmentCancelFilter == null) {
                segmentCancelFilter = getSegmentCancelFilter();
            }
            getApplicationContext().registerReceiver(segmentCancelReceiver, segmentCancelFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerSegmentCancelReceiver: segmentCancelReceiver is *not* null!");
        }
    }

    /**
     * Will unregister a segment cancel receiver.
     */
    protected void unregisterSegmentCancelReceiver() {
        if (segmentCancelReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(segmentCancelReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSegmentCancelReceiver: illegal argument", ilaExc);
            }
            segmentCancelReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSegmentCancelReceiver: segmentCancelReceiver is null!");
        }
    }

    /**
     * show segment travel points
     */
    protected void populateTravelPoints() {
        if (seg.travelPoint != null) {
            View view = null;
            int i = R.string.segment_travel_points_posted;
            String points = seg.travelPoint.getPointsPosted();
            if (points == null || points.equals("0") || points.trim().length() == 0) {
                i = R.string.segment_travel_points_pending;
                points = seg.travelPoint.getPointsPending();
            }
            view = populateField(R.id.travel_points, i, points);
            TextView valueView = (TextView) view.findViewById(R.id.field_name);
            valueView.setTextAppearance(this, R.style.HotelListRowPriceText);

            String priceToBeatStr = Const.NA;
            final Locale locale = getResources().getConfiguration().locale;
            Double priceToBeat = Parse.safeParseDouble(seg.travelPoint.getBenchmark());
            if (priceToBeat != null) {
                priceToBeatStr = FormatUtil.formatAmount(priceToBeat, locale, seg.travelPoint.getBenchmarkCurrency(),
                        true);
            }
            view = populateField(R.id.travel_points_price_to_beat, R.string.segment_travel_points_price_to_beat,
                    priceToBeatStr);
            valueView = (TextView) view.findViewById(R.id.field_name);
            valueView.setTextAppearance(this, R.style.HotelListRowPriceText);
        } else {
            hideField(R.id.travel_points);
            hideField(R.id.travel_points_price_to_beat);
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the results of canceling a segment.
     */
    static class SegmentCancelReceiver extends BaseBroadcastReceiver<SegmentDetail, ServiceRequest> {

        /**
         * Constructs an instance of <code>SegmentCancelReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        SegmentCancelReceiver(SegmentDetail activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#clearActivityServiceRequest(com.concur.mobile.activity
         * .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(SegmentDetail activity) {
            activity.segmentCancelRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(DIALOG_CANCEL_SEGMENT_PROGRESS);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(DIALOG_CANCEL_SEGMENT_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            Intent data = new Intent();
            if (activity.seg.getType() != null) {
                switch (activity.seg.getType()) {
                case AIR: {
                    data.putExtra(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_AIR);
                    break;
                }
                case CAR: {
                    data.putExtra(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_CAR);
                    break;
                }
                case DINING: {
                    data.putExtra(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_DINING);
                    break;
                }
                case EVENT: {
                    data.putExtra(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_EVENT);
                    break;
                }
                case HOTEL: {
                    data.putExtra(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_HOTEL);
                    break;
                }
                case PARKING: {
                    data.putExtra(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_PARKING);
                    break;
                }
                case RAIL: {
                    data.putExtra(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_TRAIN);
                    break;
                }
                case RIDE: {
                    data.putExtra(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_RIDE);
                    break;
                }
                }
            }
            activity.setResult(Activity.RESULT_OK, data);
            activity.showDialog(DIALOG_CANCEL_SEGMENT_SUCCEEDED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#setActivityServiceRequest(com.concur.mobile.activity.
         * BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(ServiceRequest request) {
            activity.segmentCancelRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterSegmentCancelReceiver();
        }

    }

}
