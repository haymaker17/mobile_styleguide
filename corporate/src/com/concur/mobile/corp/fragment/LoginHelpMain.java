package com.concur.mobile.corp.fragment;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.breeze.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.dialog.DialogFragmentFactory;
import com.concur.mobile.core.fragment.BaseFragment;

public class LoginHelpMain extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.login_help_main, null);

        // Setup the 'work email' popup
        TextView helpTV = (TextView) root.findViewById(R.id.emailHelpText);
        String workEmailText = getActivity().getText(R.string.login_help_work_email).toString();

        StringBuilder workEmailColored = new StringBuilder("<font color=#0078C8>");
        workEmailColored.append(workEmailText).append("</font>");

        String helpText = Format.localizeText(getActivity(), R.string.login_help_email_prompt,
                workEmailColored.toString());

        helpTV.setText(Html.fromHtml(helpText));

        helpTV.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                DialogFragmentFactory.getAlertOkayInstance("", R.string.login_help_email_popup).show(
                        getFragmentManager(), null);
            }
        });

        return root;
    }
}
