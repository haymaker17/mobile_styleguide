package com.concur.mobile.core.expense.travelallowance.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.expense.travelallowance.util.DefaultDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.IDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.ItineraryUtils;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.util.Date;
import java.util.List;

/**
 * Created by D049515 on 27.07.2015.
 */
public class SimpleItineraryListAdapter extends RecyclerView.Adapter<SimpleItineraryListAdapter.ViewHolder> {

    public static class ViewHolder  extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvValue;
        TextView tvSubtitle1;
        TextView tvSubtitle2;
        ImageView ivRowAction;

        public ViewHolder(View v) {
            super(v);
            View vContent = v.findViewById(R.id.v_content);
            tvTitle = (TextView) vContent.findViewById(R.id.tv_title);
            tvValue = (TextView) vContent.findViewById(R.id.tv_value);
            tvSubtitle1 = (TextView) vContent.findViewById(R.id.tv_subtitle_1);
            tvSubtitle2 = (TextView) vContent.findViewById(R.id.tv_subtitle_2);
            ivRowAction = (ImageView) v.findViewById(R.id.iv_row_action_icon);
        }
    }

    private List<Itinerary> itinList;
    private IDateFormat dateFormatter;
    private View.OnClickListener onClickListener;
    private View.OnClickListener onDeleteClickListener;
    private boolean deleteEnabled;

    public SimpleItineraryListAdapter(Context context, List<Itinerary> itineraryList) {
        this.itinList = itineraryList;
        this.dateFormatter = new DefaultDateFormat(context);
    }

    public void refreshAdapter(List<Itinerary> itineraryList) {
        if (itineraryList != null) {
            itinList = itineraryList;
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.ta_simple_list_row, viewGroup, false);

        if (onClickListener != null) {
            View vContent = v.findViewById(R.id.v_content);
            if (vContent != null) {
                vContent.setOnClickListener(onClickListener);
            }
        }

        if (onDeleteClickListener != null) {
            ImageView ivDelete = (ImageView) v.findViewById(R.id.iv_row_action_icon);
            if (ivDelete != null) {
                ivDelete.setOnClickListener(onDeleteClickListener);
            }
        }

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        Itinerary itinerary = itinList.get(i);

        if (holder.tvTitle != null) {
            holder.tvTitle.setVisibility(View.VISIBLE);
            holder.tvTitle.setText(itinerary.getName());
        }
        if (holder.ivRowAction != null) {
            holder.ivRowAction.setVisibility(View.VISIBLE);
            if (!deleteEnabled) {
                holder.ivRowAction.setVisibility(View.GONE);
            }
        }
        if (holder.tvValue != null) {
            holder.tvValue.setVisibility(View.GONE);
        }

        renderSubtitle1(holder, itinerary);

        if (holder.tvSubtitle2 != null) {
            holder.tvSubtitle2.setVisibility(View.VISIBLE);
            holder.tvSubtitle2.setText(ItineraryUtils.createLocationString(itinerary));
        }
        //renderSubtitle2(holder, itinerary);
    }

    private void renderSubtitle1(ViewHolder holder, Itinerary itinerary) {
        if (holder.tvSubtitle1 == null) {
            return;
        }
        holder.tvSubtitle1.setVisibility(View.GONE);
        if (itinerary == null) {
            return;
        }
        List<ItinerarySegment> segments = itinerary.getSegmentList();
        if (segments == null || segments.size() <= 0) {
            return;
        }
        Date departureDate = segments.get(0).getDepartureDateTime();
        Date arrivalDate = segments.get(segments.size() - 1).getArrivalDateTime();
        String dateText = DateUtils.startEndDateToString(departureDate, arrivalDate,
                dateFormatter, false, true, true);
        if (!StringUtilities.isNullOrEmpty(dateText)) {
            holder.tvSubtitle1.setVisibility(View.VISIBLE);
            holder.tvSubtitle1.setText(dateText);
        }
    }

    private void renderSubtitle2(ViewHolder holder, Itinerary itinerary) {
        if (holder.tvSubtitle2 == null) {
            return;
        }
        holder.tvSubtitle2.setVisibility(View.GONE);
        if (itinerary == null) {
            return;
        }
        List<ItinerarySegment> segments = itinerary.getSegmentList();
        if (segments == null || segments.size() <= 0) {
            return;
        }
        String locationString = ItineraryUtils.createLocationString(itinerary);

        if (!StringUtilities.isNullOrEmpty(locationString)) {
            holder.tvSubtitle2.setVisibility(View.VISIBLE);
            holder.tvSubtitle2.setText(locationString);
        }
    }

    @Override
    public int getItemCount() {
        if (itinList != null) {
            return itinList.size();
        }
        return 0;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnDeleteClickListener(View.OnClickListener onDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener;
    }

    public void setDeleteEnabled(boolean deleteEnabled) {
        this.deleteEnabled = deleteEnabled;
    }

}
