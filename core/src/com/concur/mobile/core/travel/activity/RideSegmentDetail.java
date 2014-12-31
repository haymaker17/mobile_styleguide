package com.concur.mobile.core.travel.activity;

import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.util.Linkify;

import com.concur.core.R;
import com.concur.mobile.core.travel.data.RideSegment;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

public class RideSegmentDetail extends SegmentDetail {

    RideSegment seg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.segment_ride);

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

        seg = (RideSegment) super.seg;

        if (seg == null) {
            // Something is wrong here. Get out of this activity and back to wherever to hopefully reload the trips.
            // MOB-10690
            finish();
            return;
        }

        setText(R.id.rideVendor, seg.segmentName);

        StringBuilder sb = new StringBuilder(getText(R.string.segment_car_pickup)).append(' ').append(
                Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY, seg.getStartDateLocal()));
        setText(R.id.ridePickup, sb.toString());

        if (!isBlank(seg.phoneNumber)) {
            String formattedNumber = PhoneNumberUtils.formatNumber(seg.phoneNumber);
            populateField(R.id.ridePhone, R.string.segment_phone, formattedNumber, Linkify.PHONE_NUMBERS);
        } else {
            hideField(R.id.ridePhone);
        }

        if (!isBlank(seg.startCityCode)) {
            // Airport
            populateField(R.id.ridePickupLocation, R.string.segment_ride_pickup_address, seg.startCityCode);
        } else {
            // An address
            sb.setLength(0);
            sb.append(seg.startAddress)
                    .append(", ")
                    .append(com.concur.mobile.base.util.Format.localizeText(this, R.string.general_address2,
                            seg.startCity, seg.startState, seg.startPostCode));
            populateField(R.id.ridePickupLocation, R.string.segment_ride_pickup_address, sb.toString());
            linkMap(R.id.ridePickupLocation, sb.toString());
        }

        if (isBlank(seg.pickupInstructions)) {
            hideField(R.id.ridePickupInstructions);
        } else {
            populateField(R.id.ridePickupInstructions, R.string.segment_ride_pickup_instructions,
                    seg.pickupInstructions, true);
        }

        if (!isBlank(seg.endCityCode)) {
            // Airport
            populateField(R.id.rideDropoffLocation, R.string.segment_ride_dropoff_address, seg.endCityCode);
        } else if (!isBlank(seg.endAddress)) {
            // An address
            sb.setLength(0);
            sb.append(seg.endAddress)
                    .append(' ')
                    .append(com.concur.mobile.base.util.Format.localizeText(this, R.string.general_address2,
                            seg.endCity, seg.endState, seg.endPostCode));
            populateField(R.id.rideDropoffLocation, R.string.segment_ride_dropoff_address, sb.toString());
            linkMap(R.id.rideDropoffLocation, sb.toString());
        } else {
            hideField(R.id.rideDropoffLocation);
        }

        if (isBlank(seg.dropoffInstructions)) {
            hideField(R.id.rideDropoffInstructions);
        } else {
            populateField(R.id.rideDropoffInstructions, R.string.segment_ride_dropoff_instructions,
                    seg.dropoffInstructions, true);
        }

        if (isBlank(seg.rateDescription)) {
            hideField(R.id.rideRateDescription);
        } else {
            populateField(R.id.rideRateDescription, R.string.segment_ride_rate_description, seg.rateDescription, true);
        }

        String conf = seg.confirmNumber;
        boolean toast = true;
        if (isBlank(conf)) {
            conf = Const.NA;
            toast = false;
        }
        populateField(R.id.rideConfirm, R.string.segment_confirm, conf, toast);

        // Rate hidden until we sort out what it is. MOB-2171
        // if (seg.rate != null) {
        // StringBuilder rateText = new StringBuilder(FormatUtil.formatAmount(seg.rate, seg.currency));
        // if (seg.rateType != null && "H".equals(seg.rateType)) {
        // rateText.append(' ').append(FormatUtil.localizeText(this, R.string.segment_ride_rate_type_hourly));
        // }
        // setText(R.id.rideRate, rateText.toString());
        // } else {
        // setText(R.id.rideRate, NA_TEXT);
        // }

    }

    @Override
    protected String getHeaderTitle() {
        return getText(R.string.segment_ride_detail_title).toString();
    }
}
