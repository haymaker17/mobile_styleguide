package com.concur.mobile.core.expense.travelallowance.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;

import java.util.List;

/**
 * Created by D049515 on 27.07.2015.
 */
public class SimpleItineraryListAdapter extends RecyclerView.Adapter<SimpleItineraryListAdapter.ViewHolder> {

    public static class ViewHolder  extends RecyclerView.ViewHolder {

        View vDividerTop;
        TextView tvTitle;
        TextView tvValue;
        TextView tvSubtitle1;
        TextView tvSubtitle2;
        View vDividerBottom;
        ImageView icon;

        public ViewHolder(View v) {
            super(v);
            tvTitle = (TextView) v.findViewById(R.id.tv_title);
            tvValue = (TextView) v.findViewById(R.id.tv_value);
            tvSubtitle1 = (TextView) v.findViewById(R.id.tv_subtitle_1);
            tvSubtitle2 = (TextView) v.findViewById(R.id.tv_subtitle_2);
            vDividerTop = v.findViewById(R.id.v_divider_top);
            vDividerBottom = v.findViewById(R.id.v_divider_bottom);
            icon = (ImageView) v.findViewById(R.id.iv_icon);
        }

    }

    private List<Itinerary> itinList;

    private View.OnClickListener onClickListener;


    public SimpleItineraryListAdapter(List<Itinerary> itineraryList) {
        this.itinList = itineraryList;
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
                inflate(R.layout.generic_table_row_layout, viewGroup, false);

        if (onClickListener != null) {
            v.setOnClickListener(onClickListener);
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        Itinerary itinerary = itinList.get(i);

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
}
