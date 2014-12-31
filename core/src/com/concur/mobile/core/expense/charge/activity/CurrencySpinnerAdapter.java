/**
 * 
 */
package com.concur.mobile.core.expense.charge.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.charge.data.CategoryListItem;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.widget.ListSpinner.ListSpinnerAdapter;

/**
 * An extension of <code>BaseAdapter</code> for making a currency selection.
 * 
 * @author AndrewK
 */
public class CurrencySpinnerAdapter extends BaseAdapter implements ListSpinnerAdapter, Filterable {

    private final String CLS_TAG = CurrencySpinnerAdapter.class.getSimpleName();

    /**
     * Contains the list of available currencies.
     */
    private ArrayList<ListItem> currencies = new ArrayList<ListItem>();

    /**
     * Contains the list of computed filtering results.
     */
    private ArrayList<ListItem> filterResults;

    /**
     * Contains the last constraint (text string) used to search.
     */
    private String filterStr;

    /**
     * Contains a reference to an adapter filter.
     */
    private CurrencyFilter filter;

    /**
     * Contains the activity associated with this adapter.
     */
    private Activity activity;

    private List<ListItem> mruList = null;
    private CategoryListItem mruCategory = null;

    /**
     * Constructs an instance of <code>CurrencySpinnerAdapter</code> to populate a list of currencies.
     */
    public CurrencySpinnerAdapter(Activity activity) {
        this.activity = activity;

        // Set the list of currency types.
        ConcurCore ConcurCore = (ConcurCore) this.activity.getApplication();
        IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
        ArrayList<ListItem> curTypes = expEntCache.getCurrencyTypes();
        if (curTypes != null && curTypes.size() > 0) {
            currencies = filterPerCommonCurrencies(curTypes);
        }
    }

    /**
     * Will re-order currencies within the list based on the order defined in <code>commonCurrencyTypes</code>.
     * 
     * @param currencies
     *            the list of currencies to reorder.
     * @return a cloned and re-ordered list of <code>currencies</code>
     */
    private ArrayList<ListItem> filterPerCommonCurrencies(ArrayList<ListItem> currencies) {
        // ArrayList<CurrencyType> filterList = new ArrayList<CurrencyType>();
        @SuppressWarnings("unchecked")
        ArrayList<ListItem> sourceList = (ArrayList<ListItem>) currencies.clone();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        ArrayList<ListItem> dbList = null;
        if (userId != null) {
            dbList = (ArrayList<ListItem>) getDBListForCurType(userId, ListItem.DEFAULT_KEY_CURRENCY);
        } else {
            dbList = null;
            Log.e(Const.LOG_TAG, CLS_TAG + ".filterPerCommonCurrencies: user id is null; can not perform mru");
        }

        if (dbList != null) {
            mruCategory = new CategoryListItem(activity.getText(R.string.expense_type_category_mru).toString());
            mruList = dbList;
            mruList.add(0, mruCategory);
        }
        // Sanity check that there was an overlap in "commonly used" currencies and those returned from the server.
        if (mruList != null && mruList.size() > 0) {
            // Add the rest.
            if (sourceList.size() > 0) {
                // Add the 'Other' category.
                CategoryListItem otherCat = new CategoryListItem(activity.getText(R.string.other).toString());
                mruList.add(otherCat);
                Iterator<ListItem> curTypeIter = sourceList.iterator();
                while (curTypeIter.hasNext()) {
                    ListItem curType = curTypeIter.next();
                    // curType.setCategory(otherCat);
                    mruList.add(curType);
                }
            }
        } else {
            // Nothing got moved! Indicates none of the returned currencies from the server are in the "commonly used"
            // list!. In this case, we'll use 'sourceList'.
            mruList = sourceList;
        }
        return (ArrayList<ListItem>) mruList;
    }

    private List<ListItem> getDBListForCurType(String key, String fieldID) {

        ConcurCore ConcurCore = (ConcurCore) activity.getApplication();
        IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
        List<ListItem> dblist = expEntCache.getListItemFromDB(key, fieldID);
        return dblist;
    }

    /**
     * Will locate the position within this adapter for the currency whose code is <code>currencyCode</code>.
     * 
     * @param currencyCode
     *            currency code.
     * 
     * @return the position within this adapter for the currency whose code is <code>currencyCode</code>; otherwise
     *         <code>-1</code> is returned.
     */
    public int getPositionForCurrency(String currencyCode) {
        int position = -1;
        if (currencies != null) {
            for (int curInd = 0; curInd < currencies.size(); ++curInd) {
                ListItem curType = currencies.get(curInd);
                if (curType != null && !(curType instanceof CategoryListItem)
                        && currencies.get(curInd).code.equalsIgnoreCase(currencyCode)) {
                    position = curInd;
                    break;
                }
            }
        }
        return position;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getCount()
     */
    public int getCount() {
        int count = 0;
        ArrayList<ListItem> curList = getCurrencyList();
        if (curList != null) {
            count = curList.size();
        }
        return count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.widget.ListSpinner.ListSpinnerAdapter#getFilteredSelectionInList(int)
     */
    public int getFilteredSelectionInList(int filteredPosition) {
        int position = filteredPosition;
        if (filterResults != null) {
            position = currencies.indexOf(filterResults.get(filteredPosition));
        }
        return position;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        View curTypeView = null;

        ListItem curType = currencies.get(position);
        if (filterResults != null) {
            curType = filterResults.get(position);
        }
        int layoutId = R.layout.expense_currency;
        if (curType instanceof CategoryListItem) {
            layoutId = R.layout.expense_currency_category;
        }
        curTypeView = getCurrencyView(position, layoutId);
        return curTypeView;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.BaseAdapter#getDropDownView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View curTypeView = null;
        int curDropDownLayResId = R.layout.expense_currency_dropdown;
        ListItem curType = getCurrencyList().get(position);
        if (curType instanceof CategoryListItem) {
            curDropDownLayResId = R.layout.expense_currency_category_dropdown;
        }
        curTypeView = getCurrencyView(position, curDropDownLayResId);
        return curTypeView;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItem(int)
     */
    public Object getItem(int position) {
        Object retVal = null;
        retVal = getCurrencyList().get(position);
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.BaseAdapter#isEnabled(int)
     */
    @Override
    public boolean isEnabled(int position) {
        boolean enabled = false;
        ListItem curType = getCurrencyList().get(position);
        enabled = !(curType instanceof CategoryListItem);
        return enabled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.BaseAdapter#getItemViewType(int)
     */
    @Override
    public int getItemViewType(int position) {
        if (!isEnabled(position)) {
            return Adapter.NO_SELECTION;
        } else {
            return super.getItemViewType(position);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.BaseAdapter#areAllItemsEnabled()
     */
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    /**
     * Will construct a view to display a currency utilizing a specific layout.
     * 
     * @param position
     *            the position of the currency.
     * @param layoutResId
     *            the resource id of the layout.
     * 
     * @return the builte view.
     */
    private View getCurrencyView(int position, int layoutResId) {
        View view = null;

        LayoutInflater inflater = LayoutInflater.from(activity);
        view = inflater.inflate(layoutResId, null);
        if (view != null) {
            TextView textView = (TextView) view.findViewById(R.id.currency_name);
            if (textView != null) {
                ListItem curType = getCurrencyList().get(position);
                textView.setText(curType.text);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getCurrencyView: can't find currency text view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getCurrencyView: can't inflate currency view!");
        }
        return view;
    }

    /**
     * Will get the list of currencies from which to retrieve <code>CurrencyType</code> objects.
     * 
     * @return the list of currencies from which to retrieve <code>CurrencyType</code> objects.
     */
    private ArrayList<ListItem> getCurrencyList() {
        ArrayList<ListItem> curList = currencies;
        if (filterResults != null) {
            curList = filterResults;
        }
        return curList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.widget.ListSpinner.ListSpinnerAdapter#clearSearchFilter()
     */
    public void clearSearchFilter() {
        filterResults = null;
        filterStr = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Filterable#getFilter()
     */
    public Filter getFilter() {
        if (filter == null) {
            filter = new CurrencyFilter();
        }
        return filter;
    }

    /**
     * An extension of <code>Filter</code> to handling filtering of currencies based on name.
     * 
     * @author AndrewK
     */
    class CurrencyFilter extends Filter {

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Filter#performFiltering(java.lang.CharSequence)
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults result = new Filter.FilterResults();
            if (constraint != null && constraint.toString().length() > 0) {
                // Apply the constraint.
                ArrayList<ListItem> results = new ArrayList<ListItem>();
                String searchStr = constraint.toString().toLowerCase();
                ArrayList<ListItem> searchList = currencies;
                if (filterResults != null && searchStr.startsWith(filterStr)) {
                    searchList = filterResults;
                }
                for (int expInd = 0; expInd < searchList.size(); ++expInd) {
                    ListItem curType = searchList.get(expInd);
                    if (!(curType instanceof CategoryListItem)) {
                        final String curName = curType.text.toLowerCase();
                        if (curName.startsWith(searchStr)) {/*
                                                             * CurrencyTypeCategory expTypeCat = curType.getCategory(); if
                                                             * (expTypeCat != null && !results.contains(expTypeCat)) {
                                                             * results.add(expTypeCat); } results.add(curType);
                                                             */
                        }
                    }
                }
                result.values = results;
                result.count = results.size();
                filterStr = searchStr;
            } else {
                // Reset to original values.
                result.values = currencies;
                result.count = currencies.size();
                filterStr = null;
            }
            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Filter#publishResults(java.lang.CharSequence, android.widget.Filter.FilterResults)
         */
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            @SuppressWarnings("unchecked")
            ArrayList<ListItem> fResults = (ArrayList<ListItem>) results.values;
            if (fResults.size() == currencies.size()) {
                filterResults = null;
            } else {
                filterResults = fResults;
            }
            notifyDataSetChanged();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.SectionIndexer#getPositionForSection(int)
     */
    public int getPositionForSection(int section) {
        return 0;

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.SectionIndexer#getSectionForPosition(int)
     */
    public int getSectionForPosition(int position) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.SectionIndexer#getSections()
     */
    public Object[] getSections() {
        return null;
    }
}