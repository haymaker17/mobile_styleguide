package com.concur.mobile.core.travel.air.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.travel.activity.SegmentDetail;
import com.concur.mobile.core.travel.air.data.AirSegment;
import com.concur.mobile.core.travel.air.data.AirSegment.FlightStatusInfo;
import com.concur.mobile.core.travel.air.service.AlternativeAirScheduleReply;
import com.concur.mobile.core.travel.air.service.AlternativeAirScheduleRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

public class AirSegmentDetail extends SegmentDetail implements View.OnClickListener {

    private AlternativeAirScheduleRequest request;
    private IntentFilter alternativeFilter = null;
    private AlternativeFightSearchReciever receiver = null;
    private Intent alternativeSearchDetailIntent;
    public static final String START_CITY_NAME = "start city name";
    public static final String END_CITY_NAME = "end city name";
    public static final String START_CITY_CODE = "start city code";
    public static final String END_CITY_CODE = "end city code";
    public static final String DEPART_DATE = "depart date";
    protected static final int RESULTS_ACTIVITY_CODE = 2;
    AirSegment seg;
    private static final String ALTERNATIVE_AIR_SCHEDULE_RECEIVER_KEY = "air.filter.receiver";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.segment_air);
        if (!segmentInitDelayed) {
            buildView();
        }
        initValue();
    }

    @Override
    protected void onServiceAvailable() {
        super.onServiceAvailable();
        buildView();
    }

    private void initValue() {
        alternativeSearchDetailIntent = new Intent(AirSegmentDetail.this, AlternativeAirScheduleList.class);
        if (seg != null) {
            alternativeSearchDetailIntent.putExtra(START_CITY_NAME, seg.startAirportCity);
            alternativeSearchDetailIntent.putExtra(END_CITY_NAME, seg.endAirportCity);
            alternativeSearchDetailIntent.putExtra(START_CITY_CODE, seg.startCityCode);
            alternativeSearchDetailIntent.putExtra(END_CITY_CODE, seg.endCityCode);
            alternativeSearchDetailIntent.putExtra(DEPART_DATE, seg.getStartDateUtc());
        } else {
            alternativeSearchDetailIntent = null;
        }

        restoreReceivers();
    }

    protected void buildView() {

        seg = (AirSegment) super.seg;

        if (seg == null) {
            // Something is wrong here. Get out of this activity and back to
            // wherever to hopefully reload the trips.
            // MOB-10690
            finish();
            return;
        }

        // check to see if the view is from trip approval functionality -
        // MOB-13566
        boolean isForTripApproval = getIntent().getBooleanExtra(Const.EXTRA_IS_FOR_TRIP_APPROVAL, false);

        setText(R.id.airFromTo, com.concur.mobile.base.util.Format.localizeText(this, R.string.segment_fromto,
                seg.startAirportCity, seg.endAirportCity));

        setText(R.id.airConfirm,
                com.concur.mobile.base.util.Format.localizeText(this, R.string.general_confirmnum, seg.confirmNumber));

        StringBuilder sb = new StringBuilder(seg.vendorName).append(' ').append(seg.flightNumber);
        if (!isBlank(seg.operatedByVendor) && !isBlank(seg.operatedByFlightNumber)) {
            sb.append(" (").append(seg.operatedByVendor).append(' ').append(seg.operatedByFlightNumber).append(')');
        }
        setText(R.id.airFlight, sb);

        // Depart side
        sb.setLength(0);
        sb.append(getText(R.string.general_depart)).append(" (").append(seg.startCityCode).append(')');
        setText(R.id.airDepartCity, sb);

        populateTimeFlipper(R.id.airDepartTime, seg.getStartDateLocal());

        setText(R.id.airDepartDate, Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY, seg.getStartDateLocal()));

        sb.setLength(0);
        sb.append(getText(R.string.segment_air_label_terminal)).append(' ').append(textOrNA(seg.startTerminal));
        setText(R.id.airDepartTerminal, sb);

        sb.setLength(0);
        sb.append(getText(R.string.segment_air_label_gate)).append(' ').append(textOrNA(seg.startGate));
        setText(R.id.airDepartGate, sb);

        // Arrive side
        sb.setLength(0);
        sb.append(getText(R.string.general_arrive)).append(" (").append(seg.endCityCode).append(')');
        setText(R.id.airArriveCity, sb);

        populateTimeFlipper(R.id.airArriveTime, seg.getEndDateLocal());

        setText(R.id.airArriveDate, Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY, seg.getEndDateLocal()));

        sb.setLength(0);
        sb.append(getText(R.string.segment_air_label_terminal)).append(' ').append(textOrNA(seg.endTerminal));
        setText(R.id.airArriveTerminal, sb);

        sb.setLength(0);
        sb.append(getText(R.string.segment_air_label_gate)).append(' ').append(textOrNA(seg.endGate));
        setText(R.id.airArriveGate, sb);

        // The rows

        if (isForTripApproval) {
            findViewById(R.id.airFlightStatus).setVisibility(View.GONE);
        } else {
            ImageView fsIcon = (ImageView) findViewById(R.id.airFlightStatusIcon);
            View fsField;
            if (seg.hasFlightStats()) {
                FlightStatusInfo fs = seg.flightStatus;
                fsField = populateField(R.id.airFlightStatus, R.string.segment_air_label_fs_status,
                        textOrNA(fs.departureShortStatus));
                if (fs.departureShortStatus != null) {
                    if (fs.departureShortStatus.equalsIgnoreCase("Delayed")
                            || fs.departureShortStatus.equalsIgnoreCase("Cancelled")) {
                        fsIcon.setImageResource(R.drawable.flight_status_red);
                    }
                }
            } else {
                fsField = populateField(R.id.airFlightStatus, R.string.segment_air_label_fs_status,
                        getText(R.string.segment_air_default_flight_status));
                fsIcon.setImageResource(R.drawable.flight_status_green);
            }
            fsField.setOnClickListener(this);
        }

        if (isBlank(seg.operatedByVendorName)) {
            hideField(R.id.airOperatedBy);
        } else {
            populateField(R.id.airOperatedBy, R.string.segment_air_label_operated, seg.operatedByVendorName);
        }

        sb.setLength(0);
        sb.append(textOrNA(seg.classOfServiceLocalized)).append(" / ").append(textOrNA(seg.seat));
        populateField(R.id.airClassSeat, R.string.segment_air_label_class_seat, sb);

        if (isForTripApproval) {
            findViewById(R.id.alternativeFlightSchedule).setVisibility(View.GONE);
        } else {
            // MOB-9112
            sb.setLength(0);
            sb.append(textOrNA(getString(R.string.segment_air_default_alternative_flights).toString()));
            View v = populateField(R.id.alternativeFlightSchedule, R.string.segment_air_label_flight_schedule, sb);
            v.setOnClickListener(this);
        }

        CharSequence duration;
        if (seg.duration != null && seg.duration > 0) {
            duration = FormatUtil.formatElapsedTime(this, seg.duration, true);
        } else {
            duration = Const.NA;
        }
        populateField(R.id.airDuration, R.string.segment_air_label_duration, duration);

        CharSequence miles;
        if (seg.miles != null && seg.miles > 0) {
            miles = com.concur.mobile.base.util.Format.localizeText(this, R.string.general_miles, seg.miles);
        } else {
            miles = Const.NA;
        }
        populateField(R.id.airDistance, R.string.segment_air_label_distance, miles);

        populateField(R.id.airAircraft, R.string.segment_air_label_aircraft, textOrNA(seg.aircraftName));

        populateField(R.id.airBookingStatus, R.string.segment_status, textOrNA(seg.statusLocalized));

        // show travel points
        populateTravelPoints();

    }

    @Override
    protected String getHeaderTitle() {
        return getText(R.string.segment_air_detail_title).toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (seg.hasFlightStats()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.segment_air, menu);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.airFlightStatus == v.getId()) {
            showFlightStatus();
        }
        if (R.id.alternativeFlightSchedule == v.getId()) {
            getAlternativeFlightSchedule();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int itemId = item.getItemId();
        if (itemId == R.id.segmentAirMenuFlightStats) {
            showFlightStatus();
        }

        return super.onOptionsItemSelected(item);
    }

    protected void showFlightStatus() {
        Intent i;
        Intent origIntent = getIntent();
        String itinLoc = origIntent.getStringExtra(Const.EXTRA_ITIN_LOCATOR);
        String segKey = origIntent.getStringExtra(Const.EXTRA_SEGMENT_KEY);

        i = new Intent(this, AirSegmentFlightStatDetail.class);
        i.putExtra(Const.EXTRA_ITIN_LOCATOR, itinLoc);
        i.putExtra(Const.EXTRA_SEGMENT_KEY, segKey);
        startActivity(i);
    }

    /**
     * Search alternative flight schedule
     */
    private void getAlternativeFlightSchedule() {
        if (ConcurCore.isConnected()) {
            ConcurService concurService = getConcurService();
            if (concurService != null) {
                registerSearchReceiver();
                request = concurService.searchAlternativeFlightScheduleRequest(seg.endCityCode, seg.startCityCode,
                        seg.vendor, seg.getStartDateUtc());
                if (request == null) {
                    Log.e(Const.LOG_TAG,
                            CLS_TAG
                                    + ".searchAlternativeFlightRequest: unable to create request for alternative air flight search!");
                    unregisterSearchReceiver();
                } else {
                    // set service request.
                    receiver.setServiceRequest(request);
                    // Show the progress dialog.
                    showDialog(Const.ALTERNATIVE_AIR_SEARCH_PROGRESS_DIALOG);
                }
            } else {
                Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }

    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the response to filter air results.
     */
    class AlternativeFightSearchReciever extends BaseBroadcastReceiver<AirSegmentDetail, AlternativeAirScheduleRequest> {

        private final String CLS_TAG = AirSegmentDetail.CLS_TAG + "."
                + AlternativeFightSearchReciever.class.getSimpleName();

        /**
         * Constructs an instance of <code>AlternativeFightSearchReciever</code> .
         * 
         * @param activity
         *            the activity.
         */
        protected AlternativeFightSearchReciever(AirSegmentDetail activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(AirSegmentDetail activity) {
            activity.request = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.ALTERNATIVE_AIR_SEARCH_PROGRESS_DIALOG);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.ALTERNATIVE_AIR_SEARCH_FAIL_DIALOG);
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            ConcurCore ConcurCore = activity.getConcurCore();
            final AlternativeAirScheduleReply reply = ConcurCore.getAlternativeFlightSchedules();
            if (reply != null) {
                Log.d(Const.LOG_TAG,
                        CLS_TAG + ".handleSuccess: filtered results // " + reply.listofSegmentOptions.size());
                if (alternativeSearchDetailIntent != null) {
                    activity.startActivityForResult(activity.alternativeSearchDetailIntent,
                            AirSegmentDetail.RESULTS_ACTIVITY_CODE);
                } else
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: segment is null so intent wont be fire.");
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: successful reply but 'reply' is null!");
            }

        }

        @Override
        protected void setActivityServiceRequest(AlternativeAirScheduleRequest request) {
            activity.request = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterSearchReceiver();
        }

    }

    /**
     * Will register an instance of <code>AlternativeFightSearchReciever</code> with the application context and set the
     * <code>alternativeFilter</code> attribute.
     */
    protected void registerSearchReceiver() {
        if (receiver == null) {
            receiver = new AlternativeFightSearchReciever(this);
            if (alternativeFilter == null) {
                alternativeFilter = new IntentFilter(Const.ACTION_ALTERNATIVE_AIR_SEARCH_RESULTS);
            }
            getApplicationContext().registerReceiver(receiver, alternativeFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerAlternativeFightSearchReciever: alternativeFilter is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>AlternativeFightSearchReciever</code> with the application context and set the
     * <code>alternativeFilter</code> to <code>null</code>.
     */
    protected void unregisterSearchReceiver() {
        if (receiver != null) {
            getApplicationContext().unregisterReceiver(receiver);
            receiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAlternativeFightSearchReciever: alternativeFilter is null!");
        }
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
            if (receiver != null) {
                receiver.setActivity(null);
                retainer.put(ALTERNATIVE_AIR_SCHEDULE_RECEIVER_KEY, receiver);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    @Override
    protected void restoreReceivers() {
        if (retainer != null) {
            if (retainer.contains(ALTERNATIVE_AIR_SCHEDULE_RECEIVER_KEY)) {
                receiver = (AlternativeFightSearchReciever) retainer.get(ALTERNATIVE_AIR_SCHEDULE_RECEIVER_KEY);
                if (receiver != null) {
                    receiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: retainer has null value for air filter receiver!");
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case AirSegmentDetail.RESULTS_ACTIVITY_CODE:
            setResult(resultCode, data);
            // If the result was 'OK', then finish the activity.
            if (resultCode == Activity.RESULT_OK) {
                finish();
            }
            break;
        }
    }

}
