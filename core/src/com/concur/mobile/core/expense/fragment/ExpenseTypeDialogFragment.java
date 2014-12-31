package com.concur.mobile.core.expense.fragment;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.concur.core.R;
import com.concur.mobile.core.expense.activity.ExpenseTypeSpinnerAdapter;
import com.concur.mobile.core.expense.data.ExpenseType;

/**
 * Reuseable Expense Type selection dialog fragment
 * 
 * @author yiwenw
 * 
 */
public class ExpenseTypeDialogFragment extends DialogFragment implements OnItemClickListener {

    public static final String DIALOG_FRAGMENT_ID = "ExpenseTypeDialog";

    public interface OnExpenseTypeSelectionListener {

        public void selectExpenseType(ExpenseType expType);
    }

    private OnExpenseTypeSelectionListener expListener;
    private ExpenseTypeSpinnerAdapter expTypeAdapter;
    private ArrayList<ExpenseType> expenseTypes;
    private boolean showSkipButton;

    @Override
    // public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.expense_type_prompt);
        // getDialog().setTitle(R.string.expense_type_prompt);
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View customView = inflater.inflate(R.layout.expense_mru, null);
        expTypeAdapter = new ExpenseTypeSpinnerAdapter(getActivity(), null);

        expTypeAdapter.addQuickExpenses(expenseTypes);

        ListView customListView = (ListView) customView.findViewById(R.id.list_expense_mru);
        EditText customEditText = (EditText) customView.findViewById(R.id.list_search_mru);
        customListView.setAdapter(expTypeAdapter);

        customListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        customEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                expTypeAdapter.clearSearchFilter();
                expTypeAdapter.getFilter().filter(s);

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        customListView.setOnItemClickListener(this);

        builder.setView(customView);

        if (showSkipButton) {
            builder.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (expListener != null) {
                        expListener.selectExpenseType(null);
                    }

                    dismiss();
                }
            });

        }
        // return customView;
        return builder.create();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int which, long id) {
        if (which != -1) {
            Object selExpObj = expTypeAdapter.getItem(which);
            if (selExpObj instanceof ExpenseType && this.expListener != null) {
                this.expListener.selectExpenseType((ExpenseType) selExpObj);
            }
        }
        this.dismiss();
    }

    public void setExpTypeSelectionListener(OnExpenseTypeSelectionListener listener) {
        this.expListener = listener;
    }

    public void setExpenseTypeList(ArrayList<ExpenseType> list) {
        this.expenseTypes = list;
    }

    public void setShowSkipButton(boolean flag) {
        this.showSkipButton = flag;
    }
}
