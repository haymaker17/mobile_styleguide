package com.concur.mobile.core.travel.rail.activity;

import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.ConcurCore.Product;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.travel.activity.SegmentDetail;
import com.concur.mobile.core.travel.rail.data.RailSegment;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

public class RailSegmentDetail extends SegmentDetail {

    RailSegment seg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.segment_rail);

        // Disable rail cancel for now MOB-11748.
        // MOB-11268 - Only allowing canceling Rail for Gov project for now,
        // otherwise, hide the cancel button for ConcurMobile (corporate).
        View cancelButton = findViewById(R.id.cancel);
        ConcurCore cnqr = (ConcurCore) ConcurCore.getContext();
        if (cnqr.getProduct() == Product.CORPORATE && cancelButton != null) {
            cancelButton.setVisibility(View.GONE);
        } else {
            cancelButton.setVisibility(View.VISIBLE);
        }

        if (!segmentInitDelayed) {
            buildView();
        }
    }

    @Override
    protected void onServiceAvailable() {
        super.onServiceAvailable();
        buildView();
    }

    protected void buildView() {
        seg = (RailSegment) super.seg;

        if (seg == null) {
            // Something is wrong here. Get out of this activity and back to wherever to hopefully reload the trips.
            // MOB-10690
            finish();
            return;
        }

        setText(R.id.railFromTo, com.concur.mobile.base.util.Format.localizeText(this, R.string.segment_fromto,
                seg.startRailStationLocalized, seg.endRailStationLocalized));

        setText(R.id.railConfirm,
                com.concur.mobile.base.util.Format.localizeText(this, R.string.general_confirmnum, seg.confirmNumber));

        StringBuilder sb = new StringBuilder(seg.vendorName).append(' ').append(seg.trainNumber);
        if (!isBlank(seg.operatedByVendor)) {
            sb.append(" (").append(seg.operatedByVendor).append(' ').append(seg.operatedByTrainNumber).append(')');
        }
        setText(R.id.railTrain, sb);

        // Depart side
        sb.setLength(0);
        if (isBlank(seg.startRailStation)) {
            sb.append(getText(R.string.general_depart));
        } else {
            sb.append(getText(R.string.general_depart)).append(" (").append(seg.startRailStation).append(')');
        }
        setText(R.id.railDepartCity, sb);

        populateTimeFlipper(R.id.railDepartTime, seg.getStartDateLocal());

        setText(R.id.railDepartDate, Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY, seg.getStartDateLocal()));

        CharSequence stops;
        if (seg.numStops != null) {
            stops = seg.numStops.toString();
        } else {
            stops = Const.NA;
        }
        sb.setLength(0);
        sb.append(getText(R.string.segment_rail_label_stops)).append(' ').append(stops);
        setText(R.id.railStops, sb);

        // Arrive side
        sb.setLength(0);
        if (isBlank(seg.endRailStation)) {
            sb.append(getText(R.string.general_arrive));
        } else {
            sb.append(getText(R.string.general_arrive)).append(" (").append(seg.endRailStation).append(')');
        }
        setText(R.id.railArriveCity, sb);

        populateTimeFlipper(R.id.railArriveTime, seg.getEndDateLocal());

        setText(R.id.railArriveDate, Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY, seg.getEndDateLocal()));

        CharSequence duration;
        if (seg.duration != null) {
            duration = FormatUtil.formatElapsedTime(this, seg.duration, true);
        } else {
            // Attempt to compute a duration
            if (seg.getStartDateUtc() != null && seg.getEndDateUtc() != null) {
                long startMillis = seg.getStartDateUtc().getTimeInMillis();
                long endMillis = seg.getEndDateUtc().getTimeInMillis();
                long minutes = (endMillis - startMillis) / 60000;
                duration = FormatUtil.formatElapsedTime(this, (int) minutes, true);
            } else {
                duration = Const.NA;
            }
        }
        sb.setLength(0);
        sb.append(getText(R.string.segment_rail_label_duration)).append(' ').append(duration);
        setText(R.id.railDuration, sb);

        // Everything else
        populateField(R.id.railStatus, R.string.segment_status, seg.statusLocalized);

        sb.setLength(0);
        if (!isBlank(seg.startRailStation)) {
            sb.append('(').append(seg.startRailStation).append(") ").append(seg.startRailStationLocalized);
        } else if (!isBlank(seg.startRailStationLocalized)) {
            sb.append(seg.startRailStationLocalized);
        }
        populateField(R.id.railDepartLocation, R.string.segment_rail_label_depart_station, sb);
        linkMap(R.id.railDepartLocation, seg.startRailStationLocalized);

        populateField(R.id.railDepartPlatform, R.string.segment_rail_label_depart_platform, textOrNA(seg.startPlatform));

        populateField(R.id.railDepartWagon, R.string.segment_rail_label_wagon, textOrNA(seg.wagonNumber));

        sb.setLength(0);
        if (!isBlank(seg.endRailStation)) {
            sb.append('(').append(seg.endRailStation).append(") ").append(seg.endRailStationLocalized);
        } else if (!isBlank(seg.endRailStationLocalized)) {
            sb.append(seg.endRailStationLocalized);
        }
        populateField(R.id.railArriveLocation, R.string.segment_rail_label_arrive_station, sb);
        linkMap(R.id.railArriveLocation, seg.endRailStationLocalized);

        populateField(R.id.railArrivePlatform, R.string.segment_rail_label_arrive_platform, textOrNA(seg.endPlatform));

        // Hide the cancel button if the booking source is *not* 'Amtrak'.
        View view = findViewById(R.id.cancel);
        if (view != null) {
            if (seg != null && seg.bookingSource != null
                    && !seg.bookingSource.equalsIgnoreCase(Const.RAIL_BOOKING_SOURCE_AMTRAK)) {
                view.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected String getHeaderTitle() {
        return getText(R.string.segment_rail_detail_title).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.travel.SegmentDetail#getSegmentCancelConfirmDialogMessage()
     */
    @Override
    protected String getSegmentCancelConfirmDialogMessage() {
        return com.concur.mobile.base.util.Format.localizeText(this, R.string.dlg_rail_confirm_cancel_message,
                ((RailSegment) seg).startRailStation,
                Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_DISPLAY, seg.getStartDayLocal()),
                Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_DISPLAY, seg.getEndDayLocal()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.travel.SegmentDetail#getSegmentCancelConfirmDialogTitle()
     */
    @Override
    protected String getSegmentCancelConfirmDialogTitle() {
        return getText(R.string.dlg_rail_confirm_cancel_title).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.travel.SegmentDetail#getSegmentCancelFilter()
     */
    @Override
    protected IntentFilter getSegmentCancelFilter() {
        return new IntentFilter(Const.ACTION_RAIL_CANCEL_RESULT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.travel.SegmentDetail#getSegmentCancelProgressDialogMessage()
     */
    @Override
    protected String getSegmentCancelProgressDialogMessage() {
        return getText(R.string.dlg_rail_cancel_progress_message).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.travel.SegmentDetail#getSegmentCancelSuccessDialogMessage()
     */
    @Override
    protected String getSegmentCancelSuccessDialogMessage() {
        return getText(R.string.dlg_rail_cancel_succeeded_message).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.travel.SegmentDetail#getSegmentCancelSuccessDialogTitle()
     */
    @Override
    protected String getSegmentCancelSuccessDialogTitle() {
        return getText(R.string.dlg_rail_cancel_succeeded_title).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.SegmentDetail#getSegmentCancelFailedDialogTitle()
     */
    @Override
    protected String getSegmentCancelFailedDialogTitle() {
        return getText(R.string.dlg_rail_cancel_failed_title).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.travel.SegmentDetail#sendCancelSegmentRequest()
     */
    @Override
    protected ServiceRequest sendCancelSegmentRequest() {
        ServiceRequest request = null;
        String bookingSource = seg.bookingSource;
        String reason = null;
        String recordLocator = seg.locator;
        String segmentKey = seg.segmentKey;
        String tripId = trip.itinLocator;

        // Make the call
        ConcurService svc = getConcurService();
        if (svc != null) {
            request = svc.sendCancelRailRequest(bookingSource, reason, recordLocator, segmentKey, tripId);
        }
        return request;
    }
}
