package com.concur.mobile.core.travel.activity;

import android.os.Bundle;
import android.text.util.Linkify;

import com.concur.core.R;
import com.concur.mobile.core.travel.data.EventSegment;

public class EventSegmentDetail extends SegmentDetail {

    EventSegment seg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        seg = (EventSegment) super.seg;

        if (seg == null) {
            // Something is wrong here. Get out of this activity and back to wherever to hopefully reload the trips.
            // MOB-10690
            finish();
            return;
        }

        setContentView(R.layout.segment_event);

        getSupportActionBar().setTitle(seg.segmentName);

        setText(R.id.eventName, seg.segmentName);
        setText(R.id.eventPhone, seg.phoneNumber, Linkify.PHONE_NUMBERS);

        setText(R.id.eventConfirmation, seg.confirmNumber);
        setText(R.id.eventNumber, seg.numPersons);

        // linkMap(R.id.eventMap, this);
    }

}
