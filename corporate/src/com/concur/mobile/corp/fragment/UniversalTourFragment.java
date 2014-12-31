package com.concur.mobile.corp.fragment;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.fragment.BaseFragment;

public class UniversalTourFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.universal_tour, null);

        // Tweak the action bar
        final ActionBar actionBar = getBaseActivity().getSupportActionBar();
        actionBar.setTitle(R.string.test_drive_welcome_title);

        // set welcome text
        TextView welcomeText = (TextView) root.findViewById(R.id.welcomeText);
        String cnqrMobText = getActivity().getText(R.string.universal_tour_welcome_text_concur_mobile).toString();

        StringBuilder cnqrMobTextColored = new StringBuilder("<font color=#FFFFFF>");
        cnqrMobTextColored.append(cnqrMobText).append("</font>");

        String moreText = getActivity().getText(R.string.universal_tour_welcome_text).toString();
        String helpText = cnqrMobTextColored.append(' ').append(moreText).toString();

        welcomeText.setText(Html.fromHtml(helpText));

        // setup travel icon and text
        View view = root.findViewById(R.id.universal_tour_topic_flight);
        setIconText(view, R.drawable.img_flight, new int[] { R.string.universal_tour_travel,
                R.string.universal_tour_travel_more });

        // setup receipt icon and text
        view = root.findViewById(R.id.universal_tour_topic_snap);
        setIconText(view, R.drawable.img_camera, new int[] { R.string.universal_tour_receipt,
                R.string.universal_tour_receipt_more });

        // setup expense icon and text
        view = root.findViewById(R.id.universal_tour_topic_report);
        setIconText(view, R.drawable.img_expense, new int[] { R.string.universal_tour_expense,
                R.string.universal_tour_expense_more });

        // set up feature text
        TextView featureText = (TextView) root.findViewById(R.id.feature_text);
        featureText.setText(getActivity().getText(R.string.universal_tour_feature_text).toString());

        return root;
    }

    /**
     * set image and message for the universal tour items
     * 
     * @param view
     *            : parent view
     * @param imgRes
     *            : image resource id
     * @param txtRes
     *            : message resource id
     */
    private void setIconText(View view, int imgRes, int... txtRes) {
        // set image view
        ImageView img = (ImageView) view.findViewById(R.id.universal_tour_img);
        img.setImageResource(imgRes);

        // set message
        TextView msg = (TextView) view.findViewById(R.id.universal_tour_text);
        String msgTxt = getActivity().getText(txtRes[0]).toString();

        StringBuilder strbldr = new StringBuilder("<font color=#FFFFFF>");
        strbldr.append(msgTxt).append("</font>");

        String moreTxt = getActivity().getText(txtRes[1]).toString();
        strbldr.append(' ');
        strbldr.append(moreTxt);

        msg.setText(Html.fromHtml(strbldr.toString()));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }
}
