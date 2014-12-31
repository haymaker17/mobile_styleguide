package com.concur.mobile.core.travel.car.activity;

import java.util.Locale;

import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.util.Linkify;

import com.concur.core.R;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.travel.activity.SegmentDetail;
import com.concur.mobile.core.travel.car.data.CarSegment;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.AsyncImageView;
import com.concur.mobile.platform.util.Format;

public class CarSegmentDetail extends SegmentDetail {

    CarSegment seg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.segment_car);

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
        seg = (CarSegment) super.seg;

        if (seg == null) {
            // Something is wrong here. Get out of this activity and back to wherever to hopefully reload the trips.
            // MOB-10690
            finish();
            return;
        }

        setText(R.id.carVendor, seg.vendorName);

        AsyncImageView aiv = (AsyncImageView) findViewById(R.id.carModelImage);
        aiv.setAsyncUri(seg.imageCarUri);

        StringBuilder sb = new StringBuilder(getText(R.string.segment_confirm)).append(' ').append(seg.confirmNumber);
        setText(R.id.carConfirm, sb.toString());

        sb.setLength(0);
        sb.append(getText(R.string.segment_car_pickup)).append(' ')
                .append(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_TIME_DISPLAY, seg.getStartDateLocal()));
        setText(R.id.carPickup, sb.toString());

        sb.setLength(0);
        sb.append(getText(R.string.segment_car_return)).append(' ')
                .append(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_TIME_DISPLAY, seg.getEndDateLocal()));
        setText(R.id.carDropoff, sb.toString());

        if (seg.phoneNumber != null && seg.phoneNumber.trim().length() > 0) {
            String formattedNumber = PhoneNumberUtils.formatNumber(PhoneNumberUtils.stripSeparators(seg.phoneNumber));
            populateField(R.id.carPhone, R.string.segment_phone, formattedNumber, Linkify.PHONE_NUMBERS);
        } else {
            hideField(R.id.carPhone);
        }

        String startAddress;
        if (!isBlank(seg.startAddress)) {
            startAddress = seg.startAddress;
        } else {
            startAddress = seg.startAirportName;
        }
        String city = isBlank(seg.startCity) ? seg.startAirportCity : seg.startCity;

        StringBuilder carLoc = new StringBuilder(startAddress).append(", ").append(city).append(", ")
                .append(seg.startAirportState).append(", ").append(seg.startAirportCountry);
        linkMap(R.id.carLocation, carLoc.toString());

        if (!isBlank(seg.startAddress)) {
            populateField(R.id.carLocation, R.string.segment_location, carLoc.toString());
        } else {
            StringBuilder carLocWithCode = new StringBuilder("(").append(seg.startCityCode).append(") ").append(carLoc);
            populateField(R.id.carLocation, R.string.segment_location, carLocWithCode.toString());

        }

        /*
         * StringBuilder carType = new StringBuilder(seg.classOfCarLocalized).append(", ")
         * .append(seg.transmissionLocalized).append(", ").append(seg.airConditionLocalized);
         */
        StringBuilder carType = new StringBuilder("");
        carType = FormatUtil.concateStringWithDelim(carType, defaultDelim, true, seg.classOfCarLocalized);
        carType = FormatUtil.concateStringWithDelim(carType, ' ', true, seg.bodyLocalized);
        carType = FormatUtil.concateStringWithDelim(carType, defaultDelim, true, seg.transmissionLocalized);
        carType = FormatUtil.concateStringWithDelim(carType, defaultDelim, true, seg.airConditionLocalized);
        populateField(R.id.carType, R.string.segment_car_type, carType.toString(), true);

        populateField(R.id.carStatus, R.string.segment_status, seg.statusLocalized);

        final Locale locale = getResources().getConfiguration().locale;
        CharSequence dailyRate;
        if (seg.dailyRate != null) {
            dailyRate = FormatUtil.formatAmount(seg.dailyRate, locale, seg.currency, true);
        } else {
            dailyRate = Const.NA;
        }
        populateField(R.id.carDailyRate, R.string.segment_car_rate_daily, dailyRate);

        CharSequence totalRate;
        if (seg.totalRate != null) {
            totalRate = FormatUtil.formatAmount(seg.totalRate, locale, seg.currency, true);
        } else {
            totalRate = Const.NA;
        }
        populateField(R.id.carTotalRate, R.string.segment_car_rate_total, totalRate);

        if (!trip.allowCancel) {
            hideField(R.id.cancel);
        }
    }

    @Override
    protected String getHeaderTitle() {
        return getText(R.string.car_detail_title).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.travel.SegmentDetail#getSegmentCancelConfirmDialogMessage()
     */
    @Override
    protected String getSegmentCancelConfirmDialogMessage() {
        return com.concur.mobile.base.util.Format.localizeText(this, R.string.dlg_car_confirm_cancel_message,
                ((CarSegment) seg).startAirportCity,
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
        return getText(R.string.dlg_car_confirm_cancel_title).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.travel.SegmentDetail#getSegmentCancelFilter()
     */
    @Override
    protected IntentFilter getSegmentCancelFilter() {
        return new IntentFilter(Const.ACTION_CAR_CANCEL_RESULT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.travel.SegmentDetail#getSegmentCancelProgressDialogMessage()
     */
    @Override
    protected String getSegmentCancelProgressDialogMessage() {
        return getText(R.string.dlg_car_cancel_progress_message).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.travel.SegmentDetail#getSegmentCancelSuccessDialogMessage()
     */
    @Override
    protected String getSegmentCancelSuccessDialogMessage() {
        return getText(R.string.dlg_car_cancel_succeeded_message).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.travel.SegmentDetail#getSegmentCancelSuccessDialogTitle()
     */
    @Override
    protected String getSegmentCancelSuccessDialogTitle() {
        return getText(R.string.dlg_car_cancel_succeeded_title).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.SegmentDetail#getSegmentCancelFailedDialogTitle()
     */
    @Override
    protected String getSegmentCancelFailedDialogTitle() {
        return getText(R.string.dlg_car_cancel_failed_title).toString();
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
        String tripId = trip.cliqbookTripId;

        // Make the call
        ConcurService svc = getConcurService();
        if (svc != null) {
            request = svc.sendCancelCarRequest(bookingSource, reason, recordLocator, segmentKey, tripId);
        }
        return request;
    }

}
