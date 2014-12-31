/**
 * 
 */
package com.concur.mobile.core.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

/**
 * An extension of <code>Spinner</code> which permits non-selectable list items.
 * 
 * @deprecated - use {@link com.concur.platform.ui.common.view.ListSpinner} instead.
 * @author AndrewK
 */
public class ListSpinner extends Spinner {

    public ListSpinner(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public ListSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public ListSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Spinner#performClick()
     */
    @Override
    public boolean performClick() {
        boolean handled = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if (getPrompt() != null) {
            builder.setTitle(getPrompt());
        }
        DropDownAdapter dropDownAdapter = new DropDownAdapter((ListSpinnerAdapter) getAdapter());
        builder.setSingleChoiceItems(dropDownAdapter, getSelectedItemPosition(), this);
        AlertDialog alertDlg = builder.create();
        ListView listView = alertDlg.getListView();
        listView.setTextFilterEnabled(true);
        // TODO: re-enable this when time permits and we can ensure it's properly being handled.
        // listView.setFastScrollEnabled(true);
        alertDlg.show();
        return handled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Spinner#onClick(android.content.DialogInterface, int)
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        which = ((ListSpinnerAdapter) getAdapter()).getFilteredSelectionInList(which);
        super.onClick(dialog, which);
        ((ListSpinnerAdapter) getAdapter()).clearSearchFilter();
    }

    /**
     * An interface that basically ensures that an adapter set on this object will support both callable interfaces. It declares
     * no new methods of itself.
     * 
     * @author AndrewK
     */
    public static interface ListSpinnerAdapter extends ListAdapter, SpinnerAdapter, Filterable, SectionIndexer {

        /**
         * Instructs the adapter to clear out any filtered search results.
         */
        public void clearSearchFilter();

        /**
         * Gets the equivalent position of the element at <code>filteredPosition</code> within the non-filtered list.
         * 
         * @param filteredPostion
         *            the selected filtered position.
         * @return the position within the non-filtered
         */
        public int getFilteredSelectionInList(int filteredPosition);

    }

    /**
     * An implementation of <code>ListSpinnerAdapter</code> used to forward calls to a delegate during drop-down viewing.
     * 
     * @author AndrewK
     */
    class DropDownAdapter implements ListSpinnerAdapter {

        private ListSpinnerAdapter delegate;

        DropDownAdapter(ListSpinnerAdapter delegate) {
            this.delegate = delegate;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.widget.ListSpinner.ListSpinnerAdapter#clearSearchFilter()
         */
        public void clearSearchFilter() {
            delegate.clearSearchFilter();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.ListAdapter#areAllItemsEnabled()
         */
        public boolean areAllItemsEnabled() {
            return delegate.areAllItemsEnabled();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.ListAdapter#isEnabled(int)
         */
        public boolean isEnabled(int position) {
            return delegate.isEnabled(position);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getCount()
         */
        public int getCount() {
            return delegate.getCount();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItem(int)
         */
        public Object getItem(int position) {
            return delegate.getItem(position);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItemId(int)
         */
        public long getItemId(int position) {
            return delegate.getItemId(position);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItemViewType(int)
         */
        public int getItemViewType(int position) {
            return delegate.getItemViewType(position);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            return delegate.getDropDownView(position, convertView, parent);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getViewTypeCount()
         */
        public int getViewTypeCount() {
            return delegate.getViewTypeCount();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#hasStableIds()
         */
        public boolean hasStableIds() {
            return delegate.hasStableIds();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#isEmpty()
         */
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#registerDataSetObserver(android.database.DataSetObserver)
         */
        public void registerDataSetObserver(DataSetObserver observer) {
            delegate.registerDataSetObserver(observer);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#unregisterDataSetObserver(android.database.DataSetObserver)
         */
        public void unregisterDataSetObserver(DataSetObserver observer) {
            delegate.unregisterDataSetObserver(observer);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.SpinnerAdapter#getDropDownView(int, android.view.View, android.view.ViewGroup)
         */
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return delegate.getDropDownView(position, convertView, parent);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Filterable#getFilter()
         */
        public Filter getFilter() {
            return delegate.getFilter();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.SectionIndexer#getPositionForSection(int)
         */
        public int getPositionForSection(int section) {
            return delegate.getPositionForSection(section);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.SectionIndexer#getSectionForPosition(int)
         */
        public int getSectionForPosition(int position) {
            return delegate.getSectionForPosition(position);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.SectionIndexer#getSections()
         */
        public Object[] getSections() {
            return delegate.getSections();
        }

        public int getFilteredSelectionInList(int filteredPosition) {
            return delegate.getFilteredSelectionInList(filteredPosition);
        }

    }

}
