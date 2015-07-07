package com.concur.mobile.core.expense.travelallowance.adapter;

import android.content.Context;
import android.text.format.DateUtils;
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
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

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
            renderDeparture((CompactItinerarySegment) getItem(i));
        } else {
            if (updateController.getCompactItinerarySegments().size() == i + 1) {
                renderArrival((CompactItinerarySegment) getItem(i));
            } else {
                renderDestination((CompactItinerarySegment) getItem(i));
            }
        }
        return resultView;
    }

    private void renderDeparture(final CompactItinerarySegment segment) {
        if (segment == null) {
            return;
        }

        renderCommonParts(segment, false);

        if (holder.tvTitle != null) {
            holder.tvTitle.setText("@Departure@");
        }

        if (segment.getDepartureDateTime() != null) {
            String dateStr;
            if (holder.tvDateValue != null) {
                dateStr = DateUtils.formatDateTime(context, segment.getDepartureDateTime().getTime(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH);
                holder.tvDateValue.setText(dateStr);
            }
            if (holder.tvTimeValue != null) {
                dateStr = DateUtils.formatDateTime(context, segment.getDepartureDateTime().getTime(),
                        DateUtils.FORMAT_SHOW_TIME);
                holder.tvTimeValue.setText(dateStr);
            }
        }
    }

    private void renderArrival(final CompactItinerarySegment segment) {
        if (segment == null) {
            return;
        }

       renderCommonParts(segment, false);

        if (holder.tvTitle != null) {
            holder.tvTitle.setText("@Arrival@");
        }

        if (segment.getArrivalDateTime() != null) {
            String dateStr;
            if (holder.tvDateValue != null) {
                dateStr = DateUtils.formatDateTime(context, segment.getArrivalDateTime().getTime(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR
                                | DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH);
                holder.tvDateValue.setText(dateStr);
            }
            if (holder.tvTimeValue != null) {
                dateStr = DateUtils.formatDateTime(context, segment.getArrivalDateTime().getTime(),
                        DateUtils.FORMAT_SHOW_TIME);
                holder.tvTimeValue.setText(dateStr);
            }
        }
    }

    private void renderDestination(final CompactItinerarySegment segment) {
        if (segment == null) {
            return;
        }

        renderCommonParts(segment, true);

        if (holder.tvTitle != null) {
            holder.tvTitle.setText("@Destination@");
        }

        String periodDateStr = StringUtilities.EMPTY_STRING;
        if (segment.getArrivalDateTime() != null) {
            String arrivalDateStr;
            if (holder.tvArrivalTimeValue != null) {
                arrivalDateStr = DateUtils.formatDateTime(context, segment.getArrivalDateTime().getTime(),
                        DateUtils.FORMAT_SHOW_TIME);
                holder.tvArrivalTimeValue.setText(arrivalDateStr);
            }
            if (holder.tvPeriodValue != null) {
                periodDateStr = DateUtils.formatDateTime(context, segment.getArrivalDateTime().getTime(),
                                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
                                    | DateUtils.FORMAT_SHOW_YEAR
                                    | DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH);
            }
        }

        if (segment.getDepartureDateTime() != null) {
            String departureDateStr = StringUtilities.EMPTY_STRING;;
            if (holder.tvDepartureTimeValue != null) {
                departureDateStr = DateUtils.formatDateTime(context, segment.getDepartureDateTime().getTime(),
                        DateUtils.FORMAT_SHOW_TIME);
                holder.tvDepartureTimeValue.setText(departureDateStr);
            }
            if (holder.tvPeriodValue != null) {
                departureDateStr = DateUtils.formatDateTime(context, segment.getDepartureDateTime().getTime(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
                                | DateUtils.FORMAT_SHOW_YEAR
                                | DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH);
            }
            if (periodDateStr != null) {
                periodDateStr = periodDateStr + " - " + departureDateStr;
            } else {
                periodDateStr = departureDateStr;
            }
        }

        if (!StringUtilities.isNullOrEmpty(periodDateStr)) {
            holder.tvPeriodValue.setText(periodDateStr);
        }

    }

    private void renderCommonParts(final CompactItinerarySegment segment, final boolean isDestination) {

        if (segment == null) {
            return;
        }

        renderIcon(isDestination);
        renderSeparator(isDestination);

        if (holder.tvLocationValue != null) {
            holder.tvLocationValue.setText(segment.getLocation().getName());
        }

        if (isDestination) {
            if (holder.vgDateTimeContainer != null) {
                holder.vgDateTimeContainer.setVisibility(View.GONE);
            }
            if (holder.vgPeriod != null) {
                holder.vgPeriod.setVisibility(View.VISIBLE);
            }

        } else {
            if (holder.vgDateTimeContainer != null) {
                holder.vgDateTimeContainer.setVisibility(View.VISIBLE);
            }
            if (holder.vgPeriod != null) {
                holder.vgPeriod.setVisibility(View.GONE);
            }
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

    private void renderSeparator (final boolean isDestination) {
        if (isDestination) {
            if (holder.vSeparatorLong != null) {
                holder.vSeparatorLong.setVisibility(View.GONE);
            }
            if (holder.vSeparatorShort != null) {
                holder.vSeparatorShort.setVisibility(View.VISIBLE);
            }
        } else {
            if (holder.vSeparatorLong != null) {
                holder.vSeparatorLong.setVisibility(View.VISIBLE);
            }
            if (holder.vSeparatorShort != null) {
                holder.vSeparatorShort.setVisibility(View.GONE);
            }
        }
    }

}
