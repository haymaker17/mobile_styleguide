package com.concur.mobile.core.expense.travelallowance.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.expense.travelallowance.util.DefaultDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.IDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.FormatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by D049515 on 15.06.2015.
 */
public class FixedTravelAllowanceListAdapter extends RecyclerViewAdapter<FixedTravelAllowanceListAdapter.ViewHolder> {

    /**
     * Holds all UI controls needed for rendering
     */
    public final class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
//        private View vListFieldContainer;
        private View vDividerTop;
        private TextView tvTitle;
        private TextView tvValue;
        private TextView tvSubtitle1;
        private TextView tvSubtitle2;
        private View vDividerBottom;
        private ViewGroup vgSubtitleEllipsized;
        private TextView tvSubtitleEllipsized;
        private TextView tvSubtitleMore;
        private CheckBox checkBox;


        public ViewHolder(View view, final OnClickListener listener) {
            super(view);
            this.view = view;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onClick(v, getAdapterPosition());
                    }
                }
            });
//            vListFieldContainer = view.findViewById(R.id.list_field_container);
            vDividerTop = view.findViewById(R.id.v_divider_top);
            vDividerBottom = view.findViewById(R.id.v_divider_bottom);
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvValue = (TextView) view.findViewById(R.id.tv_value);
            tvSubtitle1 = (TextView) view.findViewById(R.id.tv_subtitle_1);
            tvSubtitle2 = (TextView) view.findViewById(R.id.tv_subtitle_2);
            vgSubtitleEllipsized = (ViewGroup) view.findViewById(R.id.vg_subtitle_ellipsized);
            tvSubtitleEllipsized = (TextView) view.findViewById(R.id.tv_subtitle_ellipsized);
            tvSubtitleMore = (TextView) view.findViewById(R.id.tv_subtitle_more);
            checkBox = (CheckBox) view.findViewById(R.id.checkbox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (listener != null) {
                        listener.onClick(buttonView, getAdapterPosition());
                    }
                }
            });
        }


    }

    private FixedTravelAllowance currentAllowance;

    private static final String CLASS_TAG = FixedTravelAllowanceListAdapter.class.getSimpleName();

    private static final int LAYOUT_ID = R.layout.ta_generic_table_row_layout;

    private FixedTravelAllowanceController allowanceController;
    private IDateFormat dateFormatter;

    private ViewHolder holder;
    private View.OnLayoutChangeListener layoutChangeListener;

    public static final int HEADER_ROW = 0;
    public static final int ENTRY_ROW = 1;

    private Context context;

    private List<Object> items;

    private OnClickListener listener;

    private boolean inSelectionMode;

    /**
     * Creates an instance of this list adapter.
     *
     * @param context
     */
    public FixedTravelAllowanceListAdapter(final Context context, OnClickListener listener, boolean inSelectionMode) {
        this.context = context;
        this.inSelectionMode = inSelectionMode;
        this.listener = listener;

        ConcurCore app = (ConcurCore) context.getApplicationContext();
        this.allowanceController = app.getTaController().getFixedTravelAllowanceController();

        this.dateFormatter = new DefaultDateFormat(context);

        this.items = new ArrayList<Object>();
        if (inSelectionMode) {
            this.items.addAll(allowanceController.getFixedTravelAllowances());
        } else {
            this.items.addAll(allowanceController.getLocationsAndAllowances());
        }

        layoutChangeListener = new View.OnLayoutChangeListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft,
                                       int oldTop, int oldRight, int oldBottom) {

                if (currentAllowance == null || holder == null) {
                    return;
                }

                String provisionText = holder.tvSubtitleEllipsized.getText().toString();
                TextPaint paint = holder.tvSubtitleEllipsized.getPaint();
                float textWidth = paint.measureText(provisionText);
                int textViewSize = holder.tvSubtitleEllipsized.getRight() - holder.tvSubtitleEllipsized.getLeft()
                        - holder.tvSubtitleEllipsized.getPaddingRight() - holder.tvSubtitleEllipsized.getPaddingLeft();

                holder.tvSubtitleMore.setVisibility(View.INVISIBLE);

                if (textViewSize - textWidth < 0) {
                    holder.tvSubtitleMore.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    private Context getContext() {
        return this.context;
    }

    public Object getItem(int position) {
        if (position > items.size() - 1) {
            return null;
        } else {
            return items.get(position);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.
                from(parent.getContext()).
                inflate(LAYOUT_ID, parent, false);
        return new ViewHolder(v, listener);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position > getItemCount() - 1) {
            Log.e(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "getView",
                    "Index is out of bounds. Index: " + position));
        }

        if (getItemViewType(position) == ENTRY_ROW) {
            this.holder = holder;
            holder.view.addOnLayoutChangeListener(this.layoutChangeListener);

            currentAllowance = (FixedTravelAllowance) getItem(position);
            boolean withBottomDivider = false;
            if (position + 1 < getItemCount() && getItemViewType(position + 1) == HEADER_ROW) {
                withBottomDivider = true;
            }
            boolean withTopDivider = false;
            if (position != 0) {
                withTopDivider = true;
            }

            renderEntryRow(currentAllowance, withTopDivider, withBottomDivider);
        }



        if (getItemViewType(position) == HEADER_ROW) {
            this.holder = holder;
            currentAllowance = null;
            String locationName = (String) getItem(position);
            renderHeaderRow(locationName);
        }


        if (holder.vDividerBottom != null) {
            if (position + 1 >= getItemCount()) {//Last Divider
                holder.vDividerBottom.setVisibility(View.VISIBLE);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemViewType(int i) {
        if (getItem(i) instanceof String) {
            return HEADER_ROW;
        }

        if (getItem(i) instanceof FixedTravelAllowance) {
            return ENTRY_ROW;
        }

        Log.e(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "getItemViewType",
                "Cannot identify view type for index: " + i));

        return -1;
    }


    @Override
    public int getItemCount() {
        return items.size();
    }


    /**
     * Renders the header row containing the textual representation of a location
     *
     * @param location the location to be rendered
     */
    private void renderHeaderRow(String location) {

        if (holder.vDividerTop != null) {
            holder.vDividerTop.setVisibility(View.GONE);
        }

        if (holder.vDividerBottom != null) {
            holder.vDividerBottom.setVisibility(View.GONE);
        }

        if (holder.tvTitle != null) {
            holder.tvTitle.setText(location);
            holder.tvTitle.setTextAppearance(getContext(), R.style.TASoloTitle);
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

    }

    /**
     * Renders the fixed travel allowance in an entry row
     *
     * @param allowance the allowance to be rendered
     */
    private void renderEntryRow(FixedTravelAllowance allowance, boolean withTopDivider, boolean withBottomDivider) {

        if (allowance == null) {
            return;
        }

//        if (holder.vListFieldContainer != null) {
//            holder.vListFieldContainer.setBackgroundResource(R.drawable.ta_list_selector);
//        }

        if (holder.vDividerTop != null) {
            if (withTopDivider) {
                holder.vDividerTop.setVisibility(View.VISIBLE);
            } else {
                holder.vDividerTop.setVisibility(View.GONE);
            }
        }

        if (holder.vDividerBottom != null) {
            if (withBottomDivider) {
                holder.vDividerBottom.setVisibility(View.VISIBLE);
            } else {
                holder.vDividerBottom.setVisibility(View.GONE);
            }
        }

        if (holder.tvTitle != null) {
            holder.tvTitle.setTextAppearance(getContext(), R.style.TATitle);
            holder.tvTitle.setText(dateFormatter.format(allowance.getDate(), false, true, false));
        }

        if (holder.tvSubtitle1 != null) {
            holder.tvSubtitle1.setVisibility(View.GONE);
        }

        if (holder.tvSubtitle2 != null) {
            holder.tvSubtitle2.setVisibility(View.GONE);
        }

        if (holder.tvValue != null) {
            holder.tvValue.setVisibility(View.GONE);
        }

        if (holder.vgSubtitleEllipsized != null) {
            holder.vgSubtitleEllipsized.setVisibility(View.GONE);
        }

        if (allowance.getExcludedIndicator()) {
            if (holder.tvValue != null) {
                holder.tvValue.setVisibility(View.VISIBLE);
                holder.tvValue.setText(getContext().getString(R.string.ta_excluded));
            }
        } else {
            renderSubtitleEllipsized(allowance);
        }
        renderAmount(holder.tvValue, allowance.getAmount(), allowance.getCurrencyCode());

        if (inSelectionMode && holder.checkBox != null) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(allowance.isSelected());
        }
    }

    /**
     * Renders the overnight indication
     *
     * @param allowance The fixed travel allowance holding the information
     */
    private void renderOvernight(FixedTravelAllowance allowance) {
        if (allowance == null || holder.tvSubtitle1 == null) {
            return;
        }
        if (allowance.getOvernightIndicator()) {
            holder.tvSubtitle1.setVisibility(View.VISIBLE);
            holder.tvSubtitle1.setText(R.string.ta_overnight);
        } else {
            holder.tvSubtitle1.setVisibility(View.GONE);
        }
    }

    /**
     * Renders the subtitle of the given fixed travel allowance. Note: The visibility of the
     * more view is handled by the layout listener. This is necessary as we first need to
     * render the layout in order to retrieve the measure information.
     *
     * @param allowance The travel allowance to derive the subtitle information from
     */
    private void renderSubtitleEllipsized(final FixedTravelAllowance allowance) {

        if (allowance == null) {
            return;
        }

        if (holder.vgSubtitleEllipsized == null || holder.tvSubtitleEllipsized == null
                || holder.tvSubtitleMore == null) {
            return;
        }
        String provisionText = allowanceController.mealsProvisionToText(allowance, 3);
        if (StringUtilities.isNullOrEmpty(provisionText)) {
            if (allowance.getOvernightIndicator()) {
                provisionText = getContext().getString(R.string.ta_overnight);
            }
        } else {
            if (allowance.getOvernightIndicator()) {
                provisionText = provisionText + "; " + getContext().getString(R.string.ta_overnight);
            }
        }
        holder.tvSubtitleEllipsized.setText(provisionText);
        holder.vgSubtitleEllipsized.setVisibility(View.VISIBLE);
        holder.tvSubtitleMore.setVisibility(View.INVISIBLE);
    }

    /**
     * Renders the given amount currency pair into the given text view
     *
     * @param tvAmount The text view
     * @param amount   The amount to be rendered
     * @param crnCode  the currency code to be rendered
     */
    private void renderAmount(TextView tvAmount, Double amount, String crnCode) {

        if (tvAmount == null) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "renderAmount", "TextView null reference!"));
            return;
        }
        tvAmount.setVisibility(View.VISIBLE);
        if (amount != null) {
            Locale locale = getContext().getResources().getConfiguration().locale;
            tvAmount.setText(FormatUtil.formatAmount(amount, locale, crnCode, true, true));
        } else {
            tvAmount.setText(StringUtilities.EMPTY_STRING);
        }
    }

}
