package com.concur.mobile.core.travel.activity;

import java.util.Locale;

import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.util.Linkify;

import com.concur.core.R;
import com.concur.mobile.core.travel.data.ParkingSegment;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.util.Format;

public class ParkingSegmentDetail extends SegmentDetail {

    ParkingSegment seg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.segment_parking);

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
        seg = (ParkingSegment) super.seg;

        if (seg == null) {
            // Something is wrong here. Get out of this activity and back to wherever to hopefully reload the trips.
            // MOB-10690
            finish();
            return;
        }

        setText(R.id.parkingVendor, seg.getVendorName());

        StringBuilder sb = new StringBuilder(getText(R.string.segment_car_return)).append(' ').append(
                Format.safeFormatCalendar(FormatUtil.SHORT_DAY_TIME_DISPLAY, seg.getStartDateLocal()));
        setText(R.id.parkingDropoff, sb.toString());

        sb.setLength(0);
        sb.append(getText(R.string.segment_car_pickup)).append(' ')
                .append(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_TIME_DISPLAY, seg.getEndDateLocal()));
        setText(R.id.parkingPickup, sb.toString());

        if (!isBlank(seg.phoneNumber)) {
            String formattedNumber = PhoneNumberUtils.formatNumber(seg.phoneNumber);
            populateField(R.id.parkingPhone, R.string.segment_phone, formattedNumber, Linkify.PHONE_NUMBERS);
        } else {
            hideField(R.id.parkingPhone);
        }

        if (!isBlank(seg.startAddress)) {
            // An address
            sb.setLength(0);
            sb.append(seg.startAddress)
                    .append(' ')
                    .append(com.concur.mobile.base.util.Format.localizeText(this, R.string.general_address2,
                            seg.startCity, seg.startState, seg.startPostCode));
            if (ViewUtil.isMappingAvailable(this)) {
                populateField(R.id.parkingLocation, R.string.segment_ride_pickup_address, sb.toString(),
                        Linkify.MAP_ADDRESSES);
            } else {
                populateField(R.id.parkingLocation, R.string.segment_ride_pickup_address, sb.toString());
            }
        } else {
            // Some location
            populateField(R.id.parkingLocation, R.string.segment_ride_pickup_address, seg.startLocation);
        }

        final Locale locale = getResources().getConfiguration().locale;
        CharSequence totalRate;
        if (seg.totalRate != null) {
            totalRate = FormatUtil.formatAmount(seg.totalRate, locale, seg.currency, true);
        } else {
            totalRate = Const.NA;
        }

        populateField(R.id.parkingTotal, R.string.segment_parking_total, totalRate);

        String conf = seg.confirmNumber;
        boolean toast = true;
        if (isBlank(conf)) {
            conf = Const.NA;
            toast = false;
        }
        populateField(R.id.parkingConfirm, R.string.segment_confirm, conf, toast);
    }

    @Override
    protected String getHeaderTitle() {
        return getText(R.string.segment_parking_detail_title).toString();
    }
}
