package com.concur.mobile.core.expense.travelallowance.fragment;

import android.content.Context;
import android.text.Layout;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.util.DefaultDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.IDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;

import java.util.List;
import java.util.Locale;

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

    private FixedTravelAllowance currentAllowance;

    private static final String CLS_TAG = FixedTravelAllowanceListAdapter.class.getSimpleName();

    private static final int LAYOUT_ID = R.layout.generic_table_row_layout;

    private Context context;
    private FixedTravelAllowanceController allowanceController;
    private IDateFormat dateFormatter;

    private ViewHolder holder;
    private View.OnLayoutChangeListener layoutChangeListener;

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
        this.allowanceController = new FixedTravelAllowanceController(context);
        addAll(allowanceController.getLocationsAndAllowances());

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
                } else {
                    if (allowanceController.mealsProvisionToText(currentAllowance, 3).length() >
                            provisionText.length()) {
                        holder.tvSubtitleMore.setVisibility(View.VISIBLE);
                    }
                }

            }
        };
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

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            resultView = inflater.inflate(LAYOUT_ID, viewGroup, false);
            createViewHolder(resultView);
            resultView.setTag(holder);
            if (getItemViewType(i) == ENTRY_ROW ) {
                resultView.addOnLayoutChangeListener(this.layoutChangeListener);
            }
        } else {
            resultView = convertView;
            holder = (ViewHolder) resultView.getTag();
        }

        if (getItemViewType(i) == HEADER_ROW ) {
            currentAllowance = null;
            String locationName = (String) getItem(i);
            renderHeaderRow(locationName);
        }

        if (getItemViewType(i) == ENTRY_ROW ) {
            currentAllowance = (FixedTravelAllowance) getItem(i);
            boolean withBottomDivider = false;
            if (i + 1 < getCount() && getItemViewType(i + 1) == HEADER_ROW) {
                withBottomDivider = true;
            }
            boolean withTopDivider = false;
            if (i != 0) {
                withTopDivider = true;
            }

            renderEntryRow(currentAllowance, withTopDivider, withBottomDivider);
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
     * Creates the member view holder
     * @param view The inflated view to grab the IDs from
     */
    private void createViewHolder(final View view) {
        holder = new ViewHolder();
        holder.vDividerTop = view.findViewById(R.id.v_divider_top);
        holder.vDividerBottom = view.findViewById(R.id.v_divider_bottom);
        holder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
        holder.tvValue = (TextView) view.findViewById(R.id.tv_value);
        holder.tvSubtitle1 = (TextView) view.findViewById(R.id.tv_subtitle_1);
        holder.tvSubtitle2 = (TextView) view.findViewById(R.id.tv_subtitle_2);
        holder.vgSubtitleEllipsized = (ViewGroup) view.findViewById(R.id.vg_subtitle_ellipsized);
        holder.tvSubtitleEllipsized = (TextView) view.findViewById(R.id.tv_subtitle_ellipsized);
        holder.tvSubtitleMore = (TextView) view.findViewById(R.id.tv_subtitle_more);
    }

    /**
     * Renders the header row containing the textual representation of a location
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

    }

    /**
     * Renders the fixed travel allowance in an entry row
     * @param allowance the allowance to be rendered
     */
    private void renderEntryRow(FixedTravelAllowance allowance, boolean withTopDivider, boolean withBottomDivider) {

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
            renderSubtitle(allowance);
            renderAmount(holder.tvValue, allowance.getAmount(), allowance.getCurrencyCode());
        }
    }

    /**
     * Renders the subtitle of the given fixed travel allowance. Note: The visibility of the
     * more view is handled by the layout listener. This is necessary as we first need to
     * render the layout in order to retrieve the measure information.
     * @param allowance The travel allowance to derive the subtitle information from
     */
    private void renderSubtitle(final FixedTravelAllowance allowance) {
        if (holder.vgSubtitleEllipsized == null || holder.tvSubtitleEllipsized == null
                || holder.tvSubtitleMore == null) {
            return;
        }
        String provisionText = allowanceController.mealsProvisionToText(allowance, 1);
        holder.tvSubtitleEllipsized.setText(provisionText);
        holder.vgSubtitleEllipsized.setVisibility(View.VISIBLE);
        holder.tvSubtitleMore.setVisibility(View.INVISIBLE);
    }

    /**
     * Renders the given amount currency pair into the given text view
     * @param tvAmount The text view
     * @param amount The amount to be rendered
     * @param crnCode the currency code to be rendered
     */

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
