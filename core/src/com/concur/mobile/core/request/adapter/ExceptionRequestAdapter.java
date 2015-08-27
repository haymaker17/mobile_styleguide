package com.concur.mobile.core.request.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.concur.core.R;
import com.concur.mobile.platform.request.dto.RequestExceptionDTO;

import java.util.List;

/**
 * Created by ECollomb on 23/07/2015.
 */
public class ExceptionRequestAdapter extends ArrayAdapter<RequestExceptionDTO> {

    public ExceptionRequestAdapter(Context context, List<RequestExceptionDTO> exceptionsDTO) {
        super(context, 0, exceptionsDTO);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RequestExceptionDTO exceptionDTO = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.request_exception_dialog_list_row, parent, false);
        }
        // Lookup view for data population
        ImageView exceptionIcon = (ImageView) convertView.findViewById(R.id.requestExceptionDialogIcon);
        TextView exceptionTitle = (TextView) convertView.findViewById(R.id.requestExceptionDialogTitle);
        TextView exceptionMessage = (TextView) convertView.findViewById(R.id.requestExceptionDialogMessage);

        // Populate the data into the template view using the data object

        if (exceptionDTO.getLevel() == RequestExceptionDTO.ExceptionLevel.BLOCKING)
            exceptionIcon.setImageResource(R.drawable.icon_redex);
        else if (exceptionDTO.getLevel() == RequestExceptionDTO.ExceptionLevel.NON_BLOCKING)
            exceptionIcon.setImageResource(R.drawable.icon_yellowex);

        exceptionTitle.setText(exceptionDTO.getTitle());
        exceptionMessage.setText(exceptionDTO.getMessage());

        // Return the completed view to render on screen
        return convertView;
    }

}
