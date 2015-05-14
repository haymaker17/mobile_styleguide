package com.concur.mobile.core.expense.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.concur.core.R;
import com.concur.mobile.platform.expense.provider.Expense;

/**
 * @author Chris Diaz
 */
public class SortExpensesDialogFragment extends DialogFragment implements RadioGroup.OnCheckedChangeListener {

    public final static String CLS_TAG = SortExpensesDialogFragment.class.getSimpleName();

    /**
     * Extra data used to pass the current sort order.
     */
    public final static String EXTRA_CURRENT_SORT_ORDER = "EXTRA_CURRENT_SORT_ORDER";

    /**
     * Listener called when an action is performed in the dialog.
     */
    public interface SortExpenseDialogListener {

        public void onSortCriteriaSelected(String sortOrder);
    }


    /**
     * Convenience method for displaying the SortExpenseDialogFragment
     * with the given <code>sortBy</code> initially selected.
     *
     * @param sortOrder
     * @return
     */
    public final static SortExpensesDialogFragment newInstance(String sortOrder){

        SortExpensesDialogFragment dialog = new SortExpensesDialogFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_CURRENT_SORT_ORDER, sortOrder);
        dialog.setArguments(args);

        return dialog;
    }

    /**
     * Empty constructor required for DialogFragment.
     */
    public SortExpensesDialogFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.sort_expenses_dialog_fragment, container);
        RadioGroup group = (RadioGroup) view.findViewById(R.id.sort_expenses_radio_group);

        // Set selected button.
        String currentSortOrder = getArguments().getString(EXTRA_CURRENT_SORT_ORDER, Expense.SmartExpenseColumns.DATE_NEWEST_SORT_ORDER);
        if(Expense.SmartExpenseColumns.DATE_NEWEST_SORT_ORDER.equals(currentSortOrder)) {
            ((RadioButton) view.findViewById(R.id.radio_date_newest)).setChecked(true);
        } else if(Expense.SmartExpenseColumns.DATE_OLDEST_SORT_ORDER.equals(currentSortOrder)) {
            ((RadioButton) view.findViewById(R.id.radio_date_oldest)).setChecked(true);
        } else if(Expense.SmartExpenseColumns.AMOUNT_LOWEST_SORT_ORDER.equals(currentSortOrder)) {
            ((RadioButton) view.findViewById(R.id.radio_amount_lowest)).setChecked(true);
        } else if(Expense.SmartExpenseColumns.AMOUNT_HIGHEST_SORT_ORDER.equals(currentSortOrder)) {
            ((RadioButton) view.findViewById(R.id.radio_amount_highest)).setChecked(true);
        } else if(Expense.SmartExpenseColumns.EXPENSE_TYPE_SORT_ORDER.equals(currentSortOrder)) {
            ((RadioButton) view.findViewById(R.id.radio_expense)).setChecked(true);
        } else if(Expense.SmartExpenseColumns.VENDOR_SORT_ORDER.equals(currentSortOrder)) {
            ((RadioButton) view.findViewById(R.id.radio_vendor)).setChecked(true);
        }

        group.setOnCheckedChangeListener(this);
        getDialog().setTitle(R.string.dialog_sort_expenses_by);

        return view;
    }

    /**
     * <p>Called when the checked radio button has changed. When the
     * selection is cleared, checkedId is -1.</p>
     *
     * @param group     the group in which the checked radio button has changed
     * @param checkedId the unique identifier of the newly checked radio button
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        String sortOrder = Expense.SmartExpenseColumns.DATE_NEWEST_SORT_ORDER;

        if (checkedId == R.id.radio_date_newest) {
            sortOrder = Expense.SmartExpenseColumns.DATE_NEWEST_SORT_ORDER;
        } else if (checkedId == R.id.radio_date_oldest) {
            sortOrder = Expense.SmartExpenseColumns.DATE_OLDEST_SORT_ORDER;
        } else if (checkedId == R.id.radio_amount_lowest) {
            sortOrder = Expense.SmartExpenseColumns.AMOUNT_LOWEST_SORT_ORDER;
        } else if (checkedId == R.id.radio_amount_highest) {
            sortOrder = Expense.SmartExpenseColumns.AMOUNT_HIGHEST_SORT_ORDER;
        } else if (checkedId == R.id.radio_expense) {
            sortOrder = Expense.SmartExpenseColumns.EXPENSE_TYPE_SORT_ORDER;
        } else if (checkedId == R.id.radio_vendor) {
            sortOrder = Expense.SmartExpenseColumns.VENDOR_SORT_ORDER;
        }

        SortExpenseDialogListener listener = (SortExpenseDialogListener) getActivity();
        listener.onSortCriteriaSelected(sortOrder);

        this.dismiss();
    }
}
