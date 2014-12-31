package com.concur.mobile.core.travel.air.activity;

import java.util.Calendar;

import org.apache.http.HttpStatus;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.activity.SegmentDetail;
import com.concur.mobile.core.travel.air.data.AirSegment;
import com.concur.mobile.core.travel.air.data.AirSegment.FlightStatusInfo;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.service.ItineraryRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

public class AirSegmentFlightStatDetail extends SegmentDetail {

    private static final String CLS_TAG = AirSegmentFlightStatDetail.class.getSimpleName();

    private ItineraryRequest itinRequest = null;

    private SparseArray<Dialog> dialogs = new SparseArray<Dialog>(3);

    AirSegment seg;
    FlightStatusInfo fsi;

    protected String getStatusText(String longStatus, String shortStatus, String reason) {
        StringBuilder stat = new StringBuilder(Const.NA);

        if (shortStatus != null) {
            stat.setLength(0);
            stat.append(shortStatus);
            if (reason != null && !reason.equalsIgnoreCase("None")) {
                // TODO: This isn't nice. We should modify the MWS to return the reason code for a proper compare.
                stat.append(" [").append(reason).append(']');
            }
        }

        return stat.toString();
    }

    protected final IntentFilter filter = new IntentFilter(Const.ACTION_TRIP_UPDATED);

    protected final BroadcastReceiver receiver = new BroadcastReceiver() {

        /**
         * Receive notification that the list of trips has been updated. This method may be called any number of times while the
         * Activity is running.
         */
        public void onReceive(Context context, Intent intent) {

            try {
                // Dismiss the dialog.
                dismissDialog(Const.DIALOG_TRAVEL_RETRIEVE_FLIGHT_STATS);
            } catch (IllegalArgumentException ilaExc) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
            }

            // Handle the message
            int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
            if (serviceRequestStatus != -1) {
                if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                    int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                    if (httpStatusCode != -1) {
                        if (httpStatusCode == HttpStatus.SC_OK) {
                            if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                updateUI();
                            } else {
                                String actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                        + actionStatusErrorMessage + ".");
                            }
                        } else {
                            String lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- " + lastHttpErrorMessage + ".");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.segment_air_flightstat);

        if (!segmentInitDelayed) {
            updateUI();
        }
    }

    @Override
    protected void onServiceAvailable() {
        super.onServiceAvailable();
        updateUI();
    }

    protected CharSequence dateTextOrNA(Calendar cal) {
        String txt = Format.safeFormatCalendar(FormatUtil.SHORT_DAY_TIME_DISPLAY, cal);
        return textOrNA(txt);
    }

    protected void updateUI() {

        seg = (AirSegment) getSegment();
        fsi = seg.flightStatus;

        Button refresh = (Button) findViewById(R.id.fsRefresh);
        refresh.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Check for connectivity, if none, then display dialog and return.
                if (!ConcurCore.isConnected()) {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    return;
                }
                Intent i = getIntent();
                String itinLoc = i.getStringExtra(Const.EXTRA_ITIN_LOCATOR);
                if (itinLoc != null) {
                    ConcurCore ConcurCore = getConcurCore();
                    itinRequest = ConcurCore.getService().sendItineraryRequest(itinLoc);
                    if (itinRequest != null) {
                        showDialog(Const.DIALOG_TRAVEL_RETRIEVE_FLIGHT_STATS);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".updateUI.onClick: itin locator missing from intent!");
                }
            }
        });

        View depStatusView = populateField(R.id.fsDepStatus, R.string.segment_air_fs_label_dep_status,
                getStatusText(fsi.departureLongStatus, fsi.departureShortStatus, fsi.departureStatusReason));
        ImageView depIcon = (ImageView) depStatusView.findViewById(R.id.fsIcon);
        if (fsi.departureShortStatus != null) {
            if ((fsi.departureShortStatus.equalsIgnoreCase("Delayed") || fsi.departureShortStatus
                    .equalsIgnoreCase("Cancelled"))) {
                // TODO: This isn't nice. We should modify the MWS to return the status code for a proper compare.
                depIcon.setImageResource(R.drawable.flight_status_red);
            } else {
                depIcon.setImageResource(R.drawable.flight_status_green);
            }
        }

        populateField(R.id.fsDepSched, R.string.segment_air_fs_label_dep_sched, dateTextOrNA(fsi.departureScheduled));
        populateField(R.id.fsDepEstim, R.string.segment_air_fs_label_dep_estim, dateTextOrNA(fsi.departureEstimated));
        populateField(R.id.fsDepActual, R.string.segment_air_fs_label_dep_actual, dateTextOrNA(fsi.departureActual));
        populateField(R.id.fsDepTermSched, R.string.segment_air_fs_label_dep_term_sched,
                textOrNA(fsi.departureTerminalScheduled));
        populateField(R.id.fsDepTermActual, R.string.segment_air_fs_label_dep_term_actual,
                textOrNA(fsi.departureTerminalActual));
        populateField(R.id.fsDepGate, R.string.segment_air_fs_label_dep_gate, textOrNA(fsi.departureGate));

        if (isBlank(fsi.diversionCity)) {
            hideField(R.id.fsDivCity);
        } else {
            hideField(R.id.fsDivCity);
            populateField(R.id.fsDivCity, R.string.segment_air_fs_label_div_city, fsi.diversionCity);
        }

        if (isBlank(fsi.diversionAirport)) {
            hideField(R.id.fsDivAirport);
        } else {
            showField(R.id.fsDivAirport);
            populateField(R.id.fsDivAirport, R.string.segment_air_fs_label_div_airport, fsi.diversionAirport);
        }

        populateField(R.id.fsArrSched, R.string.segment_air_fs_label_arr_sched, dateTextOrNA(fsi.arrivalScheduled));
        populateField(R.id.fsArrEstim, R.string.segment_air_fs_label_arr_estim, dateTextOrNA(fsi.arrivalEstimated));

        CharSequence arrTerm = Const.NA;
        if (fsi.arrivalTerminalActual != null) {
            arrTerm = fsi.arrivalTerminalActual;
        } else if (fsi.arrivalTerminalScheduled != null) {
            arrTerm = fsi.arrivalTerminalScheduled;
        }
        populateField(R.id.fsArrTerm, R.string.segment_air_fs_label_arr_term, arrTerm);

        populateField(R.id.fsArrGate, R.string.segment_air_fs_label_arr_gate, textOrNA(fsi.arrivalGate));

        View arrStatusView = populateField(R.id.fsArrStatus, R.string.segment_air_fs_label_arr_status,
                getStatusText(fsi.arrivalLongStatus, fsi.arrivalShortStatus, fsi.arrivalStatusReason));
        ImageView arrIcon = (ImageView) arrStatusView.findViewById(R.id.fsIcon);
        if (fsi.arrivalShortStatus != null) {
            if ((fsi.arrivalShortStatus.equalsIgnoreCase("Delayed") || fsi.arrivalShortStatus
                    .equalsIgnoreCase("Cancelled"))) {
                // TODO: This isn't nice. We should modify the MWS to return the status code for a proper compare.
                arrIcon.setImageResource(R.drawable.flight_status_red);
            } else {
                arrIcon.setImageResource(R.drawable.flight_status_green);
            }
        }

        populateField(R.id.fsBaggage, R.string.segment_air_fs_label_baggage, textOrNA(fsi.baggageClaim));

        StringBuilder aircraft = new StringBuilder(Const.NA);
        if (fsi.equipmentActual != null) {
            aircraft.setLength(0);
            aircraft.append(fsi.equipmentActual);
            if (fsi.equipmentRegistration != null) {
                aircraft.append(" [").append(fsi.equipmentRegistration).append(']');
            }
        } else if (fsi.equipmentScheduled != null) {
            aircraft.setLength(0);
            aircraft.append(fsi.equipmentScheduled);
            if (fsi.equipmentRegistration != null) {
                aircraft.append(" [").append(fsi.equipmentRegistration).append(']');
            }
        }
        populateField(R.id.fsAircraft, R.string.segment_air_fs_label_aircraft, aircraft);

        String itinLoc = getIntent().getStringExtra(Const.EXTRA_ITIN_LOCATOR);
        if (itinLoc != null) {
            IItineraryCache itinCache = getConcurCore().getItinCache();
            if (itinCache != null) {
                getConcurCore().updateLastUpdateText(this, itinCache.getItineraryUpdateTime(itinLoc));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateUI: itin cache is null!");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected String getHeaderTitle() {
        return getText(R.string.segment_air_fs_title).toString();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = dialogs.get(id);

        if (dlg == null) {
            dlg = ((ConcurCore) getApplication()).createDialog(this, id);
            dialogs.put(id, dlg);
            switch (id) {
            case Const.DIALOG_TRAVEL_RETRIEVE_FLIGHT_STATS: {
                dlg.setOnCancelListener(new OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        // Cancel any outstanding request.
                        if (itinRequest != null) {
                            itinRequest.cancel();
                        }
                    }
                });
                break;
            }
            }
        }
        return dlg;
    }

}
