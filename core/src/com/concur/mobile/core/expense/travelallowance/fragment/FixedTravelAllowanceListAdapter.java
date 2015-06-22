package com.concur.mobile.core.expense.travelallowance.fragment;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowanceTestData;

import com.concur.mobile.core.expense.travelallowance.util.DefaultDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.IDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by D049515 on 15.06.2015.
 */
public class FixedTravelAllowanceListAdapter extends ArrayAdapter<Object> {

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
        private ViewGroup vgSubtitleEllipsized;
        private TextView tvSubtitleEllipsized;
        private TextView tvSubtitleMore;
    }


    private static final String CLS_TAG = FixedTravelAllowanceListAdapter.class.getSimpleName();

    private static final int LAYOUT_ID = R.layout.generic_table_row_layout;

    private Context context;
    private boolean hasMultipleGroups;
    private IDateFormat dateFormatter;


    public static final int HEADER_ROW = 0;
    public static final int ENTRY_ROW = 1;

    /**
     * Creates an instance of this list adapter.
     * @param context
     * @param fixedTravelAllowanceList
     */
    public FixedTravelAllowanceListAdapter(final Context context, List<FixedTravelAllowance> fixedTravelAllowanceList) {
        super(context, LAYOUT_ID);
        this.context = context;
        this.dateFormatter = new DefaultDateFormat(context);
        initializeGroups(fixedTravelAllowanceList);
    }

    private void initializeGroups(List<FixedTravelAllowance> fixedTravelAllowanceList) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".initializeGroups: fixed TA list size: " + fixedTravelAllowanceList.size());
        List<String> sortedLocations = new ArrayList<String>();
        List<FixedTravelAllowance> fixedTAList = new ArrayList<FixedTravelAllowance>(fixedTravelAllowanceList);
        Collections.sort(fixedTravelAllowanceList, Collections.reverseOrder());
        Map<String, List<FixedTravelAllowance>> fixedTAGroups = new HashMap<String, List<FixedTravelAllowance>>();
        List<Object> locationAndTAList = new ArrayList<Object>();

        for (FixedTravelAllowance allowance : fixedTAList) {
            List<FixedTravelAllowance> taList;
            if (fixedTAGroups.containsKey(allowance.getLocationName())) {
                taList = fixedTAGroups.get(allowance.getLocationName());
                taList.add(allowance);
            } else {
                taList = new ArrayList<FixedTravelAllowance>();
                taList.add(allowance);
                fixedTAGroups.put(allowance.getLocationName(), taList);
                sortedLocations.add(allowance.getLocationName());
            }
        }

        if (fixedTAGroups.keySet().size() > 1) {
            this.hasMultipleGroups = true;
            for(String key: sortedLocations) {
                locationAndTAList.add(key);
                for (FixedTravelAllowance value: fixedTAGroups.get(key)) {
                    locationAndTAList.add(value);
                }
            }
        } else {
            this.hasMultipleGroups = false;
            locationAndTAList.addAll(fixedTAList);
        }

        addAll(locationAndTAList);
        Log.d(Const.LOG_TAG, CLS_TAG + ".initializeGroups: Header and TA list size " + locationAndTAList.size());
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
        return (getItemViewType(position) == ENTRY_ROW ? true : false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".getView: Render view index: " + i);
        if (i > getCount()) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: Index is out of bounds. Index: " + i);
        }

        View resultView = null;
        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            resultView = inflater.inflate(LAYOUT_ID, viewGroup, false);
            holder = createViewHolder(resultView);
            resultView.setTag(holder);
        } else {
            resultView = convertView;
            holder = (ViewHolder) resultView.getTag();
        }

        if (getItemViewType(i) == HEADER_ROW ) {
            String locationName = (String) getItem(i);
            boolean isFirstHeader = false;
            if (i == 0){
                isFirstHeader = true;
            }
            renderHeaderRow(holder, locationName, isFirstHeader);
        }

        if (getItemViewType(i) == ENTRY_ROW ) {
            FixedTravelAllowance allowance = (FixedTravelAllowance) getItem(i);
            boolean withRowDevider = false;
            if (i + 1 < getCount() && getItemViewType(i+1) == ENTRY_ROW) {
                withRowDevider = true;
            }
            renderEntryRow(holder, allowance, withRowDevider);
        }

        return resultView;
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

        Log.e(Const.LOG_TAG, CLS_TAG + ".getItemViewType: Cannot identify view type for index: " + i);
        return IGNORE_ITEM_VIEW_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getViewTypeCount() {
        return 2;
    }


    /**
     * Creates an view holder
     * @param view The inflated view to grab the IDs from
     * @return the view holder
     */
    private ViewHolder createViewHolder(final View view) {
        ViewHolder holder = new ViewHolder();
        holder.vDividerTop = view.findViewById(R.id.v_divider_top);
        holder.vDividerBottom = view.findViewById(R.id.v_divider_bottom);
        holder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
        holder.tvValue = (TextView) view.findViewById(R.id.tv_value);
        holder.tvSubtitle1 = (TextView) view.findViewById(R.id.tv_subtitle_1);
        holder.tvSubtitle2 = (TextView) view.findViewById(R.id.tv_subtitle_2);
        holder.vgSubtitleEllipsized = (ViewGroup) view.findViewById(R.id.vg_subtitle_ellipsized);
        holder.tvSubtitleEllipsized = (TextView) view.findViewById(R.id.tv_subtitle_ellipsized);
        holder.tvSubtitleMore = (TextView) view.findViewById(R.id.tv_subtitle_more);
        return holder;
    }

    /**
     * Renders the header row containing the textual representation of a location
     * @param holder the view holder
     * @param location the location to be rendered
     */
    private void renderHeaderRow(ViewHolder holder, String location, boolean isFirstHeader) {
        if (holder.vDividerTop != null) {
            if (!isFirstHeader) {
                holder.vDividerTop.setVisibility(View.VISIBLE);
            }
            else {
                holder.vDividerTop.setVisibility(View.GONE);
            }
        }
        if (holder.tvTitle != null) {
            holder.tvTitle.setText(location);
            holder.tvTitle.setTextAppearance(this.context, R.style.DefaultSoloTitle);
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
        if (holder.vDividerBottom != null) {
            holder.vDividerBottom.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Renders the fixed travel allowance in an entry row
     * @param holder the view holder
     * @param allowance the allowance to be rendered
     */
    private void renderEntryRow(ViewHolder holder, FixedTravelAllowance allowance, boolean withRowDevider) {

        if (holder.vDividerTop != null) {
            if (this.hasMultipleGroups) {
                holder.vDividerTop.setVisibility(View.GONE);
            } else {
                holder.vDividerTop.setVisibility(View.VISIBLE);
            }
        }

        if (holder.vDividerBottom != null) {
            if (withRowDevider) {
                holder.vDividerBottom.setVisibility(View.VISIBLE);
            } else {
                holder.vDividerBottom.setVisibility(View.GONE);
            }
        }

        if (holder.tvTitle != null) {
            holder.tvTitle.setTextAppearance(this.context, R.style.DefaultTitle);
            holder.tvTitle.setText(dateFormatter.format(allowance.getDate(), false, true, false));
        }

        if (holder.tvSubtitle1 != null) {
            holder.tvSubtitle1.setVisibility(View.GONE);
        }

        if (holder.tvSubtitle2 != null) {
            holder.tvSubtitle2.setVisibility(View.GONE);
        }

        if (allowance.getExcludedIndicator()){
            if (holder.vgSubtitleEllipsized != null) {
                holder.vgSubtitleEllipsized.setVisibility(View.GONE);
            }
            if (holder.tvValue != null) {
                holder.tvValue.setVisibility(View.VISIBLE);
                holder.tvValue.setText(this.context.getString(R.string.itin_excluded));
            }
        } else {
            if (holder.vgSubtitleEllipsized != null) {
                holder.vgSubtitleEllipsized.setVisibility(View.VISIBLE);
                if (holder.tvSubtitleEllipsized != null) {
                    holder.tvSubtitleEllipsized.setText(allowance.mealsProvisionToText(context, 1));
                }
            }
            renderAmount(holder.tvValue, allowance.getAmount(), allowance.getCurrencyCode());
        }
    }

    private void renderAmount(TextView tvAmount, Double amount, String crnCode) {

        if (tvAmount == null){
            Log.e(Const.LOG_TAG, CLS_TAG + ".renderAmount: TextView null reference!");
            return;
        }
        tvAmount.setVisibility(View.VISIBLE);
        if (amount != null) {
            Locale locale = context.getResources().getConfiguration().locale;
            tvAmount.setText(FormatUtil.formatAmount(amount, locale, crnCode, true, true));
        } else {
            tvAmount.setText(StringUtilities.EMPTY_STRING);
        }
    }

}
