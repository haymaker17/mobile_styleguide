package com.concur.mobile.core.expense.travelallowance.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.ui.model.PositionInfoTag;
import com.concur.mobile.core.expense.travelallowance.util.ItineraryUtils;
import com.concur.mobile.core.expense.travelallowance.util.Message;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.util.List;

/**
 * Created by Michael Becherer on 03-Jul-15.
 */
public class ItineraryUpdateListAdapter extends ArrayAdapter<Object> {

    private static final String CLASS_TAG = ItineraryUpdateListAdapter.class.getSimpleName();
    private static final int LAYOUT_ID = R.layout.ta_edit_segment;

    private final Context context;
    private ViewHolder holder;
    private OnClickListener onTimeClickListener;
    private OnClickListener onDateClickListener;
    private OnClickListener onLocationClickListener;
    private OnClickListener onItemClickListener;
    private List<Message> messageList;
    private List<ItinerarySegment> segments;

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    /**
     * Holds all UI controls needed for rendering
     */
    private final class ViewHolder {

        private View vMessageArea;
        private ImageView ivIcon;
        private TextView tvMessage;

        private View vDepartureLocation;
        private ImageView ivDepartureSameLocation;
        private ImageView ivDepartureLocation;
        private TextView tvDepartureLocationLabel;
        private TextView tvDepartureLocationValue;
        private View vDepartureDateTime;
        private View vgDepartureDate;
        private TextView tvDepartureDateLabel;
        private TextView tvDepartureDateValue;
        private View vgDepartureTime;
        private TextView tvDepartureTimeLabel;
        private TextView tvDepartureTimeValue;
        private View vArrivalLocation;
        private ImageView ivArrivalSameLocation;
        private ImageView ivArrivalLocation;
        private TextView tvArrivalLocationLabel;
        private TextView tvArrivalLocationValue;
        private View vArrivalDateTime;
        private View vgArrivalDate;
        private TextView tvArrivalDateLabel;
        private TextView tvArrivalDateValue;
        private View vgArrivalTime;
        private TextView tvArrivalTimeLabel;
        private TextView tvArrivalTimeValue;
    }

    public ItineraryUpdateListAdapter(final Context context, final OnClickListener onItemClickListener,
                                      final OnClickListener onLocationClickListener,
            final OnClickListener onDateClickListener, final OnClickListener onTimeClickListener,
            List<ItinerarySegment> itinerarySegments) {

        super(context, LAYOUT_ID);

        this.context = context;
        this.onLocationClickListener = onLocationClickListener;
        this.onDateClickListener = onDateClickListener;
        this.onTimeClickListener = onTimeClickListener;
        this.onItemClickListener = onItemClickListener;
        this.segments = itinerarySegments;
        addAll(itinerarySegments);
    }

    /**
     * Creates the member view holder
     *
     * @param view The inflated view to grab the IDs from
     */
    private void createViewHolder(final View view) {
        holder = new ViewHolder();

        holder.vMessageArea = view.findViewById(R.id.v_message_area);
        if (holder.vMessageArea != null) {
            holder.ivIcon = (ImageView) holder.vMessageArea.findViewById(R.id.iv_icon);
            holder.tvMessage = (TextView) holder.vMessageArea.findViewById(R.id.tv_message);
        }

        holder.vDepartureLocation = view.findViewById(R.id.v_departure_location);
        if (holder.vDepartureLocation != null) {
            holder.tvDepartureLocationLabel = (TextView) holder.vDepartureLocation.findViewById(R.id.tv_location_label);
            holder.tvDepartureLocationValue = (TextView) holder.vDepartureLocation.findViewById(R.id.tv_location_value);
            holder.ivDepartureLocation = (ImageView) holder.vDepartureLocation.findViewById(R.id.iv_location_icon);
            holder.ivDepartureSameLocation = (ImageView) holder.vDepartureLocation.findViewById(R.id.iv_same_location_icon);
        }
        holder.vDepartureDateTime = view.findViewById(R.id.v_departure_date_time);
        if (holder.vDepartureDateTime != null) {
            holder.vgDepartureDate = holder.vDepartureDateTime.findViewById(R.id.vg_date);
            holder.tvDepartureDateLabel = (TextView) holder.vDepartureDateTime.findViewById(R.id.tv_date_label);
            holder.tvDepartureDateValue = (TextView) holder.vDepartureDateTime.findViewById(R.id.tv_date_value);
            holder.vgDepartureTime =  holder.vDepartureDateTime.findViewById(R.id.vg_time);
            holder.tvDepartureTimeLabel = (TextView) holder.vDepartureDateTime.findViewById(R.id.tv_time_label);
            holder.tvDepartureTimeValue = (TextView) holder.vDepartureDateTime.findViewById(R.id.tv_time_value);
        }
        holder.vArrivalLocation = view.findViewById(R.id.v_arrival_location);
        if (holder.vArrivalLocation != null) {
            holder.tvArrivalLocationLabel = (TextView) holder.vArrivalLocation.findViewById(R.id.tv_location_label);
            holder.tvArrivalLocationValue = (TextView) holder.vArrivalLocation.findViewById(R.id.tv_location_value);
            holder.ivArrivalLocation = (ImageView) holder.vArrivalLocation.findViewById(R.id.iv_location_icon);
            holder.ivArrivalSameLocation = (ImageView) holder.vArrivalLocation.findViewById(R.id.iv_same_location_icon);
        }
        holder.vArrivalDateTime = view.findViewById(R.id.v_arrival_date_time);
        if (holder.vArrivalDateTime != null) {
            holder.vgArrivalDate = holder.vArrivalDateTime.findViewById(R.id.vg_date);
            holder.tvArrivalDateLabel = (TextView) holder.vArrivalDateTime.findViewById(R.id.tv_date_label);
            holder.tvArrivalDateValue = (TextView) holder.vArrivalDateTime.findViewById(R.id.tv_date_value);
            holder.vgArrivalTime =  holder.vArrivalDateTime.findViewById(R.id.vg_time);
            holder.tvArrivalTimeLabel = (TextView) holder.vArrivalDateTime.findViewById(R.id.tv_time_label);
            holder.tvArrivalTimeValue = (TextView) holder.vArrivalDateTime.findViewById(R.id.tv_time_value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        View resultView = null;
        ItinerarySegment segment = (ItinerarySegment) getItem(i);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            resultView = inflater.inflate(LAYOUT_ID, viewGroup, false);
            createViewHolder(resultView);
            resultView.setTag(holder);
            if (this.onItemClickListener != null) {
                if (holder.vMessageArea != null) {
                    holder.vMessageArea.setOnClickListener(onItemClickListener);
                }
            }
            if (this.onLocationClickListener != null) {
                if (holder.vDepartureLocation != null) {
                    holder.vDepartureLocation.setOnClickListener(onLocationClickListener);
                    holder.vDepartureLocation.setClickable(true);
                }
                if (holder.vArrivalLocation != null) {
                    holder.vArrivalLocation.setOnClickListener(onLocationClickListener);
                    holder.vArrivalLocation.setClickable(true);
                }
            }
            if (this.onDateClickListener != null) {
                if (holder.vgDepartureDate != null) {
                    holder.vgDepartureDate.setOnClickListener(onDateClickListener);
                    holder.vgDepartureDate.setClickable(true);
                }
                if (holder.vgArrivalDate != null) {
                    holder.vgArrivalDate.setOnClickListener(onDateClickListener);
                    holder.vgArrivalDate.setClickable(true);
                }
            }
            if (this.onTimeClickListener != null) {
                if (holder.vgDepartureTime != null) {
                    holder.vgDepartureTime.setOnClickListener(onTimeClickListener);
                    holder.vgDepartureTime.setClickable(true);
                }
                if (holder.vgArrivalTime != null) {
                    holder.vgArrivalTime.setOnClickListener(onTimeClickListener);
                    holder.vgArrivalTime.setClickable(true);
                }
            }
        } else {
            resultView = convertView;
            holder = (ViewHolder) resultView.getTag();
        }

        setPositionInfoTag(holder.vDepartureLocation, i, PositionInfoTag.INFO_OUTBOUND);
        setPositionInfoTag(holder.vgDepartureDate, i, PositionInfoTag.INFO_OUTBOUND);
        setPositionInfoTag(holder.vgDepartureTime, i, PositionInfoTag.INFO_OUTBOUND);

        setPositionInfoTag(holder.vArrivalLocation, i, PositionInfoTag.INFO_INBOUND);
        setPositionInfoTag(holder.vgArrivalDate, i, PositionInfoTag.INFO_INBOUND);
        setPositionInfoTag(holder.vgArrivalTime, i, PositionInfoTag.INFO_INBOUND);

        renderMessageArea(segment);
        renderDeparture(segment);
        renderArrival(segment);

        return resultView;
    }

    private void setPositionInfoTag(final View view, int position, int info) {
        if (view != null) {
            PositionInfoTag positionInfoTag = new PositionInfoTag(position, info);
            view.setTag(R.id.tag_key_position, positionInfoTag);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled(int position) {
        // TODO PK: This is set to true in order to enable context menu on list items. The delete feature is currently implemented
        // in the context menu.
        return false;
    }

    private void renderMessageArea(final ItinerarySegment segment) {
        if (holder.vMessageArea == null || segment == null) {
            return;
        }

        holder.vMessageArea.setVisibility(View.GONE);
        if (segment.isLocked()) {
            holder.vMessageArea.setVisibility(View.VISIBLE);
            if (holder.ivIcon != null) {
                holder.ivIcon.setImageResource(R.drawable.profile_icon_bank);
                holder.ivIcon.setVisibility(View.VISIBLE);
            }
            if (holder.tvMessage != null) {
                holder.tvMessage.setText(R.string.general_item_locked);
            }
        } else {
            Message msg = null;
            if (messageList != null) {
                msg = ItineraryUtils.findMessage(messageList, segment);
            }
            if (msg != null && msg.getSeverity() == Message.Severity.ERROR) {
                holder.vMessageArea.setVisibility(View.VISIBLE);
                if (holder.ivIcon != null) {
                    holder.ivIcon.setImageResource(R.drawable.icon_redex);
                    holder.ivIcon.setVisibility(View.VISIBLE);
                }
                if (holder.tvMessage != null) {
                    holder.tvMessage.setText(msg.getMessageText(context));
                }
            }
        }
    }

    private void renderDeparture(final ItinerarySegment segment) {
        if (segment == null) {
            return;
        }

        if (holder.vgDepartureDate != null) {
            holder.vgDepartureDate.setEnabled(!segment.isLocked());
        }
        if (holder.vgDepartureTime != null) {
            holder.vgDepartureTime.setEnabled(!segment.isLocked());
        }
        if (holder.vDepartureLocation != null) {
            holder.vDepartureLocation.setEnabled(!segment.isLocked());
        }
        if (holder.tvDepartureLocationLabel != null) {
            holder.tvDepartureLocationLabel.setText(R.string.general_from_location);
        }
        if (holder.tvDepartureLocationValue != null) {
            if (segment.getDepartureLocation() != null) {
                holder.tvDepartureLocationValue.setText(segment.getDepartureLocation().getName());
            } else {
                holder.tvDepartureLocationValue.setText(StringUtilities.EMPTY_STRING);
            }
            holder.tvDepartureLocationValue.setEnabled(!segment.isLocked());
        }
        if (holder.tvDepartureDateLabel != null) {
            holder.tvDepartureDateLabel.setText(R.string.ta_departure_date);
        }
        if (holder.tvDepartureTimeLabel != null) {
            holder.tvDepartureTimeLabel.setText(R.string.general_time);
        }
        if (segment.getDepartureDateTime() != null) {
            String dateStr;
            if (holder.tvDepartureDateValue != null) {
                dateStr = DateUtils.formatDateTime(context, segment.getDepartureDateTime().getTime(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
                        | DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH);
                holder.tvDepartureDateValue.setText(dateStr);
            }
            if (holder.tvDepartureTimeValue != null) {
                dateStr = DateUtils.formatDateTime(context, segment.getDepartureDateTime().getTime(),
                        DateUtils.FORMAT_SHOW_TIME);
                holder.tvDepartureTimeValue.setText(dateStr);
            }
        } else {
            holder.tvDepartureTimeValue.setText(StringUtilities.EMPTY_STRING);
        }
        if (holder.ivDepartureLocation != null) {
            holder.ivDepartureLocation.setVisibility(View.VISIBLE);
            holder.ivDepartureSameLocation.setVisibility(View.GONE);
        }
        if (hasSameLocation(this.segments, segment, false)) {
            holder.ivDepartureLocation.setVisibility(View.GONE);
            holder.ivDepartureSameLocation.setVisibility(View.VISIBLE);
        }
    }

    private void renderArrival(final ItinerarySegment segment) {
        if (segment == null) {
            return;
        }
        if (holder.vgArrivalDate != null) {
            holder.vgArrivalDate.setEnabled(!segment.isLocked());
        }
        if (holder.vgArrivalTime != null) {
            holder.vgArrivalTime.setEnabled(!segment.isLocked());
        }
        if (holder.vArrivalLocation != null) {
            holder.vArrivalLocation.setEnabled(!segment.isLocked());
        }
        if (holder.tvArrivalLocationLabel != null) {
            holder.tvArrivalLocationLabel.setText(R.string.general_to_location);
        }
        if (holder.tvArrivalLocationValue != null) {
            if (segment.getArrivalLocation() != null) {
                holder.tvArrivalLocationValue.setText(segment.getArrivalLocation().getName());
            } else {
                holder.tvArrivalLocationValue.setText(StringUtilities.EMPTY_STRING);
            }
        }
        if (holder.tvArrivalDateLabel != null) {
            holder.tvArrivalDateLabel.setText(R.string.ta_arrival_date);
        }
        if (holder.tvArrivalTimeLabel != null) {
            holder.tvArrivalTimeLabel.setText(R.string.general_time);
        }
        if (segment.getArrivalDateTime() != null) {
            String dateStr;
            if (holder.tvArrivalDateValue != null) {
                dateStr = DateUtils.formatDateTime(context, segment.getArrivalDateTime().getTime(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
                                | DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH);
                holder.tvArrivalDateValue.setText(dateStr);
            }
            if (holder.tvArrivalTimeValue != null) {
                dateStr = DateUtils.formatDateTime(context, segment.getArrivalDateTime().getTime(),
                        DateUtils.FORMAT_SHOW_TIME);
                holder.tvArrivalTimeValue.setText(dateStr);
            }
        } else {
            holder.tvArrivalTimeValue.setText(StringUtilities.EMPTY_STRING);
        }
        if (holder.ivArrivalLocation != null) {
            holder.ivArrivalLocation.setVisibility(View.VISIBLE);
            holder.ivArrivalSameLocation.setVisibility(View.GONE);
        }
        if (hasSameLocation(this.segments, segment, true)) {
            holder.ivArrivalLocation.setVisibility(View.GONE);
            holder.ivArrivalSameLocation.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Checks the given segment, whether it has the same location like its
     * predecessor or successor. The method expects a sorted segment list.
     * @param segments the list holding the segments in question
     * @param segment the given segment
     * @param likeSuccessor if true, the succeeding departure (by means within the next segment)
     *                      of the given arrival location needs to be checked;
     *                      if false, the preceding arrival (by means within the previous segment)
     *                      of the given departure location needs to be checked.
     * @return true, if the locations are equal; otherwise false.
     */
    public boolean hasSameLocation(List<ItinerarySegment> segments, ItinerarySegment segment, boolean likeSuccessor) {
        if (segments == null || segment == null) {
            return false;
        }
        int position =  segments.indexOf(segment);
        if (position < 0 || position + 1 > segments.size()) {
            return false;
        }
        ItineraryLocation arrival;
        ItineraryLocation departure;
        if (likeSuccessor) {
            if (position + 1 >= segments.size()) {
                return false;
            }
            arrival = segment.getArrivalLocation();
            departure = segments.get(position + 1).getDepartureLocation();
        } else {
            if (position <= 0) {
                return false;
            }
            arrival = segments.get(position - 1).getArrivalLocation();
            departure = segment.getDepartureLocation();
        }
        if (arrival == null && departure == null) {
            return false;
        }
        if (arrival != null) {
            return arrival.equals(departure);
        }
        if (departure != null) {
            return departure.equals(arrival);
        }
        return false;
    }
}
