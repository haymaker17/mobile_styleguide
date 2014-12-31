package com.concur.mobile.core.travel.car.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.TextAppearanceSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.car.data.CarChain;
import com.concur.mobile.core.travel.car.data.CarChoice;
import com.concur.mobile.core.travel.car.data.CarDescription;
import com.concur.mobile.core.travel.car.data.CarLocation;
import com.concur.mobile.core.travel.car.service.CarSearchReply;
import com.concur.mobile.core.travel.car.service.CarSellRequest;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.StyleableSpannableStringBuilder;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.AsyncImageView;
import com.concur.mobile.platform.util.Format;

public class CarSearchDetail extends TravelBaseActivity {

    public static final String CLS_TAG = CarSearchDetail.class.getSimpleName();

    private static final String RESERVE_CAR_RECEIVER_KEY = "reserve.car.receiver";

    protected static final int DIALOG_CAR_TYPE_DESCR = DIALOG_ID_BASE + 1;

    protected String clientLocator;
    protected String recordLocator;

    protected String carId;
    protected String carTypeDescription;

    /**
     * Contains a reference to the receiver to handle the result of booking a car.
     */
    protected ReserveCarReceiver reserveCarReceiver;
    /**
     * Contains the intent filter used to register the reserve car receiver.
     */
    protected IntentFilter reserveCarFilter;
    /**
     * Contains a reference to an outstanding request to reserve a car.
     */
    protected CarSellRequest reserveCarRequest;
    /**
     * Contains a reference to the car to be reserved.
     */
    protected CarChoice carChoice;
    /**
     * Contains a reference to the car chain.
     */
    protected CarChain chain;
    /**
     * Contains a reference to the car description.
     */
    protected CarDescription desc;
    /**
     * Contains a reference to the car location.
     */
    protected CarLocation location;
    /**
     * Contains a reference to the last car search results.
     */
    protected CarSearchReply results;

    @Override
    protected void onCreate(Bundle inState) {
        super.onCreate(inState);

        // Restore any receivers.
        restoreReceivers();

        initValues(inState);
        initUI();

        // Fetch booking information fields if created on a non-orientation change
        // and no passed in trip.
        if (!orientationChange) {
            if (cliqbookTripId == null) {
                // Check whether a travel custom fields view fragment already exists. This can
                // can be the case if a device gets rotated while this activity is on the stack
                // and not directly visible. Example, is on HotelReserveRoom screen, go to hotel room details
                // and rotate the device, then press HW back button.
                if (!hasTravelCustomFieldsView()) {
                    if (shouldRequestTravelCustomFields()) {
                        sendTravelCustomFieldsRequest();
                    } else {
                        initTravelCustomFieldsView();
                    }
                }
            }
        }
    }

    @Override
    protected boolean getDisplayAtStart() {
        return false;
    }

    protected void initUI() {

        // Set the layout.
        setContentView(R.layout.car_search_detail);

        // Set the screen title.
        getSupportActionBar().setTitle(R.string.car_detail_title);

        // Load up the header with image and values
        populateHeader();

        // Load up the additional car details
        populateDetails();

        // Load up the location details
        populateLocation();

        if (carChoice != null && !orientationChange) {
            initCarPreSellOptions();
        }

        super.initUI();

    }

    @Override
    protected Boolean isSendCreditCard() {
        return carChoice.sendCreditCard;
    }

    protected void initCar(Bundle inState) {
        // Find our detail data
        carChoice = results.findCarById(carId);
        chain = CarChain.findChainByCode(results.carChains, carChoice.chainCode);
        desc = CarDescription.findDescByCode(results.carDescriptions, carChoice.carType);
        location = CarLocation.findLocationByChain(results.carLocations, carChoice.chainCode);

        if (location == null) {
            Log.e(Const.LOG_TAG, "Car search detail location was null");

            Log.e(Const.LOG_TAG, " --- dropoffIATA: " + results.dropoffIATA);
            Log.e(Const.LOG_TAG, " --- pickupIATA: " + results.pickupIATA);
            Log.e(Const.LOG_TAG,
                    " --- dropoffDateTime: " + Format.safeFormatCalendar(FormatUtil.XML_DF, results.dropoffDateTime));
            Log.e(Const.LOG_TAG,
                    " --- pickupDateTime: " + Format.safeFormatCalendar(FormatUtil.XML_DF, results.pickupDateTime));

            if (chain != null) {
                Log.e(Const.LOG_TAG, " --- Chain: " + chain.chainCode);
            }

            if (desc != null) {
                Log.e(Const.LOG_TAG, " --- Desc: " + desc.carCode);
            }
        }
    }

    protected void initValues(Bundle inState) {

        results = ((ConcurCore) getApplication()).getCarSearchResults();

        if (inState != null) {
            carId = inState.getString(Const.EXTRA_CAR_DETAIL_ID);
            clientLocator = inState.getString(Const.EXTRA_TRAVEL_CLIENT_LOCATOR);
            recordLocator = inState.getString(Const.EXTRA_TRAVEL_RECORD_LOCATOR);
        } else {
            Intent i = getIntent();
            carId = i.getStringExtra(Const.EXTRA_CAR_DETAIL_ID);
            clientLocator = i.getStringExtra(Const.EXTRA_TRAVEL_CLIENT_LOCATOR);
            recordLocator = i.getStringExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR);
        }

        // Init the car domain object.
        initCar(inState);

        // Added for MOB-14317
        if (!orientationChange) {
            showDialog(RETRIEVE_PRE_SELL_OPTIONS_DIALOG);
            getPreSellOptions(carChoice.choiceId);
        }

        super.initValues(inState);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the client locator.
        outState.putString(Const.EXTRA_TRAVEL_CLIENT_LOCATOR, clientLocator);
        // Save the record locator.
        outState.putString(Const.EXTRA_TRAVEL_RECORD_LOCATOR, recordLocator);
        // Save the car id.
        outState.putString(Const.EXTRA_CAR_DETAIL_ID, carId);
    }

    @Override
    protected CharSequence getBookingConfirmDialogMessage() {
        return getText(R.string.dlg_travel_car_confirm_message);
    }

    @Override
    protected CharSequence getBookingConfirmDialogTitle() {
        return getText(R.string.confirm);
    }

    @Override
    protected CharSequence getBookingFailedDialogTitle() {
        return getText(R.string.car_detail_book_failure_title);
    }

    @Override
    protected CharSequence getBookingProgressDialogMessage() {
        return getText(R.string.booking_car);
    }

    @Override
    protected CharSequence getBookingSucceededDialogMessage() {
        return getText(R.string.car_detail_book_success_message);
    }

    @Override
    protected CharSequence getBookingSucceededDialogTitle() {
        return getText(R.string.car_detail_book_success_title);
    }

    @Override
    protected CharSequence getBookingType() {
        return getText(R.string.general_car);
    }

    @Override
    protected void onBookingSucceeded() {
        if (!launchedWithCliqbookTripId) {
            // Set the flag that the trip list should be refetched.
            IItineraryCache itinCache = getConcurCore().getItinCache();
            if (itinCache != null) {
                itinCache.setShouldRefetchSummaryList(true);
            }
            // Retrieve an updated trip summary list, then retrieve the detailed itinerary.
            sendItinerarySummaryListRequest();
        } else {
            // Just finish the activity.
            finish();
        }
    }

    @Override
    protected List<Violation> getViolations() {
        List<Violation> violations = null;
        if (carChoice != null) {
            violations = carChoice.violations;
        }
        return violations;
    }

    @Override
    protected void cancelBookingRequest() {
        if (reserveCarRequest != null) {
            reserveCarRequest.cancel();
        }
    }

    @Override
    protected void sendBookingRequest() {

        // Log the flurry event if the user completed this hotel booking using Voice.
        if (getIntent().getBooleanExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED, false)) {
            EventTracker.INSTANCE.track(Flurry.CATEGORY_VOICE_BOOK, Flurry.EVENT_NAME_COMPLETED_CAR);
        }

        sendReserveCarRequest();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;

        switch (id) {
        case DIALOG_CAR_TYPE_DESCR: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(getText(R.string.segment_car_type));
            dlgBldr.setMessage(carTypeDescription);
            dlgBldr.setCancelable(true);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
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
        }
        return dlg;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (retainer != null) {
            // Save ReserveCarReceiver
            if (reserveCarReceiver != null) {
                // Clear the activity reference, it will be set in the 'onCreate' method.
                reserveCarReceiver.setActivity(null);
                // Add to the retainer
                retainer.put(RESERVE_CAR_RECEIVER_KEY, reserveCarReceiver);
            }
        }
    }

    @Override
    protected void restoreReceivers() {
        super.restoreReceivers();
        if (retainer != null) {
            // Restore 'CarReserveReceiver'.
            if (retainer.contains(RESERVE_CAR_RECEIVER_KEY)) {
                reserveCarReceiver = (ReserveCarReceiver) retainer.get(RESERVE_CAR_RECEIVER_KEY);
                if (reserveCarReceiver != null) {
                    // Set the activity on the receiver.
                    reserveCarReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".onCreate: retainer contains null reference for reserve car receiver!");
                }
            }
        }
    }

    protected void populateHeader() {

        setText(R.id.carDetailVendor, chain.chainName);
        AsyncImageView aiv = (AsyncImageView) findViewById(R.id.carDetailModelImage);
        aiv.setAsyncUri(carChoice.imageUri);

        setText(R.id.carDetailPickup,
                getText(R.string.segment_car_pickup) + " "
                        + Format.safeFormatCalendar(FormatUtil.SHORT_DAY_TIME_DISPLAY, results.pickupDateTime));

        setText(R.id.carDetailReturn,
                getText(R.string.segment_car_return) + " "
                        + Format.safeFormatCalendar(FormatUtil.SHORT_DAY_TIME_DISPLAY, results.dropoffDateTime));
    }

    protected void populateDetails() {

        // Set the car type (i.e. class body, transmission, fuel/AC).
        View carType = findViewById(R.id.carDetailCarType);
        ((TextView) carType.findViewById(R.id.field_name)).setText(R.string.segment_car_type);
        CarDescription desc = CarDescription.findDescByCode(results.carDescriptions, carChoice.carType);
        StringBuilder typeDescr = new StringBuilder(desc.carClass).append(' ').append(desc.carBody);
        typeDescr.append(", ").append(desc.carTrans).append(", ").append(desc.carFuel);
        carTypeDescription = typeDescr.toString();
        ((TextView) carType.findViewById(R.id.field_value)).setText(carTypeDescription);
        carType.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                showDialog(DIALOG_CAR_TYPE_DESCR);
            }
        });

        // Set the daily rate.
        View dailyRate = findViewById(R.id.carDetailDailyRate);
        dailyRate.findViewById(R.id.field_image).setVisibility(View.GONE);
        ((TextView) dailyRate.findViewById(R.id.field_name)).setText(R.string.segment_car_rate_daily);
        int textAppearanceResourceId = ViewUtil.getFormFieldValueStyle(carChoice.violations);
        StyleableSpannableStringBuilder spanStrBldr = new StyleableSpannableStringBuilder();
        String formattedAmtStr = FormatUtil.formatAmount(carChoice.dailyRate, getResources().getConfiguration().locale,
                carChoice.currency, true, true);
        spanStrBldr.appendWithStyle(new TextAppearanceSpan(this, textAppearanceResourceId), formattedAmtStr);
        spanStrBldr.append("  ");
        spanStrBldr.appendWithStyle(new TextAppearanceSpan(this, R.style.ListCellSmallText),
                getText(R.string.car_results_daily_rate));
        ((TextView) dailyRate.findViewById(R.id.field_value)).setText(spanStrBldr);

        // Set the total rate if specified.
        if (carChoice.totalRate > 0.0) {
            View totalRate = findViewById(R.id.carDetailTotalRate);
            totalRate.findViewById(R.id.field_image).setVisibility(View.GONE);
            ((TextView) totalRate.findViewById(R.id.field_name)).setText(R.string.segment_car_rate_total);
            formattedAmtStr = FormatUtil.formatAmount(carChoice.totalRate, getResources().getConfiguration().locale,
                    carChoice.currency, true, true);
            spanStrBldr = new StyleableSpannableStringBuilder();
            spanStrBldr.appendWithStyle(new TextAppearanceSpan(this, textAppearanceResourceId), formattedAmtStr);
            ((TextView) totalRate.findViewById(R.id.field_value)).setText(spanStrBldr);
        } else {
            findViewById(R.id.carDetailTotalRateSeparator).setVisibility(View.GONE);
            findViewById(R.id.carDetailTotalRate).setVisibility(View.GONE);
        }
    }

    protected void populateLocation() {

        if (location == null) {
            // Not sure why this happens and cannot repro. Protect us. It is logged up top.
            findViewById(R.id.carDetailAddressLayout).setVisibility(View.GONE);
        } else {

            // Setup the phone number and linkify it.
            View phoneView = findViewById(R.id.carDetailPhone);
            phoneView.findViewById(R.id.field_image).setVisibility(View.GONE);
            ((TextView) phoneView.findViewById(R.id.field_name)).setText(R.string.car_detail_phone_label);
            TextView phoneTextView = ((TextView) phoneView.findViewById(R.id.field_value));
            Spannable linkText = new SpannableString(location.phoneNumber);
            Linkify.addLinks(linkText, Linkify.PHONE_NUMBERS);
            phoneTextView.setText(linkText);
            phoneTextView.setMovementMethod(LinkMovementMethod.getInstance());

            // Set the address and linkify it.
            View addressView = findViewById(R.id.carDetailAddress);
            addressView.findViewById(R.id.field_image).setVisibility(View.GONE);
            ((TextView) addressView.findViewById(R.id.field_name)).setText(R.string.car_detail_location_label);
            TextView addressTextView = ((TextView) addressView.findViewById(R.id.field_value));
            addressTextView.setText(location.locationName);

            if (location.address1 != null && location.address1.trim().length() > 0) {

                Linkify.addLinks(addressTextView, Pattern.compile(".*"), null);
                // TODO: This doesn't give us the right UI interaction. The link doesn't highlight or anything
                // when clicked. May need to provide our own movement method to make it all work right.
                addressTextView.setMovementMethod(null);
                addressTextView.setOnClickListener(new OnClickListener() {

                    public void onClick(View v) {
                        String lat = "0";
                        String lon = "0";

                        if (location.latitude != null && location.latitude.trim().length() > 0) {
                            lat = location.latitude;
                        }

                        if (location.longitude != null && location.longitude.trim().length() > 0) {
                            lon = location.longitude;
                        }

                        StringBuilder address = new StringBuilder(location.address1);
                        address.append(", ").append(location.address2);

                        String uri = new StringBuilder("geo:").append(lat).append(',').append(lon).append("?q=")
                                .append(address.toString()).toString();
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        try {
                            startActivity(i);
                        } catch (ActivityNotFoundException anfExc) {
                            // No-op. No mapping application installed.
                            Log.i(Const.LOG_TAG, CLS_TAG + ".populateLocation.onClick: no mapping activity found!");
                        }
                    }

                });
            }

        }
    }

    // //////////////////////////////////////////////////////////////////
    // Helper functions
    // //////////////////////////////////////////////////////////////////

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
     * Will send a request off to reserve the car.
     */
    protected void sendReserveCarRequest() {

        ConcurService concurService = getConcurService();
        if (concurService != null) {
            registerReserveCarReceiver();

            String creditCardId = null;
            if ((isSendCreditCard() == null || isSendCreditCard()) && curCardChoice != null) {
                creditCardId = curCardChoice.id;
            }
            String reasonCodeId = "";
            if (reasonCode != null) {
                reasonCodeId = reasonCode.id;
            }
            String violationText = (justificationText != null) ? justificationText : "";
            List<TravelCustomField> tcfs = null;
            // If this booking is part of an existing trip, then custom fields are not presented.
            if (cliqbookTripId == null) {
                tcfs = getTravelCustomFields();
            }
            reserveCarRequest = concurService.reserveCar(carId, creditCardId, ((recordLocator != null) ? recordLocator
                    : ""), ((cliqbookTripId != null) ? cliqbookTripId : ""), ((clientLocator != null) ? clientLocator
                    : ""), reasonCodeId, violationText, tcfs);
            if (reserveCarRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendCarRequest: unable to create 'CarSellRequest' request!");
                unregisterReserveCarReceiver();
            } else {
                // Set the request object on the receiver.
                reserveCarReceiver.setServiceRequest(reserveCarRequest);
                // Show the progress dialog.
                showDialog(BOOKING_PROGRESS_DIALOG);
            }
        } else {
            Log.i(Const.LOG_TAG, CLS_TAG + ".sendCarRequest: service is unavailable.");
        }
    }

    /**
     * Will register an instance of <code>ReserveCarReceiver</code> with the application context and set the
     * <code>reserveCarReceiver</code> attribute.
     */
    public void registerReserveCarReceiver() {
        if (reserveCarReceiver == null) {
            reserveCarReceiver = new ReserveCarReceiver(this);
            if (reserveCarFilter == null) {
                reserveCarFilter = new IntentFilter(Const.ACTION_CAR_SELL_RESULTS);
            }
            getApplicationContext().registerReceiver(reserveCarReceiver, reserveCarFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerReserveCarReceiver: reserveCarFilter is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>ReserveCarReceiver</code> with the application context and set the
     * <code>reserveCarReceiver</code> to <code>null</code>.
     */
    public void unregisterReserveCarReceiver() {
        if (reserveCarReceiver != null) {
            getApplicationContext().unregisterReceiver(reserveCarReceiver);
            reserveCarReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterReserveCarReceiver: reserveCarReceiver is null!");
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result reserving a car.
     */
    protected class ReserveCarReceiver extends BaseBroadcastReceiver<CarSearchDetail, CarSellRequest> {

        /**
         * Constructs an instance of <code>ReserveCarReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ReserveCarReceiver(CarSearchDetail activity) {
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
        protected void clearActivityServiceRequest(CarSearchDetail activity) {
            activity.reserveCarRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(BOOKING_PROGRESS_DIALOG);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(BOOKING_FAILED_DIALOG);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            onHandleSuccessReservation(activity, intent);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#setActivityServiceRequest(com.concur.mobile.activity.
         * BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(CarSellRequest request) {
            activity.reserveCarRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterReserveCarReceiver();
        }

    }

    protected void onHandleSuccessReservation(CarSearchDetail activity, Intent intent) {
        locateTripId(activity, intent);
        flurryEvents(activity);
        activity.showDialog(BOOKING_SUCCEEDED_DIALOG);
    }

    protected void locateTripId(CarSearchDetail activity, Intent intent) {
        Intent result = new Intent();
        activity.itinLocator = intent.getStringExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR);
        result.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, activity.itinLocator);
        if (activity.cliqbookTripId != null) {
            IItineraryCache itinCache = activity.getConcurCore().getItinCache();
            Trip trip = itinCache.getItinerarySummaryByCliqbookTripId(activity.cliqbookTripId);
            if (trip != null) {
                result.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, trip.itinLocator);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unable to locate trip based on cliqbook trip id!");
            }
        }
        activity.setResult(Activity.RESULT_OK, result);
    }

    protected void flurryEvents(CarSearchDetail activity) {
        // Flurry Notification.
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_CAR);
        Intent launchIntent = activity.getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            params.put(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        EventTracker.INSTANCE.track(Flurry.CATEGORY_BOOK, Flurry.EVENT_NAME_RESERVE, params);
    }

    @Override
    protected void updatePreSellOptions() {
        initCarPreSellOptions();
    }

    // Pre-sell options
    private void initCarPreSellOptions() {
        // update credit cards
        initCreditCards();
    }

    private void initCreditCards() {
        initCardChoices();
        initCardChoiceView();
    }

}
