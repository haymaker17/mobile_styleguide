package com.concur.mobile.core.expense.travelallowance.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.travelallowance.controller.ItineraryUpdateController;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;

import java.util.List;

/**
 * Created by Michael Becherer on 03-Jul-15.
 */
public class ItineraryUpdateListAdapter extends ArrayAdapter<Object> {

    private static final String CLASS_TAG = ItineraryUpdateListAdapter.class.getSimpleName();
    private static final int LAYOUT_ID = R.layout.itin_edit_segment;

    private Context context;
    private ItineraryUpdateController updateController;
    private List<CompactItinerarySegment> segments;
    private ViewHolder holder;

    /**
     * Holds all UI controls needed for rendering
     */
    private final class ViewHolder {
        private TextView tvTitle;
        private ImageView ivLocationIcon;
        private TextView tvLocationValue;
        private View vSeparatorLong;
        private View vSeparatorShort;
        private View vgDateTimeContainer;
        private View vgDate;
        private TextView tvDateValue;
        private View vgTime;
        private TextView tvTimeValue;
        private View vgPeriod;
        private TextView tvPeriodValue;
        private View vgArrivalTime;
        private TextView tvArrivalTimeValue;
        private View vgDepartureTime;
        private TextView tvDepartureTimeValue;
    }

    public ItineraryUpdateListAdapter(Context context) {

        super(context, LAYOUT_ID);
        this.context = context;
        ConcurCore app = (ConcurCore) context.getApplicationContext();
        this.updateController = app.getItineraryUpdateController();

        addAll(updateController.getCompactItinerarySegments());
    }

    /**
     * Creates the member view holder
     *
     * @param view The inflated view to grab the IDs from
     */
    private void createViewHolder(final View view) {
        holder = new ViewHolder();
        holder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
        holder.ivLocationIcon = (ImageView) view.findViewById(R.id.iv_location_icon);
        holder.tvLocationValue = (TextView) view.findViewById(R.id.tv_location_value);
        holder.vSeparatorLong = view.findViewById(R.id.v_separator_long);
        holder.vSeparatorShort = view.findViewById(R.id.v_separator_short);
        holder.vgDateTimeContainer = view.findViewById(R.id.vg_date_time_container);
        holder.vgDate = view.findViewById(R.id.vg_date);
        holder.tvDateValue = (TextView) view.findViewById(R.id.tv_date_value);
        holder.vgTime = view.findViewById(R.id.vg_date_time);
        holder.tvTimeValue = (TextView) view.findViewById(R.id.tv_date_time_value);
        holder.vgPeriod = view.findViewById(R.id.vg_period);
        holder.tvPeriodValue = (TextView) view.findViewById(R.id.tv_period_value);
        holder.vgArrivalTime = view.findViewById(R.id.vg_arrival_time);
        holder.tvArrivalTimeValue = (TextView) view.findViewById(R.id.tv_arrival_time_value);
        holder.vgDepartureTime = view.findViewById(R.id.vg_departure_time);
        holder.tvDepartureTimeValue = (TextView) view.findViewById(R.id.tv_departure_time_value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        View resultView = null;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            resultView = inflater.inflate(LAYOUT_ID, viewGroup, false);
            createViewHolder(resultView);
            resultView.setTag(holder);
        } else {
            resultView = convertView;
            holder = (ViewHolder) resultView.getTag();
        }

        if (i == 0) {
            renderDeparture();
        } else {
            if (updateController.getCompactItinerarySegments().size() == i + 1) {
                renderArrival();
            } else {
                renderDestination();
            }
        }
        return resultView;
    }

    private void renderDeparture() {

        renderIcon(false);
        if (holder.tvTitle != null) {
            holder.tvTitle.setText("@Departure@");
        }
        if (holder.vgDateTimeContainer != null) {
            holder.vgDateTimeContainer.setVisibility(View.VISIBLE);
        }
        if (holder.vgPeriod != null) {
            holder.vgPeriod.setVisibility(View.GONE);
        }
    }

    private void renderArrival() {

        renderIcon(false);
        if (holder.tvTitle != null) {
            holder.tvTitle.setText("@Arrival@");
        }
        if (holder.vgDateTimeContainer != null) {
            holder.vgDateTimeContainer.setVisibility(View.VISIBLE);
        }
        if (holder.vgPeriod != null) {
            holder.vgPeriod.setVisibility(View.GONE);
        }
    }

    private void renderDestination() {

        renderIcon(true);
        if (holder.tvTitle != null) {
            holder.tvTitle.setText("@Destination@");
        }
        if (holder.vgDateTimeContainer != null) {
            holder.vgDateTimeContainer.setVisibility(View.GONE);
        }
        if (holder.vgPeriod != null) {
            holder.vgPeriod.setVisibility(View.VISIBLE);
        }
    }

    private void renderIcon(final boolean isDestination) {
        if (holder.ivLocationIcon == null) {
            return;
        }
        if (isDestination) {
            holder.ivLocationIcon.setImageResource(R.drawable.icon_location_blue);
        } else {
            holder.ivLocationIcon.setImageResource(R.drawable.blue_oval);
        }
    }

}
