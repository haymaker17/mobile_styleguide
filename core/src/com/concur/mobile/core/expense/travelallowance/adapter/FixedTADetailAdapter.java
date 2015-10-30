package com.concur.mobile.core.expense.travelallowance.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.datamodel.ICode;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by d049515 on 17.10.2015.
 */
public class FixedTADetailAdapter extends RecyclerViewAdapter<FixedTADetailAdapter.ViewHolder> {

    public enum RowType {
        SPINNER,
        SWITCH;
    }

    public static class ValueHolder {
        public String tag;
        public RowType rowType;
        public boolean isReadOnly;
        public String label;
        public String readOnlyValue;
        public boolean isChecked;
        public List<ICode> spinnerValues;
        public int selectedSpinnerPosition;
        public boolean multiValuesSelected;
    }

    public final class ViewHolder extends RecyclerView.ViewHolder {

        public TextView label;
        public TextView readOnlyValue;
        public Switch switchView;
        public Spinner spinner;
        public ImageView icon;

        public ViewHolder(View itemView, AdapterView.OnItemSelectedListener spinnerListener,
                CompoundButton.OnCheckedChangeListener switchListener) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.tv_label);
            readOnlyValue = (TextView) itemView.findViewById(R.id.tv_read_only_value);
            switchView = (Switch) itemView.findViewById(R.id.sv_switch);
            spinner = (Spinner) itemView.findViewById(R.id.sp_spinner);
            spinner.setOnItemSelectedListener(spinnerListener);
            icon = (ImageView) itemView.findViewById(R.id.iv_multi_value_icon);
        }

        public void setTag(String tag) {
            switchView.setTag(tag);
            spinner.setTag(tag);
        }
    }


    private static final int LAYOUT_ID = R.layout.ta_fixed_ta_detail_row;

    private List<ValueHolder> values;

    private AdapterView.OnItemSelectedListener spinnerListener;

    private CompoundButton.OnCheckedChangeListener switchListener;
    
    private Context ctx;

    
    public FixedTADetailAdapter(Context ctx, List<ValueHolder> values) {
        this.ctx = ctx;

        if (values == null) {
            this.values = new ArrayList<ValueHolder>();
        } else {
            this.values = values;
        }

    }

    public void setSpinnerListener(AdapterView.OnItemSelectedListener listener) {
        spinnerListener = listener;
    }

    public void setSwitchListener(CompoundButton.OnCheckedChangeListener listener) {
        switchListener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.
                from(parent.getContext()).
                inflate(LAYOUT_ID, parent, false);
        return new ViewHolder(v, spinnerListener, switchListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ValueHolder value = getItem(position);
        if (value == null) {
            return;
        }

        holder.setTag(value.tag);

        holder.readOnlyValue.setVisibility(View.GONE);
        holder.icon.setVisibility(View.GONE);

        if (value.rowType == RowType.SWITCH) {
            holder.spinner.setVisibility(View.GONE);
            holder.label.setVisibility(View.GONE);
            holder.switchView.setVisibility(View.VISIBLE);
            holder.switchView.setOnCheckedChangeListener(null);
            holder.switchView.setChecked(value.isChecked);
            holder.switchView.setOnCheckedChangeListener(switchListener);
            holder.switchView.setTextOn(ctx.getResources().getString(R.string.general_yes));
            holder.switchView.setTextOff(ctx.getResources().getString(R.string.general_no));
            holder.switchView.setText(value.label);
            holder.switchView.setEnabled(true);
            if (value.isReadOnly || value.multiValuesSelected == true) {
                holder.switchView.setEnabled(false);
                holder.switchView.setVisibility(View.GONE);
                holder.readOnlyValue.setVisibility(View.VISIBLE);
                holder.label.setVisibility(View.VISIBLE);
                holder.label.setText(value.label);
                if (value.isChecked) {
                    holder.readOnlyValue.setText(R.string.general_yes);
                } else {
                    holder.readOnlyValue.setText(R.string.general_no);
                }
                if (value.multiValuesSelected == true){
                    holder.icon.setVisibility(View.VISIBLE);
                    holder.readOnlyValue.setText(R.string.ta_multiple_values);
                }
            }
        }

        if (value.rowType == RowType.SPINNER) {
            holder.switchView.setVisibility(View.GONE);
            holder.label.setVisibility(View.VISIBLE);
            holder.label.setText(value.label);
            holder.spinner.setVisibility(View.VISIBLE);
            ArrayAdapter<ICode> adapter = createSpinnerAdapter(value.spinnerValues, value.multiValuesSelected);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spinner.setAdapter(adapter);
            if (value.multiValuesSelected) {
                holder.spinner.setSelection(value.spinnerValues.size() - 1);
            } else {
                holder.spinner.setSelection(value.selectedSpinnerPosition);
            }

            if (value.isReadOnly || value.multiValuesSelected == true) {
                holder.spinner.setVisibility(View.GONE);
                holder.readOnlyValue.setVisibility(View.VISIBLE);
                holder.readOnlyValue.setText(value.spinnerValues.get(holder.spinner.getSelectedItemPosition()).toString());
                if (value.multiValuesSelected == true){
                    holder.icon.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    private ArrayAdapter<ICode> createSpinnerAdapter(List<ICode> spinnerValues, boolean multiValuesSelected) {
        if (!multiValuesSelected) {
            ArrayAdapter<ICode> adapter = new ArrayAdapter<ICode>(ctx, R.layout.ta_spinner_selected_item, spinnerValues) ;
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            return adapter;
        } else {
            spinnerValues.add(new MealProvision("dummy", ctx.getString(R.string.ta_multiple_values)));
            ArrayAdapter<ICode> adapter = new ArrayAdapter<ICode>(ctx, android.R.layout.simple_spinner_item,
                    spinnerValues) {

                @Override
                public int getCount() {
                    return super.getCount() - 1;
                }
            };
            return adapter;
        }
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public ValueHolder getItem(int position) {
        if (position > -1 && position < values.size()) {
            return values.get(position);
        } else {
            return null;
        }
    }

}