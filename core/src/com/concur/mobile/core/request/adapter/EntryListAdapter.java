package com.concur.mobile.core.request.adapter;

import java.util.List;

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

public class EntryListAdapter extends AbstractGenericAdapter<RequestEntryDTO> {

    public EntryListAdapter(Context context, List<RequestEntryDTO> listEntries) {
        super(context, listEntries);
    }

    @Override
    public void updateList(List<RequestEntryDTO> listSegments) {
        clearListItems();
        if (listSegments != null) {
            getList().addAll(listSegments);
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout row = (RelativeLayout) convertView;

        if (row == null) {
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            row = (RelativeLayout) inflater.inflate(R.layout.request_detail_row, parent, false);
        }

        final RequestEntryDTO entry = getItem(position);

        final TextView type = (TextView) row.findViewById(R.id.segmentTypeRow);
        final TextView foreignAmount = (TextView) row.findViewById(R.id.segmentRowAmount);

        final String formattedAmount = FormatUtil.formatAmount(entry.getForeignAmount() != null ? entry.getForeignAmount() : 0, getContext().getResources()
                .getConfiguration().locale, entry.getForeignCurrencyCode() != null ? entry.getForeignCurrencyCode() : "", true, true);

        type.setText(entry.getSegmentType());
        foreignAmount.setText(formattedAmount);

        type.setTypeface(Typeface.DEFAULT_BOLD);
        foreignAmount.setTypeface(Typeface.DEFAULT_BOLD);

        return row;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }
}
