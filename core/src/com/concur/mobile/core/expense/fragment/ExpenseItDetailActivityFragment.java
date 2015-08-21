package com.concur.mobile.core.expense.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.expenseit.ExpenseItReceipt;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragment;

import java.util.Calendar;

/**
 * @author Elliott Jacobsen-Watts
 */

public class ExpenseItDetailActivityFragment extends PlatformFragment {

    public static final String CLS_TAG = ExpenseItDetailActivityFragment.class.getSimpleName();

    public static final int VIEW_PROCESSING_EXPENSEIT_ITEM_DETAILS = 16;

    private static final String EXPENSEIT_ITEM = "EXPENSEIT_ITEM";

    private int eta;
    private Calendar dateCreated;
    private long receiptId = 0;
    private ExpenseItReceipt expenseItReceipt;

    View fragmentView;

    private boolean isInErrorState = false;

    public interface ExpenseItDetailsViewReceiptCallback {
        void initializeViewReceipt(long receiptId);
    }

    protected ExpenseItDetailsViewReceiptCallback callbackActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof ExpenseItDetailsViewReceiptCallback)) {
            throw new IllegalArgumentException("Activity should implement DetailsCallback!");
        }
        callbackActivity = (ExpenseItDetailsViewReceiptCallback) activity;
    }

    public final static ExpenseItDetailActivityFragment newInstance(ExpenseItReceipt item){

        ExpenseItDetailActivityFragment dialog = new ExpenseItDetailActivityFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXPENSEIT_ITEM, item);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        expenseItReceipt = (ExpenseItReceipt) getArguments().getSerializable(EXPENSEIT_ITEM);
        isInErrorState = expenseItReceipt.isInErrorState();

        fragmentView = inflater.inflate(R.layout.fragment_expense_it_detail, container, false);
        buildView();

        return fragmentView;
    }

    public void buildView() {

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (expenseItReceipt == null || expenseItReceipt.getCreatedAt() == null) {
            return;
        }

        eta = expenseItReceipt.getEta();
        dateCreated = expenseItReceipt.getCreatedAt();
        receiptId = expenseItReceipt.getId();

        if (fragmentView != null) {
            getViewReceiptTransition();
            setUploadDateLabelAndFieldValues();
            setCommentLabelAndFieldValues();

            if (isInErrorState) {
                // set date sent for processing
                setDateSentForProcessingLabelAndFieldValues();
                setErrorNoteAndMessage();
            } else {
                // set eta
                setEtaLabelAndFieldValues();
            }

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView could not find the fragment!");
        }

    }

    private void setErrorNoteAndMessage() {
        TextView note = (TextView) fragmentView.findViewById(R.id.expenseit_analyzing_note);
        note.setText(R.string.expenseit_failure_note);

        LinearLayout message = (LinearLayout) fragmentView.findViewById(R.id.expenseit_error_message_field);
        message.setVisibility(View.VISIBLE);
    }

    private void setUploadDateLabelAndFieldValues() {
        View uploadedField = fragmentView.findViewById(R.id.expenseit_capture_date);
        TextView uploadedLabel = (TextView) uploadedField.findViewById(R.id.field_name);
        uploadedLabel.setText(R.string.expenseit_details_label_uploaded);

        String fmtDate = FormatUtil.SHORT_DAY_SHORT_MONTH_YEAR_TIME_WITH_SEPARATOR.format(dateCreated.getTime());

        ViewUtil.setTextViewText(fragmentView, R.id.expenseit_capture_date, R.id.field_value, fmtDate, true);
    }

    private void setDateSentForProcessingLabelAndFieldValues() {
        View processingField = fragmentView.findViewById(R.id.expenseit_details_processing_time);
        TextView processingLabel = (TextView) processingField.findViewById(R.id.field_name);
        processingLabel.setText(R.string.expenseit_details_label_sent_for_analysis_time);

        String fmtDate = FormatUtil.SHORT_DAY_SHORT_MONTH_YEAR_TIME_WITH_SEPARATOR.format(dateCreated.getTime());

        ViewUtil.setTextViewText(fragmentView, R.id.expenseit_details_processing_time, R.id.field_value, fmtDate, true);
    }

    private void setEtaLabelAndFieldValues() {
        // format ETA
        StringBuilder strBld = new StringBuilder(getString(R.string.expenseit_details_value_processing_time));
        strBld.append(" ");
        String fmtEta = FormatUtil.getEtaToString(getActivity(), eta);
        strBld.append(fmtEta);

        View uploadedField = fragmentView.findViewById(R.id.expenseit_details_processing_time);
        TextView uploadedLabel = (TextView) uploadedField.findViewById(R.id.field_name);
        uploadedLabel.setText(R.string.expenseit_details_label_processing_time);
        ViewUtil.setTextViewText(fragmentView, R.id.expenseit_details_processing_time, R.id.field_value, strBld.toString(), true);
    }

    private void setCommentLabelAndFieldValues() {
        View commentField = fragmentView.findViewById(R.id.expenseit_comment);
        TextView commentLabel = (TextView) commentField.findViewById(R.id.field_name);
        commentLabel.setText(R.string.comment);

        commentField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentDialogFragment df = new CommentDialogFragment();
                df.show(getActivity().getSupportFragmentManager(), CommentDialogFragment.DIALOG_FRAGMENT_ID);
            }
        });

    }

    private void getViewReceiptTransition() {
        View viewReceipt = fragmentView.findViewById(R.id.header_expenseit_receipt);
        if (viewReceipt != null) {
            viewReceipt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callbackActivity.initializeViewReceipt(receiptId);
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView view receipt button not found!");
        }
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
                    // TODO: save comment to the expenseit service.
                }
            });
            dlgBldr.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO: Cancel button. Don't save whatever the user types.
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
