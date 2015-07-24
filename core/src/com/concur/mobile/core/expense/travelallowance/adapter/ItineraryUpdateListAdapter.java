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
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.ui.model.PositionInfoTag;
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
    private boolean isSaveTriggered;

    /**
     * Holds all UI controls needed for rendering
     */
    private final class ViewHolder {
        private TextView tvTitle;
        private View vDepartureLocation;
        private View vgDepartureLocation;
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
        private View vgArrivalLocation;
        private TextView tvArrivalLocationLabel;
        private TextView tvArrivalLocationValue;
        private View vArrivalDateTime;
        private View vgArrivalDate;
        private TextView tvArrivalDateLabel;
        private TextView tvArrivalDateValue;
        private View vgArrivalTime;
        private TextView tvArrivalTimeLabel;
        private TextView tvArrivalTimeValue;
        private ImageView icon;
    }

    public ItineraryUpdateListAdapter(final Context context, final OnClickListener onLocationClickListener,
            final OnClickListener onDateClickListener, final OnClickListener onTimeClickListener,
            List<ItinerarySegment> itinerarySegments) {

        super(context, LAYOUT_ID);

        this.context = context;
        this.onLocationClickListener = onLocationClickListener;
        this.onDateClickListener = onDateClickListener;
        this.onTimeClickListener = onTimeClickListener;
        addAll(itinerarySegments);
    }

    /**
     * Method should be called when the user tries to save the data. As a consequence
     * the mandatory fields are marked as erroneous if they aren't filled.
     */
    public void setSaveMode(){
        this.isSaveTriggered = true;
    }

    /**
     * Method can be called e.g. after the user creates a new row to avoid initially error icons
     */
    public void resetSaveMode(){
        this.isSaveTriggered = false;
    }

    /**
     * Creates the member view holder
     *
     * @param view The inflated view to grab the IDs from
     */
    private void createViewHolder(final View view) {
        holder = new ViewHolder();
        holder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
        holder.icon = (ImageView) view.findViewById(R.id.iv_icon);
        holder.vDepartureLocation = view.findViewById(R.id.v_departure_location);
        if (holder.vDepartureLocation != null) {
            holder.vgDepartureLocation = holder.vDepartureLocation.findViewById(R.id.vg_location);
            holder.tvDepartureLocationLabel = (TextView) holder.vDepartureLocation.findViewById(R.id.tv_location_label);
            holder.tvDepartureLocationValue = (TextView) holder.vDepartureLocation.findViewById(R.id.tv_location_value);
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
            holder.vgArrivalLocation = holder.vArrivalLocation.findViewById(R.id.vg_location);
            holder.tvArrivalLocationLabel = (TextView) holder.vArrivalLocation.findViewById(R.id.tv_location_label);
            holder.tvArrivalLocationValue = (TextView) holder.vArrivalLocation.findViewById(R.id.tv_location_value);
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

        renderTitle(segment);
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

    private void renderTitle(final ItinerarySegment segment) {
        if (holder.tvTitle == null || segment == null) {
            return;
        }
        if (segment.getDepartureLocation() != null && segment.getArrivalLocation() != null) {
            holder.tvTitle.setText(segment.getDepartureLocation().getName() + " - " + segment.getArrivalLocation().getName());
        } else if (segment.getDepartureLocation() != null) {
            holder.tvTitle.setText(segment.getDepartureLocation().getName());
        } else if (segment.getArrivalLocation() != null) {
            holder.tvTitle.setText(segment.getArrivalLocation().getName());
        } else {
            holder.tvTitle.setText(StringUtilities.EMPTY_STRING);
        }
        holder.tvTitle.setFocusable(false);
        holder.tvTitle.setFocusableInTouchMode(false);
        holder.tvTitle.setClickable(false);
        if (segment.getMessage() != null && segment.getMessage().getSeverity() == Message.Severity.ERROR) {
            holder.tvTitle.setFocusable(true);
            holder.tvTitle.setFocusableInTouchMode(true);
            holder.tvTitle.setClickable(true);
            holder.tvTitle.requestFocus();
            holder.tvTitle.setError(segment.getMessage().getMessageText(context));
        } else {
            holder.tvTitle.setError(null);
        }

        if (segment.isLocked()) {
            holder.icon.setImageResource(R.drawable.profile_icon_bank);
            holder.icon.setVisibility(View.VISIBLE);
        } else {
            holder.icon.setVisibility(View.GONE);
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
                holder.tvDepartureLocationValue.setError(null);
            } else {
                holder.tvDepartureLocationValue.setText(StringUtilities.EMPTY_STRING);
                if (isSaveTriggered == true ) {
                    holder.tvDepartureLocationValue.setError("@Select departure location@"); //Seems to have no effect...
                }else {
                    holder.tvDepartureLocationValue.setError(null);
                }
            }
            holder.tvDepartureLocationValue.setEnabled(!segment.isLocked());
        }
        if (holder.tvDepartureDateLabel != null) {
            holder.tvDepartureDateLabel.setText("@Departure Date@");
        }
        if (holder.tvDepartureTimeLabel != null) {
            holder.tvDepartureTimeLabel.setText(R.string.general_time);
        }
        if (segment.getDepartureDateTime() != null) {
            String dateStr;
            if (holder.tvDepartureDateValue != null) {
                dateStr = DateUtils.formatDateTime(context, segment.getDepartureDateTime().getTime(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH);
                holder.tvDepartureDateValue.setText(dateStr);
                holder.tvDepartureDateValue.setError(null);
                if ((dateStr == null) ){
                    if (isSaveTriggered == true) {
                        holder.tvDepartureDateValue.setError("");
                    }else{
                        holder.tvDepartureDateValue.setError(null);
                    }
                }
            }
            if (holder.tvDepartureTimeValue != null) {
                dateStr = DateUtils.formatDateTime(context, segment.getDepartureDateTime().getTime(),
                        DateUtils.FORMAT_SHOW_TIME);
                holder.tvDepartureTimeValue.setText(dateStr);
                holder.tvDepartureTimeValue.setError(null);
            }
        } else {
            holder.tvDepartureTimeValue.setText(StringUtilities.EMPTY_STRING);
            if (isSaveTriggered == true){
                holder.tvDepartureTimeValue.setError("");
            }else{
                holder.tvDepartureTimeValue.setError(null);
            }
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
                holder.tvArrivalLocationValue.setError(null);
            } else {
                holder.tvArrivalLocationValue.setText(StringUtilities.EMPTY_STRING);
                if (isSaveTriggered == true ) {
                    holder.tvArrivalLocationValue.setError("");
                }else {
                    holder.tvArrivalLocationValue.setError(null);
                }
            }
        }
        if (holder.tvArrivalDateLabel != null) {
            holder.tvArrivalDateLabel.setText("@Arrival Date@");
        }
        if (holder.tvArrivalTimeLabel != null) {
            holder.tvArrivalTimeLabel.setText(R.string.general_time);
        }
        if (segment.getArrivalDateTime() != null) {
            String dateStr;
            if (holder.tvArrivalDateValue != null) {
                dateStr = DateUtils.formatDateTime(context, segment.getArrivalDateTime().getTime(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR
                                | DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH);
                holder.tvArrivalDateValue.setText(dateStr);
                if (dateStr != null){
                    holder.tvArrivalDateValue.setError(null);
                }else {
                    if (isSaveTriggered == true ) {
                        holder.tvArrivalDateValue.setError((""));
                    }else {
                        holder.tvArrivalDateValue.setError(null);
                    }
                }
            }
            if (holder.tvArrivalTimeValue != null) {
                dateStr = DateUtils.formatDateTime(context, segment.getArrivalDateTime().getTime(),
                        DateUtils.FORMAT_SHOW_TIME);
                holder.tvArrivalTimeValue.setText(dateStr);
                holder.tvArrivalTimeValue.setError(null);
                if (dateStr == null && isSaveTriggered == true){
                        holder.tvArrivalTimeValue.setError((""));
                }
            }
        } else {
            holder.tvArrivalTimeValue.setText(StringUtilities.EMPTY_STRING);
            if (isSaveTriggered == true ) {
                holder.tvArrivalTimeValue.setError("");
            }else {
                holder.tvArrivalTimeValue.setError(null);
            }
        }
    }
}
