package com.concur.mobile.core.travel.activity;

import android.os.Bundle;
import android.text.util.Linkify;

import com.concur.core.R;
import com.concur.mobile.core.travel.data.DiningSegment;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.util.Format;

public class DiningSegmentDetail extends SegmentDetail {

    DiningSegment seg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        seg = (DiningSegment) super.seg;

        if (seg == null) {
            // Something is wrong here. Get out of this activity and back to wherever to hopefully reload the trips.
            // MOB-10690
            finish();
            return;
        }

        setContentView(R.layout.segment_dining);

        getSupportActionBar().setTitle(seg.segmentName);

        setText(R.id.diningPhone, seg.phoneNumber, Linkify.PHONE_NUMBERS);

        StringBuilder addr = new StringBuilder(seg.startAddress);
        addr.append('\n').append(
                com.concur.mobile.base.util.Format.localizeText(this, R.string.general_address2, seg.startCity,
                        seg.startState, seg.startPostCode));
        if (ViewUtil.isMappingAvailable(this)) {
            setText(R.id.diningDirections, addr.toString(), Linkify.MAP_ADDRESSES);
        } else {
            setText(R.id.diningDirections, addr.toString());
        }

        setText(R.id.diningReservation,
                Format.safeFormatCalendar(FormatUtil.SHORT_DAY_TIME_DISPLAY, seg.getStartDateLocal()));

        setText(R.id.diningNumber, seg.numPersons);

    }

}
