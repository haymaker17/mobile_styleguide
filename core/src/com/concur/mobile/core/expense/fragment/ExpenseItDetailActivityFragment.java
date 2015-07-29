package com.concur.mobile.core.expense.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragment;

import java.util.Calendar;

/**
 * @author Elliott Jacobsen-Watts
 */

public class ExpenseItDetailActivityFragment extends PlatformFragment {

    public static final String CLS_TAG = ExpenseItDetailActivityFragment.class.getSimpleName();

    public static final int VIEW_PROCESSING_EXPENSEIT_ITEM_DETAILS = 16;

    public static final String EXTRA_EXPENSEIT_ETA_KEY = "expenseit.eta";

    private int eta;
    private String receiptURL; // need to add link to get this!
    private Calendar date;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_expense_it_detail, container, false);
        buildView(view);

        return view;
    }

    public void buildView(View view) {

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Bundle data = getActivity().getIntent().getExtras();

        date = (Calendar) data.get(Const.EXTRA_EXPENSE_TRANSACTION_DATE_KEY);
        eta = data.getInt(EXTRA_EXPENSEIT_ETA_KEY);
        receiptURL = data.getString(Const.EXTRA_EXPENSE_RECEIPT_URL_KEY);

        if (view != null) {
            // set URL / View Receipt button
            getViewReceiptTransition(view);
            // set date uploaded
            setUploadDateLabelAndFieldValues(view);
            // set eta
            setEtaLabelAndFieldValues(view);
            // set comment - this will take some work.
            setCommentLabelAndFieldValues(view);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView could not find the fragment!");
        }

    }

    private void setUploadDateLabelAndFieldValues(View view) {
        View uploadedField = view.findViewById(R.id.expenseit_capture_date);
        TextView uploadedLabel = (TextView) uploadedField.findViewById(R.id.field_name);
        uploadedLabel.setText(R.string.expenseit_details_label_uploaded);

        String fmtDate =
//                FormatUtil.SHORT_DAY_YEAR_DISPLAY_NO_COMMA.format(date.getTime()); // this is the existing format closest to spec.
                FormatUtil.SHORT_DAY_FULL_MONTH_YEAR_DISPLAY_NO_COMMA.format(date.getTime());

        ViewUtil.setTextViewText(view, R.id.expenseit_capture_date, R.id.field_value, fmtDate, true);
    }

    private void setEtaLabelAndFieldValues(View view) {
        // format ETA
        String fmtEta = FormatUtil.getEtaToString(getActivity(), eta);

        View uploadedField = view.findViewById(R.id.expenseit_details_processing_time);
        TextView uploadedLabel = (TextView) uploadedField.findViewById(R.id.field_name);
        uploadedLabel.setText(R.string.expenseit_details_label_processing_time);
        ViewUtil.setTextViewText(view, R.id.expenseit_details_processing_time, R.id.field_value, fmtEta, true);
    }

    private void setCommentLabelAndFieldValues(View view) {
        View commentField = view.findViewById(R.id.expenseit_comment);
        TextView commentLabel = (TextView) commentField.findViewById(R.id.field_name);
        commentLabel.setText(R.string.comment);

        if (commentField != null) {
            commentField.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommentDialogFragment df = new CommentDialogFragment();
                    df.show(getActivity().getSupportFragmentManager(), CommentDialogFragment.DIALOG_FRAGMENT_ID);
                }
            });
        }
    }

    private void getViewReceiptTransition(View view) {
        View viewReceipt = view.findViewById(R.id.header_expenseit_receipt);
        if (viewReceipt != null) {
            viewReceipt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Hook up receipt View.
                    Toast.makeText(getActivity(), "Receipt URL: " + receiptURL, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView view receipt button not found!");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public static class CommentDialogFragment extends DialogFragment {

        public CommentDialogFragment() {
            super();
        }

        public static final String DIALOG_FRAGMENT_ID = "COMMENT_DIALOG";

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(getActivity());
            dlgBldr.setTitle(getText(R.string.comment));
            dlgBldr.setCancelable(true);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO: save comment
                    Toast.makeText(getActivity(), "You hit the OK button!", Toast.LENGTH_SHORT).show();
                }
            });
            dlgBldr.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO: Cancel button
                    Toast.makeText(getActivity(), "You hit the cancel button!", Toast.LENGTH_SHORT).show();
                }
            });

            EditText textEdit = new EditText(getActivity());
            textEdit.setMinLines(3);
            textEdit.setMaxLines(3);
            textEdit.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            dlgBldr.setView(textEdit);
            return dlgBldr.create();
        }
    }
}
