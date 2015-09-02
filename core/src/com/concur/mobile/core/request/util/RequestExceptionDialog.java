package com.concur.mobile.core.request.util;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Point;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.concur.core.R;
import com.concur.mobile.core.request.adapter.ExceptionRequestAdapter;
import com.concur.mobile.platform.request.dto.RequestExceptionDTO;

import java.util.List;

/**
 * Created by ECollomb on 23/07/2015.
 */
public class RequestExceptionDialog<T extends Activity & View.OnClickListener> extends Dialog
        implements View.OnClickListener {

    private Button submitButton, backButton;

    private ExceptionRequestAdapter adapter;
    private T currentActivity;
    private List<RequestExceptionDTO> exceptionsList;

    public RequestExceptionDialog(T a, List<RequestExceptionDTO> exceptionsList_) {
        super(a, R.style.RequestExceptionDialog);
        currentActivity = a;

        // Construct the data source
        exceptionsList = exceptionsList_;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** set properties */
        Window window = this.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        /** load view */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.request_exception_dialog);
        submitButton = (Button) findViewById(R.id.btn_submit);
        backButton = (Button) findViewById(R.id.btn_back);
        submitButton.setOnClickListener(this);
        backButton.setOnClickListener(this);

        /** layout size */
        LinearLayout layout = (LinearLayout) findViewById(R.id.exceptionDialogLayout);
        Display display = currentActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y / 2;
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
        layout.setLayoutParams(parms);

        /** populate with Exceptions */
        adapter = new ExceptionRequestAdapter(this.getContext(), exceptionsList);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.requestExceptionListView);
        listView.setAdapter(adapter);

        /** Buttons */
        // check if Blocking
        boolean isBlocking = false;
        for (int i = 0; i < exceptionsList.size(); i++)
            if (exceptionsList.get(i).getLevel() == RequestExceptionDTO.ExceptionLevel.BLOCKING)
                isBlocking = true;

        /** Do not show submit if blocking Exceptions */
        if (isBlocking) {
            RelativeLayout layoutSubmit = (RelativeLayout) findViewById(R.id.layoutSubmit);
            layoutSubmit.setVisibility(View.GONE);
        }

    }

    @Override public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            this.cancel();
        } else if (v.getId() == R.id.btn_submit) {
            this.hide();
            currentActivity.onClick(v);
        }
    }

    private void addException(RequestExceptionDTO requestExceptionDTO) {
        adapter.add(requestExceptionDTO);
    }
}

