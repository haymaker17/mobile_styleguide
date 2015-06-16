package com.concur.mobile.core.expense.travelallowance.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.Location;

/**
 * Created by D049515 on 15.06.2015.
 */
public class FixedTravelAllowanceListAdapter implements ListAdapter {

    private static final int HEADER_ROW = 1;
    private static final int ENTRY_ROW = 2;




    private List<Object> locationAndTAList;

    public FixedTravelAllowanceListAdapter(List<FixedTravelAllowance> fixedTravelAllowanceList) {
        initializeGroups(fixedTravelAllowanceList);
    }

    private void initializeGroups(List<FixedTravelAllowance> fixedTravelAllowanceList) {
        List<FixedTravelAllowance> fixedTAList = new ArrayList<FixedTravelAllowance>(fixedTravelAllowanceList);
        Collections.sort(fixedTravelAllowanceList);
        Map<Location, List<FixedTravelAllowance>> fixedTAGroups = new HashMap<Location, List<FixedTravelAllowance>>();
        locationAndTAList = new ArrayList<Object>();

       for(FixedTravelAllowance allowance: fixedTAList) {
           List<FixedTravelAllowance> taList;
            if (fixedTAGroups.containsKey(allowance.getLocation())) {
                taList = fixedTAGroups.get(allowance.getLocation());
                taList.add(allowance);
            } else {
                taList = new ArrayList<FixedTravelAllowance>();
                taList.add(allowance);
                fixedTAGroups.put(allowance.getLocation(), taList);
            }
       }

        if (fixedTAGroups.keySet().size() > 1) {
            for(Location key: fixedTAGroups.keySet()) {
                locationAndTAList.add(key);
                for(FixedTravelAllowance value: fixedTAGroups.get(key)) {
                    locationAndTAList.add(value);
                }
            }
        } else {
            locationAndTAList.addAll(fixedTAList);
        }

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount() {
        return locationAndTAList.size();
    }

    @Override
    public Object getItem(int i) {
        if (i < locationAndTAList.size()) {
            return locationAndTAList.get(i);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View convertView = view;
        if (getItemViewType(i) == HEADER_ROW ) {
            Location header = (Location)getItem(i);
            if (view == null || !view.getTag().equals(HEADER_ROW)) {
                //convertView = View.inflate()
            }
        }
        if (getItemViewType(i) == ENTRY_ROW ) {}

        return null;
    }

    @Override
    public int getItemViewType(int i) {
        if (getItem(i) instanceof Location) {
            return HEADER_ROW;
        }

        if (getItem(i) instanceof FixedTravelAllowance) {
            return ENTRY_ROW;
        }

        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        if (getCount() == 0) {
            return true;
        } else {
            return false;
        }
    }
}
