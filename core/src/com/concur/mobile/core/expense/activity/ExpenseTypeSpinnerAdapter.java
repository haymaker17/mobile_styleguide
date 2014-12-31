/**
 * 
 */
package com.concur.mobile.core.expense.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SectionIndexer;
import android.widget.Spinner;
import android.widget.TextView;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.charge.data.ExpenseTypeCategory;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.widget.ListSpinner.ListSpinnerAdapter;
import com.concur.core.R;

/**
 * An extension of <code>BaseAdapter</code> for making an expense type selection.
 * 
 * @author AndrewK
 */
public class ExpenseTypeSpinnerAdapter extends BaseAdapter implements ListSpinnerAdapter, Filterable, SectionIndexer {

    private final static String CLS_TAG = ExpenseTypeSpinnerAdapter.class.getSimpleName();

    private List<ExpenseType> mruList = null;
    private ExpenseTypeCategory mruCategory = null;
    /**
     * Contains the list of expense types.
     */
    private ArrayList<ExpenseType> expenseTypes = new ArrayList<ExpenseType>();

    /**
     * Contains the list of computed filtering results.
     */
    private ArrayList<ExpenseType> filterResults;

    /**
     * Contains the last constraint (text string) used to search.
     */
    private String filterStr;

    /**
     * Contains a reference to an adapter filter.
     */
    private ExpenseTypeFilter filter;

    /**
     * Contains the array of objects indicating list sections.
     */
    private ArrayList<ExpenseType> indexerSections;

    /**
     * A reference to the activity utilizing this spinner.
     */
    private Activity activity;

    /**
     * Contains a reference to the spinner this adapter is associated with.
     */
    private Spinner expSpinner = null;

    /**
     * Contains a reference to a selection that prompts the end-user.
     */
    private ExpenseType promptType;

    private String expKey = null;
    /**
     * Contains whether or not drop-down only views should be used.
     */
    private boolean useDropDownOnly;

    /**
     * Constructs an instance of <code>ExpenseTypeSpinnerAdapter</code> to populate a list of expense types.
     */
    public ExpenseTypeSpinnerAdapter(Activity activity, Spinner expSpinner) {

        this.activity = activity;
        this.expSpinner = expSpinner;
    }

    /**
     * Constructs an instance of <code>ExpenseTypeSpinnerAdapter</code> to populate a list of expense types. This constructor is
     * used when you need to send selected expense key.
     */
    public ExpenseTypeSpinnerAdapter(Activity activity, Spinner expSpinner, String expenseKey) {

        this.activity = activity;
        this.expSpinner = expSpinner;
        this.expKey = expenseKey;
    }

    /**
     * A dirt simple mode. Currently used by Gov.
     * 
     * @param expTypes
     */
    public void setExpenseTypes(ArrayList<ExpenseType> expTypes) {
        this.expenseTypes = expTypes;
    }

    public void setExpenseTypes(List<ExpenseType> expTypes, List<ExpenseType> expTypesDB) {
        setExpenseTypes(expTypes, expTypesDB, true, false);
    }

    /**
     * Sets the list of expense types.
     * 
     * @param expTypes
     *            the list of expense types.
     */
    public void setExpenseTypes(List<ExpenseType> expTypes, List<ExpenseType> expTypesDB, boolean showParentTypes,
            boolean showChildTypes) {

        setExpenseTypes(expTypes, expTypesDB, showParentTypes, showChildTypes, (String[]) null);

    }

    /**
     * 
     * @param expTypes
     * @param expTypesDB
     * @param showParentTypes
     * @param showChildTypes
     * @param filteredExpCodes
     *            an array of expense codes to filter out from the given <code>expTypes</code>.
     */
    public void setExpenseTypes(List<ExpenseType> expTypes, List<ExpenseType> expTypesDB, boolean showParentTypes,
            boolean showChildTypes, String... filteredExpCodes) {

        ConcurCore ConcurCore = (ConcurCore) activity.getApplication();
        IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
        String polKey = expTypes.get(0).getPolKey();
        if (polKey == null) {
            // expense category
            polKey = expTypes.get(1).getPolKey();
        }
        expTypesDB = getDBListForExpType(polKey);

        if (expTypesDB != null) {
            if (expKey != null) {
                // filter MRU list as this is itemized.
                ExpenseType filteredObject = expEntCache.getFilteredExpenseType(expTypesDB, expKey);
                List<ExpenseType> resultDB;
                if (filteredObject != null) {
                    resultDB = filteredObject.filterExpensetype(expTypesDB, filteredObject);
                } else {
                    resultDB = expTypesDB;
                }
                mruCategory = new ExpenseTypeCategory(activity.getText(R.string.expense_type_category_mru).toString(),
                        null, 0);
                mruList = resultDB;
            } else {
                mruCategory = new ExpenseTypeCategory(activity.getText(R.string.expense_type_category_mru).toString(),
                        null, 0);
                mruList = expTypesDB;
            }

            // MOB-11522 - Skip any expense codes specified in filteredExpCodes.
            if (mruList != null && filteredExpCodes != null) {
                mruList = ExpenseType.filterExpenseTypes(mruList, filteredExpCodes);
            }

        }

        promptType = new ExpenseType(activity.getText(R.string.make_selection).toString());
        final int size = expTypes.size();
        if (expTypes != null && size > 0) {
            expenseTypes = new ArrayList<ExpenseType>(size);
            expenseTypes.add(promptType);
            if (mruList != null) {
                int dbSize = mruList.size();
                if (dbSize > 0) {
                    // create MRU category
                    mruCategory = new ExpenseTypeCategory(activity.getText(R.string.expense_type_category_mru)
                            .toString(), null, 0);
                    expenseTypes.add(mruCategory);
                    // add MRU expenses.
                    expenseTypes.addAll(mruList);
                }
            }

            List<String> filteredList = null;
            if (filteredExpCodes != null) {
                filteredList = Arrays.asList(filteredExpCodes);
            }
            // Populate the real list with the requested types
            for (int i = 0; i < size; i++) {
                ExpenseType et = expTypes.get(i);

                if (!(et instanceof ExpenseTypeCategory)) {
                    if (!showChildTypes && et.access == ExpenseType.Access.CHILD) {
                        continue;
                    }

                    if (!showParentTypes && et.access == ExpenseType.Access.PARENT) {
                        continue;
                    }

                    // MOB-11522 - Skip any expense codes specified in filteredExpCodes.
                    if (filteredList != null && filteredList.contains(et.expCode)) {
                        continue;
                    }
                }

                expenseTypes.add(et);
            }

        }

    }

    private List<ExpenseType> getDBListForExpType(String polKey) {
        if (polKey == null) {
            return null;
        }
        ConcurCore ConcurCore = (ConcurCore) activity.getApplication();
        IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
        List<ExpenseType> expTypesDB = expEntCache.getExpenseTypeFromDB(polKey);
        return expTypesDB;
    }

    /**
     * Indicates whether drop-down only views should be used.
     * 
     * @param useDropDownOnly
     *            whether drop-down only views should be used.
     */
    public void setUseDropDownOnly(boolean useDropDownOnly) {
        this.useDropDownOnly = useDropDownOnly;
    }

    /**
     * Gets whether the end-user has made a selection.
     * 
     * @return whether the end-user has made a selection.
     */
    public boolean selectionMade() {
        boolean retVal = false;
        int selPos = expSpinner.getSelectedItemPosition();
        retVal = (selPos >= 0 && expSpinner.getSelectedItemPosition() != expenseTypes.indexOf(promptType));
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getCount()
     */
    public int getCount() {
        int count = 0;
        if (filterResults != null) {
            count = filterResults.size();
        } else if (expenseTypes != null) {
            count = expenseTypes.size();
        }
        return count;
    }

    /**
     * Will add to <code>expenseTypes</code> a "commonly used" category by looking through <code>expenseTypes</code> for specific
     * types (by key), cloning them, and adding them to the "commonly used" category. After commonly used add MRU.
     * 
     * @param expenseTypes
     *            the list of expense types.
     */
    public void addQuickExpenses(ArrayList<ExpenseType> expenseTypes) {
        List<ExpenseType> expTypesDB = getDBListForExpType("-1");
        mruList = null;
        mruCategory = null;
        if (expTypesDB != null) {
            mruList = new ArrayList<ExpenseType>(5);
            mruList = expTypesDB;
            mruCategory = new ExpenseTypeCategory(activity.getText(R.string.expense_type_category_mru).toString(),
                    null, 0);
            mruList.add(0, mruCategory);
            expenseTypes.addAll(0, mruList);
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".ExpenseTypeSpinnerAdapter.addQuickExpenses: list from database is null");
        }
        promptType = new ExpenseType(activity.getText(R.string.make_selection).toString());
        expenseTypes.add(0, promptType);
        this.expenseTypes = expenseTypes;
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
            position = expenseTypes.indexOf(filterResults.get(filteredPosition));
        }
        return position;
    }

    /**
     * Will locate the position within this adapter for the expense type matching <code>expType</code>.
     * 
     * @param expType
     *            expense type.
     * 
     * @return the position within this adapter for the expense type matching <code>expType</code>; otherwise <code>-1</code> is
     *         returned.
     */
    public int getPositionForExpenseType(String expType) {
        int position = -1;
        if (expenseTypes != null && expType != null) {
            for (int typInd = 0; typInd < expenseTypes.size(); ++typInd) {
                ExpenseType type = expenseTypes.get(typInd);
                if (!(type instanceof ExpenseTypeCategory) && type != promptType) {
                    if (type.getKey().equalsIgnoreCase(expType)) {
                        position = typInd;
                        break;
                    }
                }
            }
        }
        return position;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        if (useDropDownOnly) {
            return getDropDownView(position, convertView, parent);
        } else {
            View expTypeView = null;
            ExpenseType expType = expenseTypes.get(position);
            if (filterResults != null) {
                expType = filterResults.get(position);
            }
            int layoutId = R.layout.expense_type;
            if (expType instanceof ExpenseTypeCategory) {
                layoutId = R.layout.expense_type_category;
            }
            expTypeView = getExpenseTypeView(position, layoutId);
            return expTypeView;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.BaseAdapter#getDropDownView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View expTypeView = null;
        ExpenseType expType = expenseTypes.get(position);
        if (filterResults != null) {
            expType = filterResults.get(position);
        }
        int layoutId = R.layout.expense_type_dropdown;
        if (expType instanceof ExpenseTypeCategory) {
            layoutId = R.layout.expense_type_category_dropdown;
        }
        expTypeView = getExpenseTypeView(position, layoutId);
        return expTypeView;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItem(int)
     */
    public Object getItem(int position) {
        Object retVal = null;
        ExpenseType expenseType = expenseTypes.get(position);
        if (filterResults != null) {
            expenseType = filterResults.get(position);
        }
        retVal = expenseType;
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
        ExpenseType expenseType = expenseTypes.get(position);
        if (filterResults != null) {
            expenseType = filterResults.get(position);
        }
        enabled = !((expenseType instanceof ExpenseTypeCategory) || (position == expenseTypes.indexOf(promptType)));
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
    private View getExpenseTypeView(int position, int layoutResId) {
        View view = null;

        LayoutInflater inflater = LayoutInflater.from(activity);
        view = inflater.inflate(layoutResId, null);
        if (view != null) {
            ExpenseType expenseType = expenseTypes.get(position);
            if (filterResults != null) {
                expenseType = filterResults.get(position);
            }
            if (expenseType != null) {
                TextView textView = (TextView) view.findViewById(R.id.expense_name);
                if (textView != null) {
                    textView.setText(expenseType.getName());
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseTypeView: can't find expense type text view!");
                }
                if (!(expenseType instanceof ExpenseTypeCategory)) {
                    // TODO: make focusable?
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseTypeView: can't inflate expense type view!");
        }
        return view;
    }

    /**
     * Will add the list of <code>ExpenseType</code> objects to the rendered list.
     * 
     * @param expTypes
     *            the list of expense types.
     */
    public void appendExpenseTypes(List<ExpenseType> expTypes) {

        if (expenseTypes == null) {
            expenseTypes = new ArrayList<ExpenseType>();
            expenseTypes.add(0, promptType);
        }
        expenseTypes.addAll(expTypes);
        notifyDataSetChanged();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.SectionIndexer#getPositionForSection(int)
     */
    public int getPositionForSection(int section) {
        int position = 0;
        if (indexerSections != null && indexerSections.size() > 0) {
            ExpenseType expType = (ExpenseType) indexerSections.get(section);
            ArrayList<ExpenseType> searchList = expenseTypes;
            if (filterResults != null) {
                searchList = filterResults;
            }
            position = searchList.indexOf(expType);
            if (position == -1) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getPositionForSection: can't locate section index of '" + section
                        + "' in search list!");
                position = 0;
            }
        }
        return position;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.SectionIndexer#getSectionForPosition(int)
     */
    public int getSectionForPosition(int position) {
        int section = 0;
        ArrayList<ExpenseType> searchList = expenseTypes;
        if (filterResults != null) {
            searchList = filterResults;
        }
        // Start at 'position' and work backwards until we come to the first
        for (int posInd = position; posInd >= 0; --posInd) {
            ExpenseType expType = searchList.get(posInd);
            if (expType instanceof ExpenseTypeCategory) {
                section = indexerSections.indexOf(expType);
                if (section == -1) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getSectionForPosition: can't find section index for position '"
                            + position + "'.");
                    section = 0;
                }
                break;
            }
        }
        return section;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.SectionIndexer#getSections()
     */
    public Object[] getSections() {
        Object[] retVal = null;
        ArrayList<ExpenseType> sections = new ArrayList<ExpenseType>();
        ArrayList<ExpenseType> searchList = expenseTypes;
        if (filterResults != null) {
            searchList = filterResults;
        }
        for (int expInd = 0; expInd < searchList.size(); ++expInd) {
            ExpenseType expType = searchList.get(expInd);
            if (expType instanceof ExpenseTypeCategory) {
                sections.add(expType);
            }
        }
        indexerSections = sections;
        retVal = indexerSections.toArray();
        return retVal;
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
            filter = new ExpenseTypeFilter();
        }
        return filter;
    }

    /**
     * An extension of <code>Filter</code> to handling filtering of expense types based on name. Add MRU category as a parent.
     * 
     * @author AndrewK, SunilL
     */
    class ExpenseTypeFilter extends Filter {

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
                ArrayList<ExpenseType> results = new ArrayList<ExpenseType>();
                String searchStr = constraint.toString().toLowerCase();
                ArrayList<ExpenseType> searchList = expenseTypes;
                if (filterResults != null && searchStr.contains(filterStr)) {
                    searchList = filterResults;
                }
                for (int expInd = 0; expInd < searchList.size(); ++expInd) {
                    if (searchList.get(expInd).getName().toLowerCase().contains(searchStr)) {
                        ExpenseType expType = searchList.get(expInd);
                        // Don't add the parent type for a category!
                        if (!(expType instanceof ExpenseTypeCategory) && expType != promptType) {
                            ExpenseType parent = expType.getParentExpenseType();
                            // set MRU category as parent.
                            if (parent == null) {
                                // mruCategory=new
                                // ExpenseTypeCategory(activity.getText(R.string.expense_type_category_mru).toString(),null,0);
                                parent = mruCategory;

                                // TODO ask walt about this specification list.
                                // String expKey = expType.getKey();
                                // String expName = expType.getName();
                                // if (mruList != null && mruList.size() > 0) {
                                // for (ExpenseType expenseType : mruList) {
                                // if (expenseType.getKey().equalsIgnoreCase(expKey)
                                // && expenseType.getName().equalsIgnoreCase(expName)) {
                                // parent = mruCategory;
                                // break;
                                // }
                                // }
                                // }

                            }
                            if (!results.contains(parent)) {
                                results.add(parent);
                            }
                            results.add(expType);
                        }
                    }
                }
                result.values = results;
                result.count = results.size();
                filterStr = searchStr;
            } else {
                // Reset to original values.
                result.values = expenseTypes;
                result.count = expenseTypes.size();
                filterStr = null;
            }
            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Filter#publishResults(java.lang.CharSequence, android.widget.Filter.FilterResults)
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<ExpenseType> fResults = (ArrayList<ExpenseType>) results.values;
            filterResults = fResults;
            notifyDataSetChanged();
        }

    }
}