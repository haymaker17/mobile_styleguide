package com.concur.mobile.core.expense.travelallowance.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.datamodel.AssignableItinerary;
import com.concur.mobile.core.expense.travelallowance.util.DefaultDateFormat;

import java.util.List;

/**
 * Created by D049515 on 14.09.2015.
 */
public class AssignableItineraryListAdapter extends ArrayAdapter<AssignableItinerary> {

    private final class ViewHolder {
        private TextView tvTitle;
        private TextView tvValue;
        private TextView tvSubtitle1;
        private TextView tvSubtitle2;
    }

    public AssignableItineraryListAdapter(Context context, List<AssignableItinerary> objects) {
        super(context, R.layout.ta_generic_table_row_layout, objects);
    }

    private ViewHolder createViewHolder(final View view) {
        ViewHolder holder = new ViewHolder();
        holder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
        holder.tvValue = (TextView) view.findViewById(R.id.tv_value);
        holder.tvSubtitle1 = (TextView) view.findViewById(R.id.tv_subtitle_1);
        holder.tvSubtitle2 = (TextView) view.findViewById(R.id.tv_subtitle_2);
        return holder;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.ta_generic_table_row_layout, parent, false);
            view.setTag(createViewHolder(view));
        }
        holder = (ViewHolder) view.getTag();

        AssignableItinerary itin = getItem(position);

        holder.tvTitle.setText(createLocationString(itin));
        DefaultDateFormat ddf = new DefaultDateFormat(getContext());
        holder.tvSubtitle1.setText(ddf.format(itin.getStartDateTime(), false, false, true) + " - "
                + ddf.format(itin.getEndDateTime(), false, false, true));
        holder.tvSubtitle2.setText(itin.getName());
        holder.tvValue.setVisibility(View.GONE);

        return view;
    }

    private String createLocationString(AssignableItinerary itin) {
        StringBuffer sb = new StringBuffer();
        boolean firstRun = true;
        for (String s: itin.getArrivalLocations()) {
            if (!firstRun) {
                sb.append(", ");
            }
            int posCountrySep = s.indexOf(",");
            sb.append(s.substring(0, posCountrySep));
            firstRun = false;
        }
        return sb.toString();
    }
}
