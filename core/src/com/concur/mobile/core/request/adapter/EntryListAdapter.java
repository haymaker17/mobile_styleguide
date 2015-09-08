package com.concur.mobile.core.request.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.concur.core.R;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.request.dto.RequestEntryDTO;
import com.concur.mobile.platform.request.dto.RequestSegmentDTO;
import com.concur.mobile.platform.request.groupConfiguration.SegmentType;
import com.concur.mobile.platform.request.util.DateUtil;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EntryListAdapter extends AbstractGenericAdapter<RequestEntryDTO> {

    private Locale locale = null;
    private Context context = null;

    public EntryListAdapter(Context context, List<RequestEntryDTO> listEntries, Locale locale) {
        super(context, listEntries);
        this.locale = locale;
        this.context = context;
    }

    @Override public void updateList(List<RequestEntryDTO> listSegments) {
        clearListItems();
        if (listSegments != null) {
            getList().addAll(listSegments);
        }
        notifyDataSetChanged();
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout row = (RelativeLayout) convertView;

        if (row == null) {
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            row = (RelativeLayout) inflater.inflate(R.layout.request_detail_row, parent, false);
        }

        final RequestEntryDTO entry = getItem(position);

        final TextView type = (TextView) row.findViewById(R.id.segmentTypeRow);
        final TextView foreignAmount = (TextView) row.findViewById(R.id.segmentRowAmount);
        final TextView segmentTrip = (TextView) row.findViewById(R.id.segmentTrip);
        final TextView segmentStartDate = (TextView) row.findViewById(R.id.segmentStartDate);

        final String formattedAmount = FormatUtil
                .formatAmount(entry.getForeignAmount() != null ? entry.getForeignAmount() : 0,
                        getContext().getResources().getConfiguration().locale,
                        entry.getForeignCurrencyCode() != null ? entry.getForeignCurrencyCode() : "", true, true);

        type.setText(entry.getSegmentType());
        foreignAmount.setText(formattedAmount);

        type.setTypeface(Typeface.DEFAULT_BOLD);
        foreignAmount.setTypeface(Typeface.DEFAULT_BOLD);

        segmentStartDate.setText(getEarliestDate(entry));
        if (entry.getListSegment() != null && entry.getListSegment().size() > 0) {
            RequestSegmentDTO segment = entry.getListSegment().iterator().next();
            segmentTrip.setText(getEntryTripText(segment, entry.getSegmentTypeCode()));
        } else {
            // --- Expected expense
            type.setText(entry.getExpenseTypeName());
            segmentTrip.setText("");
        }

        return row;
    }

    private String getEntryTripText(RequestSegmentDTO segment, String segmentTypeCode) {
        if (segmentTypeCode.equals(SegmentType.RequestSegmentType.AIR.getCode()) || segmentTypeCode
                .equals(SegmentType.RequestSegmentType.RAIL.getCode())) {
            // --- TODO this might not work as is for roundtrip / multileg entries - investigate
            return com.concur.mobile.base.util.Format
                    .localizeText(context, R.string.tr_flight_destination_label, segment.getFromLocationName(),
                            segment.getToLocationName());
        }
        return segment.getToLocationName();
    }

    private String getEarliestDate(RequestEntryDTO entry) {
        if (entry.getListSegment() != null && entry.getListSegment().size() > 0) {
            Date earliest = null;
            for (RequestSegmentDTO segment : entry.getListSegment()) {
                if (earliest == null || segment.getDepartureDate() != null && earliest.getTime() > segment
                        .getDepartureDate().getTime()) {
                    earliest = segment.getDepartureDate();
                }
            }
            return DateUtil.getFormattedDateForLocale(DateUtil.DatePattern.SHORT, locale, earliest);
        }
        return "";
    }

    @Override public boolean isEnabled(int position) {
        // --- XXX we do not enable expected expense for now
        return getItem(position).getListSegment().size() > 0;
    }
}
