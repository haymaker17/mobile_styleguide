package com.concur.mobile.core.expense.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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

    protected static final int RESULT_SAVE_COMMENT = 2610;

    protected static final int RESULT_CANCELLED = 2611;

    private static final String NOTE_KEY = "NOTE_KEY";

    private int eta;
    private Calendar dateCreated;
    private long receiptId = 0;
    private String noteBody;
    private ExpenseItReceipt expenseItReceipt;

    private View fragmentView;

    private boolean isInErrorState = false;

    public interface ExpenseItDetailsViewReceiptCallback {
        void initializeViewReceipt(long receiptId);
        void saveComment(String comment);
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
        buildView(savedInstanceState);

        return fragmentView;
    }

    public void buildView(Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (expenseItReceipt == null || expenseItReceipt.getCreatedAt() == null) {
            return;
        }

        eta = expenseItReceipt.getEta();
        dateCreated = expenseItReceipt.getCreatedAt();
        receiptId = expenseItReceipt.getId();

        if (savedInstanceState != null && savedInstanceState.containsKey(NOTE_KEY)) {
            noteBody = savedInstanceState.getString(NOTE_KEY);
        } else {
            noteBody = expenseItReceipt.getNote();
        }

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(NOTE_KEY, noteBody);
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
        if (noteBody != null) {
            ViewUtil.setTextViewText(fragmentView, R.id.expenseit_comment, R.id.field_value, noteBody, true);
        }

        commentField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment df = CommentDialogFragment.newInstance(noteBody);
                df.setTargetFragment(ExpenseItDetailActivityFragment.this, RESULT_SAVE_COMMENT);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .add(df, CommentDialogFragment.DIALOG_FRAGMENT_ID)
                        .commit();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_SAVE_COMMENT) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onActivityResult: resultCode is RESULT_SAVE_COMMENT.");
            noteBody = data.getStringExtra(CommentDialogFragment.NEW_COMMENT_KEY);
            ViewUtil.setTextViewText(fragmentView, R.id.expenseit_comment, R.id.field_value, noteBody, true);
            callbackActivity.saveComment(noteBody);
        } else if (resultCode == RESULT_CANCELLED) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onActivityResult: resultCode is RESULT_CANCELLED.");
        }
    }

    public static class CommentDialogFragment extends DialogFragment {

        public CommentDialogFragment() { }

        public static final String DIALOG_FRAGMENT_ID = "COMMENT_DIALOG";

        public static final String NEW_COMMENT_KEY = "NEW_COMMENT";

        private TextView charCount;

        private EditText textBox;

        private View dialogView;

        public static final int EXPENSEIT_MAX_SUPPORTED_CHARS = 500;

        private final TextWatcher mEditTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String charCountInt = String.valueOf(EXPENSEIT_MAX_SUPPORTED_CHARS - s.length());
                if (charCount != null) {
                    charCount.setText(getString(R.string.expenseit_char_limit, charCountInt));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                lastTextChanged = s.toString();
            }
        };

        public String lastTextChanged;

        public static String COMMENT_KEY = "comment.key";

        public static CommentDialogFragment newInstance(String comment) {
            CommentDialogFragment fragment = new CommentDialogFragment();
            Bundle args = new Bundle();
            args.putString(CommentDialogFragment.COMMENT_KEY, comment);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            dialogView = inflater.inflate(R.layout.expenseit_comment_dialog_layout, null);
            dlgBldr.setView(dialogView);
            setCommentLayout();
            dlgBldr.setTitle(getText(R.string.comment));
            dlgBldr.setCancelable(true);
            dlgBldr.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    lastTextChanged = null;
                }
            });
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String comment = textBox.getText().toString().trim();
                    getActivity().getIntent().putExtra(NEW_COMMENT_KEY, comment);
                    getTargetFragment().onActivityResult(getTargetRequestCode(),
                            ExpenseItDetailActivityFragment.RESULT_SAVE_COMMENT, getActivity().getIntent());
                    lastTextChanged = null;
                    // TODO: save comment to the expenseit service.
                }
            });
            dlgBldr.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getTargetFragment().onActivityResult(getTargetRequestCode(),
                            ExpenseItDetailActivityFragment.RESULT_CANCELLED, getActivity().getIntent());
                    lastTextChanged = null;
                }
            });
            return dlgBldr.create();
        }

        private void setCommentLayout() {
            String comment = getArguments().getString(COMMENT_KEY);
            int charCountRemaining = EXPENSEIT_MAX_SUPPORTED_CHARS;
            textBox = (EditText) dialogView.findViewById(R.id.expenseit_text_comment_edit_text);
            charCount = (TextView) dialogView.findViewById(R.id.expenseit_char_count_text_view);
            textBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(EXPENSEIT_MAX_SUPPORTED_CHARS)});
            textBox.addTextChangedListener(mEditTextWatcher);
            textBox.setMaxLines(3);
            if (comment != null) {
                charCountRemaining = (EXPENSEIT_MAX_SUPPORTED_CHARS - comment.length());
                textBox.setText(comment);
            }
            charCount.setText(getString(R.string.expenseit_char_limit, String.valueOf(charCountRemaining)));
            textBox.setSelection(textBox.getText().length());
        }
    }
}
