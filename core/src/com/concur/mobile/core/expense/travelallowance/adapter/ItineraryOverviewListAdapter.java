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
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;

/**
 * Created by Michael Becherer on 16-Jul-15.
 */
public class ItineraryOverviewListAdapter  extends ArrayAdapter<Object> {

    /**
     * The name of this {@code Class} for logging purpose.
     */
    private static final String CLASS_TAG = ItineraryOverviewListAdapter.class
            .getSimpleName();

    /**
     * The layout this adapter is dealing with
     */
    private static final int LAYOUT_ID = R.layout.generic_table_row_layout;

    /**
     * The application context
     */
    private Context context;

    /**
     * The controller holding the list of itineraries
     */
    private TravelAllowanceItineraryController itineraryController;

    /**
     * View holder to improve performance
     */
    private ViewHolder holder;

    /**
     * Holds all UI controls needed for rendering
     */
    private final class ViewHolder {
        private View vDividerTop;
        private TextView tvTitle;
        private TextView tvValue;
        private TextView tvSubtitle1;
        private TextView tvSubtitle2;
        private View vDividerBottom;
        private ImageView icon;
    }

    public ItineraryOverviewListAdapter(Context context) {
        super(context, LAYOUT_ID);
        this.context = context;

        ConcurCore app = (ConcurCore) context.getApplicationContext();
        this.itineraryController = app.getTaItineraryController();
        addAll(itineraryController.getItineraryList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        View resultView = null;
        Itinerary itinerary = (Itinerary) getItem(i);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            resultView = inflater.inflate(LAYOUT_ID, viewGroup, false);
            createViewHolder(resultView);
            resultView.setTag(holder);
        } else {
            resultView = convertView;
            holder = (ViewHolder) resultView.getTag();
        }

        renderViews(itinerary);
        return resultView;
    }

    /**
     * Creates the member view holder
     *
     * @param view The inflated view to grab the IDs from
     */
    private void createViewHolder(final View view) {
        holder = new ViewHolder();
        holder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
        holder.tvValue = (TextView) view.findViewById(R.id.tv_value);
        holder.tvSubtitle1 = (TextView) view.findViewById(R.id.tv_subtitle_1);
        holder.tvSubtitle2 = (TextView) view.findViewById(R.id.tv_subtitle_2);
        holder.vDividerTop = view.findViewById(R.id.v_divider_top);
        holder.vDividerBottom = view.findViewById(R.id.v_divider_bottom);
        holder.icon = (ImageView) view.findViewById(R.id.iv_icon);
    }

    /**
     * Populates the view content and controls the visibility of the views
     */
    private void renderViews(Itinerary itinerary) {
        if (itinerary == null) {
            return;
        }
        if (holder.tvTitle != null) {
            holder.tvTitle.setVisibility(View.VISIBLE);
            holder.tvTitle.setText(itinerary.getName());
            if (itinerary.getMessage() != null) {
                holder.tvTitle.setError(itinerary.getMessage().getMessageText());
            } else {
                holder.tvTitle.setError(null);
            }

            if (itinerary.isLocked()) {
                holder.icon.setVisibility(View.VISIBLE);
                holder.icon.setImageResource(R.drawable.profile_icon_bank);
            } else {
                holder.icon.setVisibility(View.GONE);
            }
        }
        if (holder.tvValue != null) {
            holder.tvValue.setVisibility(View.GONE);
        }
        if (holder.tvSubtitle1 != null) {
            holder.tvSubtitle1.setVisibility(View.GONE);
        }
        if (holder.tvSubtitle2 != null) {
            holder.tvSubtitle2.setVisibility(View.GONE);
        }
        if (holder.vDividerTop != null) {
            holder.vDividerTop.setVisibility(View.GONE);
        }
        if (holder.vDividerBottom != null) {
            holder.vDividerBottom.setVisibility(View.VISIBLE);
        }
    }
}
